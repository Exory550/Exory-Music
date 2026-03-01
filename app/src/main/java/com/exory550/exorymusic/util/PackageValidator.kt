package com.exory550.exorymusic.util

import android.Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
import android.Manifest.permission.MEDIA_CONTENT_CONTROL
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.os.Process
import android.support.v4.media.session.MediaSessionCompat
import android.util.Base64
import android.util.Log
import androidx.annotation.XmlRes
import androidx.media.MediaBrowserServiceCompat
import com.exory550.exorymusic.BuildConfig
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class PackageValidator(
        context: Context,
        @XmlRes xmlResId: Int
) {
    private val context: Context
    private val packageManager: PackageManager

    private val certificateWhitelist: Map<String, KnownCallerInfo>
    private val platformSignature: String

    private val callerChecked = mutableMapOf<String, Pair<Int, Boolean>>()

    init {
        val parser = context.resources.getXml(xmlResId)
        this.context = context.applicationContext
        this.packageManager = this.context.packageManager

        certificateWhitelist = buildCertificateWhitelist(parser)
        platformSignature = getSystemSignature()
    }

    fun isKnownCaller(callingPackage: String, callingUid: Int): Boolean {
        val (checkedUid, checkResult) = callerChecked[callingPackage] ?: Pair(0, false)
        if (checkedUid == callingUid) {
            return checkResult
        }

        val callerPackageInfo = buildCallerInfo(callingPackage)
                ?: throw IllegalStateException("Caller wasn't found in the system?")

        if (callerPackageInfo.uid != callingUid) {
            throw IllegalStateException("Caller's package UID doesn't match caller's actual UID?")
        }

        val callerSignature = callerPackageInfo.signature
        val isPackageInWhitelist = certificateWhitelist[callingPackage]?.signatures?.first {
            it.signature == callerSignature
        } != null

        val isCallerKnown = when {
            callingUid == Process.myUid() -> true
            isPackageInWhitelist -> true
            callingUid == Process.SYSTEM_UID -> true
            callerSignature == platformSignature -> true
            callerPackageInfo.permissions.contains(MEDIA_CONTENT_CONTROL) -> true
            callerPackageInfo.permissions.contains(BIND_NOTIFICATION_LISTENER_SERVICE) -> true
            else -> false
        }

        if (!isCallerKnown) {
            logUnknownCaller(callerPackageInfo)
        }

        callerChecked[callingPackage] = Pair(callingUid, isCallerKnown)
        return isCallerKnown
    }

    private fun logUnknownCaller(callerPackageInfo: CallerPackageInfo) {
        if (BuildConfig.DEBUG && callerPackageInfo.signature != null) {
            Log.i(TAG, "PackageValidator call" + callerPackageInfo.name + callerPackageInfo.packageName + callerPackageInfo.signature)
        }
    }

    private fun buildCallerInfo(callingPackage: String): CallerPackageInfo? {
        val packageInfo = getPackageInfo(callingPackage) ?: return null

        val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
        val uid = packageInfo.applicationInfo.uid
        val signature = getSignature(packageInfo)

        val requestedPermissions = packageInfo.requestedPermissions
        val permissionFlags = packageInfo.requestedPermissionsFlags
        val activePermissions = mutableSetOf<String>()
        requestedPermissions?.forEachIndexed { index, permission ->
            if (permissionFlags[index] and REQUESTED_PERMISSION_GRANTED != 0) {
                activePermissions += permission
            }
        }

        return CallerPackageInfo(appName, callingPackage, uid, signature, activePermissions.toSet())
    }

    @Suppress("Deprecation")
    @SuppressLint("PackageManagerGetSignatures")
    private fun getPackageInfo(callingPackage: String): PackageInfo? =
            packageManager.getPackageInfo(callingPackage,
                    PackageManager.GET_SIGNATURES or PackageManager.GET_PERMISSIONS)

    @Suppress("deprecation")
    private fun getSignature(packageInfo: PackageInfo): String? {
        return if (packageInfo.signatures == null || packageInfo.signatures.size != 1) {
            null
        } else {
            val certificate = packageInfo.signatures[0].toByteArray()
            getSignatureSha256(certificate)
        }
    }

    private fun buildCertificateWhitelist(parser: XmlResourceParser): Map<String, KnownCallerInfo> {

        val certificateWhitelist = LinkedHashMap<String, KnownCallerInfo>()
        try {
            var eventType = parser.next()
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    val callerInfo = when (parser.name) {
                        "signing_certificate" -> parseV1Tag(parser)
                        "signature" -> parseV2Tag(parser)
                        else -> null
                    }

                    callerInfo?.let { info ->
                        val packageName = info.packageName
                        val existingCallerInfo = certificateWhitelist[packageName]
                        if (existingCallerInfo != null) {
                            existingCallerInfo.signatures += callerInfo.signatures
                        } else {
                            certificateWhitelist[packageName] = callerInfo
                        }
                    }
                }

                eventType = parser.next()
            }
        } catch (xmlException: XmlPullParserException) {
            Log.e(TAG, "Could not read allowed callers from XML.", xmlException)
        } catch (ioException: IOException) {
            Log.e(TAG, "Could not read allowed callers from XML.", ioException)
        }

        return certificateWhitelist
    }

    private fun parseV1Tag(parser: XmlResourceParser): KnownCallerInfo {
        val name = parser.getAttributeValue(null, "name")
        val packageName = parser.getAttributeValue(null, "package")
        val isRelease = parser.getAttributeBooleanValue(null, "release", false)
        val certificate = parser.nextText().replace(WHITESPACE_REGEX, "")
        val signature = getSignatureSha256(certificate)

        val callerSignature = KnownSignature(signature, isRelease)
        return KnownCallerInfo(name, packageName, mutableSetOf(callerSignature))
    }

    private fun parseV2Tag(parser: XmlResourceParser): KnownCallerInfo {
        val name = parser.getAttributeValue(null, "name")
        val packageName = parser.getAttributeValue(null, "package")

        val callerSignatures = mutableSetOf<KnownSignature>()
        var eventType = parser.next()
        while (eventType != XmlResourceParser.END_TAG) {
            val isRelease = parser.getAttributeBooleanValue(null, "release", false)
            val signature = parser.nextText().replace(WHITESPACE_REGEX, "")
                .lowercase(Locale.getDefault())
            callerSignatures += KnownSignature(signature, isRelease)

            eventType = parser.next()
        }

        return KnownCallerInfo(name, packageName, callerSignatures)
    }

    private fun getSystemSignature(): String =
            getPackageInfo(ANDROID_PLATFORM)?.let { platformInfo ->
                getSignature(platformInfo)
            } ?: throw IllegalStateException("Platform signature not found")

    private fun getSignatureSha256(certificate: String): String {
        return getSignatureSha256(Base64.decode(certificate, Base64.DEFAULT))
    }

    private fun getSignatureSha256(certificate: ByteArray): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA256")
        } catch (noSuchAlgorithmException: NoSuchAlgorithmException) {
            Log.e(TAG, "No such algorithm: $noSuchAlgorithmException")
            throw RuntimeException("Could not find SHA256 hash algorithm", noSuchAlgorithmException)
        }
        md.update(certificate)

        return md.digest().joinToString(":") { String.format("%02x", it) }
    }

    private data class KnownCallerInfo(
        val name: String,
        val packageName: String,
        val signatures: MutableSet<KnownSignature>
    )

    private data class KnownSignature(
        val signature: String,
        val release: Boolean
    )

    private data class CallerPackageInfo(
        val name: String,
        val packageName: String,
        val uid: Int,
        val signature: String?,
        val permissions: Set<String>
    )
}

private const val TAG = "PackageValidator"
private const val ANDROID_PLATFORM = "android"
private val WHITESPACE_REGEX = "\\s|\\n".toRegex()

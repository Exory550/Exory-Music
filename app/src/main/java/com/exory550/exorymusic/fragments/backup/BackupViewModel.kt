package com.exory550.exorymusic.fragments.backup

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.exory550.exorymusic.activities.MainActivity
import com.exory550.exorymusic.helper.BackupContent
import com.exory550.exorymusic.helper.BackupHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess

class BackupViewModel : ViewModel() {
    private val backupsMutableLiveData = MutableLiveData<List<File>>()
    val backupsLiveData: LiveData<List<File>> = backupsMutableLiveData

    fun loadBackups() {
        BackupHelper.getBackupRoot().listFiles { _, name ->
            return@listFiles name.endsWith(BackupHelper.BACKUP_EXTENSION)
        }?.toList()?.let {
            backupsMutableLiveData.value = it
        }
    }

    suspend fun restoreBackup(activity: Activity, inputStream: InputStream?, contents: List<BackupContent>) {
        BackupHelper.restoreBackup(activity, inputStream, contents)
        if (contents.contains(BackupContent.SETTINGS) or contents.contains(BackupContent.CUSTOM_ARTIST_IMAGES)) {
            withContext(Dispatchers.Main) {
                val intent = Intent(
                    activity,
                    MainActivity::class.java
                )
                activity.startActivity(intent)
                exitProcess(0)
            }
        }
    }
}

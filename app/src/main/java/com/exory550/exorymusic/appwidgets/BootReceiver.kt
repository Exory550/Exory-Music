package com.exory550.exorymusic.appwidgets

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.exory550.exorymusic.service.MusicService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val widgetManager = AppWidgetManager.getInstance(context)

        if (widgetManager.getAppWidgetIds(
                ComponentName(
                    context, AppWidgetBig::class.java
                )
            ).isNotEmpty() || widgetManager.getAppWidgetIds(
                ComponentName(
                    context, AppWidgetClassic::class.java
                )
            ).isNotEmpty() || widgetManager.getAppWidgetIds(
                ComponentName(
                    context, AppWidgetSmall::class.java
                )
            ).isNotEmpty() || widgetManager.getAppWidgetIds(
                ComponentName(
                    context, AppWidgetCard::class.java
                )
            ).isNotEmpty()
        ) {
            val serviceIntent = Intent(context, MusicService::class.java)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                context.startService(serviceIntent)
            }
        }
    }
}


package com.bpitindia.timetable.receivers

import android.content.BroadcastReceiver
import android.content.Intent
import android.app.NotificationManager
import android.content.Context
import java.util.*

class NotificationDismissButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // if you want cancel notification
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        // if you want cancel notification
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Objects.requireNonNull(manager).cancel(notificationId)
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
    }
}
package com.bpitindia.timetable.receivers

import android.content.BroadcastReceiver
import android.content.Intent
import com.bpitindia.timetable.utils.PreferenceUtil
import android.app.AlarmManager
import android.content.Context
import com.bpitindia.timetable.utils.NotificationUtil


class DailyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null) {
            if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, ignoreCase = true)) {
                // Set the alarm here.
                if (PreferenceUtil.isAlarmOn(context)) {
                    val times = PreferenceUtil.getAlarmTime(context)
                    PreferenceUtil.setRepeatingAlarm(
                        context,
                        DailyReceiver::class.java,
                        times[0],
                        times[1],
                        times[2],
                        DailyReceiverID,
                        AlarmManager.INTERVAL_DAY
                    )
                } else PreferenceUtil.cancelAlarm(
                    context,
                    DailyReceiver::class.java,
                    DailyReceiverID
                )
                NotificationUtil.sendNotificationSummary(context, false)
                return
            }
        }
        if (!PreferenceUtil.isAlarmOn(context)) {
            PreferenceUtil.cancelAlarm(context, DailyReceiver::class.java, DailyReceiverID)
        } else {
            NotificationUtil.sendNotificationSummary(context, true)
        }
    }

    companion object {
        const val DailyReceiverID = 10000
    }
}
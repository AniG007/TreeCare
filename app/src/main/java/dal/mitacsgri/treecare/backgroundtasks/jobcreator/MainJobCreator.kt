package dal.mitacsgri.treecare.backgroundtasks.jobcreator

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.backgroundtasks.jobs.DailyGoalNotificationJob
import dal.mitacsgri.treecare.backgroundtasks.jobs.TrophiesUpdateJob
import dal.mitacsgri.treecare.consts.FCM_NOTIFICATION_CHANNEL_ID
import dal.mitacsgri.treecare.screens.MainActivity

class MainJobCreator: JobCreator {

    override fun create(tag: String): Job? =
        when(tag) {
            TrophiesUpdateJob.TAG -> {
                TrophiesUpdateJob()
            }
            DailyGoalNotificationJob.TAG -> {
                DailyGoalNotificationJob()
            }
            else -> null
        }
}
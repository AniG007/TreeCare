package dal.mitacsgri.treecare.backgroundtasks.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.work.*
import com.google.common.reflect.Reflection.getPackageName
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.backgroundtasks.jobs.DailyGoalNotificationJob
import dal.mitacsgri.treecare.consts.DAILY_GOAL_NOTIFICATION_CHANNEL_ID
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import dal.mitacsgri.treecare.screens.MainActivity
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit.*
import kotlin.random.Random

class DailyNotificationWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams), KoinComponent {
    private val sharedPrefsRepository: SharedPreferencesRepository by inject()
    private val stepCountRepository: StepCountRepository by inject()
    private val TAG = "DailyGoalNotifWork"
    private val mConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    override fun doWork(): Result {
        Log.d(TAG, "DailyNotifWork")
        createNotificationChannel()

        stepCountRepository.getTodayStepCountData {
            createNotification(sharedPrefsRepository.getDailyStepsGoal() - it)
        }
        return Result.success()
    }

    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(DailyGoalNotificationJob.TAG, "Inside If")
            val serviceChannel = NotificationChannel(
                DAILY_GOAL_NOTIFICATION_CHANNEL_ID,
                "Daily goal notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(remainingSteps: Int) {
        Log.d(DailyGoalNotificationJob.TAG, "Creating Notif")
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationSound = Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.notification)

        val title = getNotificationTitle()
        val body = getNotificationBody(remainingSteps)

        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, DAILY_GOAL_NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        applicationContext.resources,
                        R.mipmap.ic_launcher_round
                    )
                )
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(4, notificationBuilder.build())
    }

    private fun getNotificationTitle(): String {
        //Log.d(DailyGoalNotificationJob.TAG, "getNotificationTitle")
        val titlesArray = arrayListOf<String>()

        titlesArray.add(applicationContext.getString(R.string.daily_goal_notification_title1))
        titlesArray.add(applicationContext.getString(R.string.daily_goal_notification_title7))
        titlesArray.add(applicationContext.getString(R.string.daily_goal_notification_title9))
        //titlesArray.add(applicationContext.getString(R.string.daily_goal_notification_title2))
        //titlesArray.add(applicationContext.getString(R.string.daily_goal_notification_title3))
        //titlesArray.add(applicationContext.getString(R.string.daily_goal_notification_title4))
        //titlesArray.add(applicationContext.getString(R.string.daily_goal_notification_title5))
        //titlesArray.add(applicationContext.getString(R.string.daily_goal_notification_title6))
        //titlesArray.add(applicationContext.getString(R.string.daily_goal_notification_title8))

        return titlesArray[Random.nextInt(0, 3)]
    }

    private fun getNotificationBody(remainingSteps: Int): CharSequence {
        //Log.d(DailyGoalNotificationJob.TAG, "getNotificationBody")
        return if (remainingSteps > 0)
            buildSpannedString {
                append("You are ")
                bold {
                    append("$remainingSteps steps ")
                }
                append("away from achieving today's goal. You will eventually meet your goal if you walk for about ")
                bold{append("${minutesOfWalking(remainingSteps)} minutes")}
            }
        else
            applicationContext.getString(R.string.goal_completed_notification)
    }
    private fun minutesOfWalking(remainingSteps: Int): Int{
        var minutesOfWalk =  remainingSteps / 100
        return minutesOfWalk
    }

}
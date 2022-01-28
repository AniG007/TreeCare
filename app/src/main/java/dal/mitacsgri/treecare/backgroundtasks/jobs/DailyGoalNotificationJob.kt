package dal.mitacsgri.treecare.backgroundtasks.jobs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobRequest
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.consts.DAILY_GOAL_NOTIFICATION_CHANNEL_ID
import dal.mitacsgri.treecare.consts.FCM_NOTIFICATION_CHANNEL_ID
import dal.mitacsgri.treecare.extensions.getMapFormattedDate
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import dal.mitacsgri.treecare.screens.MainActivity
import expandDailyGoalMapIfNeeded
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DailyGoalNotificationJob : DailyJob(), KoinComponent {

    private val sharedPrefsRepository: SharedPreferencesRepository by inject()
    private val stepCountRepository: StepCountRepository by inject()

    companion object {

        const val TAG = "DailyGoalNotifJob"

        //        fun scheduleJob(startTime: Long, endTime: Long, tag: String){
//            schedule(JobRequest.Builder(tag), startTime, endTime)
//        }
        fun scheduleJob() {
            Log.d(TAG, "Scheduling Job")
            schedule(
                JobRequest.Builder("1"), TimeUnit.HOURS.toMillis(9) + TimeUnit.MINUTES.toMillis(0),
                TimeUnit.HOURS.toMillis(9) + TimeUnit.MINUTES.toMillis(30)
            )
            schedule(
                JobRequest.Builder("2"),
                TimeUnit.HOURS.toMillis(12) + TimeUnit.MINUTES.toMillis(30),
                TimeUnit.HOURS.toMillis(13) + TimeUnit.MINUTES.toMillis(0)
            )
            schedule(
                JobRequest.Builder("3"),
                TimeUnit.HOURS.toMillis(16) + TimeUnit.MINUTES.toMillis(15),
                TimeUnit.HOURS.toMillis(16) + TimeUnit.MINUTES.toMillis(45)
            )
            schedule(
                JobRequest.Builder("4"),
                TimeUnit.HOURS.toMillis(19) + TimeUnit.MINUTES.toMillis(30),
                TimeUnit.HOURS.toMillis(20) + TimeUnit.MINUTES.toMillis(0)
            )
            schedule(
                JobRequest.Builder("5"),
                TimeUnit.HOURS.toMillis(17) + TimeUnit.MINUTES.toMillis(0),
                TimeUnit.HOURS.toMillis(17) + TimeUnit.MINUTES.toMillis(30)
            )
        }
    }

    override fun onRunDailyJob(p0: Params): DailyJobResult {
        Log.d(TAG, "onRunDailyJob")
        createNotificationChannel()

        stepCountRepository.getTodayStepCountData {
            createNotification(sharedPrefsRepository.getDailyStepsGoal() - it)
        }

        return DailyJobResult.SUCCESS
    }

    private fun createNotification(remainingSteps: Int) {
        Log.d(TAG, "Creating Notif")
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val title = getNotificationTitle()
        val body = getNotificationBody(remainingSteps)

        val notificationBuilder =
            NotificationCompat.Builder(context, DAILY_GOAL_NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.mipmap.ic_launcher_round
                    )
                )
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun getNotificationBody(remainingSteps: Int): CharSequence {
        Log.d(TAG, "getNotificationBody")
        return if (remainingSteps > 0)
            buildSpannedString {
                append("You are ")
                bold {
                    append("$remainingSteps steps ")
                }
                append("away from achieving your daily goal")
            }
        else
            context.getString(R.string.goal_completed_notification)
    }

    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Inside If")
            val serviceChannel = NotificationChannel(
                DAILY_GOAL_NOTIFICATION_CHANNEL_ID,
                "Daily goal notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getRemainingDailyGoalSteps(): Int {
        expandDailyGoalMapIfNeeded(sharedPrefsRepository.user.dailyGoalMap)
        val dailyGoal = sharedPrefsRepository.user
            .dailyGoalMap[DateTime().getMapFormattedDate()] ?: 5000
        val dailyStepCount = sharedPrefsRepository.getDailyStepCount()

        return if (dailyGoal > dailyStepCount) dailyGoal - dailyStepCount else 0
    }

    private fun getNotificationTitle(): String {
        Log.d(TAG, "getNotificationTitle")
        val titlesArray = arrayListOf<String>()
        titlesArray.add(context.getString(R.string.daily_goal_notification_title1))
//        titlesArray.add(context.getString(R.string.daily_goal_notification_title2))
//        titlesArray.add(context.getString(R.string.daily_goal_notification_title3))
//        titlesArray.add(context.getString(R.string.daily_goal_notification_title4))
//        titlesArray.add(context.getString(R.string.daily_goal_notification_title5))
//        titlesArray.add(context.getString(R.string.daily_goal_notification_title6))
        titlesArray.add(context.getString(R.string.daily_goal_notification_title7))
        titlesArray.add(context.getString(R.string.daily_goal_notification_title9))

        return titlesArray[Random.nextInt(0, 3)]
    }
}
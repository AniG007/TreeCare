package dal.mitacsgri.treecare.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.backgroundtasks.jobs.DailyGoalNotificationJob
import dal.mitacsgri.treecare.consts.FOREGROUND
import dal.mitacsgri.treecare.screens.MainActivity
import java.util.*


class ForegroundService: Service() {

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    var counter = 0


    companion object {
        fun assertActive(context: Context) {
            //DebugLogger.getInstance(context.applicationContext).logWithParticipantId("assertBackgroundRunning")
            JobIntentService.enqueueWork(
                context,
                ForegroundService.ServiceStarter::class.java,
                0x01,
                Intent()
            )
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d("Test", "Creating Service")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForegroundService()
        else
            startForeground(1, Notification())
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Foreground: ", "Start Foreground Service")
        startTimerTask()
//        createNotificationChannel()
//
//        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)
//
//        val notification = NotificationCompat.Builder(applicationContext, FOREGROUND)
//            .setAutoCancel(false)
//            .setOngoing(true)
//            .setContentTitle("TreeCare")
//            .setOnlyAlertOnce(true)
//            .setContentText("Working towards keeping you fit")
//            .setSmallIcon(R.drawable.ic_launcher)
//            .setContentIntent(pendingIntent)
//            .build()
//        startTimerTask()
//        startForeground(1, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        Log.d("Test", "createNotificationChannel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(DailyGoalNotificationJob.TAG, "Inside If")
            val serviceChannel = NotificationChannel(
                FOREGROUND,
                "foreground",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimerTask()
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, ForegroundServiceRestarter::class.java)
        this.sendBroadcast(broadcastIntent)
    }

    //Added Later
    fun startTimerTask() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                //Log.i("Count", "=========  " + counter++)
            }
        }
        timer?.schedule(timerTask, 1000, 1000) //
    }

    //ADDED Later
    fun stopTimerTask() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    fun startMyOwnForegroundService(){

        createNotificationChannel()

        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(applicationContext, FOREGROUND)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("TreeCare")
            .setOnlyAlertOnce(true)
            .setContentText("Working towards keeping you fit")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setNotificationSilent()
            .build()

        startForeground(1, notification)
    }

    //ADDED LAter
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopTimerTask()
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, ForegroundServiceRestarter::class.java)
        this.sendBroadcast(broadcastIntent)
    }


    //ADDED Later
    class ServiceStarter: JobIntentService() {
        override fun onHandleWork(intent: Intent) {
            ContextCompat.startForegroundService(
                applicationContext,
                Intent(applicationContext, ForegroundService::class.java)
            )
        }
    }
}
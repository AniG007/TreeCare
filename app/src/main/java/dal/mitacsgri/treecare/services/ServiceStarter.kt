//package dal.mitacsgri.treecare.services
//
//import android.app.Service
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.os.IBinder
//import android.util.Log
//import androidx.core.app.JobIntentService
//import androidx.core.content.ContextCompat
//
//
///**
// * Foreground service allows unlimited access to location
// * https://hackernoon.com/android-location-tracking-with-a-service-80940218f561
// */
//class KeepAliveService: Service() {
//    class DestroyReceiver: StartServiceReceiver()
//
//    class BootReceiver: StartServiceReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            //PowerListener(context.applicationContext).logBootCompleted()
//            //DebugLogger.getInstance(context.applicationContext).logWithParticipantId("startBackgroundAfterBoot")
//            super.onReceive(context, intent)
//        }
//    }
//
//    abstract class StartServiceReceiver: BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            assertActive(context)
//        }
//    }
//
//    companion object {
//        fun assertActive(context: Context) {
//            //DebugLogger.getInstance(context.applicationContext).logWithParticipantId("assertBackgroundRunning")
//            JobIntentService.enqueueWork(
//                context,
//                ServiceStarter::class.java,
//                0x01,
//                Intent()
//            )
//        }
//    }
//
//    class ServiceStarter: JobIntentService() {
//        override fun onHandleWork(intent: Intent) {
//            ContextCompat.startForegroundService(
//                applicationContext,
//                Intent(applicationContext, KeepAliveService::class.java)
//            )
//        }
//    }
//
//    private lateinit var scheduleBundler: ScheduleBundler
//
//    override fun onCreate() {
//        super.onCreate()
//        foregroundServiceNotification()
//        scheduleBundler = ScheduleBundler.getInstance(applicationContext)
//    }
//
//    private fun foregroundServiceNotification() {
//        if (Build.VERSION.SDK_INT >= 26) {
//            ForegroundServiceNotification(applicationContext).startForeground(this)
//        }
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, "onStartCommand")
//        scheduleBundler.assertActive()
//        return START_STICKY
//    }
//
//    override fun onBind(intent: Intent): IBinder? { return null }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        val intent = Intent(this, DestroyReceiver::class.java)
//        sendBroadcast(intent)
//    }
//}
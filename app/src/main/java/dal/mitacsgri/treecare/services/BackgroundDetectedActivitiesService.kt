package dal.mitacsgri.treecare.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionClient
import dal.mitacsgri.treecare.screens.MainActivity

class BackgroundDetectedActivitiesService : Service() {

    private lateinit var mIntentService: Intent
    private lateinit var mPendingIntent: PendingIntent
    private lateinit var mActivityRecognitionClient: ActivityRecognitionClient

    internal var mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val serverInstance: BackgroundDetectedActivitiesService
            get() = this@BackgroundDetectedActivitiesService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Test", "Inside Background OnCreate")
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Test", "Inside Background OnCreate start")
        if(intent !=null) {
            super.onStartCommand(intent, flags, startId)
            mActivityRecognitionClient = ActivityRecognitionClient(applicationContext)
            mIntentService = Intent(applicationContext, DetectedActivitiesIntentService::class.java)
            mPendingIntent = PendingIntent.getService(applicationContext, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT)
            requestActivityUpdatesButtonHandler()
        }
        return START_STICKY
    }

    fun requestActivityUpdatesButtonHandler() {
        Log.d("Test", "requestActivityUpdatesButtonHandler")
        val task = mActivityRecognitionClient.requestActivityUpdates(
            MainActivity.DETECTION_INTERVAL_IN_MILLISECONDS,
            mPendingIntent
        )

        task?.addOnSuccessListener {
//            Toast.makeText(applicationContext,
//                "Successfully requested activity updates",
//                Toast.LENGTH_SHORT)
//                .show()
        }

        task?.addOnFailureListener {
//            Toast.makeText(applicationContext,
//                "Requesting activity updates failed to start",
//                Toast.LENGTH_SHORT)
//                .show()
        }
    }

    fun removeActivityUpdatesButtonHandler() {
        Log.d("Test", "removeActivityUpdatesButtonHandler")
        val task = mActivityRecognitionClient.removeActivityUpdates(
            mPendingIntent)
        task?.addOnSuccessListener {

//            Toast.makeText(applicationContext,
//                "Removed activity updates successfully!",
//                Toast.LENGTH_SHORT)
//                .show()
        }

        task?.addOnFailureListener {
//            Toast.makeText(applicationContext, "Failed to remove activity updates!",
//                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeActivityUpdatesButtonHandler()
    }

    companion object {
        private val TAG = BackgroundDetectedActivitiesService::class.java.getSimpleName()
    }
}
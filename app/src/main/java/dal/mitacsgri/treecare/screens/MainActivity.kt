package dal.mitacsgri.treecare.screens

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.DetectedActivity
import com.google.firebase.firestore.ktx.toObjects
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.backgroundtasks.jobs.DailyGoalNotificationJob
import dal.mitacsgri.treecare.consts.ACTIVITY_RECOGNITION_CHANNEL_ID
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.services.BackgroundDetectedActivitiesService
import dal.mitacsgri.treecare.services.ForegroundService
import dal.mitacsgri.treecare.services.ForegroundServiceRestarter
import org.joda.time.DateTime
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var broadcastReceiver: BroadcastReceiver
    private val REQUEST_CODE = 1
    private val REQUEST_CODE2 = 2
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()

    private var activityTimer: Timer? = null
    private var activityTimerTask: TimerTask? = null
    var counter = 0
    var count = 0

    var currentTime = 0
    var lastTime = (System.currentTimeMillis() / 1000).toInt()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startServices() //for foreground service testing

        getIgnoreBatteryOptimizationsPermission()
        Log.d("DateyBoye", System.currentTimeMillis().toString())
        Log.d("DateyBoye", (DateTime().withTimeAtStartOfDay().millis + 28800000).toString())

        //This try catch is to ask the user to allow the app to auto start to run the BG service.
        //This is only for the Chinese manufacturers
//        try {
//            val intent = Intent()
//            val manufacturer = Build.MANUFACTURER
//            if ("xiaomi".equals(manufacturer, ignoreCase = true)) {
//                intent.component = ComponentName(
//                    "com.miui.securitycenter",
//                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
//                )
//            } else if ("oppo".equals(manufacturer, ignoreCase = true)) {
//                intent.component = ComponentName(
//                    "com.coloros.safecenter",
//                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
//                )
//            } else if ("vivo".equals(manufacturer, ignoreCase = true)) {
//                intent.component = ComponentName(
//                    "com.vivo.permissionmanager",
//                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
//                )
//            }
//            val list: List<ResolveInfo> = getPackageManager()
//                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
//            if (list.size > 0) {
//                startActivity(intent)
//            }
//        } catch (e: Exception) {
//            val crashlytics = FirebaseCrashlytics.getInstance()
//            crashlytics.recordException(e)
//            //Crashlytics.logException(e)
//        }

        //For API 29 and above for activity recognition

        if (Build.VERSION.SDK_INT >= 29) {
            if (checkSelfPermission(ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(ACTIVITY_RECOGNITION),
                    REQUEST_CODE
                )
            }
        }

        //AutoStartPermissionHelper.getInstance().getAutoStartPermission(applicationContext) //Supposed to take you to the auto start permission screen for Chinese OEM's

        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                //.setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(3, AudioManager.STREAM_MUSIC, 0)
        }

        clickSound = soundPool?.load(applicationContext, R.raw.click, 1)!!
        victorySound = soundPool?.load(applicationContext, R.raw.victory, 1)!!
        splashScreenSound = soundPool?.load(applicationContext, R.raw.acoustic_intro_short, 1)!!

        this.volumeControlStream =
            AudioManager.STREAM_MUSIC //mapping the stream to the volume hardware keys

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == BROADCAST_DETECTED_ACTIVITY) {
                    val type = intent.getIntExtra("type", -1)
                    val confidence = intent.getIntExtra("confidence", 0)
                    handleUserActivity(type, confidence)
                }
            }
        }
        startTracking()
    }


    private fun handleUserActivity(type: Int, confidence: Int) {
        var label = getString(R.string.activity_unknown)
        Log.d("Test", "handleUserActivity")
        when (type) {
            DetectedActivity.IN_VEHICLE -> {
                label = "You are in Vehicle"
                Log.d("Activity", label + " Confidence " + confidence)
            }
            DetectedActivity.ON_BICYCLE -> {
                label = "You are on Bicycle"
                Log.d("Activity", label + " Confidence " + confidence)
            }
            DetectedActivity.ON_FOOT -> {
                label = "You are on Foot"
                Log.d("Activity", label + " Confidence " + confidence)
            }
            DetectedActivity.RUNNING -> {
                label = "You are Running"
                Log.d("Activity", label + " Confidence " + confidence)
            }
            DetectedActivity.STILL -> {
                label = "You are Still"
                Log.d("Activity", label + " Confidence " + confidence)
            }
            DetectedActivity.TILTING -> {
                label = "Your phone is Tilted"
                Log.d("Activity", label + " Confidence " + confidence)
            }
            DetectedActivity.WALKING -> {
                label = "You are Walking"
                Log.d("Activity", label + "Confidence " + confidence)
            }
            DetectedActivity.UNKNOWN -> {
                label = "Unkown Activity"
                Log.d("Activity", label + " Confidence " + confidence)
            }
        }

        Log.e(TAG, "User activity: $label, Confidence: $confidence")

        if (confidence > CONFIDENCE && label == "You are in Vehicle" && count == 0) {
//            Toast.makeText(applicationContext,
//                "User activity: $label, Confidence: $confidence",
//                Toast.LENGTH_SHORT)
//                .show()
            //createNotification(label,confidence)
            stopTimerTask()

            createVehicleNotification()

            clearSedentaryNotification()
            count = 1
        }
//        } else if (currentTime - lastTime >= 1 && counter != 0) {
//            if (counter >= 300) {
//                createNotification2(label)
//                stopTimerTask()
//            }
         else if (confidence > CONFIDENCE && label == "You are Still" && counter == 0) {
            startTimerTask()
            clearVehicleNotification()
            count = 0
        } else {
            Log.d("Activity", "$label, $confidence")
            stopTimerTask()
            count = 0
        }
        //Log.d("Activity", (currentTime - lastTime).toString())
//        else if (counter >= 100) {
//            createNotification2(label)
//            stopTimerTask()
//        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "OnResume")
        startTracking()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver,
            IntentFilter(BROADCAST_DETECTED_ACTIVITY)
        )
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "OnPause")
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver) //uncomment if you do not want to track activity recognition in the background
    }

    private fun startTracking() {
        val intent = Intent(this@MainActivity, BackgroundDetectedActivitiesService::class.java)
        Log.d("Test", "startTracking")
        createNotificationChannel()
        startService(intent)
    }

    private fun stopTracking() {
        val intent = Intent(this@MainActivity, BackgroundDetectedActivitiesService::class.java)
        stopService(intent)
    }

    companion object {

        val BROADCAST_DETECTED_ACTIVITY = "activity_intent" //for activit recognition

        internal val DETECTION_INTERVAL_IN_MILLISECONDS: Long = 1000 //for activit recognition

        val CONFIDENCE = 90 //for activity recognition

        var soundPool: SoundPool? = null //For Ui sound

        var clickSound: Int = 0 //For Ui sound

        var victorySound: Int = 0 //For LeaderBoard victory sound

        var splashScreenSound: Int = 0

        //For Ui sound
        fun playClickSound(){
            soundPool?.play(clickSound, 1F, 1F, 1, 0, 1F)
            //soundPool?.autoPause()
        }

        //For Leaderboard victory sound
        fun playVictorySound(){
            soundPool?.play(victorySound, 1F, 1F, 1, 0, 1F)
        }

        //For Splash Screen Sound
        fun playSplashScreenIntro(){
            soundPool?.play(splashScreenSound, 1F, 1F, 1, 0, 1F)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "OnDestroy")

        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, ForegroundServiceRestarter::class.java)
        this.sendBroadcast(broadcastIntent)

        //stopTracking()

        soundPool?.release();
        soundPool = null;

        super.onDestroy()
    }

    override fun onStop() {
        Log.d(TAG, "OnStop")
        super.onStop()
        //stopTracking()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mainViewModel.onSignInResult(requestCode, resultCode, data, this)
    }

    private fun createNotificationChannel() {
        Log.d(TAG + " Notif", "createNotificationChannel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(DailyGoalNotificationJob.TAG, "Inside If")
            val serviceChannel = NotificationChannel(
                ACTIVITY_RECOGNITION_CHANNEL_ID,
                "Activity Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        } else {
            Notification.Builder(this)

        }
    }

    private fun createVehicleNotification() {
        Log.d(TAG + " Notif", "Creating Notif")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationSound = Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.notification)

        /*val title = "It looks like you're in a vehicle!"
        val body = "Try walking short distances instead of taking a vehicle"*/

        val title = getVehicleNotificationTitle()
        val body = getVehicleNotificationBody()

        val notificationBuilder = NotificationCompat.Builder(this, ACTIVITY_RECOGNITION_CHANNEL_ID)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher_round))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(notificationSound)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))

        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(10, notificationBuilder.build())
    }

    private fun createSedentaryReminderNotification() {
        Log.d(TAG + " Notif", "Creating Notif")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationSound = Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.notification)

        /*val title = "Sedentary Reminder"
        val body = "Try to move every 30 mins to keep yourself Healthy!"*/

        val title = getSedentaryNotificationTitle()
        val body = getSedentaryNotificationBody()

        val notificationBuilder = NotificationCompat.Builder(this, ACTIVITY_RECOGNITION_CHANNEL_ID)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher_round))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(notificationSound)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))

        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notificationBuilder.build())
    }

    @SuppressLint("BatteryLife")
    private fun getIgnoreBatteryOptimizationsPermission() {
        val powerManager: PowerManager =
            (applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
        val packageName = "dal.mitacsgri.treecare"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val i = Intent()
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                i.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                i.data = Uri.parse("package:$packageName")
                startActivity(i)
            } else {
                Log.d("Battery", "Already Optimized")
            }
        }
    }

    private fun startServices() {
        Log.d("Test", "Inside start Services")
        val serviceIntent = Intent(this, ForegroundService::class.java)
        serviceIntent.putExtra("inputExtra", "input")
        //ContextCompat.startForegroundService(this, serviceIntent)
        startService(serviceIntent)
    }


    fun startTimerTask() {
        activityTimer = Timer()
        activityTimerTask = object : TimerTask() {
            override fun run() {
                //lastTime = counter
                Log.i("Counter", "=========  " + counter++)
                currentTime = (System.currentTimeMillis()/1000).toInt()

                if (currentTime - lastTime >= 1800 && counter != 0 && (System.currentTimeMillis() > DateTime().withTimeAtStartOfDay().millis && System.currentTimeMillis() >= DateTime().withTimeAtStartOfDay().millis + 28800000)) { //for preventing reminder popup in the middle of  the night.... I woke up lot of times.. got irritated and then did this
                    //if(counter >= 1800){
                    createSedentaryReminderNotification()
                    stopTimerTask()
                    lastTime = (System.currentTimeMillis() / 1000).toInt()
                    //}
                }
            }
        }
        activityTimer?.schedule(activityTimerTask, 1000, 1000)
    }

    fun stopTimerTask() {
        if (activityTimer != null) {
            activityTimer?.cancel()
            activityTimer = null
        }
        counter = 0
    }

    private fun getSedentaryNotificationTitle(): String{

        val titlesArray = arrayListOf<String>()

        //titlesArray.add(applicationContext.getString(R.string.sendentary_notification_title1))
        titlesArray.add(applicationContext.getString(R.string.sendentary_notification_title2))
        titlesArray.add(applicationContext.getString(R.string.sendentary_notification_title3))

        return titlesArray[Random.nextInt(0, 2)]
    }

    private fun getSedentaryNotificationBody(): String{
        val bodyArray = arrayListOf<String>()

        //bodyArray.add(applicationContext.getString(R.string.sendentary_notification_body2))
        bodyArray.add(applicationContext.getString(R.string.sendentary_notification_body2))
        bodyArray.add(applicationContext.getString(R.string.sendentary_notification_body3))
        bodyArray.add(applicationContext.getString(R.string.sendentary_notification_body4))
        bodyArray.add(applicationContext.getString(R.string.sendentary_notification_body5))

        return bodyArray[Random.nextInt(0, 4)]
    }

    private fun getVehicleNotificationTitle(): String{
        val titlesArray = arrayListOf<String>()

        titlesArray.add(applicationContext.getString(R.string.vehicle_notification_title1))
        titlesArray.add(applicationContext.getString(R.string.vehicle_notification_title2))
        titlesArray.add(applicationContext.getString(R.string.vehicle_notification_title3))

        return titlesArray[Random.nextInt(0, 3)]
    }

    private fun getVehicleNotificationBody(): String{
        val bodyArray = arrayListOf<String>()

        bodyArray.add(applicationContext.getString(R.string.vehicle_notification_body1))
        bodyArray.add(applicationContext.getString(R.string.vehicle_notification_body2))
        bodyArray.add(applicationContext.getString(R.string.vehicle_notification_body3))

        return bodyArray[Random.nextInt(0, 3)]
    }

    fun clearSedentaryNotification() {
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(2)
    }

    fun clearVehicleNotification() {
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(10)
    }

    //For DB Backup
    fun export(view: View?) {
        //Ref: https://www.youtube.com/watch?v=VDAwbgHoYEA
        val data = java.lang.StringBuilder()
        data.append("Users")
        firestoreRepository.getAllUsers().addOnSuccessListener {

            val dota = it.toObjects<User>()
            for (dotta in dota) {
                data.append("\n" + dotta + "," + "\n")
            }

            try {
                //saving the file into device
                val out = openFileOutput("UserData.csv", MODE_PRIVATE)
                out.write(data.toString().toByteArray())
                out.close()

                //exporting
                val context = applicationContext
                val filelocation = File(filesDir, "UserData.csv")
                val path = FileProvider.getUriForFile(
                    context,
                    "com.example.exportcsv.fileprovider",
                    filelocation
                )
                val fileIntent = Intent(Intent.ACTION_SEND)
                fileIntent.type = "text/csv"
                fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data")
                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                fileIntent.putExtra(Intent.EXTRA_STREAM, path)
                startActivity(Intent.createChooser(fileIntent, "Send mail"))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}
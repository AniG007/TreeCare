package dal.mitacsgri.treecare.services

//import dal.mitacsgri.treecare.screens.treecareunityactivity.TreeCareUnityActivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.SensorRequest
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.consts.*
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import dal.mitacsgri.treecare.screens.treecareunityactivity.TreeCareUnityActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit


class StepDetectorService : Service(), KoinComponent {

    private val TAG = "StepCountService"

    private val stepCountRepository: StepCountRepository by inject()
    private val sharedPrefsRepository: SharedPreferencesRepository by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, TreeCareUnityActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, STEP_MONITOR_SERVICE_NOTIF_CHANNEL_ID)
            .setContentTitle("TreeCare")
            .setContentText("Working towards keeping you fit")
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        stepCountRepository.getTodayStepCountData {
            sharedPrefsRepository.storeDailyStepCount(it)
            if (isRealtimeUpdateRequired()) {
                Log.d(TAG, "bool" + isRealtimeUpdateRequired())
                updateStepCountUsingApi()
            }
            //else sharedPrefsRepository.storeDailyStepCount(sharedPrefsRepository.challengeTotalStepsCount)
            else {
                Log.d(TAG, "Else " + sharedPrefsRepository.challengeTotalStepsCount)
                if (sharedPrefsRepository.gameMode == CHALLENGER_MODE) {
                    Log.d(
                        TAG,
                        "challenge end " + sharedPrefsRepository.challengeTotalStepsCount
                    )
                    sharedPrefsRepository.challengeTotalStepsCount
                } else {
                    Log.d(
                        TAG,
                        "Tournament end " + sharedPrefsRepository.challengeTotalStepsCount
                    )
                    sharedPrefsRepository.tournamentTotalStepsCount
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                STEP_MONITOR_SERVICE_NOTIF_CHANNEL_ID,
                "Step Monitor Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            serviceChannel.apply {
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun updateStepCountUsingApi() {
        Log.d(TAG, "Inside updateStepCountUsingApi")
        Fitness.getSensorsClient(
            applicationContext,
            GoogleSignIn.getLastSignedInAccount(applicationContext)!!
        )
            .findDataSources(
                DataSourcesRequest.Builder()
                    .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                    .build()
            )
            .addOnSuccessListener {
                it.forEach {
                    Log.i(TAG, "Data source found: $it")
                    Log.i(TAG, "Data Source type: " + it.dataType.name)

                    // Let's register a listener to receive Activity data!
                    if (it.dataType == DataType.TYPE_STEP_COUNT_CUMULATIVE) {
                        registerFitnessDataListener()
                    } else {
                        useCoroutineToGetStepCount()
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Sensors API failed: $it")
                useCoroutineToGetStepCount()
            }
    }

    private fun registerFitnessDataListener() {
        Log.d(TAG, "Inside registerFitnessDataListener")
        var isFirstRun = true
        var lastStepCount = 0

        Fitness.getSensorsClient(
            applicationContext,
            GoogleSignIn.getLastSignedInAccount(applicationContext)!!
        )
            .add(
                SensorRequest.Builder()
                    .setDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                    .setSamplingRate(4, TimeUnit.SECONDS)
                    .build()
            ) { dataPoint ->
                if (isFirstRun) {
                    Log.d(TAG, "First Run")
                    if (sharedPrefsRepository.gameMode == STARTER_MODE) {
                        Log.d(TAG, "First Run Starter")
                        isFirstRun = false
                        dataPoint.dataType.fields.forEach {
                            val currentStepCount = dataPoint.getValue(it).asInt()
                            lastStepCount = currentStepCount
                            Log.d(TAG, "First Run LS " + lastStepCount)
                        }
                    } else if (sharedPrefsRepository.gameMode == TOURNAMENT_MODE) {
                        Log.d(TAG, "First Run Tournament")
                        isFirstRun = false
                        dataPoint.dataType.fields.forEach {
                            //val currentStepCount = sharedPrefsRepository.tournamentTotalStepsCount
                            val currentStepCount = dataPoint.getValue(it).asInt()
                            lastStepCount = currentStepCount
                            Log.d(TAG, "First Run LS " + lastStepCount)
                        }
                    } else if (sharedPrefsRepository.gameMode == CHALLENGER_MODE) {
                        Log.d(TAG, "First Run Challenge")
                        isFirstRun = false
                        dataPoint.dataType.fields.forEach {
                            //val currentStepCount = sharedPrefsRepository.challengeTotalStepsCount
                            val currentStepCount = dataPoint.getValue(it).asInt()
                            lastStepCount = currentStepCount
                            Log.d(TAG, "First Run LS " + lastStepCount)
                        }
                    }

                } else {
                    if (sharedPrefsRepository.gameMode == STARTER_MODE) {
                        Log.d(TAG, "Started sensor")
                        dataPoint.dataType.fields.forEach {
                            val currentStepCount = dataPoint.getValue(it).asInt()
                            sharedPrefsRepository.storeDailyStepCount(
                                sharedPrefsRepository.getDailyStepCount() + currentStepCount - lastStepCount
                            )
                            lastStepCount = currentStepCount
                            Log.d(TAG, "LastStepCount " + lastStepCount)
                        }
                    }

                    else if(sharedPrefsRepository.gameMode == CHALLENGER_MODE && sharedPrefsRepository.isChallengeActive){
                        Log.d(TAG, "Challenge ended")
                        dataPoint.dataType.fields.forEach {
                            val currentStepCount = dataPoint.getValue(it).asInt()
//                            sharedPrefsRepository.storeChallengeStepCount(
//                                //  sharedPrefsRepository.getDailyStepCount() + currentStepCount - lastStepCount
//                                sharedPrefsRepository.getChallengeStepCount() + currentStepCount - lastStepCount
//                            )

                            sharedPrefsRepository.storeDailyStepCount(
                                sharedPrefsRepository.getDailyStepCount() + currentStepCount - lastStepCount
                            )

                            lastStepCount = currentStepCount
                            Log.d(TAG, "LastStepCount " + lastStepCount)
                        }
                    }

                    else if (sharedPrefsRepository.gameMode == TOURNAMENT_MODE && sharedPrefsRepository.isTournamentActive) {
                        dataPoint.dataType.fields.forEach {
                            Log.d(TAG, "Tournament sensor")
                            val currentStepCount = dataPoint.getValue(it).asInt()
                            //  sharedPrefsRepository.getDailyStepCount() + currentStepCount - lastStepCount
                            sharedPrefsRepository.storeTournamentStepCount(
                                sharedPrefsRepository.getTournamentStepCount() + currentStepCount - lastStepCount
                            )
                            sharedPrefsRepository.storeDailyStepCount(
                                sharedPrefsRepository.getDailyStepCount() + currentStepCount - lastStepCount
                            )
                            lastStepCount = currentStepCount
                            Log.d(TAG, "LastStepCount " + lastStepCount)
                        }
                    }
                }
            }
    }

    private fun useCoroutineToGetStepCount() {
        Log.d(TAG, "Inside useCoroutineToGetStepCount")
        GlobalScope.launch {
            while (true) {
                delay(4000)
                stepCountRepository.getTodayStepCountData {
                    sharedPrefsRepository.storeDailyStepCount(it)
                }
            }
        }
    }

    private fun isRealtimeUpdateRequired() =
        (sharedPrefsRepository.gameMode == STARTER_MODE) or
                ((sharedPrefsRepository.gameMode == CHALLENGER_MODE) and sharedPrefsRepository.isChallengeActive) or
                ((sharedPrefsRepository.gameMode == TOURNAMENT_MODE) and sharedPrefsRepository.isTournamentActive)

}
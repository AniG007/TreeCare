package dal.mitacsgri.treecare.backgroundtasks.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class UpdateDailyStepCount(appContext: Context, workerParams: WorkerParameters)
    : ListenableWorker(appContext, workerParams), KoinComponent {

    private val stepCountRepository: StepCountRepository by inject()
    private val sharedPrefsRepository: SharedPreferencesRepository by inject()
    private val firestoreRepository: FirestoreRepository by inject()

    val TAG = "WorkerD"
    val mConstraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    override fun startWork(): ListenableFuture<Result> {

        Log.d(TAG,"Starting Daily Worker")
        val future = SettableFuture.create<Result>()
        if(sharedPrefsRepository.user.name.isEmpty()){
            sharedPrefsRepository.user = User()
        }
        stepCountRepository.getTodayStepCountData {
            updateInSharedPref(it, future)
        }

//        val updateDailyStepCountDataRequest = OneTimeWorkRequestBuilder<UpdateDailyStepCount>().setConstraints(mConstraints)
//            .setInitialDelay(5, TimeUnit.MINUTES)
//            .build() // calling this again so as to mimic periodic work request
//        WorkManager.getInstance(applicationContext).enqueue(updateDailyStepCountDataRequest)

        val updateDailyStepCountDataRequest: WorkRequest =
            OneTimeWorkRequestBuilder<UpdateDailyStepCount>()
                .setConstraints(mConstraints)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()

       // WorkManager.getInstance(applicationContext).enqueue(updateDailyStepCountDataRequest)


        return future
    }

    fun updateInSharedPref(steps: Int, future: SettableFuture<Result>){
        Log.d(TAG,"updating in Shared Pref")
        synchronized(sharedPrefsRepository.user) {
            val user = sharedPrefsRepository.user
            user.stepMap[DateTime().withTimeAtStartOfDay(). millis.toString()] = steps
            user.leafMap[DateTime().withTimeAtStartOfDay().millis.toString()] = steps / 1000
            sharedPrefsRepository.user = user
        }
        updateInFireStore(future)
    }

    fun updateInFireStore(future: SettableFuture<Result>){
        Log.d(TAG,"updating in Firestore")
        firestoreRepository.updateUserData(sharedPrefsRepository.user.uid, mapOf(("stepMap") to sharedPrefsRepository.user.stepMap))
            .addOnSuccessListener {
                firestoreRepository.updateUserData(sharedPrefsRepository.user.uid, mapOf(("leafMap") to sharedPrefsRepository.user.leafMap))
                    .addOnSuccessListener {
                        Log.d(TAG,"DailyStep uploaded to Firestore successfully")
                        future.set(Result.success())
                    }
                    .addOnFailureListener{
                        Log.d(TAG,"DailyStep upload to Firestore failed")
                        future.set(Result.failure())
                    }
            }
            .addOnFailureListener{
                Log.d(TAG,"DailyStep upload to Firestore failed")
                future.set(Result.failure())
            }
    }

}
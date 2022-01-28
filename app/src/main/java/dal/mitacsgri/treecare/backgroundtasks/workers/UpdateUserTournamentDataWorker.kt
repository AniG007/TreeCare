package dal.mitacsgri.treecare.backgroundtasks.workers

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import calculateLeafCountFromStepCount
import calculateLeafCountFromStepCountForTeam
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.consts.CHALLENGE_TYPE_DAILY_GOAL_BASED
import dal.mitacsgri.treecare.extensions.toDateTime
import dal.mitacsgri.treecare.model.*
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import org.joda.time.DateTime
import org.joda.time.Days
import org.koin.core.KoinComponent
import org.koin.core.inject

class UpdateUserTournamentDataWorker(appContext: Context, workerParams: WorkerParameters)
    : ListenableWorker(appContext, workerParams), KoinComponent {

    private val stepCountRepository: StepCountRepository by inject()
    private val sharedPrefsRepository: SharedPreferencesRepository by inject()
    private val firestoreRepository: FirestoreRepository by inject()

    override fun startWork(): ListenableFuture<Result> {
        Log.d("WorkerTournament","Starting Worker")
        val future = SettableFuture.create<Result>()

        if(sharedPrefsRepository.user.name.isEmpty()) {
            sharedPrefsRepository.user = User()
        }
        var c = 0
        val user = sharedPrefsRepository.user

        if(!user.currentTournaments.isNullOrEmpty()) {
            user.currentTournaments.forEach { (_, tourney) ->
                c++
                val endTimeMillis = tourney.endDate.toDateTime().millis
                //Two condition checks are applied because the 'isActive' variable is set only after
                //the dialog has been displayed. The second condition check prevents update of Tournament step count
                //in the database even when the dialog has not been displayed
                if (tourney.isActive && endTimeMillis > DateTime().millis) {
                    //if (tourney.isActive) {
                    Log.d("WorkerTournament", "TourneyName "+ tourney.name)
                    stepCountRepository.getTodayStepCountData {
                        tourney.dailyStepsMap[DateTime().withTimeAtStartOfDay().millis.toString()] = it
                        updateAndStoreUserTournamentDataInSharedPrefs(tourney)
                    }
                }
            }
            if(c== user.currentTournaments.size) {
                Log.d("WorkerTournament", "count "+ c+ "size "+ user.currentTournaments.size)
                updateUserTournamentDataInFirestore(future)
            }
        }

        else {
            Log.d("WorkerTournament","CurrentTournament is empty")
        }
        return future
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////Tournament Functions////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun updateAndStoreUserTournamentDataInSharedPrefs(tournament: UserTournament) {
        Log.d("WorkerTournament", "updateAndStoreUserTournamentDataInSharedPrefs")
        tournament.lastUpdateTime = Timestamp.now()
        var totalSteps = 0
        tournament.dailyStepsMap.forEach { (time, steps) ->
            totalSteps += steps
        }
        tournament.totalSteps = totalSteps

        synchronized(sharedPrefsRepository.user) {
            val user = sharedPrefsRepository.user
            user.currentTournaments[tournament.name] = tournament
            sharedPrefsRepository.user = user
        }
        Log.d("WorkerTournament","UserPref "+ sharedPrefsRepository.user.currentTournaments)
    }

    private fun updateUserTournamentDataInFirestore(future: SettableFuture<Result>) {
        Log.d("WorkerTournament", "updateUserTournamentDataInFirestore")
        firestoreRepository.updateUserData(sharedPrefsRepository.user.uid,
            mapOf("currentTournaments" to sharedPrefsRepository.user.currentTournaments))
            .addOnSuccessListener {
                Log.d("WorkerTournament", "TUser data upload success")
                future.set(Result.success())
            }
            .addOnFailureListener {
                Log.e("WorkerTournament", "TUser data upload failed")
                future.set(Result.failure())
            }
    }
}

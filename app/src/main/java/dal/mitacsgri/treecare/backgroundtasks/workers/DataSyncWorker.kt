package dal.mitacsgri.treecare.backgroundtasks.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.TeamTournament
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class DataSyncWorker(appContext: Context, workerParams: WorkerParameters)
    : ListenableWorker(appContext, workerParams), KoinComponent {

    private val sharedPrefRepository: SharedPreferencesRepository by inject()
    private val firestoreRepository: FirestoreRepository by inject()
    private val stepCountRepository: StepCountRepository by inject()

    override fun startWork(): ListenableFuture<Result> {
        Log.d("syncWorker","Started")
        val future = SettableFuture.create<Result>()
            WorkManager.getInstance().cancelUniqueWork("teamWorker")
        WorkManager.getInstance().cancelUniqueWork("challengeWorker")

        if(sharedPrefRepository.team.currentTournaments.isEmpty()){

            firestoreRepository.getUserData(sharedPrefRepository.user.uid).addOnSuccessListener { userDataFromDB ->
                val user = userDataFromDB.toObject<User>()
                val userTeam = user?.currentTeams.toString().removeSurrounding("[", "]")
                if (userTeam.isNotEmpty() && !user?.currentTournaments.isNullOrEmpty()) {
                    firestoreRepository.getTeam(userTeam).addOnSuccessListener {
                        val team = it.toObject<Team>()
                        val teamTournaments = team?.currentTournaments
                        for (tournament in teamTournaments!!) {
                            synchronized(sharedPrefRepository) {
                                sharedPrefRepository.user = user!!
                                sharedPrefRepository.team = team
                            }
                        }
                        Log.d("syncWorker", "Sync finished")
                    }
                }
            }
        }

        else{
            firestoreRepository.getUserData(sharedPrefRepository.user.uid).addOnSuccessListener {
                val user = it.toObject<User>()
                val userTournaments = user?.currentTournaments
                val prefTournaments = sharedPrefRepository.user.currentTournaments
                val userTeam = user?.currentTeams.toString().removeSurrounding("[", "]")

                if (userTeam.isNotEmpty()) {
                    if (userTournaments?.size != prefTournaments.size) {

                        firestoreRepository.getTeam(userTeam).addOnSuccessListener {
                            val team = it.toObject<Team>()
                            val teamTournaments = team?.currentTournaments
                            synchronized(sharedPrefRepository) {
                                val userData = sharedPrefRepository.user
                                userData.currentTournaments = userTournaments!!
                                sharedPrefRepository.user = userData

                                val teamData = sharedPrefRepository.team
                                teamData.currentTournaments = teamTournaments!!
                                sharedPrefRepository.team = teamData
                            }
                            Log.d("syncWorker","Sync finished")
                        }
                    }
                }
            }
        }

        if (sharedPrefRepository.getLastDayStepCount() == 0) {
            firestoreRepository.getUserData(sharedPrefRepository.user.uid)
                .addOnSuccessListener {
                    val user = it.toObject<User>()
                    val currentTournaments = user?.currentTournaments
                    currentTournaments?.forEach { (_, tourney) ->
                        if (tourney.isActive && tourney.dailyStepsMap.isNotEmpty()) {
                            Log.d("LastCount", "tourney is active and map is not empty")
                            sharedPrefRepository.storeLastDayStepCount(tourney.dailyStepsMap.toSortedMap().values.last())
                        }
                    }
                }
        }

        startWorkers()

        if(DateTime().toLocalDateTime().dayOfMonth == 2 && DateTime().toLocalDateTime().year == 2020 && DateTime().toLocalDateTime().monthOfYear == 12){
            if(!(sharedPrefRepository.user.currentTournaments.isNullOrEmpty()))
                    uploadStepsForStudy()
        }

        return future
    }

    fun startWorkers(){
        val mConstraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val updateTeamDataRequest =
            PeriodicWorkRequestBuilder<UpdateTeamDataWorker>(15, TimeUnit.MINUTES)
                .setConstraints(mConstraints)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()

        val updateUserChallengeDataRequest =
            PeriodicWorkRequestBuilder<UpdateUserChallengeDataWorker>(15, TimeUnit.MINUTES)
                .setConstraints(mConstraints)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            "teamWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            updateTeamDataRequest,
        )

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            "challengeWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            updateUserChallengeDataRequest,
        )
    }

    fun uploadStepsForStudy(){
        stepCountRepository.getStepCountDataOverARange(DateTime().withTimeAtStartOfDay().millis - 950400000, DateTime().withTimeAtStartOfDay().millis){

            val currentTourney = sharedPrefRepository.user.currentTournaments
            it.toSortedMap()

            /*currentTourney["Walkathon"]?.dailyStepsMap!!["1606017600000"] = it.values.first()
            currentTourney["Walkathon"]?.dailyStepsMap!!["1606104000000"] = it.values.last()*/

            it.forEach {
                //Log.d("sync", "Date ${it.key} Steps ${it.value}")
                currentTourney["Walkathon"]?.dailyStepsMap!![it.key.toString()] = it.value
            }

            synchronized(sharedPrefRepository.user){
                val user = sharedPrefRepository.user
                user.currentTournaments = currentTourney
                sharedPrefRepository.user = user
            }

            firestoreRepository.updateUserData(sharedPrefRepository.user.uid, mapOf("currentTournaments" to sharedPrefRepository.user.currentTournaments))
                .addOnSuccessListener {
                    Log.d("Steps", "Revive successful")
                }
        }
    }
}
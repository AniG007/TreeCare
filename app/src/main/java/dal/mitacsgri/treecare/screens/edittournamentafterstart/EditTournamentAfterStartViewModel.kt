package dal.mitacsgri.treecare.screens.edittournamentafterstart

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.model.*
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import java.util.*

class EditTournamentAfterStartViewModel(
    private val firestoreRepository: FirestoreRepository
): ViewModel() {

    private lateinit var endDate: Calendar

    var isEndDateValid = false

    val isFullDataValid = MutableLiveData<Boolean>()

    val messageLiveData = MutableLiveData<String>()
    var messageDisplayed = false



    private fun storeEndDate(dayOfMonth: Int, monthOfYear: Int, year: Int) {
        endDate = Calendar.getInstance()
        endDate.apply {
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.MONTH, monthOfYear)
            set(Calendar.YEAR, year)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
    }

    fun getCurrentDateDestructured(): Triple<Int, Int, Int> {
        endDate = Calendar.getInstance()
        return Triple(
            endDate.get(Calendar.DAY_OF_MONTH),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.YEAR)
        )
    }

    fun getEndDateText(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
        storeEndDate(dayOfMonth, monthOfYear, year)
        return "$dayOfMonth / ${monthOfYear + 1} / $year"
    }

    fun areAllInputFieldsValid(): Boolean {
        isFullDataValid.value = isEndDateValid
        return isFullDataValid.value == true
    }

    fun updateEndDate(tournamentName: String) {
        firestoreRepository.updateTournamentData(
            tournamentName,
            mapOf("finishTimestamp" to Timestamp(endDate.timeInMillis / 1000, 0))
        )
            .addOnSuccessListener {
                /** update team and user tournaments by caching the currentTournaments and then changing it in the local and then upload it back */
                firestoreRepository.getTournament(tournamentName)
                    .addOnSuccessListener {
                        /*WorkManager.getInstance().cancelUniqueWork("teamWorker")*/
                        val tournament = it.toObject<Tournament>()
                        val teams = tournament?.teams
                        for (team in teams!!) {
                            firestoreRepository.getTeam(team).addOnSuccessListener {
                                val teamData = it.toObject<Team>()
                                val teamTourney = teamData?.currentTournaments
                                teamTourney!![tournamentName]?.endDate = tournament.finishTimestamp
                                teamData.currentTournaments = teamTourney
                                firestoreRepository.updateTeamData(team, mapOf("currentTournaments" to teamData.currentTournaments))
                                    .addOnSuccessListener {
                                        val members = teamData.members
                                        for (member in members){
                                            firestoreRepository.getUserData(member).addOnSuccessListener {
                                                val user = it.toObject<User>()
                                                val userTourney = user?.currentTournaments
                                                userTourney!![tournamentName]?.endDate = tournament.finishTimestamp
                                                user.currentTournaments = userTourney
                                                firestoreRepository.updateUserData(member, mapOf("currentTournaments" to user.currentTournaments))
                                                    .addOnSuccessListener {
                                                        messageLiveData.value = "Tournament End Date was updated successfully"
                                                        messageDisplayed = true
                                                    }
                                            }
                                        }
                                    }
                            }
                        }
                    }
            }
        /*val mConstraints =
                        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

                    val updateTeamDataRequest =
                        PeriodicWorkRequestBuilder<UpdateTeamDataWorker>(15, TimeUnit.MINUTES)
                            .setConstraints(mConstraints)
                            .setInitialDelay(5, TimeUnit.MINUTES)
                            .build()

                    WorkManager.getInstance().enqueueUniquePeriodicWork(
                        "teamWorker",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        updateTeamDataRequest,
                    )*/
    }
}
package dal.mitacsgri.treecare.screens.edittournament

import android.text.Editable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.backgroundtasks.workers.DailyNotificationWorker
import dal.mitacsgri.treecare.backgroundtasks.workers.UpdateTeamDataWorker
import dal.mitacsgri.treecare.model.*
import dal.mitacsgri.treecare.repository.FirestoreRepository
import org.joda.time.DateTime
import java.util.*

class EditTournamentViewModel (
    private val firestoreRepository: FirestoreRepository
): ViewModel()
{
    private lateinit var startDate: Calendar
    private lateinit var endDate: Calendar

    var isGoalValid = false
    var isStartDateValid = false
    var isEndDateValid = false
    val isFullDataValid = MutableLiveData<Boolean>()

    val messageLiveData = MutableLiveData<String>()
    var messageDisplayed = false

    fun editTournament(description: Editable?,
                       goal: Editable?,
                       tournamentName: String) {
        //Log.d("Datey", (DateTime(startDate)).withTimeAtStartOfDay().millis.toString() + " "+ DateTime().withTimeAtStartOfDay().millis.toString())
        if(DateTime(startDate).withTimeAtStartOfDay().millis == DateTime().withTimeAtStartOfDay().millis){
            firestoreRepository.updateTournamentData(tournamentName, mapOf("active" to true))
        }

        firestoreRepository.updateTournamentData(
            tournamentName, mapOf(
                "description" to description.toString(),
                "dailyGoal" to goal.toString().toInt(),
                "startTimestamp" to Timestamp(startDate.timeInMillis / 1000, 0),
                "finishTimestamp" to Timestamp(endDate.timeInMillis / 1000, 0),
                "lastEdited" to Timestamp.now()
            )
        ).addOnSuccessListener {

            /*WorkManager.getInstance().cancelUniqueWork("teamWorker")*/
            firestoreRepository.getTournament(tournamentName)
                .addOnSuccessListener {

                    val tournament = it.toObject<Tournament>()
                    val teams = tournament?.teams
                    for (team in teams!!) {
                        firestoreRepository.getTeam(team).addOnSuccessListener {
                            val teamData = it.toObject<Team>()
                            val teamTourney = teamData?.currentTournaments
                            teamTourney!![tournamentName]?.endDate = tournament.finishTimestamp
                            teamTourney[tournamentName]?.startDate = tournament.startTimestamp
                            teamTourney[tournamentName]?.goal = tournament.dailyGoal

                            teamData.currentTournaments = teamTourney
                            firestoreRepository.updateTeamData(
                                team,
                                mapOf("currentTournaments" to teamData.currentTournaments)
                            )
                                .addOnSuccessListener {
                                    val members = teamData.members
                                    for (member in members) {
                                        firestoreRepository.getUserData(member)
                                            .addOnSuccessListener {
                                                val user = it.toObject<User>()
                                                val userTourney = user?.currentTournaments

                                                userTourney!![tournamentName]?.endDate =
                                                    tournament.finishTimestamp
                                                userTourney[tournamentName]?.startDate =
                                                    tournament.startTimestamp
                                                userTourney[tournamentName]?.goal =
                                                    tournament.dailyGoal

                                                user.currentTournaments = userTourney

                                                firestoreRepository.updateUserData(
                                                    member,
                                                    mapOf("currentTournaments" to user.currentTournaments)
                                                )
                                                    .addOnSuccessListener {
                                                        messageLiveData.value =
                                                            "Tournament was updated successfully"
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

    fun getCurrentDateDestructured(): Triple<Int, Int, Int> {
        endDate = Calendar.getInstance()
        return Triple(
            endDate.get(Calendar.DAY_OF_MONTH),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.YEAR)
        )
    }

    fun getCurrentDateDestructured2(): Triple<Int, Int, Int> {
        startDate = Calendar.getInstance()
        return Triple(
            startDate.get(Calendar.DAY_OF_MONTH),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.YEAR)
        )
    }

    fun getStartDateText(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
        storeStartDate(dayOfMonth, monthOfYear, year)
        return "$dayOfMonth / ${monthOfYear + 1} / $year"
    }

    fun getEndDateText(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
        storeEndDate(dayOfMonth, monthOfYear, year)
        return "$dayOfMonth / ${monthOfYear + 1} / $year"
    }

    private fun storeStartDate(dayOfMonth: Int, monthOfYear: Int, year: Int) {
        startDate = Calendar.getInstance()
        startDate.apply {
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.MONTH, monthOfYear)
            set(Calendar.YEAR, year)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
    }

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

    fun areAllInputFieldsValid(): Boolean {
        isFullDataValid.value =
            isGoalValid and isEndDateValid and isStartDateValid //and isCaptain //Use this if you want to allow only captains to create tournaments
        return isFullDataValid.value == true
    }

    fun getRegexToMatchStepsGoal() =
        Regex("([1-9]0000)|([1-9][1-9]000)") //For checking if goalsteps > = 10,000 and not more than 90,000


    fun checkDateFormat(dateLength: Int): Boolean {
        return dateLength == 12 || dateLength == 13 || dateLength == 14 //|| dateLength == 10 || dateLength == 9 || dateLength == 8 //While filling up the text box, the dates do not have spaces, hence we include 10 9 8

    }
}
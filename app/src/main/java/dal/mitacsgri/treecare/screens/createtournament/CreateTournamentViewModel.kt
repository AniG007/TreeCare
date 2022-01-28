package dal.mitacsgri.treecare.screens.createtournament

import android.text.Editable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.model.*
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import org.joda.time.DateTime
import java.util.*

/**
 * Created by Anirudh on 23-04-2020
 */

class CreateTournamentViewModel(
    private val sharedPrefsRepository: SharedPreferencesRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    var isNameValid = false
    var isGoalValid = false
    var isStartDateValid = false
    var isEndDateValid = false
    var isTeamSizeValid = false
    var isCaptain = false

    var messageDisplayed = false

    val messageLiveData = MutableLiveData<String>()
    val isFullDataValid = MutableLiveData<Boolean>()
    val buttonVis = MutableLiveData<Boolean>().default(false)

    val totalSteps = MutableLiveData<Int>().default(0)

    var c = 0
    var allMembers = 0

    private lateinit var startDate: Calendar
    private lateinit var endDate: Calendar
    val currentDate: Calendar = Calendar.getInstance()

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

    //fun getRegexToMatchStepsGoal() = Regex("([5-9][0-9]*(000)+)|([1-9]+0*0000)")  ([1-9]0000)|([1-9][1-9]000) //idk what this does but nvm
    fun getRegexToMatchStepsGoal() =
        Regex("([1-9]0000)|([1-9][1-9]000)") //For checking if goalsteps > = 10,000 and not more than 90,000

    fun getRegexToMatchTeamSize() = Regex("[2-9]|10")

    fun checkCaptaincy() { if(sharedPrefsRepository.user.uid == sharedPrefsRepository.team.captain) { isCaptain = true }}

    fun checkDataFormat(dateLength: Int): Boolean {
        return dateLength == 12 || dateLength == 13 || dateLength == 14

    }


    //fun isTeamLimitValid(size: Int):Boolean{ return size<=10 }

    fun areAllInputFieldsValid(): Boolean {
        isFullDataValid.value =
            isNameValid and isGoalValid and isEndDateValid and isStartDateValid and isTeamSizeValid //and isCaptain //Use this if you want to allow only captains to create tournaments
        if (isFullDataValid.value == true) {
            buttonVis.value = true
            return true
        } else {
            buttonVis.value = false
            return false
        }
    }

    fun createTournament(
        name: Editable?,
        description: Editable?,
        type: Int,
        goal: Editable?,
        teamLimit: Editable?,
        teams: Array<String>?,
        action: (Boolean) -> Unit
    ) {
        //Log.d("FDin",isFullDataValid.value.toString())
        //Sorry about the mess in this function but it was inevitable during this time. Welcome to callback hell! A possible alternative is using coroutines.
//        Log.d("startDate", Timestamp(startDate.timeInMillis / 1000, 0).toString())
//        Log.d("currentDate", Timestamp.now().toString())

        firestoreRepository.getTournament(name.toString())
            .addOnSuccessListener {
                if (it.exists()) {
                    messageDisplayed = false
                    messageLiveData.value = "Tournament already exists"
                    action(it.exists())
                    return@addOnSuccessListener
                } else if ((currentDate.get(Calendar.YEAR).equals(startDate.get(Calendar.YEAR))) &&
                    (currentDate.get(Calendar.DAY_OF_MONTH)
                        .equals(startDate.get(Calendar.DAY_OF_MONTH))) &&
                    (currentDate.get(Calendar.MONTH).equals(startDate.get(Calendar.MONTH)))
                ) {
                    //Setting the tournament as active if the tourney starts on the same date as the creation day
                    Log.d("TestElseIf", "Inside ElseIf")
                    if (endDate < startDate) {
                        messageDisplayed = false
                        messageLiveData.value =
                            "Please check the dates that you've entered and try again"
                    } else {
                        firestoreRepository.storeTournament(
                            Tournament(
                                name = name.toString(),
                                description = description.toString(),
                                //type = type,
                                dailyGoal = goal.toString().toInt(),
                                startTimestamp = Timestamp(startDate.timeInMillis / 1000, 0),
                                finishTimestamp = Timestamp(endDate.timeInMillis / 1000, 0),
                                creationTimestamp = Timestamp.now(),
                                creatorName = sharedPrefsRepository.user.name
                                        + " (${sharedPrefsRepository.user.email.split("@")[0]})",
                                creatorUId = sharedPrefsRepository.user.uid,
                                //isActive = true,
                                active = true,
                                exist = true,
                                teamLimit = teamLimit.toString().toInt()
                            )
                        ) {
                            action(it)
                            if (it) {
                                firestoreRepository.updateUserData(
                                    sharedPrefsRepository.user.uid,
                                    mapOf("tournamentsCreated" to FieldValue.arrayUnion(name.toString() ))
                                )
                                    .addOnSuccessListener {
                                        messageDisplayed = false
                                        messageLiveData.value = "Tournament created successfully"
                                    }
                            } else {
                                messageDisplayed = false
                                messageLiveData.value = "Tournament creation failed"
                            }

//                        Log.d("DateTest", currentDate.get(Calendar.YEAR).toString()+" "+ startDate.get(Calendar.YEAR))
//                        Log.d("DateTest", currentDate.get(Calendar.MONTH).toString()+" "+ startDate.get(Calendar.MONTH))
//                        Log.d("DateTest", currentDate.get(Calendar.DATE).toString()+" "+ startDate.get(Calendar.DATE))

                            if (it) {
                                firestoreRepository.getTournament(name.toString())
                                    .addOnSuccessListener {
                                        val tournament = it.toObject<Tournament>()
                                        val teamsSize = teams?.size
                                        if (!teams.isNullOrEmpty()) {
                                            for (team in teams) {
                                                Log.d(
                                                    "Test",
                                                    "Creating current Tournament Map for team: " + team
                                                )
                                                val teamTournament = tournament?.let { it1 ->
                                                    getTeamTournament(
                                                        it1,
                                                        0,
                                                        team
                                                    )
                                                }  // sending 0 for steps. Steps are added at 190 after TODO steps
                                                firestoreRepository.updateTournamentData(
                                                    name.toString(),
                                                    mapOf("teams" to FieldValue.arrayUnion(team))
                                                )
                                                    .addOnFailureListener {
                                                        messageDisplayed = false
                                                        messageLiveData.value =
                                                            "Failed to created tournament. Please try again later"

                                                        firestoreRepository.updateTournamentData(
                                                            name.toString(),
                                                            mapOf(
                                                                "teams" to FieldValue.arrayRemove(
                                                                    team
                                                                )
                                                            )
                                                        )
                                                        Log.d(
                                                            "TourneyCreation Failure",
                                                            it.toString()
                                                        )
                                                    }
                                                /*firestoreRepository.updateTeamData(team, mapOf("currentTournaments" to FieldValue.arrayUnion(name.toString())))
                                                    .addOnSuccessListener {*/
                                                mapOf("currentTournaments.${tournament?.name}" to teamTournament).let { it1 ->
                                                    firestoreRepository.updateTeamTournamentData(
                                                        team,
                                                        it1
                                                    )
                                                }
                                                    .addOnSuccessListener {
                                                        addTournament(team, tournament?.name!!)
                                                        // adding total steps (aggregate of user steps) to the team
                                                        //The below code was used to calculate steps of all users while joining the tournament.
                                                        //Since a team is allowed to join only before or on the start of a tournament, this has been commented
//                                                Log.d("Test", "Core")
//
//                                                firestoreRepository.getTeam(team)
//                                                    .addOnSuccessListener {
//                                                        val teamData = it.toObject<Team>()
//                                                        val members = teamData?.members
//                                                        val size = members?.count()
//
//                                                        Log.d("Test", "for Team: $team")
//                                                        for (member in members!!) {
//                                                            c++
//                                                            firestoreRepository.getUserData(member)
//                                                                .addOnSuccessListener {
//
//                                                                    val user =
//                                                                        it.toObject<User>()
//                                                                    val steps =
//                                                                        user?.dailySteps!!
//                                                                    Log.d(
//                                                                        "StepTest",
//                                                                        "dailySteps ${user.uid} " + steps.toString()
//                                                                    )
//                                                                    totalSteps.value =
//                                                                        totalSteps.value?.plus(
//                                                                            steps
//                                                                        )
//                                                                    totalSteps.notifyObserver()
//                                                                    Log.d("Test", "C value: "+ c)
//                                                                    Log.d("Test", "Size value: "+ size)
//                                                                    if (c == size) {
//
//                                                                        //TODO: steps
//                                                                        val updateTotalSteps =
//                                                                            tournament?.let { it1 ->
//                                                                                getTeamTournament(
//                                                                                    it1,
//                                                                                    totalSteps.value?.toInt()!!,
//                                                                                    team
//                                                                                )
//                                                                            }
//                                                                        mapOf("currentTournaments.${tournament?.name}" to updateTotalSteps).let { it1 ->
//                                                                            firestoreRepository.updateTeamTournamentData(
//                                                                                team,
//                                                                                it1
//                                                                            )
//                                                                        }
//
//                                                                            .addOnSuccessListener {
//                                                                                Log.d("Test", "For team $team")
//                                                                                addTournament(
//                                                                                    team,
//                                                                                    tournament?.name!!
//                                                                                )
//                                                                            }
//
//                                                                            .addOnFailureListener {
//                                                                                Log.d("Test", "Adding tournament to TeamDB Failed: "+ it)
//                                                                                mapOf("currentTournaments.${tournament?.name}" to updateTotalSteps).let { it1 ->
//                                                                                    firestoreRepository.deleteTournamentFromTeamDB(team, tournament?.name!!)
//                                                                                }
//
//                                                                            }
//                                                                        c = 0
//                                                                    }
//                                                                    Log.d("StepTest", "totalsteps " + totalSteps.value?.toInt().toString())
//                                                                }
//                                                        }
//                                                    }
                                                    }
                                            }
                                        }
                                    }
                            } else {
                                messageDisplayed = false
                                messageLiveData.value = "Tournament Creation Failed"
                            }
                        }
                    }
                } else {
                    if (endDate < startDate) {
                        messageDisplayed = false
                        messageLiveData.value =
                            "Please check the dates that you've entered and try again"
                    } else {
                        Log.d("TestElse", "Inside Else")
                        firestoreRepository.storeTournament(
                            Tournament(
                                name = name.toString(),
                                description = description.toString(),
                                dailyGoal = goal.toString().toInt(),
                                startTimestamp = Timestamp(startDate.timeInMillis / 1000, 0),
                                finishTimestamp = Timestamp(endDate.timeInMillis / 1000, 0),
                                creationTimestamp = Timestamp.now(),
                                creatorName = sharedPrefsRepository.user.name
                                        + " (${sharedPrefsRepository.user.email.split("@")[0]})",
                                creatorUId = sharedPrefsRepository.user.uid,
                                active = false,
                                exist = true,
                                teamLimit = teamLimit.toString().toInt()
                            )
                        ) {
                            action(it)
                            //messageLiveData.value = if (it) "Tournament has been created successfully"
                            //else "Tournament creation failed"
                            if (it) {

//                            Log.d("DateTest", currentDate.get(Calendar.YEAR).toString()+" "+ startDate.get(Calendar.YEAR))
//                            Log.d("DateTest", currentDate.get(Calendar.MONTH).toString()+" "+ startDate.get(Calendar.MONTH))
//                            Log.d("DateTest", currentDate.get(Calendar.DATE).toString()+" "+ startDate.get(Calendar.DATE))

                                firestoreRepository.getTournament(name.toString())
                                    .addOnSuccessListener {
                                        val tournament = it.toObject<Tournament>()
                                        if (!teams.isNullOrEmpty()) {
                                            for (team in teams) {
                                                Log.d(
                                                    "Test",
                                                    "Creating current Tournament Map for team: " + team
                                                )
                                                val teamTournament = tournament?.let { it1 ->
                                                    getTeamTournament(
                                                        it1,
                                                        0,
                                                        team
                                                    ) // sending 0 for steps since no team is being added. Steps are added at 284 at TODO steps
                                                }
                                                firestoreRepository.updateTournamentData(
                                                    name.toString(),
                                                    mapOf("teams" to FieldValue.arrayUnion(team))
                                                )
                                                    .addOnFailureListener {
                                                        messageDisplayed = false
                                                        messageLiveData.value =
                                                            "Failed to created tournament. Please try again later"
                                                        firestoreRepository.updateTournamentData(
                                                            name.toString(),
                                                            mapOf(
                                                                "teams" to FieldValue.arrayRemove(
                                                                    team
                                                                )
                                                            )
                                                        )
                                                        Log.d(
                                                            "TourneyCreation Failure",
                                                            it.toString()
                                                        )
                                                    }

                                                firestoreRepository.updateTeamTournamentData(
                                                    team,
                                                    mapOf("currentTournaments.${tournament?.name}" to teamTournament)
                                                )
                                                    .addOnSuccessListener {
                                                        // adding total steps (aggregate of user steps) to the team
                                                        Log.d("Test", "Core")
                                                        firestoreRepository.getTeam(team)
                                                            .addOnSuccessListener {
                                                                addTournament(
                                                                    team,
                                                                    tournament?.name!!
                                                                )
//                                                            val teamData = it.toObject<Team>()
//                                                            val members = teamData?.members
//                                                            val size = members?.count()
//                                                            for (member in members!!) {
//                                                                c++
//                                                                firestoreRepository.getUserData(
//                                                                    member
//                                                                )
//                                                                    .addOnSuccessListener {
//
//                                                                        val user =
//                                                                            it.toObject<User>()
//                                                                        val steps =
//                                                                            user?.dailySteps!!
//                                                                        Log.d(
//                                                                            "StepTest",
//                                                                            "dailySteps ${user.uid} " + steps.toString()
//                                                                        )
//                                                                        totalSteps.value =
//                                                                            totalSteps.value?.plus(
//                                                                                steps
//                                                                            )
//                                                                        totalSteps.notifyObserver()
//
//                                                                        if (c == size) {
//                                                                            //TODO: steps
//                                                                            val updateTotalSteps =
//                                                                                tournament?.let { it1 ->
//                                                                                    getTeamTournament(
//                                                                                        it1,
//                                                                                        totalSteps.value?.toInt()!!,
//                                                                                        team
//                                                                                    )
//                                                                                }
//                                                                            mapOf("currentTournaments.${tournament?.name}" to updateTotalSteps).let { it1 ->
//                                                                                firestoreRepository.updateTeamTournamentData(
//                                                                                    team,
//                                                                                    it1
//                                                                                )
//                                                                            }
//
//                                                                                .addOnSuccessListener {
//
//                                                                                    Log.d("Test", "For team ${team}")
//                                                                                    addTournament(
//                                                                                        team,
//                                                                                        tournament?.name!!
//                                                                                    )
//                                                                                }
//
//                                                                                .addOnFailureListener {
//                                                                                    Log.d("Test", "Adding tournament to TeamDB Failed: "+ it)
//                                                                                    mapOf("currentTournaments.${tournament?.name}" to updateTotalSteps).let { it1 ->
//                                                                                        firestoreRepository.deleteTournamentFromTeamDB(
//                                                                                            team,
//                                                                                            tournament?.name!!
//                                                                                        )
//                                                                                    }
//                                                                                }
//                                                                            c=0
//                                                                        }
//                                                                        Log.d(
//                                                                            "StepTest",
//                                                                            "totalsteps " + totalSteps.value?.toInt()
//                                                                                .toString())
//                                                                    }
//                                                            }
                                                            }
                                                    }
                                                    .addOnFailureListener {
                                                        Log.d("Test", "Unable to add user")
                                                        firestoreRepository.deleteTournamentFromTeamDB(
                                                            team,
                                                            tournament?.name!!
                                                        )
                                                    }
                                            }
                                        } else {
                                            firestoreRepository.updateUserData(
                                                sharedPrefsRepository.user.uid,
                                                mapOf("tournamentsCreated" to FieldValue.arrayUnion(name.toString()))
                                            )
                                                .addOnSuccessListener {
                                                    messageDisplayed = false
                                                    messageLiveData.value = "Tournament created successfully"
                                                }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
    }

    fun addTournament(team: String, tournament: String) {
        //Tournament is added to each user in all the teams that have been added to the tournament
        Log.d("Test", "Inside addTournament")
        firestoreRepository.getTournament(tournament)
            .addOnSuccessListener {
                val tournamentData = it.toObject<Tournament>()
                firestoreRepository.getTeam(team)
                    .addOnSuccessListener {
                        Log.d("Test", "Inside addTournament for team $team  ")
                        val teamData = it.toObject<Team>()
                        val members = teamData?.members

                        val userTournament =
                            tournamentData?.let { it1 -> getUserTournament(it1, team) }

                        for (uid in members!!) {
                            //val uid = sharedPrefsRepository.user.uid
                            //Log.d("Test","UID ${uid}")
                            userTournament?.let { it1 -> updateUserSharedPrefsData(it1) }
                            Log.d("Test", "tourneyName2 ${tournamentData?.name}")
                            mapOf("currentTournaments.${tournamentData?.name}" to userTournament).let { it1 ->
                                firestoreRepository.updateUserTournamentData(
                                    uid,
                                    it1
                                )
                            }
                                .addOnSuccessListener {

//                                                                      userTournament?.leafCount =
//                                                                      sharedPrefsRepository.getDailyStepCount() / 1000
                                    messageDisplayed = false
                                    messageLiveData.value =
                                        "Tournament has been created and teams were added successfully"

                                    //TODO: These 3 lines which are below have
                                    // to be executed everytime when a user navigates to tournament fragment
                                    val user = sharedPrefsRepository.user
                                    user.currentTournaments[tournamentData!!.name] =
                                        userTournament!!
                                    sharedPrefsRepository.user = user

                                    Log.d("Test", "Being added to user")
                                }
                                .addOnFailureListener {
                                    Log.d("Test", "Unable to add user")
                                }
                        }
                    }
            }
            .addOnFailureListener {
                firestoreRepository.deleteTournamentFromTeamDB(team, tournament)
            }
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

    private fun getUserTournament(tournament: Tournament, team: String) =
        UserTournament(
            name = tournament.name,
            dailyStepsMap = mutableMapOf(),
            totalSteps = sharedPrefsRepository.getDailyStepCount(),
            joinDate = DateTime().millis,
            goal = tournament.dailyGoal,
            startDate = tournament.startTimestamp,
            endDate = tournament.finishTimestamp,
            teamName = team
        )

    private fun updateUserSharedPrefsData(userTournament: UserTournament) {
        val user = sharedPrefsRepository.user
        userTournament.leafCount = sharedPrefsRepository.getDailyStepCount() / 1000
        userTournament.totalSteps = sharedPrefsRepository.getDailyStepCount()
        user.currentTournaments[userTournament.name] = userTournament
        sharedPrefsRepository.user = user
    }

    private fun getTeamTournament(tournament: Tournament, steps: Int, team: String) =
        TeamTournament(
            name = tournament.name,
            dailyStepsMap = mutableMapOf(),
            totalSteps = steps,
            joinDate = DateTime().millis,
            goal = tournament.dailyGoal,
            startDate = tournament.startTimestamp,
            endDate = tournament.finishTimestamp,
            teamName = team
        )

//    private fun getTeamTotalSteps(team:String): Int {
//        firestoreRepository.getTeam(team)
//            .addOnSuccessListener {
//                val teamData = it.toObject<Team>()
//                val members = teamData?.members
//                for(member in members!!){
//                    firestoreRepository.getUserData(member)
//                        .addOnSuccessListener {
//                            val user = it.toObject<User>()
//                            totalSteps += user?.dailySteps!!
//                            //totalSteps.value = totalSteps.value?.plus(steps!!)
//                        }
//                }
//            }
//        return totalSteps
//    }


}

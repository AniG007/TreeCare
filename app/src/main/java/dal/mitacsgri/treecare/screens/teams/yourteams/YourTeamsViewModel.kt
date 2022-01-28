package dal.mitacsgri.treecare.screens.teams.yourteams

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.notifyObserver
import dal.mitacsgri.treecare.extensions.toArrayList
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.Tournament
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.model.UserTournament
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import kotlin.system.exitProcess

class YourTeamsViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository,
    private val stepCountRepository: StepCountRepository
    //val status: MutableLiveData<Boolean> = MutableLiveData<Boolean>().default(false)
): ViewModel() {


    val teamsLiveData = MutableLiveData<ArrayList<Team>>().default(arrayListOf())
    val teamData = MutableLiveData<Team>()
    val status = MutableLiveData<String>()
    var messageDisplayed = false

    fun getAllMyTeams(): MutableLiveData<ArrayList<Team>> {

        //Log.d("Test","name "+sharedPrefsRepository.team.name)
        //Log.d("Test","name"+sharedPrefsRepository.user.name)
        sharedPrefsRepository.team = Team()
        val userId = sharedPrefsRepository.user.uid
        firestoreRepository.getAllTeamsForUserAsMember(userId)
            .addOnSuccessListener {
                //Log.d("Teststep",sharedPrefsRepository.getDailyStepCount().toString())

                //Update daily step of each user

                //firestoreRepository.updateUserData(userId, mapOf("dailySteps" to sharedPrefsRepository.getDailyStepCount()))

                stepCountRepository.getTodayStepCountData {
                    firestoreRepository.updateUserData(userId, mapOf("dailySteps" to it))
                }

                teamsLiveData.value = it.toObjects<Team>().filter { it.exist }.toArrayList()
                teamsLiveData.notifyObserver()

                if (teamsLiveData.value.isNullOrEmpty()) {
                    sharedPrefsRepository.team = Team()
                }

                if (teamsLiveData.value!!.contains(sharedPrefsRepository.team).equals(false)) {
                    sharedPrefsRepository.team = Team()
                    val t = sharedPrefsRepository.user
                    t.currentTeams.remove(teamsLiveData.value.toString())
                    sharedPrefsRepository.user = t
                }

                for (team in teamsLiveData.value!!) {
                    sharedPrefsRepository.team = team
                }
            }
        return teamsLiveData
    }

    fun deleteTeam(team: Team) {

        val teamMembers = team.members
        val tournaments = team.currentTournaments.keys
        var count = 0
        for (member in teamMembers) {
            firestoreRepository.updateUserData(
                member,
                mapOf("currentTeams" to FieldValue.arrayRemove(team.name))
            )
                .addOnSuccessListener {
                    for (tournament in tournaments) {
                        firestoreRepository.updateTournamentData(
                            tournament,
                            mapOf("teams" to FieldValue.arrayRemove(team.name))
                        )
                        firestoreRepository.deleteTournamentFromUserDB(member, tournament)
                        firestoreRepository.deleteTournamentFromTeamDB(team.name, tournament)
                    }
                }
            count++
            if (count.equals(teamMembers.size)) {
                firestoreRepository.deleteTeam(team.name)
                    .addOnSuccessListener {
                        firestoreRepository.updateUserData(
                            team.captain,
                            mapOf("captainedTeams" to FieldValue.arrayRemove(team.name))
                        )
                            .addOnSuccessListener {
                                Log.d("Test", "Deletion of Team is successful")
                                teamsLiveData.value?.remove(team)
                                teamsLiveData.notifyObserver()
                                sharedPrefsRepository.user.currentTeams.remove(team.name)
                                sharedPrefsRepository.team = Team()
                                if (sharedPrefsRepository.user.uid.equals(team.captain)) {
                                    sharedPrefsRepository.user.captainedTeams.remove(team.name)
                                }
                                status.value = "Team has been deleted"
                            }
                    }
            }
        }

        firestoreRepository.deleteTeam(team.name)
            .addOnSuccessListener {
                teamsLiveData.value?.remove(team)
                teamsLiveData.notifyObserver()
            }

    }

    fun isUserCaptain(captainUid: String) = captainUid == sharedPrefsRepository.user.uid

    fun exitTeam(team: Team) {
        val tourneys = team.currentTournaments.keys
        //remove users steps from team db first

        firestoreRepository.updateTeamData(
            team.name,
            mapOf("members" to FieldValue.arrayRemove(sharedPrefsRepository.user.uid))
        )
            .addOnSuccessListener {
                firestoreRepository.updateUserData(
                    sharedPrefsRepository.user.uid,
                    mapOf("currentTeams" to FieldValue.arrayRemove(team.name))
                )
                    .addOnSuccessListener {
                        teamsLiveData.value?.remove(team)
                        Log.d("Test", "Tourneys " + tourneys)
                        for (tourney in tourneys) {
                            Log.d("Test", "Tourney Name" + tourney)
                            removeUserStepsFromTeam(team.name, tourney, team)  //Removing user's steps from team's step map
                        }
                        teamsLiveData.notifyObserver()
                        status.value = "You are no longer part of the team ${team.name}"
                    }
            }
    }

    fun removeUserStepsFromTeam(teamName: String, tournament: String, team:Team) {
        Log.d("Test", "Inside removeUserStepsFromTeam")
        //Receive, subtract and upload back again// or modify prefs, upload and remove
        firestoreRepository.getUserData(sharedPrefsRepository.user.uid)
            .addOnSuccessListener {
                val user = it.toObject<User>()
                val userTournaments = user?.currentTournaments
                Log.d("Test", "userTournament " + userTournaments.toString())
                if (userTournaments?.keys?.contains(tournament)!!) {
                    val userTournament = userTournaments[tournament]
                    val map = userTournament?.dailyStepsMap
                    firestoreRepository.getTeam(teamName)
                        .addOnSuccessListener {
                            val team = it.toObject<Team>()
                            val currentTournament = team?.currentTournaments!![tournament]
                            //take each and every date for tournament and subtract the steps from team tournament
                            Log.d("Test", "outside Loop " + userTournament.toString())
                            if (userTournament?.isActive!!) {
                                for (steps in map!!) {
                                    Log.d("Test", "Steps " + steps.toString())
                                    val teamStepForAParticularDay =
                                        currentTournament?.dailyStepsMap!![steps.key]
                                    Log.d("Test", "teamStepForAParticularDay " + teamStepForAParticularDay)
                                    val stepsAfterDeductingUsersSteps =
                                        teamStepForAParticularDay!! - steps.value
                                    Log.d(
                                        "Test",
                                        "stepsAfterDeductingUsersSteps " + stepsAfterDeductingUsersSteps
                                    )
                                    currentTournament.dailyStepsMap[steps.key] =
                                        stepsAfterDeductingUsersSteps
                                    Log.d("Test", "currentTournament.dailyStepsMap[steps.key] " + currentTournament.dailyStepsMap)
                                }
                                Log.d("Test", "TeamName " + teamName)
                                firestoreRepository.updateTeamTournamentData(
                                    teamName,
                                    mapOf(tournament to currentTournament)
                                )
                                    .addOnSuccessListener {
                                        deleteTournamentFromUserDB(tournament,team)
                                        Log.d("Test", "Update to team successful")
                                    }
                                    .addOnFailureListener {
                                        Log.d("Test", "Failed to update Team" + it.toString())
                                    }
                            }
                        }
                }
            }
    }

    fun deleteTournamentFromUserDB(tourney: String, team:Team) {
        firestoreRepository.deleteTournamentFromUserDB(sharedPrefsRepository.user.uid, tourney)
            .addOnSuccessListener {
                //removing the prefs for a user after quitting a team
                sharedPrefsRepository.user.currentTeams.remove(team.name)
                //sharedPrefsRepository.team = Team()
                var preffy = sharedPrefsRepository.team
                preffy = Team()
                sharedPrefsRepository.team = preffy

                val prefTourneys = sharedPrefsRepository.user

                for (prefTourney in prefTourneys.currentTournaments) {
                    prefTourneys.currentTournaments.remove(prefTourney.key)
                }

                sharedPrefsRepository.user = prefTourneys
                status.value = "You have left ${team.name}"
                teamsLiveData.notifyObserver()
            }
            .addOnFailureListener {
                Log.d("Exception", it.toString())
            }
    }

    fun displayMessageForTest(){
        status.value = "You cannot exit a team during the Study"
        messageDisplayed = false
    }
}

    /*fun delBtnVis(): MutableLiveData<Boolean> {
        val userId = sharedPrefsRepository.user.uid
        firestoreRepository.getUserData(userId)
            .addOnSuccessListener {
                val user = it.toObject<User>()
                for(t in user?.captainedTeams!!)
                    firestoreRepository.getTeam(t)
                        .addOnSuccessListener {
                            Log.d("Test", "tname" + t)
                            val team = it.toObject<Team>()
                            Log.d("Test", "CapId" + team?.captain)
                            val Cid = team?.captain
                            if (Cid.equals(userId)) {
                                status.value = true
                                status.notifyObserver()
                            }
                        }
                Log.d("Test", "status" + status.value)
            }

        return status
    }*/

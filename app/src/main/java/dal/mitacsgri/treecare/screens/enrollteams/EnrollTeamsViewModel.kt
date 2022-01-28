package dal.mitacsgri.treecare.screens.enrollteams

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.notifyObserver
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.Tournament
import dal.mitacsgri.treecare.model.UserTournament
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import org.joda.time.DateTime

class EnrollTeamsViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository
)

    :ViewModel() {

    val teamsList = MutableLiveData<ArrayList<Team>>().default(arrayListOf())
    val teamsHolder = MutableLiveData<ArrayList<String>>().default(arrayListOf())
    val existingTeams = MutableLiveData<ArrayList<String>>().default(arrayListOf())
    //val teamsToAdd:ArrayList<String> = ArrayList<String>()

    val MessageStatus = MutableLiveData<String>()

    fun getTeams(): MutableLiveData<ArrayList<Team>> {
        //Log.d("Test",teamsHolder.value.toString())
        teamsList.value?.clear() //to Avoid duplicate entries while navigating up
        firestoreRepository.getAllTeams()
            .addOnSuccessListener {
                val teams = it.toObjects<Team>().filter { it.exist }
                for (team in teams){
                    teamsList.value?.add(team)
                }
                teamsList.notifyObserver()
            }
        return teamsList
    }

//        firestoreRepository.getAllCaptainedTeams(sharedPrefsRepository.user.uid)
//            .addOnSuccessListener {
//                val docs = it.toObjects<Team>().filter {it.exist}
//                //getting the teams owned by the player
//
//                for (team in docs) {
//                   // Log.d("Test", "QSnap " + team.toString())
//
//                    //Doing this inorder to remove the team which have already enrolled in the tournament
//                    if(existingTeams.value?.contains(team.name)!!){
//                        teamsList.value?.remove(team)
//                    }
//                    else {
//                        teamsList.value?.add(team)
//                    }
//                }
//                teamsList.notifyObserver()
//            }

    fun getExistingTeams (tournamentName: String){
        firestoreRepository.getTournament(tournamentName)
            .addOnSuccessListener {
                if(it.exists()) {
                    val ct = it.toObject<Tournament>()
                    val teams = ct?.teams
                    for (team in teams!!) {
                        existingTeams.value?.add(team)
                    }
                    Log.d("Test", "existingTeams" + existingTeams.value)
                    existingTeams.notifyObserver()
                }
            }
    }

    fun enrollTeams(teamsLimit: String):Boolean {
        //TO check if a team has already been enrolled in the tournament
        if(teamsHolder.value?.count()!! > teamsLimit.toInt()){
            MessageStatus.value = "You cannot select more than ${teamsLimit.toInt()} teams"
            return false
        }
        else {
            Log.d("Test", "Teams in teamsHolder " + teamsHolder.value)
            MessageStatus.value = "Teams will be added when you create a tournament"
            return true
        }
        /*for (team in teamsHolder.value!!) {

            Log.d("Test", "insideEnrollTeams" + team)
            firestoreRepository.getTournament(tournamentName)
                .addOnSuccessListener {
                    val tournament = it.toObject<Tournament>()
                    if (existingTeams.value?.contains(team)!!) {
                        Log.d("Test", existingTeams.value.toString())
                        MessageStatus.value = "Team has already been enrolled"
                    }

                    else if (tournament?.teams?.count() == tournament?.teamLimit) {
                        MessageStatus.value = "Tournament is full"
                    }

                    else {
                        firestoreRepository.updateTournamentData(tournamentName, mapOf("teams" to FieldValue.arrayUnion(team)))
                            .addOnSuccessListener {
                                firestoreRepository.updateTeamData(team, mapOf("currentTournaments" to FieldValue.arrayUnion(tournamentName)))
                                    .addOnSuccessListener {
                                        addTournament(team,tournamentName)
                                    }
                            }
                            .addOnFailureListener {
                                MessageStatus.value = "Unable to Enroll. Please Try Again later"
                            }
                    }
                }
        }*/
    }

    fun addTournament(team: String, tournamentName : String) {
        //Adding tournament to currentTournaments in Users Collection
        firestoreRepository.getTournament(tournamentName)
            .addOnSuccessListener {
                val tournament = it.toObject<Tournament>()
                firestoreRepository.getTeam(team)
                    .addOnSuccessListener {
                        val teamData = it.toObject<Team>()
                        val members = teamData?.members
                        val userTournament =
                            tournament?.let { it1 -> getUserTournament(it1, team) }

                        Log.d("Test", "tourneyName2 ${tournament?.name}")

                        for (uid in members!!) {
                            //val uid = sharedPrefsRepository.user.uid
                            //Log.d("Test","UID ${uid}")
                            userTournament?.let { it1 -> updateUserSharedPrefsData(it1) }
                            Log.d("Test", "tourneyName2 ${tournament?.name}")
                            mapOf("currentTournaments.${tournament?.name}" to userTournament)?.let { it1 ->
                                firestoreRepository.updateUserTournamentData(uid, it1)
                            }
                                .addOnSuccessListener {
                                    MessageStatus.value = "Enrolled Successfully"
//                                        userTournament?.leafCount =
//                                            sharedPrefsRepository.getDailyStepCount() / 1000
                                    //TODO: These 3 lines which are below have
                                    // to be executed everytime when a user navigates to tournament fragment
                                    val user = sharedPrefsRepository.user
                                    user.currentTournaments[tournament!!.name] =
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
    }

    fun addTeamToList (teamName:String) {
        teamsHolder.value?.add(teamName)
        teamsHolder.notifyObserver()
        Log.d("Test","Adding"+teamsHolder.value.toString())
    }

    fun removeTeamFromList (teamName:String){

        teamsHolder.value?.remove(teamName)
        teamsHolder.notifyObserver()
        Log.d("Test","Adding"+teamsHolder.value.toString())
    }

    private fun getUserTournament(tournament: Tournament, team:String) =
        UserTournament(
            name = tournament.name,
            dailyStepsMap = mutableMapOf(),
            totalSteps = sharedPrefsRepository.getDailyStepCount(),
            joinDate = DateTime().millis,
            goal = tournament.dailyGoal,
            endDate = tournament.finishTimestamp,
            teamName = team
        )

    //TODO: to call this method everytime a user visits the tournament fragment
    // so that the prefs are updated for unity before opening the tree
    private fun updateUserSharedPrefsData(userTournament: UserTournament){
        val user = sharedPrefsRepository.user
        userTournament.leafCount = sharedPrefsRepository.getDailyStepCount() / 1000
        userTournament.totalSteps = sharedPrefsRepository.getDailyStepCount()
        user.currentTournaments[userTournament.name] = userTournament
        sharedPrefsRepository.user = user
    }
}

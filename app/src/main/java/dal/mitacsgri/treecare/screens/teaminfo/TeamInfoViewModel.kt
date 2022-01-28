package dal.mitacsgri.treecare.screens.teaminfo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.notifyObserver
import dal.mitacsgri.treecare.model.*
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.StepCountRepository

class TeamInfoViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository,
    private val stepCountRepository: StepCountRepository
)
    :ViewModel() {

    val membersList = MutableLiveData<ArrayList<TeamInfo>>().default(arrayListOf())
    val status: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val tempList = MutableLiveData<ArrayList<TeamInfo>>().default(arrayListOf())
    val bool = MutableLiveData<Boolean>().default(false)

    fun getTeamMembers(teamName: String): MutableLiveData<ArrayList<TeamInfo>> {
        firestoreRepository.getTeam(teamName)
            .addOnSuccessListener {
                val team = it.toObject<Team>()
                Log.d("Test", "members" + team?.members)
                val teamCount = team?.members?.size
                for (m in team?.members!!) {
                    Log.d("Test", "m " + m)
                    firestoreRepository.getUserData(m.toString())
                        .addOnSuccessListener {
                            val user = it.toObject<User>()
                            membersList.value?.sortAndAddToList(
                                TeamInfo(
                                    user?.uid.toString(),
                                    teamName,
                                    team.captain,
                                    user?.name.toString(),
//                                getDailyStepCount(),
                                    user?.dailySteps!!,
                                    user.photoUrl.toString(),
                                    user.dailySteps / 3000
                                )
                            )
//
//                            membersList.value?.sortList(
//                                TeamInfo(
//                                    user?.uid.toString(),
//                                    teamName,
//                                    team.captain,
//                                    user?.name.toString(),
////                                getDailyStepCount(),
//                                    user?.dailySteps!!,
//                                    user.photoUrl.toString(),
//                                    user.dailySteps / 3000
//                                )
//                            )
                            /*if (membersList.value?.size == teamCount) {
                                sortTeamBasedOnStepCount(membersList.value!!)
                            }*/
                            Log.d("Test", "memsInTeam " + membersList.value.toString())
                            membersList.notifyObserver()
                        }
                }
            }
        return membersList
    }

    fun isCurrentUser(tInfo: TeamInfo) = tInfo.uId == sharedPrefsRepository.user.uid

    fun isUserCaptain(captainUid: String) = captainUid == sharedPrefsRepository.user.uid

    fun getDailyLeafCount(): String {

        Log.d("Test", "Leaf " + (sharedPrefsRepository.getDailyStepCount() / 3000).toString())
        return (sharedPrefsRepository.getDailyStepCount() / 3000).toString()

    }

//    fun getDailyStepCount(): MutableMap<String, Int> {
////        Log.d("Test","Steps "+ (sharedPrefsRepository.getDailyStepCount()).toString())
////        return (sharedPrefsRepository.getDailyStepCount()).toString()
//        stepCountRepository.getTodayStepCountData {
//            TeamInfo.dailyStepMap[DateTime().withTimeAtStartOfDay().millis.toString()] = it
//            updateAndStoreUserChallengeDataInSharedPrefs(Team, user)
//        }
//
//    }


//    private fun updateAndStoreUserChallengeDataInSharedPrefs(teamInfo: TeamInfo, user: User) {
//        challenge.leafCount = getTotalLeafCountForChallenge(challenge)
//        challenge.fruitCount = getTotalFruitCountForChallenge(challenge)
//        challenge.challengeGoalStreak = getChallengeGoalStreakForUser(challenge, user)
//        challenge.lastUpdateTime = Timestamp.now()
//
//        var totalSteps = 0
//        challenge.dailyStepsMap.forEach { (time, steps) ->
//            totalSteps += steps
//        }
//        challenge.totalSteps = totalSteps
//
//        synchronized(sharedPrefsRepository.user) {
//            val user = sharedPrefsRepository.user
//            user.currentChallenges[challenge.name] = challenge
//            sharedPrefsRepository.user = user
//        }
//    }

    fun removePlayer(teamInfo: TeamInfo) {
        firestoreRepository.updateUserData(
            teamInfo.uId,
            mapOf("currentTeams" to FieldValue.arrayRemove(teamInfo.teamName))
        )
            .addOnSuccessListener {
                firestoreRepository.updateTeamData(
                    teamInfo.teamName,
                    mapOf("members" to FieldValue.arrayRemove(teamInfo.uId))
                )
                    .addOnSuccessListener {
                        firestoreRepository.getTeam(teamInfo.teamName).addOnSuccessListener {
                            val teamData = it.toObject<Team>()
                            val teamTourneys = teamData?.currentTournaments?.keys
                            for(tourney in teamTourneys!!) {
                                removeUserStepsFromTeam(teamInfo.teamName, tourney, teamInfo.uId)  //Removing user's steps from team's step map
                            }
                        }
                        membersList.value?.remove(teamInfo)
                        membersList.notifyObserver()
            }
    }
    }

    private fun ArrayList<TeamInfo>.sortAndAddToList(teamInfo: TeamInfo) {

        if (teamInfo.captainId.equals(teamInfo.uId)) {
            add(0, teamInfo)
            return
        } else {
            add(teamInfo)
            return
        }
    }

//    fun display() {
//        Log.d("Test", "team printing " + sharedPrefsRepository.team.captainName)
//    }

    fun isCaptain(teamName: String): MutableLiveData<Boolean> {

        firestoreRepository.getTeam(teamName)
            .addOnSuccessListener {
                val team = it.toObject<Team>()
                val captain = team?.captain
                bool.value = captain == sharedPrefsRepository.user.uid
            }
        return bool
    }

    fun getCaptainId(): String{
        return membersList.value?.get(0)?.captainId!!
    }

    fun removeUserStepsFromTeam(teamName: String, tournament: String, userId: String) {
        Log.d("Test", "Inside removeUserStepsFromTeam")
        //Receive, subtract and upload back again// or modify prefs, upload and remove
        firestoreRepository.getUserData(userId)
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
                                        currentTournament?.dailyStepsMap!![steps.key] ?:0
                                    Log.d("Test", "teamStepForAParticularDay " + teamStepForAParticularDay)
                                    val stepsAfterDeductingUsersSteps =
                                        teamStepForAParticularDay - steps.value
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
                                        deleteTournamentFromUserDB(tournament,team, userId)
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

    fun deleteTournamentFromUserDB(tourney: String, team:Team, userId: String) {
        firestoreRepository.deleteTournamentFromUserDB(userId, tourney)
            .addOnSuccessListener {
                //removing the prefs for a user after quitting a team
                firestoreRepository.getTeam(team.name)
                    .addOnSuccessListener {
                        val teamData = it.toObject<Team>()

                        var prefs = sharedPrefsRepository.team
                        prefs = teamData!!
                        sharedPrefsRepository.team = prefs
                    }
            }
            .addOnFailureListener {
                Log.d("Exception", it.toString())
            }
    }

    fun sortUsersAccordingToStepCount(): MutableLiveData<ArrayList<TeamInfo>>{

        return membersList
    }

    private fun ArrayList<TeamInfo>.sortList(teamInfo: TeamInfo) {

        sortByDescending {
            it.stepsCount
        }
    }

//    fun getPlayerPosition(): Int{
//
//        val players = membersList.value?: arrayListOf()
//        for(i in 0 until players.size){
//            if(players[i].uId == sharedPrefsRepository.user.uid)
//                return i+1
//        }
//        return 0
//    }
}
package dal.mitacsgri.treecare.screens.jointeam

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.notifyObserver
import dal.mitacsgri.treecare.model.*
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import org.joda.time.DateTime

class JoinTeamViewModel(

    private val sharedPrefsRepository: SharedPreferencesRepository,
    private val firestoreRepository: FirestoreRepository
): ViewModel() {

    val mailData = MutableLiveData<String>()
    val userID = MutableLiveData<String>()
    val valid = MutableLiveData<Boolean>()
    val list:ArrayList<Int> = ArrayList<Int>()
    val messageLiveData = MutableLiveData<String>()
    val messageLiveData2 = MutableLiveData<String>()


    fun getUserEmail(email:String, teamName:String) {
        firestoreRepository.getAllUserEmail(email)
            .addOnSuccessListener {
                val user = it.toObjects<User>()
                try {
                    mailData.value = user.get(0).email
                }
                catch (e:Exception){
                    Log.d("Exception",e.toString())
                    mailData.value=""
                }
                if(mailData.value=="" ){
                    messageLiveData.value = "PlayerID does not exist"
                    //messageLiveData.notifyObserver()
                }
                else{
                    getUserId(email,teamName)
                }
            }
    }

    fun getUserId(email:String, teamName:String) {
        firestoreRepository.getAllUserEmail(email)
            .addOnSuccessListener {
                Log.d("Test", "mailId1" + it.toString())
                val user = it.toObjects<User>()
                try {
                    //userID.value = user.get(0).email
                    val userId = user.get(0).email

                    //Log.d("Test", "mailId2" + user.get(0).email)
                    //if (userID.value == email) {
                    if (userId == email) {
                        /**Uncomment the line below for adding user directly to team and comment all other lines in this function */
                        acceptUser(user[0].uid)
                        /*firestoreRepository.updateUserData(user.get(0).uid, mapOf("teamInvites" to FieldValue.arrayUnion(teamName)))
                            .addOnSuccessListener {
                                Log.d("Invite", "sent")

                                firestoreRepository.updateTeamData(teamName, mapOf("invitedMembers" to FieldValue.arrayUnion(user.get(0).uid)))
                                    .addOnSuccessListener {
                                        Log.d("UserId", "User Added to team invites")
                                        messageLiveData2.value = "An Invite has been sent to the Player"
                                    }
                                    .addOnFailureListener {
                                        Log.d("Invite", "Failed to add to team")
                                        firestoreRepository.updateTeamData(user[0].uid, mapOf("invitedMembers" to FieldValue.arrayRemove(teamName)))
                                    }
                            }
                            .addOnFailureListener {
                                Log.e("Invite", "failed")
                            }*/
                    }
                    else{
                        messageLiveData.value = "PlayerID does not exist"
                        //messageLiveData.notifyObserver() // notify observer is not needed here
                    }
                }
                catch (e: Exception) {
                        Log.d("Exception", e.toString())
                    }
            }
    }


    fun acceptUser(userID: String): MutableLiveData<String> {
        /** most of these firebase calls can be rewritten in a concise manner */
        Log.d("Test","Inside Function")
        val tName = sharedPrefsRepository.team.name
        Log.d("Test", "tname "+ tName)
        var count2 = 0

        firestoreRepository.getUserData(userID)
            .addOnSuccessListener {
                val user = it.toObject<User>()
                // checking if the user is already in a team before accepting
                val currentTeams = user?.currentTeams
                if (currentTeams.isNullOrEmpty()) {
                    //if (currentTeams.isEmpty()) {
                    firestoreRepository.getTeam(tName)
                        .addOnSuccessListener {
                            Log.d("Test", "Inside getTeam")
                            val team = it.toObject<Team>()
                            val capId = team?.captain
                            Log.d("Test", "CaptainId" + capId)

                            count2++
                            Log.d("Count", "Count1" + count2)
                        }

                    firestoreRepository.addCurrentTeams(userID, tName)
                        .addOnSuccessListener {
                            Log.d("Test", "Inside addCurrentTeams")
                            count2++
                            Log.d("Count", "Count4" + count2)

                            Log.d("Add", "User was added to the team successfully")
                        }
                        .addOnFailureListener {
                            Log.d("Text", "Exception $it " + userID + " "+ tName)
                        }

                    firestoreRepository.addTeamMember(userID, tName)
                        .addOnSuccessListener {
                            Log.d("Test", "Inside addTeamMember")
                            count2++
                            Log.d("Count", "Count5" + count2)

                            Log.d("count", "c1 " + count2)

                            Log.d("Add", "User was added to the team successfully")

                            if (count2 == 3) {

                                firestoreRepository.getTeam(tName)
                                    .addOnSuccessListener {
                                        val team = it.toObject<Team>()
                                        val tourneys = team?.currentTournaments?.keys
                                        for (tourney in tourneys!!) {
                                            addTournament(tName, tourney, "Request", userID)
                                        }
                                    }

                                messageLiveData2.value = "Player has been added to the team"
                                //messageLiveData2.notifyObserver()

                            } else {
                                messageLiveData2.value =
                                    "Unable to process request. Please try again later"
                                //messageLiveData2.notifyObserver()
                            }
                        }
                } else {
                    messageLiveData2.value = "User is already part of a team"
                }

            }
        return messageLiveData2
    }

    fun addTournament(team: String, tournamentName : String, type:String, uid:String) {
        //Adding tournament to currentTournaments in Users Collection
        firestoreRepository.getTournament(tournamentName)
            .addOnSuccessListener {
                val tournament = it.toObject<Tournament>()
                val userTournament =
                    tournament?.let { it1 -> getUserTournament(it1, team) }

                userTournament?.let { it1 -> updateUserSharedPrefsData(it1) }
                Log.d("Test", "tourneyName2 ${tournament?.name}")
                if (tournament?.active!!){
                    mapOf("currentTournaments.${tournament.name}" to userTournament).let { it1 ->
                        firestoreRepository.updateUserTournamentData(uid, it1)
                    }
                        .addOnSuccessListener {
                            //If it's an invite, share  d preferences are updated when the user accepts the invite, else,
                            //if it's a request, shared preferences are updated when user visits the tournaments page
                            // (check tournaments view model code, under current tournaments function
                            if (type == "Invite") {
                                val user = sharedPrefsRepository.user
                                user.currentTournaments[tournament.name] =
                                    userTournament!!
                                sharedPrefsRepository.user = user

                                Log.d("Test", "Being added to user")
                            }
                        }
                        .addOnFailureListener {
                            Log.d("Test", "Unable to add user")
                        }
                }
            }
    }

    private fun getUserTournament(tournament: Tournament, team:String) =
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

    private fun updateUserSharedPrefsData(userTournament: UserTournament){
        val user = sharedPrefsRepository.user
        userTournament.leafCount = sharedPrefsRepository.getDailyStepCount() / 1000
        userTournament.totalSteps = sharedPrefsRepository.getDailyStepCount()
        user.currentTournaments[userTournament.name] = userTournament
        sharedPrefsRepository.user = user
    }

}
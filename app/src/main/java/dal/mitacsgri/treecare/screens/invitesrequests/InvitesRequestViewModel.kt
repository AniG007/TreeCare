package dal.mitacsgri.treecare.screens.invitesrequests

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

class InvitesRequestViewModel(
    private val sharedPrefsRepository: SharedPreferencesRepository,
    private val firestoreRepository: FirestoreRepository
): ViewModel() {

    val messageLiveData = MutableLiveData<String>()
    val messageLiveData2 = MutableLiveData<String>()
    val messageLiveData3 = MutableLiveData<String>()
    val messageLiveData4 = MutableLiveData<String>()
    val status = MutableLiveData<Boolean>().default(false)
    val invitesList = MutableLiveData<ArrayList<InvitesRequest>>().default(arrayListOf())
    val requestList = MutableLiveData<ArrayList<InvitesRequest>>().default(arrayListOf())

    var messageDisplayed = false

    val uId = ArrayList<String>()

    val teams = ArrayList<String>()
    val tInvites = ArrayList<String>()

    val cTeam = MutableLiveData<ArrayList<String>>().default(arrayListOf())

    fun getAllRequests(): MutableLiveData<ArrayList<InvitesRequest>> {
        val uid = sharedPrefsRepository.user.uid
        firestoreRepository.getAllCaptainedTeams(uid)
            .addOnSuccessListener {
                val ct = it.toObjects<Team>()
                for (i in 0 until ct.count()) {
                    if (ct.get(i).joinRequests.isNotEmpty()) {
                        val v = ct.get(i).joinRequests.toString()
                        cTeam.value?.add(v)
                    }
                    teams.add(ct.get(i).name)
                }
                var count = 0
                var c = 0 //used for counting the iteration while fetching user photos
                for (t in teams) {
                    firestoreRepository.getTeam(t)
                        .addOnSuccessListener {
                            val team = it.toObject<Team>()
                            //val capname = team?.captainName
                            val tName = team?.name.toString()
                            // Log.d("Requests","tnames"+tName)
                            team?.joinRequests?.let { it1 -> uId.addAll(it1) }
                            for (i in 0 until uId.count()) {
                                //  Log.d("TAG","indi"+uId.get(i))
                                //This loop is for counting all the elements in the array list
                                // since the DB returns an array list, which is added to another list
                                //So one whole list of user id's is counted as one. To avoid that we use this loop
                                count++
                            }

                            for (u in 0 until uId.count()) {
                                c += 1
                                firestoreRepository.getUserData(uId.get(u))
                                    .addOnSuccessListener {
                                        val user2 = it.toObject<User>()
                                        requestList.value?.add(
                                            InvitesRequest(
                                                user2?.name.toString(),
                                                " " + tName,
                                                user2?.photoUrl.toString(),
                                                user2?.uid.toString()
                                            )
                                        )
                                        if (c == count) {
                                            requestList.notifyObserver()
                                        }
                                    }
                            }
                            uId.clear() // clearing the Uid so that the next set from the next team can be loaded
                        }
                }

            }
        return requestList
    }

    fun getAllInvites(): MutableLiveData<ArrayList<InvitesRequest>> {

        val uid = sharedPrefsRepository.user.uid
        var count2 = 0
        firestoreRepository.getUserData(uid)
            .addOnSuccessListener {
                val user = it.toObject<User>()
                tInvites.add(user?.teamInvites.toString())
                Log.d("Inv", "tCount" + user?.teamInvites?.count())
                for (t in user?.teamInvites!!) {
                    count2++ //Increments when each team is fetched

                    firestoreRepository.getTeam(t)
                        .addOnSuccessListener {
                            val team = it.toObject<Team>()
                            val cId = team?.captain
                            firestoreRepository.getUserData(cId.toString())
                                .addOnSuccessListener {
                                    val captain = it.toObject<User>()
                                    invitesList.value?.add(InvitesRequest(
                                        team?.captainName.toString(),
                                        team?.name.toString(),
                                        captain?.photoUrl.toString(),
                                        team?.captain.toString()
                                    ))

                                    if (count2 == user.teamInvites.count())
                                        invitesList.notifyObserver()
                                }
                        }
                }
            }
        return invitesList
    }

    fun acceptInvite(item: InvitesRequest): MutableLiveData<String> {
        val userid = sharedPrefsRepository.user.uid
        var count = 0
        //Log.d("Del","tn"+item.teamName+" "+item.photoUrl+" "+item.userName+" "+userid)

        if(sharedPrefsRepository.team.name.isEmpty()){
            //TODO: These functions can be simplified using the updateTeam function instead of having so many functions
            firestoreRepository.deleteTeamInvitesFromUser(userid, item.teamName)
                .addOnSuccessListener {

                    count++

                    Log.d("del", "Deleted the invite Successfully")
                    status.value = true
                    //status.notifyObserver()
                    /*invitesList.value?.remove(item)
                    invitesList.notifyObserver()*/
                }
                .addOnFailureListener {
                    Log.e("del", "Deletion of Invite Failed")
                }
            firestoreRepository.deleteTeamJoinRequestFromUser(userid, item.teamName)
                .addOnSuccessListener {

                    count++

                    Log.d("del", "Deleted the Request Successfully")
                    status.value = true
                    //status.notifyObserver()
                }
                .addOnFailureListener {
                    Log.e("del", "Deletion of Request Failed")
                }
            firestoreRepository.deleteJoinRequestFromTeam(userid, item.teamName)
                .addOnSuccessListener {

                    count++

                    Log.d("del", "Deletion of Join Req from team was successful")
                    status.value = true
                    //status.notifyObserver()
                }

            firestoreRepository.deleteInvitedMemberFromTeam(userid, item.teamName)
                .addOnSuccessListener {

                    count++

                    Log.d("del", "Deletion of Invite from team was successful")
                    //status.notifyObserver()

                }

            firestoreRepository.addCurrentTeams(userid, item.teamName)
                .addOnSuccessListener {

                    count++

                    Log.d("Add", "User was added to the team successfully")
                    status.value = true
                    //status.notifyObserver()
                }

            firestoreRepository.addTeamMember(userid, item.teamName)
                .addOnSuccessListener {

                    count++
                    Log.d("count", "c1 " + count)

                    status.value = true
                    //status.notifyObserver()
                    Log.d("Add", "User was added to the team successfully")

                    if (count == 6) {

                        firestoreRepository.getTeam(item.teamName)
                            .addOnSuccessListener {
                                val team = it.toObject<Team>()
                                val tourneys = team?.currentTournaments?.keys
                                sharedPrefsRepository.team = team!!
                                val user = sharedPrefsRepository.user
                                //user.currentTeams.add(team.name)
                                user.currentTeams.add(team.name)
                                sharedPrefsRepository.user = user
                                for(tourney in tourneys!!){
                                    addTournament(team.name,tourney, "Invite", sharedPrefsRepository.user.uid)
                                }
                            }

                        invitesList.value?.remove(item)
                        invitesList.notifyObserver()

                        requestList.value?.remove(item)
                        requestList.notifyObserver()

                        messageLiveData.value = "You have been added to the team"
                        //messageLiveData.notifyObserver()

                    } else {
                        messageLiveData.value = "Unable to process request. Please try again later"
                        //messageLiveData.notifyObserver()
                    }
                }
            return messageLiveData
        }
        else{
            messageLiveData.value = "You can only be a part of one team"
            //messageLiveData.notifyObserver()
            return messageLiveData
        }
    }

    fun acceptUser(item: InvitesRequest): MutableLiveData<String> {
        /** most of these firebase calls can be rewritten in a concise manner */
        Log.d("Test","Inside Function")
        val userid2 = item.uId
        val tName = item.teamName.trim()
        Log.d("Del","tn"+item.teamName.trim()+" "+item.photoUrl+" "+item.userName+" "+userid2)
        Log.d("Test", "UserId "+userid2)
        var count2 = 0

        firestoreRepository.getUserData(userid2)
            .addOnSuccessListener {
                val user = it.toObject<User>()
                // checking if the user is already in a team before accepting
                if(user?.currentTeams?.isEmpty()!!) {
                    firestoreRepository.getTeam(tName)
                        .addOnSuccessListener {
                            Log.d("Test", "Inside getTeam")
                            val team = it.toObject<Team>()
                            val capId = team?.captain
                            Log.d("Test", "CaptainId" + capId)

                            firestoreRepository.deleteJoinRequestFromTeam(capId.toString(), tName)
                                .addOnSuccessListener {
                                    Log.d("Test", "Inside deleteUserJoinReq")

                                    Log.d("del2", "Deletion of Invite from team was successful")
                                    //status.notifyObserver()
                                }
                            count2++
                            Log.d("Count", "Count1" + count2)
                        }

                    firestoreRepository.deleteTeamJoinRequestFromUser(userid2, tName)
                        .addOnSuccessListener {
                            Log.d("Test", "Inside deleteTeamJoinReqFromUser")
                            count2++
                            Log.d("Count", "Count2" + count2)

                            Log.d("del2", "Deleted the Request Successfully")
                            status.value = true
                            //status.notifyObserver()
                        }

                    firestoreRepository.deleteJoinRequestFromTeam(userid2, tName)
                        .addOnSuccessListener {

                            Log.d("Test", "Inside deleteJoinReqFromTeam")

                            count2++
                            Log.d("Count", "Count3" + count2)

                            Log.d("del2", "Deletion of Join Req from team was successful")
                            status.value = true
                            //status.notifyObserver()
                        }

                    firestoreRepository.addCurrentTeams(userid2, tName)
                        .addOnSuccessListener {
                            Log.d("Test", "Inside addCurrentTeams")
                            count2++
                            Log.d("Count", "Count4" + count2)

                            Log.d("Add", "User was added to the team successfully")
                            status.value = true
                            //status.notifyObserver()
                        }

                    firestoreRepository.addTeamMember(userid2, tName)
                        .addOnSuccessListener {
                            Log.d("Test", "Inside addTeamMember")
                            count2++
                            Log.d("Count", "Count5" + count2)

                            Log.d("count", "c1 " + count2)

                            status.value = true
                            status.notifyObserver()
                            Log.d("Add", "User was added to the team successfully")

                            if (count2 == 5) {

                                firestoreRepository.getTeam(tName)
                                    .addOnSuccessListener {
                                        val team = it.toObject<Team>()
                                        val tourneys = team?.currentTournaments?.keys
                                        for(tourney in tourneys!!){
                                            addTournament(tName,tourney, "Request",item.uId)
                                        }
                                    }

                                invitesList.value?.remove(item)
                                invitesList.notifyObserver()

                                requestList.value?.remove(item)
                                requestList.notifyObserver()

                                messageLiveData2.value = "Player has been added to the team"
                                //messageLiveData2.notifyObserver()

                            } else {
                                messageLiveData2.value = "Unable to process request. Please try again later"
                                //messageLiveData2.notifyObserver()
                            }
                        }
                }

                else{
                    messageLiveData2.value = "User is already part of a team"
                    //messageLiveData2.notifyObserver()
                }
            }
        return messageLiveData2
    }

    fun declineUser(item: InvitesRequest) {

        firestoreRepository.updateUserData(item.uId, mapOf("teamJoinRequests" to FieldValue.arrayRemove(item.teamName.trim())))
            .addOnSuccessListener {
                Log.d("Test","uid"+item.uId +" "+item.teamName)
                Log.d("del3","teamJoinReq for user was deleted successfully")
            }

        firestoreRepository.updateTeamData(item.teamName.trim(), mapOf("joinRequests" to FieldValue.arrayRemove(item.uId)))
            .addOnSuccessListener {
                Log.d("del3","teamInvite for user was deleted successfully")
            }

        requestList.value?.remove(item)
        requestList.notifyObserver()
        messageLiveData4.value = "User Request was declined"
    }

    fun declineInvite(item: InvitesRequest){

        firestoreRepository.updateUserData(sharedPrefsRepository.user.uid, mapOf("teamInvites" to FieldValue.arrayRemove(item.teamName)))
            .addOnSuccessListener {
                Log.d("del3","teamJoinReq for user was deleted successfully")
            }

        firestoreRepository.updateTeamData(item.teamName.trim(), mapOf("invitedMembers" to FieldValue.arrayRemove(sharedPrefsRepository.user.uid)))
            .addOnSuccessListener {
                Log.d("del3","InvitedMembers from team was deleted successfully")
            }
        messageLiveData3.value = "Invite has been Declined"

        invitesList.value?.remove(item)
        invitesList.notifyObserver()

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
                if (tournament?.active!!) {
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
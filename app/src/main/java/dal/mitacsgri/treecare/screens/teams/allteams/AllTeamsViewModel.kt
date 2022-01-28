package dal.mitacsgri.treecare.screens.teams.allteams

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import dal.mitacsgri.treecare.extensions.getCardItemDescriptorText
import dal.mitacsgri.treecare.extensions.notifyObserver
import dal.mitacsgri.treecare.extensions.toArrayList
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository

class AllTeamsViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository
): ViewModel() {
    val teamsLiveData = MutableLiveData<ArrayList<Team>>()
    fun getAllTeams(): LiveData<ArrayList<Team>> {


        firestoreRepository.getAllTeams()
            .addOnSuccessListener {
                //teamsLiveData.value = it.toObjects()
                teamsLiveData.value = it.toObjects<Team>().filter { it.exist }.toArrayList()
                teamsLiveData.notifyObserver()
            }
            .addOnFailureListener {
            }

        return teamsLiveData
    }

    fun getMembersCountText(team: Team) =
        getCardItemDescriptorText("Members", team.members.size.toString())

    fun getCaptainNameText(team: Team) =
        getCardItemDescriptorText("Captain", team.captainName)

    fun isUserCaptain(captainUid: String) = captainUid == sharedPrefsRepository.user.uid

    fun sendJoinRequest(teamName: String, action: (status: String) -> Unit) {
        val uid = sharedPrefsRepository.user.uid


        if(sharedPrefsRepository.user.currentTeams.isEmpty()) {
            firestoreRepository.updateTeamData(teamName, mapOf("joinRequests" to FieldValue.arrayUnion(uid)))
                .addOnSuccessListener {
                    Log.d("Join request", "sent")
                    firestoreRepository.updateUserData(uid, mapOf("teamJoinRequests" to FieldValue.arrayUnion(teamName)))
                        .addOnSuccessListener {
                            action("true")
                        }
                        .addOnFailureListener {
                            action("false")
                            firestoreRepository.updateTeamData(
                                teamName,
                                mapOf("joinRequests" to FieldValue.arrayRemove(uid))
                            )
                        }
                }
                .addOnFailureListener {
                    Log.d("Join request", "failed")
                    action("false")
                }
        }

        else{
            action("teamexists")
        }
    }

        //commented this part since user may send request to more than one team and same uid cannot be added to
        // db twice. MutableMap or Map can be an alternative
        /*firestoreRepository.getTeam(teamName)
            .addOnSuccessListener {
            val team = it.toObject<Team>()
                val captainId = team?.captain
                firestoreRepository.updateUserData(captainId.toString(), mapOf("userJoinRequests" to FieldValue.arrayUnion(uid)))
                //firestoreRepository.updateUserJoinRequestInUser(captainId.toString(), team?.name.toString(), uid)
                    .addOnSuccessListener {
                        Log.d("sendRequest","userreq sent to captain")
                        action(true)
                    }
                    .addOnFailureListener{
                        action(false)
                    }

        }*/






    fun cancelJoinRequest(teamName: String, action: (status: String) -> Unit) {
        val uid = sharedPrefsRepository.user.uid

        firestoreRepository.updateTeamData(teamName,
            mapOf("joinRequests" to FieldValue.arrayRemove(uid)))
            .addOnSuccessListener {
                Log.d("Join request", "cancelled")
                firestoreRepository.updateUserData(uid,
                    mapOf("teamJoinRequests" to FieldValue.arrayRemove(teamName)))
                    .addOnSuccessListener {
                        action("true")
                    }
                    .addOnFailureListener {
                        action("false")
                        firestoreRepository.updateTeamData(teamName,
                            mapOf("joinRequests" to FieldValue.arrayUnion(uid)))
                    }
            }
            .addOnFailureListener {
                Log.d("Join request cancel", "failed")
                action("false")
            }
    }

    fun isJoinRequestSent(team: Team) = team.joinRequests.contains(sharedPrefsRepository.user.uid)

    fun teamExist(): Boolean {
        return sharedPrefsRepository.team.name.isNotEmpty()
    }
}
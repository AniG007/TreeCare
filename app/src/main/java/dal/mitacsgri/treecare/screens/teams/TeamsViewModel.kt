package dal.mitacsgri.treecare.screens.teams

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.notifyObserver
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository

class TeamsViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository
): ViewModel() {

    fun hasTeamRequests(): MutableLiveData<Boolean> {
        //This is for checking whether the user has team requests or invites
        //Log.d("Test","InsidehasTeamReq")
        val status = MutableLiveData<Boolean>().default(false)
        val count = MutableLiveData<Int>().default(0)

        firestoreRepository.getUserData(sharedPrefsRepository.user.uid)
            .addOnSuccessListener {
                //Log.d("Test","InsideDB Call")
                val user = it.toObject<User>()
                //Log.d("teamName","tnames"+ user?.captainedTeams)
                if(user?.captainedTeams!!.isNotEmpty()) {
                    //for loop not needed since user can be only in 1 team. But may be useful when that restriction is lifted off
                    for (i in user.captainedTeams) {
                        //Log.d("teamName", "TNAME" + i)
                        firestoreRepository.getTeam(i)
                            .addOnSuccessListener {
                                val team = it.toObject<Team>()
                                //Log.d("Count", "jrcount" + team?.joinRequests?.count())
                                if (team?.joinRequests?.count()!! > 0) {
                                    count.value = 1
                                    //count.notifyObserver()
                                    //Log.d("Count", "count1 " + count.value)
                                }
                                user.let {
                                    sharedPrefsRepository.user = user
                                    //Log.d("Count", "count4" + count.value)
                                    //status.value = user.userJoinRequests.isNotEmpty() || user.teamInvites.isNotEmpty()
                                    //Log.d("Count", "tInvite" + user.teamInvites)
                                    status.value =
                                        (count.value!! > 0) || user.teamInvites.isNotEmpty()
                                    status.notifyObserver()
                                }
                            }
                    }
                }
                else{
                    //When a user doesn't have any team, this code is to check if the user just has invites
                    user.let {
                        sharedPrefsRepository.user = user
                        //Log.d("Count", "count4" + count.value)
                        //status.value = user.userJoinRequests.isNotEmpty() || user.teamInvites.isNotEmpty()
                        //Log.d("Count", "tInvite" + user.teamInvites)
                        status.value = user.teamInvites.isNotEmpty()
                        status.notifyObserver()
                    }

                }
            }
        return status
    }

  /*  fun hasTeamRequests1(): MutableLiveData<Boolean> {
        val status = MutableLiveData<Boolean>().default(false)
        firestoreRepository.getUserData(sharedPrefsRepository.user.uid)
            .addOnSuccessListener {
                val user = it.toObject<User>()

                user?.let {
                    sharedPrefsRepository.user = user
                    status.value = user.userJoinRequests.isNotEmpty() || user.teamInvites.isNotEmpty()
                }
            }
        return status
    }*/
}
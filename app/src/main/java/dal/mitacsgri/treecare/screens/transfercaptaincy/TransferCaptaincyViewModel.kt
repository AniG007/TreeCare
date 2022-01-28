package dal.mitacsgri.treecare.screens.transfercaptaincy

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.notifyObserver
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.TeamInfo
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository

class TransferCaptaincyViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository
):ViewModel() {

    val userList = MutableLiveData<ArrayList<User>>().default(arrayListOf())
    val status : MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val userHolder = MutableLiveData<ArrayList<String>>().default(arrayListOf())
    val toast = MutableLiveData<String>().default("")
    val display = MutableLiveData<Boolean>().default(false)

    fun getUsersForTeam(teamName: String):MutableLiveData<ArrayList<User>>{
        firestoreRepository.getTeam(teamName)
            .addOnSuccessListener {
                val team = it.toObject<Team>()
                val members = team?.members
                for(member in members!!){
                    firestoreRepository.getUserData(member)
                        .addOnSuccessListener {
                            val user= it.toObject<User>()
                            if(user?.uid!= sharedPrefsRepository.user.uid) {
                            userList.value?.add(user!!)
                            userList.notifyObserver()
                            }
                        }
                }
            }
        return userList
    }

    fun addUserToList (uid: String) {
        Log.d("Test", "uid"+ uid)
        userHolder.value?.add(uid)
        Log.d("Test","Adding "+ userHolder.value.toString())
    }

    fun removeUserFromList (uid: String){
        Log.d("Test", "uid"+ uid)
        userHolder.value?.remove(uid)
        Log.d("Test","removing "+ userHolder.value.toString())
    }

    fun singleSelectionCheck(): MutableLiveData<String>{
        display.value = false
        if(userHolder.value?.count()!! > 1){
            toast.value = "Select only 1 person"
            display.value = true
            Log.d("Test", toast.value)
        }

        else if(userHolder.value?.count() == 0){
            toast.value = "Select at least one option"
            display.value = true
            Log.d("Test", toast.value)
        }

        else{
            toast.value = "You're not a captain anymore"
            removeUserAsCaptain(userHolder.value.toString().removeSurrounding("[","]"))
            display.value = true
            Log.d("Test", toast.value)
        }
        return toast
    }

    fun check(uid: String): Boolean{
        return uid == sharedPrefsRepository.user.uid
    }

    fun removeUserAsCaptain(newCaptain: String){

        firestoreRepository.updateTeamData(sharedPrefsRepository.team.name, mapOf("captain" to newCaptain))
            .addOnSuccessListener {
                firestoreRepository.updateUserData(sharedPrefsRepository.user.uid, mapOf("captainedTeams" to FieldValue.arrayRemove(sharedPrefsRepository.user.captainedTeams[0].removeSurrounding("[","]"))))
                    .addOnSuccessListener {
                        firestoreRepository.updateUserData(newCaptain, mapOf("captainedTeams" to FieldValue.arrayUnion(sharedPrefsRepository.team.name)))
                            .addOnSuccessListener {
                                sharedPrefsRepository.team.captain = newCaptain
                                firestoreRepository.getUserData(newCaptain)
                                    .addOnSuccessListener {
                                        val user = it.toObject< User>()
                                        sharedPrefsRepository.team.captainName = user?.name!!
                                        firestoreRepository.updateTeamData(sharedPrefsRepository.team.name, mapOf("captainName" to user.name))
                                    }
                            }
                    }
            }
    }
}
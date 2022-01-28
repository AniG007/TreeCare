package dal.mitacsgri.treecare.screens.createteam

import android.text.Editable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository

/**
 * Created by Devansh on 16-07-2019
 * Changes made by Anirudh
 */

class CreateTeamViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository
) : ViewModel() {

    var isNameValid = false

    val messageLiveData = MutableLiveData<String>()
    val isFullDataValid = MutableLiveData<Boolean>().default(false)

    fun checkAllInputFieldsValid(): Boolean {
        isFullDataValid.value = isNameValid
        return isFullDataValid.value ?: false
    }

    fun createTeam(name: Editable?, description: Editable?, action: () -> Unit) {

        if (checkAllInputFieldsValid()) {
            //checking if the team already exists or the user is already in a team
            firestoreRepository.getTeam(name.toString()).addOnSuccessListener {
                if (it.exists()) {
                    messageLiveData.value = "Team name already in use"
                    Log.d("Test","printing team"+ sharedPrefsRepository.team)
                    Log.d("Test","printing team"+ sharedPrefsRepository.user.currentTeams)
                    //return@addOnSuccessListener
                }

                else if(sharedPrefsRepository.team.name.isNotEmpty()){

                    messageLiveData.value = "You can only be a part of one team"
                    //return@addOnSuccessListener
                }

                else {
                    firestoreRepository.storeTeam(
                        Team(name = name.toString(),
                            description = description.toString(),
                            captain = sharedPrefsRepository.user.uid,
                            captainName = sharedPrefsRepository.user.name,
                            members = arrayListOf(sharedPrefsRepository.user.uid)
                            ))

                    {
                        firestoreRepository.updateUserData(sharedPrefsRepository.user.uid, mapOf("captainedTeams" to FieldValue.arrayUnion(name.toString())))
                            .addOnSuccessListener {
                                firestoreRepository.updateUserData(sharedPrefsRepository.user.uid, mapOf("currentTeams" to FieldValue.arrayUnion(name.toString())))
                                    .addOnSuccessListener {
                                        firestoreRepository.getTeam(name.toString())
                                            .addOnSuccessListener {
                                                sharedPrefsRepository.team = it.toObject<Team>()!!
                                                val user = sharedPrefsRepository.user
                                                Log.d("Test","Tname "+ sharedPrefsRepository.team.name)
                                                user.currentTeams.add(sharedPrefsRepository.team.name)
                                                sharedPrefsRepository.user = user
                                            }
                                        Log.d("TAG", "Team Name has been added")
                                    }
                            }
                            .addOnFailureListener{
                                Log.d("TAG","Team Name could not be added for Captain")
                            }
                        messageLiveData.value = if (it) "Team created successfully"
                                                else "Team creation failed"

                        action()
                    }

                }
            }
        }
    }
}
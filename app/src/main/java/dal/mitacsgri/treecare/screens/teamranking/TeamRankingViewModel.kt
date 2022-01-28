package dal.mitacsgri.treecare.screens.teamranking

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.notifyObserver
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.TeamInfo
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import org.joda.time.DateTime

class TeamRankingViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository
)
    : ViewModel() {

    val membersList = MutableLiveData<ArrayList<TeamInfo>>().default(arrayListOf())
    val status: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val bool = MutableLiveData<Boolean>().default(false)

    fun getTeamMembers(teamName: String, tournamentName: String): MutableLiveData<ArrayList<TeamInfo>> {
        firestoreRepository.getTeam(teamName)
            .addOnSuccessListener {

                val team = it.toObject<Team>()

                for (m in team?.members!!) {
                    Log.d("Test", "m " + m)
                    firestoreRepository.getUserData(m)
                        .addOnSuccessListener {
                            val user = it.toObject<User>()
                            val userStepMap = user?.currentTournaments!![tournamentName]?.dailyStepsMap?.values
                            var totalStepsForATournament = 0

                            if (userStepMap != null) {
                                for (step in userStepMap){
                                    totalStepsForATournament += step
                                    Log.d("Steps", step.toString())
                                }
                            }

                            membersList.value?.add(
                                TeamInfo(
                                    user.uid,
                                    teamName,
                                    team.captain,
                                    user.name,
                                    totalStepsForATournament,
                                    user.photoUrl,
                                    totalStepsForATournament / 3000
                                )
                            )

                            membersList.value?.sortList()
//                                TeamInfo(
//                                    user.uid,
//                                    teamName,
//                                    team.captain,
//                                    user.name,
//                                    user.dailySteps,
//                                    user.photoUrl,
//                                    user.dailySteps / 3000
//                                )

                            membersList.notifyObserver()
                        }
                }
            }
        return membersList
    }

    private fun ArrayList<TeamInfo>.sortList() {

        sortByDescending {
            it.stepsCount
        }
    }

    fun isCurrentUser(tInfo: TeamInfo) = tInfo.uId == sharedPrefsRepository.user.uid
}
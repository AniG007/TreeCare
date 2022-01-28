package dal.mitacsgri.treecare.screens.profile

import android.content.Context
import android.util.Log
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
//import com.crashlytics.android.Crashlytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.UserChallengeTrophies
import dal.mitacsgri.treecare.model.UserTournamentTrophies
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository

class ProfileViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository
) : ViewModel() {

    val trophiesCountData = MutableLiveData<Triple<Int, Int, Int>>().default(Triple(0, 0, 0))
    val teamTrophiesCountData = MutableLiveData<Triple<Int, Int, Int>>().default(Triple(0, 0, 0))

    fun getUserPhotoUrl() = sharedPrefsRepository.user.photoUrl

    fun getUserFullName() = sharedPrefsRepository.user.name

    fun getUserFirstName() = sharedPrefsRepository.user.name.split(' ')[0]

    fun getDailyGoalCompletionStreakCount() = sharedPrefsRepository.dailyGoalStreak

    fun getWeeklyDailyGoalAchievedCount() = sharedPrefsRepository.dailyGoalAchievedCount

    fun getCurrentWeekDayForStreak() = sharedPrefsRepository.currentDayOfWeek

    fun getGoalCompletionStreakData(): Array<Boolean> {
        val goalCompletionString = sharedPrefsRepository.dailyGoalStreakString
        val boolArray = arrayOf(false, false, false, false, false, false, false)

        for (i in 0 until goalCompletionString.length) {
            boolArray[i] = goalCompletionString[i] == '1'
        }

        return boolArray
    }

    fun getDailyGoalStreakText() = buildSpannedString {
        val count = getWeeklyDailyGoalAchievedCount()
        append("You achieved ")
        bold {
            append("$count daily " +
                    if (getWeeklyDailyGoalAchievedCount() > 1) "goals" else "goal")
        }
        append(" in ")
        bold {
            append("the last ${sharedPrefsRepository.currentDayOfWeek + 1} " +
                    if ((sharedPrefsRepository.currentDayOfWeek + 1) > 1) "days" else "day")
        }
    }

    fun getTrophiesCount() {
        firestoreRepository.getTrophiesData(sharedPrefsRepository.user.uid)
            .addOnSuccessListener {
                val userTrophies = it.toObject<UserChallengeTrophies>()
                userTrophies?.let {
                    trophiesCountData.value = Triple(
                        userTrophies.gold.size,
                        userTrophies.silver.size,
                        userTrophies.bronze.size)
                }
            }
            .addOnFailureListener {
                Log.d("Profile", "Failed to obtain trophies data")
            }
    }

    fun getTeamTrophiesCount() {
        //sharedPrefsRepository.team = Team()

        var teamData = sharedPrefsRepository.team
        Log.d("Test",sharedPrefsRepository.user.currentTeams.toString().removeSurrounding("[","]"))
        teamData.name = sharedPrefsRepository.user.currentTeams.toString().removeSurrounding("[","]")
        sharedPrefsRepository.team = teamData

//        firestoreRepository.getTeam(sharedPrefsRepository.user.currentTeams.toString().removeSurrounding("[","]"))
//            .addOnSuccessListener {
//                val team = it.toObject<Team>()
//                teamData = team!!
//                sharedPrefsRepository.team = teamData
//            }

        Log.d("Test", sharedPrefsRepository.team.name)

        if(!sharedPrefsRepository.team.name.isEmpty()) {
            firestoreRepository.getTeamTrophiesData(sharedPrefsRepository.team.name)
                .addOnSuccessListener {
                    val teamTrophies = it.toObject<UserTournamentTrophies>()
                    teamTrophies?.let {
                        teamTrophiesCountData.value = Triple(
                            teamTrophies.gold.size,
                            teamTrophies.silver.size,
                            teamTrophies.bronze.size
                        )
                    }
                }
                .addOnFailureListener {
                    Log.d("Profile", "Failed to obtain team trophies data " + it)
                }
//            firestoreRepository.getTeam(sharedPrefsRepository.team.name)
//                .addOnSuccessListener {
//                    val team = it.toObject<Team>()
//                    sharedPrefsRepository.team = team!!
//                    Log.d("Test", sharedPrefsRepository.team.toString())
//                }
        }
        Log.d("Test", sharedPrefsRepository.team.toString())
    }

    fun updateUserName(newName: String, successAction: () -> Unit) {
        firestoreRepository.updateUserData(sharedPrefsRepository.user.uid, mapOf("name" to newName))
            .addOnSuccessListener {
                updateUserNameInSharedPrefs(newName)
                successAction()
                Log.d("UserName", "Name changed to $newName")
            }
            .addOnFailureListener {
                Log.d("UserName", "Name change failed")
            }
    }

    fun logout(context: Context) {
        sharedPrefsRepository.clearSharedPrefs()
        Fitness.getConfigClient(context, GoogleSignIn.getLastSignedInAccount(context)!!).disableFit()
        val crashlytics = FirebaseCrashlytics.getInstance()
//        Crashlytics.setUserIdentifier("")
//        Crashlytics.setUserName("")
//        Crashlytics.setUserEmail("")
        crashlytics.setUserId("")
        //TODO: Maybe cancellation of jobs is needed
    }

    private fun updateUserNameInSharedPrefs(newName: String) {
        val user = sharedPrefsRepository.user
        user.name = newName
        sharedPrefsRepository.user = user
    }
}

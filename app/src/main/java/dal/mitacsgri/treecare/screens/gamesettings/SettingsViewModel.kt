package dal.mitacsgri.treecare.screens.gamesettings

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.getMapFormattedDate
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat

/**
 * Created by Devansh on 24-06-2019
 */

class SettingsViewModel(
    private val sharedPrefRepository: SharedPreferencesRepository,
    private val firestoreRepository: FirestoreRepository
    ): ViewModel() {

    val dailyGoal = MutableLiveData<Int>().default(5000)
    val settingsChanged = MutableLiveData<Boolean>().default(false)

    var hasSettingsChanged
        get() = settingsChanged.value ?: true
        set(value) {
            settingsChanged.value = value
        }

    fun getCurrentVolume() = (sharedPrefRepository.volume*10).toInt()

    fun getCurrentDailyStepsGoal() = sharedPrefRepository.user
        .dailyGoalMap[DateTime().plusDays(1).getMapFormattedDate()] ?: 5000

    fun saveSettings(volume: Int, updatedStepGoal: Int, action: () -> Unit) {
        val oldVolume = sharedPrefRepository.volume
        val newVolume = volume / 10f

        sharedPrefRepository.volume = newVolume
        storeUpdatedStepGoalInPrefs(updatedStepGoal)

        //Doing this to prevent database update when changing only volume
        if (oldVolume == newVolume) {
            firestoreRepository.storeUser(sharedPrefRepository.user)
                .addOnSuccessListener {
                    action()
                }
                .addOnFailureListener {
                    Log.d("Goal update in DB", "failed: $it")
                }
        } else
            action()
    }

    fun getGameMode() = sharedPrefRepository.gameMode

    private fun storeUpdatedStepGoalInPrefs(updatedStepGoal: Int) {
        val user = sharedPrefRepository.user
        //Actually, this will be the date from which the new goal will be applicable
        val lastGoalChangeTime = DateTime().plusDays(1).withTimeAtStartOfDay().millis
        //Set the goal as updated daily goal for the next day
        user.dailyGoalMap[DateTime(lastGoalChangeTime).getMapFormattedDate()] = updatedStepGoal
        user.lastGoalChangeTime = lastGoalChangeTime

        completeUserDailyGoalMap(user, updatedStepGoal)

        sharedPrefRepository.user = user
        sharedPrefRepository.storeDailyStepsGoal(updatedStepGoal)
    }

    //This function will add missing daily step goal dal.mitacsgri.treecare.data if user does not opens app for many days
    // TODO: 2020-11-26 Function logic seems to be incorrect. This does not happen. Also, this part of code is run only when the user clicks the save button on the settings screen
    private fun completeUserDailyGoalMap(user: User, updatedStepGoal: Int) {
        val dailyGoalMap = user.dailyGoalMap

        var keysList = mutableListOf<String>()
        dailyGoalMap.keys.forEach {
            keysList.add(it)
        }
        keysList = keysList.sorted().toMutableList()

        //This needs to be done only for the last element, because the map is done every time,
        //So every time only last element needs to be checked
        val lastTime = keysList[keysList.size-1]
        val lastDate = DateTime.parse(lastTime, DateTimeFormat.forPattern("yyyy/MM/dd"))
        val days = Days.daysBetween(lastDate, DateTime()).days

        val oldGoal = dailyGoalMap[lastTime]

        //The days for which no change in daily goal was made by the user are filled by the last daily goal

        for (i in 1..days) {
            val key = DateTime(lastTime).plusDays(i).getMapFormattedDate()
            dailyGoalMap[key] = oldGoal!!
        }

        dailyGoalMap[DateTime(user.lastGoalChangeTime).getMapFormattedDate()] = updatedStepGoal
        user.dailyGoalMap = dailyGoalMap.toSortedMap()
    }
}

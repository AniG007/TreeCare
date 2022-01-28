package dal.mitacsgri.treecare.screens

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.getMapFormattedDate
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.time.Month
import java.util.*

/**
 * Created by Devansh on 20-06-2019
 */

class StepCountDataProvidingViewModel(
    private val sharedPrefsRepository: SharedPreferencesRepository,
    private val stepCountRepository: StepCountRepository)
    : ViewModel(), KoinComponent {

    private val db: FirestoreRepository by inject()

    val isLoginDone = sharedPrefsRepository.isLoginDone

    //This variable is accessed synchronously. The moment its value reaches 2, we move to new fragment
    //Value 2 means both the steps counts have been obtained
    val stepCountDataFetchedCounter = MutableLiveData<Int>().default(0)

    fun storeDailyStepsGoal(goal: Int) {
        sharedPrefsRepository.storeDailyStepsGoal(goal)
    }

    fun resetDailyGoalCheckedFlag() {
        //Will execute only once in each day, when the app is opened for the first time in the day
        if (sharedPrefsRepository.lastOpenedDayPlus1 < Date().time) {
            sharedPrefsRepository.isDailyGoalChecked = 0
            sharedPrefsRepository.isGoalCompletionSteakChecked = false

            val timeToStore = DateTime().plusDays(1).withTimeAtStartOfDay().millis

            Log.v("Current time: ", Date().time.toString())
            Log.v("Time to store: ", timeToStore.toString())

            sharedPrefsRepository.apply {
                lastOpenedDayPlus1 = timeToStore
                lastLeafCount = currentLeafCount
            }

        }
    }

    fun accessStepCountDataUsingApi() {

        /** Goal as of now is being changed immediately. The stepmap generation in the settings screen can be used if goal settings needs to reflect the next day */
        stepCountRepository.apply {

            //Get aggregate step count up to the last day
            getStepCountDataOverARange(
                DateTime(sharedPrefsRepository.user.firstLoginTime).withTimeAtStartOfDay().millis,
                DateTime().withTimeAtStartOfDay().millis
            ) {

                if (!sharedPrefsRepository.isGoalCompletionSteakChecked) {
                    calculateFruitsOnTree(it as MutableMap<Long, Int>)
                    calculateDailyGoalStreak(sharedPrefsRepository.currentDayOfWeek, it)
                }

                increaseStepCountDataFetchedCounter()

                val dailyGoalMap = sharedPrefsRepository.user.dailyGoalMap

                //Updated the daily goal stored in SharedPrefs to display in Unity
                if (sharedPrefsRepository.isDailyGoalChecked == 0) {
                    sharedPrefsRepository.storeDailyStepsGoal(
                        //dailyGoalMap[DateTime().withTimeAtStartOfDay().millis.toString()] ?: 5000)
                        dailyGoalMap.values.last())
                }

                expandDailyGoalMapIfNeeded(sharedPrefsRepository.user)

                var totalLeafCountTillLastDay = 0
                it.forEach { (date, stepCount) ->
                    val goal = sharedPrefsRepository.user.dailyGoalMap.values.last()
                    totalLeafCountTillLastDay +=
                        calculateLeafCountFromStepCount(stepCount, goal)
                    Log.d("Date: $date", "StepCount: $stepCount")
                }

                var currentLeafCount = totalLeafCountTillLastDay
                //Add today's leaf count to leafCountTillLastDay
                //Call needs to be made here because it uses dal.mitacsgri.treecare.data from previous call

                getTodayStepCountData {
                    currentLeafCount += it/1000
                    sharedPrefsRepository.currentLeafCount = currentLeafCount
                    sharedPrefsRepository.storeDailyStepCount(it)
                    increaseStepCountDataFetchedCounter()
                }
            }
        }
    }

    private fun increaseStepCountDataFetchedCounter() {
        synchronized(stepCountDataFetchedCounter) {
            stepCountDataFetchedCounter.value = stepCountDataFetchedCounter.value?.plus(1)
            Log.d("Counter value", stepCountDataFetchedCounter.value.toString())
        }
    }

    private fun calculateLeafCountFromStepCount(stepCount: Int, dailyGoal: Int): Int {
        var leafCount = stepCount / 1000
        if (stepCount < dailyGoal) {
            leafCount -= Math.ceil((dailyGoal - stepCount) / 1000.0).toInt()
            if (leafCount < 0) leafCount = 0
        }
        return leafCount
    }

    private fun calculateFruitsOnTree(stepCountMap: MutableMap<Long, Int>) {
        stepCountMap.toSortedMap()
        var currentDay = 0
        var goalAchievedStreak = arrayOf(false, false, false, false, false, false, false)
        val fullStreak = arrayOf(true, true, true, true, true, true, true)
        sharedPrefsRepository.currentFruitCount = 0
        val stepMapSplit = stepCountMap.size / 7

        //get the quotient . Iterate every 7 days for quotient count. Reminder days are left and are not considered for fruit. // This is done in the basis of rolling weeks with the start date as the users' first login time
        for(i in 0 until stepMapSplit) {
            stepCountMap.entries.take(7).forEach {(_,stepCount)->

                goalAchievedStreak[currentDay] =
                    stepCount >= sharedPrefsRepository.getDailyStepsGoal()

                currentDay++

                if (currentDay == 7) {
                    sharedPrefsRepository.lastFruitCount = sharedPrefsRepository.currentFruitCount

                    if (goalAchievedStreak.contentEquals(fullStreak)) {
                        sharedPrefsRepository.currentFruitCount += 1
                        goalAchievedStreak =
                            arrayOf(false, false, false, false, false, false, false)
                    } else {
                        sharedPrefsRepository.currentFruitCount -= 1
                        goalAchievedStreak =
                            arrayOf(false, false, false, false, false, false, false)
                    }
                    currentDay = 0
                }
            }
            for(j in 1..7){
                stepCountMap.remove(stepCountMap.keys.first())
            }
        }
        if(sharedPrefsRepository.currentFruitCount < 0)
            sharedPrefsRepository.currentFruitCount = 0
        sharedPrefsRepository.currentDayOfWeek = currentDay
    }

    private fun expandDailyGoalMapIfNeeded(user: User) {
        val dailyGoalMap = user.dailyGoalMap
        var keysList = mutableListOf<String>()
        dailyGoalMap.keys.forEach {
            keysList.add(it)
        }
        keysList = keysList.sorted().toMutableList()

        user.dailyGoalMap = dailyGoalMap.toSortedMap()
        Log.d("dailyGoalMap","${user.dailyGoalMap}")
        sharedPrefsRepository.user = user
    }

    private fun calculateDailyGoalStreak(currentDay: Int, stepCountMap: Map<Long, Int>) {
        val mapSize = stepCountMap.size
        val mapKeys = stepCountMap.keys.sorted()
        var streakCount = 0
        var dailyGoalAchievedCount = 0
        val dailyGoal = sharedPrefsRepository.getDailyStepsGoal()
        val goalStreakStringBuilder = StringBuilder(sharedPrefsRepository.dailyGoalStreakString)

        for (i in 0..6) {
            goalStreakStringBuilder[i] = '0'
        }

        if (mapSize > 0) {
            for (i in (mapSize - (currentDay)) until mapSize) {
                if (stepCountMap[mapKeys[i]] ?: error("No data for key ${mapKeys[i]}") >= dailyGoal) {
                    streakCount++
                } else {
                    streakCount = 0
                    break
                }
            }

            val startIndex = mapSize - currentDay
            for (i in (mapSize - (currentDay)) until mapSize) {
                if (stepCountMap[mapKeys[i]] ?: error("No data for key ${mapKeys[i]}") >= dailyGoal) {
                    goalStreakStringBuilder[i - startIndex] = '1'
                    dailyGoalAchievedCount++
                }
            }
        }

        sharedPrefsRepository.dailyGoalStreak = streakCount
        sharedPrefsRepository.dailyGoalAchievedCount = dailyGoalAchievedCount
        sharedPrefsRepository.dailyGoalStreakString = goalStreakStringBuilder.toString()
        sharedPrefsRepository.isGoalCompletionSteakChecked = true
    }

    //Remove this function if all the users have moved to a version greater than the first version
    private fun fixDailyGoalMap() {
        if (!sharedPrefsRepository.isDailyGoalMapFixed) {
            sharedPrefsRepository.isDailyGoalMapFixed = true

            val dailyGoalMap = sharedPrefsRepository.user.dailyGoalMap
            val fixedMap = mutableMapOf<String, Int>()

            dailyGoalMap.forEach { (key, value) ->
                val date = DateTime(key.toLong())
                fixedMap[date.getMapFormattedDate()] = value
            }

            sharedPrefsRepository.myMap = dailyGoalMap.toString()

            val newSortedMap = fixedMap.toSortedMap()

            db.updateUserData(sharedPrefsRepository.user.uid,
                mapOf("dailyGoalMap" to newSortedMap))

            val user = sharedPrefsRepository.user
            user.dailyGoalMap = newSortedMap
            sharedPrefsRepository.user = user
        }
    }
}
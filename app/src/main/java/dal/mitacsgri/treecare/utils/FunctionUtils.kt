import dal.mitacsgri.treecare.extensions.getMapFormattedDate
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

/**
 * Created by Devansh on 03-08-2019
 */

fun calculateLeafCountFromStepCount(stepCount: Int, dailyGoal: Int): Int {
    var leafCount = stepCount / 1000
    if (stepCount < dailyGoal) {
        leafCount -= Math.ceil((dailyGoal - stepCount) / 1000.0).toInt()
        if (leafCount < 0) leafCount = 0
    }
    return leafCount
}

fun calculateLeafCountFromStepCountForTeam(stepCount: Int, dailyGoal: Int): Int {
    var leafCount = stepCount / 3000
    if (stepCount < dailyGoal) {
        leafCount -= Math.ceil((dailyGoal - stepCount) / 3000.0).toInt()
        if (leafCount < 0) leafCount = 0
    }
    return leafCount
}

fun calculateDailyGoalsAchievedFromStepCountForTeam (stepCount: Int, dailyGoal:Int): Int{
    var dailyGoalsAchieved = 0

    if(stepCount >= dailyGoal){
        dailyGoalsAchieved +=1
    }
    return dailyGoalsAchieved
}


fun expandDailyGoalMapIfNeeded(dailyGoalMap: MutableMap<String, Int>)
        : Map<String, Int> {

    var keysList = mutableListOf<String>()
    dailyGoalMap.keys.forEach {
        keysList.add(it)
    }
    keysList = keysList.sorted().toMutableList()

    val lastTime = keysList[keysList.size-1]
    val lastDate = DateTime.parse(lastTime, DateTimeFormat.forPattern("yyyy/MM/dd"))
    val days = Days.daysBetween(lastDate, DateTime()).days

    val oldGoal = dailyGoalMap[lastTime]

    for (i in 1..days) {
        val key = DateTime(lastTime).plusDays(i).getMapFormattedDate()
        dailyGoalMap[key] = oldGoal!!
    }
    return dailyGoalMap.toSortedMap()
}

fun getStartOfWeek(dateMillis: Long): Long {
    val startDate = LocalDate(dateMillis)
    val weekStartDate = startDate.withDayOfWeek(DateTimeConstants.MONDAY)
    return weekStartDate.toDateTimeAtCurrentTime().withTimeAtStartOfDay().millis
}

fun getStartOfMonth(dateMillis: Long): Long {
    val startDate = LocalDate(dateMillis)
    val monthStartDate = startDate.withDayOfMonth(1)
    return monthStartDate.toDateTimeAtCurrentTime().withTimeAtStartOfDay().millis
}
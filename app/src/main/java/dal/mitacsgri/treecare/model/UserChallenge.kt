package dal.mitacsgri.treecare.model

import com.google.firebase.Timestamp

data class UserChallenge(
    val name: String = "",
    val dailyStepsMap: MutableMap<String, Int> = mutableMapOf(),
    var totalSteps: Int = 0,
    var challengeGoalStreak: Int = 0,
    val joinDate: Long = 0,
    var isCurrentChallenge: Boolean = true,
    val type: Int = 0,
    var leafCount: Int = 0,
    var fruitCount: Int = 0,
    var currentDayOfWeek: Int = 0,
    val goal: Int = 0,
    var isActive: Boolean = true,
    val endDate: Timestamp = Timestamp.now(),
    var lastUpdateTime: Timestamp = Timestamp.now()
)
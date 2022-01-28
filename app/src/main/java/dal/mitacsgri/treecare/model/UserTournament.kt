package dal.mitacsgri.treecare.model

import com.google.firebase.Timestamp

data class UserTournament(
        val name: String = "",
        var dailyStepsMap: MutableMap<String, Int> = mutableMapOf(),
        var totalSteps: Int = 0,
        var tournamentGoalStreak: Int = 0,
        val joinDate: Long = 0,
        var isCurrentTournament: Boolean = true,
        val type: Int = 0,
        val teamName: String = "",
        var leafCount: Int = 0,
        var fruitCount: Int = 0,
        var currentDayOfWeek: Int = 0,
        var goal: Int = 0,
        var isActive: Boolean = true,
        var startDate:Timestamp = Timestamp.now(),
        var endDate: Timestamp = Timestamp.now(),
        var lastUpdateTime: Timestamp = Timestamp.now()
    )

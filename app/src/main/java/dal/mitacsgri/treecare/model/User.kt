package dal.mitacsgri.treecare.model

import dal.mitacsgri.treecare.extensions.getMapFormattedDate
import org.joda.time.DateTime

/**
 * Created by Devansh on 22-06-2019
 */

data class User(
    val uid: String = "",
    val isFirstRun: Boolean = false,
    val firstLoginTime: Long = 0,
    var name: String = "",
    val email: String = "",
    val captainedTeams: ArrayList<String> = arrayListOf(),
    var dailyGoalMap: MutableMap<String, Int> = mutableMapOf(
        DateTime(firstLoginTime).getMapFormattedDate() to 5000,
        DateTime(firstLoginTime).plusDays(1).getMapFormattedDate() to 5000
    ),
    var lastGoalChangeTime: Long = DateTime(firstLoginTime).withTimeAtStartOfDay().millis,
    var currentChallenges: MutableMap<String, UserChallenge> = mutableMapOf(),
    var currentTournaments: MutableMap<String, UserTournament> = mutableMapOf(),
    val currentTeams: ArrayList<String> = arrayListOf(),
    val teamInvites: ArrayList<String> = arrayListOf(),
    val userJoinRequests: ArrayList<String> = arrayListOf(),
    val stepMap: MutableMap<String, Int> = mutableMapOf(),
    val leafMap: MutableMap<String, Int> = mutableMapOf(),
    val teamJoinRequests: ArrayList<String> = arrayListOf(),
    val photoUrl: String = "",
    val dailyGoalStreakCount: Int = 0,
    val dailySteps: Int = 0,
    val tournamentsCreated: ArrayList<String> = arrayListOf()
)
package dal.mitacsgri.treecare.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.User
import org.json.JSONException

class SharedPreferencesRepository(val context: Context) {

    val sharedPref: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.unity_shared_preferences),
        Context.MODE_PRIVATE
    )

    //This value needs to be obtained from the database to check if the user is using the app for the first time or not
    var isFirstRun
        get() = getBoolean(R.string.is_first_run, true)
        set(value) {
            storeBoolean(R.string.is_first_run, value)
        }

    //This needs to be as int because this will be used by Unity and Unity does not support boolean prefs
    var isDailyGoalChecked
        get() = sharedPref.getInt(context.getString(R.string.daily_goal_checked), 0)
        set(value) {
            storeInt(R.string.daily_goal_checked, value)
        }

    var isGoalCompletionSteakChecked
        get() = getBoolean(R.string.goal_completion_streak_checked, false)
        set(value) {
            storeBoolean(R.string.goal_completion_streak_checked, value)
        }

    var isLoginDone: Boolean
        get() = sharedPref.getBoolean(context.getString(R.string.login_done), false)
        set(value) {
            with(sharedPref.edit()) {
                putBoolean(context.getString(R.string.login_done), value)
                apply()
            }
        }

    var lastOpenedDayPlus1: Long
        get() = sharedPref.getLong(context.getString(R.string.last_opened_day), 0)
        set(value) {
            with(sharedPref.edit()) {
                putLong(context.getString(R.string.last_opened_day), value)
                apply()
            }
        }

    var starterModeInstructionsDisplayed: Boolean
        get() = sharedPref.getBoolean(
            context.getString(R.string.starter_mode_instructions_displayed),
            false
        )
        set(value) {
            sharedPref.edit {
                putBoolean(context.getString(R.string.starter_mode_instructions_displayed), value)
            }
        }

    var challengerModeInstructionsDisplayed: Boolean
        get() = sharedPref.getBoolean(
            context.getString(R.string.challenger_mode_instructions_displayed),
            false
        )
        set(value) {
            sharedPref.edit {
                putBoolean(
                    context.getString(R.string.challenger_mode_instructions_displayed),
                    value
                )
            }
        }

    var tournamentModeInstructionsDisplayed: Boolean
        get() = sharedPref.getBoolean(
            context.getString(R.string.tournament_mode_instructions_displayed),
            false
        )
        set(value) {
            sharedPref.edit {
                putBoolean(
                    context.getString(R.string.tournament_mode_instructions_displayed),
                    value
                )
            }
        }


    var firstLoginTime: Long
        get() = sharedPref.getLong(context.getString(R.string.first_login_time_ms), 0)
        set(value) {
            with(sharedPref.edit()) {
                putLong(context.getString(R.string.first_login_time_ms), value)
                apply()
            }
        }

    var lastLoginTime: Long
        get() = sharedPref.getLong(context.getString(R.string.last_login_time_ms), 0)
        set(value) {
            with(sharedPref.edit()) {
                putLong(context.getString(R.string.last_login_time_ms), value)
                apply()
            }
        }

    var lastLogoutTime: Long
        get() = sharedPref.getLong(context.getString(R.string.last_logout_time_ms), 0)
        set(value) {
            with(sharedPref.edit()) {
                putLong(context.getString(R.string.last_logout_time_ms), value)
                apply()
            }
        }

    var lastLeafCount: Int
        get() = getInt(R.string.last_leaf_count)
        set(value) {
            storeInt(R.string.last_leaf_count, value)
        }

    var challengeActive: Int
        get() = getInt(R.string.challenge_active)
        set(value) {
            storeInt(R.string.challenge_active, value)
        }

    var tournamentActive: Int
        get() = getInt(R.string.tournament_active)
        set(value) {
            storeInt(R.string.tournament_active, value)
        }

    var currentLeafCount: Int
        get() = getInt(R.string.current_leaf_count)
        set(value) {
            storeInt(R.string.current_leaf_count, value)
        }

    var lastFruitCount
        get() = getInt(R.string.last_fruit_count)
        set(value) {
            storeInt(R.string.last_fruit_count, value)
        }

    var currentFruitCount
        get() = getInt(R.string.current_fruit_count)
        set(value) {
            if (value < 0) return
            storeInt(R.string.current_fruit_count, value)
        }

    var currentDayOfWeek
        get() = getInt(
            R.string.current_day_of_week,
            -1
        )    //-1 because the value is incremented by 1 in first run itself
        set(value) {
            storeInt(R.string.current_day_of_week, value)
        }

    var dailyGoalStreak
        get() = getInt(R.string.daily_goal_streak)
        set(value) {
            storeInt(R.string.daily_goal_streak, value)
        }

    var dailyGoalAchievedCount
        get() = getInt(R.string.daily_goal_achieved_count, 0)
        set(value) {
            storeInt(R.string.daily_goal_achieved_count, value)
        }

    var dailyGoalStreakString: String
        get() = sharedPref.getString(
            context.getString(R.string.daily_goal_streak_string),
            "0000000"
        )!!
        set(value) {
            sharedPref.edit {
                putString(context.getString(R.string.daily_goal_streak_string), value)
            }
        }

    var user: User
        get() {
            try {
                return Gson().fromJson(
                    sharedPref.getString(context.getString(R.string.user), ""),
                    User::class.java
                )
            } catch (e: JSONException) {
                Log.e("Exception", e.toString())
            }
            return User()
        }
        set(value) {
            sharedPref.edit {
                putString(context.getString(R.string.user), Gson().toJson(value))
            }
        }

//    var check:FirstTourneyUpdateCheck
//        get() {
//            try {
//                return Gson().fromJson(
//                    sharedPref.getString(context.getString(R.string.user), ""),
//                    FirstTourneyUpdateCheck::class.java
//                )
//            } catch (e: JSONException) {
//                Log.e("Exception", e.toString())
//            }
//            return FirstTourneyUpdateCheck()
//        }
//        set(value) {
//            sharedPref.edit {
//                putString(context.getString(R.string.user), Gson().toJson(value))
//            }
//        }

    //Added by Anirudh
    var team: Team
        get() {
            try {
                //Log.d("PrefTest","Inside Try Method")
                //Log.d("PrefTest",sharedPref.getString(context.getString(R.string.team), ""))
                return Gson().fromJson(
                    sharedPref.getString(context.getString(R.string.team), ""),
                    Team::class.java
                )
            } catch (e: JSONException) {
                Log.e("Exception", e.toString())
            }
            return Team()
        }
        set(value) {
            sharedPref.edit {
                putString(context.getString(R.string.team), Gson().toJson(value))
            }
        }

    var gameMode
        get() = getInt(R.string.game_mode)
        set(value) {
            storeInt(R.string.game_mode, value)
        }

    var challengeType = 0
        set(value) {
            storeInt(R.string.challenge_type, value)
        }
    var tournamentType = 0
        set(value) {
            storeInt(R.string.tournament_type, value)
        }

    var isChallengeActive
        get() = getBoolean(R.string.challenge_is_active, false)
        set(value) {
            storeBoolean(R.string.challenge_is_active, value)
        }

    var isTournamentActive
        get() = getBoolean(R.string.tournament_is_active, false)
        set(value) {
            storeBoolean(R.string.tournament_is_active, value)
        }

    var challengeLeafCount: Int = 0
        set(value) {
            challengeLastLeafCount = getInt(R.string.challenge_leaf_count)
            storeInt(R.string.challenge_leaf_count, value)
        }

    var tournamentLeafCount: Int = 0
        set(value) {
            tournamentLastLeafCount = getInt(R.string.tournament_leaf_count)
            storeInt(R.string.tournament_leaf_count, value)
        }

    private var challengeLastLeafCount = 0
        set(value) {
            storeInt(R.string.challenge_last_leaf_count, value)
        }
    private var tournamentLastLeafCount = 0
        set(value) {
            storeInt(R.string.tournament_last_leaf_count, value)
        }

    var challengeFruitCount: Int = 0
        set(value) {
            storeInt(R.string.challenge_fruit_count, value)
        }
    var tournamentFruitCount: Int = 0
        set(value) {
            storeInt(R.string.tournament_fruit_count, value)
        }

    var challengeGoal = 0
        set(value) {
            storeInt(R.string.challenge_goal, value)
        }
    var tournamentGoal = 0
        set(value) {
            storeInt(R.string.tournament_goal, value)
        }

    var challengeStreak = 0
        set(value) {
            storeInt(R.string.challenge_streak, value)
        }
    var tournamentStreak = 0
        set(value) {
            storeInt(R.string.tournament_streak, value)
        }

    var challengeName
        get() = sharedPref.getString(context.getString(R.string.challenge_name), "")
        set(value) {
            sharedPref.edit {
                putString(context.getString(R.string.challenge_name), value)
            }
        }
    var tournamentName
        get() = sharedPref.getString(context.getString(R.string.tournament_name), "")
        set(value) {
            sharedPref.edit {
                putString(context.getString(R.string.tournament_name), value)
            }
        }

    var challengeTotalStepsCount = 0
        set(value) {
            storeInt(R.string.challenge_total_steps_count, value)
        }

    var tournamentTotalStepsCount = 0
        set(value) {
            storeInt(R.string.tournament_total_steps_count, value)
        }


    var volume
        get() = sharedPref.getFloat(context.getString(R.string.volume), .5f)
        set(value) {
            sharedPref.edit {
                putFloat(context.getString(R.string.volume), value)
            }
        }

    var challengeLeaderboardPosition
        get() = getInt(R.string.leaderboard_position, 1)
        set(value) {
            storeInt(R.string.leaderboard_position, value)
        }
    var tournamentLeaderboardPosition
        get() = getInt(R.string.tournament_leaderboard_position, 1)
        set(value) {
            storeInt(R.string.tournament_leaderboard_position, value)
        }

    var isDailyGoalMapFixed
        get() = getBoolean(R.string.daily_goal_map_fixed, false)
        set(value) {
            storeBoolean(R.string.daily_goal_map_fixed, value)
        }

    var myMap
        get() = sharedPref.getString("myMap", "")
        set(value) {
            sharedPref.edit {
                putString("myMap", value)
            }
        }

    fun storeDailyStepCount(stepCount: Int) {
        storeInt(R.string.daily_step_count, stepCount)
    }

    fun storeChallengeStepCount(stepCount: Int){
        storeInt(R.string.challenge_total_steps_count, stepCount)
    }

    fun storeTournamentStepCount(stepCount: Int){
        storeInt(R.string.tournament_total_steps_count, stepCount)
    }

    fun getDailyStepCount() = getInt(R.string.daily_step_count)

    fun getChallengeStepCount() = getInt(R.string.challenge_total_steps_count)

    fun getTournamentStepCount() = getInt(R.string.tournament_total_steps_count)

    fun storeLastDayStepCount(stepCount: Int) {
        storeInt(R.string.last_day_step_count, stepCount)
    }

    fun getLastDayStepCount() = getInt(R.string.last_day_step_count, 0)

    fun storeDailyStepsGoal(goal: Int) {
        storeInt(R.string.daily_steps_goal, goal)
    }

    fun getDailyStepsGoal() = getInt(R.string.daily_steps_goal, 5000)

    fun storeLeafCountBeforeToday(leafCount: Int) {
        storeInt(R.string.leaf_count_before_today, leafCount)
    }

    fun clearSharedPrefs() {
        sharedPref.edit {
            clear()
        }
    }

    private fun storeInt(key: Int, value: Int) {
        with(sharedPref.edit()) {
            putInt(context.getString(key), value)
            apply()
        }
    }

    private fun storeInt1(key: Int, value: Int) {
        //commit is used instead of apply
        with(sharedPref.edit()) {
            putInt(context.getString(key), value)
            commit()
        }
    }

    private fun getInt(key: Int, defValue: Int = 0) =
        sharedPref.getInt(context.getString(key), defValue)

    private fun storeBoolean(key: Int, value: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(context.getString(key), value)
            apply()
        }
    }

    private fun getBoolean(key: Int, defValue: Boolean) =
        sharedPref.getBoolean(context.getString(key), defValue)

}
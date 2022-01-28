package dal.mitacsgri.treecare.backgroundtasks.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import calculateLeafCountFromStepCountForTeam
import calculateDailyGoalsAchievedFromStepCountForTeam
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.extensions.toDateTime
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.TeamTournament
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.model.UserTournament
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import org.jetbrains.anko.doAsync
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class UpdateTeamDataWorker (appContext: Context, workerParams: WorkerParameters)
    : ListenableWorker(appContext, workerParams), KoinComponent {

    private val stepCountRepository: StepCountRepository by inject()
    private val sharedPrefsRepository: SharedPreferencesRepository by inject()
    private val firestoreRepository: FirestoreRepository by inject()

    val today = DateTime()
    val localMN = today.withTimeAtStartOfDay().millis
    val localDate = today.toLocalDateTime().dayOfMonth
    val localMonth = today.toLocalDateTime().monthOfYear

    val halifaxTimeZone = DateTimeZone.forID("America/Halifax")

    val halifax = today.withZone(halifaxTimeZone)
    val halifaxMN = today.withZone(halifaxTimeZone).withTimeAtStartOfDay().millis
    val halifaxDate = halifax.toLocalDateTime().dayOfMonth
    val halifaxMonth = halifax.toLocalDateTime().monthOfYear

    var mNight: Long = 0


    //private val dateOutputFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withZone(halifaxTimeZone)

    var cr = 0 //for counting number of calc call for the active tourneys

    override fun startWork(): ListenableFuture<Result> {

        when {
            (localMN > halifaxMN)  -> {
                if(localDate == halifaxDate)
                    mNight = today.withZone(halifaxTimeZone).withTimeAtStartOfDay().millis

                if(localDate > halifaxDate || (localDate < halifaxDate && localMonth > halifaxMonth))
                    mNight = today.withZone(halifaxTimeZone).withTimeAtStartOfDay().millis + 86400000

                if(localDate < halifaxDate || (localDate > halifaxDate && localMonth < halifaxMonth))
                    mNight = today.withZone(halifaxTimeZone).withTimeAtStartOfDay().millis - 86400000
            }
            (localMN < halifaxMN) -> {
                if(localDate == halifaxDate)
                    mNight = today.withZone(halifaxTimeZone).withTimeAtStartOfDay().millis

                if(localDate < halifaxDate || (localDate > halifaxDate && localMonth < halifaxMonth))
                    mNight = today.withZone(halifaxTimeZone).withTimeAtStartOfDay().millis - 86400000

                if(localDate > halifaxDate || (localDate < halifaxDate && localMonth > halifaxMonth))
                    mNight = today.withZone(halifaxTimeZone).withTimeAtStartOfDay().millis + 86400000
            }
            else -> {
                mNight = today.withZone(halifaxTimeZone).withTimeAtStartOfDay().millis
            }
        }

        Log.d("WorkerT", "Starting Team Worker")
        val future = SettableFuture.create<Result>()

        val team = sharedPrefsRepository.team
        val user = sharedPrefsRepository.user
        var county = 0

        team.currentTournaments.forEach { (_, tourney) ->
            if(tourney.isActive && tourney.endDate.toDateTime().millis > DateTime().millis && tourney.startDate.toDateTime().millis <= DateTime().millis)
                county++
        }

        if (!team.currentTournaments.isNullOrEmpty()) {
            team.currentTournaments.forEach { (_, tourney) ->
                val startTimeMillis = tourney.startDate.toDateTime().millis
                val endTimeMillis = tourney.endDate.toDateTime().millis
                if (tourney.isActive && endTimeMillis > DateTime().millis && startTimeMillis <= DateTime().millis) {
                    stepCountRepository.getTodayStepCountData {
                        Log.d("WorkerT", "TourneyName: " + tourney.name)
                        calc(team, it, future, county, tourney, mNight)
                        //updateStepsForUserAndTeam(team, user,it, future, tourney, mNight)
                    }
                }
            }
        }
        return future
    }

    private fun calc(team: Team, currentStepCount: Int, future:SettableFuture<Result>, county: Int, tourney: TeamTournament, midNight: Long) {

        cr++
        val userTourneys = sharedPrefsRepository.user
        //Sorting the step map according to the dates
        Log.d("WorkerT", "Inside Calc")

        team.currentTournaments.forEach { (_, teamTourney) ->
            teamTourney.dailyStepsMap = teamTourney.dailyStepsMap.toSortedMap()
        }

        userTourneys.currentTournaments.forEach { (_, userTourney) ->
            userTourney.dailyStepsMap = userTourney.dailyStepsMap.toSortedMap()
        }

        firestoreRepository.getUserData(sharedPrefsRepository.user.uid)
            .addOnSuccessListener {
                val user = it.toObject<User>()
                val userTournaments = user?.currentTournaments

                if (userTournaments!![tourney.name]?.dailyStepsMap?.isEmpty()!! && userTournaments[tourney.name]?.isActive!!) {
                    Log.d("WorkerT", "Stepmap is empty for ${tourney.name}")


                    userTourneys.currentTournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] =
                        currentStepCount
                    userTourneys.currentTournaments[tourney.name]?.lastUpdateTime =
                        Timestamp.now()
                    userTourneys.currentTournaments[tourney.name]?.totalSteps =
                        currentStepCount

                    updateAndStoreUserDataInSharedPrefs(userTourneys.currentTournaments[tourney.name]!!)

                    Log.d("WorkerT", sharedPrefsRepository.user.currentTournaments[tourney.name].toString())

                    firestoreRepository.updateUserData(
                        user.uid,
                        mapOf("currentTournaments" to sharedPrefsRepository.user.currentTournaments)
                    )
                        .addOnSuccessListener {
                            Log.d("WorkerT", "user stepMap before Upload for tourney: ${tourney.name}" + userTourneys.currentTournaments)
                            Log.d("WorkerT", "stepmap for ${tourney.name} was uploaded successfully")

                            firestoreRepository.getTeam(team.name)
                                .addOnSuccessListener {
                                    val teamDB = it.toObject<Team>()
                                    val teamTourney = teamDB?.currentTournaments
                                    if (teamTourney!![tourney.name]?.dailyStepsMap?.isEmpty()!!) {
                                        Log.d(
                                            "WorkerT",
                                            "Team tourney:${tourney.name} Daily Step Map is empty"
                                        )
                                        team.currentTournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] =
                                            currentStepCount
                                        doAsync {
                                            val i = updateAndStoreTeamDataInSharedPrefs(
                                                team.currentTournaments[tourney.name]!!,
                                                team,
                                                userTourneys.currentTournaments[tourney.name]!!
                                            )
                                            updateUserTeamDataInFirestore(future, cr,county,i)
                                        }
                                    } else {
                                        Log.d("WorkerT", "Daily Step Map is not empty")
                                        if (teamTourney[tourney.name]?.dailyStepsMap?.keys?.sorted()?.last()
                                                .toString() != midNight.toString()
                                        ) {
                                            Log.d(
                                                "WorkerT",
                                                "Team tourney: ${tourney.name} is not up to date"
                                            )
                                            team.currentTournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] =
                                                currentStepCount
                                            doAsync {
                                                val i = updateAndStoreTeamDataInSharedPrefs(
                                                    team.currentTournaments[tourney.name]!!,
                                                    team,
                                                    userTourneys.currentTournaments[tourney.name]!!
                                                )
                                                updateUserTeamDataInFirestore(future, cr,county,i)
                                            }
                                        } else {
                                            Log.d(
                                                "WorkerT",
                                                "Team tourney: ${tourney.name} is up to date"
                                            )
                                            val oldStep =
                                                teamTourney[tourney.name]?.dailyStepsMap?.values?.last()
                                            val updatedSteps = oldStep!! + currentStepCount
                                            team.currentTournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] = updatedSteps


                                            team.currentTournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] =
                                                updatedSteps
                                            doAsync {
                                                val i = updateAndStoreTeamDataInSharedPrefs(
                                                    team.currentTournaments[tourney.name]!!,
                                                    team,
                                                    userTourneys.currentTournaments[tourney.name]!!
                                                )
                                                updateUserTeamDataInFirestore(future, cr,county,i)
                                            }
                                        }
                                    }
                                }
                        }
                        .addOnFailureListener {
                            Log.d("WorkerT", "stepmap for ${tourney.name} failed to upload")
                        }

                } else if (tourney.isActive) {
                    Log.d("WorkerT", "User Tourney: ${tourney.name} stepmap is not empty")
                    firestoreRepository.getTeam(team.name)
                        .addOnSuccessListener {
                            val teamDB = it.toObject<Team>()
                            val tournaments = teamDB?.currentTournaments
//                        team.currentTournaments.forEach { (_, tourney) ->
                            if (tourney.isActive) {
                                Log.d("WorkerT", "userStepMap for Tourney ${tourney.name}"+ userTournaments[tourney.name]?.dailyStepsMap?.keys+ "Last "+ userTournaments[tourney.name]?.dailyStepsMap?.keys?.last())
                                if (userTournaments[tourney.name]?.dailyStepsMap?.keys?.sorted()?.last()
                                        .toString() == midNight.toString()
                                ) {

                                    //TODO:replace users daily steps
                                    Log.d("WorkerT", "UserTourney is up to date")

                                    userTourneys.currentTournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] =
                                        currentStepCount

                                    userTourneys.currentTournaments[tourney.name]?.lastUpdateTime =
                                        Timestamp.now()
                                    userTourneys.currentTournaments[tourney.name]?.totalSteps =
                                        currentStepCount
                                    updateAndStoreUserDataInSharedPrefs(userTourneys.currentTournaments[tourney.name]!!)
                                    firestoreRepository.updateTeamData(team.name, mapOf("currentTournaments" to sharedPrefsRepository.team.currentTournaments))


                                    firestoreRepository.updateUserData(
                                        user.uid,
                                        mapOf("currentTournaments" to sharedPrefsRepository.user.currentTournaments)
                                    )
                                        .addOnSuccessListener {
                                            Log.d("WorkerT", "user stepMap before Upload for tourney: ${tourney.name}" + userTourneys.currentTournaments)
                                            Log.d(
                                                "WorkerT",
                                                "stepmap for ${tourney.name} was uploaded successfully"
                                            )
                                            if (tournaments!![tourney.name]?.dailyStepsMap?.isEmpty()!!) {
                                                Log.d(
                                                    "WorkerT",
                                                    "Team tourney step map is empty"
                                                )
                                                tournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] =
                                                    currentStepCount
                                                doAsync {
                                                    val i = updateAndStoreTeamDataInSharedPrefs(
                                                        tournaments[tourney.name]!!,
                                                        team,
                                                        userTourneys.currentTournaments[tourney.name]!!
                                                    )
                                                    updateUserTeamDataInFirestore(future, cr,county,i)
                                                }
                                            } else {
                                                Log.d(
                                                    "WorkerT",
                                                    "Team tourney step map is not empty"
                                                )
                                                if (tournaments[tourney.name]?.dailyStepsMap?.keys?.sorted()?.last()
                                                        .toString() != midNight.toString()
                                                ) {
                                                    Log.d(
                                                        "WorkerT",
                                                        "Team tourney is not up to date"
                                                    )
                                                    tourney.dailyStepsMap =
                                                        teamDB.currentTournaments[tourney.name]?.dailyStepsMap!!
                                                    tourney.dailyStepsMap[midNight.toString()] =
                                                        currentStepCount
                                                    doAsync {
                                                        val i = updateAndStoreTeamDataInSharedPrefs(
                                                            tourney,
                                                            team,
                                                            userTourneys.currentTournaments[tourney.name]!!
                                                        )
                                                        updateUserTeamDataInFirestore(future, cr,county,i)
                                                    }
                                                } else {
                                                    Log.d(
                                                        "WorkerT",
                                                        "Team tourney is up to date"
                                                    )
                                                    val oldStep =
                                                        tournaments[tourney.name]?.dailyStepsMap!![midNight.toString()]

                                                    Log.d("WorkerT", "OldStep for tourney: ${tourney.name} " + oldStep)
                                                    Log.d("WorkerT", "Last Day step count "+ sharedPrefsRepository.getLastDayStepCount())
                                                    val diff =
                                                        currentStepCount - sharedPrefsRepository.getLastDayStepCount()
                                                    Log.d("WorkerT", "Diff3 for tourney: ${tourney.name} " + diff)
                                                    val updatedSteps = oldStep!! + diff

                                                    if (diff > 0) {
                                                        tourney.dailyStepsMap[midNight.toString()] =
                                                            updatedSteps
                                                        doAsync {
                                                            val i = updateAndStoreTeamDataInSharedPrefs(
                                                                tourney,
                                                                team,
                                                                userTourneys.currentTournaments[tourney.name]!!
                                                            )
                                                            updateUserTeamDataInFirestore(future, cr,county,i)
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                } else {
                                    Log.d("WorkerT", "UserTourney is not up to date")

                                    userTourneys.currentTournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] =
                                        currentStepCount

                                    userTourneys.currentTournaments[tourney.name]?.lastUpdateTime =
                                        Timestamp.now()
                                    userTourneys.currentTournaments[tourney.name]?.totalSteps =
                                        currentStepCount

                                    updateAndStoreUserDataInSharedPrefs(userTourneys.currentTournaments[tourney.name]!!)


                                    firestoreRepository.updateUserData(
                                        user.uid,
                                        mapOf("currentTournaments" to sharedPrefsRepository.user.currentTournaments)
                                    )
                                        .addOnSuccessListener {
                                            Log.d("WorkerT", "user stepMap before Upload for tourney: ${tourney.name}" + userTourneys.currentTournaments)
                                            Log.d(
                                                "WorkerT",
                                                "stepmap for ${tourney.name} was uploaded successfully"
                                            )

                                            //TODO: Update user Tourney
                                            if (tournaments!![tourney.name]?.dailyStepsMap?.isEmpty()!!) {
                                                Log.d(
                                                    "WorkerT",
                                                    "Team tourney step map is empty"
                                                )
                                                tournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] =
                                                    currentStepCount

                                                doAsync {
                                                    val i = updateAndStoreTeamDataInSharedPrefs(
                                                        tournaments[tourney.name]!!,
                                                        team,
                                                        userTourneys.currentTournaments[tourney.name]!!
                                                    )
                                                    updateUserTeamDataInFirestore(future, cr,county,i)
                                                }
                                                //updateUserTeamDataInFirestore(future)
//                                                        sharedPrefsRepository.storeLastDayStepCount(
//                                                            currentStepCount
//                                                        )
                                            } else {
                                                Log.d(
                                                    "WorkerT",
                                                    "Team tourney step map is not empty"
                                                )
                                                if (tournaments[tourney.name]?.dailyStepsMap?.keys?.sorted()?.last()
                                                        .toString() != midNight.toString()
                                                ) {
                                                    //Data is present in team tourney
                                                    Log.d(
                                                        "WorkerT",
                                                        "Team tourney is not up to date"
                                                    )

                                                    tourney.dailyStepsMap =
                                                        teamDB.currentTournaments[tourney.name]?.dailyStepsMap!!
                                                    tourney.dailyStepsMap[midNight.toString()] =
                                                        currentStepCount

                                                    doAsync {
                                                        val i = updateAndStoreTeamDataInSharedPrefs(
                                                            tourney,
                                                            team,
                                                            userTourneys.currentTournaments[tourney.name]!!
                                                        )
                                                        updateUserTeamDataInFirestore(future, cr,county,i)
                                                    }
                                                    //updateUserTeamDataInFirestore(future)
//                                                            sharedPrefsRepository.storeLastDayStepCount(
//                                                                currentStepCount
//                                                            )
                                                }
                                                else {
                                                    //Data is not present in TeamTourney

                                                    Log.d(
                                                        "WorkerT",
                                                        "Team tourney is up to date"
                                                    )
                                                    val oldStep =
                                                        tournaments[tourney.name]?.dailyStepsMap!![midNight.toString()]
                                                    val updatedSteps = oldStep!! + currentStepCount
                                                    team.currentTournaments[tourney.name]?.dailyStepsMap!![midNight.toString()] = updatedSteps

                                                    doAsync {
                                                        val i = updateAndStoreTeamDataInSharedPrefs(
                                                            team.currentTournaments[tourney.name]!!,
                                                            team,
                                                            userTourneys.currentTournaments[tourney.name]!!
                                                        )
                                                        updateUserTeamDataInFirestore(future, cr,county,i)
                                                    }
                                                    //updateUserTeamDataInFirestore(future)
//                                                            sharedPrefsRepository.storeLastDayStepCount(
//                                                                currentStepCount
//                                                            )
                                                }
                                            }
                                        }
                                }
                            }
                        }
                }
            }
        // }
    }

    private fun updateAndStoreTeamDataInSharedPrefs(teamTourney: TeamTournament, team: Team, userTourney: UserTournament): Int {
        teamTourney.leafCount = getTotalLeafCountForTeam(teamTourney)
        teamTourney.fruitCount = getTotalFruitCountForTeam(teamTourney)
        teamTourney.tournamentGoalStreak = getTeamGoalStreakForUser(teamTourney, team)
        teamTourney.lastUpdateTime = Timestamp.now()
        teamTourney.dailyGoalsAchieved = calculateDailyGoalsAchieved(teamTourney)
        Log.d("WorkerT", "updateAndStoreTeamDataInSharedPrefs")
        var totalSteps = 0

        /*teamTourney.dailyStepsMap.forEach { (time, steps) ->
            totalSteps += steps
        }*/

        for (steps in userTourney.dailyStepsMap.values){
            totalSteps += steps
        }

        teamTourney.totalSteps = totalSteps
        synchronized(sharedPrefsRepository.team) {
            val team = sharedPrefsRepository.team
            team.currentTournaments[teamTourney.name] = teamTourney
            sharedPrefsRepository.team = team
        }
        //updateUserTeamDataInFirestore()
        return 0
    }


    private fun updateUserTeamDataInFirestore(future: SettableFuture<Result>, i: Int, cr: Int, county: Int) {
        //passing i as dummy variable since we run both sharedPrefs and update function in background thread, i is needed for synchronizing the function calls
        //if (cr == county) {
            Log.d("WorkerT", "updateUserTeamDataInFirestore")
            Log.d("WorkerT", "Pref " + sharedPrefsRepository.team)
            Log.d("WorkerT", "PrefValue " + sharedPrefsRepository.team.currentTournaments)
            Log.d("WorkerT", "TeamName " + sharedPrefsRepository.team.name)
            firestoreRepository.updateUserData(
                sharedPrefsRepository.user.uid,
                mapOf("currentTournaments" to sharedPrefsRepository.user.currentTournaments)
            )
                .addOnSuccessListener {
                    firestoreRepository.updateTeamData(
                        sharedPrefsRepository.team.name,
                        mapOf("currentTournaments" to sharedPrefsRepository.team.currentTournaments)
                    )
                        .addOnSuccessListener {
                            Log.d("WorkerT", "Team User data upload success")
                            Log.d("WorkerT", "cr: " + cr + "county: " + county)

                            stepCountRepository.getTodayStepCountData {
                                sharedPrefsRepository.storeLastDayStepCount(it)
                            }
                            future.set(Result.success())
                        }
                        .addOnFailureListener {
                            Log.e("WorkerT", "Team User data upload failed")
                            future.set(Result.failure())
                        }
                }
        //}
    }

    private fun getTotalLeafCountForTeam(teamTournament: TeamTournament): Int {
        Log.d("WorkerT","getTotalLeafCountForTeam")
        val stepsMap = teamTournament.dailyStepsMap
        val goal = teamTournament.goal
        var leafCount = 0

        val keys = stepsMap.keys.sortedBy {
            it.toLong()
        }

        for (i in 0 until keys.size-1) {
            //for (i in 0 until keys.size) {
            Log.d("WorkerT","StepMap "+ stepsMap[keys[i]])
            leafCount += calculateLeafCountFromStepCountForTeam(stepsMap[keys[i]]!!, goal)
        }
        leafCount += stepsMap[keys[keys.size-1]]!! / 3000
        return leafCount
    }

    private fun getTotalFruitCountForTeam(tournament: TeamTournament): Int {
        Log.d("WorkerT","getTotalFruitCountForTeam")
        val joinDate = DateTime(tournament.joinDate)
        val currentDate = DateTime()
        val days = Days.daysBetween(joinDate, currentDate).days
        val weeks = Math.ceil(days/7.0).toInt()

        var fruitCount = 0

        var weekStartDate = joinDate
        var newWeekDate = weekStartDate.plusWeeks(1)
        var mapPartition: Map<String, Int>
        for (i in 0 until weeks) {
            mapPartition = tournament.dailyStepsMap.filter {
                val keyAsLong = it.key.toLong()
                keyAsLong >= weekStartDate.millis && keyAsLong < newWeekDate.millis
            }
            fruitCount += calculateTeamFruitCountForWeek(tournament, mapPartition)
            weekStartDate = newWeekDate
            newWeekDate = weekStartDate.plusWeeks(1)
        }
        if(fruitCount < 0)
            fruitCount = 0
        return fruitCount
    }

    private fun calculateTeamFruitCountForWeek(tournament: TeamTournament, stepCountMap: Map<String, Int>): Int {
        Log.d("WorkerT","calculateTeamFruitCountForWeek")
        var currentDay = 0
        val goalAchievedStreak = arrayOf(false, false, false, false, false, false, false)
        val fullStreak = arrayOf(true, true, true, true, true, true, true)

        if (stepCountMap.size < 7) return 0

        stepCountMap.forEach { (_, stepCount) ->
            Log.d("WorkerT", "currentDay "+ currentDay)
            goalAchievedStreak[currentDay] =
                stepCount >= tournament.goal
            currentDay++
        }

        return if (goalAchievedStreak.contentEquals(fullStreak)) 1 else -1
    }

    private fun getTeamGoalStreakForUser(tournament: TeamTournament, team: Team): Int {
        Log.d("WorkerT","getTeamGoalStreakForUser")
        val teamTournamentData = team.currentTournaments[tournament.name]!!
        var streakCount = 0

        teamTournamentData.dailyStepsMap.forEach { (date, stepCount) ->
            //This check prevents resetting streak count if goal is yet to be met today
            if (date.toLong() < DateTime().withTimeAtStartOfDay().millis) {
                if (stepCount >= tournament.goal) streakCount++
                else streakCount = 0
            }
        }
        return streakCount
    }

    private fun updateAndStoreUserDataInSharedPrefs(userTourney: UserTournament){

        synchronized(sharedPrefsRepository.user){
            val user = sharedPrefsRepository.user
            user.currentTournaments[userTourney.name] = userTourney
            sharedPrefsRepository.user = user
        }
        // updateUserTourneyDataInFirestore()
    }

    fun calculateDailyGoalsAchieved(teamTournament: TeamTournament): Int{

        Log.d("WorkerT","getTotalLeafCountForTeam")
        val stepsMap = teamTournament.dailyStepsMap
        val goal = teamTournament.goal
        var dailyGoalsAchieved = 0

        val keys = stepsMap.keys.sortedBy {
            it.toLong()
        }

        for (i in 0 until keys.size-1) {
            //for (i in 0 until keys.size) {
            Log.d("WorkerT","DailyGoalStepMap "+ stepsMap[keys[i]])
            dailyGoalsAchieved += calculateDailyGoalsAchievedFromStepCountForTeam(stepsMap[keys[i]]!!, goal)
        }

        dailyGoalsAchieved += if(stepsMap[keys[keys.size-1]]!! > goal) 1 else 0

        return dailyGoalsAchieved
    }
}
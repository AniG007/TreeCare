package dal.mitacsgri.treecare.backgroundtasks.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import calculateLeafCountFromStepCountForTeam
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.backgroundtasks.jobs.TrophiesUpdateJob
import dal.mitacsgri.treecare.model.*
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import org.joda.time.DateTime
import org.joda.time.Days
import org.koin.core.KoinComponent
import org.koin.core.inject

class TrophiesUpdateWorker(appContext: Context, workerParams: WorkerParameters) :
    ListenableWorker(appContext, workerParams), KoinComponent {

    private val sharedPrefsRepository: SharedPreferencesRepository by inject()
    private val firestoreRepository: FirestoreRepository by inject()

    private var challengesCounter = ObservableInt()
    private var tournamentsCounter = ObservableInt1()

    val TAG = "TrophiesUpdateWorker"

    override fun startWork(): ListenableFuture<Result> {
        val future = SettableFuture.create<Result>()
        val currentChallenges = sharedPrefsRepository.user.currentChallenges
        val challengersList = ArrayList<Challenger>()
        val userTrophies = UserChallengeTrophies()

        Log.d(TrophiesUpdateJob.TAG, "Running Job")
        val currentTournaments = sharedPrefsRepository.team.currentTournaments
        val tournamentTrophies = UserTournamentTrophies()
        val teamList = ArrayList<TeamTournament>()


        /** Trophies for Challenges*/

        currentChallenges.forEach { (name, _) ->

            firestoreRepository.getChallenge(name)
                .addOnSuccessListener {

                    val challenge = it.toObject<Challenge>()
                    val challengers = challenge?.players
                    val challengersCount = challengers?.size

                    if (challenge != null && !challenge.active) {
                        for (i in 0 until challengersCount!!) {
                            firestoreRepository.getUserData(challengers[i])
                                .addOnSuccessListener {

                                    val user = it.toObject<User>()
                                    val challenger =
                                        user?.let { makeChallengerFromUser(user, challenge) }

                                    challengersList.add(challenger!!)

                                    if (challengersList.size == challengersCount) {

                                        challengersList.sortWith(
                                            compareBy(
                                                { it.totalLeaves },
                                                { it.totalSteps })
                                        )
                                        challengersList.reverse()

                                        when (getCurrentChallengerPosition(challengersList)) {
                                            0 -> userTrophies.gold.add(name)
                                            1 -> userTrophies.silver.add(name)
                                            2 -> userTrophies.bronze.add(name)
                                        }

                                    }
                                }
                        }
                        challengesCounter.setValue(challengesCounter.getValue() + 1) {
                            Log.d(TAG, "C counter " + it + "C size " + currentTournaments.size)
                            if (it == currentChallenges.size) {
                                firestoreRepository.storeTrophiesData(
                                    sharedPrefsRepository.user.uid,
                                    userTrophies
                                )
                                    .addOnSuccessListener {
                                        Log.d(TrophiesUpdateJob.TAG, "Success")
                                    }
                                    .addOnFailureListener {
                                        Log.d(TrophiesUpdateJob.TAG, it.toString())
                                    }
                            }
                        }
                    }
                }
        }

        /** Trophies for Teams*/
        currentTournaments.forEach { (name, _) ->

            firestoreRepository.getTournament(name)
                .addOnSuccessListener {
                    val tournament = it.toObject<Tournament>()

                    sharedPrefsRepository.tournamentName = name

                    val teams = tournament?.teams
                    val teamsCount = teams?.size

                    Log.d("leaderBoard", teams.toString())
                    if (tournament != null && !tournament.active) {
                        for (i in 0 until teamsCount!!) {
                            firestoreRepository.getTeam(teams[i])
                                .addOnSuccessListener {
                                    val teamFromDB = it.toObject<Team>()
                                    val teamTourney = teamFromDB?.currentTournaments!![name]!!
                                    val members = teamFromDB.members
                                    var totalStepsForATournament = 0
                                    var memberCount = members.count()
                                    val mappy: MutableMap<String, Int> = mutableMapOf()

                                    for (member in members) {
                                        firestoreRepository.getUserData(member)
                                            .addOnSuccessListener {
                                                val user = it.toObject<User>()
                                                val userStepMap =
                                                    user?.currentTournaments!![name]?.dailyStepsMap

                                                if (userStepMap?.values != null) {
                                                    for (step in userStepMap.values) {
                                                        totalStepsForATournament += step
                                                        Log.d("Steps", step.toString())
                                                    }

                                                    userStepMap.forEach {
                                                        if (mappy.keys.contains(it.key)) {
                                                            val last = mappy[it.key]!!
                                                            mappy[it.key] = last + it.value
                                                            Log.d("Mapper", "For Team ${teamFromDB.name} For User ${user.name}" + it.value)

                                                        } else {
                                                            mappy[it.key] = it.value
                                                            Log.d("MapperElse", "For Team ${teamFromDB.name} For User ${user.name}" + userStepMap.values.last().toString()
                                                            )
                                                        }
                                                    }
                                                }
                                                Log.d("Steps", "membercount $memberCount members.count ${members.count()}")

                                                memberCount--
                                                if (memberCount == 0) {
                                                    Log.d("Steps", "Adding to List")
                                                    teamTourney.totalSteps =
                                                        totalStepsForATournament

                                                    teamTourney.dailyStepsMap = mappy

                                                    teamTourney.leafCount =
                                                        getTotalLeafCountForTeam(teamTourney)

                                                    teamTourney.fruitCount =
                                                        getTotalFruitCountForTeam(teamTourney)

                                                    teamList.add(teamTourney)
                                                    mappy.clear()
                                                }
                                                if (teamList.size == teamsCount) {

                                                    //Sorting according to daily goals first, then if any of the goals are equal, then the team's totalSteps are taken into account
                                                    teamList.sortWith(
                                                        compareBy(
                                                            { it.leafCount },
                                                            { it.totalSteps })
                                                    )
                                                    teamList.reverse()
                                                    when (getCurrentTeamPosition(teamList)) {
                                                        0 -> tournamentTrophies.gold.add(name)
                                                        1 -> tournamentTrophies.silver.add(name)
                                                        2 -> tournamentTrophies.bronze.add(name)
                                                    }
                                                }
                                            }
                                    }

                                    tournamentsCounter.setValue(tournamentsCounter.getValue() + 1) {

                                        Log.d(TrophiesUpdateJob.TAG, "counter " + it + "Size " + currentTournaments.size)

                                        if (it == currentTournaments.size) {
                                            Log.d(TrophiesUpdateJob.TAG, "Size equals tournament counter")

                                            firestoreRepository.storeTeamTrophiesData(sharedPrefsRepository.team.name, tournamentTrophies)
                                                .addOnSuccessListener {
                                                    Log.d(TrophiesUpdateJob.TAG, "TSuccess")
                                                    future.set(Result.success())
                                                }
                                                .addOnFailureListener {
                                                    Log.d(TrophiesUpdateJob.TAG, "TFailure")
                                                    future.set(Result.failure())
                                                }
                                        }
                                    }
                                }
                        }
                    }
                }
        }
        return future
    }

    private class ObservableInt(private var value: Int = 0) {

        fun getValue() = value
        fun setValue(value: Int, action: (Int) -> (Unit)) {
            this.value = value
            action(value)
        }
    }

    private class ObservableInt1(private var value: Int = 0) {

        fun getValue() = value
        fun setValue(value: Int, action: (Int) -> (Unit)) {
            this.value = value
            action(value)
        }
    }

    private fun makeChallengerFromUser(user: User, challenge: Challenge): Challenger {
        val userChallengeData = user.currentChallenges[challenge.name]!!

        return Challenger(
            name = user.name,
            uid = user.uid,
            photoUrl = user.photoUrl,
            challengeGoalStreak = userChallengeData.challengeGoalStreak,
            totalSteps = userChallengeData.totalSteps,
            totalLeaves = userChallengeData.leafCount
        )
    }

    private fun getTotalLeafCountForTeam(teamTournament: TeamTournament): Int {
        Log.d(TAG, "getTotalLeafCountForTeam")
        val stepsMap = teamTournament.dailyStepsMap
        val goal = teamTournament.goal
        var leafCount = 0

        val keys = stepsMap.keys.sortedBy {
            it.toLong()
        }

        for (i in 0 until keys.size - 1) {
            Log.d("WorkerT", "StepMap " + stepsMap[keys[i]])
            leafCount += calculateLeafCountFromStepCountForTeam(stepsMap[keys[i]]!!, goal)
        }
        leafCount += stepsMap[keys[keys.size - 1]]!! / 3000
        return leafCount
    }

    private fun getTotalFruitCountForTeam(tournament: TeamTournament): Int {
        Log.d(TAG, "getTotalFruitCountForTeam")
        val joinDate = DateTime(tournament.joinDate)
        val currentDate = DateTime()
        val days = Days.daysBetween(joinDate, currentDate).days
        val weeks = Math.ceil(days / 7.0).toInt()

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

        return fruitCount
    }

    private fun calculateTeamFruitCountForWeek(
        tournament: TeamTournament,
        stepCountMap: Map<String, Int>
    ): Int {
        Log.d("WorkerT", "calculateTeamFruitCountForWeek")
        var currentDay = 0
        val goalAchievedStreak = arrayOf(false, false, false, false, false, false, false)
        val fullStreak = arrayOf(true, true, true, true, true, true, true)

        if (stepCountMap.size < 7) return 0

        stepCountMap.forEach { (_, stepCount) ->
            Log.d(TAG, "currentDay " + currentDay)
            goalAchievedStreak[currentDay] =
                stepCount >= tournament.goal
            currentDay++
        }

        return if (goalAchievedStreak.contentEquals(fullStreak)) 1 else -1
    }

    fun getCurrentChallengerPosition(challengers: java.util.ArrayList<Challenger>): Int {

        val currentUserUid = sharedPrefsRepository.user.uid

        return challengers.indexOfFirst {
            it.uid == currentUserUid
        }
    }

    fun getCurrentTeamPosition(teams: java.util.ArrayList<TeamTournament>): Int {

        val currentTeam = sharedPrefsRepository.team.name

        return teams.indexOfFirst {
            it.teamName == currentTeam
        }
    }
}
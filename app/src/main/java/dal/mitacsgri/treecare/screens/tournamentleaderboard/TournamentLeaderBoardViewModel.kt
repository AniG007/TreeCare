package dal.mitacsgri.treecare.screens.tournamentleaderboard

import android.text.SpannedString
import android.util.Log
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import calculateLeafCountFromStepCountForTeam
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.notifyObserver
import dal.mitacsgri.treecare.extensions.xnor
import dal.mitacsgri.treecare.model.*
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import org.joda.time.DateTime
import org.joda.time.Days
import kotlin.collections.ArrayList
import kotlin.math.ceil

class TournamentLeaderBoardViewModel(
    private val firestoreRepository: FirestoreRepository,
    private val sharedPrefsRepository: SharedPreferencesRepository
):ViewModel() {

    private lateinit var tournament: Tournament
    //private var teamTournament: TeamTournament? = null //todo: remove this if checking with Team in DB
    private var userTournament: UserTournament? = null

    private val mTeamList = MutableLiveData<ArrayList<TeamTournament>>()
    var totalSteps = 0

    var isDialogDisplayed: Boolean

    get() =  (tournament.active.xnor(userTournament?.isActive ?: tournament.active))
    set(value){
     userTournament?.let {
         it.isActive = value
         //val team = sharedPrefsRepository.team //todo: remove this if checking with Team in DB
         val userPref = sharedPrefsRepository.user
         userPref.currentTournaments[it.name] = it
         //sharedPrefsRepository.team = team // todo: remove this if checking with Team in DB
         sharedPrefsRepository.user = userPref

         val user = sharedPrefsRepository.user
         user.currentTournaments[it.name] = getUserTournament(it, value)
         sharedPrefsRepository.user = user

         firestoreRepository.updateUserData(sharedPrefsRepository.user.uid,
         mapOf("currentTournaments" to user.currentTournaments))

         /*firestoreRepository.updateTeamData(sharedPrefsRepository.team.name,
         mapOf("currentTournaments" to team.currentTournaments))*/ // todo: remove this if checking with Team in DB
     }
    }

    fun isCurrentTeam(teamTournament: TeamTournament) = teamTournament.teamName == sharedPrefsRepository.team.name


    fun getTeamList(tournamentName: String): LiveData<ArrayList<TeamTournament>> {

        val teamList = MutableLiveData<ArrayList<TeamTournament>>().default(arrayListOf())

        firestoreRepository.getTournament(tournamentName)
            .addOnSuccessListener {
                tournament = it.toObject<Tournament>()!!

                sharedPrefsRepository.tournamentName = tournamentName
                //teamTournament = sharedPrefsRepository.team.currentTournaments[tournamentName]
                userTournament = sharedPrefsRepository.user.currentTournaments[tournamentName] //todo: remove this if checking with Team in DB
                val teams = tournament.teams
                val teamsCount = teams.size

                Log.d("leaderBoard", teams.toString())

                for (i in 0 until teamsCount) {
                    firestoreRepository.getTeam(teams[i])
                        .addOnSuccessListener {
                            val teamFromDB = it.toObject<Team>()
                            val teamTourney = teamFromDB?.currentTournaments!![tournamentName]!!
                            val members = teamFromDB?.members
                            var totalStepsForATournament = 0
                            var memberCount = members?.count()
                            val mappy: MutableMap<String, Int> = mutableMapOf()

                                for(member in members!!) {
                                    firestoreRepository.getUserData(member)
                                        .addOnSuccessListener {
                                            val user = it.toObject<User>()
                                            val userStepMap =
                                                user?.currentTournaments!![tournamentName]?.dailyStepsMap

                                            if (userStepMap?.values != null) {
                                                for (step in userStepMap.values) {
                                                    totalStepsForATournament += step
                                                    Log.d("Steps", step.toString())
                                                }

                                                userStepMap.forEach {
                                                    if (mappy.keys.contains(it.key)) {
                                                        val last = mappy[it.key]!!
                                                        mappy[it.key] = last + it.value
                                                        Log.d(
                                                            "Mapper",
                                                            "For Team ${teamFromDB.name} For User ${user.name}" + it.value
                                                        )
                                                    } else {
                                                        mappy[it.key] = it.value
                                                        Log.d(
                                                            "MapperElse",
                                                            "For Team ${teamFromDB.name} For User ${user.name}" + userStepMap.values.last()
                                                                .toString()
                                                        )
                                                    }
                                                }
                                            }
                                            Log.d("Steps", "membercount $memberCount members.count ${members.count()}")

                                            memberCount--
                                            //memberCount = memberCount!!-1
                                            if (memberCount == 0) {
                                                /*Log.d("Steps", "for Team ${teams[i]} $totalStepsForATournament")
                                                Log.d("Mappy", "for Team ${teams[i]} ${mappy.toSortedMap()} ")*/
                                                teamTourney.totalSteps = totalStepsForATournament
                                                teamTourney.dailyStepsMap = mappy
                                                teamTourney.leafCount = getTotalLeafCountForTeam(teamTourney)
                                                teamTourney.fruitCount = getTotalFruitCountForTeam(teamTourney)
                                                teamList.value?.add(teamTourney)

                                                mappy.clear()
                                            }
                                            if (teamList.value?.size == teamsCount) {

                                                //Sorting according to daily goals first, then if any of the goals are equal, then the team's totalSteps are taken into account
                                                teamList.value!!.sortWith(compareBy({it.leafCount}, {it.totalSteps}))
                                                teamList.value!!.reverse()
                                                teamList.notifyObserver()
                                            }
                                        }
                                }
                        }
                }
            }
        mTeamList.value = teamList.value
        return teamList
    }

    fun getTournamentNameForLeaderBoard(tournamentName: String) = buildSpannedString {
        run {
        append(tournamentName)
    }
    append("\nLeaderboard")
}

    fun getTournamentName(): String = sharedPrefsRepository.tournamentName!!

    fun getTotalStepsText(teamTournament: TeamTournament) :SpannedString =
        buildSpannedString {
            bold {
                append("Total steps: ")
            }
            append(teamTournament.totalSteps.toString())
        }

    fun getTeamPosition(): Int{
        val userTeam = sharedPrefsRepository.team.name
        val teams = mTeamList.value?: arrayListOf()
        for(i in 0 until teams.size){
            if(teams[i].teamName == userTeam)
                return i+1
        }
        return 0
    }

    fun getLeafCountText(teamTournament: TeamTournament): String = teamTournament.leafCount.toString()

    private fun getUserTournament(tournament: UserTournament, value: Boolean) = //change to TeamTournament
        UserTournament(
            name = tournament.name,
            dailyStepsMap = tournament.dailyStepsMap,
            totalSteps = tournament.totalSteps,
            joinDate = tournament.joinDate,
            isActive = value,
            goal = tournament.goal,
            endDate = tournament.endDate,
            teamName = tournament.teamName
        )


    private fun getTotalLeafCountForTeam(teamTournament: TeamTournament): Int {
        Log.d("WorkerT","getTotalLeafCountForTeam")
        val stepsMap = teamTournament.dailyStepsMap
        val goal = teamTournament.goal
        var leafCount = 0

        val keys = stepsMap.keys.sortedBy {
            it.toLong()
        }

        for (i in 0 until keys.size-1) {
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
        val weeks = ceil(days/7.0).toInt()

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
}

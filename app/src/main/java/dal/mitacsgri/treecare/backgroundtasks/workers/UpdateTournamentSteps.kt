package dal.mitacsgri.treecare.backgroundtasks.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import calculateLeafCountFromStepCountForTeam
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
import org.joda.time.DateTime
import org.joda.time.Days
import org.koin.core.KoinComponent
import org.koin.core.inject

class UpdateTournamentSteps(appContext: Context, workerParams:WorkerParameters): Worker(appContext, workerParams),
    KoinComponent {

    private val stepCountRepository: StepCountRepository by inject()
    private val sharedPrefsRepository: SharedPreferencesRepository by inject()
    private val firestoreRepository: FirestoreRepository by inject()
    var stringy: String = ""
    override fun doWork(): Result {
        if(sharedPrefsRepository.user.name.isEmpty()){
            sharedPrefsRepository.user = User()
        }
        else if(sharedPrefsRepository.team.name.isEmpty()){
            sharedPrefsRepository.team = Team()
        }

        val user = sharedPrefsRepository.user
        val team = sharedPrefsRepository.team

        if(!user.currentTournaments.isNullOrEmpty() && !team.currentTournaments.isNullOrEmpty()) {
            user.currentTournaments.forEach { (_, tourney) ->

                val startTimeMillis = tourney.startDate.toDateTime().millis
                val endTimeMillis = tourney.endDate.toDateTime().millis
                //Two condition checks are applied because the 'isActive' variable is set only after
                //the dialog has been displayed. The second condition check prevents update of Tournament step count
                //in the database even when the dialog has not been displayed
                if (tourney.isActive && endTimeMillis > DateTime().millis) {
                    //if (tourney.isActive) {
                    stepCountRepository.getTodayStepCountData {
                        tourney.dailyStepsMap[DateTime().withTimeAtStartOfDay().millis.toString()] =
                            it
                        val index =
                            tourney.dailyStepsMap.keys.indexOf(DateTime().withTimeAtStartOfDay().millis.toString())
                        Log.d("WorkerT", "Index " + index.toString())
                        updateAndStoreUserTournamentDataInSharedPrefs(tourney, user)
                        calc(team, it, tourney, index)
                    }
                }
            }
//
            updateUserTournamentDataInFirestore(object : MyCallBack {
                override fun onCallBack(value: String) {
                    stringy = value
                }
            })
            //updateUserTeamDataInFirestore(future)
            if(stringy == "Success") return Result.success()
            else return Result.failure()
        }
        else{
            Log.d("WorkerT","CurrentTournament is empty")
            return Result.success()
        }
    }


    private fun updateAndStoreUserTournamentDataInSharedPrefs(tournament: UserTournament, user: User) {
        //tournament.leafCount = getTotalLeafCountForTeam(tournament)
        // tournament.fruitCount = getTotalFruitCountForTournament(tournament)
        // tournament.tournamentGoalStreak = getTournamentGoalStreakForUser(tournament, user)
        // tournament.lastUpdateTime = Timestamp.now()
        Log.d("WorkerT", "updateAndStoreUserTournamentDataInSharedPrefs")
        var totalSteps = 0
        tournament.dailyStepsMap.forEach { (time, steps) ->
            totalSteps += steps
        }
        tournament.totalSteps = totalSteps

        synchronized(sharedPrefsRepository.user) {
            val user = sharedPrefsRepository.user
            user.currentTournaments[tournament.name] = tournament
            sharedPrefsRepository.user = user
        }
        Log.d("WorkerT","UserPref "+ sharedPrefsRepository.user.currentTournaments)
    }

    private fun updateUserTournamentDataInFirestore(myCallBack: MyCallBack) {
        Log.d("WorkerT", "updateUserTournamentDataInFirestore")
        firestoreRepository.updateUserData(sharedPrefsRepository.user.uid,
            mapOf("currentTournaments" to sharedPrefsRepository.user.currentTournaments))
            .addOnSuccessListener() {
                updateUserTeamDataInFirestore(object:MyCallBack{
                    override fun onCallBack(value: String) {
                    }
                })
                Log.d("WorkerT", "TUser data upload success")
                myCallBack.onCallBack("Success")
                //future.set(Result.success())
            }
            .addOnFailureListener {
                Log.e("WorkerT", "TUser data upload failed")
                myCallBack.onCallBack("Failure")
                //future.set(Result.failure())
            }
    }

    private fun updateUserTeamDataInFirestore(myCallBack: MyCallBack) {
        Log.d("WorkerT", "updateUserTeamDataInFirestore")
        Log.d("WorkerT","Pref "+sharedPrefsRepository.team)
        Log.d("WorkerT","PrefValue "+ sharedPrefsRepository.team.currentTournaments)
        firestoreRepository.updateTeamData(sharedPrefsRepository.team.name,
            mapOf("currentTournaments" to sharedPrefsRepository.team.currentTournaments))
            .addOnSuccessListener {
                myCallBack.onCallBack("Success")
                Log.d("WorkerT", "Team User data upload success")
            }
            .addOnFailureListener {
                myCallBack.onCallBack("Failure")
                Log.e("WorkerT", "Team User data upload failed")
            }
    }

    private fun calc(team:Team, currentStepCount: Int, tourney: UserTournament, index:Int) {
        //Sorting the step map according to the dates
        team.currentTournaments.forEach{ (_, teamTourney) ->
            teamTourney.dailyStepsMap = teamTourney.dailyStepsMap.toSortedMap()
        }

        team.currentTournaments.forEach { (_, teamTourney) ->
            if (teamTourney.isActive && teamTourney.endDate.toDateTime().millis > DateTime().millis) {
                if (teamTourney.dailyStepsMap.isNotEmpty()) {
                    Log.d("WorkerT", "TAG2")
                    Log.d("WorkerT", teamTourney.dailyStepsMap.keys.last() + " " + tourney.dailyStepsMap.keys.elementAt(index))

                    if (teamTourney.dailyStepsMap.keys.last() != tourney.dailyStepsMap.keys.elementAt(index)) {
                        //when tournament exists and user starts a new day // user opens the app first day for a day
                        //condition check for new day. Since only one time stamp (date in millis) is used during the update of values
                        //DB fetch is needed
                        firestoreRepository.getTeam(team.name)
                            .addOnSuccessListener {

                                val teamDB = it.toObject<Team>()

                                if (teamDB?.currentTournaments!![teamTourney.name]?.dailyStepsMap?.isNotEmpty()!!) { //check for today's date stamp in db
                                    Log.d("WorkerT", "teamIf")
                                    teamDB.currentTournaments[teamTourney.name]?.dailyStepsMap!![DateTime().withTimeAtStartOfDay().millis.toString()] =
                                        currentStepCount
                                    updateAndStoreTeamDataInSharedPrefs(teamDB.currentTournaments[teamTourney.name]!!, team)
                                    sharedPrefsRepository.storeLastDayStepCount(currentStepCount)
                                }

                                else {
                                    Log.d("WorkerT", "teamElse")
                                    teamTourney.dailyStepsMap[DateTime().withTimeAtStartOfDay().millis.toString()] =
                                        currentStepCount
                                    sharedPrefsRepository.storeLastDayStepCount(currentStepCount)
                                    updateAndStoreTeamDataInSharedPrefs(teamTourney, team)
                                }
                                //                                teamDB.currentTournaments.forEach { (_, dbTournament) ->
                                //                                    if (teamDB.name == tourney.name) {
                                //                                        dbTournament.dailyStepsMap[DateTime().withTimeAtStartOfDay().millis.toString()] =
                                //                                            currentStepCount
                                //                                        updateAndStoreTeamDataInSharedPrefs(dbTournament, team)
                                //                                    }
                                //                                }

                            }
                    }

                    else {
                        Log.d("WorkerT", "LastDayStepCount " + sharedPrefsRepository.getLastDayStepCount())
                        //val oldStep = tourney.dailyStepsMap[DateTime().withTimeAtStartOfDay().millis.toString()]
                        firestoreRepository.getTeam(sharedPrefsRepository.team.name)
                            .addOnSuccessListener {
                                val team = it.toObject<Team>()
                                for (teamDB in team?.currentTournaments?.values!!) {
                                    if (teamDB.name == tourney.name) {
                                        //team?.currentTournaments?.forEach{(_, teamDB) ->\
                                        val oldStep =
                                            teamDB.dailyStepsMap[DateTime().withTimeAtStartOfDay().millis.toString()]
                                        Log.d("WorkerT", "OldStep " + oldStep)

                                        val diff =
                                            currentStepCount - sharedPrefsRepository.getLastDayStepCount()
                                        Log.d("WorkerT", "Diff ${tourney.name} " + diff)

                                        if (diff > 0) {
                                            teamTourney.dailyStepsMap[DateTime().withTimeAtStartOfDay().millis.toString()] =
                                                oldStep!! + diff
                                            updateAndStoreTeamDataInSharedPrefs(teamTourney, team)
                                            sharedPrefsRepository.storeLastDayStepCount(currentStepCount) //store only during the first iteration
                                        }

                                        else {
                                            continue
                                        }
                                    }
                                }
                            }
                    }
                }
                //                                        teamTourney.dailyStepsMap[DateTime().withTimeAtStartOfDay().millis.toString()] = oldStep!! + diff
                //                                        sharedPrefsRepository.storeLastDayStepCount(it)
                //                                        updateAndStoreTeamDataInSharedPrefs(teamTourney)

                else{
                    // When tournament exists but doesn't have steps in the teams collection
                    // take care of updating team and tour prefs when user has joined a tournament/team (when user is not a captain)
                    teamTourney.dailyStepsMap[DateTime().withTimeAtStartOfDay().millis.toString()] = currentStepCount
                    sharedPrefsRepository.storeLastDayStepCount(currentStepCount)
                    updateAndStoreTeamDataInSharedPrefs(teamTourney, team)
                }
            }
        }
    }

    private fun updateAndStoreTeamDataInSharedPrefs(teamTourney: TeamTournament, team: Team) {
        teamTourney.leafCount = getTotalLeafCountForTeam(teamTourney)
        teamTourney.fruitCount = getTotalFruitCountForTeam(teamTourney)
        teamTourney.tournamentGoalStreak = getTeamGoalStreakForUser(teamTourney, team)
        teamTourney.lastUpdateTime = Timestamp.now()
        Log.d("WorkerT", "updateAndStoreTeamDataInSharedPrefs")
        var totalSteps = 0
        teamTourney.dailyStepsMap.forEach { (time, steps) ->
            totalSteps += steps
        }
        teamTourney.totalSteps = totalSteps

        synchronized(sharedPrefsRepository.team) {
            val team = sharedPrefsRepository.team
            team.currentTournaments[teamTourney.name] = teamTourney
            sharedPrefsRepository.team = team
        }
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

        return fruitCount
    }

    private fun calculateTeamFruitCountForWeek(tournament: TeamTournament, stepCountMap: Map<String, Int>): Int {
        Log.d("WorkerT","calculateTeamFruitCountForWeek")
        var currentDay = 0
        val goalAchievedStreak = arrayOf(false, false, false, false, false, false, false)
        val fullStreak = arrayOf(true, true, true, true, true, true, true)

        if (stepCountMap.size < 7) return 0

        stepCountMap.forEach { (_, stepCount) ->
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

    interface MyCallBack{
        fun onCallBack(value : String)

    }
}
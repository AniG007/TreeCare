package dal.mitacsgri.treecare.screens.challenges

import android.text.SpannedString
import android.util.Log
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import calculateLeafCountFromStepCount
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import dal.mitacsgri.treecare.backgroundtasks.workers.UpdateTeamDataWorker
import dal.mitacsgri.treecare.backgroundtasks.workers.UpdateUserChallengeDataWorker
import dal.mitacsgri.treecare.consts.CHALLENGER_MODE
import dal.mitacsgri.treecare.consts.CHALLENGE_TYPE_DAILY_GOAL_BASED
import dal.mitacsgri.treecare.extensions.*
import dal.mitacsgri.treecare.model.Challenge
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.model.UserChallenge
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class ChallengesViewModel(
    private val sharedPrefsRepository: SharedPreferencesRepository,
    private val firestoreRepository: FirestoreRepository,
    private val stepCountRepository: StepCountRepository
    ): ViewModel() {

    companion object Types {
        const val ACTIVE_CHALLENGES = 0
        const val CHALLENGES_BY_YOU = 1
    }

    val activeChallengesList = MutableLiveData<ArrayList<Challenge>>().default(arrayListOf())
    val currentChallengesList = MutableLiveData<ArrayList<Challenge>>().default(arrayListOf())
    val challengesByYouList = MutableLiveData<ArrayList<Challenge>>().default(arrayListOf())

    //The error status message must contain 'error' in string because it is used to check whether to
    //disable or enable join button
    val statusMessage = MutableLiveData<String>()
    var messageDisplayed = true

    fun getAllActiveChallenges(): MutableLiveData<ArrayList<Challenge>> {
        WorkManager.getInstance().cancelUniqueWork("challengeWorker")

        firestoreRepository.getAllActiveChallenges()
            .addOnSuccessListener {
                val challenges = it.toObjects<Challenge>()
//                activeChallengesList.value = it.toObjects<Challenge>().filter { it.exist && it.active }.toArrayList()
//                activeChallengesList.notifyObserver()
                val user = sharedPrefsRepository.user

                activeChallengesList.value =
                    it.toObjects<Challenge>().filter { it.exist && it.active }
                        .toArrayList()
                activeChallengesList.notifyObserver()

                if (user.currentChallenges.isNotEmpty() && !user.currentChallenges.isNullOrEmpty()) {
                    for (challenge in user.currentChallenges) {
                        if (challenge.value.isActive) {
                            Log.d("Today", "today ${DateTime().withTimeAtStartOfDay().millis}")
                            stepCountRepository.getTodayStepCountData {
                                user.currentChallenges[challenge.key]?.dailyStepsMap!![DateTime().withTimeAtStartOfDay().millis.toString()] = it
                                synchronized(sharedPrefsRepository.user) {
                                    Log.d("chlg", it.toString())
                                    val userData = sharedPrefsRepository.user
                                    userData.currentChallenges[challenge.key] =
                                        user.currentChallenges[challenge.key]!!
                                    sharedPrefsRepository.user = userData
                                }
                                if(user.currentChallenges[challenge.key]?.isActive!!)
                                    updateAndStoreUserChallengeDataInSharedPrefs(user.currentChallenges[challenge.key]!!, user)
                            }
                        }
                    }
                }
            }
        enqueWorkForChallenge()
        return activeChallengesList
    }

    fun getCurrentChallengesForUser(): MutableLiveData<ArrayList<Challenge>> {
        val challengeReferences = sharedPrefsRepository.user.currentChallenges

        challengeReferences.forEach { (_, userChallenge) ->
            //Getting challenges from the Challenges DB after getting reference
            // from the challenges list obtained from the user
            firestoreRepository.getChallenge(userChallenge.name)
                .addOnSuccessListener {
                    val challenge = it.toObject<Challenge>() ?: Challenge(exist = false)
                    synchronized(currentChallengesList.value!!) {
                        if (challenge.exist) {
                            currentChallengesList.value?.sortAndAddToList(challenge)
                            currentChallengesList.notifyObserver()
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d("Challenge not found", it.toString())
                }
        }
        return currentChallengesList
    }

    fun getAllCreatedChallengesChallenges(userId: String): MutableLiveData<ArrayList<Challenge>> {
        firestoreRepository.getAllChallengesCreatedByUser(userId)
            .addOnSuccessListener {
                challengesByYouList.value =
                    it.toObjects<Challenge>().filter { it.exist }.toArrayList()
                challengesByYouList.notifyObserver()
            }
            .addOnFailureListener {
                Log.e("Active challenges", "Fetch failed: $it")
            }
        return challengesByYouList
    }

    fun joinChallenge(challenge: Challenge, successAction: () -> Unit) {
        val userChallenge = getUserChallenge(challenge)
        val uid = sharedPrefsRepository.user.uid

        updateUserSharedPrefsData(userChallenge)

        firestoreRepository.updateUserData(
            uid,
            mapOf("currentChallenges.${challenge.name}" to userChallenge)
        )
            .addOnSuccessListener {
                //updateUserSharedPrefsData(userChallenge)
                messageDisplayed = false
                statusMessage.value = "You are now a part of ${challenge.name}"
                var index = activeChallengesList.value?.indexOf(challenge)!!
                activeChallengesList.value?.get(index)?.players?.add(uid)
                activeChallengesList.notifyObserver()

                index = challengesByYouList.value?.indexOf(challenge)!!
                Log.d("Test", "Index " + index)
                if (index != -1) challengesByYouList.value?.get(index)?.players?.add(uid)
                challengesByYouList.notifyObserver()

                //Do this to display the leaf count as soon as the user joins the challenge
                if (challenge.type == CHALLENGE_TYPE_DAILY_GOAL_BASED) {
                    userChallenge.leafCount = sharedPrefsRepository.getDailyStepCount() / 1000
                }

                val user = sharedPrefsRepository.user
                user.currentChallenges[challenge.name] = userChallenge
                sharedPrefsRepository.user = user
                successAction()
            }
            .addOnFailureListener {
                messageDisplayed = false
                statusMessage.value = "Error joining challenge"
                Log.e("Error joining challenge", it.toString())
            }

        firestoreRepository.updateChallengeData(
            challenge.name,
            mapOf("players" to FieldValue.arrayUnion(sharedPrefsRepository.user.uid))
        )

        currentChallengesList.value?.add(challenge)
        currentChallengesList.notifyObserver()

        //Update data as soon as user joins a challenge
        val updateUserChallengeDataRequest =
            OneTimeWorkRequestBuilder<UpdateUserChallengeDataWorker>().build()
        WorkManager.getInstance().enqueue(updateUserChallengeDataRequest).result.addListener(
            Runnable {
                Log.d("Challenge data", "updated by work manager")
            }, MoreExecutors.directExecutor()
        )
    }

    fun leaveChallenge(challenge: Challenge) {
        val userId = sharedPrefsRepository.user.uid
        var counter = 0

        firestoreRepository.deleteUserFromChallengeDB(challenge, userId)
            .addOnSuccessListener {
                synchronized(counter) {
                    counter++
                    if (counter == 2) {
                        removeChallengeFromCurrentChallengesLists(challenge)
                    }
                }
                Log.d("Challenge deleted", "from DB")
            }
            .addOnFailureListener {
                Log.e("Challenge delete failed", it.toString())
            }

        val userChallenge = getUserChallenge(challenge).let {
            it.isCurrentChallenge = false
            it
        }

        //TODO: Maybe later on we can think of only disabling the challenge instead of actually deleting from the database - done
        firestoreRepository.deleteChallengeFromUserDB(
            userId,
            userChallenge.name
            //userChallenge.toJson<UserChallenge>()
        )
            .addOnSuccessListener {
                synchronized(counter) {
                    counter++
                    if (counter == 2) {
                        removeChallengeFromCurrentChallengesLists(challenge)
                    }
                }
                statusMessage.value = "Success"
            }
            .addOnFailureListener {
                statusMessage.value = "Failed"
            }

        var index = activeChallengesList.value?.indexOf(challenge)!!
        activeChallengesList.value?.get(index)?.players?.remove(sharedPrefsRepository.user.uid)
        activeChallengesList.notifyObserver()

        index = currentChallengesList.value?.indexOf(challenge)!!
        currentChallengesList.value?.get(index)?.players?.remove(sharedPrefsRepository.user.uid)
        currentChallengesList.notifyObserver()
    }

    fun deleteChallenge(challenge: Challenge) {
        firestoreRepository.setChallengeAsNonExist(challenge.name)
            .addOnSuccessListener {
                activeChallengesList.value?.remove(challenge)
                activeChallengesList.notifyObserver()

                currentChallengesList.value?.remove(challenge)
                currentChallengesList.notifyObserver()

                challengesByYouList.value?.remove(challenge)
                challengesByYouList.notifyObserver()
            }
            .addOnFailureListener {
                Log.e("Deletion failed", it.toString())
            }
    }

    fun startUnityActivityForChallenge(challenge: Challenge, action: () -> Unit) {

        if(challenge.active) {
            sharedPrefsRepository.apply {

                val userChallenge = user.currentChallenges[challenge.name]!!
                //Log.d("Unity", "UserSteps"+ userChallenge.totalSteps)
                gameMode = CHALLENGER_MODE
                challengeType = userChallenge.type
                challengeGoal = userChallenge.goal
                challengeLeafCount = userChallenge.leafCount
                challengeFruitCount = userChallenge.fruitCount
                challengeStreak = userChallenge.challengeGoalStreak
                challengeName = userChallenge.name
                isChallengeActive = true // (userChallenge.endDate.toDateTime().millis > DateTime().millis)
                challengeActive = 1
                //challengeTotalStepsCount = if (userChallenge.isActive) userChallenge.totalSteps else  userChallenge.totalSteps //getDailyStepCount()
                challengeTotalStepsCount = getDailyStepCount()
                //challengeTotalStepsCount = getDailyStepCount()
                //Log.d("Unity", challengeTotalStepsCount.toString())
                Log.d("Unity", "UserStepsActive" + isChallengeActive)
                action()
            }
        }
        else{
            sharedPrefsRepository.apply {

                val userChallenge = user.currentChallenges[challenge.name]!!
                //Log.d("Unity", "UserSteps"+ userChallenge.totalSteps)
                gameMode = CHALLENGER_MODE
                challengeType = userChallenge.type
                challengeGoal = userChallenge.goal
                challengeLeafCount = userChallenge.leafCount
                challengeFruitCount = userChallenge.fruitCount
                challengeStreak = userChallenge.challengeGoalStreak
                challengeName = userChallenge.name
                isChallengeActive = false // (userChallenge.endDate.toDateTime().millis > DateTime().millis)
                challengeActive = 0
                //challengeTotalStepsCount = if (userChallenge.isActive) userChallenge.totalSteps else  userChallenge.totalSteps //getDailyStepCount()
                challengeTotalStepsCount = userChallenge.totalSteps
                //challengeTotalStepsCount = getDailyStepCount()
                Log.d("Unity", "UserStepsInActive" + isChallengeActive)
                action()
            }
        }
    }

    fun getChallengeDurationText(challenge: Challenge): SpannedString {
        val finishDate = challenge.finishTimestamp.toDateTime().millis
        val finishDateString =
            challenge.finishTimestamp.toDateTime().getStringRepresentation().split(",")
        val finishDateFormat1 =
            SimpleDateFormat("HH:mm") //reference for conversion: https://beginnersbook.com/2017/10/java-display-time-in-12-hour-format-with-ampm/?unapproved=224896&moderation-hash=b24a6ccf99e5d9013cc45de55849ebb4#comment-224896
        val finishDateParsed = finishDateFormat1.parse(finishDateString.get(1).trim())
        val finishDateFormat2 = SimpleDateFormat("hh:mm aa")
        val finishDateTime = finishDateFormat2.format(finishDateParsed)
        val finishDateText = finishDateString.get(0) + ", " + finishDateTime

        val challengeEnded = finishDate < DateTime().millis

        return buildSpannedString {
            bold {
                append(if (challengeEnded) "Ended: " else "Ends: ")
            }
            append(finishDateText)
        }
    }

    fun hasUserJoinedChallenge(challenge: Challenge): Boolean {
        return sharedPrefsRepository.user.currentChallenges[challenge.name] != null
    }

    fun getChallengeTypeText(challenge: Challenge) =
        buildSpannedString {
            bold {
                append("Type: ")
            }
            append(
                if (challenge.type == CHALLENGE_TYPE_DAILY_GOAL_BASED) "Daily Goal Based"
                else "Aggregate based"
            )
        }

    fun getGoalText(challenge: Challenge) =
        buildSpannedString {
            bold {
                append(
                    if (challenge.type == CHALLENGE_TYPE_DAILY_GOAL_BASED) "Minimum Daily Goal: "
                    else "Total steps goal: "
                )
            }
            append(challenge.goal.toString())
        }

    fun getPlayersCountText(challenge: Challenge) = challenge.players.size.toString()

    fun getCurrentUserId() = sharedPrefsRepository.user.uid

    fun getJoinChallengeDialogTitleText(challenge: Challenge) =
        buildSpannedString {
            append("Join challenge ")
            bold {
                append("'${challenge.name}'")
            }
        }

    fun getJoinChallengeMessageText() = "Are you ready to join now?"

    fun storeChallengeLeaderboardPosition(position: Int) {
        sharedPrefsRepository.challengeLeaderboardPosition = position
    }


    private fun updateUserSharedPrefsData(userChallenge: UserChallenge) {
        val user = sharedPrefsRepository.user
        userChallenge.leafCount = sharedPrefsRepository.getDailyStepCount() / 1000
        userChallenge.totalSteps = sharedPrefsRepository.getDailyStepCount()
        user.currentChallenges[userChallenge.name] = userChallenge
        sharedPrefsRepository.user = user
    }

    private fun getUserChallenge(challenge: Challenge) =
        UserChallenge(
            name = challenge.name,
            dailyStepsMap = mutableMapOf(),
            totalSteps = sharedPrefsRepository.getDailyStepCount(),
            joinDate = DateTime().millis,
            type = challenge.type,
            goal = challenge.goal,
            endDate = challenge.finishTimestamp
        )

    private fun removeChallengeFromCurrentChallengesLists(challenge: Challenge) {

        currentChallengesList.value?.remove(challenge)
        currentChallengesList.notifyObserver()

        sharedPrefsRepository.user = sharedPrefsRepository.user.let {
            it.currentChallenges.remove(challenge.name)
            it
        }
    }

    private fun ArrayList<Challenge>.sortAndAddToList(challenge: Challenge) {
        val finishTimestampMillis = challenge.finishTimestamp.toDateTime().millis
        if (size == 0) {
            add(challenge)
            return
        }

        for (i in 0 until size) {
            if (this[i].finishTimestamp.toDateTime().millis < finishTimestampMillis) {
                add(i, challenge)
                return
            }
        }
        this.add(challenge)
    }

    fun disp(challenge: Challenge) {
        //val usr = sharedPrefsRepository.user.currentChallenges[challenge.name]
        //Log.d("Test", usr?.name!!)
        /*val challenges = sharedPrefsRepository.user
        challenges.currentChallenges["TestChallenger"]?.dailyStepsMap!!["1604635200000"] = 5001
        challenges.currentChallenges["TestChallenger"]?.dailyStepsMap!!["1604721600000"] = 5001
        challenges.currentChallenges["TestChallenger"]?.dailyStepsMap!!["1604808000000"] = 5001
        challenges.currentChallenges["TestChallenger"]?.dailyStepsMap!!["1604894400000"] = 5001
        challenges.currentChallenges["TestChallenger"]?.dailyStepsMap!!["1604980800000"] = 5001
        challenges.currentChallenges["TestChallenger"]?.dailyStepsMap!!["1605067200000"] = 5001
        challenges.currentChallenges["TestChallenger"]?.dailyStepsMap!!["1605153600000"] = 5001
        challenges.currentChallenges["TestChallenger"]?.dailyStepsMap!!["1605240000000"] = 5001
        sharedPrefsRepository.user = challenges

        Log.d("disp", sharedPrefsRepository.user.currentChallenges["TestChallenger"].toString())*/

    }

    fun enqueWorkForChallenge() {
        val mConstraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val updateUserChallengeDataRequest =
            PeriodicWorkRequestBuilder<UpdateUserChallengeDataWorker>(15, TimeUnit.MINUTES)
                .setConstraints(mConstraints)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            "challengeWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            updateUserChallengeDataRequest,
        )
    }

//    fun updateChallengeData() {
//
//        firestoreRepository.updateUserData(
//            sharedPrefsRepository.user.uid,
//            mapOf("currentChallenges" to sharedPrefsRepository.user.currentChallenges)
//        )
//            .addOnSuccessListener {
//                Log.d("chlg", "challenge update was successful")
//                /*firestoreRepository.getAllActiveChallenges().addOnSuccessListener {
//                    activeChallengesList.value =
//                        it.toObjects<Challenge>().filter { it.exist && it.active }
//                            .toArrayList()
//                    activeChallengesList.notifyObserver()
//                }*/
////                    }
//            }
//            .addOnFailureListener {
//                Log.e("Active challenges", "Fetch failed: $it")
//            }
//    }

    private fun updateAndStoreUserChallengeDataInSharedPrefs(challenge: UserChallenge, user: User) {
        challenge.leafCount = getTotalLeafCountForChallenge(challenge)
        challenge.fruitCount = getTotalFruitCountForChallenge(challenge)
        challenge.challengeGoalStreak = getChallengeGoalStreakForUser(challenge, user)
        challenge.lastUpdateTime = Timestamp.now()

        var totalSteps = 0
        challenge.dailyStepsMap.forEach { (time, steps) ->
            totalSteps += steps
        }
        challenge.totalSteps = totalSteps

        synchronized(sharedPrefsRepository.user) {
            val userData = sharedPrefsRepository.user
            userData.currentChallenges[challenge.name] = challenge
            sharedPrefsRepository.user = user
        }
        updateUserChallengeDataInFirestore()
    }

    private fun updateUserChallengeDataInFirestore() {
        firestoreRepository.updateUserData(
            sharedPrefsRepository.user.uid,
            mapOf("currentChallenges" to sharedPrefsRepository.user.currentChallenges)
        )
    }

    private fun getChallengeGoalStreakForUser(challenge: UserChallenge, user: User): Int {
        val userChallengeData = user.currentChallenges[challenge.name]!!
        var streakCount = 0

        userChallengeData.dailyStepsMap.forEach { (date, stepCount) ->
            //This check prevents resetting streak count if goal is yet to be met today
            if (date.toLong() < DateTime().withTimeAtStartOfDay().millis) {
                if (stepCount >= challenge.goal) streakCount++
                else streakCount = 0
            }
        }
        return streakCount
    }

    private fun getTotalLeafCountForChallenge(challenge: UserChallenge): Int {
        val stepsMap = challenge.dailyStepsMap
        val goal = challenge.goal
        var leafCount = 0

        val keys = stepsMap.keys.sortedBy {
            it.toLong()
        }

        for (i in 0 until keys.size-1) {
            leafCount += calculateLeafCountFromStepCount(stepsMap[keys[i]]!!, goal)
        }
        leafCount += stepsMap[keys[keys.size-1]]!! / 1000

        return leafCount
    }

    private fun getTotalFruitCountForChallenge(challenge: UserChallenge): Int {
        val joinDate = DateTime(challenge.joinDate)
        val currentDate = DateTime()
        val days = Days.daysBetween(joinDate, currentDate).days
        val weeks = Math.ceil(days/7.0).toInt()

        //Log.d("Worker", "weeks "+ weeks)
        var fruitCount = 0

        var weekStartDate = joinDate
        var newWeekDate = weekStartDate.plusWeeks(1)
        var mapPartition: Map<String, Int>
        for (i in 0 until weeks) {
            mapPartition = challenge.dailyStepsMap.filter {
                val keyAsLong = it.key.toLong()
                keyAsLong >= weekStartDate.millis && keyAsLong < newWeekDate.millis
            }
            fruitCount += calculateFruitCountForWeek(challenge, mapPartition)
            weekStartDate = newWeekDate
            newWeekDate = weekStartDate.plusWeeks(1)
        }

        Log.d("fruits", fruitCount.toString())
        if(fruitCount < 0)
            fruitCount = 0
        return fruitCount
    }

    private fun calculateFruitCountForWeek(challenge: UserChallenge, stepCountMap: Map<String, Int>): Int {
        var currentDay = 0
        val goalAchievedStreak = arrayOf(false, false, false, false, false, false, false)
        val fullStreak = arrayOf(true, true, true, true, true, true, true)

        if (stepCountMap.size < 7) return 0

        stepCountMap.forEach { (_, stepCount) ->
            goalAchievedStreak[currentDay] =
                stepCount >= challenge.goal
            currentDay++
        }

        return if (goalAchievedStreak.contentEquals(fullStreak)) 1 else -1
    }
}
package dal.mitacsgri.treecare.backgroundtasks.jobs

import android.util.Log
import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobRequest
import com.google.firebase.firestore.ktx.toObject
import dal.mitacsgri.treecare.model.Challenge
import dal.mitacsgri.treecare.model.Tournament
import dal.mitacsgri.treecare.model.UserChallengeTrophies
import dal.mitacsgri.treecare.model.UserTournamentTrophies
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class TrophiesUpdateJob : DailyJob(), KoinComponent {

    private val sharedPrefsRepository: SharedPreferencesRepository by inject()
    private val firestoreRepository: FirestoreRepository by inject()

    private var challengesCounter = ObservableInt()
    private var tournamentsCounter = ObservableInt1()

    companion object {

        const val TAG = "TrophiesUpdateJob"

        fun scheduleJob() {
//            schedule(
//                JobRequest.Builder(TAG),
//                TimeUnit.HOURS.toMillis(17) + TimeUnit.MINUTES.toMillis(14),
//                TimeUnit.HOURS.toMillis(17) + TimeUnit.MINUTES.toMillis(30)
//            )
            Log.d(TAG, "Scheduling Job")
            schedule(JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(0) + TimeUnit.MINUTES.toMillis(15),
                TimeUnit.HOURS.toMillis(0) + TimeUnit.MINUTES.toMillis(40))
        }
    }

    override fun onRunDailyJob(p0: Params): DailyJobResult {
        val currentChallenges = sharedPrefsRepository.user.currentChallenges
        val userTrophies = UserChallengeTrophies()
        Log.d(TAG, "Running Job")
        val currentTournaments = sharedPrefsRepository.team.currentTournaments
        val tournamentTrophies = UserTournamentTrophies()

        currentChallenges.forEach { (name, userChallenge) ->
            firestoreRepository.getChallenge(name).addOnSuccessListener {
                val challenge = it.toObject<Challenge>()
                Log.d(TAG, "Running Job for challenges")
                if (challenge != null && !challenge.active) {
                    Log.d(TAG, "Some challenge is not active")
                    when (challenge.players.indexOf(sharedPrefsRepository.user.uid)) {
                        0 -> userTrophies.gold.add(name)
                        1 -> userTrophies.silver.add(name)
                        2 -> userTrophies.bronze.add(name)
                    }

                    challengesCounter.setValue(challengesCounter.getValue() + 1) {
                        Log.d(TAG, "C counter " + it + "C size " + currentTournaments.size)
                        if (it == currentChallenges.size) {
                            firestoreRepository.storeTrophiesData(
                                sharedPrefsRepository.user.uid,
                                userTrophies
                            )
                                .addOnSuccessListener {
                                    Log.d(TAG, "Success")
                                }
                                .addOnFailureListener {
                                    Log.d(TAG, it.toString())
                                }
                        }
                    }
                }
            }
        }

        currentTournaments.forEach { (name, userTournament) ->
            firestoreRepository.getTournament(name).addOnSuccessListener {
                val tournament = it.toObject<Tournament>()
                Log.d(TAG, "Running Job for tournaments")
                if (tournament != null && !tournament.active) {
                    Log.d(TAG, "Some tournament is not active")
                    Log.d(TAG, "index of your team " + tournament.teams.indexOf(sharedPrefsRepository.team.name).toString())
                    Log.d(TAG, "Name of your tournament " + tournament.name + "Teams in it " + tournament.teams)
                    when (tournament.teams.indexOf(sharedPrefsRepository.team.name)) {
                        0 -> tournamentTrophies.gold.add(name)
                        1 -> tournamentTrophies.silver.add(name)
                        2 -> tournamentTrophies.bronze.add(name)
                    }
                }

                tournamentsCounter.setValue(tournamentsCounter.getValue() + 1) {
                    Log.d(TAG, "counter " + it + "Size " + currentTournaments.size)
                    if (it == currentTournaments.size) {
                        Log.d(TAG, "Size equals tournament counter")
                        firestoreRepository.storeTeamTrophiesData(
                            sharedPrefsRepository.team.name,
                            tournamentTrophies
                        )
                            .addOnSuccessListener {
                                Log.d(TAG, "TSuccess")
                            }
                            .addOnFailureListener {
                                Log.d(TAG, "TFailure")
                            }
                    }
                }
                //}
            }
        }

        return DailyJobResult.SUCCESS
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
}
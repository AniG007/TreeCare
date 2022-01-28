package dal.mitacsgri.treecare

import android.app.Application
import androidx.work.*
import com.evernote.android.job.JobApi
import com.evernote.android.job.JobConfig
import com.evernote.android.job.JobManager
import dal.mitacsgri.treecare.backgroundtasks.jobcreator.MainJobCreator
import dal.mitacsgri.treecare.backgroundtasks.jobs.DailyGoalNotificationJob
import dal.mitacsgri.treecare.backgroundtasks.jobs.TrophiesUpdateJob
import dal.mitacsgri.treecare.backgroundtasks.workers.*
import dal.mitacsgri.treecare.di.appModule
import dal.mitacsgri.treecare.di.firestoreRepositoryModule
import dal.mitacsgri.treecare.di.sharedPreferencesRepositoryModule
import dal.mitacsgri.treecare.di.stepCountRepositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit.*

class TreeCareApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TreeCareApplication)
            modules(
                listOf(
                    appModule, sharedPreferencesRepositoryModule, stepCountRepositoryModule,
                    firestoreRepositoryModule
                )
            )
        }

        val mConstraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val updateUserChallengeDataRequest =
            PeriodicWorkRequestBuilder<UpdateUserChallengeDataWorker>(15, MINUTES)
                .setConstraints(mConstraints)
                .setInitialDelay(5, MINUTES)
                .build()
//
        val updateDailyStepCountDataRequest =
            PeriodicWorkRequestBuilder<UpdateDailyStepCount>(15, MINUTES)
                .setConstraints(mConstraints)
                .setInitialDelay(5, MINUTES)
                .build()
//
        val updateTeamDataRequest =
            PeriodicWorkRequestBuilder<UpdateTeamDataWorker>(15, MINUTES)
                .setConstraints(mConstraints)
                .setInitialDelay(5, MINUTES)
                .build()


        val dailyGoalNotificationRequest =
            PeriodicWorkRequestBuilder<DailyNotificationWorker>(3, HOURS)
                .setConstraints(mConstraints)
                .setInitialDelay(5, MINUTES)
                .build()


        val trophiesUpdateRequest =
            PeriodicWorkRequestBuilder<TrophiesUpdateWorker>(8, HOURS)
                .setConstraints(mConstraints)
                .setInitialDelay(5, MINUTES)
                .build()

        val syncDataRequest =
            PeriodicWorkRequestBuilder<DataSyncWorker>(15, MINUTES)
                .setConstraints(mConstraints)
                .setInitialDelay(2, MINUTES)
                .build()

        /*WorkManager.getInstance(this).enqueue(
            listOf(
                dailyGoalNotificationRequest,
                trophiesUpdateRequest
            )
        )*/

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "teamWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            updateTeamDataRequest
        )

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "challengeWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            updateUserChallengeDataRequest
        )
//
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dailyStepsWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            updateDailyStepCountDataRequest
        )
//
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "notificationWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyGoalNotificationRequest
        )

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "trophiesWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            trophiesUpdateRequest
        )

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "syncWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            syncDataRequest
        )
    }
}
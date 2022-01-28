package dal.mitacsgri.treecare.backgroundtasks.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class UpdateUserChallengeDataWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testUpdateUserChallengeDataWorker() {
        val worker = TestListenableWorkerBuilder<UpdateUserChallengeDataWorker>(context).build()

        runBlocking {
            val result = worker.startWork()
            result.addListener(Runnable {
            }, Executors.newSingleThreadExecutor())
            assertThat(result.get(), `is`(Result.success()))
        }
    }
}
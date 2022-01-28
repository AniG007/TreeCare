package dal.mitacsgri.treecare.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.DataType.*
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.fitness.result.DataReadResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.TimeUnit


class StepCountRepository(private val context: Context) {

    fun getTodayStepCountData(onDataObtained: (stepCount: Int) -> Unit) {
        //Intrinsics.checkParameterIsNotNull(onDataObtained, "onDataObtained")
        var total = 0
        //total.element = 0

        val currentTime = Calendar.getInstance()
        val midNight = Calendar.getInstance()
        val now = Date()
        midNight.apply {
            time = now
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR, -12) //Setting time to current day's midnight. setting it to 0, was putting the clock at noon initially
        }
        val response = Fitness.getHistoryClient(
            context,
            GoogleSignIn.getLastSignedInAccount(context)!!
        )
            .readDailyTotal(TYPE_STEP_COUNT_DELTA)
            //.readDailyTotal(TYPE_DISTANCE_CUMULATIVE)

        response.addOnSuccessListener {
            if (it.dataPoints.size > 0)
                total = it.dataPoints[0].getValue(Field.FIELD_STEPS).asInt()
                //total = it.dataPoints[0].getValue(Field.FIELD_DISTANCE).asInt()
            onDataObtained(total)
        }.addOnFailureListener {
            Log.e("DailyStepCount", "error: $it")
            onDataObtained(total)
        }
        //Log.d("Test",currentTime.toString())

        Log.d("Test","Midnight"+midNight.timeInMillis)
        Log.d("Test","CurrentTime"+currentTime.timeInMillis)

        /*val response: Task<DataReadResponse> =
            Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
                .readData(
                    DataReadRequest.Builder()
                        .aggregate(TYPE_STEP_COUNT_DELTA, AGGREGATE_STEP_COUNT_DELTA)
                        .setTimeRange(
                            midNight.timeInMillis,
                            currentTime.timeInMillis,
                            TimeUnit.MILLISECONDS
                        )
                        .bucketByTime(1, TimeUnit.DAYS)
                        .build()
                )
                .addOnSuccessListener {
                    Log.d("Test","dataset "+it.dataSets.toString())
                    Log.d("Test","Millis"+System.currentTimeMillis().toString())
                    Log.d("Test", "Buckets "+it.buckets.toString())
                    Log.d("Test","Millis"+System.currentTimeMillis().toString())
                    for (bucket in it.buckets) {
                        Log.d("Test","Bucket "+bucket)
                        //val dataSets = bucket.dataSets
                        val dataSets: List<DataSet>  = bucket.dataSets
                        for (dataSet in dataSets) {
                            Log.d("Test","dataset "+dataSet.toString())
                            Log.d("Test","dp's"+dataSet.dataPoints.toString())
                            for (dataPoint in dataSet.dataPoints) {
                                Log.d("Datapoint", dataPoint.toString())
                                onDataObtained(dataPoint)
                            }
                        }
                    }
                }
                .addOnFailureListener{
                    Log.d("Test","Failed "+it.toString())
                }*/
    }



//        val readDataResult: DataReadResponse? = Tasks.await(response)
    /* response.addOnSuccessListener {
//val dataSet: DataSet = response.result!!.getDataSet(TYPE_STEP_COUNT_DELTA)
val dataSet: DataSet = it.getDataSet(TYPE_STEP_COUNT_DELTA)
Log.d("Test", dataSet.dataPoints.toString())
}
response.addOnFailureListener{
Log.d("Test","Unable to fetch today's step data $it")
}*/

    // val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context)
//        val fitnessOptions: GoogleSignInOptionsExtension = FitnessOptions.builder()
//            .addDataType(
//                TYPE_STEP_COUNT_DELTA,
//                FitnessOptions.ACCESS_READ
//            )
//            .build()
    // val googleSignInAccount = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    //Log.d("Test","name ${googleSignInAccount.displayName}")




    /*response.addOnSuccessListener {
        total = it.getDataPoints().size
        //total = it.dataPoints[0].getValue(Field.FIELD_STEPS).asInt()
        // total = it.getDataPoints().get(0).getValue(FIELD_STEPS).asInt();
        Log.d("DailyStepCount","This "+total.toString())
        Log.d("DailyStepCount", total.toString())
        onDataObtained(total)
    }.addOnFailureListener {
        Log.e("DailyStepCount", "error: $it")
        onDataObtained(total)
    }*/





    fun getLastDayStepCountData(mClient: GoogleApiClient, onDataObtained: (stepCount: Int) -> Unit) {
        val cal = Calendar.getInstance()
        val now = Date()
        cal.apply {
            time = now
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR, 0)
        }

        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = cal.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        context.doAsync {
            val result = Fitness.HistoryApi.readData(mClient, readRequest).await(30, TimeUnit.SECONDS)
            var lastDayStepCount = 0

            if (result.status.isSuccess) {
                for (bucket in result.buckets) {
                    val dataSets = bucket.dataSets
                    for (dataSet in dataSets) {
                        for (dataPoint in dataSet.dataPoints) {
                            //Log.d("Datapoint", dataPoint.toString())
                            lastDayStepCount += dataPoint.getValue(dataPoint.dataType.fields[0]).asInt()
                        }
                    }

                    uiThread {
                        onDataObtained(lastDayStepCount)
                    }
                }
            }
        }
    }

    fun getStepCountDataOverARange(startTime: Long, endTime: Long, onDataObtained: (stepCount: Map<Long, Int>) -> Unit) {
        //Log.d("Test","Inside getStepCountDataOverARange")
        //This statement prevents running the following code when the app is being used for the first day
        //Otherwise the app will crash
        if (endTime <= startTime) {
            onDataObtained(mutableMapOf())
            return
        }

        val stepCountMap = mutableMapOf<Long, Int>()

        val readRequest = DataReadRequest.Builder()
            .aggregate(TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener {
                for (bucket in it.buckets) {
                    val dataSets = bucket.dataSets
                    for (dataSet in dataSets) {
                        for (dataPoint in dataSet.dataPoints) {
                            Log.d("Datapoint", dataPoint.toString())
                            val stepCount = dataPoint.getValue(dataPoint.dataType.fields[0]).asInt()
                            val dpStartTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                            stepCountMap[DateTime(dpStartTime).withTimeAtStartOfDay().millis]= stepCount
                        }
                    }
                }
                onDataObtained(stepCountMap)
                Log.d("Step count map", stepCountMap.toString())
            }
    }

    fun getAggregateStepCountDataOverARange(startTime: Long, endTime: Long, onDataObtained: (stepCount: Int) -> Unit) {
        var aggregateStepCount = 0

        //This statement prevents running the following code when the app is being used for the first day
        //Otherwise the app will crash
        if (endTime <= startTime) {
            onDataObtained(aggregateStepCount)
            return
        }

        val readRequest = DataReadRequest.Builder()
            .aggregate(TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.HOURS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readData(readRequest)
            .addOnSuccessListener {
                for (bucket in it.buckets) {
                    val dataSets = bucket.dataSets
                    for (dataSet in dataSets) {
                        for (dataPoint in dataSet.dataPoints) {
                            Log.d("Datapoint", dataPoint.toString())
                            aggregateStepCount += dataPoint.getValue(dataPoint.dataType.fields[0]).asInt()
                        }
                    }
                }

                onDataObtained(aggregateStepCount)
                Log.d("Aggregate step count", aggregateStepCount.toString())
            }
    }
}
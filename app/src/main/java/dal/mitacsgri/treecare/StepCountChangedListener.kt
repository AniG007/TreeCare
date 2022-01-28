package dal.mitacsgri.treecare

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class StepCountChangedListener(private val sensorType: Int) : SensorEventListener, KoinComponent {

    private val sharedPrefsRepository: SharedPreferencesRepository by inject()

    private var lastStepCount = 0
    private var stepCountDelta = 0
    private var isFirstCall = true

    init {
        when(sensorType) {
            Sensor.TYPE_STEP_DETECTOR -> Log.d("Step Sensor", "Step detector")
            Sensor.TYPE_STEP_COUNTER -> Log.d("Step Sensor", "Step counter")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) updateDailyStepCount(event.values[0].toInt())
    }

    private fun updateDailyStepCount(sensorValue: Int) {
        if (sensorType == Sensor.TYPE_STEP_DETECTOR) {
            stepCountDelta = sensorValue
            sharedPrefsRepository.storeDailyStepCount(
                sharedPrefsRepository.getDailyStepCount() + stepCountDelta)

        } else if (sensorType == Sensor.TYPE_STEP_COUNTER) {
            if (isFirstCall) {
                isFirstCall = false
                lastStepCount = sensorValue
            }
            sharedPrefsRepository.storeDailyStepCount(sharedPrefsRepository.getDailyStepCount()
                    + sensorValue - lastStepCount)
            lastStepCount = sensorValue
        }
    }
}
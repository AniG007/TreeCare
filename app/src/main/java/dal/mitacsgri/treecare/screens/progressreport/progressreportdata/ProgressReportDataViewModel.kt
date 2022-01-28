package dal.mitacsgri.treecare.screens.progressreport.progressreportdata

import android.content.Context
import android.graphics.Color
import android.text.SpannedString
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.getMapFormattedDate
import dal.mitacsgri.treecare.extensions.i
import dal.mitacsgri.treecare.model.ProgressReportDataItem
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import dal.mitacsgri.treecare.screens.progressreport.progressreportdata.ProgressReportDataFragment.Companion.MONTH_DATA
import dal.mitacsgri.treecare.screens.progressreport.progressreportdata.ProgressReportDataFragment.Companion.WEEK_DATA
import dal.mitacsgri.treecare.screens.progressreport.progressreportdata.bargraph.XAxisDataLabelFormatter
import getStartOfWeek
import org.jetbrains.anko.append
import org.jetbrains.anko.foregroundColor
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants.*
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.text.NumberFormat
import java.util.*

class ProgressReportDataViewModel(
    private val stepCountRepository: StepCountRepository,
    private val sharedPreferencesRepository: SharedPreferencesRepository
): ViewModel() {

    private var aggregateStepCount = 0
    private val progressReportDataList = arrayListOf<ProgressReportDataItem>()

    fun getProgressReportDurationText(progressReportType: Long, startTime: DateTime): String {
        return when(progressReportType) {
            WEEK_DATA -> {
                val weekStartDate = DateTime(getStartOfWeek(startTime.millis))
                val weekEndDate = weekStartDate.plusDays(6)
                val formatter = DateTimeFormat.forPattern("MMMM d")

                formatter.print(weekStartDate) + " - " + formatter.print(weekEndDate)
            }

            MONTH_DATA -> {
                val formatter = DateTimeFormat.forPattern("MMMM")
                formatter.print(startTime)
            }

            else -> ""
        }
    }

    fun getStepsDataForWeek(weekStartDate: DateTime): MutableLiveData<BarData> {

        val barLiveData =  MutableLiveData<BarData>()

        val entries = arrayListOf<BarEntry>()
        for (i in 0..6) {
            entries.add(BarEntry(i.toFloat(), 0f))
        }

        val weekStartDateMillis = weekStartDate.millis
        val weekEndDateMillis = weekStartDate.plusDays(7).millis

        val endDate = if (weekEndDateMillis < DateTime().millis) weekEndDateMillis else DateTime().millis

        Log.d("DateStartWeek"," $weekStartDateMillis")
        Log.d("DateCurrentWeek"," $endDate")

        stepCountRepository.getStepCountDataOverARange(
            weekStartDateMillis, endDate) {

            it.forEach { (date, steps) ->
                val dayOfWeek = DateTime(date).dayOfWeek
                entries[dayOfWeek-1] = BarEntry(dayOfWeek-1.toFloat(), steps.toFloat())

                aggregateStepCount += steps
                progressReportDataList.add(getProgressReportDataItem(date, steps))
            }
            val set = BarDataSet(entries, "Weekly Step Count")
            barLiveData.value = createBarData(set, WEEK_DATA)
        }

        return barLiveData
    }

    fun getStepsDataForMonth(monthStartDate: DateTime): MutableLiveData<BarData> {

        val barLiveData = MutableLiveData<BarData>()
        val entries = arrayListOf<BarEntry>()
        addBarEntriesBasedOnDaysInMonth(entries)

        val monthStartDateMillis = monthStartDate.millis
        val monthEndDateMillis = monthStartDate.plusMonths(1).withTimeAtStartOfDay().millis

        val endDateMillis =
            if (monthEndDateMillis < DateTime().millis) {
                monthEndDateMillis
            } else {
                DateTime().millis
            }
        Log.d("StartDate", "Start $monthStartDateMillis End $endDateMillis")
        stepCountRepository.getStepCountDataOverARange(
            monthStartDateMillis, endDateMillis) {map ->
            val keys = map.keys.sorted()

            //var i = 0
            keys.forEach {
                val i = DateTime(it).toDateTime().dayOfMonth
                entries.add(BarEntry((i).toFloat(), map[it]?.toFloat() ?: 0f))
                aggregateStepCount += map[it] ?: 0
                progressReportDataList.add(getProgressReportDataItem(it, map[it] ?: 0))
            }
            val set = BarDataSet(entries, "Daily Step Count")
            barLiveData.value = createBarData(set, MONTH_DATA)
        }
        return barLiveData
    }

    //Must be called from inside the observer for BarData live data
    fun getAggregateStepCount(): String =
        NumberFormat.getNumberInstance(Locale.getDefault()).format(aggregateStepCount) + " steps"

    //Must be called from inside the observer for BarData live data
    fun getProgressReportDataList(): ArrayList<ProgressReportDataItem> = progressReportDataList

    fun getFormattedDateText(date: Long): String {
        val formatter = DateTimeFormat.forPattern("EEEE, MMMM d")
        return formatter.print(date)
    }

    fun getStepsCountText(stepCount: Int) =
        NumberFormat.getNumberInstance(Locale.getDefault()).format(stepCount) + " steps"

    fun getLeavesGainedAndLostText(data: ProgressReportDataItem, context: Context): SpannedString {
        val stepCount = data.steps
        val goal = data.goal
        val leavesGained = stepCount / 1000
        val leavesLost = if (stepCount < goal) (goal - (stepCount/1000)*1000) / 1000 else 0

        return buildSpannedString {
            bold {
                append("+", foregroundColor(ContextCompat.getColor(context, R.color.colorPrimary)))
            }
            append(" $leavesGained\t\t\t")

            if (DateTime(data.date).withTimeAtStartOfDay().millis == DateTime().withTimeAtStartOfDay().millis) {
                append("")
            } else {
                bold {
                    append("-", foregroundColor(Color.RED))
                }
                append(" $leavesLost")
            }
        }
    }

    private fun createBarData(set: BarDataSet, progressReportType: Long): BarData {
        set.setGradientColor(0xff53c710.i, 0xFF6CFF13.i)
        val data = BarData(set)
        data.barWidth = 0.4f
        if (progressReportType == MONTH_DATA)
            data.setValueFormatter(XAxisDataLabelFormatter())
        return data
    }

    private fun addBarEntriesBasedOnDaysInMonth(entries: ArrayList<BarEntry>) {
        when(LocalDate.now().monthOfYear) {
            FEBRUARY -> {
                if (DateTime().withYear(DateTime().year).year().isLeap) {
                    for (i in 0..29) {
                        entries.add(BarEntry(i.toFloat(), 0f))
                    }
                } else {
                    for (i in 0..28) {
                        entries.add(BarEntry(i.toFloat(), 0f))
                    }
                }
            }
            JANUARY, MARCH, MAY, JULY, AUGUST, OCTOBER, DECEMBER ->
                for (i in 0..31) {
                    entries.add(BarEntry(i.toFloat(), 0f))
                }
            else -> for (i in 0..30) {
                    entries.add(BarEntry(i.toFloat(), 0f))
                }
        }
    }

    private fun getProgressReportDataItem(date: Long, stepCount: Int)
            : ProgressReportDataItem {
        return ProgressReportDataItem(date, stepCount,
            sharedPreferencesRepository.user.dailyGoalMap[DateTime(date).getMapFormattedDate()] ?: 5000)
    }
}
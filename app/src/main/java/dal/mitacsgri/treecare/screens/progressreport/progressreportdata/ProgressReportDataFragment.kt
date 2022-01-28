package dal.mitacsgri.treecare.screens.progressreport.progressreportdata

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.screens.progressreport.ProgressReportHolderFragment
import dal.mitacsgri.treecare.screens.progressreport.progressreportdata.bargraph.XAxisWeekDataFormatter
import kotlinx.android.synthetic.main.fragment_progress_report_data.*
import kotlinx.android.synthetic.main.fragment_progress_report_data.view.*
import org.joda.time.DateTime
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProgressReportDataFragment : Fragment() {

    private val mViewModel: ProgressReportDataViewModel by viewModel()
    private var reportType: Long = 1
    private var startDateMillis: Long = DateTime().millis

    companion object {
        const val REPORT_TYPE = "report_type"
        const val START_DATE = "start_date"
        const val WEEK_DATA = 0L
        const val MONTH_DATA = 1L

        fun newInstance(dataType: Long, startDate: DateTime): ProgressReportDataFragment {
            val fragment = ProgressReportDataFragment()
            val args = Bundle()
            args.putLong(REPORT_TYPE, dataType)
            args.putLong(START_DATE, startDate.millis)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        reportType = arguments?.getLong(REPORT_TYPE)!!
        startDateMillis = arguments?.getLong(START_DATE)!!
        val startDate = DateTime(startDateMillis)
        val view = inflater.inflate(R.layout.fragment_progress_report_data, container, false)
        view.apply {

            Log.d("Date", "RepType $reportType")

            val barChartLiveData =
                when(reportType) {
                    WEEK_DATA -> mViewModel.getStepsDataForWeek(startDate)
                    MONTH_DATA -> mViewModel.getStepsDataForMonth(startDate)
                    else -> MutableLiveData()
                }

            barChartLiveData.observe(viewLifecycleOwner, {
                updateBarChart(barChart, it)
                totalStepCountTV.text = mViewModel.getAggregateStepCount()
                recyclerView.adapter = ProgressReportRecyclerViewAdapter(
                    mViewModel.getProgressReportDataList(), mViewModel)
                nestedScrollView.smoothScrollTo(nestedScrollView.x.toInt(), 0)
            })

            progressReportDurationTV.text = mViewModel.getProgressReportDurationText(reportType, startDate)

            if (parentFragment is ProgressReportHolderFragment) {
                val parent = parentFragment as ProgressReportHolderFragment

                if (nestedScrollView.scrollX == 0) {
                    parent.hidePreviousDataButton(false)
                    parent.hideNextDataButton(false)
                }

                nestedScrollView.setOnScrollChangeListener {
                        _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->

                    val hideButton = scrollY != 0
                    parent.hidePreviousDataButton(hideButton)
                    parent.hideNextDataButton(hideButton)
                }
            }
        }
        return view
    }

    fun reanimateBarChart(durationMillis: Int) {
        if (view != null) {
            barChart.animateY(durationMillis)
        }
    }

    private fun updateBarChart(barChart: BarChart, barData: BarData) {
        barChart.apply {
            data = barData
            setFitBars(true)

            val newDescription = Description()
            newDescription.text = ""
            description = newDescription

            setTouchEnabled(false)

            xAxis.apply {
                if (reportType == WEEK_DATA) valueFormatter =
                    XAxisWeekDataFormatter()
                position = XAxis.XAxisPosition.BOTTOM
                setDrawAxisLine(false)
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            legend.isEnabled = false

            axisLeft.apply {
                setDrawAxisLine(false)
                axisLeft.axisMinimum = 0f
            }
            animateY(1000)
        }
    }


}

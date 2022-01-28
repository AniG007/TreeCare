package dal.mitacsgri.treecare.screens.progressreport.progressreportdata.bargraph

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

class XAxisDataLabelFormatter: ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String = ""

    override fun getBarLabel(barEntry: BarEntry?): String = ""
}
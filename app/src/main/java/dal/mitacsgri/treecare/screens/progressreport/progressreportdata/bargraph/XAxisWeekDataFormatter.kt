package dal.mitacsgri.treecare.screens.progressreport.progressreportdata.bargraph

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

class XAxisWeekDataFormatter: ValueFormatter() {

    override fun getFormattedValue(value: Float): String =
        when(value.toInt()) {
            0 -> "MON"
            1 -> "TUE"
            2 -> "WED"
            3 -> "THU"
            4 -> "FRI"
            5 -> "SAT"
            6 -> "SUN"
            else -> ""
        }

    override fun getBarLabel(barEntry: BarEntry?): String =
            barEntry?.y?.toInt().toString()
}
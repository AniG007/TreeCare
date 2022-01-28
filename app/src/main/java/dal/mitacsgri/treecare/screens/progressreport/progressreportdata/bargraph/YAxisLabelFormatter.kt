package dal.mitacsgri.treecare.screens.progressreport.progressreportdata.bargraph

import com.github.mikephil.charting.formatter.ValueFormatter

class YAxisLabelFormatter: ValueFormatter() {

    override fun getFormattedValue(value: Float): String = value.toInt().toString()
}
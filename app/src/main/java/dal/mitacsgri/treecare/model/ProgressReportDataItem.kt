package dal.mitacsgri.treecare.model

data class ProgressReportDataItem(
    val date: Long,
    val steps: Int,
    val goal: Int
)
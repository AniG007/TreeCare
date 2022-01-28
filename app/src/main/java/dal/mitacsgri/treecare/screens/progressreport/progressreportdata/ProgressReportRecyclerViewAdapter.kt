package dal.mitacsgri.treecare.screens.progressreport.progressreportdata

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.ProgressReportDataItem

class ProgressReportRecyclerViewAdapter(
    private val progressReportDataList: ArrayList<ProgressReportDataItem>,
    private val viewModel: ProgressReportDataViewModel
): RecyclerView.Adapter<ProgressReportDataViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProgressReportDataViewHolder =
        ProgressReportDataViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_progress_report_data, parent, false), viewModel)

    override fun getItemCount(): Int = progressReportDataList.size

    override fun onBindViewHolder(holder: ProgressReportDataViewHolder, position: Int) {
        holder.bind(progressReportDataList[position])
    }
}
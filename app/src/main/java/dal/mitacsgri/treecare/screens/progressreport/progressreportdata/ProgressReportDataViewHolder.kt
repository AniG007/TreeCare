package dal.mitacsgri.treecare.screens.progressreport.progressreportdata

import android.view.View
import dal.mitacsgri.treecare.model.ProgressReportDataItem
import dal.mitacsgri.treecare.screens.BaseViewHolder
import kotlinx.android.synthetic.main.item_progress_report_data.view.*

class ProgressReportDataViewHolder(
    itemView: View, private val viewModel: ProgressReportDataViewModel
    ): BaseViewHolder<ProgressReportDataItem>(itemView) {

    override fun bind(item: ProgressReportDataItem) {
        itemView.apply {
            dateTV.text = viewModel.getFormattedDateText(item.date)
            stepsCountTV.text = viewModel.getStepsCountText(item.steps)
            leafCountTV.text = viewModel.getLeavesGainedAndLostText(item, context)
        }
    }
}
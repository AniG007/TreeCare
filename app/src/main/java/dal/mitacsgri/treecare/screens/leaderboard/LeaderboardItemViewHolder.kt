package dal.mitacsgri.treecare.screens.leaderboard

import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Challenger
import dal.mitacsgri.treecare.screens.BaseViewHolder
import kotlinx.android.synthetic.main.item_leaderboard.view.*

class LeaderboardItemViewHolder(
    itemView: View,
    private val viewModel: LeaderboardItemViewModel
    ): BaseViewHolder<Challenger>(itemView) {

    private var challengerPosition = 0

    override fun bind(item: Challenger) {
        itemView.apply {
            nameTV.text = item.name
            rankTV.text = challengerPosition.toString()
            stepsCountTV.text = viewModel.getTotalStepsText(item)
            leafCountTV.text = viewModel.getLeafCountText(item)

            Glide.with(this).load(item.photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)

            if (viewModel.isCurrentUser(item)) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(context,
                    R.color.colorPrimaryLight))
            }
        }
    }

    fun bind(item: Challenger, position: Int) {
        challengerPosition = position + 1
        bind(item)
    }
}
package dal.mitacsgri.treecare.screens.teamranking

import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.TeamInfo
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.item_team_ranking.view.*


class TeamRankingViewHolder(
    itemView: View,
    private val viewModel: TeamRankingViewModel
): BaseViewHolder<TeamInfo>(itemView) {

    private var playerPosition = 0

    override fun bind(item: TeamInfo) {

        itemView.apply {
            nameTV.text = item.userName

            Glide.with(this).load(item.photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)

            if (viewModel.isCurrentUser(item)) {
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimaryLight
                    )
                )
            }

            stepsCountTV.text = item.stepsCount.toString()
            //leafCountTV.text = item.leavesCount.toString()
            rankTV.text = playerPosition.toString()

            if (item.captainId == item.uId) {
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.captain_primary_color
                    )
                )
            }

            cardView.setOnClickListener {
                MainActivity.playClickSound()
            }
        }
    }

    fun bind(item: TeamInfo, position: Int){
        playerPosition = position + 1
        bind(item)
    }
}
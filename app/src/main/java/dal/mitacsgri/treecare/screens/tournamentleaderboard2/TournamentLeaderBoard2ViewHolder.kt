package dal.mitacsgri.treecare.screens.tournamentleaderboard2

import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.di.sharedPreferencesRepositoryModule
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.TournamentLB2
import dal.mitacsgri.treecare.screens.BaseViewHolder
import kotlinx.android.synthetic.main.fragment_tournament_leaderboard_2.view.*
import kotlinx.android.synthetic.main.item_team_info.view.*
import kotlinx.android.synthetic.main.item_tournament_leaderboard.view.*
import kotlinx.android.synthetic.main.item_tournament_leaderboard2.view.*
import kotlinx.android.synthetic.main.item_tournament_leaderboard2.view.imageView

class TournamentLeaderBoard2ViewHolder(itemView: View, private val viewModel: TournamentLeaderBoard2ViewModel)
    :BaseViewHolder<TournamentLB2>(itemView) {
    override fun bind(item: TournamentLB2) {
        itemView.apply {
            teamNameTV.text = item.teamName
            captainNameTV.text = item.captainName
            Glide.with(this).load(item.photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)

            if (viewModel.isCurrentTeam(item.teamName)) {
                cardView2.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.colorPrimaryLight))
            }

        }
    }
}
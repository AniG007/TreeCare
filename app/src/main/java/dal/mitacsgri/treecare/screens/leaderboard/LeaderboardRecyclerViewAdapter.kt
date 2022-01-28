package dal.mitacsgri.treecare.screens.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Challenger

class LeaderboardRecyclerViewAdapter(
    private val playersList: List<Challenger>,
    private val viewModel: LeaderboardItemViewModel
    ): RecyclerView.Adapter<LeaderboardItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardItemViewHolder =
        LeaderboardItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false),
            viewModel)


    override fun getItemCount() = playersList.size

    override fun onBindViewHolder(holder: LeaderboardItemViewHolder, position: Int) {
        holder.bind(playersList[position], position)
    }
}
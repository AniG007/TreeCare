package dal.mitacsgri.treecare.screens.teamranking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.TeamInfo

class TeamRankingRecyclerViewAdapter(
    private val membersList : ArrayList<TeamInfo>,
    private val viewModel: TeamRankingViewModel
): RecyclerView.Adapter<TeamRankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamRankingViewHolder =
        TeamRankingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_team_ranking,parent, false),
            viewModel)

    override fun onBindViewHolder(holder: TeamRankingViewHolder, position: Int) {
        holder.bind(membersList[position], position)
    }

    override fun getItemCount() = membersList.size
}
package dal.mitacsgri.treecare.screens.tournamentleaderboard2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.TournamentLB2

class TournamentLeaderBoard2RecyclerViewAdapter(
    private val teams: List<TournamentLB2>,
    private val viewModel: TournamentLeaderBoard2ViewModel
): RecyclerView.Adapter<TournamentLeaderBoard2ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    )= TournamentLeaderBoard2ViewHolder((LayoutInflater.from(parent.context))
        .inflate(R.layout.item_tournament_leaderboard2, parent, false), viewModel)

    override fun getItemCount() = teams.size
    override fun onBindViewHolder(holder: TournamentLeaderBoard2ViewHolder, position: Int) {
        holder.bind(teams[position])
    }
}
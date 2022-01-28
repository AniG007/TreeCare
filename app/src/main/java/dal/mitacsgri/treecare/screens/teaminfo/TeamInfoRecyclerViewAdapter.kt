package dal.mitacsgri.treecare.screens.teaminfo

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.TeamInfo
import kotlin.time.ExperimentalTime

class TeamInfoRecyclerViewAdapter(
    private val membersList : ArrayList<TeamInfo>,
    private val viewModel: TeamInfoViewModel
): RecyclerView.Adapter<TeamInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamInfoViewHolder =
        TeamInfoViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_team_info,parent, false),
        viewModel)

    override fun getItemCount() = membersList.size

    override fun onBindViewHolder(holder: TeamInfoViewHolder, position: Int) {
        holder.bind(membersList[position], position)
    }

}
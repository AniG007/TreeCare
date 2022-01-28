package dal.mitacsgri.treecare.screens.teams.allteams

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.screens.teams.allteams.AllTeamsViewModel
import java.util.*
import kotlin.collections.ArrayList

class AllTeamsRecyclerViewAdapter(
    private val teamsList: ArrayList<Team>,
    private val viewModel: AllTeamsViewModel
): RecyclerView.Adapter<AllTeamsItemViewHolder>(), Filterable {

    var teamsFilterList = ArrayList<Team>()

    init {
        teamsFilterList = teamsList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AllTeamsItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_all_teams_team, parent, false), viewModel
        )

    override fun getItemCount() = teamsFilterList.size

    override fun onBindViewHolder(holder: AllTeamsItemViewHolder, position: Int) {
        holder.bind(teamsFilterList[position])
    }

    //For Search functionality
    override fun getFilter(): Filter {
        Log.d("Test", "Inside getFilter")
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    Log.d("Test", "char is empty")
                    teamsFilterList = teamsList
                } else {
                    Log.d("Test", "something typed")
                    val resultList = ArrayList<Team>()
                    for (row in teamsList) {
                        if (row.name.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    teamsFilterList = resultList

                    Log.d("Test", teamsFilterList.toString())
                }
                val filterResults = FilterResults()
                filterResults.values = teamsFilterList
                Log.d("Filter", filterResults.values.toString())
                return filterResults
            }


            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                teamsFilterList = results?.values as ArrayList<Team>
                Log.d("Publish", teamsFilterList.toString())
                notifyDataSetChanged()
            }
        }
    }
}
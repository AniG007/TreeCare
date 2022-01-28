package dal.mitacsgri.treecare.screens.enrollteams

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Team
import java.util.*
import kotlin.collections.ArrayList

class EnrollTeamsRecyclerViewAdapter(
    private val membersList : ArrayList<Team>,
    private val viewModel: EnrollTeamsViewModel
): RecyclerView.Adapter<EnrollTeamsViewHolder>(), Filterable {

    var membersFilterList = ArrayList<Team>()

    init{
        membersFilterList = membersList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrollTeamsViewHolder =
        EnrollTeamsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_enroll_teams, parent, false), viewModel)

    override fun getItemCount() = membersFilterList.size

    override fun onBindViewHolder(holder: EnrollTeamsViewHolder, position: Int) {
        holder.bind(membersFilterList[position])
    }

    //For Search functionality
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    Log.d("Test","char is empty")
                    membersFilterList = membersList
                } else {
                    Log.d("Test","something typed")
                    val resultList = ArrayList<Team>()
                    for (row in membersList) {
                        if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    membersFilterList = resultList

                    Log.d("Test",membersFilterList.toString())
                }
                val filterResults = FilterResults()
                filterResults.values = membersFilterList
                Log.d("Filter",filterResults.values.toString())
                return filterResults
            }


            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                membersFilterList = results?.values as ArrayList<Team>
                Log.d("Publish",membersFilterList.toString())
                notifyDataSetChanged()
            }
        }
    }
}
package dal.mitacsgri.treecare.screens.tournaments.activetournaments

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Tournament
import dal.mitacsgri.treecare.screens.tournaments.TournamentsViewModel
import java.util.*
import kotlin.collections.ArrayList

class ActiveTournamentsRecyclerViewAdapter(
    private val tournamentsList : ArrayList<Tournament>,
    private val viewModel: TournamentsViewModel
): RecyclerView.Adapter<ActiveTournamentsViewHolder>(), Filterable {

    var tournamentsFilterList = ArrayList<Tournament>()

    init{
        tournamentsFilterList = tournamentsList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ActiveTournamentsViewHolder(
            viewModel,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_active_tournament, parent, false)
        )

            //LayoutInflater.from(parent.context).inflate(R.layout.item_current_tournament, parent, false))

    override fun getItemCount() = tournamentsFilterList.size

    override fun onBindViewHolder(holder: ActiveTournamentsViewHolder, position: Int) {
        holder.bind(tournamentsFilterList[position])
    }

    //For Search functionality
    override fun getFilter(): Filter {
        Log.d("Test", "Inside getFilter")
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    Log.d("Test","char is empty")
                    tournamentsFilterList = tournamentsList
                } else {
                    Log.d("Test","something typed")
                    val resultList = ArrayList<Tournament>()
                    for (row in tournamentsList) {
                        if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    tournamentsFilterList = resultList

                    Log.d("Test",tournamentsFilterList.toString())
                }
                val filterResults = FilterResults()
                filterResults.values = tournamentsFilterList
                Log.d("Filter",filterResults.values.toString())
                return filterResults
            }


            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                tournamentsFilterList = results?.values as ArrayList<Tournament>
                Log.d("Publish",tournamentsFilterList.toString())
                notifyDataSetChanged()
            }
        }
    }
}
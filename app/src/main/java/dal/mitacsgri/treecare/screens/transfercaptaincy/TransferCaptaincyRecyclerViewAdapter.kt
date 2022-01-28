package dal.mitacsgri.treecare.screens.transfercaptaincy

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.User
import java.util.*
import kotlin.collections.ArrayList

class TransferCaptaincyRecyclerViewAdapter(
    private val usersList: ArrayList<User>,
    private val viewModel: TransferCaptaincyViewModel
):RecyclerView.Adapter<TransferCaptaincyViewHolder>(), Filterable {

    var usersFilterList = ArrayList<User>()
    //var lastSelectedPosition = -1

    init{
        usersFilterList = usersList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferCaptaincyViewHolder =
        TransferCaptaincyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_transfer_captaincy, parent, false), viewModel)

    override fun getItemCount() = usersFilterList.size

    override fun onBindViewHolder(holder: TransferCaptaincyViewHolder, position: Int) {
        holder.bind(usersFilterList[position])
    }

//        holder.itemView.radio_user.setChecked(lastSelectedPosition == position)
//
//        holder.itemView.radio_user.setOnClickListener {
//            lastSelectedPosition = holder.adapterPosition
//            notifyDataSetChanged()
//        }

//        holder.itemView.radio_user.setOnClickListener {
//            val copy = lastSelectedPosition
//            lastSelectedPosition = holder.adapterPosition
//            if(copy != lastSelectedPosition) {
//                notifyItemChanged(copy)
//                notifyItemChanged(lastSelectedPosition)
//            }
//        }
    //For Search functionality
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    Log.d("Test","char is empty")
                    usersFilterList = usersList
                } else {
                    Log.d("Test","something typed")
                    val resultList = ArrayList<User>()
                    for (row in usersList) {
                        if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    usersFilterList = resultList

                    Log.d("Test",usersFilterList.toString())
                }
                val filterResults = FilterResults()
                filterResults.values = usersFilterList
                Log.d("Filter",filterResults.values.toString())
                return filterResults
            }


            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                usersFilterList = results?.values as ArrayList<User>
                Log.d("Publish",usersFilterList.toString())
                notifyDataSetChanged()
            }
        }
    }
}
package dal.mitacsgri.treecare.screens.invitesrequests.request

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.InvitesRequest
import dal.mitacsgri.treecare.screens.invitesrequests.InvitesRequestViewModel

class RequestRecyclerViewAdapter(
    private val requests: List<InvitesRequest>,
    /*private val requests: List<InvitesRequest>?,*/
    private val viewModel: InvitesRequestViewModel
): RecyclerView.Adapter<RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RequestViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_requests, parent, false),
            viewModel
        )
    override fun getItemCount() = requests.size
    override fun onBindViewHolder(holder: RequestViewHolder, position: Int){
        holder.bind(requests[position])
        /*holder.bind(requests[position])*/
    }

}
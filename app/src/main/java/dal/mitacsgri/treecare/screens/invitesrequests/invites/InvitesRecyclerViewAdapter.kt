package dal.mitacsgri.treecare.screens.invitesrequests.invites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.InvitesRequest
import dal.mitacsgri.treecare.screens.invitesrequests.InvitesRequestViewModel

class InvitesRecyclerViewAdapter(
    private val invites: List<InvitesRequest>,
    private val viewModel: InvitesRequestViewModel
) :RecyclerView.Adapter<InvitesViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InvitesViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invites, parent, false),
            viewModel
        )
    override fun getItemCount() = invites.size
    override fun onBindViewHolder(holder: InvitesViewHolder, position: Int){
        holder.bind(invites[position])
        /*holder.bind(requests[position])*/
    }

}
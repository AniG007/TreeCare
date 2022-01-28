package dal.mitacsgri.treecare.screens.invitesrequests.invites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.screens.invitesrequests.InvitesRequestViewModel
import kotlinx.android.synthetic.main.fragment_invites.*
import kotlinx.android.synthetic.main.fragment_teams.view.*
import kotlinx.android.synthetic.main.item_invites.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Observer

class InvitesFragment : Fragment() {

    private val viewModel: InvitesRequestViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_invites, container, false)

        view.apply {
            /*tabLayout.setupWithViewPager(viewPager)
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }*/

            viewModel.getAllInvites().observe(this@InvitesFragment, androidx.lifecycle.Observer{
                recyclerView.adapter = InvitesRecyclerViewAdapter(it, viewModel)
            })

            viewModel.messageLiveData.observe(this@InvitesFragment, androidx.lifecycle.Observer {
                it.toast(context)
            })

            viewModel.messageLiveData3.observe(this@InvitesFragment, androidx.lifecycle.Observer {
                it.toast(context)
            })


        }
        return view
    }
}
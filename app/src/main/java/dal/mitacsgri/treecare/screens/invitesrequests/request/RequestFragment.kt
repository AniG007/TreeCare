package dal.mitacsgri.treecare.screens.invitesrequests.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.screens.invitesrequests.InvitesRequestViewModel
//import kotlinx.android.synthetic.main.fragment_active_challenges.view.*
import kotlinx.android.synthetic.main.fragment_request.*
import kotlinx.android.synthetic.main.fragment_invites_request.view.tabLayout
import kotlinx.android.synthetic.main.fragment_invites_request.view.toolbar
import kotlinx.android.synthetic.main.fragment_invites_request.view.viewPager
import org.koin.androidx.viewmodel.ext.android.viewModel

class RequestFragment : Fragment() {

    private val viewModel: InvitesRequestViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //val view = inflater.inflate(R.layout.fragment_request, container, false)
        val view = inflater.createFragmentViewWithStyle(activity, R.layout.fragment_request,R.style.tournament_mode_theme)
        view.apply {
            /*viewPager.adapter = TeamsPagerAdapter(childFragmentManager)*/

            /*tabLayout.setupWithViewPager(viewPager)*/
            /*recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                adapter =
                    RequestRecyclerViewAdapter(
                        //viewModel.invitesList.value!!,
                        viewModel.requestList.value!!, viewModel
                    )
            }*/

            /*toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }*/

            viewModel.getAllRequests().observe(this@RequestFragment, Observer {
                recyclerView.adapter = RequestRecyclerViewAdapter(it, viewModel)
            })

            viewModel.messageLiveData2.observe(this@RequestFragment, Observer {
                it.toast(context)
            })

            viewModel.messageLiveData4.observe(this@RequestFragment, Observer {
                it.toast(context)
            })
            /*viewModel.requestList.observe(this@RequestFragment, androidx.lifecycle.Observer {
                view.recyclerView.adapter =
                    RequestRecyclerViewAdapter(
                        it,
                        viewModel
                    )
            })*/
         /*   viewModel.requestList.observe(this@InvitesRequestFragment, Observer {
                view.recyclerView.adapter = InvitesRequestRecyclerViewAdapter(it,viewModel)
            })*/

            /*viewModel.getAllRequests()*/
        }
        return view
    }
}
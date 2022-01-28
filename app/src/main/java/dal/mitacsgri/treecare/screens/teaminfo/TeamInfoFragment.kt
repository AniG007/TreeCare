package dal.mitacsgri.treecare.screens.teaminfo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
/*import android.widget.LinearLayout*/
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.di.sharedPreferencesRepositoryModule
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.screens.teaminfo.TeamInfoViewModel
import kotlinx.android.synthetic.main.fragment_team_info.view.*
import kotlinx.android.synthetic.main.fragment_teams.view.*
import kotlinx.android.synthetic.main.fragment_teams.view.title
import kotlinx.android.synthetic.main.fragment_teams.view.toolbar
import kotlinx.android.synthetic.main.item_team_info.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

//import kotlinx.android.synthetic.main.fragment_invites_request.view.*

class TeamInfoFragment : Fragment() {

    //val teamName= MutableLiveData<String>()
    private val mViewModel: TeamInfoViewModel by viewModel()
    val args: TeamInfoFragmentArgs by navArgs()
    private val sharedPrefs: SharedPreferencesRepository by inject()

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        teamName.value = args.teamName
    }*/
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.createFragmentViewWithStyle(
            activity, R.layout.fragment_team_info, R.style.tournament_mode_theme
        )

        view.apply {

            transferCaptaincy.visibility = View.INVISIBLE

            title.text = args.teamName

            mViewModel.membersList.value?.clear() //To avoid duplicates

            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            mViewModel.getTeamMembers(args.teamName).observe(viewLifecycleOwner, Observer {

                recyclerView.apply {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = TeamInfoRecyclerViewAdapter(it, mViewModel)
                }
            })

            if (args.sourceScreen == "enrollTeamsFragment")
                transferCaptaincy.visibility = View.INVISIBLE

            else if (args.sourceScreen == "teamsFragment") {

                if (sharedPrefs.user.uid == sharedPrefs.team.captain)
                    transferCaptaincy.visibility = View.VISIBLE
                else transferCaptaincy.visibility = View.INVISIBLE

            }
            else{
                transferCaptaincy.visibility = View.INVISIBLE
            }

//            mViewModel.bool.observe(viewLifecycleOwner, Observer {
//                if(it) MutableLiveData<String>()
//                else View.INVISIBLE
//            })


            transferCaptaincy.setOnClickListener {
                val action =
                    TeamInfoFragmentDirections.actionTeamInfoFragmentToTransferCaptaincy2(args.teamName)
                findNavController().navigate(action)
            }
        }
        return view

    }
}
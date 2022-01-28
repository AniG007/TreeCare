package dal.mitacsgri.treecare.screens.teams.yourteams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.screens.teams.TeamsFragmentDirections
import dal.mitacsgri.treecare.screens.tournaments.activetournaments.ActiveTournamentsRecyclerViewAdapter
import dal.mitacsgri.treecare.screens.tournaments.mytournaments.MyTournamentsRecyclerViewAdapter
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_active_tournaments.view.*
import kotlinx.android.synthetic.main.fragment_my_tournaments.*
import kotlinx.android.synthetic.main.fragment_your_teams.view.*
import kotlinx.android.synthetic.main.fragment_your_teams.view.empty_view
import kotlinx.android.synthetic.main.fragment_your_teams.view.recyclerView
import kotlinx.android.synthetic.main.item_your_captained_team.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.w3c.dom.Text

class YourTeamsFragment : Fragment() {

    private val mViewModel: YourTeamsViewModel by viewModel()

    lateinit var adapter: YourTeamsRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.createFragmentViewWithStyle(activity,
            R.layout.fragment_your_teams, R.style.tournament_mode_theme)
        view.apply {
            mViewModel.getAllMyTeams().observe(viewLifecycleOwner, Observer {
                recyclerView.adapter = YourTeamsRecyclerViewAdapter(it,mViewModel)

                adapter = recyclerView.adapter as YourTeamsRecyclerViewAdapter

                if(adapter.itemCount == 0){
                    empty_view.visibility = View.VISIBLE
                }
                else{
                    empty_view.visibility = View.INVISIBLE
                }
            })
            //mViewModel.delBtnVis()

            mViewModel.status.observe(viewLifecycleOwner, Observer {
                if(!mViewModel.messageDisplayed)
                    it.toast(context)
                mViewModel.messageDisplayed = true
            })
            /*mViewModel.status.observe(this@YourTeamsFragment, Observer {
                if(it) buttonDelete.visibility = View.VISIBLE
                else buttonDelete.visibility = View.INVISIBLE
            })*/
        }

        return view
    }


}

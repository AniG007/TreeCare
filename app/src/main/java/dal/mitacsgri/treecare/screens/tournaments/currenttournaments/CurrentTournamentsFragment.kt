package dal.mitacsgri.treecare.screens.tournaments.currenttournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.model.Tournament
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.enrollteams.EnrollTeamsRecyclerViewAdapter
import dal.mitacsgri.treecare.screens.tournaments.TournamentsViewModel
import kotlinx.android.synthetic.main.fragment_current_tournaments.*
import kotlinx.android.synthetic.main.fragment_current_tournaments.empty_view
import kotlinx.android.synthetic.main.fragment_current_tournaments.view.*
import kotlinx.android.synthetic.main.fragment_enroll_teams.view.*
import kotlinx.android.synthetic.main.fragment_my_tournaments.*
import kotlinx.android.synthetic.main.fragment_tournaments.*
import kotlinx.android.synthetic.main.item_current_challenge.*
import kotlinx.android.synthetic.main.item_current_tournament.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CurrentTournamentsFragment : Fragment() {

    private val mViewModel: TournamentsViewModel by sharedViewModel()
    lateinit var adapter: CurrentTournamentsRecyclerViewAdapter
    lateinit var tournamemntsrv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.createFragmentViewWithStyle(
            activity, R.layout.fragment_current_tournaments, R.style.tournament_mode_theme)

        //val view = inflater.inflate(R.layout.fragment_current_tournaments, container, false)


        //This clear needs to be done here otherwise whenever this fragment is created as a result of coming
        //back from a fragment up in the navigation stack, the elements are added to the list, as a result of which,
        //the elements are duplicated
        mViewModel.currentTournamentsList.value?.clear()

//        view.recyclerView.apply {
//            setHasFixedSize(true)
//            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//            adapter = CurrentTournamentsRecyclerViewAdapter(mViewModel.currentTournamentsList.value!!, mViewModel)
//        }


//        mViewModel.currentTournamentsList.observe(viewLifecycleOwner, Observer {
//            //view.recyclerView.adapter?.notifyDataSetChanged()
//            view.recyclerView.adapter = CurrentTournamentsRecyclerViewAdapter(it, mViewModel)
//        })

        if(!mViewModel.messageDisplayed) {

            mViewModel.statusMessage.observe(viewLifecycleOwner, Observer {
                it.toast(requireContext())
                mViewModel.messageDisplayed = true
                //findNavController().navigateUp()
            })
        }

        mViewModel.getCurrentTournamentsForUser().observe(viewLifecycleOwner, Observer {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL, false)
                adapter = CurrentTournamentsRecyclerViewAdapter(it, mViewModel)
                //membersrv.adapter = adapter
            }
            adapter = recyclerView.adapter as CurrentTournamentsRecyclerViewAdapter

            if(adapter.itemCount == 0){
                empty_view.visibility = View.VISIBLE
            }
            else{
                empty_view.visibility = View.INVISIBLE
            }
        })
        tournamemntsrv = view.findViewById(R.id.recyclerView)

        //mViewModel.display()
        /*tournament_search1.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })*/
        return view
    }

//    private fun setUpRecyclerView(recyclerView: RecyclerView, tournamentsList: List<Tournament>) {
//        recyclerView.apply {
//            setHasFixedSize(true)
//            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//            adapter = CurrentTournamentsRecyclerViewAdapter(tournamentsList, mViewModel)
//        }
//    }
}

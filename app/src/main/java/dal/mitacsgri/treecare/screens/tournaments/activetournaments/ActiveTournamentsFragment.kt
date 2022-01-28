package dal.mitacsgri.treecare.screens.tournaments.activetournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.screens.tournaments.TournamentsViewModel
import kotlinx.android.synthetic.main.fragment_active_tournaments.view.*
import kotlinx.android.synthetic.main.fragment_my_tournaments.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ActiveTournamentsFragment : Fragment() {

    private val mViewModel: TournamentsViewModel by viewModel()

    lateinit var adapter: ActiveTournamentsRecyclerViewAdapter
    lateinit var tournamentsrv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.createFragmentViewWithStyle(
            activity, R.layout.fragment_active_tournaments, R.style.tournament_mode_theme)

        view.apply {
//            recyclerView.apply {
//                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//                adapter = ActiveTournamentsRecyclerViewAdapter(mViewModel.activeTournamentsList.value!!, mViewModel)
//            }
            mViewModel.getAllActiveTournaments().observe(viewLifecycleOwner, {
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL, false)
                    adapter = ActiveTournamentsRecyclerViewAdapter(it, mViewModel)
                }
                adapter = recyclerView.adapter as ActiveTournamentsRecyclerViewAdapter

                if(adapter.itemCount == 0){
                    empty_view.visibility = View.VISIBLE
                }
                else{
                    empty_view.visibility = View.INVISIBLE
                }
            })
            tournamentsrv = findViewById(R.id.recyclerView)

//            mViewModel.activeTournamentsList.observe(viewLifecycleOwner, Observer {
//                recyclerView.adapter = ActiveTournamentsRecyclerViewAdapter(it, mViewModel)
//                adapter = recyclerView.adapter as ActiveTournamentsRecyclerViewAdapter
//            })

//            mViewModel.totalList.observe(viewLifecycleOwner, Observer {
//                mViewModel.addSteps(it)
//            })

            tournament_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })
            if(!mViewModel.messageDisplayed2) {
                mViewModel.MessageStatus.observe(viewLifecycleOwner, Observer {

                    it.toast(requireContext())
                    mViewModel.messageDisplayed2 = true
                    //findNavController().navigateUp()
                })
            }
        }
        //mViewModel.getAllActiveTournaments()
        return view
    }
}

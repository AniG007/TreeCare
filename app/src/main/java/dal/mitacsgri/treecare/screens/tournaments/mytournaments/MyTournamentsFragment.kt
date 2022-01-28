package dal.mitacsgri.treecare.screens.tournaments.mytournaments

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
import kotlinx.android.synthetic.main.fragment_my_tournaments.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MyTournamentsFragment: Fragment() {

    private val mViewModel: TournamentsViewModel by sharedViewModel()
    lateinit var adapter: MyTournamentsRecyclerViewAdapter
    lateinit var tournamemntsrv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.createFragmentViewWithStyle(
            activity, R.layout.fragment_my_tournaments, R.style.tournament_mode_theme)

        //This clear needs to be done here otherwise whenever this fragment is created as a result of coming
        //back from a fragment up in the navigation stack, the elements are added to the list, as a result of which,
        //the elements are duplicated
        mViewModel.myTournamentsList.value?.clear()

        if(!mViewModel.messageDisplayed) {

            mViewModel.statusMessage.observe(viewLifecycleOwner, Observer {
                it.toast(requireContext())
                mViewModel.messageDisplayed = true
                //findNavController().navigateUp()
            })
        }

        mViewModel.getUserTournaments().observe(viewLifecycleOwner, {
            recyclerView1.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                adapter = MyTournamentsRecyclerViewAdapter(it, mViewModel)
            }
            adapter = recyclerView1.adapter as MyTournamentsRecyclerViewAdapter

            if(adapter.itemCount == 0){
                empty_view.visibility = View.VISIBLE
            }
            else{
                empty_view.visibility = View.INVISIBLE
            }
        })
        tournamemntsrv = view.findViewById(R.id.recyclerView1)

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
}
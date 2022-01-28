package dal.mitacsgri.treecare.screens.teams.allteams


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import kotlinx.android.synthetic.main.fragment_all_teams.*
import kotlinx.android.synthetic.main.fragment_all_teams.empty_view
import kotlinx.android.synthetic.main.fragment_my_tournaments.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AllTeamsFragment : Fragment() {

    private val mViewModel: AllTeamsViewModel by viewModel()
   // public var teamName = nameTV.text

    lateinit var adapter: AllTeamsRecyclerViewAdapter
    lateinit var teamssrv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.createFragmentViewWithStyle(activity,
            R.layout.fragment_all_teams, R.style.tournament_mode_theme)


        mViewModel.getAllTeams().observe(viewLifecycleOwner, {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                adapter = AllTeamsRecyclerViewAdapter(it, mViewModel)
            }
            adapter = recyclerView.adapter as AllTeamsRecyclerViewAdapter

            if(adapter.itemCount == 0){
                empty_view.visibility = View.VISIBLE
            }
            else{
                empty_view.visibility = View.INVISIBLE
            }
        })

         //teamssrv = findViewById(R.id.recyclerView)


        /*team_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

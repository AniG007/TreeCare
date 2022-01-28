package dal.mitacsgri.treecare.screens.enrollteams

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.fragment_enroll_teams.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EnrollTeamsFragment : Fragment() {

    private val viewModel: EnrollTeamsViewModel by viewModel()

    val args: EnrollTeamsFragmentArgs by navArgs()
    lateinit var adapter: EnrollTeamsRecyclerViewAdapter
    lateinit var membersrv: RecyclerView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tournamentName = args.tournamentName
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.createFragmentViewWithStyle(
            activity,
            R.layout.fragment_enroll_teams,
            R.style.tournament_mode_theme
        )
        Log.d("Test", "TeamLimit " + args.teamsLimit)

        view.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
                //findNavController().navigate(R.id.action_enrollTeamsFragment_to_createTournamentFragment)
            }

            //viewModel.getExistingTeams(args.tournamentName)
            enrollButton.setOnClickListener {
                MainActivity.playClickSound()
                //viewModel.enrollTeams(args.tournamentName)

                val bool = viewModel.enrollTeams(args.teamsLimit)
                if (bool) {
                   // Log.d("Frag", "VM" + viewModel.teamsHolder.value)

//                    Log.d("Test","TeamLimit "+args.teamsLimit)
//                    Log.d("Test","endDate "+args.endDate)
//                    Log.d("Test", "startDate "+args.startDate)
//                    Log.d("Test","goal "+args.goal)
//                    Log.d("Test","tourDesc "+args.tournamentDescription)
//                    Log.d("Test","tourName "+args.tournamentName)


                    viewModel.MessageStatus.observe(viewLifecycleOwner, Observer {
                        it.toast(context, Toast.LENGTH_SHORT)
//                        val action = viewModel.teamsHolder.value?.toTypedArray()?.let { it1 ->
//                            EnrollTeamsFragmentDirections.actionEnrollTeamsFragmentToCreateTournamentFragment(
//                                true,
//                                args.tournamentName,
//                                args.tournamentDescription,
//                                args.goal,
//                                args.endDate,
//                                args.teamsLimit,
//                                it1)
//                        }
                        val action = viewModel.teamsHolder.value?.toTypedArray()?.let { it1 ->
                            EnrollTeamsFragmentDirections.actionEnrollTeamsFragmentToCreateTournamentFragment(
                                true,
                                args.tournamentName,
                                args.tournamentDescription,
                                args.goal,
                                args.endDate,
                                args.teamsLimit,
                                it1,
                                args.startDate
                            )
                        }
                        findNavController().navigate(action!!)
                    })
                } else {
                    viewModel.MessageStatus.observe(viewLifecycleOwner, Observer {
                        it.toast(context, Toast.LENGTH_SHORT)
                    })
                }
            }

            viewModel.getTeams().observe(viewLifecycleOwner, {
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = EnrollTeamsRecyclerViewAdapter(it, viewModel)
                }
                adapter = recyclerView.adapter as EnrollTeamsRecyclerViewAdapter
            })

            membersrv = findViewById(R.id.recyclerView)

            team_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })
        }
        return view
    }
}

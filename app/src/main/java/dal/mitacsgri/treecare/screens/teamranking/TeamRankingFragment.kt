package dal.mitacsgri.treecare.screens.teamranking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import kotlinx.android.synthetic.main.fragment_team_ranking.view.*
import kotlinx.android.synthetic.main.item_team_ranking.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TeamRankingFragment : Fragment() {

    private val mViewModel: TeamRankingViewModel by viewModel()
    val args: TeamRankingFragmentArgs by navArgs()
    private val sharedPrefs: SharedPreferencesRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.createFragmentViewWithStyle(
            activity, R.layout.fragment_team_ranking, R.style.tournament_mode_theme
        )

        view.apply {
            title.text = args.teamName

            mViewModel.membersList.value?.clear() //To avoid duplicates

            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            mViewModel.getTeamMembers(args.teamName, args.tournamentName).observe(viewLifecycleOwner, {

                recyclerView.apply {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = TeamRankingRecyclerViewAdapter(it, mViewModel)
                }
            })
        }
        return view
    }
}
package dal.mitacsgri.treecare.screens.teams

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import kotlinx.android.synthetic.main.fragment_teams.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class TeamsFragment : Fragment() {

    private val mViewModel: TeamsViewModel by viewModel()

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.createFragmentViewWithStyle(
            activity, R.layout.fragment_teams, R.style.tournament_mode_theme)

        view.apply {
            viewPager.adapter = TeamsPagerAdapter(childFragmentManager)

            tabLayout.setupWithViewPager(viewPager)
            toolbar.setNavigationOnClickListener {
                    findNavController().navigateUp()
            }

            mViewModel.hasTeamRequests().observe(viewLifecycleOwner, Observer {
                memberRequestsBadge.visibility = if (it) View.VISIBLE else View.GONE
            })

            memberRequestsIcon.setOnClickListener{
                findNavController().navigate(R.id.action_teamsFragment_to_invitesRequestFragment)
            }

            fabCreateTeam.setOnClickListener {
                findNavController().navigate(R.id.action_teamsFragment_to_createTeamFragment)
            }
        }

        return view
    }
}

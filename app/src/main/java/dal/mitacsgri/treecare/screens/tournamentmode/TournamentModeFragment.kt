package dal.mitacsgri.treecare.screens.tournamentmode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.fragment_teams.view.*
import kotlinx.android.synthetic.main.fragment_tournament_mode.view.*
import kotlinx.android.synthetic.main.fragment_tournament_mode.view.toolbar
import java.lang.IllegalStateException

class TournamentModeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tournament_mode, container, false)

        view.apply {
            Glide.with(this).load(R.drawable.tournament_stadium).into(tournamentsImage)
            Glide.with(this).load(R.drawable.team_run).into(teamsImage)

            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            tournaments.setOnClickListener {
                MainActivity.playClickSound()
                findNavController().navigate(R.id.action_tournamentModeFragment_to_tournamentsFragment)
            }

            teams.setOnClickListener {
                MainActivity.playClickSound()
                findNavController().navigate(R.id.action_tournamentModeFragment_to_teamsFragment)
            }

            /*returnHome.setOnClickListener {
                try{
                    findNavController().navigateUp()
                }
                catch (e: IllegalStateException){
                    activity?.onBackPressed()
                }
            }*/

        }

        return view
    }

}


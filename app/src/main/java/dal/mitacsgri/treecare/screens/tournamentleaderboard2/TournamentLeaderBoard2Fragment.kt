package dal.mitacsgri.treecare.screens.tournamentleaderboard2

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import kotlinx.android.synthetic.main.fragment_tournament_leaderboard.view.*
import kotlinx.android.synthetic.main.fragment_tournament_leaderboard_2.view.*
import kotlinx.android.synthetic.main.fragment_tournament_leaderboard_2.view.backButton
import kotlinx.android.synthetic.main.fragment_tournament_leaderboard_2.view.headingTV
import kotlinx.android.synthetic.main.fragment_tournament_leaderboard_2.view.recyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel

class TournamentLeaderBoard2Fragment : Fragment() {
    private val mViewModel: TournamentLeaderBoard2ViewModel by viewModel()
    val args : TournamentLeaderBoard2FragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.createFragmentViewWithStyle(activity,
            R.layout.fragment_tournament_leaderboard_2, R.style.tournament_mode_theme)


        view.apply {
            mViewModel.getTournamentTeams(args.tournamentName).observe(viewLifecycleOwner, Observer {
                recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                recyclerView.adapter = TournamentLeaderBoard2RecyclerViewAdapter(it, mViewModel)
            })

            //headingTV.text = args.tournamentName
            headingTV.text = mViewModel.getTournamentNameForLeaderBoard(args.tournamentName)

            val parent = backButton.parent as View // button: the view you want to enlarge hit area

            parent.post {
                val rect = Rect()
                backButton.getHitRect(rect)
                rect.top -= 100 // increase top hit area
                rect.left -= 100 // increase left hit area
                rect.bottom += 100 // increase bottom hit area
                rect.right += 100 // increase right hit area
                parent.touchDelegate = TouchDelegate(rect, backButton)
            }

            backButton.setOnClickListener {
                try{
                    findNavController().navigateUp()
                }
                catch (e: IllegalStateException){
                    activity?.onBackPressed()
                }
            }

            /*toolbar2.setNavigationOnClickListener {
                findNavController().navigateUp()
            }*/

        }
        return view
    }
}
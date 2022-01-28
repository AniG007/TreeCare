package dal.mitacsgri.treecare.screens.tournamentleaderboard

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.fragment_tournament_leaderboard.view.backButton
import kotlinx.android.synthetic.main.fragment_tournament_leaderboard.view.headingTV
import kotlinx.android.synthetic.main.fragment_tournament_leaderboard.view.recyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel

class TournamentLeaderBoardFragment : Fragment() {
    private val mViewModel: TournamentLeaderBoardViewModel by viewModel()
    //private val args: TournamentLeaderBoardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tournament_leaderboard, container, false)

        val args: TournamentLeaderBoardFragmentArgs = TournamentLeaderBoardFragmentArgs.fromBundle(arguments ?:
        Bundle().let {
            it.putString(view.context.getString(R.string.tournament_name), mViewModel.getTournamentName())
            it
        }
        )
        view.apply {
            //headingTV.text = args.tournamentName
            headingTV.text = mViewModel.getTournamentNameForLeaderBoard(args.tournamentName)

            val isSourceMainActivity = (requireActivity() as? MainActivity) != null //failure gives null

            backButton.setOnClickListener {
                try{
                    findNavController().navigateUp()
                }
                catch (e: IllegalStateException){
                    activity?.onBackPressed()
                }
            }

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

//            toolbar.setNavigationOnClickListener {
//                findNavController().navigateUp()
//            }

            mViewModel.getTeamList(args.tournamentName).observe(viewLifecycleOwner, Observer {
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = TournamentLeaderBoardRecyclerViewAdapter(it, mViewModel, isSourceMainActivity)

                    if((it.size>0) && !mViewModel.isDialogDisplayed){
                        mViewModel.isDialogDisplayed = false
                        val pos = mViewModel.getTeamPosition()
                        val action = TournamentLeaderBoardFragmentDirections.actionTournamentLeaderBoardFragmentToTournamentEndedDialog(pos)
                        findNavController().navigate(action)
                    }
                }
            })
        }
        return view
    }
}
package dal.mitacsgri.treecare.screens.leaderboard

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
import kotlinx.android.synthetic.main.fragment_leaderboard.view.backButton1
import kotlinx.android.synthetic.main.fragment_leaderboard.view.headingTV
import kotlinx.android.synthetic.main.fragment_leaderboard.view.recyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel


class LeaderboardFragment : Fragment() {

    private val mViewModel: LeaderboardItemViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        val args: LeaderboardFragmentArgs = LeaderboardFragmentArgs.fromBundle(
            arguments ?: Bundle().let {
                it.putString(
                    view.context.getString(R.string.challenge_name),
                    mViewModel.getChallengeName()
                )
                it
            }
        )

        view.apply {
            //headingTV.text = args.challengeName + " LeaderBoard"
            headingTV.text = mViewModel.getChallengeNameForLeaderBoard(args.challengeName)

            backButton1.setOnClickListener {
                try {
                    findNavController().navigateUp()
                } catch (e: IllegalStateException) {
                    activity?.onBackPressed()
                }
            }

            val parent = backButton1.parent as View // button: the view you want to enlarge hit area

            parent.post {
                val rect = Rect()
                backButton1.getHitRect(rect)
                rect.top -= 100 // increase top hit area
                rect.left -= 100 // increase left hit area
                rect.bottom += 100 // increase bottom hit area
                rect.right += 100 // increase right hit area
                parent.touchDelegate = TouchDelegate(rect, backButton1)
            }

//            toolbar.setNavigationOnClickListener {
//                findNavController().navigateUp()
//            }

            mViewModel.getChallengersList(args.challengeName).observe(viewLifecycleOwner, Observer {
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = LeaderboardRecyclerViewAdapter(it, mViewModel)

                    if ((it.size > 0) && !mViewModel.isDialogDisplayed) {
                        mViewModel.isDialogDisplayed = false

                        val pos = mViewModel.getCurrentChallengerPosition()
                        val action = LeaderboardFragmentDirections
                            .actionLeaderboardFragmentToChallengeCompleteDialog(pos)
                        findNavController().navigate(action)
                    }
                }
            })

        }
        return view
    }
}

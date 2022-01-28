package dal.mitacsgri.treecare.screens.challenges.currentchallenges

import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dal.mitacsgri.treecare.model.Challenge
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.challenges.ChallengesFragmentDirections
import dal.mitacsgri.treecare.screens.challenges.ChallengesViewModel
import dal.mitacsgri.treecare.screens.treecareunityactivity.TreeCareUnityActivity
import kotlinx.android.synthetic.main.item_current_challenge.view.*

/**
 * Created by Devansh on 25-06-2019
 */
class CurrentChallengesViewHolder(
    itemView: View,
    private val viewModel: ChallengesViewModel
    ): BaseViewHolder<Challenge>(itemView) {

    override fun bind(item: Challenge) {
        itemView.apply {
            nameTV.text = item.name
            goalTV.text = viewModel.getGoalText(item)
            durationTV.text = viewModel.getChallengeDurationText(item)
            membersCountTV.text = viewModel.getPlayersCountText(item)

            buttonTree.setOnClickListener {
                MainActivity.playClickSound()
                viewModel.startUnityActivityForChallenge(item) {
                    context.startActivity(Intent(context, TreeCareUnityActivity::class.java))
                }
            }

            buttonLeaderBoard.setOnClickListener {
                MainActivity.playClickSound()
                val action = ChallengesFragmentDirections
                    .actionChallengesFragmentToLeaderboardFragment(item.name)
                findNavController().navigate(action)
            }

            buttonExit.visibility = if (item.active) View.VISIBLE else View.INVISIBLE

            buttonExit.setOnClickListener {
                MainActivity.playClickSound()
                MaterialAlertDialogBuilder(context)
                    .setTitle("Leave challenge")
                    .setMessage("Do you really want to leave the challenge '${item.name}' ?")
                    .setPositiveButton("No") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("Yes") { _: DialogInterface, _: Int ->
                        viewModel.leaveChallenge(item)
                    }
                    .show()
            }

            currentChallengeCard.setOnClickListener {
                MainActivity.playClickSound()
                val action = ChallengesFragmentDirections
                    .actionChallengesFragmentToLeaderboardFragment(item.name)
                findNavController().navigate(action)
                //viewModel.disp(item)
            }
        }
    }
}
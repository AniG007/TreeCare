package dal.mitacsgri.treecare.screens.challenges.challengesbyyou

import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dal.mitacsgri.treecare.extensions.disable
import dal.mitacsgri.treecare.extensions.enable
import dal.mitacsgri.treecare.model.Challenge
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.challenges.ChallengesFragmentDirections
import dal.mitacsgri.treecare.screens.challenges.ChallengesViewModel
import dal.mitacsgri.treecare.screens.treecareunityactivity.TreeCareUnityActivity
import kotlinx.android.synthetic.main.item_challenge_by_you.view.*

/**
 * Created by Devansh on 28-06-2019
 */
class ChallengesByYouViewHolder(
    itemView: View,
    private val viewModel: ChallengesViewModel
    ): BaseViewHolder<Challenge>(itemView) {

    override fun bind(item: Challenge) {
        itemView.apply {
            nameTV.text = item.name
            descriptionTV.text = item.description
            durationTV.text = viewModel.getChallengeDurationText(item)
            membersCountTV.text = viewModel.getPlayersCountText(item)
            goalTV.text = viewModel.getGoalText(item)

            if (item.active) {
                buttonDelete.isEnabled = true
                buttonDelete.setOnClickListener {
                    MainActivity.playClickSound()
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Delete challenge")
                        .setMessage("Do you really want to delete the challenge '${item.name}' ?")
                        .setPositiveButton("No") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("Yes") { _: DialogInterface, _: Int ->
                            viewModel.deleteChallenge(item)
                        }
                        .show()
                }
            } else {
                buttonDelete.disable()
            }

            if (item.active && !viewModel.hasUserJoinedChallenge(item)) {
                buttonJoin.enable()
                buttonJoin.setOnClickListener {
                    MainActivity.playClickSound()
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Join the challenge")
                        .setMessage("Do you really want to join the challenge '${item.name}'")
                        .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                            viewModel.joinChallenge(item) {
                                viewModel.startUnityActivityForChallenge(item) {
                                    context.startActivity(Intent(context, TreeCareUnityActivity::class.java))
                                }
                            }
                        }
                        .setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        .show()
                }
            } else {
                buttonJoin.disable()
            }

            buttonLeaderBoard.setOnClickListener {
                MainActivity.playClickSound()
                val action = ChallengesFragmentDirections
                    .actionChallengesFragmentToLeaderboardFragment(item.name)
                findNavController().navigate(action)
            }

            cardView.setOnClickListener {
                MainActivity.playClickSound()
            }
        }
    }
}
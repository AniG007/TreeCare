package dal.mitacsgri.treecare.screens.tournaments.currenttournaments


import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.toDateTime
import dal.mitacsgri.treecare.model.Tournament
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.tournaments.TournamentsFragmentDirections
import dal.mitacsgri.treecare.screens.tournaments.TournamentsViewModel
import dal.mitacsgri.treecare.screens.treecareunityactivity.TreeCareUnityActivity
import kotlinx.android.synthetic.main.item_current_tournament.view.*
import kotlinx.android.synthetic.main.item_current_tournament.view.durationTV
import kotlinx.android.synthetic.main.item_current_tournament.view.goalTV
import kotlinx.android.synthetic.main.item_current_tournament.view.nameTV
import kotlinx.android.synthetic.main.item_current_tournament.view.teamCountTV
import org.joda.time.DateTime

/**
 * Created by Anirudh on 25-04-2020
 */
class CurrentTournamentsViewHolder(
    itemView: View,
    private val viewModel: TournamentsViewModel
): BaseViewHolder<Tournament>(itemView) {

    override fun bind(item: Tournament) {
        itemView.apply {
            nameTV.text = item.name
            goalTV.text = viewModel.getGoalText(item)
            durationTV.text = viewModel.getTournamentDurationText(item)
            currentStartDate.text = viewModel.getTournamentStartDate(item)
            //team1CountTV.text = viewModel.getTeamsCountText(item)
            teamCountTV.text = viewModel.getTeamsCountText(item).toString()

            buttonTree.setOnClickListener {
                MainActivity.playClickSound()
                viewModel.startUnityActivityForTournament(item) {
                    context.startActivity(Intent(context, TreeCareUnityActivity::class.java))
                }
            }

            buttonLeaderBoard.setOnClickListener {
                MainActivity.playClickSound()
                if(item.startTimestamp.toDateTime().withTimeAtStartOfDay().millis <= DateTime().withTimeAtStartOfDay().millis) {
                    val action =
                        TournamentsFragmentDirections.actionTournamentsFragmentToTournamentLeaderBoardFragment(
                            item.name
                        )
                    findNavController().navigate(action)
                }

                else{
                    val action = TournamentsFragmentDirections.actionTournamentsFragmentToTournamentLeaderBoard2Fragment(item.name)
                    findNavController().navigate(action)
                }
            }

            buttonExit.visibility = if (item.active) View.VISIBLE else View.INVISIBLE

            buttonExit.setOnClickListener {
                MainActivity.playClickSound()
                MaterialAlertDialogBuilder(context)
                    .setTitle("Leave tournament")
                    .setMessage("Do you really want to leave the tournament '${item.name}' ?")
                    .setPositiveButton("No") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("Yes") { _: DialogInterface, _: Int ->
                        viewModel.leaveTournament(item)
                        //findNavController().navigateUp()
                    }
                    .show()
            }

            currentTournamentCard.setOnClickListener {
                MainActivity.playClickSound()
                //viewModel.display2(item)
                if(item.startTimestamp.toDateTime().withTimeAtStartOfDay().millis <= DateTime().withTimeAtStartOfDay().millis) {
                    val action =
                        TournamentsFragmentDirections.actionTournamentsFragmentToTournamentLeaderBoardFragment(
                            item.name
                        )
                    findNavController().navigate(action)
                }
                else{
                    val action = TournamentsFragmentDirections.actionTournamentsFragmentToTournamentLeaderBoard2Fragment(item.name)
                    findNavController().navigate(action)
                }
            }
        }
    }
}
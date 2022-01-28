package dal.mitacsgri.treecare.screens.tournaments.mytournaments

import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dal.mitacsgri.treecare.extensions.getMapFormattedDate
import dal.mitacsgri.treecare.extensions.toDateTime
import dal.mitacsgri.treecare.model.Tournament
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.tournaments.TournamentsFragmentDirections
import dal.mitacsgri.treecare.screens.tournaments.TournamentsViewModel
import dal.mitacsgri.treecare.screens.treecareunityactivity.TreeCareUnityActivity
import kotlinx.android.synthetic.main.item_my_tournaments.view.*
import org.joda.time.DateTime

class MyTournamentsViewHolder (
    itemView: View,
    private val viewModel: TournamentsViewModel
): BaseViewHolder<Tournament>(itemView) {
    override fun bind(item: Tournament) {
        itemView.apply {

            nameTV.text = item.name
            goalTV.text = viewModel.getGoalText(item)
            durationTV.text = viewModel.getTournamentDurationText(item)
            currentStartDate.text = viewModel.getTournamentStartDate(item)
            teamCountTV.text = viewModel.getTeamsCountText(item).toString()

            buttonTree.setOnClickListener {
                MainActivity.playClickSound()
                viewModel.startUnityActivityForTournament(item) {
                    context.startActivity(Intent(context, TreeCareUnityActivity()::class.java))
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

            buttonDelete.visibility = if (!item.active && item.exist && item.startTimestamp.toDateTime().millis > DateTime().withTimeAtStartOfDay().millis) View.VISIBLE else View.INVISIBLE
            buttonEdit.visibility = if (item.exist && item.finishTimestamp.toDateTime().millis >= DateTime().withTimeAtStartOfDay().millis) View.VISIBLE else View.INVISIBLE

            /*Log.d("Fraggy", item.finishTimestamp.toDateTime().millis.toString())
            Log.d("Fraggy2", DateTime().withTimeAtStartOfDay().millis.toString())*/


            buttonDelete.setOnClickListener {
                MainActivity.playClickSound()
                MaterialAlertDialogBuilder(context)
                    .setTitle("Delete tournament")
                    .setMessage("Do you really want to delete the tournament '${item.name}' ?")
                    .setPositiveButton("No") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("Yes") { _: DialogInterface, _: Int ->
                        viewModel.deleteTournament(item)
                        //findNavController().navigateUp()
                    }
                    .show()
            }

            buttonEdit.setOnClickListener {

                if(item.startTimestamp.toDateTime().millis <= DateTime().withTimeAtStartOfDay().millis) {

                    val action =
                        TournamentsFragmentDirections.actionTournamentsFragmentToEditTournamentAfterStartFragment(item.name, item.finishTimestamp.toDateTime().getMapFormattedDate())

                    findNavController().navigate(action)
                }

                else{

                    val action =
                        TournamentsFragmentDirections.actionTournamentsFragmentToEditTournamentFragment(item.name, item.description, item.dailyGoal.toString(), item.startTimestamp.toDateTime().getMapFormattedDate(), item.finishTimestamp.toDateTime().getMapFormattedDate())

                    findNavController().navigate(action)
                }
            }

            myTournamentCard.setOnClickListener {
                MainActivity.playClickSound()
                /** remove this if you want to navigate to leader board */
                /*if(item.startTimestamp.toDateTime().withTimeAtStartOfDay().millis <= DateTime().withTimeAtStartOfDay().millis) {
                    val action =
                        TournamentsFragmentDirections.actionTournamentsFragmentToTournamentLeaderBoardFragment(
                            item.name
                        )
                    findNavController().navigate(action)
                }
                else{
                    val action = TournamentsFragmentDirections.actionTournamentsFragmentToTournamentLeaderBoard2Fragment(item.name)
                    findNavController().navigate(action)
                }*/
            }
        }
    }
}
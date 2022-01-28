package dal.mitacsgri.treecare.screens.tournaments.activetournaments

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.toObjects
import dal.mitacsgri.treecare.extensions.disable
import dal.mitacsgri.treecare.extensions.enable
import dal.mitacsgri.treecare.extensions.toDateTime
import dal.mitacsgri.treecare.model.Challenge
import dal.mitacsgri.treecare.model.Tournament
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.tournaments.TournamentsFragmentDirections
import dal.mitacsgri.treecare.screens.tournaments.TournamentsViewModel
import kotlinx.android.synthetic.main.item_active_tournament.view.*
import kotlinx.android.synthetic.main.item_active_tournament.view.durationTV
import kotlinx.android.synthetic.main.item_active_tournament.view.goalTV
import kotlinx.android.synthetic.main.item_active_tournament.view.nameTV
import kotlinx.android.synthetic.main.item_active_tournament.view.teamCountTV
import org.joda.time.DateTime
import java.io.FileOutputStream

class ActiveTournamentsViewHolder(
    private val viewModel: TournamentsViewModel,
    itemView: View): BaseViewHolder<Tournament>(itemView) {

    @SuppressLint("SetTextI18n")
    override fun bind(item: Tournament) {

        itemView.apply {

            nameTV.text = item.name
            goalTV.text = viewModel.getGoalText(item)
            descriptionTV.text = item.description
            durationTV.text = viewModel.getTournamentDurationText(item)
            startDate.text = viewModel.getTournamentStartDate(item)
            //membersCountTV.text = viewModel.getTeamsCountText(item)
            teamCountTV.text = viewModel.getTeamsCountText(item).toString()
            //team2CountTV.text = viewModel.getTeamsCountText(item).toString()

            //if (item.active && !viewModel.hasTeamJoinedTournament(item)) {

            if ((item.active || item.finishTimestamp.toDateTime().withTimeAtStartOfDay().millis > DateTime().withTimeAtStartOfDay().millis) && !viewModel.hasTeamJoined(item.name)) {
                buttonJoin.enable()
                buttonJoin.setOnClickListener {
                    MainActivity.playClickSound()
                    MaterialAlertDialogBuilder(context)
                        .setTitle(viewModel.getJoinTournamentDialogTitleText(item))
                        .setMessage(viewModel.getJoinTournamentMessageText())
                        .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                           // val action = TournamentsFragmentDirections.actionTournamentsFragmentToEnrollTeamsFragment(item.name)
                            viewModel.getExistingTeams(item.name)
                            viewModel.enrollTeams(item.name, item)
                            //buttonJoin.setEnabled(false)

                            //findNavController().navigateUp()
                            /*viewModel.joinTournament(item) {
                                viewModel.startUnityActivityForTournament(item) {
                                    context.startActivity(Intent(context, TreeCareUnityActivity::class.java))
                                }
                            }*/
                        }
                        .setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        .show()
                }
            } else {
                buttonJoin.disable()
            }

            tourney_card.setOnClickListener {
                MainActivity.playClickSound()
                //viewModel.display2(item)
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
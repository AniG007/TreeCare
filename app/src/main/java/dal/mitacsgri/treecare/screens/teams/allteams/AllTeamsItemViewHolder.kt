package dal.mitacsgri.treecare.screens.teams.allteams

import android.util.Log
import android.view.View
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.di.sharedPreferencesRepositoryModule
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.teams.TeamsFragmentDirections
import kotlinx.android.synthetic.main.item_all_teams_team.view.*
import org.jetbrains.anko.toast

/**
 * Created by Devansh on 15-07-2019
 */

class AllTeamsItemViewHolder(itemView: View, private val viewModel: AllTeamsViewModel)
    : BaseViewHolder<Team>(itemView) {

    override fun bind(item: Team) {
        itemView.apply {
            nameTV.text = item.name
            descriptionTV.text = item.description
            membersCountTV.text = viewModel.getMembersCountText(item)
            captainNameTV.text = viewModel.getCaptainNameText(item)
            val sharedPrefsRepository: SharedPreferencesRepository
            if (viewModel.isUserCaptain(item.captain)) {
                button.text = context.getString(R.string.invite_member)
                button.setOnClickListener {
                    MainActivity.playClickSound()
                    if (viewModel.teamExist()) {
                        val tName = nameTV.text.toString()
                        Log.d("TAG", "TeamNameinAll" + tName)
                        val action =
                            TeamsFragmentDirections.actionTeamsFragmentToJoinTeamFragment(tName)
                        //findNavController().navigate(R.id.action_teamsFragment_to_joinTeamFragment)
                        findNavController().navigate(action)
                    } else {
                        findNavController().navigateUp()
                    }
                }
            }

            else {
                button.text =
                    if (viewModel.isJoinRequestSent(item)) context.getString(R.string.cancel_request)
                    else context.getString(R.string.send_join_request)
                joinRequestAction(button, item)
            }

            allTeamsCard.setOnClickListener {
                MainActivity.playClickSound()
                val action = TeamsFragmentDirections.actionTeamsFragmentToTeamInfoFragment(item.name, "AllTeamsFragment")
                findNavController().navigate(action)
            }
        }
    }

    private fun joinRequestAction(button: MaterialButton, team: Team) {

        var sendJoinRequestListener = View.OnClickListener {  }
        var cancelJoinRequestListener = View.OnClickListener {  }

        sendJoinRequestListener = View.OnClickListener {
            MainActivity.playClickSound()
            viewModel.sendJoinRequest(team.name) {
                changeButtonStateAndBehaviour(
                    it, button, sendJoinRequestListener, cancelJoinRequestListener)
            }
        }

        cancelJoinRequestListener = View.OnClickListener {
            MainActivity.playClickSound()
            viewModel.cancelJoinRequest(team.name) {
                changeButtonStateAndBehaviourTwo(
                    //!it, button, sendJoinRequestListener, cancelJoinRequestListener)
                    it, button, sendJoinRequestListener, cancelJoinRequestListener)
            }
        }

        button.setOnClickListener(
            if (viewModel.isJoinRequestSent(team)) cancelJoinRequestListener
            else sendJoinRequestListener)
    }
   /* private fun invitePlayerAction(button: MaterialButton, user: User) {

        var sendInviteListener = View.OnClickListener {  }
        var cancelInviteListener = View.OnClickListener {  }

        sendInviteListener = View.OnClickListener {
            viewModel.sendInvite(user.uid) {
                changeButtonStateAndBehaviour(
                    it, button, sendInviteListener, cancelInviteListener)
            }
        }

        cancelInviteListener = View.OnClickListener {
            viewModel.cancelInvite(user.uid) {
                changeButtonStateAndBehaviour(
                    !it, button, sendInviteListener, cancelInviteListener)
            }
        }

        button.setOnClickListener(
            if (viewModel.isInviteSent(user)) cancelInviteListener
            else sendInviteListener)
    }*/

    private fun changeButtonStateAndBehaviour(
        status: String, button: MaterialButton,
        sendJoinRequestListener: View.OnClickListener, cancelJoinRequestListener: View.OnClickListener) {

        val context = button.context

        context.apply {
            if (status.equals("true")) {
                toast(R.string.join_request_sent)
                button.text = context.getString(R.string.cancel_request)
                button.setOnClickListener(cancelJoinRequestListener)
            }
            else if(status.equals("teamexists")){
                toast(R.string.join_request_exceed)
            }
            else {
                toast(R.string.join_request_cancelled)
                button.text = context.getString(R.string.send_join_request)
                button.setOnClickListener(sendJoinRequestListener)
            }
        }
    }

    private fun changeButtonStateAndBehaviourTwo(
        status: String, button: MaterialButton,
        sendJoinRequestListener: View.OnClickListener, cancelJoinRequestListener: View.OnClickListener) {

        val context = button.context

        context.apply {
            if (status.equals("true")) {
                toast(R.string.join_request_cancelled)
                button.text = context.getString(R.string.send_join_request)
                button.setOnClickListener(sendJoinRequestListener)
            }
            else {
                toast(R.string.join_request_sent)
                button.text = context.getString(R.string.cancel_request)
                button.setOnClickListener(cancelJoinRequestListener)
            }
        }
    }
}
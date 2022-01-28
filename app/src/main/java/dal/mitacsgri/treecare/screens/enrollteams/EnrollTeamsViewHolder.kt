package dal.mitacsgri.treecare.screens.enrollteams

import android.view.View
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.item_enroll_teams.view.*


class EnrollTeamsViewHolder(
    itemView: View,
    private val viewModel: EnrollTeamsViewModel
):BaseViewHolder<Team>(itemView) {
    override fun bind(item: Team){
        itemView.apply{
            teamNameTV.text = item.name
            membersCountTV.text = item.members.count().toString()

            checkbox_team.setOnClickListener {
                if(it is CheckBox){
                    val checked :Boolean = it.isChecked
                    when(it.id) {
                        R.id.checkbox_team -> {
                            if(checked)
                                viewModel.addTeamToList(item.name)
                            else
                                viewModel.removeTeamFromList(item.name)
                        }
                    }
                }
            }

            enrollteamCard.setOnClickListener {
                MainActivity.playClickSound()
                val action = EnrollTeamsFragmentDirections.actionEnrollTeamsFragmentToTeamInfoFragment(item.name, "enrollTeamsFragment")
                findNavController().navigate(action)
            }

        }

    }
}
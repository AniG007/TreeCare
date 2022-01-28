package dal.mitacsgri.treecare.screens.teams.yourteams

import android.content.DialogInterface
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.teams.TeamsFragmentDirections
import kotlinx.android.synthetic.main.item_your_captained_team.view.*


class YourTeamViewHolder(itemView: View, private val viewModel: YourTeamsViewModel)
    : BaseViewHolder<Team>(itemView) {

    override fun bind(item: Team) {
        itemView.apply {
            teamNameTV.text = item.name
            membersCountTV.text = item.members.size.toString()

            if (viewModel.isUserCaptain(item.captain)) {
                buttonDelete.visibility = View.VISIBLE
                addButton.visibility = View.VISIBLE
                playerRank.visibility = View.INVISIBLE

                buttonDelete.setOnClickListener {
                    MainActivity.playClickSound()
                    MaterialAlertDialogBuilder(context)
                            .setTitle("Delete Team")
                        .setMessage("Do you really want to delete your team '${item.name}' ?")
                        .setPositiveButton("No") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("Yes") { _: DialogInterface, _: Int ->
                            viewModel.deleteTeam(item)
                           // findNavController().navigateUp()
                        }
                        .show()
                }
            }

            else {
                buttonDelete.visibility = View.INVISIBLE
                addButton.visibility = View.INVISIBLE

                //captainRank.visibility = View.INVISIBLE
                  playerRank.visibility = View.VISIBLE
            }

            if (!viewModel.isUserCaptain(item.captain)) {
                buttonExit.visibility = View.VISIBLE
                buttonExit.setOnClickListener {
                    MainActivity.playClickSound()
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Exit Team")
                        .setMessage("Do you really want to exit the team '${item.name}' ?")
                        .setPositiveButton("No") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("Yes") { _: DialogInterface, _: Int ->
                            //viewModel.exitTeam(item)
                            viewModel.displayMessageForTest()
                        }
                        .show()
                }
            }

            else{
                buttonExit.visibility = View.INVISIBLE
            }

            addButton.setOnClickListener {
                MainActivity.playClickSound()
                val action = TeamsFragmentDirections.actionTeamsFragmentToJoinTeamFragment(item.name)
                findNavController().navigate(action)
            }

//            viewButton.setOnClickListener {
//                val action = TeamsFragmentDirections.actionTeamsFragmentToTeamInfoFragment(item.name)
//                findNavController().navigate(action)
//            } //To be removed since card can be clicked to view team members

            teamCard.setOnClickListener{
                MainActivity.playClickSound()
                val action = TeamsFragmentDirections.actionTeamsFragmentToTeamInfoFragment(
                    item.name,
                    "teamsFragment"
                )
                findNavController().navigate(action)
            }
        }
    }
}

           /* buttonDelete.setOnClickListener{
                MaterialAlertDialogBuilder(context)
                    .setTitle("Delete challenge")
                    .setMessage("Do you really want to delete your team '${item.name}' ?")
                    .setPositiveButton("No") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("Yes") { _: DialogInterface, _: Int ->
                        viewModel.deleteTeam(item.name)
                    }
                    .show()
            }*/


            //viewModel.delBtnVis(item)
            //if(status.value == true) return
            //else buttonDelete.visibility = View.INVISIBLE

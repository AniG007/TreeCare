package dal.mitacsgri.treecare.screens.teaminfo

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.TeamInfo
import dal.mitacsgri.treecare.screens.BaseViewHolder
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.item_team_info.view.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TeamInfoViewHolder(
    itemView: View,
    private val viewModel: TeamInfoViewModel
):BaseViewHolder<TeamInfo>(itemView) {
    private var playerPosition = 0

    override fun bind(item: TeamInfo){
        itemView.apply {
            nameTV.text = item.userName
//            stepsCountTV.text = item.stepsCount.toString()
//            leafCountTV.text = item.leavesCount.toString()
//            rankTV.text = playerPosition.toString()

            //stepsCountTV.text = item.stepsCount.toString()
            //leafCountTV.text = item.leavesCount.toString()

            Glide.with(this).load(item.photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)

            if (viewModel.isCurrentUser(item)) {
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimaryLight
                    )
                )
            }

            if(item.captainId == item.uId){
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.captain_primary_color
                    )
                )
            }
            /*viewModel.captainHighlight(item.uId,item.teamName).observe()
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context,
                        R.color.captain_primary_color))
            }*/


            /*if (viewModel.isUserCaptain(item.uId)) {
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context,
                        R.color.captain_primary_color))
            }*/

            if (viewModel.isUserCaptain(item.captainId) && (item.captainId != item.uId) ) {
                removePlayer.visibility = View.VISIBLE
                removePlayer.setOnClickListener{
                    MainActivity.playClickSound()
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Remove Team Member")
                        .setMessage("Do you really want to remove '${item.userName}' from your team?")
                        .setPositiveButton("No") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("Yes") { _: DialogInterface, _: Int ->
                            viewModel.removePlayer(item)
                        }
                        .show()
                }
            }
            else{
                removePlayer.visibility = View.INVISIBLE
            }

            cardView.setOnClickListener {
                MainActivity.playClickSound()

                val today = DateTime()
                //val dateOutputFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val dateOutputFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withZone(DateTimeZone.forID("America/Halifax"))

                //dateOutputFormat.timeZone = TimeZone.getTimeZone("America/Vancouver")
                Log.d("Datey2", "Before conversion ${today}")
//                val mNight = (dateOutputFormat.format(today))
//                //val mNight2 = Date(dateOutputFormat2.format(mNight)).time
                Log.d("Datey2", "After conversion "+ today.toString(dateOutputFormat))
//                //viewModel.display()
//                val nighty = DateTime(mNight).withTimeAtStartOfDay()
                Log.d("Datey2", "MidNight "+ today.withZone(DateTimeZone.forID("America/Halifax")).withTimeAtStartOfDay().millis)
//                Log.d("Datey2", "MN ${nighty}")
            }
        }
    }

    fun bind(item: TeamInfo, position: Int){
        playerPosition = position + 1
        bind(item)
    }
}
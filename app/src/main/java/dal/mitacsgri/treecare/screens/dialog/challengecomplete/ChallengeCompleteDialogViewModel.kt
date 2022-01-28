package dal.mitacsgri.treecare.screens.dialog.challengecomplete

import android.text.SpannedString
import androidx.annotation.ColorInt
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.lifecycle.ViewModel
import dal.mitacsgri.treecare.R

class ChallengeCompleteDialogViewModel: ViewModel() {

    fun getChallengerPositionText(@ColorInt color: Int, position: Int): SpannedString {
        return buildSpannedString {
            append("You finished at position ")
            color(color) {
                append("#$position")
            }
        }
    }

    fun getTrophyImage(position: Int) =
            when(position) {
                1 -> R.drawable.ic_trophy_first
                2 -> R.drawable.ic_trophy_second
                3 -> R.drawable.ic_trophy_third
                else -> R.color.transparent
            }

}
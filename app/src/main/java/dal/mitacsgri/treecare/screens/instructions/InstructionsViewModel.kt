package dal.mitacsgri.treecare.screens.instructions

import android.content.Context
import androidx.lifecycle.ViewModel
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.consts.CHALLENGER_MODE
import dal.mitacsgri.treecare.consts.STARTER_MODE
import dal.mitacsgri.treecare.consts.TOURNAMENT_MODE

class InstructionsViewModel: ViewModel() {

    fun getModeName(mode: Int) =
            when(mode) {
                STARTER_MODE -> "Starter Mode"
                CHALLENGER_MODE -> "Challenger Mode"
                TOURNAMENT_MODE -> "Tournament Mode"
                else -> ""
            }

    fun getInstructions(mode: Int, context: Context) =
            when(mode) {
                STARTER_MODE -> context.getString(R.string.starter_mode_instructions)
                CHALLENGER_MODE -> context.getString(R.string.challenger_mode_instructions)
                TOURNAMENT_MODE -> context.getString(R.string.tournament_mode_instructions)
                else -> ""
            }
}
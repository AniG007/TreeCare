package dal.mitacsgri.treecare.screens.dialog.tournamentended

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.dialog_tournament_ended.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class TournamentEndedDialog: DialogFragment() {

    private val mViewModel: TournamentEndedDialogViewModel by viewModel()
    private val args: TournamentEndedDialogArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val position = args.position
        val view = inflater.inflate(R.layout.dialog_tournament_ended, container, true)

        view.apply {
            MainActivity.playVictorySound()
            positionTV.text= mViewModel.getTeamPositionText(
                ContextCompat.getColor(context, R.color.challenger_mode_primary_color),position)
            trophyImage.setImageResource(mViewModel.getTrophyImage(position))
        }
        return view
    }
}
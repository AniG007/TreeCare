package dal.mitacsgri.treecare.screens.dialog.challengecomplete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.dialog_challenge_complete.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChallengeCompleteDialog: DialogFragment() {

    private val args: ChallengeCompleteDialogArgs by navArgs()
    private val mViewModel: ChallengeCompleteDialogViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val position = args.position
        val view = inflater.inflate(R.layout.dialog_challenge_complete, container, true)

        view.apply {
            MainActivity.playVictorySound()
            positionTV.text = mViewModel.getChallengerPositionText(
                ContextCompat.getColor(context, R.color.challenger_mode_primary_color), position)
            trophyImage.setImageResource(mViewModel.getTrophyImage(position))
        }
        return view
    }
}
package dal.mitacsgri.treecare.screens.instructions


import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.consts.*
import dal.mitacsgri.treecare.extensions.startNextActivity
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.MainViewModel
import dal.mitacsgri.treecare.screens.treecareunityactivity.TreeCareUnityActivity
//import dal.mitacsgri.treecare.screens.treecareunityactivity.TreeCareUnityActivity
import kotlinx.android.synthetic.main.fragment_instructions.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class InstructionsFragment : Fragment() {

    private val mainViewModel: MainViewModel by sharedViewModel()
    private val instructionsViewModel: InstructionsViewModel by viewModel()
    private val args: InstructionsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_instructions, container, false)

        mainViewModel.setInstructionsDisplayed(args.mode, true)

        view.apply {
            changeBackgroundSolidAndStrokeColor(scrollView, "ccffffff", "ff646464")
            changeButtonColorAccordingToMode(continueButton as MaterialButton, args.mode)
            modeNameTV.text = instructionsViewModel.getModeName(args.mode)
            instructionsTV.text = instructionsViewModel.getInstructions(args.mode, context)

            continueButton.setOnClickListener {
                MainActivity.playClickSound()
                when(args.mode) {
                    STARTER_MODE -> {
                        activity?.startNextActivity(TreeCareUnityActivity::class.java)
                    }
                    CHALLENGER_MODE -> {
                        findNavController().navigate(R.id.action_instructionsFragment_to_challengesFragment)
                    }
                    TOURNAMENT_MODE -> {
                        findNavController().navigate(R.id.action_instructionsFragment_to_tournamentModeFragment)
                    }
                }
                findNavController().popBackStack(R.id.instructionsFragment, true)
            }
        }
        return view
    }

    private fun changeButtonColorAccordingToMode(button: MaterialButton, mode: Int) {
        when(mode) {
            STARTER_MODE -> {
                changeMaterialButtonBackgroundColor(button, STARTER_MODE_SOLID_COLOR)
            }
            CHALLENGER_MODE -> {
                changeMaterialButtonBackgroundColor(button, CHALLENGER_MODE_SOLID_COLOR)
            }
            TOURNAMENT_MODE -> {
                changeMaterialButtonBackgroundColor(button, TOURNAMENT_MODE_SOLID_COLOR)
            }
        }
    }

    private fun changeBackgroundSolidAndStrokeColor(
        view: View, solidColor: String, strokeColor: String) {

        val background = view.background as GradientDrawable
        background.setColor(Color.parseColor("#$solidColor"))
        background.setStroke(
            resources.getDimension(com.intuit.sdp.R.dimen._4sdp).toInt(),
            Color.parseColor("#$strokeColor"))

    }

    private fun changeMaterialButtonBackgroundColor(button: MaterialButton, color: String) {
        button.setBackgroundColor(Color.parseColor("#$color"))
    }
}

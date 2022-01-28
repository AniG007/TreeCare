package dal.mitacsgri.treecare.screens.modeselection

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.consts.CHALLENGER_MODE
import dal.mitacsgri.treecare.consts.STARTER_MODE
import dal.mitacsgri.treecare.consts.TOURNAMENT_MODE
import dal.mitacsgri.treecare.extensions.enable
import dal.mitacsgri.treecare.extensions.startNextActivity
import dal.mitacsgri.treecare.screens.MainActivity.Companion.playClickSound
import dal.mitacsgri.treecare.screens.MainViewModel
import dal.mitacsgri.treecare.screens.profile.ProfileViewModel
import dal.mitacsgri.treecare.screens.treecareunityactivity.TreeCareUnityActivity
import kotlinx.android.synthetic.main.fragment_mode_selection.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ModeSelectionFragment : Fragment() {

    private val mainViewModel: MainViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mode_selection, container, false)

        view.apply {
            changeBackgroundSolidAndStrokeColor(tournamentModeButton, "FF9C27B0", "1A237E")

            /*starterModeButton.visibility = View.INVISIBLE
            starterModeButtonHolder.visibility = View.INVISIBLE

            challengerModeButton.visibility = View.INVISIBLE
            challengerModeButtonHolder.visibility = View.INVISIBLE

            tournamentModeButton.visibility = View.INVISIBLE
            tournamentModeButtonHolder.visibility = View.INVISIBLE

            buttonProfile.visibility = View.INVISIBLE*/

            starterModeButtonHolder.setOnClickListener {
                playClickSound()
                starterModeButtonAction()
            }
            starterModeButton.setOnClickListener {
                playClickSound()
                starterModeButtonAction()
            }

            challengerModeButtonHolder.setOnClickListener {
                playClickSound()
                challengerModeButtonAction()
            }
            challengerModeButton.setOnClickListener {
                playClickSound()
                challengerModeButtonAction()
            }

            tournamentModeButtonHolder.setOnClickListener{
                playClickSound()
                tournamentModeButtonAction()
            }

            tournamentModeButton.setOnClickListener(){
                playClickSound()
                tournamentModeButtonAction()
            }

            Glide.with(this).load(profileViewModel.getUserPhotoUrl())
                .placeholder(R.drawable.ic_profile_empty)
                .apply(RequestOptions.circleCropTransform())
                .into(buttonProfile)
            buttonProfile.setOnClickListener {
                playClickSound()
                val extras = FragmentNavigatorExtras(
                    buttonProfile to getString(R.string.profile_image_end_transition)
                )
                findNavController().navigate(R.id.action_modeSelectionFragment_to_profileFragment,
                    null, null, extras)
            }
        }

        return view
    }

    private fun starterModeButtonAction() {
        mainViewModel.setGameMode(STARTER_MODE)
        startInstructionOrUnityActivity(
            STARTER_MODE,
            mainViewModel.hasInstructionsDisplayed(STARTER_MODE)
        )
    }

    private fun challengerModeButtonAction() {
        mainViewModel.setGameMode(CHALLENGER_MODE)
        if (mainViewModel.hasInstructionsDisplayed(CHALLENGER_MODE))
            findNavController().navigate(R.id.action_modeSelectionFragment_to_challengesFragment)
        else {
            mainViewModel.setInstructionsDisplayed(CHALLENGER_MODE, true)
            val action = ModeSelectionFragmentDirections
                .actionModeSelectionFragmentToInstructionsFragment(CHALLENGER_MODE)
            findNavController().navigate(action)
        }
    }

    private fun tournamentModeButtonAction(){
        mainViewModel.setGameMode(TOURNAMENT_MODE)
        if(mainViewModel.hasInstructionsDisplayed(TOURNAMENT_MODE))
            findNavController().navigate(R.id.action_modeSelectionFragment_to_tournamentModeFragment)
        else {
            mainViewModel.setInstructionsDisplayed(TOURNAMENT_MODE, true)
            val action =
                ModeSelectionFragmentDirections.actionModeSelectionFragmentToInstructionsFragment(
                    TOURNAMENT_MODE)
            findNavController().navigate(action)
        }
       // findNavController().navigate(R.id.action_modeSelectionFragment_to_tournamentModeFragment)

    }

    private fun changeBackgroundSolidAndStrokeColor(
        button: MaterialButton, solidColor: String, strokeColor: String) {
        button.setBackgroundColor(Color.parseColor("#$solidColor"))
    }

    private fun startInstructionOrUnityActivity(mode: Int, hasInstructionsDisplayed: Boolean) {
        if (hasInstructionsDisplayed)
            activity?.startNextActivity(TreeCareUnityActivity::class.java)
        else {
            val action = ModeSelectionFragmentDirections
                .actionModeSelectionFragmentToInstructionsFragment(mode)
            findNavController().navigate(action)
        }
    }

}

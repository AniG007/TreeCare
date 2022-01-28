package dal.mitacsgri.treecare.screens.splash


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.StepCountDataProvidingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashScreenFragment : Fragment() {

    private val stepCountDataProvidingViewModel: StepCountDataProvidingViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash_screen, container, false)
        stepCountDataProvidingViewModel.apply {
            //testGameByManipulatingSharedPrefsData(this)
            resetDailyGoalCheckedFlag()
            stepCountDataFetchedCounter.observe(viewLifecycleOwner, {
                //This value will be 2 only when login has been done
                if (it == 2) {
                    navigateWithDelay(R.id.action_splashScreenFragment_to_modeSelectionFragment)
                }
            })

            if (isLoginDone) {
                accessStepCountDataUsingApi()
            }
            else {
                navigateWithDelay(R.id.action_splashScreenFragment_to_loginFragment)
            }
        }
        return view
    }

    private fun navigateWithDelay(actionResId: Int, delay: Long = 5000L) {
        MainActivity.playSplashScreenIntro()
        Handler().postDelayed({
            findNavController().navigate(actionResId)
        }, delay)
    }

}

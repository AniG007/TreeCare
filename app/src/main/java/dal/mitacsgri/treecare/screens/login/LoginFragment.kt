package dal.mitacsgri.treecare.screens.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.model.Team
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.screens.MainActivity
import dal.mitacsgri.treecare.screens.MainViewModel
import dal.mitacsgri.treecare.screens.StepCountDataProvidingViewModel
import dal.mitacsgri.treecare.screens.dialog.logindataloading.LoginDataLoadingDialog
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {

    //Shared ViewModel with parent activity
    private val mainViewModel: MainViewModel by sharedViewModel()
    private val stepCountDataProvidingViewModel: StepCountDataProvidingViewModel by viewModel()
    private val sharedPrefsRepository: SharedPreferencesRepository by inject()

    private val loadingDialog = LoginDataLoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.signInButton.setOnClickListener {
            MainActivity.playClickSound()
            //mainViewModel.startLoginAndConfiguration(activity!!)
            mainViewModel.startLoginAndConfiguration(requireActivity())
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            it.window.statusBarColor = ContextCompat.getColor(context, R.color.gray)
        }
        setUpLiveDataObservers(context)
    }

    private fun setUpLiveDataObservers(context: Context) {

        mainViewModel.apply {

            userFirstName.observe(this@LoginFragment, Observer {
                "Welcome $it !".toast(context)
                walkAnimation.pauseAnimation()
                //loadingDialog.show(requireFragmentManager(), "login_dialog")
            })

            isLoginDone.observe(this@LoginFragment, Observer {value ->
                if (value) {
                    Log.d("FitAPI", "Login succes, accessing step count using api")
                    stepCountDataProvidingViewModel.accessStepCountDataUsingApi()
                }
            })

            stepCountDataProvidingViewModel.stepCountDataFetchedCounter
                .observe(this@LoginFragment, Observer {
                    if (it == 2) {
//                        loadingDialog.dismiss()
                        mainViewModel.setCrashlyticsUserIdentifiers()
                        mainViewModel.subscribeToFCMDailyGoalNotification(context)
                        NavHostFragment.findNavController(this@LoginFragment)
                            .navigate(R.id.action_loginFragment_to_modeSelectionFragment)
                    }
                    else{
                        //so that the bg workers do not crash when the user does not login.
                        sharedPrefsRepository.team = Team()
                    }
                })
        }
    }
}

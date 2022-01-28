package dal.mitacsgri.treecare.screens.createteam


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.extensions.validate
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.fragment_create_team.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateTeamFragment : Fragment() {

    private val mViewModel: CreateTeamViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_team, container, false)

        view.apply {

            toolbar.setNavigationOnClickListener {

                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(windowToken, 0)

                findNavController().navigateUp()
            }

            mViewModel.apply {
                isFullDataValid.observe(viewLifecycleOwner, Observer {
                    createTeamButton.isEnabled = it
                })

                messageLiveData.observe(viewLifecycleOwner, Observer {
                    it.toast(context)
                })

                inputTeamName.validate("Please enter the team name") {
                    isNameValid = it.isNotEmpty()
                    checkAllInputFieldsValid()
                    isNameValid
                }

                createTeamButton.setOnClickListener {
                    MainActivity.playClickSound()
                    createTeam(inputTeamName.text, inputTeamDescription.text) {
                        findNavController().navigateUp()
                    }
                }
            }
        }

        return view
    }
}

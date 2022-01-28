package dal.mitacsgri.treecare.screens.edittournament

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.consts.TOURNAMENT_TYPE_DAILY_GOAL_BASED
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.extensions.enable
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.extensions.validate
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.fragment_create_tournament.view.*
import kotlinx.android.synthetic.main.fragment_create_tournament.view.toolbar
import kotlinx.android.synthetic.main.fragment_edit_tournament.view.*
import org.joda.time.DateTime
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditTournamentFragment: Fragment() {

    val args: EditTournamentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? {
        val viewModel: EditTournamentViewModel by viewModel()
        val view = inflater.createFragmentViewWithStyle(
            activity,
            R.layout.fragment_edit_tournament,
            R.style.AppTheme
        )

        view.apply {
            toolbar.setNavigationOnClickListener {
                //for hiding keyboard while navigating back to the previous screen
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(windowToken, 0)

                findNavController().navigateUp()
            }

            editTournamentDescription.setText(args.tournamentDescription)
            editTournamentGoal.setText(args.tournamentGoal)
            editTournamentStartDate.setText(args.tournamentStartDate.replace("/", " / "))
            editTournamentEndDate.setText(args.tournamentEndDate.replace("/", " / "))

            viewModel.getStartDateText(args.tournamentStartDate.split("/").get(0).trim().toInt(),
                args.tournamentStartDate.split("/").get(1).trim().toInt() - 1,
            args.tournamentStartDate.split("/").get(2).trim().toInt())

            viewModel.getEndDateText(args.tournamentEndDate.split("/").get(0).trim().toInt(),
                args.tournamentEndDate.split("/").get(1).trim().toInt() - 1,
                args.tournamentEndDate.split("/").get(2).trim().toInt())

            viewModel.isFullDataValid.value = true
            viewModel.isEndDateValid = true
            viewModel.isGoalValid = true
            viewModel.isStartDateValid = true

            //saveTournamentButton.isEnabled = false

            viewModel.messageLiveData.observe(viewLifecycleOwner, {
                if (!viewModel.messageDisplayed) {
                    it.toast(context)
                    viewModel.messageDisplayed = true
                }
            })

            viewModel.isFullDataValid.observe(viewLifecycleOwner, {
                saveTournamentButton.isEnabled = it
            })

            editTournamentStartDate.apply {
                inputType = InputType.TYPE_NULL
                setOnClickListener {
                    val (day, month, year) = viewModel.getCurrentDateDestructured2()
                    val datePickerDialog = DatePickerDialog(
                        context,
                        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                            setText(viewModel.getStartDateText(year, monthOfYear, dayOfMonth))
                        }, year, month, day
                    )
                    datePickerDialog.datePicker.minDate = DateTime().millis
                    datePickerDialog.show()
                }
            }

            editTournamentEndDate.apply {
                inputType = InputType.TYPE_NULL
                setOnClickListener {
                    val (day, month, year) = viewModel.getCurrentDateDestructured()
                    val datePickerDialog = DatePickerDialog(
                        context,
                        { _, year, monthOfYear, dayOfMonth ->
                            setText(viewModel.getEndDateText(year, monthOfYear, dayOfMonth))
                        }, year, month, day
                    )
                    datePickerDialog.datePicker.minDate = DateTime().plusDays(1).millis
                    datePickerDialog.show()
                }
            }

            editTournamentGoal.validate("Goal should be a multiple of 1000 greater than 9000") {
                viewModel.isGoalValid = it.matches(viewModel.getRegexToMatchStepsGoal())
                viewModel.areAllInputFieldsValid()
                viewModel.isGoalValid
            }

            editTournamentStartDate.validate("Please provide a Tournament Start date") {
                viewModel.isStartDateValid = it.isNotEmpty()
                viewModel.areAllInputFieldsValid()
                viewModel.isStartDateValid
            }

            editTournamentEndDate.validate("Please provide a Tournament end date") {
                viewModel.isEndDateValid = it.isNotEmpty()
                viewModel.areAllInputFieldsValid()
                viewModel.isEndDateValid
            }

            saveTournamentButton.setOnClickListener {
                MainActivity.playClickSound()
                Log.d("Test", "FD" + viewModel.isFullDataValid.value)
                if (viewModel.isFullDataValid.value!!
                    && viewModel.checkDateFormat(editTournamentStartDate.length())
                    && viewModel.checkDateFormat(editTournamentEndDate.length())
                ) {
                    viewModel.editTournament(
                        description = editTournamentDescription.text,
                        goal = editTournamentGoal.text,
                        args.tournamentName)
                    findNavController().navigateUp()
                }

                else {
                    viewModel.messageLiveData.value =
                        "Please check the data you've entered and try again"
                }
            }
        }
        return view
    }
}
package dal.mitacsgri.treecare.screens.edittournamentafterstart

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.extensions.validate
import kotlinx.android.synthetic.main.fragment_create_tournament.view.toolbar
import kotlinx.android.synthetic.main.fragment_edit_tournament.view.*
import kotlinx.android.synthetic.main.fragment_edit_tournament_after_start.view.*
import org.joda.time.DateTime
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditTournamentAfterStartFragment:Fragment() {

    val args: EditTournamentAfterStartFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? {
        val viewModel: EditTournamentAfterStartViewModel by viewModel()
        val view = inflater.createFragmentViewWithStyle(
            activity,
            R.layout.fragment_edit_tournament_after_start,
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

            tournamentEndDate.setText(args.tournamentEndDate.replace("/", " / "))

            viewModel.getEndDateText(args.tournamentEndDate.split("/").get(0).trim().toInt(),
                args.tournamentEndDate.split("/").get(1).trim().toInt() - 1,
                args.tournamentEndDate.split("/").get(2).trim().toInt())
            viewModel.isEndDateValid = true
            viewModel.isFullDataValid.value = true

            //saveDateButton.isEnabled = false

            tournamentEndDate.apply {
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

            tournamentEndDate.validate("Please provide a Tournament end date") {
                viewModel.isEndDateValid = it.isNotEmpty()
                viewModel.areAllInputFieldsValid()
                viewModel.isEndDateValid
            }

            viewModel.messageLiveData.observe(viewLifecycleOwner, {
                if (!viewModel.messageDisplayed) {
                    it.toast(context)
                    viewModel.messageDisplayed = true
                }
            })

            viewModel.isFullDataValid.observe(viewLifecycleOwner, {
                saveDateButton.isEnabled = it
            })

            saveDateButton.setOnClickListener {
                viewModel.updateEndDate(args.tournamentName)
                findNavController().navigateUp()
            }
        }
        return view
    }
}
package dal.mitacsgri.treecare.screens.createtournament

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.consts.TOURNAMENT_TYPE_DAILY_GOAL_BASED
import dal.mitacsgri.treecare.extensions.*
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.fragment_create_tournament.view.*
import kotlinx.android.synthetic.main.fragment_create_tournament.view.inputTournamentName
import kotlinx.android.synthetic.main.fragment_create_tournament.view.toolbar
import org.joda.time.DateTime
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Anirudh on 23-04-2020
 */
class CreateTournamentFragment : Fragment() {

    val args: CreateTournamentFragmentArgs by navArgs()

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val status = args.status
//        val tournamentName = args.tournamentName ?:""
//    }


    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            with(savedInstanceState) {
                Log.d("Test", "InsideOncreate ")
                inputTournamentName.setText(getString("tourneyName"))
            }
        } else {
            Log.d("Test", "LiveData" + mViewModel.tourneyName.value.toString())
            inputTournamentName.setText(tourney)
        }
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: CreateTournamentViewModel by viewModel()
        val view = inflater.createFragmentViewWithStyle(
            activity,
            R.layout.fragment_create_tournament,
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

            createTournamentButton.disable()

            viewModel.messageLiveData.observe(viewLifecycleOwner, Observer {
                if (!viewModel.messageDisplayed) {
                    it.toast(context)
                    viewModel.messageDisplayed = true
                }
            })

            viewModel.isFullDataValid.observe(viewLifecycleOwner, Observer {
                createTournamentButton.isEnabled = it
            })

            if (args.status == true) {
                inputTournamentName.setText(args.tournamentName)
                inputTournamentDescription.setText(args.tournamentDescription)
                inputTournamentGoal.setText(args.goal)
                inputTournamentStartDate.setText(args.startDate)
                Log.d("Test", "start Date" + args.startDate)
                Log.d("Test", "end Date" + args.endDate)
                viewModel.getStartDateText( //for storing date in viewModel
                    args.startDate.split("/").get(2).trim().toInt(),
                    args.startDate.split("/").get(1).trim().toInt() - 1,
                    args.startDate.split("/").get(0).trim().toInt()
                )
                inputTournamentEndDate.setText(args.endDate)                                                                                                         //Subtracting 1 from month since we're reinitialising start and end date variable after navigating from enroll fragment. Else month would be set set as next month
                viewModel.getEndDateText( //for storing date in viewModel
                    args.endDate.split("/").get(2).trim().toInt(),
                    args.endDate.split("/").get(1).trim().toInt() - 1,
                    args.endDate.split("/").get(0).trim().toInt()
                )
                inputTeamLimit.setText(args.teamsLimit)
                viewModel.isFullDataValid.value = true
                createTournamentButton.enable()
            }

            inputTournamentStartDate.apply {
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

            viewModel.buttonVis.observe(viewLifecycleOwner, Observer {
                if (it) AddTeamsButton.visibility = View.VISIBLE
                else AddTeamsButton.visibility = View.INVISIBLE
            })

            inputTournamentEndDate.apply {
                inputType = InputType.TYPE_NULL
                setOnClickListener {
                    val (day, month, year) = viewModel.getCurrentDateDestructured()
                    val datePickerDialog = DatePickerDialog(
                        context,
                        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                            setText(viewModel.getEndDateText(year, monthOfYear, dayOfMonth))
                        }, year, month, day
                    )
                    datePickerDialog.datePicker.minDate = DateTime().plusDays(1).millis
                    datePickerDialog.show()
                }
            }


            inputTournamentName.validate("Please enter tournament name (Should not contain ':')") {
                viewModel.isNameValid = it.isNotEmpty() and !it.contains(':')
                viewModel.areAllInputFieldsValid()
                viewModel.isNameValid
            }

            inputTournamentGoal.validate("Goal should be a multiple of 1000 greater than 9000") {
                viewModel.isGoalValid = it.matches(viewModel.getRegexToMatchStepsGoal())
                viewModel.areAllInputFieldsValid()
                viewModel.isGoalValid
            }

            inputTeamLimit.validate("Number of teams should be between 2 to 10") {
                viewModel.isTeamSizeValid = it.matches(viewModel.getRegexToMatchTeamSize())
                viewModel.areAllInputFieldsValid()
                viewModel.isTeamSizeValid
            }

            inputTournamentStartDate.validate("Please provide a Tournament Start date") {
                viewModel.isStartDateValid = it.isNotEmpty()
                viewModel.areAllInputFieldsValid()
                viewModel.isStartDateValid
            }

            inputTournamentEndDate.validate("Please provide a Tournament end date") {
                viewModel.isEndDateValid = it.isNotEmpty()
                viewModel.areAllInputFieldsValid()
                viewModel.isEndDateValid
            }

            //viewModel.checkCaptaincy()

            AddTeamsButton.setOnClickListener {
                val action = CreateTournamentFragmentDirections
                    .actionCreateTournamentFragmentToEnrollTeamsFragment(
                        inputTournamentName.text.toString(),
                        inputTournamentDescription.text.toString(),
                        inputTournamentGoal.text.toString(),
                        inputTournamentEndDate.text.toString(),
                        inputTeamLimit.text.toString(),
                        inputTournamentStartDate.text.toString()
                    )
                if (viewModel.areAllInputFieldsValid()) findNavController().navigate(action)
            }

            createTournamentButton.setOnClickListener {
                MainActivity.playClickSound()
                Log.d("Test", "FD" + viewModel.isFullDataValid.value)
                if (viewModel.isFullDataValid.value!!
                    && viewModel.checkDataFormat(inputTournamentStartDate.length())
                    && viewModel.checkDataFormat(inputTournamentEndDate.length())
                ) {

                    Log.d("BTest", "CFrag " + args.teamsToAdd?.toList().toString())
                    viewModel.createTournament(
                        name = inputTournamentName.text,
                        description = inputTournamentDescription.text,
                        type = TOURNAMENT_TYPE_DAILY_GOAL_BASED,
                        goal = inputTournamentGoal.text,
                        teamLimit = inputTeamLimit.text,
                        teams = args.teamsToAdd
                    ) {

                        val imm =
                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(windowToken, 0)

                        findNavController().navigateUp()
                    }
                } else {
                    viewModel.messageLiveData.value =
                        "Please check the data you've entered and try again"
                }
            }
        }
        return view
    }
}
//    override fun onSaveInstanceState(outState: Bundle) {
//
//        outState.run {
//            putString("tourneyName",inputTournamentName.text.toString())
//        }
//        //Log.d("Test","InsideSaveinstance ")
//        super.onSaveInstanceState(outState)
//    }


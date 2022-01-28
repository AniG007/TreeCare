package dal.mitacsgri.treecare.screens.profile

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.dialog_edit_name.view.*
import kotlinx.android.synthetic.main.fragment_leaderboard.view.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {

    private val mViewModel: ProfileViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val animationDuration: Long = 200
        sharedElementEnterTransition = ChangeBounds().apply {
            duration = animationDuration
        }
        sharedElementReturnTransition = ChangeBounds().apply {
            duration = animationDuration
        }

        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        view.apply {
            Glide.with(this)
                .load(mViewModel.getUserPhotoUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(profileImageView)

            nameTV.text = mViewModel.getUserFirstName()

            val parent = backButton.parent as View // button: the view you want to enlarge hit area

            parent.post {
                val rect = Rect()
                backButton.getHitRect(rect)
                rect.top -= 100 // increase top hit area
                rect.left -= 100 // increase left hit area
                rect.bottom += 100 // increase bottom hit area
                rect.right += 100 // increase right hit area
                parent.touchDelegate = TouchDelegate(rect, backButton)
            }

            backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            logOutButton.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.logout_title))
                    .setMessage(context.getString(R.string.logout_message))
                    .setPositiveButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setNegativeButton("Yes") { _, _ ->
                        mViewModel.logout(context)
                        activity?.finish()
                        startActivity(Intent(activity, MainActivity::class.java))
                        "Logged out".toast(context)
                    }
                    .show()
            }

            buttonEdit.setOnClickListener {
                MainActivity.playClickSound()
                //findNavController().navigate(R.id.action_profileFragment_to_editNameDialog)
                createEditNameDialog()
            }

            buttonProgressReport.setOnClickListener {
                MainActivity.playClickSound()
                findNavController().navigate(R.id.action_profileFragment_to_progressReportFragment)
            }

            showGoalAchievedIcons(this, mViewModel.getWeeklyDailyGoalAchievedCount())

            streakDescriptionTV.text = mViewModel.getDailyGoalStreakText()

            mViewModel.trophiesCountData.observe(viewLifecycleOwner, Observer {
                goldAwardCountTV.text = it.first.toString()
                silverAwardCountTV.text = it.second.toString()
                bronzeAwardCount.text = it.third.toString()
            })

            challengerModeButtonHolder.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_challengesFragment)
            }

            tournamentModeButtonHolder.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_tournamentModeFragment)
            }

            mViewModel.teamTrophiesCountData.observe(viewLifecycleOwner, Observer {
                goldTeamAwardCountTV.text = it.first.toString()
                silverTeamAwardCountTV.text = it.second.toString()
                bronzeTeamAwardCount.text = it.third.toString()
            })

            mViewModel.getTrophiesCount()
            mViewModel.getTeamTrophiesCount()
        }

        return view
    }

    private fun showGoalAchievedIcons(view: View, dailyGoalAchievedCount: Int) {
        view.apply {
            val streakImages = arrayOf(streakImage1, streakImage2, streakImage3,
                streakImage4, streakImage5, streakImage6, streakImage7)

            streakImages.forEach {
                it.visibility = View.GONE
            }

            for (i in 0 until dailyGoalAchievedCount) {
                streakImages[i].visibility = View.VISIBLE
            }
            
        }
    }

    private fun createEditNameDialog() {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_name, null)
        val usernameEditText = dialogView.usernameInput.editText

        usernameEditText?.setText(nameTV.text)

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Save") { dialogInterface: DialogInterface, i: Int ->
                val newName = usernameEditText?.text.toString()
                mViewModel.updateUserName(newName) {
                    nameTV.text = newName
                }
            }
            .setNegativeButton("Back") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }.create()

        alertDialog.setOnShowListener {
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.apply {
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(ContextCompat.getColor(alertDialog.context, R.color.colorPrimary))
            }

            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.apply {
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(ContextCompat.getColor(alertDialog.context, R.color.colorPrimary))
            }
        }

        alertDialog.show()


    }

}

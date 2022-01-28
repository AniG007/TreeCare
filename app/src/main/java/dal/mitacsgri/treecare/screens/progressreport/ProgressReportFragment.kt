package dal.mitacsgri.treecare.screens.progressreport

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.fragment_leaderboard.view.*
import kotlinx.android.synthetic.main.fragment_progress_report.view.*

class ProgressReportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress_report, container, false)

        view.apply {
            viewPager.adapter = ProgressReportPagerAdapter(childFragmentManager, lifecycle)
            //tabLayout.setupWithViewPager(viewPager)

            TabLayoutMediator(tabLayout, viewPager){tab,position->
                when(position){
                    0 -> tab.text = "Week"
                    1 -> tab.text = "Month"
                    else -> tab.text = ""
                }
            }.attach()

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
                try {
                    findNavController().navigateUp()
                } catch (e: IllegalStateException) {
                    activity?.onBackPressed()
                }
            }
        }
        return view
    }
}

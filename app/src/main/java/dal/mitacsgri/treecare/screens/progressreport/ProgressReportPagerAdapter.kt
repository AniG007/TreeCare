package dal.mitacsgri.treecare.screens.progressreport

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProgressReportPagerAdapter(fm: FragmentManager, lf: Lifecycle):
    FragmentStateAdapter(fm, lf) {

    /*override fun getItem(position: Int): Fragment = ProgressReportHolderFragment.newInstance(
        when(position) {
            0 -> ProgressReportHolderFragment.WEEK_DATA
            1 -> ProgressReportHolderFragment.MONTH_DATA
            else -> 0
        }
    )*/

    override fun getItemCount(): Int = 2

    /*override fun getPageTitle(position: Int) =
        when(position) {
            0 -> "Week"
            1 -> "Month"
            else -> ""
        }*/


    override fun createFragment(position: Int): Fragment {
        return  ProgressReportHolderFragment.newInstance(
            when(position) {
                0 -> ProgressReportHolderFragment.WEEK_DATA
                1 -> ProgressReportHolderFragment.MONTH_DATA
                else -> 0
            }
        )
    }
}
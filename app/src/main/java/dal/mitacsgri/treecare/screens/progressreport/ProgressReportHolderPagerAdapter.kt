package dal.mitacsgri.treecare.screens.progressreport

import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.screens.progressreport.ProgressReportHolderFragment.Companion.MONTH_DATA
import dal.mitacsgri.treecare.screens.progressreport.progressreportdata.ProgressReportDataFragment
import dal.mitacsgri.treecare.screens.progressreport.progressreportdata.ProgressReportDataFragment.Companion.WEEK_DATA
import getStartOfMonth
import getStartOfWeek
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject

class ProgressReportHolderPagerAdapter(fm: FragmentManager, private val reportType: Long, lf: Lifecycle)
    : FragmentStateAdapter(fm, lf), KoinComponent {

    private val sharedPrefRepository: SharedPreferencesRepository by inject()

    private var mCurrentFragment =
        ProgressReportDataFragment()
    private var position1ShownOnce = false
    private var position0ShownOnce = false

    /*override fun getItem(position: Int): Fragment {
        return ProgressReportDataFragment.newInstance(
            reportType,
            getStartDateForPosition(reportType, position)
        )
    }*/

    override fun getItemCount(): Int {
        val firstLoginDate = DateTime(sharedPrefRepository.user.firstLoginTime)
        val currentDate = DateTime()

        return when(reportType) {
            WEEK_DATA -> {
                val startWeek = firstLoginDate.weekOfWeekyear
                val currentWeek = currentDate.weekOfWeekyear

                Log.d("DateStartWeek"," $startWeek")
                Log.d("DateCurrentWeek"," $currentWeek")

                currentWeek - startWeek + 1
            }
            MONTH_DATA -> {
                val startMonth = firstLoginDate.monthOfYear
                val currentMonth = currentDate.monthOfYear

                Log.d("DateStartMonth"," $startMonth")
                Log.d("DateCurrentMonth"," $currentMonth")

                currentMonth - startMonth + 1
            }
            else -> 0
        }
    }

    override fun createFragment(position: Int): Fragment =
        ProgressReportDataFragment.newInstance(
            reportType,
            getStartDateForPosition(reportType, position)
        )

   /* override fun setPrimaryItem(container: ViewGroup, position: Int, any: Any) {
        if (mCurrentFragment != any) {
            when(position) {
                0 -> if (!position0ShownOnce) {
                    position0ShownOnce = true
                    reanimateFragmentChart()
                }

                1 -> if (!position1ShownOnce) {
                    position1ShownOnce = true
                    reanimateFragmentChart()
                }
            }
        }
        super.setPrimaryItem(container, position, any)
    }*/

    private fun reanimateFragmentChart() {
        mCurrentFragment.reanimateBarChart(1000)
    }

    private fun getStartDateForPosition(reportType: Long, position: Int): DateTime {
        val itemCount = itemCount
        val currentStartDateMillis =
            if (reportType == WEEK_DATA) getStartOfWeek(DateTime().millis) else getStartOfMonth(DateTime().millis)
        val currentStartDate = DateTime(currentStartDateMillis)

        val diff = itemCount - position - 1

        return if (reportType == WEEK_DATA) currentStartDate.minusWeeks(diff) else currentStartDate.minusMonths(diff)
    }
}
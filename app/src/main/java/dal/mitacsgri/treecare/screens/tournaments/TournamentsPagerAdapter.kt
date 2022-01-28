package dal.mitacsgri.treecare.screens.tournaments

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import dal.mitacsgri.treecare.screens.tournaments.activetournaments.ActiveTournamentsFragment
import dal.mitacsgri.treecare.screens.tournaments.currenttournaments.CurrentTournamentsFragment
import dal.mitacsgri.treecare.screens.tournaments.mytournaments.MyTournamentsFragment

class TournamentsPagerAdapter(fm: FragmentManager)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int) =
        when(position) {
            0 -> CurrentTournamentsFragment()
            1 -> ActiveTournamentsFragment()
            2 -> MyTournamentsFragment()
            else -> ActiveTournamentsFragment()
        }

    override fun getCount() = 3

    override fun getPageTitle(position: Int) =
            when(position) {
                0 -> "Current\nTournaments"
                1 -> "Active\nTournaments"
                2 -> "My\nTournaments"
                else -> ""
            }
}
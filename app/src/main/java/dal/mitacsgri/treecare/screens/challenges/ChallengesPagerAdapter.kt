package dal.mitacsgri.treecare.screens.challenges

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import dal.mitacsgri.treecare.screens.challenges.activechallenges.ActiveChallengesFragment
import dal.mitacsgri.treecare.screens.challenges.challengesbyyou.ChallengesByYouFragment
import dal.mitacsgri.treecare.screens.challenges.currentchallenges.CurrentChallengesFragment

/**
 * Created by Devansh on 25-06-2019
 */

class ChallengesPagerAdapter(fm: FragmentManager)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int) =
        when(position) {
            0 -> CurrentChallengesFragment()
            1 -> ActiveChallengesFragment()
            2 -> ChallengesByYouFragment()
            else -> ActiveChallengesFragment()
        }

    override fun getCount() = 3

    override fun getPageTitle(position: Int) =
            when(position) {
                0 -> "Current\nChallenges"
                1 -> "Active\nChallenges"
                2 -> "My\nChallenges"
                else -> "Challenge"
            }
}
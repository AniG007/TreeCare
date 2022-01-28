package dal.mitacsgri.treecare.screens.invitesrequests

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import dal.mitacsgri.treecare.screens.invitesrequests.invites.InvitesFragment
import dal.mitacsgri.treecare.screens.invitesrequests.request.RequestFragment

class InvitesRequestPagerAdapter(fm: FragmentManager)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int) =
        when(position) {
            0 -> InvitesFragment()
            1 -> RequestFragment()
            else -> InvitesFragment()
        }

    override fun getCount() = 2

    override fun getPageTitle(position: Int) =
        when(position) {
            0 -> "Team Invites"
            1 -> "Team Join Requests"
            else -> ""
        }
}
package dal.mitacsgri.treecare.screens.tournaments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import kotlinx.android.synthetic.main.fragment_challenges.view.*
import kotlinx.android.synthetic.main.fragment_my_tournaments.view.*
import kotlinx.android.synthetic.main.fragment_tournaments.view.*
import kotlinx.android.synthetic.main.fragment_tournaments.view.tabLayout
import kotlinx.android.synthetic.main.fragment_tournaments.view.toolbar
import kotlinx.android.synthetic.main.fragment_tournaments.view.viewPager
import org.koin.androidx.viewmodel.ext.android.viewModel


class TournamentsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.createFragmentViewWithStyle(
            activity, R.layout.fragment_tournaments, R.style.tournament_mode_theme)

        view.apply {
            viewPager.adapter = TournamentsPagerAdapter(childFragmentManager)

            viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    when(position) {
                        0 -> fabCreateTournament.show()
                        1 -> fabCreateTournament.show()
                    }
                }
            })

            tabLayout.setupWithViewPager(viewPager)
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            fabCreateTournament.setOnClickListener{
                findNavController().navigate(R.id.action_tournamentsFragment_to_createTournamentFragment)
            }
        }
        return view
    }
}

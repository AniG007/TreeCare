package dal.mitacsgri.treecare.screens.challenges


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import dal.mitacsgri.treecare.R
import kotlinx.android.synthetic.main.fragment_challenges.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChallengesFragment : Fragment() {

    private val mViewModel: ChallengesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view = inflater.createFragmentViewWithStyle(
//            activity, R.layout.fragment_challenges, R.style.challenger_mode_theme)
        val view = inflater.inflate(R.layout.fragment_challenges, container, false)

        view.apply {
            //Use fragment's fragment manager instead of the activity's fragment manager
            viewPager.adapter = ChallengesPagerAdapter(childFragmentManager)
            viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }


                override fun onPageSelected(position: Int) {
                    when(position) {
                        0 -> fabCreateChallenge.hide()
                        1 -> fabCreateChallenge.show()
                        2 -> fabCreateChallenge.show()
                    }
                }
            })

            if (viewPager.currentItem == 0) {
                fabCreateChallenge.hide()
            }

            tabLayout.setupWithViewPager(viewPager)
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            fabCreateChallenge.setOnClickListener {
                findNavController().navigate(R.id.action_challengesFragment_to_createChallengeFragment)
            }
        }

        return view
    }
}

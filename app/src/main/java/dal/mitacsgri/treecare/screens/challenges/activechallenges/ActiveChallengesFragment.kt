package dal.mitacsgri.treecare.screens.challenges.activechallenges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.screens.challenges.ChallengesViewModel
import dal.mitacsgri.treecare.screens.challenges.challengesbyyou.ChallengesByYouRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_active_challenges.view.*
import kotlinx.android.synthetic.main.fragment_my_tournaments.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ActiveChallengesFragment : Fragment() {

    private val mViewModel: ChallengesViewModel by sharedViewModel()

    lateinit var adapter: ActiveChallengesRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view = inflater.createFragmentViewWithStyle(
//            activity, R.layout.fragment_active_challenges, R.style.challenger_mode_theme)
        val view = inflater.inflate(R.layout.fragment_active_challenges, container, false)

        view.apply {

            mViewModel.getAllActiveChallenges().observe(viewLifecycleOwner, Observer {
                //There should be a better approach to this
                //view.recyclerView.adapter = ActiveChallengesRecyclerViewAdapter(it, mViewModel)
                recyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = ActiveChallengesRecyclerViewAdapter(
                        mViewModel.activeChallengesList.value!!, mViewModel)
                }

                adapter = recyclerView.adapter as ActiveChallengesRecyclerViewAdapter

                if(adapter.itemCount == 0){
                    empty_view.visibility = View.VISIBLE
                }
                else{
                    empty_view.visibility = View.INVISIBLE
                }
            })

            mViewModel.statusMessage.observe(viewLifecycleOwner, Observer {
                if (!mViewModel.messageDisplayed) {
                    it.toast(view.context)
                    mViewModel.messageDisplayed = true
                }
            })

            //mViewModel.getAllActiveChallenges()

        }
        return view
    }
}

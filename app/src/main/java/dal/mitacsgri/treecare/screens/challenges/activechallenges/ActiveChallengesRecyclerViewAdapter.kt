package dal.mitacsgri.treecare.screens.challenges.activechallenges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Challenge
import dal.mitacsgri.treecare.screens.challenges.ChallengesViewModel

/**
 * Created by Devansh on 25-06-2019
 */

class ActiveChallengesRecyclerViewAdapter(
    private val activeChallengesList: List<Challenge>,
    private val viewModel: ChallengesViewModel
    ): RecyclerView.Adapter<ActiveChallengesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ActiveChallengesViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_challenge, parent, false),
            viewModel)

    override fun getItemCount() = activeChallengesList.size

    override fun onBindViewHolder(holder: ActiveChallengesViewHolder, position: Int) {
        holder.bind(activeChallengesList[position])
    }
}
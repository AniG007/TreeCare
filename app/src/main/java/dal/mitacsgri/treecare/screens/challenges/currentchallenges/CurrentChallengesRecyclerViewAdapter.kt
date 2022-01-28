package dal.mitacsgri.treecare.screens.challenges.currentchallenges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Challenge
import dal.mitacsgri.treecare.screens.challenges.ChallengesViewModel

/**
 * Created by Devansh on 25-06-2019
 */

class CurrentChallengesRecyclerViewAdapter(
    private val challengesList: List<Challenge>,
    private val viewModel: ChallengesViewModel
    ) : RecyclerView.Adapter<CurrentChallengesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentChallengesViewHolder =
        CurrentChallengesViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_current_challenge, parent, false),
            viewModel)

    override fun getItemCount() = challengesList.size

    override fun onBindViewHolder(holder: CurrentChallengesViewHolder, position: Int) {
        holder.bind(challengesList[position])
    }
}
package dal.mitacsgri.treecare.screens.challenges.challengesbyyou

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.Challenge
import dal.mitacsgri.treecare.screens.challenges.ChallengesViewModel

/**
 * Created by Devansh on 28-06-2019
 */
class ChallengesByYouRecyclerViewAdapter(
    private val challengesList: List<Challenge>,
    private val viewModel: ChallengesViewModel
    ): RecyclerView.Adapter<ChallengesByYouViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengesByYouViewHolder =
        ChallengesByYouViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_challenge_by_you, parent, false), viewModel)

    override fun getItemCount() = challengesList.size

    override fun onBindViewHolder(holder: ChallengesByYouViewHolder, position: Int) {
        holder.bind(challengesList[position])
    }
}
package dal.mitacsgri.treecare.model

data class Challenger(
    val name: String,
    val uid: String,
    val photoUrl: String,
    val challengeGoalStreak: Int,
    val totalSteps: Int,
    val totalLeaves: Int
) {

    override fun equals(other: Any?) = if (other is Challenger) uid == other.uid else false

    override fun hashCode() = uid.length
}
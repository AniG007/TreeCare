package dal.mitacsgri.treecare.model

import com.google.firebase.Timestamp

/**
 * Created by Devansh on 25-06-2019
 */

data class Challenge(
    val name: String = "",
    val description: String = "",
    val creationTimestamp: Timestamp = Timestamp.now(),
    val finishTimestamp: Timestamp = Timestamp.now(),
    val type: Int = 0,
    val goal: Int = 5000,
    var players: ArrayList<String> = arrayListOf(),
    val exist: Boolean = true,
    val active: Boolean = true,
    val creatorName: String = "",
    val creatorUId: String = ""
) {
    override fun equals(other: Any?): Boolean {
        other as Challenge
        return (name == other.name)
    }

    override fun hashCode(): Int {
        return name.length
    }
}

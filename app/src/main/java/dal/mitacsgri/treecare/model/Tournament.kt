package dal.mitacsgri.treecare.model

import com.google.firebase.Timestamp

data class Tournament (
    val name: String = "",
    val dailyGoal: Int = 0,
    val description: String = "",
    val active: Boolean = true,
    val teams: ArrayList<String> = arrayListOf(),
    val creationTimestamp: Timestamp = Timestamp.now(),
    var startTimestamp: Timestamp = Timestamp.now(),
    var finishTimestamp: Timestamp = Timestamp.now(),
    val exist: Boolean = true,
    val creatorName: String = "",
    val creatorUId: String = "",
    val teamLimit: Int = 0,
    val lastEdited: Timestamp = Timestamp.now()
){

    override fun equals(other: Any?): Boolean {
    other as Tournament
    return (name == other.name)
}

override fun hashCode(): Int {
    return name.length
}
}
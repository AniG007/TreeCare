package dal.mitacsgri.treecare.model

import com.google.firebase.Timestamp

class TeamInfo (
    val uId : String,
    val teamName : String,
    val captainId: String,
    val userName: String = "",
    //val dailyStepsMap: MutableMap<String, Int> = mutableMapOf(),
    val stepsCount: Int,
    val photoUrl: String,
    val leavesCount: Int
    //var players: ArrayList<String> = arrayListOf()
    // val description: String = "",
    // val type :Int = 0,
) {
    override fun equals(other: Any?): Boolean {
        other as TeamInfo
        return (userName == other.userName)
    }

    override fun hashCode(): Int {
        return userName.length
    }
}
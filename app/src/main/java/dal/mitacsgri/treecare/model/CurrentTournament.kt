package dal.mitacsgri.treecare.model

import com.google.firebase.Timestamp

//Contains information about a particular team's performance in a tournament

class CurrentTournament (
    val name: String = "",
    val playerContributionsMap: MutableMap<String, Int> = mutableMapOf(),
    val joinTimestamp: Timestamp = Timestamp.now()
)
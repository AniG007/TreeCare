package dal.mitacsgri.treecare.model

import com.google.firebase.Timestamp

data class Team (
    var name: String = "",
    val description: String = "",
    val members: ArrayList<String> = arrayListOf(),
    val invitedMembers: ArrayList<String> = arrayListOf(),
    val joinRequests: ArrayList<String> = arrayListOf(),
    //val currentTournaments: ArrayList<String> = arrayListOf(),
    var currentTournaments: MutableMap<String, TeamTournament> = mutableMapOf(),
    var captain: String = "",
    val exist :Boolean = true,
    var captainName: String = "",
    val creationTimestamp: Timestamp = Timestamp.now(),
    val newCaptain: String = ""
)
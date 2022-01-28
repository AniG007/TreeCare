package dal.mitacsgri.treecare.model

import dal.mitacsgri.treecare.consts.JOIN_REQUEST

data class TeamRequest(
    val teamId: String = "",
    val requestType: Int = JOIN_REQUEST
)
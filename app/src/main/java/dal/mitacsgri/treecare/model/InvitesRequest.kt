package dal.mitacsgri.treecare.model

import com.google.firebase.Timestamp

class InvitesRequest (
    val userName: String = "",
   // val description: String = "",
   // val type :Int = 0,
    val teamName: String="",
    val photoUrl: String,
    val uId : String
    //var players: ArrayList<String> = arrayListOf()
    ) {
        override fun equals(other: Any?): Boolean {
            other as InvitesRequest
            return (userName == other.userName)

        }

        override fun hashCode(): Int {
            return userName.length
        }
    }

package dal.mitacsgri.treecare.repository

import android.util.Log
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dal.mitacsgri.treecare.backgroundtasks.workers.UpdateTeamDataWorker
import dal.mitacsgri.treecare.consts.*
import dal.mitacsgri.treecare.model.*
import dal.mitacsgri.treecare.screens.MainActivity
import org.joda.time.DateTime


/**
 * Created by Devansh on 22-06-2019
 *
 * Modified by Anirudh (Tournament and Teams) :)
 */
class FirestoreRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    fun getUserData(uid: String): Task<DocumentSnapshot> {
        val docRef = db.collection(COLLECTION_USERS).document(uid)
        return docRef.get()
    }

    fun getAllUserEmail(user: String):Task<QuerySnapshot>{
        return db.collection(COLLECTION_USERS).whereEqualTo("email",user).get()
    }
    fun countUsers(): Int {
        val users = db.collection(COLLECTION_USERS).toString()
        return users.count()
    }

    fun storeUser(user: User) = db.collection(COLLECTION_USERS).document(user.uid)
            .set(user, SetOptions.merge())

    fun updateUserData(userId: String, values: Map<String, Any>) =
        db.collection(COLLECTION_USERS).document(userId).update(values)

    /*fun grick(teamName: String, tourney: String) {

        val bir = db.runTransaction {
            val team = it.get(db.collection(COLLECTION_TEAMS).document(teamName)).toObject<Team>()
            //val tourneys = team.getDocumentReference("currentTournaments")
            val curr = team?.currentTournaments!![tourney]
            val stepMap = curr?.dailyStepsMap
            if(stepMap?.isEmpty()!!) {

            }
            else if(stepMap.keys.last().toString() == DateTime().withTimeAtStartOfDay().toString()){

            }
            else{

            }
        }
    }*/

    fun updateUserTournamentData(userId: String, values: Map<String, UserTournament?>) =
        db.collection(COLLECTION_USERS).document(userId).update(values)

    fun updateUserJoinRequestInUser(captainId:String, userId: String, teamName: String)=
        db.collection(COLLECTION_USERS).document(captainId).update(teamName,userId)

    fun getChallenge(id: String) = db.collection(COLLECTION_CHALLENGES).document(id).get()

    fun updateChallengeData(challengeName: String, values: Map<String, Any>) =
        db.collection(COLLECTION_CHALLENGES).document(challengeName).update(values)

    fun getAllActiveChallenges() = db.collection(COLLECTION_CHALLENGES).get()

    fun storeChallenge(challenge: Challenge, action: (status: Boolean) -> Unit) {
        db.collection(COLLECTION_CHALLENGES).document(challenge.name)
            .set(challenge)
            .addOnSuccessListener {
                action(true)
                Log.d("Challenge stored", challenge.toString())
            }
            .addOnFailureListener {
                action(false)
                Log.d("Challenge store failed ", it.toString() + "Challenge: $challenge")
            }
    }

    fun deleteUserFromChallengeDB(challenge: Challenge, userId: String) =
        db.collection(COLLECTION_CHALLENGES).document(challenge.name)
            .update("players", FieldValue.arrayRemove(userId))

//    fun deleteChallengeFromUserDB(userId: String, userChallenge: UserChallenge, userChallengeJson: String) =
//        db.collection(COLLECTION_USERS).document(userId)
//            .update(mapOf("currentChallenges.${userChallenge.name}" to userChallengeJson))

    fun deleteChallengeFromUserDB(userId: String, challengeName: String) =
        db.collection(COLLECTION_USERS).document(userId)
            .update(mapOf("currentChallenges.${challengeName}" to FieldValue.delete()))

    fun getAllChallengesCreatedByUser(userId: String) =
        db.collection(COLLECTION_CHALLENGES)
            .whereEqualTo("creatorUId", userId).get()

    fun setChallengeAsNonExist(challengeName: String) =
        db.collection(COLLECTION_CHALLENGES).document(challengeName)
            .update("exist", false)

    fun getAllActiveTournaments() = db.collection(COLLECTION_TOURNAMENTS).get() //To retrieve all tournaments

    fun getAllUsers() = db.collection(COLLECTION_USERS).get()

    fun getAllTrophies() = db.collection(COLLECTION_TROPHIES).get()

    fun getAllTournamentTrophies() = db.collection(COLLECTION_TOURNAMENT_TROPHIES).get()

    fun getTournament(id: String) = db.collection(COLLECTION_TOURNAMENTS).document(id).get()

    fun updateTournamentData(tournamentName: String, values: Map<String, Any>) =
        db.collection(COLLECTION_TOURNAMENTS).document(tournamentName).update(values)


    fun storeTournament(tournament: Tournament, action: (status: Boolean) -> Unit) {
        db.collection(COLLECTION_TOURNAMENTS).document(tournament.name)
            .set(tournament)
            .addOnSuccessListener {
                action(true)
                Log.d("Tournament stored", tournament.toString())
            }
            .addOnFailureListener {
                action(false)
                Log.d("Tournament storefailed ", it.toString() + "Tournament: $tournament")
            }
    }

    fun deleteUserFromTournamentDB(tournament: Tournament, userId: String) =
        db.collection(COLLECTION_TOURNAMENTS).document(tournament.name)
            .update("players", FieldValue.arrayRemove(userId))

    fun deleteTournamentFromUserDB (uid: String, tournamentname: String) =
        db.collection(COLLECTION_USERS).document(uid).update( mapOf("currentTournaments.${tournamentname}" to FieldValue.delete()))





//    fun deleteTournamentFromUserDB(userId: String, userTournament: UserTournament, userTournamentJson: String) =
//        db.collection(COLLECTION_USERS).document(userId)
//            .update(mapOf("currentTournament.${userTournament.name}" to userTournamentJson))

    fun getAllTournamentsCreatedByUser(userId: String) =
        db.collection(COLLECTION_TOURNAMENTS)
            .whereEqualTo("creatorUId", userId).get()

    fun setTournamentAsNonExist(tournamentName: String) =
        db.collection(COLLECTION_TOURNAMENTS).document(tournamentName)
            .update("exist", false)

    fun getAllTeams() = db.collection(COLLECTION_TEAMS).get()

    fun getAllCaptainedTeams(userId: String) = db.collection(COLLECTION_TEAMS)
        .whereEqualTo("captain", userId).get()

    fun getAllTeamsForUserAsMember(userId: String) = db.collection(COLLECTION_TEAMS)
        .whereArrayContains("members", userId).get()

    fun getTeam(teamName: String)  = db.collection(COLLECTION_TEAMS).document(teamName).get()


    fun storeTeam(team: Team, action: (status: Boolean) -> Unit) {
        db.collection(COLLECTION_TEAMS).document(team.name)
            .set(team)
            .addOnSuccessListener {
                action(true)
                Log.d("Team store success", "Team: $team")
            }
            .addOnFailureListener {
                action(false)
                Log.d("Team store failed", it.toString() + "Challenge: $team")
            }
    }

    fun updateTeamData(teamName: String, values: Map<String, Any>) =
        db.collection(COLLECTION_TEAMS).document(teamName).update(values)

    fun updateTeamTournamentData(teamName: String, values: Map<String, TeamTournament?>) =
        db.collection(COLLECTION_TEAMS).document(teamName).update(values)

    fun deleteTeamInvitesFromUser(userId:String, teamName :String) =
        db.collection(COLLECTION_USERS).document(userId)
            .update("teamInvites", FieldValue.arrayRemove(teamName))

    fun deleteTeamJoinRequestFromUser(userId:String, teamName :String) =
        db.collection(COLLECTION_USERS).document(userId)
            .update("teamJoinRequests", FieldValue.arrayRemove(teamName))

    fun deleteJoinRequestFromTeam (userId:String, teamName :String) =
        db.collection(COLLECTION_TEAMS).document(teamName)
            .update("joinRequests", FieldValue.arrayRemove(userId))

    fun deleteInvitedMemberFromTeam (userId:String, teamName :String) =
        db.collection(COLLECTION_TEAMS).document(teamName)
            .update("invitedMembers", FieldValue.arrayRemove(userId))

  /*  fun deleteUserJoinRequestFromTeam (userId:String, teamName :String) =
        db.collection(COLLECTION_TEAMS).document(teamName)
            .update("joinRequests", FieldValue.arrayRemove(userId))*/

    fun addTeamMember (userId:String, teamName :String) =
        db.collection(COLLECTION_TEAMS).document(teamName)
            .update("members", FieldValue.arrayUnion(userId))

    fun addCurrentTeams (userId: String, teamName: String) =
        db.collection(COLLECTION_USERS).document(userId)
            .update("currentTeams", FieldValue.arrayUnion(teamName))

    fun deleteTeam (teamName : String) =
        db.collection(COLLECTION_TEAMS).document(teamName)
            .update("exist", false)
    // TODO: Teams/ Challenges/ Tournaments are never deleted.

    fun deleteTournamentFromTeamDB (teamName: String, tournamentname: String) =
        db.collection(COLLECTION_TEAMS).document(teamName).update( mapOf("currentTournaments.${tournamentname}" to FieldValue.delete()))



    fun getTrophiesData(userId: String) =
            db.collection(COLLECTION_TROPHIES).document(userId).get()

    fun storeTrophiesData(userId: String, trophies: UserChallengeTrophies) =
            db.collection(COLLECTION_TROPHIES).document(userId).set(trophies)

    fun getTeamTrophiesData(teamName: String) =
        db.collection(COLLECTION_TOURNAMENT_TROPHIES).document(teamName).get()

    fun storeTeamTrophiesData(teamName: String, trophies: UserTournamentTrophies) =
        db.collection(COLLECTION_TOURNAMENT_TROPHIES).document(teamName).set(trophies)


    fun changeUserName(userId: String, newName: String) =
            db.collection(COLLECTION_USERS).document(userId)
                .update(mapOf("name" to newName))
}
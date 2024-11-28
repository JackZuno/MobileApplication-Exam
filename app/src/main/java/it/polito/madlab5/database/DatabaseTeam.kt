package it.polito.madlab5.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Team.Achievement
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.screens.TeamScreen.LoadingState
import it.polito.madlab5.screens.TeamScreen.generateInviteLink
import it.polito.madlab5.viewModel.TeamViewModels.memberStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DatabaseTeam() {

    val db = Firebase.firestore

    fun getTeams(): Flow<Pair<LoadingState, List<Team>>> = callbackFlow {
        val listener = db.collection("teams").addSnapshotListener { r, e ->
            if (r != null) {
                val teams = r.documents.mapNotNull { document ->
                    val team = document.toObject(Team::class.java)
                    team?.teamID = document.id
                    team
                }

                if (teams.isEmpty()) {
                    trySend(Pair(LoadingState.EMPTY, teams))    //trySend(teams)
                } else {
                    // Launch a coroutine to fetch images
                    val scope = CoroutineScope(Job() + Dispatchers.IO)
                    scope.launch {
                        try {
                            fetchImagesForTeams(teams)
                            trySend(Pair(LoadingState.SUCCESS, teams))  //trySend(teams)
                        } catch (e: Exception) {
                            Log.e("ERROR", "Error fetching images", e)
                            trySend(Pair(LoadingState.EMPTY, emptyList()))    //trySend(teams)
                        }
                    }
                }
            } else {
                Log.e("ERROR", e.toString())
                trySend(Pair(LoadingState.EMPTY, emptyList()))
            }
        }
        awaitClose {
            listener.remove()
        }
    }

    fun getPersonalTeams(profileId: String): Flow<Pair<LoadingState, List<Team>>> = callbackFlow {
        val personalTeam = getPersonalTeam(profileId)

        if (personalTeam.isEmpty()){
            trySend(Pair(LoadingState.EMPTY, personalTeam))
        } else {
            // Launch a coroutine to fetch images
            val scope = CoroutineScope(Job() + Dispatchers.IO)
            scope.launch {
                try {
                    fetchImagesForTeams(personalTeam)
                    trySend(Pair(LoadingState.SUCCESS, personalTeam))  //trySend(teams)
                } catch (e: Exception) {
                    Log.e("ERROR", "Error fetching images", e)
                    trySend(Pair(LoadingState.EMPTY, emptyList()))    //trySend(teams)
                }
            }
        }

        awaitClose{

        }

    }

    suspend fun fetchImagesForTeams(teams: List<Team>) {
        teams.forEach { team ->
            team.imageURL?.let { imageUrl ->
                team.image = fetchImage(imageUrl)
            }
        }
    }

    suspend fun fetchImage(imageUrl: String): Bitmap? = suspendCancellableCoroutine { continuation ->
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl(imageUrl)

        storageRef.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { byteArray ->
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                Log.d("RETRIEVE IMAGE", "Image retrieved successfully")
                continuation.resume(bitmap)
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase Storage", "Error downloading image", exception)
                continuation.resumeWithException(exception)
            }
    }
    data class AchievementsTmP(
        val achievements: List<Achievement> = listOf()
    )
    suspend fun getTeamAchievements(achievementId: String): MutableList<Achievement>  = suspendCancellableCoroutine { continuation ->

            val achievementsList = mutableListOf<Achievement>()
        GlobalScope.launch {
            try {
                val documentSnapshot = db.collection("achievements")
                    .document(achievementId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val achievements = documentSnapshot.get("achievement") as? List<Map<String, Any>>
                    achievements?.let {
                        for (achievementMap in it) {
                            val achievement = Achievement(
                                name = achievementMap["name"] as? String ?: "",
                                description = achievementMap["description"] as? String ?: "",
                                achievementState = achievementMap["achievementState"] as? String ?: ""
                            )
                            achievementsList.add(achievement)
                        }
                    }

                    continuation.resume(achievementsList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun updateTeamAchievements(achievementId: String, achievement: List<Achievement>) {
        db.collection("achievements").document(achievementId).update("achievement", AchievementsTmP(achievement).achievements)
    }



    suspend fun getPersonalTeam(profileId: String): List<Team> = suspendCancellableCoroutine { continuation ->
        val personalTeam = mutableListOf<Team>()
        val docRef = db.collection("profileTeam").whereEqualTo("profileId", profileId)

        GlobalScope.launch {
            try {
                val querySnapshot = docRef.get().await()
                val teamTasks = querySnapshot.documents.map {
                        profileTeam ->
                    db.collection("teams").document(profileTeam["teamId"].toString()).get()
                }

                teamTasks.forEach{ task ->
                    val document = task.await()
                    if (document.exists()){
                        Log.e("Test",document.data.toString())

                        val team = document.toObject(Team::class.java)
                        team?.teamID = document.id

                        //val team  =Team(document.id, task["name"].)

                        if (team != null){
                            personalTeam.add(team)
                        }
                    }
                }

                Log.d("RETRIEVE TEAMS", "TEAMS retrieved successfully")
                continuation.resume(personalTeam)
            }catch (e: Exception){
                Log.w("Firebase Storage", "Error retrieving teams", e)
                continuation.resumeWithException(e)
            }
        }

    }

    fun getTeamById(teamId: String): Flow<Pair<LoadingState, Team?>> = callbackFlow {
        val docRef = db.collection("teams").document(teamId)
        var team: Team? = null

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        val listenerRegistration = docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("TEAM", "DocumentSnapshot data: ${document.data}")
                    team = document.toObject(Team::class.java)
                    team?.teamID = document.id

                    scope.launch {
                        team?.let {
                            if (it.imageURL != null) {
                                it.image = fetchImage(it.imageURL!!)
                            }
                        }
                        trySend(Pair(LoadingState.SUCCESS, team))
                        close()
                    }
                } else {
                    Log.e("TEAM ERROR", "No such document")
                    trySend(Pair(LoadingState.EMPTY, null))
                    close()
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TEAM FAILURE", "get failed with ", exception)
                trySend(Pair(LoadingState.EMPTY, null))
                close()
            }

        awaitClose {

        }
    }

    /*fun getTeams(): Flow<List<Team>> = callbackFlow {
        val listener = db.collection("teams").addSnapshotListener { r, e ->
            if (r!=null){
                val teams = r.documents.map { document ->
                    val team = document.toObject(Team::class.java)
                    team?.teamID = document.id // Set the ID
                    if(team?.imageURL != null) {
                        getTeamImages(team)
                    }
                    team
                }.filterNotNull()
                trySend(teams)
            } else {
                Log.e("ERROR", e.toString())
                trySend(emptyList())
            }
        }
        awaitClose{
            listener.remove()
        }
    }

    fun getTeamImages(team: Team) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl(team.imageURL!!)

        val onSuccess = { byteArray: ByteArray ->
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            Log.d("RETRIEVE IMAGE", "Image retrieved successfully")
            team.image = bitmap
        }

        storageRef.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener {
                Log.w("Firebase Storage", "Error downloading image", it)
            }
    }*/

    /*fun getTeamById(teamId: String) = callbackFlow<Team?> {
        val docRef = db.collection("teams").document(teamId)
        var team: Team? = null

        val listenerRegistration = docRef.get()
            .addOnSuccessListener { document ->
                if (document != null){
                    Log.d("TEAM", "DocumentSnapshot data: ${document.data}")
                    team = document.toObject(Team::class.java)
                    team?.teamID = document.id
                    if(team?.imageURL != null) {
                        //getTeamImages(team!!)
                    }
                    trySend(team)
                } else {
                    Log.e("TEAM ERROR", "No such document")
                    trySend(null)
                }
                close()
            }
            .addOnFailureListener { exception ->
                Log.d("TEAM FAILURE", "get failed with ", exception)
                trySend(null)
                close()
            }

        awaitClose{

        }
    }*/




    fun addTeam(team: Team) {
        var urlImage: String? = null

        if(team.imageURL != null) {
            urlImage = team.imageURL.toString()
        }
        val achievement = hashMapOf<String, Any>(
            "achievement" to team.achievement
        )



        db.collection("achievements").add(achievement).addOnSuccessListener {
            /* Add the team */
            val teamUpdate = hashMapOf<String, Any?>(
                "category" to team.category,
                "creationDate" to team.creationDate,
                "description" to team.description,
                "image" to null,
                "imageURL" to urlImage,
                "name" to team.name,
                "teamAdmin" to team.teamAdmin,
                "achievementId" to it.id
            )
            db.collection("teams").add(teamUpdate)

                .addOnSuccessListener { documentReference ->
                    val data = hashMapOf<String, Any>(
                        "link" to generateInviteLink(
                            team,
                            documentReference.id
                        )
                    )
                    documentReference.update(data)

                    /* Add the relation TeamProfile */
                    val profileTeamRelationUpdate = hashMapOf<String, Any>(
                        "profileId" to team.teamAdmin,
                        /* TO FIX */
                        "teamChatId" to "",
                        "teamId" to documentReference.id,
                        "teamMemberStatus" to memberStatus.Erratic.name
                    )


                    db.collection("profileTeam").add(profileTeamRelationUpdate)
                        .addOnSuccessListener { profileTeam ->
                            Log.d(
                                "PROFILETEAM",
                                "Relation Profile Team inserted with ID: ${profileTeam.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e("PROFILETEAM", "Error adding document", e)
                        }

                    /* Add Also the teamChat*/
                    val teamChatMap = mapOf<String, Any>(
                        "profileId" to team.teamAdmin,
                        "teamId" to documentReference.id,
                        "lastAccess" to LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )

                    db.collection("teamChats").add(teamChatMap)
                        .addOnSuccessListener { teamChat ->
                            Log.d(
                                "TEAMCHAT",
                                "Relation Profile Team inserted with ID: ${teamChat.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e("TEAMCHAT", "Error adding document", e)
                        }

                    Log.d("TEAM", "Team inserted with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("TEAM", "Error adding document", e)
                }
        }
    }

    // Get teamMembers
    fun getTeamMemberStatus(teamId: String): Flow<SnapshotStateList<Pair<String, memberStatus>>> = callbackFlow {
        val listener = db.collection("profileTeam")
            .whereEqualTo("teamId", teamId)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val listMemberStatus = snapshot.documents.mapNotNull { document ->
                        val profileId = document.getString("profileId").toString()
                        val statusString = document.getString("teamMemberStatus")
                        val status = statusString?.let { memberStatus.valueOf(it) }

                        if(status != null) {
                            Pair(profileId, status)
                        } else {
                            null
                        }
                    }
                    trySend(listMemberStatus.toMutableStateList()).isSuccess
                } else {
                    Log.e("ERROR", error.toString())
                    trySend(emptyList<Pair<String, memberStatus>>().toMutableStateList()).isSuccess
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    fun updateTeamMemberStatus(teamId: String, profileId: String, newStatus: memberStatus) {
        db.collection("profileTeam")
            .whereEqualTo("teamId", teamId)
            .whereEqualTo("profileId", profileId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Handle case where the profile is not part of the team
                    Log.w("updateTeamMemberStatus", "Profile not found in team")
                } else {
                    val document = documents.documents[0]
                    db.collection("profileTeam").document(document.id)
                        .update("teamMemberStatus", newStatus.name)
                        .addOnSuccessListener {
                            Log.i("updateTeamMemberStatus", "Status updated successfully")
                        }
                        .addOnFailureListener { error ->
                            Log.e("updateTeamMemberStatus", "Error updating status", error)
                        }
                }
            }
            .addOnFailureListener { error ->
                Log.e("updateTeamMemberStatus", "Error getting profile team data", error)
            }
    }

    fun updateTeam(team: Team){
        var urlImage: String? = null

        if(team.imageURL != null) {
            urlImage = team.imageURL.toString()
        }

        val teamUpdate = hashMapOf<String, Any?>(
            "category" to team.category,
            "creationDate" to team.creationDate,
            "description" to team.description,
            "image" to null,
            "imageURL" to urlImage,
            "name" to team.name,
            "teamAdmin" to team.teamAdmin,
            "taskList" to team.taskList,
            "link" to generateInviteLink(team, team.teamID),
            "achievementId" to team.achievementId,
            "taskList" to emptyList<Task>(),
            "achievement" to emptyList<Achievement>()
        )
        Log.d("teamID", team.teamID)
        Log.d("teamURL", team.imageURL.toString())

        /* Update the team */
        db.collection("teams").document(team.teamID).update(teamUpdate)
            .addOnSuccessListener {
                    documentReference ->
                Log.d("TEAM", "Team updated with ID: ${team.teamID}")
            }
            .addOnFailureListener {
                    e ->
                Log.e("TEAM", "Error updating document", e)
            }
    }

    fun removeUserFromTeam(profileId: String, teamId: String) {
        db.collection("profileTeam")
            .whereEqualTo("profileId", profileId)
            .whereEqualTo("teamId", teamId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        document.reference.delete()
                            .addOnSuccessListener {
                                println("Member leaved successfully")
                            }
                            .addOnFailureListener { e ->
                                println("Error in leaving team ${e.message}")
                            }
                    }
                } else {
                    println("No match")
                }
            }
            .addOnFailureListener { e ->
                println("Error in finding relationship: ${e.message}")
            }
    }

    fun addUserToTeam(profileId: String, teamId: String) {
        val profileTeam = hashMapOf<String, Any?>(
            "profileId" to profileId,
            "teamId" to teamId,
            "teamMemberStatus" to "Erratic"
        )

        db.collection("profileTeam").add(profileTeam)
            .addOnSuccessListener { documentReference ->
                Log.d("PROFILETEAM", "Relation Profile Team inserted with ID: ${documentReference.id}")

                val profileChatTeam = hashMapOf<String, Any?>(
                    "profileId" to profileId,
                    "teamId" to teamId,
                    "lastAccess" to LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )

                db.collection("teamChats").add(profileChatTeam)
                    .addOnSuccessListener { docRef ->
                        Log.d("PROFILE CHAT TEAM", "Relation added with ID: ${docRef.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("PROFILE CHAT TEAM", "Error: $e")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("PROFILETEAM", "Error: $e")
            }
    }

}
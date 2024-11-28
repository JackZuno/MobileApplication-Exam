package it.polito.madlab5.database


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.toMutableStateList
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Task.TaskEffort
import it.polito.madlab5.model.Task.TaskStates
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.screens.TeamScreen.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DatabaseProfile() {
    val db = Firebase.firestore

    fun getProfileById(profileId: String, isLoading: MutableState<Boolean>): Flow<Profile?> = callbackFlow {
        isLoading.value = true
        val docRef = db.collection("profiles").document(profileId)
        var profile: Profile? = null

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        val listenerRegistration = docRef.get()
            .addOnSuccessListener { document ->
                if (document != null){
                    Log.d("PROFILE", "DocumentSnapshot data: ${document.id}")
                    profile = document.toObject(Profile::class.java)
                    profile?.id = profileId
                    scope.launch {
                        profile?.let {
                            if (it.imageURL != null) {
                                it.image = fetchImageProfile(it.imageURL!!)
                            }
                        }
                        trySend(profile)
                        isLoading.value = false
                        close()
                    }
                } else {
                    Log.e("PROFILE ERROR", "No such document")
                    trySend(null)
                    isLoading.value = false
                }
                close()
            }
            .addOnFailureListener { exception ->
                Log.d("PROFILE FAILURE", "get failed with ", exception)
                trySend(null).isSuccess
                close()
            }

        awaitClose{
            Log.d("PROFILE", "Listener closed for $profileId")
            isLoading.value = false
        }
    }

    fun getProfileByIdEdit(profileId: String): Flow<Pair<LoadingState, Profile?>> = callbackFlow {
        val docRef = db.collection("profiles").document(profileId)
        var profile: Profile? = null

        val scope = CoroutineScope(Job() + Dispatchers.IO)

        docRef.get()
            .addOnSuccessListener { document ->
                if(document != null) {
                    Log.d("PROFILE", "DocumentSnapshot data: ${document.id}")
                    profile = document.toObject(Profile::class.java)
                    profile?.id = profileId

                    scope.launch {
                        profile?.let {
                            if (it.imageURL != null) {
                                it.image = fetchImageProfile(it.imageURL!!)
                            }
                        }
                        trySend(Pair(LoadingState.SUCCESS, profile))
                        close()
                    }
                } else {
                    Log.e("PROFILE ERROR", "No such document")
                    trySend(Pair(LoadingState.EMPTY, null))
                }
            }
            .addOnFailureListener { exception ->
                Log.d("PROFILE FAILURE", "get failed with ", exception)
                trySend(Pair(LoadingState.EMPTY, null))
                close()
            }

        awaitClose{
            Log.d("PROFILE", "Listener closed for $profileId")
        }
    }

    suspend fun fetchImageProfile(imageUrl: String): Bitmap? = suspendCancellableCoroutine { continuation ->
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl(imageUrl)

        Log.d("RETRIEVE IMAGE PROFILE", "Retrieving image from URL: $imageUrl")

        storageRef.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { byteArray ->
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                Log.d("RETRIEVE IMAGE PROFILE", "Image retrieved successfully")
                continuation.resume(bitmap)
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase Storage", "Error downloading image", exception)
                continuation.resumeWithException(exception)
            }
    }

    fun getProfiles(): Flow<MutableList<Profile>> = callbackFlow {
        val profiles = mutableListOf<Profile>()

        db.collection("profiles").addSnapshotListener{ results, err ->
                if (results!=null){
                    results.documents.forEach { document ->
                        val skills = (document["skills"] as? MutableList<String>) ?: mutableListOf()
                        val profile = Profile(
                            id = document.id,
                            name = document["name"].toString(),
                            lastname = document["lastname"].toString(),
                            username = document["username"].toString(),
                            email = document["email"].toString(),
                            location = document["location"].toString(),
                            description = document["description"].toString(),
                            skills = skills,
                            joineddate = document["joineddate"].toString(),
                            image = null,
                            imageURL = document["imageURL"].toString(),
                            personalChats = mutableListOf()
                        )
                        profiles.add(profile)

                        trySend(profiles)
                    }

                }  else {
                    Log.e("PROFILE ERROR", "")
                }

            }

        awaitClose{
            Log.d("PROFILE", "Listener closed")
        }
    }

    fun getListProfileByIDs(profileIds: List<String>): Flow<List<Profile?>> = callbackFlow {
        val profiles = mutableListOf<Profile>()

        profileIds.forEach { profileId ->
            val listener = db.collection("profiles").document(profileId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profile = document.toObject(Profile::class.java)
                        profile?.id = document.id
                        profiles.add(profile!!)
                    } else {
                        Log.e("PROFILE ERROR", "No such document for ID: $profileId")
                    }
                    // Only send data when all profiles are fetched
                    if (profiles.size == profileIds.size) {
                        Log.d("OK RETRIEVED", profiles.size.toString())
                        trySend(profiles.toList()).isSuccess // Sending the list
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PROFILE FAILURE", "get failed with ", exception)
                    close(exception) // Close flow on failure
                }
        }

        awaitClose {}
    }

    fun getProfileByIdWithoutPhoto(profileId: String): Flow<Pair<LoadingState, Profile?>> = callbackFlow {
        val docRef = db.collection("profiles").document(profileId)
        var profile: Profile? = null


        docRef.get()
            .addOnSuccessListener { document ->
                if(document != null) {
                    Log.d("PROFILE", "DocumentSnapshot data: ${document.id}")
                    profile = document.toObject(Profile::class.java)
                    profile?.id = profileId

                    trySend(Pair(LoadingState.SUCCESS, profile))
                } else {
                    Log.e("PROFILE ERROR", "No such document")
                    trySend(Pair(LoadingState.SUCCESS, null))
                }
            }
            .addOnFailureListener { exception ->
                Log.d("PROFILE FAILURE", "get failed with ", exception)
                trySend(Pair(LoadingState.SUCCESS, null))
                close()
            }

        awaitClose{
            Log.d("PROFILE", "Listener closed for $profileId")
        }
    }

    fun getTeamMembersFromTeam(teamId: String) = callbackFlow<List<Profile>>{
        val profiles = mutableListOf<Profile>()

        val listener = db.collection("profileTeam").whereEqualTo("teamId", teamId)
            .addSnapshotListener{ result, err ->
                if (result!=null){
                    result.documents.forEach{ profileTeam ->
                        db.collection("profiles").document(profileTeam["profileId"].toString()).get()
                            .addOnSuccessListener{ document ->
                                if (document != null && document.exists()) {
                                    val profile = document.toObject(Profile::class.java)
                                    profile?.id = document.id
                                    profiles.add(profile!!)
                                } else {
                                    Log.e("PROFILE ERROR", "No such document for ID: $teamId")
                                }
                                trySend(profiles.toList()).isSuccess
                            }
                            .addOnFailureListener{
                                    exception ->
                                Log.e("PROFILE FAILURE", "get failed with ", exception)
                                close(exception) // Close flow on failure
                            }
                    }

                } else {
                    Log.e("PROFILE ERROR", "No such profile associated to ID: {$teamId}")
                }
            }

        awaitClose{
            listener.remove()
        }

    }

    suspend fun getProfilesFromListIds(profilesIds: List<String>) : Map<String, Profile?>{
        val profiles = mutableMapOf<String, Profile?>()

        coroutineScope {
            profilesIds.map { id ->
                async {
                    val doc = db.collection("profiles").document(id).get().await()
                    if (doc.exists()) {
                        profiles[id] = doc.toObject(Profile::class.java)
                    } else {
                        profiles[id] = null
                    }
                }
            }.awaitAll()
        }

        return profiles
    }

    fun getProfilesForChat(profilesIds: List<String>): Flow<List<Profile>> = callbackFlow {

        val profiles = getProfilesFromListIds(profilesIds).map { it.value }.filterNotNull()

        trySend(profiles)

        awaitClose {

        }
    }

    fun getAssignedTeamMembersTask(taskId: String): Flow<List<Profile>> = callbackFlow{
        val profiles = mutableListOf<Profile>()

        val listener = db.collection("profileTask").whereEqualTo("taskId", taskId)
            .addSnapshotListener{
                result, err ->
                if (result != null) {
                    result.documents.forEach{profileTask ->
                        db.collection("profiles").document(profileTask["profileId"].toString()).get()
                            .addOnSuccessListener{document ->
                                if (document != null && document.exists()) {
                                    val profile = document.toObject(Profile::class.java)
                                    profile?.id = document.id
                                    profiles.add(profile!!)
                                } else {
                                    Log.e("PROFILE FAILURE", "No such document for ID: ${taskId}" )
                                }
                                trySend(profiles.toList()).isSuccess
                            }
                            .addOnFailureListener{
                                    exception ->
                                Log.e("PROFILE FAILURE", "get failed with ", exception)
                                close(exception) // Close flow on failure
                            }
                    }
                } else {
                    Log.e("PROFILE ERROR", "No such profile associated to ID: {$taskId}")
                }
            }

        awaitClose{
            listener.remove()
        }
    }

}
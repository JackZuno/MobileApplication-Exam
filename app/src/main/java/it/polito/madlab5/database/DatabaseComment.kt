package it.polito.madlab5.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import it.polito.madlab5.model.Task.Comment
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Task.TaskEffort
import it.polito.madlab5.model.Task.TaskStates
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DatabaseComment {
    val db = Firebase.firestore

    fun getCommentsFromTask(taskId: String): Flow<MutableList<Comment>> = callbackFlow {

        val listener = db.collection("comments").whereEqualTo("taskId", taskId)
            .addSnapshotListener{
                    r, e ->
                if (r!= null){
                    val listComments = r.documents.mapNotNull {

                        Comment(
                            profileId = it["profileId"].toString(),
                            profileName = null,
                            taskId = it["takId"].toString(),
                            text = it["text"].toString(),
                            data = it["data"].toString()
                        )

                    }.sortedBy { it.data }
                    trySend(listComments.toMutableList()).isSuccess
                } else {
                    Log.e("TASK ERROR", e.toString())
                    trySend(emptyList<Comment>().toMutableList())
                }
            }

        awaitClose{
            listener.remove()
        }
    }

    fun addComment(comment: Comment) {
        val commentUpdate = hashMapOf<String, Any?>(
            "profileId" to comment.profileId,
            "profileName" to comment.profileName,
            "taskId" to comment.taskId,
            "text" to comment.text,
            "data" to comment.data
        )

        /* Bisogna aggiungere le persone assegnate alle task */

        val listener = db.collection("comments").add(commentUpdate)
            .addOnSuccessListener {
                    documentReference ->
                Log.d("Comment", "Comment inserted with ID: ${documentReference.id}")
            }
            .addOnFailureListener {
                    e ->
                Log.e("Comment", "Error adding document", e)
            }
    }
}
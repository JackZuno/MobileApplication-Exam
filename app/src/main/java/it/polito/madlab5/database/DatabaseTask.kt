package it.polito.madlab5.database

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.madlab5.model.Task.History
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Task.TaskEffort
import it.polito.madlab5.model.Task.TaskStates
import it.polito.madlab5.model.Team.Achievement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.coroutines.resume

class DatabaseTask() {

    val db = Firebase.firestore
    suspend fun getTaskHistory(taskHistoryId: String): MutableList<History>  = suspendCancellableCoroutine { continuation ->

        val historyList = mutableListOf<History>()
        GlobalScope.launch {
            try {
                val documentSnapshot = db.collection("history")
                    .document(taskHistoryId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val achievements = documentSnapshot.get("history") as? List<Map<String, Any>>
                    achievements?.let {
                        for (historyMap in it) {
                            val history = History(
                                taskHistory = historyMap["taskHistory"] as? String ?: "",
                                status = historyMap["status"] as? String ?: "",
                                date  = historyMap["date"] as? String ?: "",
                                user = historyMap["user"] as? String ?: "",
                            )
                            historyList.add(history)
                        }
                    }
                    continuation.resume(historyList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
    data class HistoryTmp(
        val history: List<History> = listOf()
    )
    fun updateTaskHistory(historyId: String, history: List<History>) {
        db.collection("history").document(historyId).update("history",HistoryTmp(history).history )
    }
    fun getTasksFromTeam(teamId: String): Flow<MutableList<Task>> = callbackFlow {

        val listener = db.collection("tasks").whereEqualTo("teamId", teamId)
            .addSnapshotListener{
                r, e ->
                    if (r!= null){
                        val listTasks = r.documents.mapNotNull {
                            val taskEffort = it.getString("taskEffort")?.let { effort -> TaskEffort.valueOf(effort) }
                            val taskStatus = it.getString("taskStatus")?.let { status -> TaskStates.valueOf(status) }

                            if (taskEffort != null && taskStatus != null){
                                val pdfData = it["pdf"] as? List<Map<String, String>> ?: listOf()
                                val pdfList = pdfData.map { Pair(it["first"] ?: "", it["second"] ?: "") }.toMutableList()

                                val linkData = it["link"] as? List<String> ?: listOf()
                                val linkList = linkData.toMutableList()

                                Task(
                                    title = it["title"].toString(), taskID = it.id, taskStatus = taskStatus, taskEffort = taskEffort, category = it["category"].toString(),
                                    tag = it["tag"].toString(), startingDate = it["startingDate"].toString(), dueDate = it["dueDate"].toString(), mandatory = it["mandatory"].toString().toBoolean(),
                                    recurrent = it["recurrent"].toString().toBoolean(), recurrentTime = it["recurrentTime"].toString().toInt(10), description = it["description"].toString(),
                                    maxNumber = it["maxNumber"].toString().toInt(10), teamId = it["teamId"].toString(), taskHistoryId =  it["taskHistoryId"].toString(),
                                    pdf = pdfList, link = linkList
                                )
                            } else {
                                null
                            }

                        }
                        trySend(listTasks.toMutableList()).isSuccess
                    } else {
                        Log.e("TASK ERROR", e.toString())
                        trySend(emptyList<Task>().toMutableList())
                    }
            }

        awaitClose{
            listener.remove()
        }
    }

    fun getTaskById(taskId: String): Flow<Task?> = callbackFlow {
        val docRef = db.collection("tasks").document(taskId)

        val taskListener = docRef.addSnapshotListener{ it, error ->
            if (it != null){
                /* Costruiamo l'oggetto task */
                val taskEffort = it.getString("taskEffort")?.let { effort -> TaskEffort.valueOf(effort) }
                val taskStatus = it.getString("taskStatus")?.let { status -> TaskStates.valueOf(status) }

                if (taskEffort != null && taskStatus != null) {
                    val task = Task(
                        title = it["title"].toString(), taskID = it.id, taskStatus = taskStatus, taskEffort = taskEffort, category = it["category"].toString(),
                        tag = it["tag"].toString(), startingDate = it["startingDate"].toString(), dueDate = it["dueDate"].toString(), mandatory = it["mandatory"].toString().toBoolean(),
                        recurrent = it["recurrent"].toString().toBoolean(), recurrentTime = it["recurrentTime"].toString().toInt(10), description = it["description"].toString(),
                        maxNumber = it["maxNumber"].toString().toInt(10), teamId = it["teamId"].toString()
                    )
                    trySend(task)
                } else {
                    Log.w("TASK", "Error in TaskEffort and/or TaskStatus")
                    trySend(null)
                }
            } else {
                Log.e("TASK ERROR", error.toString())
            }
        }

        awaitClose{
            taskListener.remove()
        }
    }

    fun addTask(task: Task) {

        val history = hashMapOf<String, Any>(
            "history" to task.taskHistory
        )
        db.collection("history").add(history).addOnSuccessListener {
            val taskUpdate = hashMapOf<String, Any?>(
                "category" to task.category,
                "description" to task.description,
                "dueDate" to task.dueDate,
                "mandatory" to task.mandatory,
                "maxNumber" to task.maxNumber,
                "recurrent" to task.recurrent,
                "recurrentTime" to task.recurrentTime,
                "startingDate" to task.startingDate,
                "tag" to task.tag,
                "taskEffort" to task.taskEffort.name,
                "taskStatus" to task.taskStatus.name,
                "teamId" to task.teamId,
                "title" to task.title,
                "link" to task.link,
                "pdf" to task.pdf,
                "taskHistoryId" to it.id
            )

            /* Bisogna aggiungere le persone assegnate alle task */

            val listener = db.collection("tasks").add(taskUpdate)
                .addOnSuccessListener { documentReference ->
                    Log.d("TASK", "Task inserted with ID: ${documentReference.id}")
                    task.assignedPerson.forEach {
                        val profileTask = hashMapOf<String, Any>(
                            "profileId" to it.id,
                            "taskId" to documentReference.id
                        )
                        db.collection("profileTask").add(profileTask)
                            .addOnSuccessListener { profileTeam ->
                                Log.d(
                                    "PROFILETASK",
                                    "Relation Profile Task inserted with ID: ${profileTeam.id}"
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.e("PROFILETASK", "Error adding document", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TASK", "Error adding document", e)
                }
        }
    }
    fun updateTaskStatus(taskId: String, newStatus: TaskStates){
        Log.d("TaskID", taskId)
        db.collection("tasks")
            .document(taskId)
            .update("taskStatus", newStatus.name)
            .addOnSuccessListener { 
                Log.i("UPDATE STATUS", "Status Succesfully Updated")
            }
            .addOnFailureListener{error ->
                Log.e("UPDATE STATUS", "Status Update Failure ", error)
            }

    }

    fun updateTask(task: Task){
        val taskUpdate = hashMapOf<String, Any?>(
            "category" to task.category,
            "description" to task.description,
            "dueDate" to task.dueDate,
            "mandatory" to task.mandatory,
            "maxNumber" to task.maxNumber,
            "recurrent" to task.recurrent,
            "recurrentTime" to task.recurrentTime,
            "startingDate" to task.startingDate,
            "tag" to task.tag,
            "taskEffort" to task.taskEffort.name,
            "taskStatus" to task.taskStatus.name,
            "teamId" to task.teamId,
            "title" to task.title,
            "link" to task.link,
            "pdf" to task.pdf,
            "taskHistoryId" to task.taskHistoryId
        )

        db.collection("tasks").document(task.taskID)
            .update(taskUpdate).addOnSuccessListener {
                Log.d("TASK", "Task updated with ID: ${task.taskID}")
                val itemsRef = db.collection("profileTask")
                val query = itemsRef.whereEqualTo("taskId", task.taskID)
                query.get()
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful){
                            task.getResult().forEach { document ->
                                itemsRef.document(document.id).delete()

                            }
                        } else {
                            Log.e("UPDATE TASK", "Error getting documents: ", task.exception)
                        }
                    }
                task.assignedPerson.forEach{
                    val profileTask = hashMapOf<String, Any>( "profileId" to it.id, "taskId" to task.taskID)
                    db.collection("profileTask").add(profileTask)
                        .addOnSuccessListener {
                                profileTeam ->
                            Log.d("UPDATE PROFILETASK", "Relation Profile Task inserted with ID: ${profileTeam.id}")
                        }
                        .addOnFailureListener{
                                e ->
                            Log.e("UPDATE PROFILETASK", "Error adding document", e)
                        }
                }
            }
            .addOnFailureListener {
                    e ->
                Log.e("TASK", "Error updating document", e)
            }
    }

    fun getPersonalTask(profileId: String): Flow<List<Task>> = callbackFlow {
        val personalTasks = mutableListOf<Task>()

        val listener = db.collection("profileTask").whereEqualTo("profileId", profileId)
            .addSnapshotListener{ result, err ->
                    if (result!=null){
                        result.documents.forEach { profileTask ->
                            db.collection("tasks").document(profileTask["taskId"].toString()).get()
                                .addOnSuccessListener{ document ->
                                    if (document != null && document.exists()){
                                        val taskEffort = document.getString("taskEffort")?.let { effort -> TaskEffort.valueOf(effort) }
                                        val taskStatus = document.getString("taskStatus")?.let { status -> TaskStates.valueOf(status) }
                                        if (taskEffort != null && taskStatus != null){

                                            // Extract and convert the pdf field
                                            val pdfData = document["pdf"] as? List<Map<String, String>> ?: listOf()
                                            val pdfList = pdfData.map { Pair(it["first"] ?: "", it["second"] ?: "") }.toMutableList()

                                            val linkData = document["link"] as? List<String> ?: listOf()
                                            val linkList = linkData.toMutableList()

                                            val task = Task(
                                                title = document["title"].toString(), taskID = document.id, taskStatus = taskStatus, taskEffort = taskEffort, category = document["category"].toString(),
                                                tag = document["tag"].toString(), startingDate = document["startingDate"].toString(), dueDate = document["dueDate"].toString(), mandatory = document["mandatory"].toString().toBoolean(),
                                                recurrent = document["recurrent"].toString().toBoolean(), recurrentTime = document["recurrentTime"].toString().toInt(10), description = document["description"].toString(),
                                                maxNumber = document["maxNumber"].toString().toInt(10), teamId = document["teamId"].toString(), taskHistoryId = document["taskHistoryId"].toString(),
                                                pdf = pdfList, link = linkList
                                            )
                                            personalTasks.add(task)
                                        }
                                    } else {
                                        Log.e("TASK ERROR", "No such document for ID: $profileId")
                                    }
                                    trySend(personalTasks)
                                }
                                .addOnFailureListener{
                                    exception ->
                                    Log.e("TASK FAILURE", "get failed with ", exception)
                                    close(exception) // Close flow on failure
                                }
                        }
                    }  else {
                        Log.e("PROFILE ERROR", "No such task associated to ID: {$profileId}")
                    }

            }

        awaitClose{
            listener.remove()
        }
    }

    fun uploadDocument(
        documents: MutableList<Pair<String, String>>,
        fileName: String,
        pdfUri: Uri?,
        context: Context
    ) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("task_files/${System.currentTimeMillis()}.pdf")

        if(pdfUri != null) {
            val fileRef = storageRef.putFile(pdfUri)

            fileRef
                .addOnSuccessListener {
                    Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                    documents.add(Pair(fileName, storageRef.toString()))
                }
                .addOnFailureListener { exception ->
                    Log.w("EXCEPTION DOCUMENT UPLOAD", "Upload failed:", exception)
                    Toast.makeText(context, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    updateToastProgress(progress, context)
                }
        }
    }

    private fun updateToastProgress(progress: Double, context: Context, ) {
        val toastText = "Uploading... ${progress.toInt()}%"
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
    }

    fun deleteDocument(documentUrl: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl(documentUrl)

        storageRef.delete().addOnSuccessListener {
            // Previous image deleted successfully
            Log.d("DELETE DOCUMENT", "Document deleted")
        }.addOnFailureListener {
            // Handle deletion failure (optional)
            Log.w("DELETE DOCUMENT", "Failed to delete the document", it)
        }
    }

    fun getDocument(intent: Intent, documentUrl: String, context: Context, fileName: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl(documentUrl)

        val pathDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        val localFile = File(pathDir, fileName)

        // Check if the file already exists (optional)
        if (localFile.exists()) {
            // Construct a secure content provider URI using FileProvider
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", localFile)

            Toast.makeText(context, "File already downloaded: ${uri}", Toast.LENGTH_SHORT).show()

            openDocumentFromDevice(intent, uri, context)
        } else {
            downloadAndOpenDocument(storageRef, localFile, context, intent)
        }
    }

    private fun downloadAndOpenDocument(
        storageRef: StorageReference,
        localFile: File,
        context: Context,
        intent: Intent
    ) {
        // Download and open the file
        val downloadTask = storageRef.getFile(localFile)
        downloadTask.addOnSuccessListener {
            Log.d("FirebaseStorageManager", "File downloaded successfully to: ${localFile.absolutePath}")
            Toast.makeText(context, "File downloaded successfully!", Toast.LENGTH_SHORT).show()

            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", localFile)

            openDocumentFromDevice(intent, uri, context)
        }.addOnFailureListener { exception ->
            Log.w("FirebaseStorageManager", "Download failed:", exception)
            Toast.makeText(context, "Download failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            updateToastProgressDownload(progress, context)
        }
    }

    private fun openDocumentFromDevice(intent: Intent, localFile: Uri, context: Context) {
        intent.setDataAndType(localFile, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    private fun updateToastProgressDownload(progress: Double, context: Context, ) {
        val toastText = "Downloading... ${progress.toInt()}%"
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
    }
}
package it.polito.madlab5.database

import android.util.Log
import androidx.compose.runtime.toMutableStateList
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.model.chat.Message
import it.polito.madlab5.model.chat.PersonChat
import it.polito.madlab5.model.chat.TeamChat
import it.polito.madlab5.screens.TeamScreen.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DatabaseChats {
    val db = Firebase.firestore
    fun getTeamChat(teamId: String, profileId: String): Flow<TeamChat?> = callbackFlow {
        var teamChat: TeamChat? = null
        val listener = db.collection("teamChats")
            .whereEqualTo("profileId", profileId)
            .whereEqualTo("teamId", teamId)
            .get()
            .addOnSuccessListener {document ->
                if (document!= null){
                    val documentChat = document.documents.first()
                    teamChat = documentChat.toObject(TeamChat::class.java)
                    teamChat?.chatId = documentChat.id
                    trySend(teamChat)
                } else {
                    Log.e("TEAMCHAT ERROR", "No such document")
                    trySend(null)
                }
                close()
            }
            .addOnFailureListener{ exception ->
                Log.d("TEAMCHAT FAILURE", "get failed with ", exception)
                trySend(null).isSuccess
                close()
            }

        awaitClose{

        }
    }

    fun getTeamChatMessages(teamId: String): Flow<MutableList<Message>> = callbackFlow {

        val teamChat = fetchTeamChatMessages(teamId)


        trySend(teamChat.toMutableStateList())

        awaitClose{
        }
    }

    suspend fun fetchTeamChatMessages(teamId: String): List<Message>{
        val teamChat = mutableListOf<Message>()
        val teamChatDB = db.collection("teamChats").whereEqualTo("teamId", teamId)
        val messageDB = db.collection("messages")

        val queryTeamChatSnapshot = teamChatDB.get().await()
        val teamChatTasks = queryTeamChatSnapshot.documents.map { teamChat ->
            Log.d("TEAMCHAT",teamChat.id)
            messageDB.whereEqualTo("chatId", teamChat.id).get()
        }

        teamChatTasks.forEach { task ->
            val profileChats = task.await()
            profileChats.documents.forEach {
                val message = it.toObject(Message::class.java)
                message?.messageId = it.id
                if (message != null) {
                    teamChat.add(message)
                }
                teamChat.sortBy { it.sentAt }
            }
        }

        return teamChat
    }

    fun addMessagesToTeamChat(message: Message, chatId: String){

        val messageWithoutId = hashMapOf<String, Any?>(
            "text" to message.text,
            "author" to message.author,
            "sentAt" to message.sentAt,
            "chatId" to chatId
        )
        db.collection("messages").add(messageWithoutId)
            .addOnSuccessListener {
                Log.d("Message Added", "Message Successfully added")
            }
            .addOnFailureListener{e ->
                Log.e("Error Message", "error inserting Mesage", e)
            }
    }

    fun updateTeamChatLastAccess(chatId: String, newLastAccess: String){

        val newLastAccessMap = mapOf<String, Any>( "lastAccess" to newLastAccess)

        db.collection("teamChats").document(chatId).update(newLastAccessMap)
            .addOnSuccessListener {
                Log.d("LASTACCESS UPDATE", "Last Access successfully updated for chat with id: ${chatId}",  )
            }
            .addOnFailureListener{e ->
                Log.e("LASTACCESS UPDATE", "error in updating the lastAccess", e)
            }
    }

    suspend fun createNewPersonalChat(profileFrom: String, profileTo: String){
        val newChatMap = mapOf<String, Any>(
            "profileFrom" to profileFrom,
            "profileTo" to profileTo,
            "lastAccess" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"))
        )

        //db.collection("personalChats").add(new)
    }

    suspend fun checkQuery(profileFrom: String, profileTo: String): Boolean {
        val query = db.collection("personalChats")
            .whereEqualTo("profileFrom", profileFrom)
            .whereEqualTo("profileTo", profileTo).get().await()

        if (query.isEmpty){
            return false
        }

        return true
    }

    fun getOrCreatePersonalChat(profileFrom: String, profileTo: String): Flow<PersonChat?> = callbackFlow {

        val isChatCreated = checkQuery(profileFrom, profileTo)

        if (isChatCreated){
            db.collection("personalChats")
                .whereEqualTo("profileTo", profileTo)
                .whereEqualTo("profileFrom", profileFrom)
                .get()
                .addOnSuccessListener {chats ->
                    if (chats != null){
                        val query = chats.documents.first()
                        val newChat = query.toObject(PersonChat::class.java)
                        newChat?.chatId = query.id
                        Log.d("PersonChat", "Chat successfully retrieved")
                        trySend(newChat)
                    } else {
                        Log.e("PersonChat", "ChatError retrieving chats")
                        trySend(null)
                    }

                }
                .addOnFailureListener{ e ->
                    Log.e("PersonChat", "Error retrieving the team", e)
                }
        } else {
            val newChatMap = mapOf<String, Any>(
                "profileFrom" to profileFrom,
                "profileTo" to profileTo,
                "lastAccess" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"))
            )

            db.collection("personalChats").add(newChatMap)
                .addOnSuccessListener{ personChat ->
                    db.collection("personalChats").document(personChat.id)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null){
                                val newChat = document.toObject(PersonChat::class.java)
                                newChat?.chatId = document.id
                                trySend(newChat)
                            }
                        }
                    Log.d("PersonChat", "Created and Retrieved a new PersonChat with id: ${personChat.id}")
                }
                .addOnFailureListener{e ->
                    Log.e("PersonChat", "Error creating new chat", e)
                    trySend(null)
                }
        }

        awaitClose {

        }
    }

    fun getListPersonalChats(profileFrom: String): Flow<List<PersonChat>> = callbackFlow {

        val listener = db.collection("personalChats")
            .whereEqualTo("profileFrom", profileFrom)
            .addSnapshotListener{ r,e ->
                if (r != null){
                    val chats = r.documents.mapNotNull { document ->
                        val chat = document.toObject(PersonChat::class.java)
                        chat?.chatId = document.id
                        chat
                    }
                    Log.d("CHATS", "PersonalChats successfully retrieved")
                    trySend(chats)
                } else {
                    Log.e("CHATS", "Error retrieving chats", e)
                    trySend(emptyList())
                }
            }


        awaitClose {
            listener.remove()
        }
    }

    fun addPersonalChat(chat: PersonChat){
        val chatMap = mapOf<String, Any>(
            "lastAccess" to chat.lastAccess,
            "profileFrom" to chat.profileFrom,
            "profileTo" to chat.profileTo
        )

        GlobalScope.launch {
            val isChatCreated = checkQuery(chat.profileFrom, chat.profileTo)

            if(!isChatCreated){
                db.collection("personalChats").add(chatMap)
                    .addOnSuccessListener {
                        Log.d("CHATS", "Chat successfully Added")
                    }
                    .addOnFailureListener{e ->
                        Log.e("CHATS", "Error in adding the chat")
                    }
            }
        }
    }

    suspend fun fetchPersonalMessages(profileFrom: String, profileTo: String): List<Message> {

        val personalChat = mutableListOf<Message>()
        val personalChatFrom = db.collection("personalChats").whereEqualTo("profileFrom", profileFrom).whereEqualTo("profileTo", profileTo)
        val personalChatTo = db.collection("personalChats").whereEqualTo("profileFrom", profileTo).whereEqualTo("profileTo", profileFrom)
        val messageDB = db.collection("messages")

        val queryTeamChatSnapshotFrom = personalChatFrom.get().await()
        val personalChatTaskFrom = queryTeamChatSnapshotFrom.documents.map { personalChat ->
            messageDB.whereEqualTo("chatId", personalChat.id).get()
        }

        personalChatTaskFrom.forEach { task ->
            val profileChats = task.await()
            profileChats.documents.forEach {
                val message = it.toObject(Message::class.java)
                message?.messageId = it.id
                if (message != null) {
                    personalChat.add(message)
                }
                personalChat.sortBy { it.sentAt }
            }
        }

        val queryTeamChatSnapshotTo = personalChatTo.get().await()
        val personalChatTaskTo = queryTeamChatSnapshotTo.documents.map { personalChat ->
            messageDB.whereEqualTo("chatId", personalChat.id).get()
        }

        personalChatTaskTo.forEach { task ->
            val profileChats = task.await()
            profileChats.documents.forEach {
                val message = it.toObject(Message::class.java)
                message?.messageId = it.id
                if (message != null) {
                    personalChat.add(message)
                }
                personalChat.sortBy { it.sentAt }
            }
        }

        return personalChat
    }
    fun getPersonalMessages(profileFrom: String, profileTo: String): Flow<MutableList<Message>> = callbackFlow {
        val profileChat = fetchPersonalMessages(profileFrom, profileTo)

        Log.d("PersonalChats", profileChat.toString().length.toString())
        trySend(profileChat.toMutableStateList())
        close()

        awaitClose{
        }
    }

    fun addPersonalMessage(message: Message, chatId: String){
        val messageWithoutId = hashMapOf<String, Any?>(
            "text" to message.text,
            "author" to message.author,
            "sentAt" to message.sentAt,
            "chatId" to chatId
        )
        db.collection("messages").add(messageWithoutId)
            .addOnSuccessListener {
                Log.d("Personal Message", "Message Successfully added")
            }
            .addOnFailureListener{e ->
                Log.e("Personal Message", "error inserting Mesage", e)
            }
    }

    fun updatePersonalChatLastAccess(chatId: String, newLastAccess: String){
        val newLastAccessMap = mapOf<String, Any>( "lastAccess" to newLastAccess)

        db.collection("personalChats").document(chatId).update(newLastAccessMap)
            .addOnSuccessListener {
                Log.d("LASTACCESS UPDATE", "Last Access successfully updated for chat with id: ${chatId}",  )
            }
            .addOnFailureListener{e ->
                Log.e("LASTACCESS UPDATE", "error in updating the lastAccess", e)
            }
    }


    fun getAllTeamChats(profileId: String): Flow<List<TeamChat>> = callbackFlow {

        db.collection("teamChats").whereEqualTo("profileId", profileId)
            .addSnapshotListener{ r, e ->
                if (r != null){
                    val chats = r.documents.mapNotNull { document ->
                        val chat = document.toObject(TeamChat::class.java)
                        chat?.chatId = document.id
                        chat
                    }
                    Log.d("TEAMCHATS", "TeamChats successfully retrieved")
                    trySend(chats)
                } else {
                    Log.e("TEAMCHATS", "Error retrieving chats", e)
                    trySend(emptyList())
                }
            }
        awaitClose{

        }
    }
}
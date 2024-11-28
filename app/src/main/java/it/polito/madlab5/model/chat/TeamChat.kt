package it.polito.madlab5.model.chat

import androidx.compose.runtime.toMutableStateList
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Team.Team
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TeamChat(
    var chatId: String = "",
    override var messages: MutableList<Message> = mutableListOf(),
    var teamId: String = "",
    var profileId: String = "",
    var lastAccess: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
) : Chat(messages) {
    var messagesState = messages.toMutableStateList()

    override fun addMessage(message: Message){
        messagesState.add(message)
    }

    fun setMessagesStateAfter(newMessages: List<Message>){
        messagesState = newMessages.toMutableStateList()
    }

    fun setNewLastAccess(newAccess: String){
        lastAccess = newAccess
    }
}

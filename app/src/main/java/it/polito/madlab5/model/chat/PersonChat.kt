package it.polito.madlab5.model.chat

import androidx.compose.runtime.toMutableStateList
import it.polito.madlab5.model.Profile.Profile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PersonChat(
    var chatId: String = "",
    var profileFrom: String="",
    var profileTo: String="",
    var lastAccess: String="",
    override var messages: MutableList<Message> = mutableListOf()
) : Chat(messages) {
    var messagesState = messages.toMutableStateList()

    override fun addMessage(message: Message) {
        messagesState.add(message)
    }

    fun setChatLastAccess(currentTime: String){
        lastAccess = currentTime
    }

    fun getChatLastAccess(): String{
        return this.lastAccess
    }

    fun setMessagesStateAfter(newMessages: List<Message>){
        messagesState = newMessages.toMutableStateList()
    }
}
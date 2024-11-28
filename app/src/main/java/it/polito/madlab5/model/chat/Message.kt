package it.polito.madlab5.model.chat

import it.polito.madlab5.model.Profile.Profile

data class Message(
    var messageId: String="",
    val text: String="",
    val author: String="",
    val sentAt: String=""
) {

    fun isFromMe(loggedInUser: String): Boolean {
        return this.author == loggedInUser
    }
}
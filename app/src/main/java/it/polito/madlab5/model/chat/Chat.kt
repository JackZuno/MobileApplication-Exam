package it.polito.madlab5.model.chat

open class Chat(
    messages: MutableList<Message> = mutableListOf()
) {
    open var messages = messages

    open fun addMessage(message: Message){
        messages.add(message)
    }
}
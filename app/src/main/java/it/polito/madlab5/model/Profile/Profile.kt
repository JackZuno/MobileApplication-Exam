package it.polito.madlab5.model.Profile

import android.graphics.Bitmap
import android.util.Log
import it.polito.madlab5.model.chat.PersonChat
import it.polito.madlab5.viewModel.ProfileViewModels.ProfileViewModel

var PROFILE_ID: Long = 0L

data class Profile (
    var id: String = "",
    var name: String="",
    var lastname: String="",
    var username: String="",
    var email: String="",
    var location: String="",
    var description: String="",
    var skills: MutableList<String> = mutableListOf(),
    var joineddate: String="",
    var image: Bitmap? = null,
    var imageURL: String? = null,
    var personalChats: MutableList<PersonChat> = mutableListOf()
){

    fun copyFromViewModel(pvm: ProfileViewModel){
        id = pvm.idValue
        name = pvm.nameValue
        lastname = pvm.lastNameValue
        username = pvm.nicknameValue
        email = pvm.emailValue
        location = pvm.locationValue
        description = pvm.descriptionValue
        skills = pvm.skillsValue
        joineddate = pvm.joinedDateValue
        image = pvm.imageValue
        personalChats = pvm.chatsValue
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Profile) return false

        if(this.id == other.id) return true

        return false
    }

    fun getChatWithUser(profileTo: Profile){
        /*val index = personalChats.indexOf(PersonChat(profileFrom = this, profileTo = profileTo))
        Log.d("INDEX", "$index")
        return if (index != -1){
            personalChats[index]
        } else {
            null
        }*/
    }
}

fun Profile.fromProfileToViewModel(): ProfileViewModel {
    val newImage = this.image
    if(this.image != null) {
        Log.w("IMAGE fromPrToVM", "IMAGE IS NOT NULL")
    }

    if(newImage != null) {
        Log.w("IMAGE new fromPrToVM", "IMAGE IS NOT NULL")
    }

    return ProfileViewModel(
        id = this.id,
        name = this.name,
        lastname = this.lastname,
        username = this.username,
        email = this.email,
        location = this.location,
        description = this.description,
        skills = this.skills,
        joineddate = this.joineddate,
        image = newImage,
        imageURL = this.imageURL,
        personalChat = this.personalChats
    )
}
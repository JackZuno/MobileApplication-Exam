package it.polito.madlab5.viewModel.ProfileViewModels

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.chat.PersonChat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProfileViewModel(
    var id: String = "",
    var name: String= "",
    var lastname: String= "",
    var username: String= "",
    var email: String,
    var location: String="",
    var description: String="",
    var skills: MutableList<String> = mutableListOf(),
    var joineddate: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    var image: Bitmap? = null,
    var imageURL: String? = null,
    var personalChat: MutableList<PersonChat> = mutableListOf()
): ViewModel() {
    var idValue by mutableStateOf(id)
        private set

    var isEditing by mutableStateOf(false)
        private set

    var isValid by mutableStateOf(false)
        private set

    var showingStats by mutableStateOf(false)
        private set

    var showingTeamStats by mutableStateOf(false)
        private set

    // Show Stats
    fun showStats() {
        showingStats = true
    }

    fun showStatsDone() {
        showingStats = false
    }

    // Edit
    fun edit() {
        isEditing = true
    }
    fun editDone() {
        isEditing = false
    }

    // Show Team Stats
    fun showTeamStats(){
        showingTeamStats = true
    }

    fun showTeamStatsDone(){
        showingTeamStats = false
    }

    fun validate() {
        isValid = if(nameError.isBlank() && lastNameError.isBlank() && nicknameError.isBlank() && locationError.isBlank() && descriptionError.isBlank())
            true
        else
            false
    }

    fun initializeValidEdit(){
        isValid = true
    }

    // Name
    var nameValue by mutableStateOf(name)
        private set
    var nameError by mutableStateOf("")
        private set
    fun set_Name(n: String) {
        nameError = if(n == ""){
            "Name cannot be empty"
        } else if(n.length < 2 || n.length > 18)
            "Name should be between 2 and 18 characters"
        else if(n.contains("[0-9]".toRegex()))
            "Name cannot contain numbers"
        else
            ""
        nameValue = n

        validate()
    }

    // Last Name
    var lastNameValue by mutableStateOf(lastname)
        private set
    var lastNameError by mutableStateOf("")
        private set
    fun setLastName(l: String){
        lastNameError = if(l == "")
            "Name cannot be empty"
        else if(l.length < 2 || l.length > 18)
            "Name should be between 2 and 18 characters"
        else if(l.contains("[0-9]".toRegex()))
            "Name cannot contain numbers"
        else
            ""
        lastNameValue = l
        validate()
    }

    // Username
    var nicknameValue by mutableStateOf(username)
        private set
    var nicknameError by mutableStateOf("")
        private set
    fun setNickname(n: String){
        nicknameError = if(n == "")
            "Username cannot be empty"
        else if(n.length < 2 || n.length > 18)
            "Username should be between 2 and 18 characters"
        else
            ""
        nicknameValue = n
        validate()
    }

    // email
    var emailValue by mutableStateOf(email)
        private set
    var emailError by mutableStateOf("")
        private set
    fun set_Email(e: String){
        //google email, can't change
        emailValue = e
    }

    // Location
    var locationValue by mutableStateOf(location)
        private set
    var locationError by mutableStateOf("")
        private set
    fun set_Location(l: String){
        locationError = if(l == "")
            "Location cannot be empty"
        else if(l.length < 2 || l.length > 18)
            "Location should be between 2 and 18 characters"
        else
            ""
        locationValue = l
        validate()
    }

    // Description
    var descriptionValue by mutableStateOf(description)
        private set
    var descriptionError by mutableStateOf("")
        private set
    fun set_Description(d: String){
        descriptionError = if(d.length > 100)
            "Description must not exceed 100 characters"
        else
            ""
        descriptionValue = d
        validate()
    }

    // Skills
    var skillsValue = skills
        private set

    var currentSkillValue by mutableStateOf("")
        private set

    fun setCurrentSkill(s: String){
        currentSkillValue = s
    }

    fun addSkill(s: String){
        var t = s.replace("\n", "")
        t = t.trim()
        if (t != "" && t != " " && t != "\n") {
            skillsValue.add(s.trim())
        }

    }

    fun removeSkill(index: Int){
        skillsValue.removeAt(index)
    }

    // Image
    var imageValue by mutableStateOf(image)
        private set

    fun setImageBitmap(bitmap: Bitmap?) {
        imageValue = bitmap
    }

    var imageURLValue by mutableStateOf(imageURL)
        private set

    fun setImageURLFromStorage(url: String?){
        imageURLValue = url
    }

    var showAllert by mutableStateOf(false)
    var showNoPermissionAllert by mutableStateOf(false)

    var joinedDateValue by mutableStateOf(joineddate)
        private set

    var chatsValue = personalChat.toMutableList()

}

fun ProfileViewModel.fromViewModelToProfile(): Profile {
    return Profile(
        id = this.idValue,
        name = this.nameValue,
        lastname = this.lastname,
        username = this.nicknameValue,
        description = this.descriptionValue,
        email = this.emailValue,
        skills = this.skillsValue,
        joineddate = this.joinedDateValue,
        image = this.imageValue,
        imageURL = this.imageURLValue,
        location = this.locationValue,
        personalChats = chatsValue
    )
}
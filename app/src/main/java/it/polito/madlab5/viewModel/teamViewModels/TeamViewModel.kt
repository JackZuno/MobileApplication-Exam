package it.polito.madlab5.viewModel.TeamViewModels

import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.EmojiFlags
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Start
import androidx.compose.material.icons.outlined.Task
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Team.Achievement
import it.polito.madlab5.model.Team.AchievementState
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.model.chat.Message
import it.polito.madlab5.model.chat.TeamChat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class memberStatus{
    FullTime,
    PartTime,
    Erratic
}
class TeamViewModel(
    var teamId: String="",
    var teamName: String="Team Name",
    var category: String="Work",
    var description: String="",
    var image: Bitmap? = null,
    var imageURL: String? = null,
    var teamAdmin: String,
    var taskList: MutableList<Task> = mutableListOf(),
    var creationDate: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    var link: String = "",
    var teamMembers: MutableList<Profile> = mutableListOf(),
    var teamMemberStatus: MutableList<Pair<String, memberStatus>> = mutableListOf(),
    var achievementId: String = "",
    var achievement: MutableList<Achievement> = mutableListOf(
        /*Achievement(Icons.Outlined.Start, "Project Kickoff", "Successfully launch Team project..", AchievementState.ToClaim.name),
        Achievement(Icons.Outlined.Task,"First Task Together", "Create and assign the first task as a Team.", AchievementState.NotAchieved.name),
        Achievement(Icons.Outlined.Message, "Collaborative Discussion", "Have the first group chat.",  AchievementState.NotAchieved.name),
        Achievement(Icons.Outlined.DocumentScanner, "Shared Document", "Upload and share the first file with the Team.", AchievementState.NotAchieved.name),
        Achievement(Icons.Outlined.Message,"Communication Pro", "50 messages or discussions in the Team chat feature",  AchievementState.NotAchieved.name),
        Achievement(Icons.Outlined.FileCopy,"Taskmaster", "Complete 10 tasks.", AchievementState.NotAchieved.name),
        Achievement(Icons.Outlined.PeopleOutline,"Collaboration Champion", "Successfully complete a project with 100% participation from all Team members.",  AchievementState.NotAchieved.name),
        Achievement(Icons.Outlined.Lightbulb,"Group Efficiency", "Complete a group task ahead of the deadline.", AchievementState.NotAchieved.name),
        Achievement(Icons.Outlined.EmojiFlags,"Collaborative Completion", "Finish your first group project successfully.",  AchievementState.NotAchieved.name),),*/
        Achievement("Project Kickoff", "Successfully launch Team project..", AchievementState.ToClaim.name),
        Achievement("First Task Together", "Create and assign the first task as a Team.", AchievementState.NotAchieved.name),
        Achievement( "Collaborative Discussion", "Have the first group chat.",  AchievementState.NotAchieved.name),
        Achievement( "Shared Document", "Upload and share the first file with the Team.", AchievementState.NotAchieved.name),
        Achievement("Communication Pro", "50 messages or discussions in the Team chat feature",  AchievementState.NotAchieved.name),
        Achievement("Taskmaster", "Complete 10 tasks.", AchievementState.NotAchieved.name),
        Achievement("Collaboration Champion", "Successfully complete a project with 100% participation from all Team members.",  AchievementState.NotAchieved.name),
        Achievement("Group Efficiency", "Complete a group task ahead of the deadline.", AchievementState.NotAchieved.name),
        Achievement("Collaborative Completion", "Finish your first group project successfully.",  AchievementState.NotAchieved.name),),

        var teamChats: TeamChat = TeamChat()
) {


    var isValid by mutableStateOf(false)
        private set

    var nameValue by mutableStateOf(teamName)
        private set

    var nameError by mutableStateOf("")
        private set

    fun setName(n: String){
        nameError = if (n == "") {
            "Name must not be blank"
        } else {
           ""
        }

        nameValue = n
    }

    var categoryValue by mutableStateOf(category)
        private set

    fun setTeamCategory(c: String){
        categoryValue = c
    }

    var descriptionValue by mutableStateOf(description)
        private set

    fun setTeamDescription(d: String){
        descriptionValue = d
    }

    /* IMAGE */
    var imageValue by mutableStateOf(image)
        private set

    fun setImageBitMap(bm: Bitmap?){
        imageValue = bm
    }

    var imageURLValue by mutableStateOf(imageURL)
        private set

    fun setImageURLFromStorage(url: String?){
        imageURLValue = url
    }

    var teamAdminValue by mutableStateOf(teamAdmin)
        private set


    var taskListState = taskList.toMutableStateList()

    var creationDateValue by mutableStateOf(creationDate)
        private set

    var linkValue by mutableStateOf(link)
        private set

    fun setGeneratedLink(l: String){
        linkValue = l
    }

    fun validate() {
        isValid = if(nameError.isBlank())
            true
        else
            false
    }

    var chatState by mutableStateOf(teamChats)
        private set

    fun setChatLastAccess(profile: Profile, time: String){
        //chatState.setLastAccessTime(profile, time)
    }

    fun addMessageToChat(message: Message){
        chatState.addMessage(message)
    }
    var membersStatusValue = teamMemberStatus.toMutableStateList()

    var teamMembersState = teamMembers.toMutableStateList()

}

fun TeamViewModel.fromViewModelToTeam(): Team {
    return Team(
        teamID = this.teamId,
        name = this.nameValue,
        category = this.categoryValue,
        image = this.imageValue,
        imageURL = this.imageURLValue,
        description = this.descriptionValue,
        teamAdmin = this.teamAdminValue,
        taskList = this.taskListState,
        link = this.linkValue,
        creationDate = this.creationDateValue,
        teamMembers = this.teamMembers,
        teamMemberStatus = this.membersStatusValue,
        achievement =  this.achievement,
        achievementId = this.achievementId
        //teamChat = this.chatState,

    )
}
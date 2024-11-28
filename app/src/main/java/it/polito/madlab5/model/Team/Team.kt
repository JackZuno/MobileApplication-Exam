package it.polito.madlab5.model.Team

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.toMutableStateList
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Task.TaskStates
import it.polito.madlab5.viewModel.TeamViewModels.TeamViewModel
import it.polito.madlab5.viewModel.TeamViewModels.memberStatus

var ID_TEAM: Long = 4L

class Team(
    var teamID: String = "",
    var name: String = "",
    var category: String = "",
    var image: Bitmap? = null,
    var imageURL: String? = null,
    var description: String = "",
    var teamAdmin: String = "",
    var taskList: MutableList<Task> = mutableListOf(),
    var achievement: MutableList<Achievement> = mutableListOf(),
    var achievementId: String  = "",
    var creationDate: String = "",
    var link: String = "",
    var teamMembers: MutableList<Profile> = mutableListOf(),
    var teamMemberStatus: MutableList<Pair<String, memberStatus>> = mutableListOf(),
    //var teamChat: TeamChat = TeamChat(),
) {

    var teamMembersState = teamMembers.toMutableStateList()
    var teamMemberStatusState = teamMemberStatus.toMutableStateList()

    /*init {
        teamChat = if (teamChat.checkTeamInitialization()) {
            teamChat
        } else {
            TeamChat(team = this)
        }
    }*/

    fun copyFromViewModel(tvm: TeamViewModel) {
        name = tvm.nameValue
        category = tvm.categoryValue
        image = tvm.imageValue
        imageURL = tvm.imageURLValue
        description = tvm.descriptionValue
        teamAdmin = tvm.teamAdminValue
        taskList = tvm.taskListState
        creationDate = tvm.creationDate
        link = tvm.linkValue
        teamMemberStatus = tvm.membersStatusValue
        achievementId = tvm.achievementId
        /* Modify the states */
        teamMemberStatusState = tvm.membersStatusValue
        teamMembersState = tvm.teamMembersState
        achievement = tvm.achievement
        /*teamChat = if (tvm.chatState.checkTeamInitialization()){
            tvm.chatState
        } else {
            TeamChat(team = this)
        }
        teamMemberStatus = tvm.membersStatusValue

        */

    }

    fun checkAchievement() {
        //primo always on

        //secondo
        if (taskList.any { t -> t.taskStatus == TaskStates.Completed }) {
            if (achievement[AchievementIndex.FirstTaskTogether.ordinal].achievementState != AchievementState.Achieved.name)
                achievement[AchievementIndex.FirstTaskTogether.ordinal].achievementState =
                    AchievementState.ToClaim.name
        }
        //terzo MESAGGI DA COLLEGARE
        /*if(teamChat.messages.size > 0){
            if (achievement[AchievementIndex.CollaborativeDiscussion.ordinal].achievementState != AchievementState.Achieved)
                achievement[AchievementIndex.CollaborativeDiscussion.ordinal].achievementState =
                    AchievementState.ToClaim
        }*/
        //quarto
        if (taskList.any { t -> t.pdf.size != 0 }) {
            if (achievement[AchievementIndex.SharedDocument.ordinal].achievementState != AchievementState.Achieved.name)
                achievement[AchievementIndex.SharedDocument.ordinal].achievementState =
                    AchievementState.ToClaim.name
        }
        //quinto manca chat
        /*if(teamChat.messages.size > 50){
            if (achievement[AchievementIndex.CollaborationChampion.ordinal].achievementState != AchievementState.Achieved)
                achievement[AchievementIndex.CollaborationChampion.ordinal].achievementState =
                    AchievementState.ToClaim
        }*/
        if (taskList.count { t -> (t.taskStatus == TaskStates.Completed) } >= 10) {
            if (achievement[AchievementIndex.Taskmaster.ordinal].achievementState != AchievementState.Achieved.name)
                achievement[AchievementIndex.Taskmaster.ordinal].achievementState =
                    AchievementState.ToClaim.name
        }
        //sesto
       /*var allCompleted = false
        for (m in teamMembers) {
            val memberTasks = taskList.filter { t ->
                t.taskStatus == TaskStates.Completed &&
                        t.taskHistory.any { h -> h.status == TaskStates.Completed && h.user.username == m.username }
            }

            if (memberTasks.isEmpty()) {
                allCompleted = true
                break
            }

        }

        if (!allCompleted) {
            if (achievement[AchievementIndex.CollaborationChampion.ordinal].achievementState != AchievementState.Achieved.name)
                achievement[AchievementIndex.CollaborationChampion.ordinal].achievementState =
                    AchievementState.ToClaim.name
        }*/

        //ottavo
        val firstTask = taskList.filter{t -> t.taskStatus == TaskStates.Completed}

       /* if (firstTask.any { t -> t.taskHistory.first { h ->h.status == TaskStates.Completed }.date < t.dueDate }) {
            if (achievement[AchievementIndex.GroupEfficiency.ordinal].achievementState != AchievementState.Achieved.name)
                achievement[AchievementIndex.GroupEfficiency.ordinal].achievementState =
                    AchievementState.ToClaim.name
        }*/
        //Nono
        if (taskList.filter { t -> t.taskStatus == TaskStates.Completed }.size == taskList.size) {
            if (achievement[AchievementIndex.CollaborationChampion.ordinal].achievementState != AchievementState.Achieved.name)
                achievement[AchievementIndex.CollaborationChampion.ordinal].achievementState =
                    AchievementState.ToClaim.name

        }
    }

    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (other !is Team) return false

        if(this.teamID == other.teamID) return true

        return false
    }

}



fun Team.fromTeamToViewModel(): TeamViewModel {
    return TeamViewModel(
        teamId = this.teamID,
        teamName = this.name,
        category = this.category,
        description = this.description,
        image = this.image,
        imageURL = this.imageURL,
        teamAdmin = this.teamAdmin,
        taskList = this.taskList,
        creationDate = this.creationDate,
        link = this.link.orEmpty(),
        teamMemberStatus = this.teamMemberStatus,
        achievement = this.achievement,
        achievementId = this.achievementId
        //teamChats = this.teamChat,
        //teamMemberStatus = this.teamMemberStatus
    )
}

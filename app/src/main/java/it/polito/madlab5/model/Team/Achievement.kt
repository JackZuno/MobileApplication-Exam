package it.polito.madlab5.model.Team

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.EmojiFlags
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Start
import androidx.compose.material.icons.outlined.Task
import androidx.compose.ui.graphics.vector.ImageVector

var ID_ACHIEVEMENT: Long = 0L
enum class AchievementState(){
    Achieved,
    NotAchieved,
    ToClaim
}

enum class AchievementIndex{
    ProjectKickoff,
    FirstTaskTogether,
    CollaborativeDiscussion,
    SharedDocument,
    CommunicationPro,
    Taskmaster,
    CollaborationChampion,
    GroupEfficiency,
    CollaborativeCompletion

}
/*val achievementsListInitialize = mutableListOf(
    Achievement(Icons.Outlined.Start, "Project Kickoff", "Successfully launch Team project..", AchievementState.ToClaim),
    Achievement(Icons.Outlined.Task,"First Task Together", "Create and assign the first task as a Team.", AchievementState.NotAchieved),
    Achievement(Icons.Outlined.Message, "Collaborative Discussion", "Have the first group chat.",  AchievementState.NotAchieved),
    Achievement(Icons.Outlined.DocumentScanner, "Shared Document", "Upload and share the first file with the Team.", AchievementState.NotAchieved),
    Achievement(Icons.Outlined.Message,"Communication Pro", "50 messages or discussions in the Team chat feature",  AchievementState.NotAchieved),
    Achievement(Icons.Outlined.FileCopy,"Taskmaster", "Complete 10 tasks.", AchievementState.NotAchieved),
    Achievement(Icons.Outlined.PeopleOutline,"Collaboration Champion", "Successfully complete a project with 100% participation from all Team members.",  AchievementState.NotAchieved),
    Achievement(Icons.Outlined.Lightbulb,"Group Efficiency", "Complete a group task ahead of the deadline.", AchievementState.NotAchieved),
    Achievement(Icons.Outlined.EmojiFlags,"Collaborative Completion", "Finish your first group project successfully.",  AchievementState.NotAchieved),
)*/
val iconsVec = listOf(
    Icons.Outlined.Start,
    Icons.Outlined.Task,
    Icons.Outlined.Message,
    Icons.Outlined.DocumentScanner,
    Icons.Outlined.Message,
    Icons.Outlined.FileCopy,
    Icons.Outlined.PeopleOutline,
    Icons.Outlined.Lightbulb,
    Icons.Outlined.EmojiFlags,
)
class Achievement(
//    var icon: ImageVector,
    var name: String,
    var description: String,
    var achievementState: String
    ) {
}

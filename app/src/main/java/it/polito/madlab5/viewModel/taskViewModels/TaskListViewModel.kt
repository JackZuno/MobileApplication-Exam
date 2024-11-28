package it.polito.madlab5.viewModel.TaskViewModels


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import it.polito.madlab5.database.DatabaseComment
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Team.Team
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TaskListViewModel(
    var teamTaskList: MutableList<Task> = mutableListOf(),
    var taskTeamMembers: MutableList<Profile> = mutableListOf(),
    var team: Team? = null
) {

    var filteredTaskList = teamTaskList.toMutableStateList()

    var taskList = teamTaskList.toMutableStateList()

    var teamMembers = taskTeamMembers.toMutableStateList()

    var members: MutableList<Profile> = mutableListOf()

    var teamValue by mutableStateOf(team)
        private set

    var isCreationDate = mutableStateOf(true)
        private set

    fun addNewTask(task: Task){
        taskList.add(task)
        filteredTaskList.add(task)



    }
    fun getTaskByIndex(index: Int): Task {
        return filteredTaskList.get(index)
    }

    fun getMyTaskByIndex(index: Int, uid: String): Task {
      return taskList.filter { t -> t.assignedPerson.any { p -> p.id == uid }}[index]
    }

    fun addTeamMemberInTaskList(){
        for (t in taskList){
            val scope  = CoroutineScope(Job() + Dispatchers.IO)
            scope.launch {
                DatabaseProfile().getAssignedTeamMembersTask(t.taskID).collect { p ->
                    t.assignedPerson = p

                }
            }
        }
    }

    private val dbCommentHandler = DatabaseComment()
    fun loadCommentFromTask(taskId: String, taskIndex: Int) {

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            dbCommentHandler.getCommentsFromTask(taskId).collect { comments ->
                taskList[taskIndex].comments.clear()
                taskList[taskIndex].comments.addAll(comments)
                for (c in taskList[taskIndex].comments){
                    val profile = teamMembers.first { m -> m.id == c.profileId }
                    c.profileName = profile.name
                }
            }
        }
    }

    private val dbProfileHandler = DatabaseProfile()
    fun loadProfiles(){
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            dbProfileHandler.getProfiles().collect { profiles ->
                members.clear()
                members.addAll(profiles)

            }
        }
    }

}

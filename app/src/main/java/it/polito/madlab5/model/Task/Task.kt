package it.polito.madlab5.model.Task

import it.polito.madlab5.database.DatabaseComment
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.viewModel.TaskViewModels.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class TaskEffort{
    None, Low, Medium, High
}

enum class TaskStates(){
    Completed,
    OverDue,
    InProgress,
    Pending,
    All
}

enum class TaskHistoryEdit(){
                            Edited,
    Status,
    Created,
}

data class Task(
    var taskID: String = "",

    var title: String = "",

    var category: String = "",

    var tag: String = "",

    var startingDate: String = "",

    var dueDate: String = "",

    var mandatory: Boolean = false,

    var recurrent: Boolean = false,

    var recurrentTime: Int = 0,

    var taskEffort: TaskEffort,

    var maxNumber: Int,

    var description: String,

    var taskStatus: TaskStates,

    var assignedPerson: List<Profile> = listOf(),

    var link: MutableList<String> = mutableListOf(),

    var comments: MutableList<Comment> = mutableListOf(),

    var pdf: MutableList<Pair<String, String>> = mutableListOf(),

    var taskHistory: MutableList<History> = mutableListOf(),

    var teamId: String = "",

    var taskHistoryId: String = ""

    ){

    fun addComment(comment: Comment){
        comments.add(comment)
    }
    fun addNewHistoryItem(item: History){
        taskHistory.add(item)

    }

    fun copyFromViewModel(tvm: TaskViewModel){
        taskID = tvm.taskId
        title = tvm.titleValue
        category = tvm.categoryValue
        tag = tvm.tagValue
        startingDate = tvm.startingDateValue
        dueDate = tvm.dueDateValue
        mandatory = tvm.mandatoryValue
        recurrent = tvm.recurrentValue
        recurrentTime = tvm.recurrentTimeValue
        taskEffort = tvm.effortValue
        maxNumber = tvm.maxPersonAssigned
        description = tvm.descriptionValue
        assignedPerson = tvm.assignedTeamMembers
        link = tvm.linkState
        comments = tvm.commentState
        pdf = tvm.pdfList
        taskHistory = tvm.taskHistory
        teamId = tvm.teamState
    }
}

fun Task.fromTaskToViewModel(): TaskViewModel {
        return TaskViewModel(
            taskId = this.taskID,
            title = this.title,
            category = this.category,
            tag = this.tag,
            startingDate = this.startingDate,
            dueDate = this.dueDate,
            mandatory = this.mandatory,
            recurrent = this.recurrent,
            recurrentTime = this.recurrentTime,
            taskEffort = this.taskEffort,
            maxNumber = this.maxNumber,
            description = this.description,
            status = this.taskStatus,
            personAssigned = this.assignedPerson.toMutableList(),
            comments = this.comments,
            links = this.link,
            pdf = this.pdf,
            taskHistory = this.taskHistory ,
            team = this.teamId,
            taskHistoryId = this.taskHistoryId
        )
}
package it.polito.madlab5.viewModel.TaskViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.Comment
import it.polito.madlab5.model.Task.History
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Task.TaskEffort
import it.polito.madlab5.model.Task.TaskStates
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskViewModel(
    var taskId: String = "",
    var title: String = "Task Name",
    var category: String = "Work",
    var tag: String = "Finance",
    var startingDate: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    var dueDate: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    var mandatory: Boolean = false,
    var recurrent: Boolean = false,
    var recurrentTime: Int = 1,
    var taskEffort: TaskEffort = TaskEffort.None,
    var maxNumber: Int = 1,
    var description: String = "",
    var status: TaskStates = TaskStates.Pending,
    var personAssigned: MutableList<Profile> = mutableListOf(),
    var comments: MutableList<Comment> = mutableListOf(),
    var links: MutableList<String> = mutableListOf(),
    var pdf: MutableList<Pair<String, String>> = mutableListOf(),
    var taskHistory: MutableList<History> = mutableListOf(),
    var team: String ="",
    var taskHistoryId: String = ""

) : ViewModel() {



    var isValid by mutableStateOf(false)
        private set


    // Title
    var titleValue by mutableStateOf(title)
        private set

    var titleError by mutableStateOf("")
        private set


    fun setTaskTitle(t: String){

        titleError = if(t == ""){
            "Name cannot be empty"
        } else if(t.length < 2 || t.length > 18)
            "Name should be between 2 and 18 characters"
        else
            ""
        titleValue = t
    }

    // Description
    var descriptionValue by mutableStateOf(description)
        private set

    fun setTaskDesctiption(d: String){
        descriptionValue = d
    }

    // Tag
    var tagValue by mutableStateOf(tag)
        private set

    fun setTaskTag(tag: String){
        tagValue = tag
    }

    // Category
    var categoryValue by mutableStateOf(category)
        private set

    fun setTaskCat(cat: String){
        categoryValue = cat
    }

    // Assigned Team Members
    var assignedTeamMembers = personAssigned.toMutableStateList()

    fun setTeamMembersTask(teamMembers: List<Profile>){
        assignedTeamMembers = teamMembers.toMutableStateList()
    }


    // DueDate
    var dueDateValue by mutableStateOf(dueDate)
        private set

    var dueDateError by mutableStateOf("")
        private set

    fun setDueDatePicker(d: String){

        val dateFormatter = DateTimeFormatter.ISO_DATE

        dueDateError = if (LocalDate.parse(d, dateFormatter).isBefore(LocalDate.parse(startingDateValue, dateFormatter))){
            "Due Date must not be before the Starting Date"
        } else if ( LocalDate.now().isAfter( LocalDate.parse(d, dateFormatter) ) ){
            "Due Date must not be before the actual date"
        } else {
            ""
        }

        startingDateError = if (LocalDate.parse(startingDateValue, dateFormatter).isAfter(LocalDate.parse(d, dateFormatter))){
            "Starting Date must not be after the Due Date"
        } else {
            ""
        }

        dueDateValue = d
    }

    // Starting Date
    var startingDateValue by mutableStateOf(startingDate)
        private set

    var startingDateError by mutableStateOf("")
        private set

    fun setStartingDatePicker(d: String){

        val dateFormatter = DateTimeFormatter.ISO_DATE

        startingDateError =
            if(LocalDate.parse(d, dateFormatter).isAfter(LocalDate.parse(dueDateValue, dateFormatter))){
                "Starting Date must not be after the Due Date"
            } else if (LocalDate.parse(d,dateFormatter).isBefore(LocalDate.now()))  {
                "Starting Date must not be before the actual date"
            } else {
                ""
            }

        dueDateError = if (LocalDate.parse(dueDateValue, dateFormatter).isBefore(LocalDate.parse(d, dateFormatter))){
            "Due Date must not be before the Starting Date"
        } else{
            ""
        }

        startingDateValue = d
    }

    // State
    var stateValue by mutableStateOf(status)
        private set


    // Mandatory
    var mandatoryValue by mutableStateOf(mandatory)
        private set

    fun setMandatoryFlag(b: Boolean){
        mandatoryValue = b
    }

    // Recurrent
    var recurrentValue by mutableStateOf(recurrent)
        private set

    fun setRecurrentFlag(b: Boolean){
        recurrentValue = b
    }

    // Recurrent Time
    var recurrentTimeValue by mutableIntStateOf(recurrentTime)
        private set

    fun addRecurrentTime(){
        recurrentTimeValue++
    }

    fun subRecurrentTime(){
        recurrentTimeValue--
    }

    // Effort
    var effortValue by mutableStateOf(taskEffort)
        private set

    fun setEffortRequired(e: TaskEffort){
        effortValue = e
    }

    var maxPersonAssigned by mutableIntStateOf(maxNumber)
        private set

    var maxPersonError by mutableStateOf("")
        private set

    fun addPerson(){
        maxPersonAssigned++

        maxPersonError = if (maxPersonAssigned < assignedTeamMembers.size){
            "The max number must be more or equal to the number of selected Team Members"
        } else {
            ""
        }
    }

    fun removePerson(){
        maxPersonAssigned--

        maxPersonError = if (maxPersonAssigned < assignedTeamMembers.size){
            "The max number must be more or equal to the number of selected Team Members"
        } else {
            ""
        }

    }



    /* Comments */
    var commentState = comments.toMutableStateList()
    /* Funzione di validazione */

    /*Link */
    var linkState = links.toMutableStateList()

    /* pdf list*/
    var pdfList = pdf.toMutableStateList()

    fun validate() {
        isValid = titleError.isBlank() && dueDateError.isBlank() && startingDateError.isBlank() && maxPersonError.isBlank()
    }

    var teamState by mutableStateOf(team)




}

fun TaskViewModel.fromViewModelToTask(): Task {
    return Task(
        title = this.titleValue,
        category =this.categoryValue,
        tag = this.tagValue,
        mandatory = this.mandatoryValue,
        recurrent = this.recurrentValue,
        recurrentTime = this.recurrentTimeValue,
        taskEffort = this.effortValue,
        taskStatus = this.stateValue,
        description = this.descriptionValue,
        startingDate = this.startingDateValue,
        dueDate = this.dueDateValue,
        maxNumber = this.maxPersonAssigned,
        assignedPerson = this.assignedTeamMembers,
        comments = this.commentState,
        link = this.linkState,
        pdf = this.pdfList,
        taskHistory = this.taskHistory,
        teamId = this.teamState,
        taskHistoryId = this.taskHistoryId
    )
}




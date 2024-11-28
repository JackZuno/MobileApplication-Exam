package it.polito.madlab5.model

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import it.polito.madlab5.database.DatabaseChats
import it.polito.madlab5.database.DatabaseComment
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.database.DatabaseTask
import it.polito.madlab5.database.DatabaseTeam
import it.polito.madlab5.model.Task.Person
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.model.chat.PersonChat
import it.polito.madlab5.model.chat.TeamChat
import it.polito.madlab5.screens.TeamScreen.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class LocalConfiguration(uid: String = "") {

    val uid = uid
    val isBottomBar = mutableStateOf(true)

    val dbTeamHandler = DatabaseTeam()
    val dbTaskHandler = DatabaseTask()
    val dbProfileHandler = DatabaseProfile()
    val dbChatsHandler = DatabaseChats()
    var personalTeamList: MutableState<Pair<LoadingState, List<Team>>> =
        mutableStateOf(Pair(LoadingState.LOADING, emptyList()))
    var personalTaskList: MutableState<MutableList<Task>> = mutableStateOf(mutableListOf())
    var personalChats: MutableList<PersonChat> = mutableListOf()
    var teamChats: MutableList<TeamChat> = mutableListOf()


    val globalTeamMembers: MutableList<Person> = mutableListOf(
        Person("Mario", "Rossi", "RedMario"),
        Person("Pippo", "Brown", "Dawg"),
        Person("John", "Doe", "BlueJohn2"),
        Person("Alice", "Smith", "GreenAli3"),
        Person("Emma", "Johnson", "YellowEm4"),
        Person("Michael", "Williams", "PurpleMich5564376"),
        Person("Sophia", "Brown", "OrangeSo6"),
        Person("Daniel", "Jones", "PinkDan7"),
        Person("Olivia", "Garcia", "BlackOli8"),
        Person("James", "Martinez", "WhiteJam9"),
        Person("Davide", "Palatroni", "Palatrauss00"),
        Person("Andrea", "Sillano", "ASillano2000"),
        Person("Riccardo", "Renda", "Renda00"),
        Person("Giacomo", "Zunino", "JackNobile69")
    )

    fun loadPersonalTeams() {

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            dbTeamHandler.getPersonalTeams(uid).collect { teams ->
                personalTeamList.value = teams

            }
        }
    }

    fun loadTeamAchievement(achievementId: String, index: Int ) {

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            personalTeamList.value.second[index].achievement =  dbTeamHandler.getTeamAchievements(achievementId)
        }
    }

    fun loadTaskHistoryAchievement(historyId: String, index: Int, taskIndex: Int) {

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {

            if(index != -1) {
                personalTeamList.value.second[index].taskList[taskIndex].taskHistory =
                    dbTaskHandler.getTaskHistory(historyId)
            }else{
                personalTaskList.value[taskIndex].taskHistory = dbTaskHandler.getTaskHistory(historyId)

            }
        }
    }

    fun loadTeamMemberStatus(teamId: String, teamIndex: Int){

        val scope  = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            dbTeamHandler.getTeamMemberStatus(teamId).collect { members ->
                personalTeamList.value.second[teamIndex].teamMemberStatus = members
            }
        }


    }
    fun loadTeamMember(teamId: String, teamIndex: Int){

        val scope  = CoroutineScope(Job() + Dispatchers.IO)

            scope.launch {dbProfileHandler.getTeamMembersFromTeam(teamId).collect{
                    members-> personalTeamList.value.second[teamIndex].teamMembers = members.toMutableList()

            }
        }
    }
    fun loadPersonalTasks() {

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            dbTaskHandler.getPersonalTask(uid).collect { tasks ->
                personalTaskList.value.clear()
                personalTaskList.value.addAll(tasks)

            }
        }
    }

    fun loadTasksFromTeam(teamId: String, teamIndex: Int) {

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            dbTaskHandler.getTasksFromTeam(teamId).collect { tasks ->
                personalTeamList.value.second[teamIndex].taskList = tasks

            }
        }
    }

    fun loadPersonalChats(loggedInUser: String){
        val scope = CoroutineScope( Job() + Dispatchers.IO)
        scope.launch {
            dbChatsHandler.getListPersonalChats(loggedInUser).collect{chats ->
                personalChats = chats.toMutableList()
            }
        }
    }

    fun checkPersonChat(profileId: String): Boolean {
        return personalChats.any { it.profileTo == profileId }
    }

    fun returnChat(profileId: String): PersonChat {
        return personalChats.filter { it.profileTo == profileId }.first()
    }

    fun loadTeamChats(loggedInUser: String){
        val scope = CoroutineScope( Job() + Dispatchers.IO )

        scope.launch {
            dbChatsHandler.getAllTeamChats(loggedInUser).collect{ chats ->
                teamChats = chats.toMutableList()
            }
        }
    }
}
package it.polito.madlab5.screens.TaskScreen

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.polito.madlab5.GeneralAppScreen
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.viewModel.TaskViewModels.TaskListViewModel

enum class TaskListScreen{
    TaskList,
    NewTask,
    TaskDetails,
    TaskFilter
}
@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun PersonalTaskList(
    user: String,
    localConf: LocalConfiguration,
    tlvm: TaskListViewModel,
    username: String,
    signOut: () -> Unit,

) {
    val navController = rememberNavController()
    val speedAnimation = 700
    tlvm.taskList.clear()
    tlvm.taskList.addAll(localConf.personalTaskList.value)


    NavHost(navController = navController,
        startDestination = TaskListScreen.TaskList.name
    ) {
        composable(route = TaskListScreen.TaskList.name,) {
           PersonalTaskListScreen(navController, user, tlvm)
            localConf.isBottomBar.value = true
        }
        composable(
            route = TaskListScreen.TaskDetails.name + "/{taskIndex}",
            enterTransition = {

                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(speedAnimation)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(speedAnimation)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(speedAnimation)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(speedAnimation)
                )
            },
        ) { backStackEntry ->
            val taskIndex = backStackEntry.arguments?.getString("taskIndex")
            val task = taskIndex?.let { tlvm.taskList[it.toInt()] }

            if (task != null) {

                val teamIndex = localConf.personalTeamList.value.second.indexOfFirst { t -> t.teamID == task.teamId}
                localConf.loadTeamMember(task.teamId, teamIndex)
                tlvm.addTeamMemberInTaskList()
                tlvm.teamMembers = mutableStateListOf<Profile>().apply { addAll(localConf.personalTeamList.value.second[teamIndex].teamMembers) }
                if (tlvm.teamMembers.size != 0){
                    tlvm.loadCommentFromTask(task.taskID, taskIndex.toInt())
                    TaskDetailsMain(navController, task, user, localConf, { signOut() }, tlvm.teamMembers, -1, taskIndex.toInt(),username)
                }
                //task.taskHistory = localConf.personalTaskList[taskIndex.toInt()].taskHistory

                localConf.isBottomBar.value = false

            }
        }
        composable(
            route = TaskListScreen.TaskFilter.name,
            enterTransition = {

                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(speedAnimation)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(speedAnimation)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.
                    Up,
                    animationSpec = tween(speedAnimation)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(speedAnimation)
                )
            },
        ) {
            FilterScreen(
                navController = navController,
                tlvm,
                tlvm.members.map { tm -> Pair(true, tm) },
                true
            )
            localConf.isBottomBar.value = false

        }
    }

}
@Composable
fun PersonalTaskListScreen(navController: NavController, user: String, tlvm: TaskListViewModel){
    //val isCreationDate = remember { mutableStateOf(tlvm.isCreationDate.value) }
    Log.d("profile id", user)
    Column ( horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)){
        Row (verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,modifier = Modifier.fillMaxWidth(0.9f)){
            Text(
                text = "My Tasks",
                modifier = Modifier,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                color = Color.Black
            )
            IconButton(onClick = { navController.navigate(GeneralAppScreen.TaskFilter.name) }) {
                Icon(imageVector = Icons.Filled.FilterAlt, contentDescription = null)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
        TaskList(tlvm.filteredTaskList, navController)
    }
}
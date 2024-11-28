package it.polito.madlab5.screens.TeamScreen

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.polito.madlab5.database.DatabaseChats
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.database.DatabaseTeam
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.TaskStates
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.model.Team.fromTeamToViewModel
import it.polito.madlab5.screens.TaskScreen.FilterScreen
import it.polito.madlab5.screens.TaskScreen.Members
import it.polito.madlab5.screens.TaskScreen.TagChip
import it.polito.madlab5.screens.TaskScreen.TaskController
import it.polito.madlab5.screens.TaskScreen.TaskDetailsMain
import it.polito.madlab5.screens.TaskScreen.TaskWrapper
import it.polito.madlab5.screens.TaskScreen.TextChip
import it.polito.madlab5.screens.chatScreen.ChatGroupView
import it.polito.madlab5.screens.profileScreen.ProfileMain
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.Purple80
import it.polito.madlab5.viewModel.TaskViewModels.TaskListViewModel
import it.polito.madlab5.viewModel.TeamViewModels.TeamListViewModel
import it.polito.madlab5.viewModel.TeamViewModels.memberStatus

enum class TeamDetailsScreen{
    TeamDetails,
    TeamEdit,
    NewTask,
    TaskDetails,
    TaskFilter,
    TaskList,
    GroupChat,
    ShowProfile,
    Achievements,
    LeaveTeam,
    InviteNewMember
}

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun TeamDetailsMain(
    navController: NavHostController,
    team: Team,
    localConf: LocalConfiguration,
    isFromLink: Boolean,
    user: String,
    signOut: () -> Unit,
    tlvm: TaskListViewModel,
    teamListViewModel: TeamListViewModel,
    teamIndex: Int,
    username: String
){
    val nestedNavController = rememberNavController()
    val speedAnimation = 700
    val team = remember {
        team
    }

    val isOpenDialogLink =  remember { mutableStateOf(isFromLink) }


    NavHost(
        navController = nestedNavController,
        startDestination = TeamDetailsScreen.TeamDetails.name
    ){
        composable(route = TeamDetailsScreen.TeamDetails.name,
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

        ){
            AddMemberDialog(openAlertDialog = isOpenDialogLink, nestedNavHostController = navController, team = team, user = user)
            TeamDetails(
                team = team, navController = navController, nestedNavHostController = nestedNavController, team.teamMemberStatusState, team.teamMembers, user, tlvm)
        }
        composable(route = TeamDetailsScreen.TeamEdit.name){
            TeamEditController(generalNavController = nestedNavController, team = team)
        }
        composable(route = TeamDetailsScreen.TaskList.name) {
            //TaskWrapper(navController = nestedNavController)
        }
        composable(route = TeamDetailsScreen.NewTask.name) {
            TaskController(generalNavController = nestedNavController, localConf, team, username)
            //bottomBarView.value = false
        }
        composable(
            route = TeamDetailsScreen.TaskDetails.name + "/{index}",
            enterTransition = {

                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(speedAnimation)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.
                    Right,
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
            val taskIndex = backStackEntry.arguments?.getString("index")
            val task = taskIndex?.let { tlvm.taskList[taskIndex.toInt()] }

            //val task = taskId?.let { DatabaseTask().getTaskById(taskId).collectAsState(initial = null)}
            if (task != null) {
                if(teamIndex != -1) { // -1 if I arrive from the deep link
                    tlvm.loadCommentFromTask(task.taskID, taskIndex.toInt())
                    TaskDetailsMain(nestedNavController, task, user, localConf, { signOut() }, tlvm.teamMembers, teamIndex, taskIndex.toInt(), username )
                }
            }
        }
        composable(
            route = TeamDetailsScreen.TaskFilter.name,
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
            val membersList = tlvm.teamMembers.map { tm -> Pair(true, tm) }
            FilterScreen(navController = nestedNavController, tlvm = tlvm, membersList = membersList, isPersonal = false)
        }
        composable(route = TeamDetailsScreen.GroupChat.name) {
            val teamChat by DatabaseChats().getTeamChat(team.teamID, user).collectAsState(initial = null)
            if (teamChat != null){
                ChatGroupView(navController = nestedNavController, tvm = team.fromTeamToViewModel(), loggedInUser = user, teamChat = teamChat!!)
            }

        }
        composable(
            route = TeamDetailsScreen.ShowProfile.name + "/{index}",
            enterTransition = {

                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(speedAnimation)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
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
            val profileIndex = backStackEntry.arguments?.getString("index")
            val profile = profileIndex?.let { team.teamMembers[profileIndex.toInt()] }
            //val isLoading = mutableStateOf(true)
            if (profile != null) {
                ProfileMain(profile, nestedNavController, user, false, { signOut() }, localConf )

            }
        }
        composable(route = TeamDetailsScreen.Achievements.name,enterTransition = {

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
            }) {
           // team.checkAchievement()
           AchievementMain(nestedNavController, team)

        }

        composable(route = TeamDetailsScreen.LeaveTeam.name) {
            LeaveTeam(team, teamListViewModel, navController, localConf, user)
        }

        composable(route = TeamDetailsScreen.InviteNewMember.name){
            InviteNewTeamMember(navController = nestedNavController, team = team)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetails(
    team: Team,
    navController: NavHostController,
    nestedNavHostController: NavHostController,
    teamMemberStatus: List<Pair<String, memberStatus>>,
    profileList: MutableList<Profile>,
    user: String,
    tlvm: TaskListViewModel
){
    val context = LocalContext.current
    val openAlertDialog = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = team.name)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        //navController.navigate(TeamListScreen.TeamList.name)
                        navController.navigate(TeamListScreen.TeamList.name)
                    }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (user == team.teamAdmin){
                                Toast.makeText(context, "The admin cannot leave the team!", Toast.LENGTH_LONG).show()
                            }else{
                                openAlertDialog.value = true
                            }
                        }) {
                        Icon(imageVector = Icons.Filled.Logout, contentDescription = null)
                    }

                    if(true) {  //team.teamAdmin.username == loggedInUser.username
                        IconButton(onClick = {
                            nestedNavHostController.navigate(TeamDetailsScreen.TeamEdit.name)
                        }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                        }
                    }

                    IconButton(onClick = { nestedNavHostController.navigate(TeamDetailsScreen.GroupChat.name) }) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                        val unreadMessages = remember { mutableStateOf(0) }

                        //countUnreadMessages(unreadMessages, team)

                        if(unreadMessages.value > 0) {
                            Badge(
                                content = { Text(text = "${unreadMessages.value}") },
                                modifier = Modifier.offset(x = 6.dp, y = (-6).dp)
                            )
                        }
                    }
                }
                //backgroundColor = MaterialTheme.colors.primary,
                //contentColor = Color.White,
                //elevation = 10.dp
            )
        },content = {
            Details(team, nestedNavHostController, teamMemberStatus, profileList, user, tlvm)
            DialogLeave(openAlertDialog = openAlertDialog, nestedNavHostController = nestedNavHostController)
        })
}

/*fun countUnreadMessages(unreadMessages: MutableState<Int>, team: Team) {
    unreadMessages.value = team.teamChat.messagesState.count{
        isFirstDateGreaterThanSecond(it.sentAt, team.teamChat.getLastAccessPerson(loggedInUserone) ) && !it.isFromMe
    }
}*/

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun Details(
    team: Team,
    navController: NavHostController,
    teamMemberStatus: List<Pair<String, memberStatus>>,
    profileList: MutableList<Profile>,
    user: String,
    tlvm: TaskListViewModel
) {
    Column(
        Modifier
            .padding(top = 60.dp)
            .verticalScroll(rememberScrollState())
            //.nestedScroll(nestedScrollConnection)
            .fillMaxSize()
            ,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        DetailHeader(team = team, navController = navController, teamMemberStatus, profileList, user)
        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.fillMaxWidth(0.8f))

        Row( modifier = Modifier
            .height(400.dp)
            ) {

            TaskWrapper( team, navController, tlvm )
        }
    }

}
@SuppressLint("UnrememberedMutableState")
@Composable
fun DetailHeader(
    team: Team,
    navController: NavHostController,
    teamMemberStatus: List<Pair<String, memberStatus>>,
    profileList: MutableList<Profile>,
    user: String
){
    // Get the status of team members
    val loggedInUserId = user
    /*var currentTeamMembersStatus by remember {
        mutableStateOf(teamMemberStatus.find { it.first == loggedInUserId })
    }*/
    var currentTeamMembersStatus = mutableStateOf(team.teamMemberStatus.find { it.first == loggedInUserId })
    val teamMembersStatus by DatabaseTeam().getTeamMemberStatus(team.teamID).collectAsState(initial = mutableListOf<Pair<String, memberStatus>>().toMutableStateList())

    Log.d("Current", currentTeamMembersStatus.toString())

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            // Team image
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Purple80)
            ) {
                if(team.image != null) {  //imageBitmap.value != null
                    Image(
                        bitmap = team.image!!.asImageBitmap(),
                        contentDescription =null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = "Group Icon",
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = team.description,
                color = Color.LightGray,
                fontWeight = FontWeight.Normal
            )
        }

        TagChip(team.category)

        Row(
            Modifier
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = Color.LightGray
            )
            Text(
                text = "Creation Date",
                color = Color.LightGray,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.weight(0.4f))
            TextChip(date = team.creationDate)
        }
        Row(
            Modifier
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Work,
                contentDescription = null,
                tint = Color.LightGray
            )
            Text(
                text = "Participation",
                color = Color.LightGray,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.weight(0.4f))
            //TextChip(date = Team.creationDate)

            ExtendedFloatingActionButton(onClick = {
                //navController.navigate(TeamDetailsScreen.InviteNewMember.name)
                if(currentTeamMembersStatus.value != null) {
                     when(currentTeamMembersStatus.value!!.second){
                         memberStatus.FullTime -> {
                             DatabaseTeam().updateTeamMemberStatus(
                                 team.teamID,
                                 loggedInUserId,
                                 memberStatus.PartTime
                             )
                             currentTeamMembersStatus.value = currentTeamMembersStatus.value!!.copy(second = memberStatus.PartTime)
                         }
                         memberStatus.PartTime -> {
                             DatabaseTeam().updateTeamMemberStatus(
                                 team.teamID,
                                 loggedInUserId,
                                 memberStatus.Erratic
                             )
                             currentTeamMembersStatus.value = currentTeamMembersStatus.value!!.copy(second = memberStatus.Erratic)
                         }
                         memberStatus.Erratic -> {
                             DatabaseTeam().updateTeamMemberStatus(
                                 team.teamID,
                                 loggedInUserId,
                                 memberStatus.FullTime
                             )
                             currentTeamMembersStatus.value = currentTeamMembersStatus.value!!.copy(second = memberStatus.FullTime)
                         }
                     }
                 }
            },
            containerColor = Purple40,
            contentColor = Color.White,
            icon = { Icon(imageVector = Icons.Filled.ChangeCircle, contentDescription = null) },
            text = {

                var text=  ""
                if(currentTeamMembersStatus.value != null){
                    text = when(currentTeamMembersStatus.value!!.second){
                        memberStatus.FullTime -> "Full Time"
                        memberStatus.PartTime -> "Part Time"
                        memberStatus.Erratic -> "Erratic"
                    }
                }
                Text(text = text) }
            )
        }

        Row(
            Modifier
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Members", color = Color.LightGray, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.weight(0.4f))
            Members(members = profileList, nestedNavHostController = navController, true, teamMembersStatus.toMutableStateList())
        }

        Row(modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically) {

            LinearProgressIndicator(
                progress = if(team.taskList.size == 0) {
                    0.toFloat()
                } else {
                    team.taskList.filter { task -> task.taskStatus == TaskStates.Completed }.size.toFloat() / team.taskList.size.toFloat()
                },
                modifier = Modifier.fillMaxWidth(),
                trackColor = Color.LightGray
            )
            Spacer(modifier = Modifier.weight(1f))

        }
        Row(modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Completed Tasks",
                modifier = Modifier,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = team.taskList.filter { task -> task.taskStatus == TaskStates.Completed }.size.toString() + "/" + team.taskList.size.toString(),
                modifier = Modifier,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                color = Color.DarkGray
            )
        }

        Row(
            Modifier
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ExtendedFloatingActionButton(onClick = {
                team.checkAchievement()
                navController.navigate(TeamDetailsScreen.Achievements.name)
            },
                containerColor = Purple40,
                contentColor = Color.White,
                icon = { Icon(imageVector = Icons.Filled.AddModerator, contentDescription = null) },
                text = { Text(text = "Achievements") })
            Spacer(modifier = Modifier.width(10.dp))
            ExtendedFloatingActionButton(onClick = {
                navController.navigate(TeamDetailsScreen.InviteNewMember.name)
            },
                containerColor = Purple40,
                contentColor = Color.White,
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                text = { Text(text = "Add Member") })
        }
    }
}

@Composable
fun DialogLeave(openAlertDialog: MutableState<Boolean>, nestedNavHostController: NavHostController) {
    when {
        // ...
        openAlertDialog.value -> {
            AlertDialogLeave(
                onDismissRequest = { openAlertDialog.value = false },
                onConfirmation = {
                    openAlertDialog.value = false
                    println("Confirmation registered") // Add logic here to handle confirmation.
                    nestedNavHostController.navigate(TeamDetailsScreen.LeaveTeam.name)
                },
                dialogTitle = "Leaving Team",
                dialogText = "Are you sure you want to leave this Team?",
                icon = Icons.Filled.Logout,
                confirmationString = "Leave"
            )
        }
    }
}


@Composable
fun AddMemberDialog(openAlertDialog: MutableState<Boolean>, nestedNavHostController: NavHostController, team: Team, user: String){

    when {
        openAlertDialog.value -> {
            AlertDialogLeave(
                onDismissRequest = {
                    openAlertDialog.value = false
                    nestedNavHostController.navigate(TeamListScreen.TeamList.name)
                },
                onConfirmation = {
                    openAlertDialog.value = false
                    /*if (!team.teamMembers.contains(loggedInUserone)){
                        team.teamMembersState.add(loggedInUserone)
                        team.teamMembers = team.teamMembersState.toMutableList()
                        team.teamMemberStatusState.add(Pair(loggedInUserone, memberStatus.FullTime))
                        team.teamMemberStatus = team.teamMemberStatusState.toMutableList()
                        team.teamChat.lastAccess.add(Pair(loggedInUserone, LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                    }*/
                    DatabaseTeam().addUserToTeam(profileId = user, teamId = team.teamID)
                    nestedNavHostController.navigate(TeamListScreen.TeamList.name)
                },
                dialogTitle = "Join Team",
                dialogText = "Do you Wanna Join Team?",
                icon = Icons.Filled.People,
                confirmationString = "Join"
            )
        }
    }
}

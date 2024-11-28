package it.polito.madlab5.screens.TeamScreen

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.polito.madlab5.LoadingScreen
import it.polito.madlab5.R
import it.polito.madlab5.database.DatabaseTeam
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.screens.chatScreen.ProfileImageTeam
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.Purple80
import it.polito.madlab5.viewModel.TaskViewModels.TaskListViewModel
import it.polito.madlab5.viewModel.TeamViewModels.TeamListViewModel
import it.polito.madlab5.viewModel.TeamViewModels.memberStatus
import kotlinx.coroutines.delay

enum class TeamListScreen {
    TeamList,
    NewTeam,
    TeamDetails,
    TeamFilter,
    LinkDetails
}

enum class LoadingState {
    LOADING,
    SUCCESS,
    EMPTY
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun TeamListController(
    localConf: LocalConfiguration,
    teamId: String?,
    loggedInUser: String,
    signOut: () -> Unit,
    teamListViewModel: TeamListViewModel,
    username: String
) {
    val navController = rememberNavController()
    val speedAnimation = 700
    var inviteTeamId = teamId
    var firstTime = remember{ mutableStateOf(true) }

    NavHost(navController = navController,
        startDestination = if (inviteTeamId.isNullOrEmpty() || !firstTime.value) {
            TeamListScreen.TeamList.name
        } else {
            TeamListScreen.LinkDetails.name
        }
    ){
        composable(route = TeamListScreen.LinkDetails.name) {
            Log.d("FOUND teamList", "${teamListViewModel.teamList.size}")
            val team = inviteTeamId?.let { it1 -> teamListViewModel.getTeamByIdDeepLink(it1) }

            if(team != null) {
                Log.d("FOUND", "TEAM")
                // User already in the group
                val tlvm = TaskListViewModel(team.taskList, team.teamMembers, team)
                val teamIndex = teamListViewModel.getIndexById(team.teamID)

                localConf.loadTasksFromTeam(team.teamID, teamIndex)
                localConf.loadTeamMember(team.teamID, teamIndex)
                localConf.loadTeamAchievement(team.achievementId, teamIndex)
                localConf.loadPersonalChats(loggedInUser)

                team.achievement = localConf.personalTeamList.value.second[teamIndex].achievement

                team.taskList = localConf.personalTeamList.value.second[teamIndex].taskList

                tlvm.teamMembers.addAll(localConf.personalTeamList.value.second[teamIndex].teamMembers)

                team.teamMemberStatus = localConf.personalTeamList.value.second[teamIndex].teamMemberStatus
                team.teamMembers = localConf.personalTeamList.value.second[teamIndex].teamMembers


                tlvm.filteredTaskList.clear()
                tlvm.filteredTaskList.addAll(team.taskList)
                tlvm.taskList.clear()
                tlvm.taskList.addAll(team.taskList)
                tlvm.addTeamMemberInTaskList()

                TeamDetailsMain(
                    team = team,
                    navController = navController,
                    tlvm = tlvm,
                    localConf = localConf,
                    teamListViewModel = teamListViewModel,
                    isFromLink = false,
                    signOut = {signOut()},
                    user = loggedInUser,
                    username = username,
                    teamIndex = teamIndex
                )
                localConf.isBottomBar.value = false
                inviteTeamId = null
            } else {
                // Look for the team in the database
                val teamFromDatabase = DatabaseTeam().getTeamById(teamId!!).collectAsState(initial = Pair(LoadingState.LOADING, null))

                when(teamFromDatabase.value.first) {
                    LoadingState.LOADING -> {
                        LoadingScreen()
                    }
                    LoadingState.SUCCESS -> {
                        // open a teamDetail with the join request
                        val tlvm = TaskListViewModel(teamFromDatabase.value.second!!.taskList, teamFromDatabase.value.second!!.teamMembers, team)

                        TeamDetailsMain(
                            team = teamFromDatabase.value.second!!,
                            navController = navController,
                            tlvm = tlvm,
                            localConf = localConf,
                            teamListViewModel = teamListViewModel,
                            isFromLink = true,
                            signOut = {signOut()},
                            user = loggedInUser,
                            username = username,
                            teamIndex = -1
                        )
                        localConf.isBottomBar.value = false
                        inviteTeamId = null
                    }
                    LoadingState.EMPTY -> {
                        // no team found
                    }
                }

                localConf.isBottomBar.value = false
                inviteTeamId = null
            }
        }
        composable(route = TeamListScreen.TeamList.name){

            TeamWrapper(navController = navController, localConf = localConf, teamListViewModel = teamListViewModel)
            localConf.isBottomBar.value = true
        }
        composable(route = TeamListScreen.NewTeam.name){

            TeamController(generalNavHostController = navController, localConf, loggedInUser)
            localConf.isBottomBar.value = false
        }
        composable(
            route = TeamListScreen.TeamDetails.name + "/{teamIndex}",
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
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(speedAnimation)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(speedAnimation)
                )
            },
        ) { backStackEntry ->
            val teamIndex = backStackEntry.arguments?.getString("teamIndex")
            val team = teamIndex?.let { teamListViewModel.getTeamByIndex(it.toInt()) }
            if (team != null) {
                localConf.loadTasksFromTeam(team.teamID, teamIndex.toInt())
                localConf.loadTeamMember(team.teamID, teamIndex.toInt())
                localConf.loadTeamMemberStatus(team.teamID, teamIndex.toInt())

                localConf.loadTeamAchievement(team.achievementId, teamIndex.toInt())
                team.achievement = localConf.personalTeamList.value.second[teamIndex.toInt()].achievement
                Log.e("ACH", team.achievement.size.toString())
                team.taskList = localConf.personalTeamList.value.second[teamIndex.toInt()].taskList
                val tlvm = TaskListViewModel(team.taskList, mutableListOf(), team)
                tlvm.teamMembers.addAll(localConf.personalTeamList.value.second[teamIndex.toInt()].teamMembers)
                //localConf.personalTeamList.value.second[teamIndex.toInt()].teamMemberStatus = DatabaseTeam().getTeamMemberStatus(team.teamID).collectAsState(initial = mutableListOf<Pair<String, memberStatus>>()).value.toMutableList()
                //team.teamMemberStatus  = localConf.personalTeamList.value.second[teamIndex.toInt()].teamMemberStatus

               // team.teamMembers.clear()
                team.teamMembers = localConf.personalTeamList.value.second[teamIndex.toInt()].teamMembers

                Log.d("TaskList", team.taskList.toString())
                tlvm.filteredTaskList.clear()
                tlvm.filteredTaskList.addAll(team.taskList)
                tlvm.taskList.clear()
                tlvm.taskList.addAll(team.taskList)
                tlvm.addTeamMemberInTaskList()

                TeamDetailsMain(
                    team = team,
                    navController = navController,
                    localConf = localConf,
                    isFromLink = false,
                    user = loggedInUser,
                    signOut = { signOut() },
                    tlvm = tlvm,
                    teamListViewModel = teamListViewModel,
                    teamIndex = teamIndex.toInt(),
                    username = username
                )
                localConf.isBottomBar.value = false//bottomBarView.value = false
            }
            /*val teamId = backStackEntry.arguments?.getString("teamId")

            val teamWithLoadingState = teamId?.let { DatabaseTeam().getTeamById(teamId).collectAsState(initial = Pair(LoadingState.LOADING, null))}

            when(teamWithLoadingState?.value?.first) {
                LoadingState.LOADING -> {
                    LoadingComponent()
                    localConf.isBottomBar.value = false
                }
                LoadingState.SUCCESS -> {
                    val team = teamWithLoadingState.value.second
                    TeamDetailsMain(
                        team = team!!,
                        navController = navController,
                        localConf = localConf,
                        isFromLink = false,
                        user = loggedInUser,
                        signOut = { signOut() }
                    )
                    localConf.isBottomBar.value = false
                }
                LoadingState.EMPTY -> {
                    //
                    localConf.isBottomBar.value = false
                }
                null -> {
                    //
                    localConf.isBottomBar.value = false
                }
            }*/
        }
        composable(
            route = TeamListScreen.TeamFilter.name,
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

            FilterScreenTeam(navController, teamListViewModel, localConf)
            localConf.isBottomBar.value = false
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TeamWrapper(
    navController: NavController,
    localConf: LocalConfiguration,
    teamListViewModel: TeamListViewModel,
){
         val isRefreshing = remember {
            mutableStateOf(false)
        }
    val pullToRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        refreshThreshold = 120.dp,
        onRefresh = {
            localConf.personalTeamList.value = Pair(LoadingState.LOADING, localConf.personalTeamList.value.second)
            localConf.loadPersonalTeams()
        })
    Column ( horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)){
        Row (verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,modifier = Modifier.fillMaxWidth(0.9f)){
            Text(
                text = "Teams",
                modifier = Modifier,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                color = Color.Black
            )
            IconButton(onClick = { navController.navigate(TeamListScreen.TeamFilter.name) }) {
                Icon(imageVector = Icons.Filled.FilterAlt, contentDescription = null)
            }

            Spacer(modifier = Modifier.weight(1f))

            ExtendedFloatingActionButton(onClick = {
                navController.navigate(TeamListScreen.NewTeam.name)
            },
                containerColor = Purple40,
                contentColor = Color.White,
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                text = { Text(text = "New Team") })
        }

        when(localConf.personalTeamList.value.first) {
            LoadingState.LOADING -> {
                isRefreshing.value = true
                // Show a loading indicator
                LoadingComponent()
            }
            LoadingState.SUCCESS -> {
                isRefreshing.value =false
                Box(modifier = Modifier
                    .pullRefresh(pullToRefreshState), contentAlignment = Alignment.TopCenter) {
                    TeamList(navController, teamListViewModel.teamList)
                    PullRefreshIndicator(
                        isRefreshing.value,
                        pullToRefreshState,
                    )
                }
            }
            LoadingState.EMPTY -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.flying),
                            contentDescription = "Image"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Text(
                            text = "No teams found! Maybe they're hiding in the cloud or taking a well-deserved coffee break...",
                            modifier = Modifier,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingComponent() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun TeamList(navController: NavController, teamList: MutableList<Team>) {
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(teamList) { i, team  ->
            TeamItem(navController, team, i )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamItem(navController: NavController, team: Team, index: Int) {
    var itemWidth by remember {
        mutableStateOf(0.dp)
    }
    val localDensity = LocalDensity.current

    Column (
        Modifier
            .height(180.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight()
                .onGloballyPositioned {
                    itemWidth =
                        with(localDensity) { it.size.height.toDp() }
                }
                .padding(vertical = 10.dp)
                .shadow(5.dp, shape = RoundedCornerShape(10), spotColor = Color.Black)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Purple80,
                            Color.White
                        )
                    )
                )
                .clip(RoundedCornerShape(10)),
            onClick = { navController.navigate(TeamListScreen.TeamDetails.name+"/${index}") },
            //colors = /*CardDefaults.cardColors(containerColor = TaskBg),*/
            border = BorderStroke(2.dp, Color.White),
        ) {
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.Top){

                    ProfileImageTeam(team.image, sizeBox = 60.dp, sizeIcon = 36.dp)

                    Spacer(modifier = Modifier.weight(1f))


                    TagChip(team.category)
                }

                Row(modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = team.name,
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 28.sp),
                        color = Color.Black
                    )

                }
            }
        }
    }
}

@Composable
fun TagChip(category: String) {
    val bgColor = Purple80
    AssistChip(
        onClick = { /*TODO*/ },
        label = {
            Text(
                text = category,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxWidth()
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = bgColor
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = Color.Transparent
        )
    )
}

fun leaveTeam(teamlvm: TeamListViewModel, team: Team, localConf: LocalConfiguration, user: String) {
    DatabaseTeam().removeUserFromTeam(user, team.teamID)
    teamlvm.teamList.remove(team)
}
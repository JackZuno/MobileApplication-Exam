package it.polito.madlab5.screens.TaskScreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.polito.madlab5.database.DatabaseComment
import it.polito.madlab5.database.DatabaseTask
import it.polito.madlab5.database.DatabaseTeam
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.Comment
import it.polito.madlab5.model.Task.History
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Task.TaskEffort
import it.polito.madlab5.model.Task.TaskHistoryEdit
import it.polito.madlab5.model.Task.TaskStates
import it.polito.madlab5.screens.TeamScreen.ProfileImageMemberChip
import it.polito.madlab5.screens.TeamScreen.TeamDetailsScreen
import it.polito.madlab5.screens.profileScreen.ProfileMain
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.completeBg
import it.polito.madlab5.ui.theme.completeText
import it.polito.madlab5.ui.theme.effortHigh
import it.polito.madlab5.ui.theme.effortHighText
import it.polito.madlab5.ui.theme.effortLow
import it.polito.madlab5.ui.theme.effortLowText
import it.polito.madlab5.ui.theme.effortMedium
import it.polito.madlab5.ui.theme.effortMediumText
import it.polito.madlab5.ui.theme.effortNone
import it.polito.madlab5.ui.theme.effortNoneText
import it.polito.madlab5.ui.theme.inProgressBg
import it.polito.madlab5.ui.theme.inProgressText
import it.polito.madlab5.ui.theme.lightGrayCustom
import it.polito.madlab5.ui.theme.linkColor
import it.polito.madlab5.ui.theme.overDueBg
import it.polito.madlab5.ui.theme.overDueText
import it.polito.madlab5.ui.theme.pendingBg
import it.polito.madlab5.ui.theme.pendingText
import it.polito.madlab5.viewModel.TeamViewModels.memberStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class TaskDetailsScreen{
    TaskDetails,
    TaskHistory,
    TaskEdit,
    ShowProfile
}
@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TaskDetailsMain(
    navController: NavHostController,
    task: Task,
    loggedInUser: String,
    localConf: LocalConfiguration,
    signOut: () -> Unit,
    teamMembers: SnapshotStateList<Profile>,
    teamIndex: Int,
    taskIndex: Int,
    username: String
) {
    val nestedNavController = rememberNavController()
    val speedAnimation = 700

    val taskState = remember {
        task
    }

    Log.d("TASKMEMBERS", taskState.assignedPerson.toString())

    NavHost(
        navController = nestedNavController,
        startDestination = TaskDetailsScreen.TaskDetails.name
    ) {
        composable(route = TaskDetailsScreen.TaskDetails.name ) {
            TaskDetails(navController = navController, nestedNavController,task = taskState, teamMembers = teamMembers, loggedInUser = loggedInUser, username = username)
        }
        composable(
            route = TaskDetailsScreen.TaskHistory.name,
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
           localConf.loadTaskHistoryAchievement(task.taskHistoryId, teamIndex, taskIndex)
            if(teamIndex != -1) {
                task.taskHistory = localConf.personalTeamList.value.second[teamIndex].taskList[taskIndex].taskHistory
            }else{
                task.taskHistory = localConf.personalTaskList.value[taskIndex].taskHistory
            }
            TaskHistory(nestedNavController,task)
        }

        composable(route = TaskDetailsScreen.TaskEdit.name) {
            EditTaskInternalController(navController = nestedNavController, task = taskState, username)
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
            val profileIndex = backStackEntry.arguments?.getString("index")
            val profile = profileIndex?.let { taskState.assignedPerson[profileIndex.toInt()] }
            //val isLoading = mutableStateOf(true)
            if (profile != null) {
                ProfileMain(profile, nestedNavController, localConf.uid, false, { signOut() }, localConf )
            }
        }
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetails(
    navController: NavHostController,
    nestedNavHostController: NavHostController,
    task: Task,
    teamMembers: SnapshotStateList<Profile>,
    loggedInUser: String,
    username: String
){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = task.title)
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        nestedNavHostController.navigate(TaskDetailsScreen.TaskEdit.name)
                    }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                    }
                    /*IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    }*/
                    IconButton(onClick = { nestedNavHostController.navigate(TaskDetailsScreen.TaskHistory.name)}) {
                        Icon(imageVector = Icons.Filled.History, contentDescription = null)
                    }
                }
                //backgroundColor = MaterialTheme.colors.primary,
                //contentColor = Color.White,
                //elevation = 10.dp
            )
        },content = {

        Details(task, nestedNavHostController, teamMembers, loggedInUser, username)

        })

}
@Composable
fun Details(
    task: Task,
    nestedNavHostController: NavHostController,
    teamMembers: SnapshotStateList<Profile>,
    loggedInUser: String,
    username: String
){

    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Column(modifier = Modifier
            .fillMaxHeight(0.98f)
            .fillMaxWidth(0.9f)
            .verticalScroll(rememberScrollState()))
            {
            Row() {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.WifiTethering,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                        Text(
                            text = "Status",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(0.1f))
                        CustomDropdownMenu(
                            task,
                            list = listOf("Completed", "Over Due", "In Progress", "Pending"),
                            color = Purple40,
                            modifier = Modifier,

                            {},
                            username
                        )
                        // StatusChip(status = TaskStates.Pending)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Hardware,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                        Text(
                            text = "Effort",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(0.4f))
                        EffortChip(status = task.taskEffort)
                        //StatusChip(status = TaskStates.Pending)
                    }
                    if (!task.recurrent) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                            Text(
                                text = "Start Date",
                                color = Color.LightGray,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.weight(0.4f))
                            //Text(text = "December 21, 2024", color = Color.DarkGray, fontWeight = FontWeight.Normal)
                            TextChip(date = task.startingDate)

                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                            Text(
                                text = "Due Date",
                                color = Color.LightGray,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.weight(0.4f))
                            //Text(text = "December 23, 2024", color = Color.DarkGray, fontWeight = FontWeight.Normal)
                            TextChip(date = task.dueDate)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                            Text(
                                text = "Next",
                                color = Color.LightGray,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.weight(0.4f))
                            //Text(text = "December 21, 2024", color = Color.DarkGray, fontWeight = FontWeight.Normal)

                            TextChip(date = "In ${task.recurrentTime} days")
                        }

                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Category,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                        Text(
                            text = "Category",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(0.4f))
                        TagChip(task.category)
                        //StatusChip(status = TaskStates.Pending)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Tag,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                        Text(text = "Tag", color = Color.LightGray, fontWeight = FontWeight.Normal)
                        Spacer(modifier = Modifier.weight(0.4f))
                        TagChip(task.tag)
                        //StatusChip(status = TaskStates.Pending)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.SupervisorAccount,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                        Text(
                            text = "Assigned To",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(0.4f))
                        Members(task.assignedPerson, nestedNavHostController, false)

                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                        Text(text = "Link", color = Color.LightGray, fontWeight = FontWeight.Normal)
                        Spacer(modifier = Modifier.weight(0.4f))
                        LinkChip(task.link, task.pdf)

                    }
                }
                Divider(color = Color.LightGray, thickness = 1.dp)
            }
                Row() {
            Column(//modifier = Modifier.fillMaxWidth(0.5f), TODO serve per la parte statica
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

                /*TODO if che fa solo statico description se non accettata la task o il tab*/
                //Text(text = "Description", color = Color.Black, fontWeight = FontWeight.Normal)
                //Divider(color = Color(0xFF6650a4), thickness = 1.dp)

                    TabScreen(task, nestedNavHostController, teamMembers, loggedInUser)
                }

            }
        }
    }
}
@Composable
fun Members(members: List<Profile>, nestedNavHostController: NavHostController, isFromTeam: Boolean, memberStatusL: MutableList<Pair<String, memberStatus>> = mutableListOf()){
    // Add members
    LazyRow(
        Modifier.fillMaxWidth(0.4f),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        itemsIndexed(members) {i, it ->
            val status = memberStatusL.find { p->p.first == it.id }?.second
            if (status == null){
                MemberChipDetails(
                    user = it,
                    nestedNavHostController = nestedNavHostController,
                    isFromTeam = isFromTeam,
                    memberStatus = memberStatus.FullTime,
                    index = i
                )
            }else {
                MemberChipDetails(
                    user = it,
                    nestedNavHostController = nestedNavHostController,
                    isFromTeam = isFromTeam,
                    memberStatus = status,
                    index = i
                )
            }
        }
    }
}

@Composable
fun MemberChipDetails(
    modifier: Modifier = Modifier,
    user: Profile,
    nestedNavHostController: NavHostController,
    isFromTeam: Boolean,
    memberStatus: memberStatus,
    index: Int
) {
    AssistChip(
        onClick = {
            if (isFromTeam){
                nestedNavHostController.navigate(TeamDetailsScreen.ShowProfile.name+"/${index}")
            }else{
                nestedNavHostController.navigate(TaskDetailsScreen.ShowProfile.name+"/${index}")
            }
                  },
        label = {
            Box(
                contentAlignment = Alignment.CenterStart
            ) {
                ProfileImageMemberChip(memberProfile = user)
            }
            /*Box(modifier = Modifier
                .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ProfileImageMemberChip(memberProfile = user)
            }*/
        },
        shape = CircleShape,
        colors = AssistChipDefaults.assistChipColors(
            containerColor =  White
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = if (isFromTeam) memberColor(memberStatus) else Transparent ,
            borderWidth = 2.dp

        ),
        modifier = modifier
            .size(65.dp)
    )
}
fun memberColor(memberS: memberStatus): Color {
    when(memberS){
        memberStatus.FullTime -> return completeBg
        memberStatus.PartTime -> return pendingBg
        memberStatus.Erratic -> return overDueBg
    }
}
@Composable
fun TaskMemberChip(
    modifier: Modifier = Modifier,
    username: String,
    //profileImageUrl: String,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AssistChip(
            onClick = { },
            label = {
                Column(
                    //verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon
                    Row(
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile Image",

                            modifier = Modifier
                                .size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Username
                    Row(
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 14.sp),

                        )
                    }
                }
            },
            shape = RoundedCornerShape(100),
            colors = AssistChipDefaults.assistChipColors(
                containerColor =  White
            ),
            border = AssistChipDefaults.assistChipBorder(
                borderColor =  lightGrayCustom,
                borderWidth = 2.dp
            ),
            modifier = modifier
        )
    }
}

@Composable
fun TextChip(date: String, color: Color = Color.Black) {
    AssistChip(
        onClick = { /*TODO*/ },
        label = {
            Text(
                text = date,
                textAlign = TextAlign.Right,
                color = color,
                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Right,
                    fontSize = 14.sp
                )
            )
        },
        colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent),
        border = AssistChipDefaults.assistChipBorder(borderColor = Color.LightGray)
    )
}

@Composable
fun LinkChip(links: MutableList<String>, pdf: MutableList<Pair<String, String>>) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    LazyRow (
        Modifier.fillMaxWidth(0.40f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    )
    { items(links){
    AssistChip(
        onClick = {
            if (it.contains("https://")){
                uriHandler.openUri(it)
            }else{
                uriHandler.openUri("https://$it")
            }
                  },
        leadingIcon = { Icon(imageVector = Icons.Filled.Link, contentDescription = null) },
        label = {
            Text(
                text = it,
                textAlign = TextAlign.Right,
                color = linkColor,
                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Right,
                    fontSize = 14.sp
                )
            )
        },
        colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent),
        border = AssistChipDefaults.assistChipBorder(borderColor = Color.LightGray)
    )

    }
        Log.d("TASK DOC YOO", pdf.size.toString())
        items(pdf){
            AssistChip(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW)

                    DatabaseTask().getDocument(intent, it.second, context, it.first)
                },
                leadingIcon = { Icon(imageVector = Icons.Filled.InsertDriveFile, contentDescription = null) },
                label = {
                    Text(
                        text = it.first,
                        textAlign = TextAlign.Right,
                        color = linkColor,
                        style =
                        MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Right,
                            fontSize = 14.sp
                        )
                    )
                },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent),
                border = AssistChipDefaults.assistChipBorder(borderColor = Color.LightGray)
            )
        }
    }
}

@Composable
fun TagChip(tag: String){
    LazyRow (
        Modifier
           ,

        horizontalArrangement = Arrangement.spacedBy(8.dp)
    )
    { items(1) {
        AssistChip(
            onClick = { /*TODO*/ },
            label = { Text(text = tag,
                textAlign = TextAlign.Center,
                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp)
            )
            },
            shape = RoundedCornerShape(20),
            colors = AssistChipDefaults.assistChipColors(containerColor = lightGrayCustom),
            border = AssistChipDefaults.assistChipBorder(borderColor = Color.LightGray)

            )
        }
    }

}

@Composable
fun CustomDropdownMenu(
    task: Task, // Menu Options
    list: List<String>, // Default Selected Option on load
    color: Color, //
    modifier: Modifier,
    onSelected: (Int) -> Unit, // Pass the Selected Option,
    username: String
) {
    var expand by remember { mutableStateOf(false) }
    val listItem: List<@Composable ()-> Unit> = listOf({ StatusChipMenuItem(status = TaskStates.Completed, onClick = {
        expand = !expand
    }) }, { StatusChipMenuItem(status = TaskStates.OverDue, onClick = {
        expand = !expand
    }) }, { StatusChipMenuItem(status = TaskStates.InProgress, onClick = {
        expand = !expand
    }) }, { StatusChipMenuItem(status = TaskStates.Pending, onClick = {
        expand = !expand
    }) })
    val defaultSelect= when(task.taskStatus){
        TaskStates.Completed -> {
            0
        }
        TaskStates.OverDue -> {
            1
        }
        TaskStates.InProgress -> {
            2
        }
        TaskStates.Pending-> {
            3
        }
        TaskStates.All-> {
            4
        }

    }
    var selectedIndex by remember { mutableIntStateOf(defaultSelect) }
    Box(
        modifier
            .clickable {
                expand = true
            },
        contentAlignment = Alignment.Center
    ) {
        when(selectedIndex){
            0 -> { task.taskStatus = TaskStates.Completed }
            1 -> {task.taskStatus =  TaskStates.OverDue }
            2 -> {task.taskStatus = TaskStates.InProgress }
            3 -> {task.taskStatus =  TaskStates.Pending }

        }
        if (selectedIndex != defaultSelect) {

            task.addNewHistoryItem(
                History(
                    TaskHistoryEdit.Status.name, task.taskStatus.name,
                LocalDate.now().format(
                    DateTimeFormatter.ISO_DATE), username )
            )
            DatabaseTask().updateTaskHistory(task.taskHistoryId, task.taskHistory)

        }
        listItem[selectedIndex].invoke().also {
            DatabaseTask().updateTaskStatus(taskId = task.taskID, newStatus = TaskStates.entries[selectedIndex])
        }

        DropdownMenu(
            expanded = expand,
            onDismissRequest = {
                expand = false
            },
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
            modifier = Modifier
                .background(White)
                .fillMaxWidth(.4f)
        ) {
            list.forEachIndexed { index, item ->
                DropdownMenuItem(text = {
                    Text(
                    text = item,
                    color = color,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )},
                    onClick = {
                        selectedIndex = index
                        expand = false
                        onSelected(selectedIndex)
                    }
                )
            }
        }

    }
}

@Composable
fun StatusChipMenuItem(status: TaskStates, onClick:()-> Unit){
    var statusLabel = ""
    var bgColor: Color = Color.Transparent
    var textColor: Color = Color.Transparent
    if(status == TaskStates.Completed){
        statusLabel = "Completed"
        bgColor = completeBg
        textColor = completeText

    }else if(status == TaskStates.InProgress){
        statusLabel = "In Progress"
        bgColor = inProgressBg
        textColor = inProgressText

    }else if(status == TaskStates.OverDue){
        statusLabel = "Over Due"
        bgColor = overDueBg
        textColor = overDueText

    }else if(status == TaskStates.Pending){
        statusLabel = "Pending"
        bgColor = pendingBg
        textColor = pendingText

    }else if(status == TaskStates.All){
        statusLabel = "All"
        bgColor = Color.Gray
        textColor = Color.DarkGray
    }



    AssistChip(onClick =  onClick ,
        label = { Text(text = statusLabel, color = textColor, textAlign = TextAlign.Center , modifier = Modifier
            .width(70.dp)
            .fillMaxWidth()) },
        colors = AssistChipDefaults.assistChipColors(containerColor = bgColor),
        border = AssistChipDefaults.assistChipBorder(borderColor = Color.Transparent), trailingIcon = {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = White
            )
        })


}



@Composable
fun EffortChip(status: TaskEffort){
    var statusLabel = ""
    var bgColor: Color = Color.Transparent
    var textColor: Color = Color.Transparent
    if(status == TaskEffort.None){
        statusLabel = "None"
        bgColor = effortNone
        textColor = effortNoneText

    }else if(status == TaskEffort.Low){
        statusLabel = "Low"
        bgColor = effortLow
        textColor = effortLowText

    }else if(status == TaskEffort.Medium){
        statusLabel = "Medium"
        bgColor = effortMedium
        textColor = effortMediumText

    }else if(status == TaskEffort.High){
        statusLabel = "High"
        bgColor = effortHigh
        textColor = effortHighText
    }

    AssistChip(onClick =  {} ,
        label = { Text(text = statusLabel, color = textColor, textAlign = TextAlign.Center , modifier = Modifier
            .width(90.dp)
            .fillMaxWidth()) },
        colors = AssistChipDefaults.assistChipColors(containerColor = bgColor),
        border = AssistChipDefaults.assistChipBorder(borderColor = Color.Transparent))
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun TabScreen(
    task: Task,
    nestedNavHostController: NavHostController,
    teamMembers: SnapshotStateList<Profile>,
    loggedInUser: String
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("Description", "Comments")
    val commentsLazy = rememberLazyListState()
    val comments = mutableStateListOf<Comment>()


    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        when (tabIndex) {
            0 -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                Text(text = task.description, color = Color.Black, fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center)
            }
            1 -> {

                    CommentsWrapper(commentsLazy, task = task, comments, nestedNavHostController, teamMembers)
                    Spacer(modifier = Modifier.weight(1f))
                    Column(modifier = Modifier.fillMaxHeight()) {
                    CommentTextField(commentsLazy, coroutineScope, task, comments, teamMembers.first { tm -> tm.id == loggedInUser })
                    }

            }
        }
    }
}


@SuppressLint("UnrememberedMutableState")
@Composable
fun CommentsWrapper(
    commentsLazy: LazyListState,
    task: Task,
    comments: SnapshotStateList<Comment>,
    nestedNavHostController: NavHostController,
    teamMembers: SnapshotStateList<Profile>
){
    val localDensity = LocalDensity.current
    comments.addAll(task.comments)

    Row (

    ){
        LazyColumn(
            modifier = Modifier
                .weight(1f, false)
                .heightIn(100.dp, 400.dp),

            state = commentsLazy
        ) {
            //comments.forEach { comment ->
            //  Comment(name = comment.first, text = comment.second, profile = comment.third)
            //Spacer(modifier = Modifier.height(15.dp))
            items(comments) {
                var index = 0
                for ((i, tm) in teamMembers.withIndex()){
                    if (tm.id == it.profileId){
                        index = i
                        break
                    }
                }
                Comment(it, nestedNavHostController, teamMembers.first { tm -> tm.id == it.profileId }, index)
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}

@Composable
fun Comment(comment: Comment, nestedNavHostController: NavHostController, user: Profile, index: Int){

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {

        MemberChipDetails(
            user = user,
            nestedNavHostController = nestedNavHostController,
            isFromTeam = false,
            memberStatus = memberStatus.FullTime,
            index = index
        )

        Text(
            text = comment.profileName!!,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 14.sp),
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp)
            )
    }
    Row(modifier = Modifier
        .padding(top = 5.dp)){
        Text(text = comment.text, color = Color.Black, fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Divider(color = Color.LightGray, thickness = 1.dp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentTextField(
    scrollState: LazyListState,
    coroutineScope: CoroutineScope,
    task: Task,
    comments: SnapshotStateList<Comment>,
    user: Profile
) {
    var comment by rememberSaveable { mutableStateOf("") }

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comment") },
            modifier = Modifier
                .fillMaxWidth(0.8f),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White
            )
        )
        Button(
            onClick = {

                val newComment = Comment(user.id, user.name, task.taskID, comment, LocalDateTime.now().toString())
                if (comment.isNotEmpty()) {
                    comment = ""
                    task.addComment(newComment)
                    comments.add(newComment)
                    DatabaseComment().addComment(newComment)
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(
                            if (comments.size > 0) {
                                comments.size - 1
                            } else 0
                        )
                    }
                }
                      },
            colors = ButtonDefaults.buttonColors(containerColor = White)
        ) {
            Icon(imageVector = Icons.Filled.Send, contentDescription = null, tint = Color.Black)
        }
    }

}
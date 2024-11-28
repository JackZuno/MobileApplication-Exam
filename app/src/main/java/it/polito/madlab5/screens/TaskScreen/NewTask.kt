package it.polito.madlab5.screens.TaskScreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.database.DatabaseTask
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.History
import it.polito.madlab5.model.Task.TaskEffort
import it.polito.madlab5.model.Task.TaskHistoryEdit
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.screens.TeamScreen.ProfileImageMemberChip
import it.polito.madlab5.screens.TeamScreen.TeamDetailsScreen
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.effortHigh
import it.polito.madlab5.ui.theme.effortHighText
import it.polito.madlab5.ui.theme.effortLow
import it.polito.madlab5.ui.theme.effortLowText
import it.polito.madlab5.ui.theme.effortMedium
import it.polito.madlab5.ui.theme.effortMediumText
import it.polito.madlab5.ui.theme.effortNone
import it.polito.madlab5.ui.theme.effortNoneText
import it.polito.madlab5.viewModel.TaskViewModels.TaskViewModel
import it.polito.madlab5.viewModel.TaskViewModels.fromViewModelToTask
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

enum class TaskScreen(){
    Step1,
    Step2,
    Step3,
    TaskFinished
}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun TaskController(generalNavController: NavHostController, localConf: LocalConfiguration, team: Team, username: String){
    val navController = rememberNavController()
    val taskViewModel = remember {
        TaskViewModel(
            category = team.category,
            team = team.teamID,
        )
    }
    val teamMembersDB by DatabaseProfile().getTeamMembersFromTeam(team.teamID).collectAsState(initial = emptyList())

    NavHost(navController = navController,
            startDestination = TaskScreen.Step1.name
    ){
        composable(route = TaskScreen.Step1.name){ navBackstackEntry ->
            CreateTaskStep1(navController = navController, generalNavController=generalNavController, taskViewModel, team)
        }
        composable(route = TaskScreen.Step2.name){
            CreateTaskStep2(navController = navController, taskViewModel, teamMembersDB)

        }
        composable(route = TaskScreen.Step3.name){
            CreateTaskStep3(navController = navController, taskViewModel, teamMembersDB)
        }
        composable(route = TaskScreen.TaskFinished.name){
            CreateTaskDone(navController = generalNavController, taskViewModel, team, username )
             /*Modificare NavController*/
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskStep1(navController: NavHostController, generalNavController: NavHostController, taskViewModel: TaskViewModel, team: Team){

    // State to track whether the settings for recurrent are true or false
    var isSettingsOpen by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step 1-3
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85F)
                .padding(bottom = 10.dp)
        ) {
            Text(
                text = "Step 1 of 3",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Color.LightGray,
                    fontSize = 18.sp
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85F)
                .padding(bottom = 8.dp)
        ) {
            LinearProgressIndicator(
                progress = 0.33F,
                color = Purple40,
                strokeCap = StrokeCap.Butt,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.85F)
            ) {
                Text(
                    text = "Create a new Task",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        Color.Black,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Task name
            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Enter the task name",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.75F)
                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 10.dp)
            ) {
                OutlinedTextField(
                    value = taskViewModel.titleValue,
                    onValueChange = taskViewModel::setTaskTitle,
                    label = { Text(text = "Name") },
                    isError = taskViewModel.titleError.isNotBlank(),
                    supportingText = {
                        if (taskViewModel.titleError.isNotBlank())
                            Text(text = taskViewModel.titleError)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLeadingIconColor = Purple40,
                        focusedTrailingIconColor = Purple40,
                        cursorColor = Purple40,
                        focusedLabelColor = Purple40
                    )
                )
            }


            Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85f))

            // Task tag
            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Enter task tag",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            val teamCategory = team.category
            taskViewModel.setTaskCat(teamCategory)
            ShowCategoryTask(teamCategory)

            SelectTagTask(taskViewModel = taskViewModel)

            Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85f))

            // Starting and due date
            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Enter the starting and due date",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Starting date
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
                ) {
                    Text(
                        text = "Starting date:",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.LightGray,
                            fontSize = 16.sp
                        )
                    )
                }
                Column {
                    DateSelection(taskViewModel, "startingDate")
                }
            }

            // Due date
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
                ) {
                    Text(
                        text = "Due date:",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.LightGray,
                            fontSize = 16.sp
                        )
                    )
                }
                Column(
                ) {
                    DateSelection(taskViewModel, "dueDate")
                }
            }

            Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85f))

            // Mandatory
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = taskViewModel.mandatoryValue,
                    onCheckedChange = taskViewModel::setMandatoryFlag
                )
                Text(
                    text = "Mandatory",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        Color.LightGray,
                        fontSize = 18.sp
                    )
                )
            }

            // Recurrent
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = taskViewModel.recurrentValue,
                    onCheckedChange = taskViewModel::setRecurrentFlag
                )
                Text(
                    text = "Recurrent",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        Color.LightGray,
                        fontSize = 18.sp
                    )
                )

                if(taskViewModel.recurrentValue) {
                    IconButton(onClick = { isSettingsOpen = true }) {
                        Icon(Icons.Rounded.SettingsSuggest, contentDescription = "Settings Recurrent")
                    }

                    SettingRecurrentPopUp(isSettingOpen = isSettingsOpen, settingPopUpOpenChange = { isSettingsOpen = !isSettingsOpen}, tvm = taskViewModel )
                }
            }
        }

        Button(
            onClick = {
                taskViewModel.validate()
                if (taskViewModel.isValid) {
                    navController.navigate(TaskScreen.Step2.name)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 0.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(
                text = "Next", style = MaterialTheme.typography.headlineMedium.copy(
                    Color.White,
                    fontSize = 25.sp
                )
            )
        }

        Button(
            onClick = {
                generalNavController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 0.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Purple40)
        ) {
            Text(
                text = "Back", style = MaterialTheme.typography.headlineMedium.copy(
                    Purple40,
                    fontSize = 25.sp
                )
            )
        }
    }
}

@Composable
fun CreateTaskStep2(navController: NavHostController, tvm: TaskViewModel, teamMembers: List<Profile>){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85F)
                .padding(bottom = 10.dp)
        ) {
            Text(
                text = "Step 2 of 3",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Color.LightGray,
                    fontSize = 18.sp
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(0.85F)
                .padding(bottom = 8.dp)
        ) {
            LinearProgressIndicator(
                progress = 0.66F,
                color = Purple40,
                strokeCap = StrokeCap.Butt,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Enter the minimum effort required",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            SliderTaskEffortBar(tvm = tvm)

            Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85f))

            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Enter the max number of people required",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            SelectMaxMembers(tvm = tvm, teamMembers)

            Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85f))

            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Enter the task description",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            OutlinedTextField(
                value = tvm.descriptionValue,   //vm.descriptionValue
                onValueChange = tvm::setTaskDesctiption,
                label = { Text(text = "Description") },
                /*isError = vm.descriptionError.isNotBlank(),
            supportingText = {
                if (vm.descriptionError.isNotBlank())
                    Text(text = vm.descriptionError)
            },*/
                minLines = 3,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .fillMaxWidth(0.80f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    focusedLeadingIconColor = Purple40,
                    focusedTrailingIconColor = Purple40,
                    cursorColor = Purple40,
                    focusedLabelColor = Purple40
                )
            )

            Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85f))

            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Insert link or other documents",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            //Insert link or
            AddNewLinkOrDocument(tvm = tvm)
        }

        Button(
            onClick = {
                tvm.validate()
                if (tvm.isValid) {
                    navController.navigate(TaskScreen.Step3.name)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 0.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(
                text = "Next", style = MaterialTheme.typography.headlineMedium.copy(
                    Color.White,
                    fontSize = 25.sp
                )
            )
        }

        Button(
            onClick = {
                navController.navigate(TaskScreen.Step1.name)
            }, /*DA MODIFICARE*/
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 0.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Purple40)
        ) {
            Text(
                text = "Back", style = MaterialTheme.typography.headlineMedium.copy(
                    Purple40,
                    fontSize = 25.sp
                )
            )
        }
    }
}

@Composable
fun CreateTaskStep3(navController: NavHostController, tvm: TaskViewModel, teamMembers: List<Profile>){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85F)
                .padding(bottom = 10.dp)
        ) {
            Text(
                text = "Step 3 of 3",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Color.LightGray,
                    fontSize = 18.sp
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(0.85F)
                .padding(bottom = 8.dp)
        ) {
            LinearProgressIndicator(
                progress = 1F,
                color = Purple40,
                strokeCap = StrokeCap.Butt,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .weight(1f) //, false
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nSelectedMember = tvm.assignedTeamMembers.size
            val maxMembers = tvm.maxPersonAssigned

            // Task name
            Row(
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = "Assign to someone (${nSelectedMember}/${maxMembers}):",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Add members
            MemberChipList(tvm.assignedTeamMembers, nSelectedMember, maxMembers, teamMembers)
        }

        Button(
            onClick = {
                navController.navigate(TaskScreen.TaskFinished.name)
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 0.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(
                text = "Create", style = MaterialTheme.typography.headlineMedium.copy(
                    Color.White,
                    fontSize = 25.sp
                )
            )
        }

        Button(
            onClick = {
                navController.navigate(TaskScreen.Step2.name)
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 0.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Purple40)
        ) {
            Text(
                text = "Back", style = MaterialTheme.typography.headlineMedium.copy(
                    Purple40,
                    fontSize = 25.sp
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateTaskDone(navController: NavHostController, taskViewModel: TaskViewModel, team: Team, username: String){

    FunctionCompleted(
        nameComponent = "Task",
        screenToReturn = TeamDetailsScreen.TeamDetails.name,
        nameCompletion = taskViewModel.titleValue,
        navController = navController,
        taskViewModel = taskViewModel,
        team = team,
        username = username
    )
}

@Composable
fun MemberChipList(selectedMembers: MutableList<Profile>, nSelectedMember: Int, maxMembers: Int, teamMembers: List<Profile>) {
    LazyRow(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(0.9f)
    ) {
        items(teamMembers) { user ->
            val username = user.username
            val isSelected = selectedMembers.any { it.username == username }
            MemberChip(
                user,
                modifier = Modifier
                    .size(90.dp, 100.dp),  //width - height
                //profileImageUrl = "profile_image_url_$index",
                username = username,
                isSelected = isSelected,
                onSelected = { selected ->
                    if(selected && nSelectedMember < maxMembers) {
                        selectedMembers.add(user)
                    } else {
                        selectedMembers.removeIf { it.username == username }
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun DateSelection(tvm: TaskViewModel, type: String){
    val date = remember { mutableStateOf(LocalDate.now())}
    val isOpen = remember { mutableStateOf(false)}

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.width(180.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Purple40,
            ),
            readOnly = true,
            value = if (type == "startingDate"){ tvm.startingDateValue} else { tvm.dueDateValue },
            label = { Text("Date") },
            onValueChange = {
            },
            trailingIcon = {
                IconButton(
                    onClick = { isOpen.value = true }
                ) {
                    Icon(imageVector = Icons.Rounded.DateRange, contentDescription = "Calendar")
                }
            },
            isError = if(type == "startingDate"){ tvm.startingDateError.isNotBlank() } else { tvm.dueDateError.isNotBlank() },
            supportingText = {
                if (tvm.startingDateError.isNotBlank() && type =="startingDate")
                    Text(text = tvm.startingDateError)
                else if (tvm.dueDateError.isNotBlank() && type=="dueDate")
                    Text(text = tvm.dueDateError)
            }
        )
    }

    if (isOpen.value) {
        CustomDatePickerDialog(
            onAccept = {
                isOpen.value = false // close dialog
                if (it != null) { // Set the date
                    date.value = Instant
                        .ofEpochMilli(it)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()

                    if (type == "startingDate"){
                        tvm.setStartingDatePicker(date.value.format(DateTimeFormatter.ISO_DATE))
                    }else{
                        tvm.setDueDatePicker(date.value.format(DateTimeFormatter.ISO_DATE))
                    }
                }
            },
            onCancel = {
                isOpen.value = false //close dialog
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onAccept: (Long?) -> Unit,
    onCancel: () -> Unit
) {
    val state = rememberDatePickerState()

    // Minimum date (today)
    val minDate = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    DatePickerDialog(
        onDismissRequest = { },
        confirmButton = {
            Button(onClick = { onAccept(state.selectedDateMillis) }) {
                Text("Accept")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FunctionCompleted(nameComponent: String, screenToReturn : String, nameCompletion: String, navController: NavHostController, taskViewModel: TaskViewModel, team: Team, username: String) {
    Surface(
        Modifier.fillMaxSize(),
        color = Purple40
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(color = Color.White, shape = CircleShape)
                    .padding(10.dp),
                contentAlignment = Alignment.Center

            ){
                Icon(
                    Icons.Filled.Check, contentDescription = "Localized Desciption", tint = Purple40,
                    modifier = Modifier.size(56.dp)
                )
            }
            Row(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "$nameComponent Successfully Created",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }

            Row(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "$nameCompletion is added to the Team home",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(bottom = 16.dp)
        ){
            Button(onClick = {
                val task = taskViewModel.fromViewModelToTask()
                task.addNewHistoryItem(
                    History(
                        TaskHistoryEdit.Created.name, task.taskStatus.name,LocalDate.now().format(
                        DateTimeFormatter.ISO_DATE), username)
                )
                task.addNewHistoryItem(
                    History(
                        TaskHistoryEdit.Status.name, task.taskStatus.name,LocalDate.now().format(
                        DateTimeFormatter.ISO_DATE), username)
                )

                //taskListViewModel.addNewTask(task)
                //localConf.globalTaskList.add(task)
                DatabaseTask().addTask(task)
                navController.navigate(screenToReturn)
                             }, /*DA MODIFICARE*/
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple40), border = BorderStroke(2.dp, Color.White)
            ) {
                Text(text = "Continue", style = MaterialTheme.typography.headlineMedium.copy(
                    Color.White,
                    fontSize = 25.sp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderTaskEffortBar(tvm: TaskViewModel){

    val taskEfforts = TaskEffort.entries.toTypedArray()
    val minValue = 0
    val maxValue = taskEfforts.size - 1
    var sliderValue by remember {
        mutableFloatStateOf(tvm.effortValue.ordinal.toFloat())
    }

    Row {

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
                val index = newValue.roundToInt()
                tvm.setEffortRequired(e = taskEfforts[index])
            },
            valueRange = minValue.toFloat()..maxValue.toFloat(),
            modifier = Modifier.fillMaxWidth(0.8F),
            thumb = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            when (tvm.effortValue) {
                                TaskEffort.None -> effortNone
                                TaskEffort.Low -> effortLow
                                TaskEffort.Medium -> effortMedium
                                TaskEffort.High -> effortHigh
                            }
                        )
                ){
                    Text(
                        text = tvm.effortValue.name,
                        color = when(tvm.effortValue){
                            TaskEffort.None -> effortNoneText
                            TaskEffort.Low -> effortLowText
                            TaskEffort.Medium -> effortMediumText
                            TaskEffort.High -> effortHighText
                        },
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
}

@Composable
fun TitleParagraphTask(string: String){
    Row(
        modifier = Modifier.fillMaxWidth(0.80F)
    ){
        Text(text = string,
            style = MaterialTheme.typography.headlineSmall.copy(
                Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold))
    }
}

@Composable
fun ShowCategoryTask(category: String) {
    Row(
        modifier = Modifier.fillMaxWidth(0.75F),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Category:",
            style = MaterialTheme.typography.headlineMedium.copy(
                Color.LightGray,
                fontSize = 16.sp))

        //Spacer(modifier = Modifier.width(32 .dp))
        Spacer(modifier = Modifier.weight(1f))

        AssistChip(
            onClick = { /*TODO*/ },
            label = { Text(text = category,
                textAlign = TextAlign.Center,
                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Purple40,
                    fontWeight = FontWeight.Bold)
                )
            },
            shape = RoundedCornerShape(20),
            colors = AssistChipDefaults.assistChipColors(containerColor = Color.White),
            border = AssistChipDefaults.assistChipBorder(borderColor = Purple40)
        )
    }
}

@Composable
fun SelectTagTask(taskViewModel: TaskViewModel) {
    val tagsList = listOf(
        TagTask(0, "Project" ),
        TagTask(1, "Finance" ),
        TagTask(2, "Design" ),
        TagTask(3, "Chores" ),
        TagTask(4, "Grocery shopping" ),
        )

    Row(
        modifier = Modifier.fillMaxWidth(0.75F),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tag:",
            style = MaterialTheme.typography.headlineMedium.copy(
                Color.LightGray,
                fontSize = 16.sp
            )
        )

        //Spacer(modifier = Modifier.width(32 .dp))
        Spacer(modifier = Modifier.weight(1f))

        Column {
            DynamicSelectTextField(
                selectedValue = taskViewModel.tagValue,
                options = tagsList,
                onValueChangedEvent = { taskViewModel.setTaskTag(it) }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicSelectTextField(
    selectedValue: String,
    options: List<TagTask>,
    onValueChangedEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        AssistChip(
            onClick = {  },
            label = { Text(text = selectedValue,
                textAlign = TextAlign.Center,
                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Purple40,
                    fontWeight = FontWeight.Bold)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Purple40
                )
            },
            shape = RoundedCornerShape(20),
            colors = AssistChipDefaults.assistChipColors(containerColor = Color.White),
            border = AssistChipDefaults.assistChipBorder(borderColor = Purple40),
            modifier = Modifier
                .menuAnchor()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option: TagTask ->
                DropdownMenuItem(
                    text = { Text(text = option.name) },
                    onClick = {
                        expanded = false
                        onValueChangedEvent(option.name)
                    }
                )
            }
        }
    }
}

@Composable
fun MemberChip(
    user: Profile,
    modifier: Modifier = Modifier,
    username: String,
    //profileImageUrl: String,
    isSelected: Boolean,
    onSelected: (Boolean) -> Unit
) {
    AssistChip(
        onClick = { onSelected(!isSelected) },
        label = {
            Column(modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Row(
                    modifier = Modifier
                        .padding(top = 5.dp, end = 5.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /*Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile Image",
                        tint = if(isSelected) Color.White else Color.Black,
                        modifier = Modifier
                            .size(42.dp)
                    )*/
                    ProfileImageMemberChip(memberProfile = user)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Username
                Box(
                    modifier = Modifier
                        .padding(bottom = 5.dp, end = 5.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 12.sp),
                        color = if(isSelected) Color.White else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        shape = RoundedCornerShape(20),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if(isSelected) Color.LightGray else Color.White
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = if(isSelected) Color.Transparent else Purple40
        ),
        modifier = modifier
    )
}

@Composable
fun SelectMaxMembers(tvm: TaskViewModel, teamMembers: List<Profile>) {
    /* Bisogna modificare il numero massimo di persone */
    val totalTeamMember = teamMembers.size

    Row(
        modifier = Modifier
            .fillMaxWidth(0.6f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Max:",
            style = MaterialTheme.typography.headlineMedium.copy(
                Color.LightGray,
                fontSize = 18.sp))

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedTextCounter(tvm.maxPersonAssigned)
                Column {
                    CounterButton("+") {
                        if(tvm.maxPersonAssigned < totalTeamMember) {
                            tvm.addPerson()
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CounterButton("-") {
                        if(tvm.maxPersonAssigned > 1) {
                            tvm.removePerson()
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = if(tvm.maxPersonAssigned == 1) "person" else "persons",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Color.LightGray,
                    fontSize = 16.sp))


        }


    }

    if (tvm.maxPersonError.isNotBlank()){
        Row (
            modifier = Modifier
                .fillMaxWidth(0.6f),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = tvm.maxPersonError,
                color = Color.Red,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun CounterButton(s: String, content: () -> Unit) {
    val iconSize = 24.dp

    IconButton(
        onClick = { content() },
        modifier = Modifier
            .height(26.dp)
            .width(26.dp)
            .border(
                width = 2.dp,
                color = Purple40,
                shape = CircleShape
            )
            .padding(0.dp)
    ) {
        if(s == "+") {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = Icons.Filled.Add,
                contentDescription = "Add",
                tint = Purple40
            )
        } else {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = Icons.Filled.Remove,
                contentDescription = "Remove",
                tint = Purple40
            )
        }
    }
}

@Composable
fun AnimatedTextCounter(maxMembers: Int) {
    AnimatedContent(
        targetState = maxMembers,
        transitionSpec = {
            if (targetState > initialState) {
                slideInVertically { -it } togetherWith slideOutVertically { it }
            } else {
                slideInVertically { it } togetherWith slideOutVertically { -it }
            }
        }
    ) { maxMembers ->
        Text(
            "$maxMembers",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

@Composable
fun AddNewLinkOrDocument(tvm: TaskViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UploadLink(tvm = tvm)
    }

    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UploadPDF(tvm = tvm)
    }
}


@Composable
fun UploadPDF(tvm: TaskViewModel) {
    val nDocs = tvm.pdfList.size

    val context = LocalContext.current

    // State to track whether the list of links is expanded or not
    var isDocsPopupOpen by remember { mutableStateOf(false) }

    val pickPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pdfUri ->
            handlePdfSelection(context, pdfUri, tvm.pdfList)
        }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                pickPdfLauncher.launch("application/pdf")
            }) {
                Icon(
                    modifier = Modifier.size(34.dp),
                    imageVector = Icons.Filled.FileUpload,
                    contentDescription = "Add",
                    tint = Purple40
                )
            }
            // Show the uploaded documents
            // Text to show the content of the list
            if (tvm.pdfList.isNotEmpty()) {
                Text(
                    text = if(nDocs == 1) "Document(${nDocs})" else "Documents(${nDocs})",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        Purple40,
                        fontSize = 16.sp),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { isDocsPopupOpen = !isDocsPopupOpen }
                )
            }
        }

        DocumentsPopup(
            isDocsPopupOpen,
            tvm.pdfList,
            isPopupOpenChange = { isDocsPopupOpen = !isDocsPopupOpen }
        )
    }
}

private fun handlePdfSelection(
    context: Context,
    pdfUri: Uri,
    documents: MutableList<Pair<String, String>>
) {
    try {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            getFileName(context, pdfUri)
        )

        // Copy the PDF file to the app's external files directory
        copyFile(context, pdfUri, file)

        // Check if the file exists
        if (file.exists()) {
            // Add in documents the new document
            val fileName = getFileName(context, pdfUri)

            Log.d("PDF URI", "File name: $pdfUri")

            // call function in databaseTask
            DatabaseTask().uploadDocument(documents, fileName, pdfUri, context)
        } else {
            // File does not exist, log an error
            Log.e("PDF_SELECTION_ERROR", "Failed to copy PDF file: File does not exist")
        }
    } catch (e: Exception) {
        // Exception occurred during file handling, log the error
        Log.e("PDF_SELECTION_ERROR", "Error handling PDF selection: ${e.message}", e)
    }
}

@SuppressLint("Range")
private fun getFileName(context: Context, uri: Uri): String {
    var result = ""
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.apply {
            moveToFirst()
            result = getString(getColumnIndex(OpenableColumns.DISPLAY_NAME))
            close()
        }
    }
    if (result.isEmpty()) {
        result = uri.path ?: ""
        val cut = result.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

private fun copyFile(context: Context, srcUri: Uri, dstFile: File) {
    try {
        context.contentResolver.openInputStream(srcUri)?.use { inputStream ->
            FileOutputStream(dstFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        Toast.makeText(context, "PDF file copied to: ${dstFile.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        // Handle any exceptions
        Log.e("COPY_FILE_ERROR", "Error copying PDF file: ${e.message}", e)
    }
}

@Composable
fun DocumentsPopup(
    isPopupOpen: Boolean,
    documents: MutableList<Pair<String, String>>,  // fileName, uri
    isPopupOpenChange: () -> Unit,

) {
    val context = LocalContext.current

    if(isPopupOpen) {
        AlertDialog(
            modifier = Modifier
                .width(275.dp) // Set the desired fixed width
                .height(375.dp) // Set the desired fixed height
                .verticalScroll(rememberScrollState()),
            onDismissRequest = isPopupOpenChange,
            title = {
                Text(
                    text = "Show documents",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(1f)
                ) {
                    items(documents) { doc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(4f)) {
                                Text(
                                    text = doc.first,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        Purple40,
                                        fontSize = 16.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable {
                                        // Launch intent to open PDF
                                        val intent = Intent(Intent.ACTION_VIEW)

                                        DatabaseTask().getDocument(intent, doc.second, context, doc.first)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = {
                                documents.remove(doc)
                                DatabaseTask().deleteDocument(doc.second)
                            }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }

                        Divider(
                            color = Color.LightGray,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 8.dp)
                                .fillMaxWidth(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { isPopupOpenChange()},
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Ok",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.White,
                            fontSize = 16.sp)
                    )
                }
            }
        )
    }
}

@Composable
fun UploadLink(tvm: TaskViewModel) {

    // State to track whether the popup is open or not
    var isPopupOpen by remember { mutableStateOf(false) }

    // State to track whether the list of links is expanded or not
    var isLinksPopupOpen by remember { mutableStateOf(false) }

    val nLinks = tvm.linkState.size

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { isPopupOpen = true }) {
            Icon(
                modifier = Modifier.size(34.dp),
                imageVector = Icons.Filled.Link,
                contentDescription = "Add",
                tint = Purple40
            )
        }

        // Add new link
        AddLinkPopup(
            isPopupOpen = isPopupOpen,
            links = tvm.linkState,
            isPopupOpenChange = { isPopupOpen = !isPopupOpen }
        )

        // Text to show the content of the list
        if (tvm.linkState.isNotEmpty()) {
            Text(
                text = if(nLinks == 1) "Link(${nLinks})" else "Links(${nLinks})",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Purple40,
                    fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { isLinksPopupOpen = !isLinksPopupOpen }
            )
        }
        LinksPopup(
            isPopupOpen = isLinksPopupOpen,
            links = tvm.linkState,
            isPopupOpenChange = { isLinksPopupOpen = !isLinksPopupOpen })

    }
}

@Composable
fun AddLinkPopup(isPopupOpen: Boolean, links: MutableList<String>, isPopupOpenChange: () -> Unit) {
    // Text field to input link
    var linkText by remember { mutableStateOf("") }

    fun resetTextFieldValue() {
        linkText = ""
    }

    LaunchedEffect(Unit){
        resetTextFieldValue()
    }

    // Popup to add link
    if(isPopupOpen) {
        AlertDialog(
            onDismissRequest = {
                resetTextFieldValue()
                isPopupOpenChange()
            },
            title = { Text(text = "Add Link") },
            text = {
                TextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    label = { Text("Enter Link") }
                )
            },
            confirmButton = {
                // Confirm button to add link to list
                Button(
                    onClick = {
                        links.add(deleteAllSpacesFromString(linkText).lowercase(Locale.getDefault()))
                        resetTextFieldValue()
                        isPopupOpenChange()
                    },
                    enabled = linkText.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40)
                ) {
                    Text(text = "Add", style = MaterialTheme.typography.headlineMedium.copy(
                        Color.White,
                        fontSize = 16.sp))
                }
            },
            dismissButton = {
                // Dismiss button to close popup
                Button(
                    onClick = {
                        resetTextFieldValue()
                        isPopupOpenChange()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40)
                ) {
                    Text(text = "Cancel", style = MaterialTheme.typography.headlineMedium.copy(
                        Color.White,
                        fontSize = 16.sp))
                }
            }
        )
    }
}

@Composable
fun LinksPopup(
    isPopupOpen: Boolean,
    links: MutableList<String>,
    isPopupOpenChange: () -> Unit
) {
    val context = LocalContext.current

    if(isPopupOpen) {
        AlertDialog(
            modifier = Modifier
                .width(275.dp) // Set the desired fixed width
                .height(375.dp) // Set the desired fixed height
                .verticalScroll(rememberScrollState()),
            onDismissRequest = isPopupOpenChange,
            title = {
                Text(
                text = "Show links",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        Color.Black,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(1f)
                ) {
                    items(links) { link ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(4f)) {
                                Text(
                                    text = link,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        Purple40,
                                        fontSize = 16.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(urlReadyToUse(link))
                                        context.startActivity(intent)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = { links.remove(link) }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }

                        Divider(
                            color = Color.LightGray,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 8.dp)
                                .fillMaxWidth(1f)
                        )
                    }
                }
            },
            confirmButton = {
                // Confirm button to add link to list
                Button(
                    onClick = { isPopupOpenChange()},
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Ok",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.White,
                            fontSize = 16.sp)
                    )
                }
            }
        )
    }
}

@Composable
fun SettingRecurrentPopUp(
    tvm: TaskViewModel,
    isSettingOpen: Boolean,
    settingPopUpOpenChange: () -> Unit
){

    var settingRecurrentTime by remember {
        mutableIntStateOf(0)
    }

    if(isSettingOpen) {
        AlertDialog(
            onDismissRequest = settingPopUpOpenChange,
            title = {
                Text(
                    text = "Recurrence Time",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        Color.Black,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                   SelectRecurrentTime(tvm = tvm)
            },
            confirmButton = {
                Button(
                    onClick = { settingPopUpOpenChange() },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                ) {
                    Text(
                        text = "Ok",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.White,
                            fontSize = 16.sp)
                    )
                }
            },
        )
    }
}

@Composable
fun SelectRecurrentTime(
    tvm: TaskViewModel
){
    Row(
        modifier = Modifier
            .fillMaxWidth(0.85f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = "Max:",
            style = MaterialTheme.typography.headlineMedium.copy(
                Color.LightGray,
                fontSize = 18.sp),
            modifier = Modifier.padding(end = 24.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedTextCounter(tvm.recurrentTimeValue)
                Column {
                    CounterButton("+") {
                        tvm.addRecurrentTime()
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    CounterButton("-") {
                        if(tvm.recurrentTimeValue > 1) {
                            tvm.subRecurrentTime()
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = if(tvm.recurrentTimeValue == 1 || tvm.recurrentTimeValue == 0) "day" else "days",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Color.LightGray,
                    fontSize = 16.sp))


        }


    }
}

fun urlReadyToUse(input: String): String {
    var linkToUse = ""
    if(!input.startsWith("http://") && !input.startsWith("https://")) {
        linkToUse = "http://$input"
    } else {
        linkToUse = input
    }
    return linkToUse.lowercase(Locale.getDefault())
}

fun deleteAllSpacesFromString(input: String): String {
    return input.replace("\\s".toRegex(), "")
}

data class TagTask(val id: Int, val name: String)
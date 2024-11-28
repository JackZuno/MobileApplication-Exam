@file:OptIn(ExperimentalLayoutApi::class)

package it.polito.madlab5.screens.TaskScreen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.database.DatabaseTask
import it.polito.madlab5.database.DatabaseTeam
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.History
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Task.TaskHistoryEdit
import it.polito.madlab5.model.Task.fromTaskToViewModel
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.viewModel.TaskViewModels.TaskListViewModel
import it.polito.madlab5.viewModel.TaskViewModels.TaskViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class EditTaskScreen(){
    Step1,
    Step2,
    Step3,
    TaskFinished,
    Loading

}
@Composable
fun EditTaskInternalController(
    navController: NavHostController,
    task: Task,
    username: String
){
    val nestedController = rememberNavController()
    val teamMembersDB by DatabaseProfile().getTeamMembersFromTeam(task.teamId).collectAsState(initial = emptyList())
    val taskViewModel = remember {
        task.fromTaskToViewModel()
    }

    LaunchedEffect(Unit) {
        DatabaseProfile().getAssignedTeamMembersTask(task.taskID).collect{ assignedMembers ->
            taskViewModel.setTeamMembersTask(assignedMembers)
        }
    }


    NavHost(navController = nestedController,
        startDestination = EditTaskScreen.Step1.name ){

        composable(route = EditTaskScreen.Step1.name){ navBackstackEntry ->
            EditTaskScreenStep1(navController = nestedController, generalNavController = navController, tvm = taskViewModel )
        }

        composable(route = EditTaskScreen.Step2.name){
            EditTaskScreenStep2(navController = nestedController, tvm = taskViewModel, teamMembersDB)
        }

        composable(route= EditTaskScreen.Step3.name){
            EditTaskScreenStep3(navController = nestedController, tvm = taskViewModel, teamMembersDB)
        }
        composable(route = EditTaskScreen.TaskFinished.name){
            EditTaskScreenFinished(nestedNavController = nestedController, taskViewModel = taskViewModel, task = task, username)
        }
        composable(route = EditTaskScreen.Loading.name){
            LoadingScreen(navController = navController)
        }
    }
}

@Composable
fun EditTaskScreenStep1(navController: NavHostController, generalNavController: NavHostController, tvm: TaskViewModel){
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
                    text = "Edit a Task",
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
                    value = tvm.titleValue,
                    onValueChange = tvm::setTaskTitle,
                    label = { Text(text = "Name") },
                    isError = tvm.titleError.isNotBlank(),
                    supportingText = {
                        if (tvm.titleError.isNotBlank())
                            Text(text = tvm.titleError)
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

            ShowCategoryTask(tvm.categoryValue)
            SelectTagTask(taskViewModel = tvm)

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
                    DateSelection(tvm, "startingDate")
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
                    DateSelection(tvm, "dueDate")
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
                    checked = tvm.mandatoryValue,
                    onCheckedChange = tvm::setMandatoryFlag
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
                    checked = tvm.recurrentValue,
                    onCheckedChange = { tvm.setRecurrentFlag( !tvm.recurrentValue) }
                )
                Text(
                    text = "Recurrent",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        Color.LightGray,
                        fontSize = 18.sp
                    )
                )

                if(tvm.recurrentValue) {
                    IconButton(onClick = { isSettingsOpen = true }) {
                        Icon(Icons.Rounded.SettingsSuggest, contentDescription = "Settings Recurrent")
                    }
                }

                SettingRecurrentPopUp(isSettingOpen = isSettingsOpen, settingPopUpOpenChange = { isSettingsOpen = !isSettingsOpen}, tvm = tvm )
            }
        }

        Button(
            onClick = {
                tvm.validate()
                if (tvm.isValid) {
                    navController.navigate(EditTaskScreen.Step2.name)
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
fun EditTaskScreenStep2(navController: NavHostController, tvm: TaskViewModel, teamMembers: List<Profile>){

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
                    navController.navigate(EditTaskScreen.Step3.name)
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
                navController.navigate(EditTaskScreen.Step1.name)
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
fun EditTaskScreenStep3(navController: NavHostController, tvm: TaskViewModel, teamMembers: List<Profile>){
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
                navController.navigate(EditTaskScreen.TaskFinished.name)
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 0.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(
                text = "Save", style = MaterialTheme.typography.headlineMedium.copy(
                    Color.White,
                    fontSize = 25.sp
                )
            )
        }

        Button(
            onClick = {
                navController.navigate(EditTaskScreen.Step2.name)
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
fun EditTaskScreenFinished(
    nestedNavController: NavHostController,
    taskViewModel: TaskViewModel,
    task: Task,
    username: String
){
    EditFunctionCompleted(
        nameComponent = "Task",
        nameCompletion = taskViewModel.titleValue,
        navController = nestedNavController,
        taskViewModel = taskViewModel,
        task = task,
        username = username
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditFunctionCompleted(nameComponent: String, nameCompletion: String, navController: NavHostController, taskViewModel: TaskViewModel, task: Task, username: String){

    val screenSize = LocalConfiguration.current.screenWidthDp
    val fontSize = if (screenSize < 600 ){
        16.sp
    } else {
        24.sp
    }

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
                    text = "$nameComponent Successfully Edited",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }

            FlowRow(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "$nameCompletion is successfully updated",
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
                task.addNewHistoryItem(
                    History(
                        TaskHistoryEdit.Edited.name, taskViewModel.status.name,LocalDate.now().format(
                        DateTimeFormatter.ISO_DATE), username)
                )
                DatabaseTask().updateTaskHistory(task.taskHistoryId, task.taskHistory)

                task.copyFromViewModel(taskViewModel)
                DatabaseTask().updateTask(task)

                navController.navigate(EditTaskScreen.Loading.name)
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

@Composable
fun LoadingScreen(navController: NavHostController) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }

    LaunchedEffect(Unit) {
        delay(500) // Wait for 1 second
        navController.navigate(TaskDetailsScreen.TaskDetails.name)
    }
}
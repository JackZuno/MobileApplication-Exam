package it.polito.madlab5.screens.TaskScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Details
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import it.polito.madlab5.GeneralAppScreen
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Task.TaskStates
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.screens.TeamScreen.TeamDetailsScreen
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.TaskBg
import it.polito.madlab5.ui.theme.completeBg
import it.polito.madlab5.ui.theme.completeText
import it.polito.madlab5.ui.theme.inProgressBg
import it.polito.madlab5.ui.theme.inProgressText
import it.polito.madlab5.ui.theme.overDueBg
import it.polito.madlab5.ui.theme.overDueText
import it.polito.madlab5.ui.theme.pendingBg
import it.polito.madlab5.ui.theme.pendingText
import it.polito.madlab5.viewModel.TaskViewModels.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    navController: NavController,
    index: Int
){

    Column (
        Modifier
            .height(150.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight()
                    .padding(vertical = 10.dp)
                    .shadow(5.dp, shape = RoundedCornerShape(10), spotColor = Color.Black)
                    .clip(RoundedCornerShape(10)),
                onClick = { navController.navigate(GeneralAppScreen.TaskDetails.name+"/${index}") },
                colors = CardDefaults.cardColors(containerColor = TaskBg),
                border = BorderStroke(2.dp, Color.White),
            ) {
                Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Text(
                            text = task.title,
                            modifier = Modifier,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 28.sp),
                            color = Color.Black
                        )
                        if(task.mandatory) {
                            Icon(
                                imageVector = Icons.Outlined.Details,
                                contentDescription = null,
                                tint = overDueBg,
                                modifier = Modifier.size(30.dp,30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Filled.MoreHoriz, contentDescription = null, tint = Color.LightGray)
                    }

                    Row(modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically) {
                        if(task.recurrent){
                            Icon(imageVector = Icons.Outlined.Repeat,
                                contentDescription = null, tint = Color.LightGray, modifier = Modifier.padding(end = 5.dp))
                            Text(text = "Every ${task.recurrentTime} days ", color = Color.LightGray, fontWeight = FontWeight.Bold)

                        }else{
                            Icon(imageVector = Icons.Outlined.WatchLater,
                                contentDescription = null, tint = Color.LightGray, modifier = Modifier.padding(end = 5.dp))
                            Text(text = task.startingDate, color = Color.LightGray, fontWeight = FontWeight.Bold)
                        }

                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        StatusChip(task.taskStatus)
                    }

                }
        }
    }
}

@Composable
fun StatusChip(status: TaskStates){
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

    }
    AssistChip(onClick = { /*TODO*/ },
        label = { Text(text = statusLabel, color = textColor, textAlign = TextAlign.Center , modifier = Modifier
            .width(80.dp)
            .fillMaxWidth()) },
        colors = AssistChipDefaults.assistChipColors(containerColor = bgColor),
        border = AssistChipDefaults.assistChipBorder(borderColor = Color.Transparent))

}

@Composable
fun TaskWrapper(
    team: Team,
    navController: NavController,
    tlvm: TaskListViewModel,
){

    val isCreationDate = remember { mutableStateOf(tlvm.isCreationDate.value) }
    Column ( horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)){
        Row (verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,modifier = Modifier.fillMaxWidth(0.9f)){
            Text(
                text = "Tasks",
                modifier = Modifier,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                color = Color.Black
            )
            IconButton(onClick = { navController.navigate(TeamDetailsScreen.TaskFilter.name) }) {
                Icon(imageVector = Icons.Filled.FilterAlt, contentDescription = null)
            }
            IconButton(onClick = {
                isCreationDate.value = !isCreationDate.value
                tlvm.isCreationDate.value = !tlvm.isCreationDate.value
                sorting(isCreationDate.value, tlvm)
            }) {
                Row {
                    Icon(imageVector = Icons.Filled.Sort, contentDescription = null)
                    Text(text = if(isCreationDate.value){"C"} else "E")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = {
                navController.navigate(TeamDetailsScreen.NewTask.name)

            }, colors = ButtonDefaults.buttonColors(containerColor = Purple40)){
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Text(text = "New Task")
            }
        }

        TaskList(tlvm.filteredTaskList, navController)
    }
}

@Composable
fun TaskList(taskList: List<Task>, navController: NavController){

    Row() {
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            itemsIndexed(taskList) { i, task  ->

                TaskItem(task = task, navController, i)
            }
        }
    }
}

fun sorting(isCreationDate: Boolean, tlvm: TaskListViewModel){
    if (isCreationDate){
        tlvm.filteredTaskList.sortBy { t -> t.startingDate }
    }else{
        tlvm.filteredTaskList.sortBy { t -> t.dueDate }
    }
}
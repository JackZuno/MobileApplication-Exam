package it.polito.madlab5.screens.TaskScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Tour
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Task.TaskStates
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.PurpleGrey80
import it.polito.madlab5.model.Task.TaskHistoryEdit

var sizeCur = IntSize(width = 0, height = 0)
var posCurr: Offset = Offset(x = 0f, y = 0f)


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable

fun TaskHistory(nestedNavController: NavHostController, task: Task) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Task History")
                },
                navigationIcon = {
                    IconButton(onClick = { nestedNavController.popBackStack() }) {
                        Icon(Icons.Filled.Close, "backIcon")
                    }
                },

                //backgroundColor = MaterialTheme.colors.primary,
                //contentColor = Color.White,
                //elevation = 10.dp
            )
        }, content = {
            Box(modifier = Modifier.padding(top =it.calculateTopPadding())){
                TaskHistoryView(task)
            }


        })
}
@Composable
fun TaskHistoryView(task: Task) {
    val localDensity = LocalDensity.current
    var columnHeightCompletedDp by remember {
        mutableStateOf(0.dp)
    }
    var columnHeightToDoDp by remember {
        mutableStateOf(0.dp)
    }
    Row (modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .padding(top = 20.dp)
        .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {

        Column(modifier = Modifier
            .weight(0.2f)
            .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Outlined.Tour,contentDescription = null, modifier = Modifier
                .size(40.dp)
                .padding(bottom = 10.dp))
            VerticalProgressBar(progress = 1f ,size = Size(20f,columnHeightCompletedDp.value), backgroundColor = PurpleGrey80, strokeColor = Color.Transparent, color = Purple40)

            //VerticalProgressBar(progress = 0.0f,size = Size(20f,100f), backgroundColor = PurpleGrey80, strokeColor = Color.Transparent, color = Purple40)

        }

        Column(modifier = Modifier
            .weight(0.8f)
            .padding(top = 40.dp)
            .fillMaxHeight()
            .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(80.dp)) {
                Row() {
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                columnHeightCompletedDp =
                                    with(localDensity) { it.size.height.toDp() }
                            },
                        verticalArrangement = Arrangement.spacedBy(80.dp)
                    ) {

                        task.taskHistory.forEach(
                        ) {
                            when (it.taskHistory) {
                                TaskHistoryEdit.Created.name -> {
                                    TextInfo("Task Created", it.date, it.user)
                                }

                                TaskHistoryEdit.Edited.name -> {
                                    TextInfo("Task Edited", it.date, it.user)
                                }

                                TaskHistoryEdit.Status.name -> {
                                    val status: TaskStates = when(it.status){
                                        TaskStates.Completed.name -> TaskStates.Completed
                                        TaskStates.OverDue.name -> TaskStates.OverDue
                                        TaskStates.InProgress.name -> TaskStates.InProgress
                                        TaskStates.Pending.name -> TaskStates.Pending
                                        else -> {TaskStates.Completed}
                                    }
                                    StatusInfo(
                                        status = status,
                                        date = it.date,
                                        by = it.user,
                                    )
                                }
                            }
                        }
                    }
                }
            }
    }
}

@Composable
fun StatusInfo(status: TaskStates, date: String, by: String){

    Column() {
        Row(
            modifier = Modifier

        ) {
            StatusChip(status = status)
            Text(modifier = Modifier.padding(start = 15.dp),
                text = " \nby $by",
                textAlign = TextAlign.Start,
                color = Color.LightGray,

                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp
                )
            )
        }
        Row {
            Text(
                text = "    $date",
                textAlign = TextAlign.Center,
                color = Color.LightGray,
                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun TextInfo(info: String, date: String, by: String){
    Column {
        Row() {
            Text(modifier = Modifier.padding(end = 15.dp),
                text = info,
                textAlign = TextAlign.Right,
                color = Color.Black,
                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Right,
                    fontWeight = FontWeight.Normal,
                    fontSize = 25.sp
                )
            )

            Text(
                text = " \nby $by",
                textAlign = TextAlign.Start,
                color = Color.LightGray,

                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp
                )
            )
        }
        Row {
            Text(
                text = "    $date",
                textAlign = TextAlign.Center,
                color = Color.LightGray,
                style =
                MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            )
        }
    }


}

@Composable
fun VerticalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    backgroundColor: Color = Color.Black,
    size: Size = Size(width = 10f, height = 100f),
    strokeSize: Float = 1f,
    strokeColor: Color = Color.Blue
) {
    Canvas(
        modifier = modifier
            .size(size.width.dp, size.height.dp)
            .border(width = strokeSize.dp, color = strokeColor)
    ) {
        // Progress made
        drawRoundRect(
            color = color,
            size = Size(size.width.dp.toPx(), height = (progress * (size.height-50)).dp.toPx()), //Size(size.width.dp.toPx(), posCurr.y),//Size(size.width.dp.toPx(), height = (progress * size.height).dp.toPx()),
            cornerRadius = CornerRadius(100f,100f),
            //topLeft = Offset(0.dp.toPx(), ((1-progress) * size.height).dp.toPx())
        )
        // background
        drawRoundRect(
            color = backgroundColor,
            size = Size(width = size.width.dp.toPx(), height = ((1 - progress) * (size.height-50)).dp.toPx()),//Size(size.width.dp.toPx(), size.height - posCurr.y - sizeCur.height),//Size(width = size.width.dp.toPx(), height = ((1 - progress) * size.height).dp.toPx()),
            topLeft = Offset(0.dp.toPx(), ((progress) * size.height-50).dp.toPx()),//Offset(0f, posCurr.y + sizeCur.height),//Offset(0.dp.toPx(), ((progress) * size.height).dp.toPx()),
            cornerRadius = CornerRadius(100f,100f),
        )
    }
}
package it.polito.madlab5.screens.statistics

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import it.polito.madlab5.screens.TeamScreen.Details
import it.polito.madlab5.screens.TeamScreen.DialogLeave
import it.polito.madlab5.screens.TeamScreen.TeamDetailsScreen
import it.polito.madlab5.screens.TeamScreen.TeamListScreen
import it.polito.madlab5.ui.theme.ChartBlue
import it.polito.madlab5.ui.theme.ChartGreen
import it.polito.madlab5.ui.theme.ChartOrange
import it.polito.madlab5.ui.theme.ChartRed
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.backgroundBar


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ShowPersonalTeamStats(navHostController: NavHostController) {
    val teamEntries = listOf(
        TeamStatsEntry("Team 1", 5f,80, 100 ),
        TeamStatsEntry("Team 2", 3f,50, 100 ),
        TeamStatsEntry("Team 3", 4f,40, 100 ),
        TeamStatsEntry("Team 4", 4f,20, 100 ),
        TeamStatsEntry("Team 5", 2f,10, 100 ),

    )
    val nTeams = teamEntries.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Team Stats ")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navHostController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                }
            )
        },content = {
            Box(modifier = Modifier
                .padding(start = 50.dp, top = 60.dp, end = 0.dp, bottom = 0.dp)
                .fillMaxWidth(0.85f),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Teams (${nTeams}):",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.LightGray,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Left))

                    LazyColumn(
                        modifier = Modifier
                            .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
                            .fillMaxWidth()) {
                        items(nTeams) { i ->
                            TeamEntry(team = teamEntries[i])
                        }
                    }
                }
            }
        })
}


@Composable
fun TeamEntry(team: TeamStatsEntry) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 0.dp, top = 5.dp, end = 0.dp, bottom = 0.dp), verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = team.teamName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.Black,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Left
                        )
                    )
                    Icon(
                        imageVector = if(!expanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Dropdown",
                        modifier = Modifier.size(24.dp)
                    )

            }

            if(expanded) {
                // Average rating that team members gave to the user
                Column(modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally){

                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Left))
                    Row ( ){
                        LinearProgressIndicator(progress = (team.taskCompleted.toFloat()/team.totalTask.toFloat()), color = Purple40, strokeCap = StrokeCap.Butt)
                        Text(
                            text = "${team.taskCompleted}/${team.totalTask}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Right),
                            modifier = Modifier.padding(start = 10.dp))
                    }
                    Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85F))
                    Text(
                        text = "Your Rating",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Left))
                    RatingBar(rating = team.rating)

                    Text(
                        text = "${team.rating}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = backgroundBar,
                            fontSize = 16.sp,
                            textAlign = TextAlign.End
                        )
                    )
                    Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85F))

                    Text(
                        text = "Last 7 days completed task",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Left))
                    LineChartFullCurvedPrev()
                    Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85F))
                    Text(
                        text = "Team Member task completed",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.Black,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Left))
                    val barEntries = listOf(
                        BarEntry(25, "Anna", ChartRed),
                        BarEntry(40, "Rosa", ChartBlue),
                        BarEntry(15, "Steve", ChartGreen),
                        BarEntry(30, "Bob", ChartOrange),
                        BarEntry(40, "Rosa", ChartBlue),
                        BarEntry(15, "Steve", ChartGreen),
                        BarEntry(30, "Bob", ChartOrange)
                    )
                    BarChart(entries = barEntries)

                    /*
                    Text(
                        "Average rating",
                        modifier = Modifier
                            .weight(1f)
                            .padding(0.dp, 0.dp),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            Color.LightGray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Start
                        )
                    )
                    RatingBar(rating = team.rating)

                    Text(
                        text = "${team.rating}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = backgroundBar,
                            fontSize = 16.sp,
                            textAlign = TextAlign.End
                        )
                    )
                }

                // Number of tasks completed by the user in the team
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Task completed",
                        modifier = Modifier
                            .weight(1f)
                            .padding(0.dp, 0.dp),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            Color.LightGray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Start
                        )
                    )
                    Text(
                        text = "${team.nTaskCompleted}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = backgroundBar,
                            fontSize = 16.sp,
                            textAlign = TextAlign.End
                        )
                    )*/


                }

            }
        }
    }
    Divider(
        color = Color.LightGray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 0.dp, top = 5.dp, end = 0.dp, bottom = 10.dp)
    )
}

@Composable
fun RatingBar(rating: Float, maxRating: Int = 5, starColor: Color = backgroundBar, emptyStarColor: Color = Color.LightGray) {
    Row {
        repeat(maxRating) { index ->
            val tint = when {
                index <= rating - 1 -> starColor
                index < rating -> {
                    val fraction = rating - index
                    val halfStarColor = lerp(emptyStarColor, starColor, fraction)
                    halfStarColor
                }
                else -> emptyStarColor
            }
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

data class PieEntry(val value: Int, val label: String, val color: Color)
data class BarEntry(val value: Int, val label: String, val color: Color)
data class TeamStatsEntry(val teamName: String, val rating:Float,val taskCompleted: Int, val totalTask:Int)
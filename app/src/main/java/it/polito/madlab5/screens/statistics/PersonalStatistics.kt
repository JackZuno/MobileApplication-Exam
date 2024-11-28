package it.polito.madlab5.screens.statistics

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import it.polito.madlab5.screens.TeamScreen.Details
import it.polito.madlab5.screens.TeamScreen.DialogLeave
import it.polito.madlab5.screens.TeamScreen.TeamDetailsScreen
import it.polito.madlab5.screens.TeamScreen.TeamListScreen
import it.polito.madlab5.ui.theme.ChartBlue
import it.polito.madlab5.ui.theme.ChartGreen
import it.polito.madlab5.ui.theme.ChartLightBlue
import it.polito.madlab5.ui.theme.ChartOrange
import it.polito.madlab5.ui.theme.ChartPurple
import it.polito.madlab5.ui.theme.ChartRed
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.backgroundBar
import java.time.LocalDate


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowStats(navController: NavHostController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "MyStats")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                },
                //backgroundColor = MaterialTheme.colors.primary,
                //contentColor = Color.White,
                //elevation = 10.dp
            )
        },content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 60.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pieEntries = listOf(
                    PieEntry(25, "Category A", ChartRed),
                    PieEntry(20, "Category B", ChartBlue),
                    PieEntry(15, "Category C", ChartGreen),
                    PieEntry(10, "Category D", ChartOrange),
                    PieEntry(30, "Category E", ChartPurple),
                    PieEntry(3, "Category F", ChartLightBlue)
                )

                val barEntries = listOf(
                    BarEntry(25, "None", ChartRed),
                    BarEntry(40, "Low", ChartBlue),
                    BarEntry(15, "Medium", ChartGreen),
                    BarEntry(30, "High", ChartOrange)
                )
                Box(modifier = Modifier
                    .padding(start = 0.dp, top = 10.dp, end = 0.dp, bottom = 0.dp)
                    .fillMaxWidth(0.85f),
                    contentAlignment = Alignment.CenterStart)
                {
                    Text(
                        text = "Your Rating",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.Black,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Left
                        )
                    )
                }


                RatingBar(rating = 4f)

                Text(
                    text = "${4}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = backgroundBar,
                        fontSize = 16.sp,
                        textAlign = TextAlign.End
                    )
                )

                Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85F))

                Box(modifier = Modifier
                    .padding(start = 0.dp, top = 10.dp, end = 0.dp, bottom = 0.dp)
                    .fillMaxWidth(0.85f),
                    contentAlignment = Alignment.CenterStart)
                {
                    Column( verticalArrangement = Arrangement.spacedBy(10.dp),){
                        Text(
                            text = "Last 7 days completed task",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                Color.Black,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Left))
                        LineChartFullCurvedPrev()
                    }


                }
                Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85F))



                Box(modifier = Modifier
                    .padding(start = 0.dp, top = 10.dp, end = 0.dp, bottom = 0.dp)
                    .fillMaxWidth(0.85f),
                    contentAlignment = Alignment.CenterStart)
                {
                    Text(
                        text = "Task Effort Type",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.Black,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Left))
                }



                BarChart(entries = barEntries)

                Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85F))

                Box(modifier = Modifier.fillMaxWidth(0.85f),
                    contentAlignment = Alignment.CenterStart)
                {
                    Text(
                        text = "Task Category",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            Color.Black,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Left))
                }

                PieChart(
                    modifier = Modifier.size(250.dp),
                    entries = pieEntries
                )
                Legend(entries = pieEntries)
            }
        })
}


@Composable
fun BarChart(entries: List<BarEntry>) {
    val maxBarValue = entries.maxOf { it.value }
    val maxBarHeight = 150.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 0.dp, top = 16.dp, end = 0.dp, bottom = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        entries.forEach { entry ->
            val barHeight = (entry.value.toFloat() / maxBarValue.toFloat()) * maxBarHeight.value

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(barHeight.dp)
                        .clip(RoundedCornerShape(20))
                        .background(color = Purple40),
                    //.padding(vertical = 4.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "${entry.value}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Normal),
                        color = Color.White,
                        modifier = Modifier.padding(start = 3.dp, top = 3.dp, end = 3.dp, bottom = 0.dp)
                    )
                }
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 12.sp),
                    color = Color.Black,
                )
            }
        }
    }
}

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    entries: List<PieEntry>
) {
    Canvas(
        modifier = modifier
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val radius = (canvasWidth / 2) * 0.8f // Adjust as needed

        var startAngle = 0f
        val totalValue = entries.sumOf { it.value.toDouble() }.toFloat()

        entries.forEach { entry ->
            val sweepAngle = (360f * (entry.value / totalValue))
            drawArc(
                color = entry.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun Legend(entries: List<PieEntry>) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEach { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(20))
                        .background(color = entry.color)
                )
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                    text = entry.label,
                    style = MaterialTheme.typography.headlineSmall.copy(Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Start)
                )
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                    text = "${entry.value}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp, textAlign = TextAlign.End)
                )
            }
        }
    }
}
@Composable
fun LineChartFullCurved(
    dataPoints: MutableList<LineEntry>,
    color: Color = Purple40,
    height: Dp = 200.dp
) {
    //y-Axis
    Row (modifier = Modifier.fillMaxWidth()){
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(200.dp/(dataPoints.maxByOrNull{ it.value }?.value ?: 1f)),
            ) {
        dataPoints.sortedBy { it.value }.reversed().forEachIndexed { index, lineEntry ->
            Text(
                text = lineEntry.value.toInt().toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            )
        }
    }
        Column {
            //Create canvas
            Canvas(modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(height)
                .padding(top = 10.dp, start = 2.dp) ){
                //create line where we will save all the points
                val linePath = androidx.compose.ui.graphics.Path()
                val m = (dataPoints.maxByOrNull{ it.value }?.value ?: 1f)
                val xStep  = size.width / (dataPoints.size -1)
                val yStep = size.height / m

                //iterate all the points
                dataPoints.forEachIndexed { index, dataPoint ->
                    //create the points with cubicTo
                    val xPos = index * xStep
                    val yPos = size.height - (dataPoint.value * yStep)

                    if(index == 0){
                        linePath.moveTo(xPos, yPos)
                    } else {
                        //get the previous point
                        val prevDataPoint = dataPoints[index - 1]
                        val prevXPos = (index - 1) * xStep
                        val prevYPos = size.height - (prevDataPoint.value * yStep)

                        //create the control points for the curve
                        val controlX1 = prevXPos + (xPos - prevXPos) / 2
                        val controlX2 = prevXPos + (xPos - prevXPos) / 2

                        //create the curve with cubicTo - references from control points
                        linePath.cubicTo(controlX1, prevYPos, controlX2, yPos, xPos, yPos)

                        //If u want to create a circle in the point
                        drawCircle(
                            color = color,
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(xPos, yPos)
                        )

                    }
                }

                //draw the line

                drawPath(
                    path = linePath,
                    color = color,
                    //alpha = 0.4f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4.dp.toPx()
                    )
                )
            }

        }

    }
    //x-Axis
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
        ) {
        dataPoints.forEachIndexed { index, lineEntry ->
            Text(text = lineEntry.label ,
                style = MaterialTheme.typography.bodySmall.copy(
                textAlign = TextAlign.Center,
                fontSize = 13.sp
                )
            )
        }
    }

}
data class LineEntry(val value: Float, val label: String)
@Preview
@Composable
fun LineChartFullCurvedPrev() {
    val currentDate = LocalDate.now()
    var oldDate: LocalDate?
    var day: Int
    var month: Int
    var s: String
    val lineEntries = mutableListOf<LineEntry>()
    for(k in 7 downTo 0){
        oldDate =currentDate.minusDays(k.toLong())
        day =  oldDate.dayOfMonth
        month = oldDate.monthValue
        s = "$day/$month"
        lineEntries.add(LineEntry((0..20).random().toFloat(), s))
    }

    LineChartFullCurved(
        dataPoints = lineEntries
    )
}

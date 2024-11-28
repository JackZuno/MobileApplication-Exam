package it.polito.madlab5.screens.TeamScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import it.polito.madlab5.database.DatabaseTeam
import it.polito.madlab5.model.Team.Achievement
import it.polito.madlab5.model.Team.AchievementState
import it.polito.madlab5.model.Team.iconsVec
import it.polito.madlab5.ui.theme.grayBgColor
import it.polito.madlab5.ui.theme.grayBgColor2
import it.polito.madlab5.ui.theme.greenBgColor
import it.polito.madlab5.ui.theme.greenBgColor2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable

fun AchievementDetailsScreen(
    nestedNavController: NavHostController,
    ach: Achievement,
    index: Int
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                ,title = {
                },
                navigationIcon = {
                    IconButton(onClick = {nestedNavController.popBackStack()}) {
                        Icon(Icons.Filled.Close, "closeIcon", tint = Color.White, modifier = Modifier.size(30.dp,30.dp))
                    }
                },
                actions = {
                    /*IconButton(onClick = {
                        /*nestedNavHostController.navigate(TaskDetailsScreen.TaskEdit.name)*/
                    }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                    }*/
                    /*IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    }*/

                }
                //backgroundColor = MaterialTheme.colors.primary,
                //contentColor = Color.White,
                //elevation = 10.dp
            )
        },content = {

            AchievementDetails(ach, nestedNavController, index)
        })

}

@Composable
fun AchievementDetails(ach: Achievement, nestedNavController: NavHostController, index: Int) {
    var firstColor: Color = Color.White
    var secondColor: Color =Color.White
    when(ach.achievementState){
        AchievementState.Achieved.name -> {firstColor = greenBgColor; secondColor = greenBgColor2 }
        AchievementState.NotAchieved.name -> {firstColor = grayBgColor; secondColor = grayBgColor2 }
        AchievementState.ToClaim.name -> {firstColor = greenBgColor; secondColor = greenBgColor2 }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .gradientBackground(listOf(firstColor, secondColor), angle = -90f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        AchievementsChipBig(achievement = ach, index)
        if (ach.achievementState == AchievementState.ToClaim.name){
        Row(modifier = Modifier.padding(top =50.dp).fillMaxWidth(0.6f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
            Button(modifier = Modifier.padding(top =30.dp).fillMaxWidth(),
                shape = RoundedCornerShape(20), onClick = {
                    ach.achievementState = AchievementState.Achieved.name;
                    nestedNavController.popBackStack()

                                                          }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                Text(
                    text = "Claim",
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            }
        }
        }
    }
}

@Composable
fun AchievementsChipBig(achievement: Achievement, index: Int){
        Column(  verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally){
            Row {
                Button(onClick = { /*TODO*/ }, modifier = Modifier.size(200.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent ),
                    //border = BorderStroke(3.dp, if (achievement.achieved) Color.Green else Color.Transparent),
                ) {
                    Icon(imageVector = iconsVec[index], tint =  Color.DarkGray ,contentDescription = null, modifier = Modifier.size(60.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = achievement.name,
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Medium),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = achievement.description,
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Light),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

    }
}

fun Modifier.gradientBackground(colors: List<Color>, angle: Float) = this.then(
    Modifier.drawBehind {
        val angleRad = angle / 180f * PI
        val x = cos(angleRad).toFloat() //Fractional x
        val y = sin(angleRad).toFloat() //Fractional y

        val radius = sqrt(size.width.pow(2) + size.height.pow(2)) / 2f
        val offset = center + Offset(x * radius, y * radius)

        val exactOffset = Offset(
            x = min(offset.x.coerceAtLeast(0f), size.width),
            y = size.height - min(offset.y.coerceAtLeast(0f), size.height)
        )

        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(size.width, size.height) - exactOffset,
                end = exactOffset
            ),
            size = size
        )
    }
)
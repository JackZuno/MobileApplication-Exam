package it.polito.madlab5.screens.TeamScreen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.polito.madlab5.database.DatabaseTeam
import it.polito.madlab5.model.Team.Achievement
import it.polito.madlab5.model.Team.AchievementState
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.model.Team.iconsVec
import it.polito.madlab5.ui.theme.greenBgColor
enum class AchievementsScreens{
    Achievements,
    Details
}
@Composable
fun AchievementMain(navController: NavHostController, team: Team) {
    val nestedNavController = rememberNavController()
    val speedAnimation = 700


    NavHost(
        navController = nestedNavController,
        startDestination = AchievementsScreens.Achievements.name
    ) {
        composable(route = AchievementsScreens.Achievements.name) {
            DatabaseTeam().updateTeamAchievements(team.achievementId, team.achievement)

            AchievementScreen(nestedNavController, team.achievement,navController)
        }
        composable(
            route = AchievementsScreens.Details.name + "/{taskIndex}",
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
                backStackEntry ->
            val achIndex = backStackEntry.arguments?.getString("taskIndex")
            val ach = achIndex?.let { team.achievement[it.toInt()] }
            if (ach != null){

                AchievementDetailsScreen(nestedNavController, ach, achIndex.toInt())
            }
        }

    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    nestedNavController: NavHostController,
    achievements: List<Achievement>,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements",
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                        color = Color.Black
                    )

                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
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

            AchievementsList(achievements, it.calculateTopPadding(), nestedNavController)
        })

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementsList(
    achievements: List<Achievement>,
    topPadding: Dp,
    nestedNavController: NavHostController
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
        .padding(top = topPadding, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row( horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

            FlowRow(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.9f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalArrangement = Arrangement.SpaceEvenly,
                maxItemsInEachRow = 3
            ) {
                achievements.forEachIndexed { index, achievement ->

                        AchievementsChip(achievement, index, nestedNavController)
                    }
                }
            }
        }

    }


@Composable
fun AchievementsChip(achievement: Achievement, index: Int, nestedNavController: NavHostController){
    var borderColor: Color = Color.White
    var iconColor: Color = Color.White
    var containerColor: Color =Color.White
    when(achievement.achievementState){
        AchievementState.Achieved.name -> {containerColor = greenBgColor; borderColor = greenBgColor; iconColor = Color.White}
        AchievementState.NotAchieved.name -> {containerColor = Color.DarkGray; borderColor = Color.Transparent; iconColor = Color.LightGray}
        AchievementState.ToClaim.name-> {containerColor = Color.Transparent; borderColor = greenBgColor; iconColor = Color.DarkGray}
    }
    Box(modifier = Modifier.size(110.dp, 160.dp)){
        Column(  verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally){
            Row {
                Button(onClick = { nestedNavController.navigate(AchievementsScreens.Details.name+"/${index}") }, modifier = Modifier.size(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                    border = BorderStroke(3.dp, color = borderColor),
                ) {
                    Icon(imageVector = iconsVec[index], tint = iconColor,contentDescription = null, modifier = Modifier.size(60.dp))
                }
            }
        Row( modifier = Modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
            Text(
                text = achievement.name,
                modifier = Modifier,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp,),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        }
    }
}


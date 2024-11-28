package it.polito.madlab5.screens.TeamScreen

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.polito.madlab5.alerts.LoadOrTakeTeamImageAlert
import it.polito.madlab5.alerts.NoPermissionAlert
import it.polito.madlab5.database.DatabaseTeam
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.Purple80
import it.polito.madlab5.viewModel.TeamViewModels.TeamViewModel
import it.polito.madlab5.viewModel.TeamViewModels.fromViewModelToTeam


enum class NewTeamScreen {
    CreateTeam,
    TeamCreated
}

@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun TeamController(
    generalNavHostController: NavHostController,
    localConf: LocalConfiguration,
    loggedInUser: String
){    // teamListVm: TeamListViewModel
    val navController = rememberNavController()
    val teamViewModel = TeamViewModel(teamAdmin = loggedInUser)

    NavHost(
        navController = navController,
        startDestination = NewTeamScreen.CreateTeam.name
    ){
        composable(route = NewTeamScreen.CreateTeam.name) {
            CreateTeam(navController = navController, teamViewModel= teamViewModel, generalNavHostController = generalNavHostController)
        }
        composable(route = NewTeamScreen.TeamCreated.name) {
            TeamCreated(generalNavHostController, teamViewModel, localConf)
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CreateTeam(navController: NavHostController, generalNavHostController: NavHostController, teamViewModel: TeamViewModel){   //, generalNavController: NavHostController, taskViewModel: TaskViewModel
    val showAlert = remember { mutableStateOf(false) }
    val showNoPermissionAlert = remember { mutableStateOf(false) }

    if(showAlert.value){
        LoadOrTakeTeamImageAlert(
            teamViewModel,
            showAlert,
            showNoPermissionAlert
        ) {
            showAlert.value = false
            showNoPermissionAlert.value = false
        }
    }

    if(showNoPermissionAlert.value){
        NoPermissionAlert(onDismiss = {
            showNoPermissionAlert.value = false
            showAlert.value = false
        })
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
    ) {
        // Step 1-1
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85F)
                .padding(bottom = 10.dp)
        ) {
            Text(
                text = "Step 1 of 1",
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
                progress = 1f,
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
                    text = "Create a new Team",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        Color.Black,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Team image
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Purple80)

            ) {
                if(teamViewModel.imageValue != null) {  //imageBitmap.value != null
                    Image(
                        bitmap = teamViewModel.imageValue!!.asImageBitmap(),
                        contentDescription =null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Groups,
                        contentDescription = "Group Icon",
                        modifier = Modifier.size(72.dp)
                    )
                }

                Button(colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .alpha(0.5f),
                    onClick = {
                        showAlert.value = true
                    }) {
                }
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Color.White
                )
            }


            // Team name
            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Enter your Team name",
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
                    value = teamViewModel.nameValue,
                    onValueChange = teamViewModel::setName,
                    label = { Text(text = "Name") },
                    isError = teamViewModel.nameError.isNotBlank(),
                    supportingText = {
                        if (teamViewModel.nameError.isNotBlank())
                            Text(text = teamViewModel.nameError)
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

            // Team Category
            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Enter Team category",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            SelectCategoryTeam(teamViewModel)

            Divider(color = Color.LightGray, modifier = Modifier.fillMaxWidth(0.85f))

            // Team Description
            Row(
                modifier = Modifier.fillMaxWidth(0.80F)
            ) {
                Text(
                    text = "Enter Team description",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8F)
                    .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 10.dp)
            ) {
                OutlinedTextField(
                    value = teamViewModel.descriptionValue,
                    onValueChange = teamViewModel::setTeamDescription,
                    label = { Text(text = "Description") },
                    /*isError = taskViewModel.titleError.isNotBlank(),
                    supportingText = {
                        if (taskViewModel.titleError.isNotBlank())
                            Text(text = taskViewModel.titleError)
                    },*/
                    minLines = 6,
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
        }

        Button(
            onClick = {
                teamViewModel.validate()
                if (teamViewModel.isValid) {
                    navController.navigate(NewTeamScreen.TeamCreated.name)
                }
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
                generalNavHostController.popBackStack()
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
fun SelectCategoryTeam(teamViewModel: TeamViewModel) {   //taskViewModel: TaskViewMode
    val categoriesList = listOf(
        TeamCategory(0, "Work" ),
        TeamCategory(1, "Vacation" ),
        TeamCategory(2, "Project" ),
        TeamCategory(3, "Family" ),
        TeamCategory(4, "Party" ),
        TeamCategory(5, "Events" ),
        TeamCategory(6, "Roommates" ),
    )

    Row(
        modifier = Modifier.fillMaxWidth(0.75F),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Category:",
            style = MaterialTheme.typography.headlineMedium.copy(
                Color.LightGray,
                fontSize = 16.sp
            )
        )

        //Spacer(modifier = Modifier.width(32 .dp))
        Spacer(modifier = Modifier.weight(1f))

        Column {
            DynamicSelectTextField(
                selectedValue = teamViewModel.categoryValue,
                options = categoriesList,
                onValueChangedEvent = { teamViewModel.setTeamCategory(it) }
            )
        }
    }
}

fun generateInviteLink(team: Team, teamId: String): String {
    val url = "https://madlab5/invite" //change MADLab4 (app name)
    val teamName = deleteAllSpacesFromString(team.name).lowercase()

    return "$url/$teamName/$teamId"
}

fun deleteAllSpacesFromString(input: String): String {
    return input.replace("\\s".toRegex(), "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicSelectTextField(
    selectedValue: String,
    options: List<TeamCategory>,
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
            options.forEach { option: TeamCategory ->
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
fun TeamCreated(navController: NavHostController, teamViewModel: TeamViewModel, localConf: LocalConfiguration) {
    val teamName = teamViewModel.nameValue

    FunctionCompleted(
        "Team",
        teamName,
        teamViewModel,
        navController,
        TeamListScreen.TeamList.name,
        localConf
    )
}

@Composable
fun FunctionCompleted(  // Dopo che aggiungiamo tutti i navComponent si può usare quella di newTask forse
    nameComponent: String,
    nameCompletion: String,
    teamViewModel: TeamViewModel,
    navController: NavHostController,
    screenToReturn: String,
    localConf: LocalConfiguration
) {

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
                    .fillMaxWidth(0.9f),
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
                val team = teamViewModel.fromViewModelToTeam()
                DatabaseTeam().addTeam(team)

                //localConf.globalTeamList.add(team)
                navController.navigate(screenToReturn)
            },
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

data class TeamCategory(val id: Int, val name: String)
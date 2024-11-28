package it.polito.madlab5.screens.TeamScreen

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import it.polito.madlab5.alerts.LoadOrTakeTeamImageAlert
import it.polito.madlab5.alerts.NoPermissionAlert
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.database.DatabaseTeam
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.Task
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.model.Team.fromTeamToViewModel
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.Purple80
import it.polito.madlab5.viewModel.TeamViewModels.TeamViewModel
import it.polito.madlab5.viewModel.TeamViewModels.fromViewModelToTeam

enum class EditTeamScreen(){
    EditTeam,
    TeamEdited
}

@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun TeamEditController(
    generalNavController: NavHostController,
    team: Team
){    //
    val navController = rememberNavController()
    val teamViewModel = remember {
        team.fromTeamToViewModel()
    }

    NavHost(
        navController = navController,
        startDestination = EditTeamScreen.EditTeam.name
    ){
        composable(route = EditTeamScreen.EditTeam.name) {
            EditTeam(navController = navController, generalNavHostController = generalNavController, teamViewModel, team.category)
        }
        composable(route = EditTeamScreen.TeamEdited.name) {
            TeamEdited(generalNavController, teamViewModel, team)
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun EditTeam(navController: NavHostController, generalNavHostController: NavHostController,  teamViewModel: TeamViewModel, currentCategory: String){   //, generalNavController: NavHostController, taskViewModel: TaskViewModel
    val showAlert = remember { mutableStateOf(false) }
    val showNoPermissionAlert = remember { mutableStateOf(false) }
    val teamMembers by DatabaseProfile().getTeamMembersFromTeam(teamViewModel.teamId).collectAsState(initial = emptyList())

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
                    text = "Edit Team",
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
                if(teamViewModel.imageValue != null){
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
                    text = "Team name",
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
                    text = "Team category",
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
                    text = "Team description",
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

            //Remove members from the Team
            TeamMembers(teamMembers.toMutableList(), teamViewModel.teamAdminValue)
        }

        Button(
            onClick = {
                teamViewModel.validate()
                if (teamViewModel.isValid) {
                    if(currentCategory != teamViewModel.categoryValue) {
                        changeCategory(teamViewModel)
                    }
                    navController.navigate(EditTeamScreen.TeamEdited.name)
                }
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

fun changeCategory(teamViewModel: TeamViewModel) {
    val newCategory = teamViewModel.categoryValue

    teamViewModel.taskListState.forEach {task ->
        task.category = newCategory
    }
}

@Composable
fun TeamMembers(teamMembers: MutableList<Profile>, teamAdmin: String) {

    /* Da Modificare */
    val nMembers = teamMembers.size

    Row(
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        Text(
            text = "Team members: $nMembers",
            style = MaterialTheme.typography.headlineSmall.copy(
                Color.LightGray,
                fontSize = 16.sp,
                //fontWeight = FontWeight.Bold
            )
        )
    }



    // Add members
    TeamMemberChipList(teamMembers = teamMembers, teamAdmin)
}

@Composable
fun TeamMemberChipList(teamMembers: MutableList<Profile>, teamAdmin: String) {
    // In seguito potrà diventare: val teamAdmin: Person
    val teamAdminUsername = teamAdmin

    val context = LocalContext.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth(0.9f)
    ) {
        items(teamMembers) {user ->
            val username = user.username

            TeamMemberChip(
                user,
                modifier = Modifier
                    .size(90.dp, 100.dp),  //width - height
                //profileImageUrl = "profile_image_url_$index",
                username = username,
                onRemove = {
                    if(user.username == teamAdminUsername) {
                        // admin cannot leave the Team
                        Toast.makeText(context, "The admin cannot leave the Team!", Toast.LENGTH_LONG).show()
                    } else {
                        /* DA MODIFICARE */
                        /*teamMembers.remove(user)
                        for (task in taskList){
                            task.assignedPerson = task.assignedPerson.filter { it != user }.toMutableList()
                        }*/
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun TeamMemberChip(
    user: Profile,
    modifier: Modifier = Modifier,
    username: String,
    //profileImageUrl: String,
    onRemove: () -> Unit
) {
    AssistChip(
        onClick = { /* Maybe we can show the profile of the Team member */ },
        label = {
            Box {
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
                        ProfileImageMemberChip(user)
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
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Remove icon
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircle,
                        contentDescription = "Remove",
                        tint = Purple40
                    )
                }
            }
        },
        shape = RoundedCornerShape(20),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.White
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = Purple40
        ),
        modifier = modifier
    )
}

@Composable
fun ProfileImageMemberChip(memberProfile: Profile) {
    Box(
        modifier = Modifier
            //.padding(end = 4.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(color = Purple80),
        contentAlignment = Alignment.Center
    ) {
        if(memberProfile.image != null) {
            Image(
                bitmap = memberProfile.image!!.asImageBitmap(),
                contentDescription =null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "${memberProfile.name.first().uppercase()}${memberProfile.lastname.first().uppercase()}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
            )
        }
    }
}

@Composable
fun TeamEdited(navController: NavHostController, teamViewModel: TeamViewModel, team: Team) {
    val teamName = teamViewModel.nameValue

    FunctionEditCompleted(
        "Team",
        teamName,
        TeamDetailsScreen.TeamDetails.name,
        navController,
        teamViewModel,
        team
    )
}

@Composable
fun FunctionEditCompleted(
    nameComponent: String,
    nameCompletion: String,
    screenToReturn: String,
    navController: NavHostController,
    teamViewModel: TeamViewModel,
    team: Team
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

            Row(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "$nameCompletion has been modified",
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
                team.copyFromViewModel(teamViewModel)
                DatabaseTeam().updateTeam(teamViewModel.fromViewModelToTeam())
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
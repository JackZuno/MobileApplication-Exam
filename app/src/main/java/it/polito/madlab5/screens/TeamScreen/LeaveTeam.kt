package it.polito.madlab5.screens.TeamScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.viewModel.TeamViewModels.TeamListViewModel

@Composable
fun AlertDialogLeave(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    confirmationString: String
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "LeaveIcon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(confirmationString)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LeaveTeam(
    team: Team,
    teamlvm: TeamListViewModel,
    navController: NavHostController,
    localConf: LocalConfiguration,
    user: String
){
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
                    text = "Team Successfully Left",
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
                    text = "You left ${team.name}",
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
                leaveTeam(teamlvm, team, localConf, user)
                navController.navigate(TeamListScreen.TeamList.name)
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
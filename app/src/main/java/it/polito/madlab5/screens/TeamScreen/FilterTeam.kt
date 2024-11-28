package it.polito.madlab5.screens.TeamScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.viewModel.TeamViewModels.TeamListViewModel

fun applyFilterTeam(
    teamlvm: TeamListViewModel,
    filterCategory: SnapshotStateList<Pair<Boolean, String>>,
    isAdmin: MutableState<Boolean>,
    localConf: LocalConfiguration
) {

    val llCat = mutableListOf<String>()
    llCat.addAll(filterCategory.filter { fc -> fc.first }.map { fc -> fc.second })//di ogni lista prendo solo i nomi selezionati
    teamlvm.teamList.clear()

    if (llCat.isEmpty()){
        llCat.add("All")
    }
    if(llCat[0] == "All"){
        teamlvm.teamList.addAll(localConf.personalTeamList.value.second)
    } else {
        val filteredTeam = localConf.personalTeamList.value.second.filter { team -> team.category in llCat }
        teamlvm.teamList.addAll(filteredTeam)
    }

    if(isAdmin.value){
        val teamlOnlyAdmin = teamlvm.teamList.filter { team -> team.teamAdmin == localConf.uid }
        teamlvm.teamList.clear()
        teamlvm.teamList.addAll(teamlOnlyAdmin)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FilterScreenTeam(navController: NavController, teamlvm: TeamListViewModel, localConf: LocalConfiguration){

    val filterCategory = remember { mutableStateListOf(
        Pair(true, "All"),
        Pair(false, "Work"),
        Pair(false, "Vacation"),
        Pair(false, "Project"),
        Pair(false, "Family"),
        Pair(false, "Party"),
        Pair(false, "Events"),
        Pair(false, "Roommates")
    ) }

    val isAdmin = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Task Filter")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, "backIcon")
                    }
                },

                //backgroundColor = MaterialTheme.colors.primary,
                //contentColor = Color.White,
                //elevation = 10.dp
            )
        }, content = {

            //FilterOption(navController, tlvm, filterStatus, filterTag, filterMembers)
            FilterOptionTeam(navController, teamlvm, filterCategory, isAdmin, localConf)

        })
}
@Composable
fun FilterOptionTeam(
    navController: NavController,
    teamlvm: TeamListViewModel,
    filterCategory: SnapshotStateList<Pair<Boolean, String>>,
    isAdmin: MutableState<Boolean>,
    localConf: LocalConfiguration
){
    Column (
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally){
        Row(
            modifier = Modifier
                .padding(top = 70.dp)
                .fillMaxWidth(0.9f),
            //horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            CategoryButtonsTeam(filterCategory)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 30.dp),
            //horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Created by you?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Color.LightGray,
                    fontSize = 30.sp
                )
            )
            AdminCheckbox(isAdmin)
            //Spacer(modifier = Modifier.width(32 .dp))
            //Spacer(modifier = Modifier.weight(1f))
        }

        /*Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            //horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MemberChipButtons(selectedMembers = filterMembers)
        }*/

        //Text(text = filterTag.value, color = Color.LightGray, fontWeight = FontWeight.Normal)
        //Text(text = filterStatus.value, color = Color.LightGray, fontWeight = FontWeight.Normal)
        Spacer(Modifier.weight(1f))
        Button(onClick = {
            applyFilterTeam(teamlvm, filterCategory, isAdmin, localConf)
            navController.popBackStack()
        },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Purple40)
        ) {
            Text(text = "Apply", style = MaterialTheme.typography.headlineMedium.copy(
                Purple40,
                fontSize = 25.sp))
        }
    }
}

@Composable
fun CategoryButtonsTeam(filterCategory: SnapshotStateList<Pair<Boolean, String>>) {
    //val buttonStates = remember { mutableStateListOf(true, false, false, false) }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(modifier = Modifier.size(50.dp), imageVector = Icons.Filled.Category, contentDescription = null, tint = Color.LightGray)
            Text(text = "Category", color = Color.LightGray, fontWeight = FontWeight.Normal, fontSize = 35.sp)
        }
        //Spacer(modifier = Modifier.weight(0.1f))
        Row(
            modifier = Modifier
                //.fillMaxWidth(0.9f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            for (index in filterCategory.indices) {
                Button(
                    colors = if (filterCategory[index].first) ButtonDefaults.buttonColors(containerColor = Purple40) else ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    onClick = {
                        if (index == 0) {
                            // Deselect all buttons
                            for (i in 1..<filterCategory.size){
                                filterCategory[i] = Pair(false, filterCategory[i].second)
                            }
                        }else{
                            filterCategory[0] = Pair(false, filterCategory[0].second)
                        }
                        filterCategory[index] = Pair(!filterCategory[index].first, filterCategory[index].second)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .background(color = Color.White)
                    ,
                ) {
                    Text(text = filterCategory[index].second)
                }
            }
        }
    }
}

@Composable
fun AdminCheckbox(isAdmin: MutableState<Boolean>) {

    Row(modifier = Modifier
        .padding(start = 20.dp),
        verticalAlignment = Alignment.CenterVertically){
        Checkbox(
            checked = isAdmin.value,//taskViewModel.recurrentValue,
            onCheckedChange = {isAdmin.value = !isAdmin.value}//taskViewModel::setRecurrentFlag
        )

        if(isAdmin.value){
            Text(
                text = "Yes",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Color.LightGray,
                    fontSize = 18.sp
                )
            )
        }else{
            Text(
                text = "No",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Color.LightGray,
                    fontSize = 18.sp
                )
            )
        }
    }
}
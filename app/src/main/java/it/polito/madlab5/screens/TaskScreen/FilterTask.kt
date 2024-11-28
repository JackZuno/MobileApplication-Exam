package it.polito.madlab5.screens.TaskScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Task.TaskStates
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.viewModel.TaskViewModels.TaskListViewModel

fun applyFilter(
    tlvm: TaskListViewModel,
    filterStatus: SnapshotStateList<Pair<Boolean, TaskStates>>,
    filterTag: SnapshotStateList<Pair<Boolean, String>>,
    filterMembers: SnapshotStateList<Pair<Boolean, Profile>>,
    isPersonal: Boolean
) {

    val llStatus = mutableListOf<TaskStates>()
    llStatus.addAll(filterStatus.filter { fs -> fs.first }.map { fs -> fs.second })
    val llTag = mutableListOf<String>() //di ogni lista prendo solo i nomi selezionati
    llTag.addAll(filterTag.filter { ft -> ft.first }.map { ft -> ft.second })

    val llMember = filterMembers.filter { m -> m.first }.map { m -> m.second.username }

    if (llStatus.isEmpty()){
        llStatus.add(TaskStates.All)
    }
    if (llTag.isEmpty()){
        llTag.add("All")
    }

    if(llStatus[0].name == "All" && llTag[0] == "All"){ //BOH??
        tlvm.filteredTaskList.clear()
        tlvm.filteredTaskList.addAll(tlvm.taskList)
    } else if (llStatus[0].name == "All"){
        val filteredTaskOnlyTag = tlvm.taskList.filter { task -> task.tag in llTag }
        tlvm.filteredTaskList.clear()
        tlvm.filteredTaskList.addAll(filteredTaskOnlyTag)
    } else if (llTag[0] == "All"){
        val filteredTaskOnlyStatus = tlvm.taskList.filter { task -> task.taskStatus in llStatus }
        tlvm.filteredTaskList.clear()
        tlvm.filteredTaskList.addAll(filteredTaskOnlyStatus)
    } else {
        val ll: MutableList<String> = mutableListOf()
        for (element in llStatus){
            ll.add(element.name)
        }
        ll.addAll(llTag)
        val filteredTask = tlvm.taskList.filter { task -> task.taskStatus.name in ll && task.tag in ll }
        tlvm.filteredTaskList.clear()
        tlvm.filteredTaskList.addAll(filteredTask)
    }

    if(llMember.isNotEmpty() && !isPersonal){
        val filteredMembers = tlvm.filteredTaskList.filter { task -> task.assignedPerson.any { it.username in llMember } }
        tlvm.filteredTaskList.clear()
        tlvm.filteredTaskList.addAll(filteredMembers)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FilterScreen(
    navController: NavController,
    tlvm: TaskListViewModel,
    membersList: List<Pair<Boolean, Profile>>,
    isPersonal: Boolean
){

    val filterStatus = remember { mutableStateListOf(
        Pair(true, TaskStates.All),
        Pair(false, TaskStates.Pending),
        Pair(false, TaskStates.Completed),
        Pair(false, TaskStates.InProgress),
        Pair(false, TaskStates.OverDue)
    ) }

    val filterTag = remember { mutableStateListOf(
        Pair(true, "All"),
        Pair(false, "Project"),
        Pair(false, "Finance"),
        Pair(false, "Design"),
        Pair(false, "Chores"),
        Pair(false, "Grocery shopping")
    ) }

    val filterMembers = remember { mutableStateListOf<Pair<Boolean,Profile>>() }
    filterMembers.clear()
    filterMembers.addAll(membersList)


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
                }
            )
        }, content = {

            FilterOption(navController, tlvm, filterStatus, filterTag, filterMembers, isPersonal)

        })
}
@Composable
fun FilterOption(
    navController: NavController,
    tlvm: TaskListViewModel,
    filterStatus: SnapshotStateList<Pair<Boolean, TaskStates>>,
    filterTag: SnapshotStateList<Pair<Boolean, String>>,
    filterMembers: SnapshotStateList<Pair<Boolean, Profile>>,
    isPersonal: Boolean
){
    Column (Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally){
        Row(
            modifier = Modifier
                .padding(top = 70.dp)
                .fillMaxWidth(0.9f),
            //horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            StatusButtons(filterStatus)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 15.dp),
            //horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tag",
                style = MaterialTheme.typography.headlineMedium.copy(
                    Color.LightGray,
                    fontSize = 35.sp
                )
            )

            //Spacer(modifier = Modifier.width(32 .dp))
            //Spacer(modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            //horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TagButtons(filterTag)
        }

        if (!isPersonal){
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 15.dp),
                //horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        Color.LightGray,
                        fontSize = 35.sp
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                //horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                MemberChipButtons(selectedMembers = filterMembers)
            }
        }

        //Text(text = filterTag.value, color = Color.LightGray, fontWeight = FontWeight.Normal)
        //Text(text = filterStatus.value, color = Color.LightGray, fontWeight = FontWeight.Normal)
        Spacer(Modifier.weight(1f))
        Button(onClick = {
            applyFilter(tlvm, filterStatus, filterTag, filterMembers, isPersonal)
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
fun StatusButtons(filterStatus: SnapshotStateList<Pair<Boolean, TaskStates>>) {
    //val buttonStates = remember { mutableStateListOf(true, false, false, false) }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(modifier = Modifier.size(50.dp), imageVector = Icons.Filled.WifiTethering, contentDescription = null, tint = Color.LightGray)
            Text(text = "Status", color = Color.LightGray, fontWeight = FontWeight.Normal, fontSize = 35.sp)
        }
        //Spacer(modifier = Modifier.weight(0.1f))
        Row(
            modifier = Modifier
                //.fillMaxWidth(0.9f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            for (index in filterStatus.indices) {
                Button(
                    colors = if (filterStatus[index].first) ButtonDefaults.buttonColors(containerColor = Purple40) else ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    onClick = {
                        if (index == 0) {
                            // Deselect all buttons
                            for (i in 1..<filterStatus.size){
                                filterStatus[i] = Pair(false, filterStatus[i].second)
                            }
                        }else{
                            filterStatus[0] = Pair(false, filterStatus[0].second)
                        }
                        filterStatus[index] = Pair(!filterStatus[index].first, filterStatus[index].second)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .background(color = Color.White)
                    ,
                ) {
                    Text(text = filterStatus[index].second.name)
                }
            }
        }
    }
}

@Composable
fun TagButtons(filterTag: SnapshotStateList<Pair<Boolean, String>>) {
    //val buttonStates = remember { mutableStateListOf(true, false, false, false) }

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .horizontalScroll(rememberScrollState()),
        //horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {
        for (index in filterTag.indices) {
            Button(
                colors = if (filterTag[index].first) ButtonDefaults.buttonColors(containerColor = Purple40) else ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                onClick = {
                    if (index == 0) {
                        // Deselect all buttons
                        for (i in 1..<filterTag.size){
                            filterTag[i] = Pair(false, filterTag[i].second)
                        }
                    }else{
                        filterTag[0] = Pair(false, filterTag[0].second)
                    }
                    filterTag[index] = Pair(!filterTag[index].first, filterTag[index].second)
                },
                modifier = Modifier
                    .padding(8.dp)
                    .background(color = Color.White)
                ,
            ) {
                Text(text = filterTag[index].second)
            }
        }
    }
}

@Composable
fun MemberChipButtons(selectedMembers: SnapshotStateList<Pair<Boolean, Profile>>) {
    LazyRow(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(0.9f)
    ) {
        itemsIndexed(selectedMembers) { i, user ->
            MemberChipFilter(
                modifier = Modifier
                    .size(90.dp, 100.dp),  //width - height
                //profileImageUrl = "profile_image_url_$index",
                username = user.second.username,
                isSelected = user.first,
                onSelected = { selected ->
                    if(selected) {
                        selectedMembers[i] = Pair(true, user.second)
                    } else {
                        selectedMembers[i] = Pair(false, user.second)
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun MemberChipFilter(
    modifier: Modifier = Modifier,
    username: String,
    //profileImageUrl: String,
    isSelected: Boolean,
    onSelected: (Boolean) -> Unit
) {
    AssistChip(
        onClick = { onSelected(!isSelected) },
        label = {
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
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile Image",
                        tint = if(isSelected) Color.White else Color.Black,
                        modifier = Modifier
                            .size(42.dp)
                    )
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
                        color = if(isSelected) Color.White else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        shape = RoundedCornerShape(20),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if(isSelected) Purple40 else Color.White
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = if(isSelected) Color.Transparent else Purple40
        ),
        modifier = modifier
    )
}
package it.polito.madlab5.screens.profileScreen

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.InsertChartOutlined
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.polito.madlab5.database.DatabaseChats
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.Profile.fromProfileToViewModel
import it.polito.madlab5.model.chat.Chat
import it.polito.madlab5.model.chat.PersonChat
import it.polito.madlab5.screens.TeamScreen.AlertDialogLeave
import it.polito.madlab5.screens.TeamScreen.LoadingComponent
import it.polito.madlab5.screens.TeamScreen.LoadingState
import it.polito.madlab5.screens.TeamScreen.TeamList
import it.polito.madlab5.screens.chatScreen.ChatView
import it.polito.madlab5.screens.statistics.ShowPersonalTeamStats
import it.polito.madlab5.screens.statistics.ShowStats
import it.polito.madlab5.screens.statistics.TeamStatsEntry
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.Purple80
import it.polito.madlab5.ui.theme.backgroundBar
import it.polito.madlab5.ui.theme.lightGrayCustom
import it.polito.madlab5.viewModel.ProfileViewModels.ProfileViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class ProfileScreen{
    ShowProfile,
    DirectChat,
    EditProfile,
    PersonalStats,
    TeamStats
}
@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ProfileMain(profile: Profile?, navController: NavHostController, loggedInUser: String, isProfileMain: Boolean, signOut: () -> Unit, localConf: LocalConfiguration){
    val nestedNavController = rememberNavController()
    val profilevm = profile?.fromProfileToViewModel()
    val openAlertDialog = remember { mutableStateOf(false) }
    val profileUpdate by DatabaseProfile().getProfileByIdEdit(loggedInUser).collectAsState(initial = Pair(LoadingState.EMPTY, null))


    NavHost(
        navController = nestedNavController,
        startDestination = ProfileScreen.ShowProfile.name
    ) {

        composable(route = ProfileScreen.ShowProfile.name) {
            if (profilevm != null) {
                if(isProfileMain){
                    if(profileUpdate.first == LoadingState.SUCCESS)
                        ShowProfile(profileUpdate.second?.fromProfileToViewModel()!!, navController, nestedNavController, loggedInUser, isProfileMain, openAlertDialog) { signOut() }
                }else{
                    ShowProfile(profilevm, navController, nestedNavController, loggedInUser, isProfileMain, openAlertDialog) { signOut() }
                }

                localConf.isBottomBar.value = true
            }
        }
        composable(route = ProfileScreen.DirectChat.name) {
            if (profile != null){
                /* Search the chat in the DB */
                val isPresent = localConf.checkPersonChat(profileId = profile.id)
                if (isPresent){
                    val chat = localConf.returnChat(profile.id)
                    ChatView(navController = navController, chat)
                    localConf.isBottomBar.value = false
                } else {
                    val newChat = PersonChat(profileFrom = loggedInUser, profileTo = profile.id,
                        lastAccess = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    DatabaseChats().addPersonalChat(newChat)
                    ChatView(navController = navController, chat = newChat)
                    localConf.isBottomBar.value = false
                }
            }
        }
        composable(route = ProfileScreen.PersonalStats.name){
            ShowStats(nestedNavController)
            localConf.isBottomBar.value = false
        }
        composable(route = ProfileScreen.TeamStats.name){
            ShowPersonalTeamStats(nestedNavController)
        }
        composable(route = ProfileScreen.EditProfile.name) {
            val profileWithLoadingState by DatabaseProfile().getProfileByIdEdit(loggedInUser).collectAsState(
                initial = Pair(LoadingState.LOADING, null)
            )

            when(profileWithLoadingState.first) {
                LoadingState.LOADING -> {
                    LoadingComponent()
                }
                LoadingState.SUCCESS -> {
                    val profileEdit = profileWithLoadingState.second

                    if (profileEdit != null) {
                        CreateOrEditProfile(
                            profileEdit.fromProfileToViewModel(),
                            uid = loggedInUser,
                            backRoute = ProfileScreen.ShowProfile.name,
                            navController = nestedNavController
                        )
                        localConf.isBottomBar.value = false
                    }
                }
                LoadingState.EMPTY -> {
                    //
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShowProfile(
    vm: ProfileViewModel,
    navController: NavHostController,
    nestedNavController: NavHostController,
    loggedInUser: String,
    isProfileMain: Boolean,
    openAlertDialog: MutableState<Boolean>,
    signOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Profile")
                },
                navigationIcon = {
                                 if ( !isProfileMain){
                                     IconButton(onClick = { navController.popBackStack() }) {
                                         Icon(Icons.Filled.ArrowBack, "backIcon")
                                     }
                                 }

                },
                actions = {
                    if (vm.idValue == loggedInUser) {
                        IconButton(onClick = { signOut() }) {
                            Icon(Icons.Filled.Logout, "backIcon")
                        }
                    }

                    if(loggedInUser != vm.idValue) {
                        Button(colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray),
                            onClick = { nestedNavController.navigate(ProfileScreen.DirectChat.name) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Message")
                                Spacer(modifier = Modifier.width(4.dp))

                                val unreadMessages = remember { mutableStateOf(0) }

                                if(doesDirectChatExist(vm)) {
                                    //countUnreadDirectMessages(unreadMessages, vm)
                                    Log.d("After the count in the if", "${unreadMessages.value}")
                                } else {
                                    Log.d("NO CHAT", "NO CHAT")
                                }

                                Log.d("Before the if for the badge", "${unreadMessages.value}")
                                if(unreadMessages.value > 0) {
                                    Badge(content = { Text(text = "${unreadMessages.value}") })
                                }
                            }
                        }
                    } else {
                        IconButton(onClick = { nestedNavController.navigate(ProfileScreen.EditProfile.name) }) {
                            Icon(Icons.Filled.Edit, "EditIcon")
                        }
                    }
                }
            )
        },content = {
            Log.d("LoggedInUser", loggedInUser)
            Log.d("profileId", vm.id)
            ShowProfileContent(vm, nestedNavController)
        }
    )
}



fun doesDirectChatExist(vm: ProfileViewModel): Boolean {
    var found = false

    /*loggedInUserone.personalChats.forEach {
        if(it.profileTo.username == vm.username) {
            found = true
        }
    }*/

    return found
}

/*fun countUnreadDirectMessages(unreadMessages: MutableState<Int>, vm: ProfileViewModel) {
    unreadMessages.value = loggedInUserone.personalChats.first { it.profileTo.username == vm.username }.messagesState.count{
        isFirstDateGreaterThanSecond(it.sentAt, loggedInUserone.personalChats.first{ it.profileTo.username == vm.username }.lastAccess) && !it.isFromMe
    }
}*/

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowProfileContent(vm: ProfileViewModel, navController: NavHostController){
    Column(
        modifier = Modifier
            .padding(top = 60.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image
        // Mettere qui il codice per la vera immagine
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(start = 0.dp, top = 16.dp, end = 0.dp, bottom = 0.dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(Purple80),
        ) {
            if(vm.image != null){
                Image(
                    bitmap = vm.image!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                if(vm.name.isNotBlank() && vm.lastname.isNotBlank()) {
                    Text(
                        text = vm.name.first().uppercase() + vm.lastname.first().uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = backgroundBar
                    )
                }
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            //Nickname
            if(vm.username.isNotBlank()) {
                Text(
                    text = "@"+vm.username,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray

                    )
                )
            }
        }
        // Name + Last name
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if(vm.name.isNotBlank() && vm.lastname.isNotBlank()) {
                Text(
                    vm.name.trim() + " " + vm.lastname.trim(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
        if(vm.email.isNotBlank()){
            Text(
                text = vm.email,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    color = Color.LightGray
                ),
                modifier = Modifier.padding(bottom = 5.dp)

            )
        }
        //Location
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            if(vm.location.isNotBlank()) {
                Text(
                    text = vm.location,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal,
                        color = Purple40

                    )
                )
                Divider(color = Color.LightGray, modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxHeight(
                    )
                    .width(1.dp))

                Text(
                    text = "Joined " +vm.joineddate,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = Color.LightGray
                    )
                )
            }
        }


        Row  (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 10.dp)){
            OutlinedButton(onClick = { navController.navigate(ProfileScreen.PersonalStats.name)}, modifier = Modifier
                .weight(0.6f, false)
                .padding(end = 10.dp), colors = ButtonDefaults.buttonColors(contentColor = Color.Black, containerColor = Color.Transparent
            ),border = BorderStroke(1.dp, Color.LightGray),shape = RoundedCornerShape(20)) {
                Icon(imageVector = Icons.Outlined.InsertChartOutlined, contentDescription = null)
                Text("My Stats")
            }

            OutlinedButton(onClick = { navController.navigate(ProfileScreen.TeamStats.name) }, modifier = Modifier.weight(0.4f, false),colors = ButtonDefaults.buttonColors(contentColor = Color.Black, containerColor = Color.Transparent
            ),border = BorderStroke(1.dp, Color.LightGray),shape = RoundedCornerShape(20)) {
                Icon(imageVector = Icons.Outlined.Group, contentDescription = null)
                Text("Team Stats")
            }
        }

        if (vm.description.isNotBlank()) {
            Row(modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(bottom = 5.dp),
                horizontalArrangement = Arrangement.Center) {
                Text(
                    text = vm.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp),
                )
            }
        }

        Divider(color = lightGrayCustom, modifier = Modifier.fillMaxWidth(0.85F))

        FlowRow (
            Modifier
                .fillMaxWidth(0.8F)
                .padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            vm.skills.forEach{ item ->
                AssistChip(
                    onClick = { /*TODO*/ },
                    label = { Text(text = item.trim(),
                        textAlign = TextAlign.Center,
                        style =
                        MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp)
                    )
                    },
                    shape = RoundedCornerShape(20),
                    colors = AssistChipDefaults.assistChipColors(containerColor = lightGrayCustom),
                    border = AssistChipDefaults.assistChipBorder(borderColor = Color.LightGray)

                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ShowProfileLandscape(vm: ProfileViewModel = viewModel()) {
    Row {
        Column(modifier = Modifier
            .weight(0.3f)
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(175.dp)
                    .clip(CircleShape)
                    .background(Purple80),
            ) {

                if(vm.imageValue != null){
                    Image(bitmap = vm.imageValue!!.asImageBitmap(), contentDescription =null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize() )

                } else {
                    if(vm.nameValue.isNotBlank() && vm.lastNameValue.isNotBlank()) {
                        Text(
                            text = vm.nameValue.first().uppercase() + vm.lastNameValue.first().uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = backgroundBar
                        )
                    }
                }
            }
            if(vm.nicknameValue.isNotBlank()) {
                Text(
                    text = "@"+vm.nicknameValue,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray

                    )
                )
            }
        }

        Divider(color = Color.LightGray, modifier = Modifier
            .fillMaxHeight(0.8f)
            .width(1.dp)
            .align(Alignment.CenterVertically)
        )


        Column(
            modifier = Modifier
                .weight(0.7f)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
                .fillMaxWidth(0.8f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Name + Last name
            Row(
                //modifier = Modifier.fillMaxWidth(0.8f),
                //verticalAlignment = Alignment.CenterVertically
            ) {
                if(vm.nameValue.isNotBlank() && vm.lastNameValue.isNotBlank()) {
                    Text(
                        vm.nameValue.trim() + " " + vm.lastNameValue.trim(),
                        modifier = Modifier
                            .weight(1f),
                        //.padding(16.dp, 0.dp),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
            if(vm.emailValue.isNotBlank()){
                Text(
                    text = vm.emailValue,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = Color.LightGray
                    ),
                    modifier = Modifier.padding(bottom = 5.dp)

                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                if(vm.locationValue.isNotBlank()) {
                    Text(
                        text = vm.locationValue,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal,
                            color = Purple40

                        )
                    )

                    Divider(color = Color.LightGray, modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxHeight(
                        )
                        .width(1.dp))

                    Text(
                        text = "Joined " +vm.joinedDateValue,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            color = Color.LightGray
                        )
                    )
                }
            }
            Row  (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 10.dp)){
                OutlinedButton(onClick = { vm.showStats()}, modifier = Modifier
                    .weight(0.6f, false)
                    .padding(end = 10.dp), colors = ButtonDefaults.buttonColors(contentColor = Color.Black, containerColor = Color.Transparent
                ),border = BorderStroke(1.dp, Color.LightGray),shape = RoundedCornerShape(20)) {
                    Icon(imageVector = Icons.Outlined.InsertChartOutlined, contentDescription = null)
                    Text("My Stats")
                }

                OutlinedButton(onClick = { vm.showTeamStats()}, modifier = Modifier.weight(0.4f, false),colors = ButtonDefaults.buttonColors(contentColor = Color.Black, containerColor = Color.Transparent
                ),border = BorderStroke(1.dp, Color.LightGray),shape = RoundedCornerShape(20)) {
                    Icon(imageVector = Icons.Outlined.Group, contentDescription = null)
                    Text("Team Stats")
                }
            }
            //description
            if (vm.descriptionValue.isNotBlank()) {
                Row(modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 5.dp),
                    horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = vm.descriptionValue,
                        style = MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp),
                    )
                }
            }
            Divider(color = lightGrayCustom, modifier = Modifier.fillMaxWidth(0.85F))



            FlowRow (
                Modifier
                    .fillMaxWidth(0.8F)
                    .padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                vm.skillsValue.forEach{ item ->
                    AssistChip(
                        onClick = { /*TODO*/ },
                        label = { Text(text = item.trim(),
                            textAlign = TextAlign.Center,
                            style =
                            MaterialTheme.typography.bodySmall.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp)
                        )
                        },
                        shape = RoundedCornerShape(20),
                        colors = AssistChipDefaults.assistChipColors(containerColor = lightGrayCustom),
                        border = AssistChipDefaults.assistChipBorder(borderColor = Color.LightGray)

                    )
                }
            }

        }
    }
}
@Composable
fun DialogLogOut(openAlertDialog: MutableState<Boolean>, signOut: () -> Unit) {
    when {
        // ...
        openAlertDialog.value -> {
            AlertDialogLeave(
                onDismissRequest = { openAlertDialog.value = false },
                onConfirmation = {
                    openAlertDialog.value = false
                    signOut()
                },
                dialogTitle = "Logout",
                dialogText = "Are you sure you want to logout",
                icon = Icons.Filled.Logout,
                confirmationString = "Logout"
            )
        }
    }
}
package it.polito.madlab5

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.model.LocalConfiguration
import it.polito.madlab5.screens.TaskScreen.PersonalTaskList
import it.polito.madlab5.screens.TeamScreen.LoadingComponent
import it.polito.madlab5.screens.TeamScreen.LoadingState
import it.polito.madlab5.screens.TeamScreen.TeamListController
import it.polito.madlab5.screens.profileScreen.CreateOrEditProfile
import it.polito.madlab5.screens.profileScreen.ProfileMain
import it.polito.madlab5.ui.theme.Lab4Theme
import it.polito.madlab5.viewModel.ProfileViewModels.ProfileViewModel
import it.polito.madlab5.viewModel.TaskViewModels.TaskListViewModel
import it.polito.madlab5.viewModel.TeamViewModels.TeamListViewModel

enum class GeneralAppScreen{
    TaskList,
    NewTask,
    TaskDetails,
    TaskFilter,
    PersonalTaskList
}

data class TabBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : ComponentActivity() {
    val myTask = TabBarItem(title = "My Task", selectedIcon = Icons.Filled.Task, unselectedIcon = Icons.Outlined.Task)
    val teamList = TabBarItem(title = "My Team", selectedIcon = Icons.Filled.People, unselectedIcon = Icons.Outlined.People)
    val myProfile = TabBarItem(title = "My Profile", selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person)
    val tabBarItems = listOf(teamList, myTask, myProfile)

    val isLoading = mutableStateOf(true)

    //val teamlvm = TeamListViewModel(localConf.globalTeamList)
    //val tlvm = TaskListViewModel(localConf.globalTaskList, localConf.globalProfileMembers)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var uuid = intent.getStringExtra("uid")
        val loggedEmail = intent.getStringExtra("email")

        val intentHandleLink: Intent = intent

        val localConf: LocalConfiguration

        if (uuid == null || loggedEmail == null) {
            // Redirect to login activity
            val loginIntent = Intent(this, LoginActivity::class.java)
            val uri: Uri? = intent.data
            if (uri != null) {
                // Pass the invite link to the login activity
                val userIdLink = Firebase.auth.currentUser?.uid
                uuid = userIdLink
                loginIntent.data = intent.data

                Log.d("LocalConf load", "UserId: $uuid")
                localConf = LocalConfiguration(uuid!!)
                localConf.loadPersonalTeams()
            }

            startActivity(loginIntent)
            finish()
            return
        } else {
            Log.d("LocalConf load", "UserId: $uuid")
            localConf = LocalConfiguration(uuid)
            localConf.loadPersonalTeams()
        }

        setContent {
            val navController = rememberNavController()
            /*if (tlvm.taskList.size != localConf.globalTaskList.size){
                tlvm.filteredTaskList = localConf.globalTaskList.toMutableStateList()
            }*/
            //tlvm.taskList = localConf.globalTaskList.toMutableStateList()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            //Log.d("LocalConf", "${localConf.globalTaskList.size}")
            val profileDB = DatabaseProfile()
            val profile = profileDB.getProfileByIdEdit(uuid).collectAsState(initial = Pair(LoadingState.LOADING, null))
            Lab4Theme(darkTheme = false) {
                // A surface container using the 'background' color from the theme

                val teamListViewModel = TeamListViewModel()
                teamListViewModel.teamList.addAll(localConf.personalTeamList.value.second)
                val personaltlvm = TaskListViewModel()
                localConf.loadPersonalTasks()
                localConf.loadPersonalChats(uuid)
                localConf.loadTeamChats(uuid)
                personaltlvm.taskList.clear()
                personaltlvm.taskList.addAll(localConf.personalTaskList.value)
                personaltlvm.filteredTaskList.clear()
                personaltlvm.filteredTaskList.addAll(localConf.personalTaskList.value)
                personaltlvm.loadProfiles()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (profile.value.first == LoadingState.LOADING) {// isLoading
                        LoadingComponent()
                    }else{
                        Scaffold(bottomBar = {
                            if (localConf.isBottomBar.value) TabView(tabBarItems, navController, currentRoute) }) {
                            val bottomPadding = it.calculateBottomPadding()
                            NavHost(
                                navController = navController,
                                startDestination = if (profile.value.second == null) "newProfile" else teamList.title
                            ) {
                                composable(myTask.title) {
                                    Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                                        PersonalTaskList(user = uuid, localConf, personaltlvm, profile.value.second!!.username) { signOut() }
                                        localConf.isBottomBar.value = true
                                    }
                                }
                                composable(teamList.title) {
                                    val teamId = handleIntent(intentHandleLink, navController)

                                    when(localConf.personalTeamList.value.first) {
                                        LoadingState.LOADING -> {
                                            LoadingScreen()
                                        }
                                        LoadingState.SUCCESS -> {
                                            Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                                                TeamListController(localConf, teamId, uuid, { signOut() }, teamListViewModel, profile.value.second!!.username)
                                                localConf.isBottomBar.value = true
                                                intent.data = null
                                            }
                                        }
                                        LoadingState.EMPTY -> {
                                            Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                                                TeamListController(localConf, teamId, uuid, { signOut() }, teamListViewModel, profile.value.second!!.username)
                                                localConf.isBottomBar.value = true
                                            }
                                        }
                                    }

                                    /*Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                                        TeamListController(localConf, teamId, uuid, { signOut() }, teamListViewModel, profile.value.second!!.username)
                                        localConf.isBottomBar.value = true
                                    }*/
                                }
                                composable("newProfile") {
                                    Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                                        CreateOrEditProfile(
                                            ProfileViewModel(email = loggedEmail),
                                            navController = navController,
                                            uid = uuid,
                                            backRoute = teamList.title
                                        )
                                        localConf.isBottomBar.value = false
                                    }
                                }
                                composable(myProfile.title) {
                                    Box(modifier = Modifier.padding(bottom = bottomPadding)) {
                                        localConf.isBottomBar.value = true
                                        ProfileMain(profile = profile.value.second,
                                            navController = navController,
                                            uuid,
                                            true,
                                            { signOut() },
                                            localConf
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleIntent(intent: Intent, navControllerDeepLink: NavController): String? {
        val uri: Uri? = intent.data

        if (uri != null) {
            when {
                uri.path?.startsWith("/invite") == true -> {
                    val teamName = uri.pathSegments.getOrNull(1) ?: ""
                    val teamId = uri.pathSegments.getOrNull(2) ?: ""
                    if (teamName.isNotEmpty() && teamId.isNotEmpty()) {
                        // navigate to the Team detail
                        return teamId
                    }
                }
            }
        }

        return null
    }

    fun signOut(){
        Firebase.auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("971470859327-mqa0vmacrt8lru2gm5tcla29k6vl04a5.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        val login = Intent(this, LoginActivity::class.java)
        startActivity(login)
        finish()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Lab4Theme {
        Greeting("Android")
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = { TabBarBadgeView(badgeAmount) }) {
        Icon(
            imageVector = if (isSelected) {selectedIcon} else {unselectedIcon},
            contentDescription = title
        )
    }
}
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController, currentRoute: String?) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            val isSelected = currentRoute == tabBarItem.title
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    selectedTabIndex = index
                    navController.navigate(tabBarItem.title)
                },
                icon = {
                    TabBarIconView(
                        isSelected = selectedTabIndex == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                label = {Text(tabBarItem.title)})
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp)
        )
    }
}
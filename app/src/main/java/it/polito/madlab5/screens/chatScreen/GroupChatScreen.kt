package it.polito.madlab5.screens.chatScreen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavHostController
import coil.Coil
import it.polito.madlab5.LoadingScreen
import it.polito.madlab5.database.DatabaseChats
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.chat.Message
import it.polito.madlab5.model.chat.TeamChat
import it.polito.madlab5.screens.TeamScreen.LoadingComponent
import it.polito.madlab5.screens.TeamScreen.LoadingState
import it.polito.madlab5.ui.theme.PurpleGrey40
import it.polito.madlab5.ui.theme.PurpleGrey80
import it.polito.madlab5.ui.theme.fucsiaName
import it.polito.madlab5.ui.theme.greenName
import it.polito.madlab5.ui.theme.idkName
import it.polito.madlab5.ui.theme.orangeName
import it.polito.madlab5.viewModel.TeamViewModels.TeamViewModel
import kotlinx.coroutines.flow.first
import java.lang.reflect.Member
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatGroupView(navController: NavHostController, teamChat: TeamChat, tvm: TeamViewModel, loggedInUser: String) {
    val teamName = tvm.nameValue
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val teamChatState = remember {
        teamChat
    }
    val messagesState by DatabaseChats().getTeamChatMessages(teamChat.teamId).collectAsState(initial = mutableListOf<Message>().toMutableStateList())
    LaunchedEffect(messagesState.size) {
        DatabaseChats().getTeamChatMessages(teamChat.teamId).collect{
            teamChatState.setMessagesStateAfter(it)
        }
    }


    // Calculate number of unread messages
    val unreadMessagesCount = teamChatState.messagesState.count {
        isFirstDateGreaterThanSecond(it.sentAt,teamChatState.lastAccess ) && !it.isFromMe(loggedInUser)
    }

    val chatListState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    val currentTime = LocalDateTime.now().format(formatter)
                    teamChatState.setNewLastAccess(currentTime)
                    /*Update DB*/
                    DatabaseChats().updateTeamChatLastAccess(teamChat.chatId, currentTime)
                    navController.popBackStack()
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            title = {
                Text(
                    teamName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )},
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black
            ),
            actions = { //profile image
                ProfileImageTeam(tvm.imageValue)
            }
        )

        ConstraintLayout(
            modifier = Modifier.fillMaxSize(),
        ) {
            val (messages, chatBox) = createRefs()
                LazyColumn(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .fillMaxWidth(0.85f)
                        .constrainAs(messages) {
                            top.linkTo(parent.top)
                            bottom.linkTo(chatBox.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.fillToConstraints
                        },
                    reverseLayout = true,
                    state = chatListState
                ) {
                    // Find the index of the first unread message
                    val firstUnreadIndex = messagesState.indexOfFirst {
                        isFirstDateGreaterThanSecond(
                            it.sentAt,
                            teamChatState.lastAccess
                        )
                    }

                    itemsIndexed(messagesState.reversed()) { index, message ->
                        val displayIndex = messagesState.size - 1 - index

                        if (displayIndex == firstUnreadIndex && unreadMessagesCount > 0) {
                            Column {
                                UnreadMessagesBar(unreadCount = unreadMessagesCount)
                                Spacer(modifier = Modifier.height(8.dp))
                                ChatGroupItem(message = message, loggedInUser)
                            }
                        } else {
                            ChatGroupItem(message = message, loggedInUser)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

            TeamChatBox(modifier = Modifier
                .fillMaxWidth()
                .constrainAs(chatBox) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                messagesState,
                loggedInUser,
                teamChat
            )

            LaunchedEffect(Unit) {
                val firstUnreadIndex = messagesState.indexOfFirst { isFirstDateGreaterThanSecond(it.sentAt, teamChatState.lastAccess) && !it.isFromMe(loggedInUser) }
                val displayIndex = messagesState.size - 1 - firstUnreadIndex
                if (firstUnreadIndex != -1) {
                    chatListState.scrollToItem(displayIndex)
                }
            }
        }
    }
}

@Composable
fun ProfileImageTeam(image: Bitmap?, sizeBox: Dp = 40.dp, sizeIcon: Dp = 24.dp) { // with the view model I can also add the image
    Box(
        modifier = Modifier
            .padding(end = 4.dp)
            .size(sizeBox)
            .clip(CircleShape)
            .background(color = PurpleGrey80),
        contentAlignment = Alignment.Center
    ) {
        if(image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription =null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Groups,
                contentDescription = "Group Icon",
                modifier = Modifier.size(sizeIcon)
            )
        }
    }
}

@Composable
fun TeamChatBox(
    modifier: Modifier,
    messages: MutableList<Message>,
    loggedInUser: String,
    teamChat: TeamChat
) {
    val chatBoxValue = remember { mutableStateOf("") }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") //remove ss
    val currentTime = LocalDateTime.now().format(formatter)

    Row(
        modifier = modifier
            .background(Color.White)
            .padding(start = 4.dp, top = 0.dp, end = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PurpleGrey80),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = chatBoxValue.value,
            onValueChange = { chatBoxValue.value = it },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = PurpleGrey80,
                unfocusedContainerColor = PurpleGrey80,
                disabledContainerColor = PurpleGrey80,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            placeholder = {
                Text(text = "Type something")
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        IconButton(
            onClick = {
                if(chatBoxValue.value.isNotBlank()) {
                    val newMessage = Message(text = chatBoxValue.value, author = loggedInUser, sentAt = currentTime)
                    teamChat.setNewLastAccess(currentTime)
                    DatabaseChats().updateTeamChatLastAccess(teamChat.chatId, currentTime)
                    teamChat.addMessage(newMessage)
                    messages.add(newMessage)
                    DatabaseChats().addMessagesToTeamChat(newMessage, teamChat.chatId)
                    chatBoxValue.value = ""
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send message")
        }
    }
}

@Composable
fun ChatGroupItem(message: Message, loggedInUser: String) {
    // Assign a color to each user based on their username
    val userColor = getUserColor((message.author))
    val profileState by DatabaseProfile().getProfileByIdWithoutPhoto(message.author).collectAsState(initial = null)



    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val maxWidth = (screenWidth * 3)/5
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = if (message.isFromMe(loggedInUser)) Arrangement.End else Arrangement.Start
        ) {
            if(!message.isFromMe(loggedInUser) && profileState != null) {
                Box(
                    modifier = Modifier.align(Alignment.Bottom)
                ) {
                    ProfileImageUser(message.author, true, textColor = userColor, isMessage = true)
                }
            }
            Box(
                modifier = Modifier
                    .widthIn(min = 40.dp, max = maxWidth)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isFromMe(loggedInUser)) 16.dp else 0.dp,
                            bottomEnd = if (message.isFromMe(loggedInUser)) 0.dp else 16.dp
                        )
                    )
                    .background(if (message.isFromMe(loggedInUser)) PurpleGrey80 else PurpleGrey40)
                    .padding(8.dp)
            ) {
                Column {
                    if(!message.isFromMe(loggedInUser) && profileState?.second!= null) {
                        Text(
                            text = profileState!!.second?.username!! /* DA MODIFICARE */,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                userColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            if(message.isFromMe(loggedInUser)) Color.Black else Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sent at ${message.sentAt}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            if(message.isFromMe(loggedInUser)) Color.DarkGray else Color.LightGray,
                            fontSize = 8.sp,
                        ),
                        modifier = Modifier.align(Alignment.End) // Aligning the text to the end (right side)
                    )
                }
            }
        }
    }
}

// Function to assign a color to each user based on their username
fun getUserColor(username: String): Color {
    // List of predefined colors for users
    val userColors = listOf(
        Color.LightGray,
        Color.Green,
        Color.Red,
        Color.Cyan,
        Color.Yellow,
        greenName,
        orangeName,
        fucsiaName,
        idkName
    )

    val hashCode = username.hashCode()
    val index = (hashCode + username.length) % userColors.size
    // Ensure index is non-negative
    val positiveIndex = if (index >= 0) index else index + userColors.size
    return userColors[positiveIndex]
}
package it.polito.madlab5.screens.chatScreen

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import it.polito.madlab5.database.DatabaseChats
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.model.Profile.Profile
import it.polito.madlab5.model.chat.Message
import it.polito.madlab5.model.chat.PersonChat
import it.polito.madlab5.screens.TeamScreen.LoadingState
import it.polito.madlab5.ui.theme.PurpleGrey40
import it.polito.madlab5.ui.theme.PurpleGrey80
import it.polito.madlab5.ui.theme.veryLightGrey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(navController: NavHostController, chat: PersonChat) {

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") //remove ss

    val chatState = remember {
        chat
    }
    var chatMessages = chatState.messagesState
    val messagesState by DatabaseChats().getPersonalMessages(chat.profileFrom, chat.profileTo).collectAsState(
        initial = mutableListOf<Message>().toMutableStateList()
    )
    LaunchedEffect(messagesState.size) {
        DatabaseChats().getPersonalMessages(chat.profileFrom, chat.profileTo).collect{
            chatState.setMessagesStateAfter(it)
            chatMessages = it.toMutableStateList()
        }

    }
    val profileTo by DatabaseProfile().getProfileByIdEdit(chat.profileTo).collectAsState(initial = Pair(LoadingState.EMPTY, null))
    Log.d("PersonChat", chatState.toString())
    // Calculate number of unread messages
    val unreadMessagesCount = messagesState.count {
        isFirstDateGreaterThanSecond(it.sentAt, chatState.lastAccess) && !it.isFromMe(loggedInUser = chatState.profileFrom)
    }

    val chatListState = rememberLazyListState()
    Log.d("MESSAGES", chatMessages.size.toString())

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            navigationIcon = {
                IconButton(onClick = {
                    chat.setChatLastAccess(LocalDateTime.now().format(formatter))
                    DatabaseChats().updatePersonalChatLastAccess(chat.chatId, LocalDateTime.now().format(formatter))
                    navController.popBackStack()
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            title = { profileTo.second?.username?.let { Text(it) }}, // Name of the user
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black
            ),
            actions = { //profile image
                ProfileImageUser(chatState.profileTo, false, isMessage = false)
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
                val firstUnreadIndex = messagesState.indexOfFirst { isFirstDateGreaterThanSecond(it.sentAt, chatState.lastAccess) }

                itemsIndexed(messagesState.reversed()) {index, message ->
                    val displayIndex = messagesState.size - 1 - index

                    if(displayIndex == firstUnreadIndex && unreadMessagesCount > 0) {
                        Column { 
                            UnreadMessagesBar(unreadCount = unreadMessagesCount)
                            Spacer(modifier = Modifier.height(8.dp))
                            ChatItem(message = message, chatState.profileFrom)
                        }
                    } else {
                        ChatItem(message = message, chatState.profileFrom)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            ChatBox(modifier = Modifier
                .fillMaxWidth()
                .constrainAs(chatBox) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                chatState,
                chatState.profileFrom,
                messagesState
            )

            LaunchedEffect(Unit) {
                val firstUnreadIndex = chatState.messagesState.indexOfFirst { isFirstDateGreaterThanSecond(it.sentAt, chatState.lastAccess) && !it.isFromMe(chatState.profileFrom) }
                val displayIndex = chatState.messagesState.size - 1 - firstUnreadIndex
                if (firstUnreadIndex != -1) {
                    chatListState.scrollToItem(displayIndex)
                }
            }
        }
    }
}

@Composable
fun ProfileImageUser(chatUser: String, groupChat: Boolean, textColor: Color = Color.Black, isMessage: Boolean) { // with the view model I can also add the image
    val profile = if (isMessage){
        DatabaseProfile().getProfileByIdWithoutPhoto(chatUser).collectAsState(initial = Pair(LoadingState.EMPTY, null))
    } else {
        DatabaseProfile().getProfileByIdEdit(chatUser).collectAsState(initial = Pair(LoadingState.EMPTY, null))
    }
    Box(
        modifier = Modifier
            .padding(end = 4.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(color = if (groupChat) PurpleGrey40 else PurpleGrey80),
        contentAlignment = Alignment.Center
    ) {
        if(profile.value.second?.image != null) {
            Log.d("ImageURL", profile.value.second?.imageURL!!)
            Log.d("Image", profile.value.second?.image!!.toString())
            Image(
                bitmap = profile.value.second?.image!!.asImageBitmap(),
                contentDescription =null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "${profile.value.second?.name?.first()?.uppercase()}${profile.value.second?.lastname?.first()?.uppercase()}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
            )
        }

    }
}

@Composable
fun ChatBox(
    modifier: Modifier,
    chat: PersonChat,
    loggedInUser: String,
    chatMessages: MutableList<Message>
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
                    chat.setChatLastAccess(currentTime)
                    chat.messages.add(newMessage)
                    DatabaseChats().updatePersonalChatLastAccess(chat.chatId, currentTime)
                    chatMessages.add(newMessage)
                    DatabaseChats().addPersonalMessage(newMessage, chat.chatId)
                    chatBoxValue.value = ""
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send message")
        }
    }
}

@Composable
fun ChatItem(message: Message, loggedInUser: String) {
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

@Composable
fun UnreadMessagesBar(unreadCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(color = veryLightGrey)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if(unreadCount > 1) "$unreadCount unread messages" else "$unreadCount unread message",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 12.sp,
                ),
                color = Color.Black
            )
        }
    }
}

fun isFirstDateGreaterThanSecond(date1Str: String, date2Str: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val date1 = LocalDateTime.parse(date1Str, formatter)
    val date2 = LocalDateTime.parse(date2Str, formatter)

    return date1.isAfter(date2)
}
package it.polito.madlab5.screens.TeamScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.lightspark.composeqr.DotShape
import com.lightspark.composeqr.QrCodeColors
import com.lightspark.composeqr.QrCodeView
import it.polito.madlab5.model.Team.Team
import it.polito.madlab5.ui.theme.Purple40

@Composable
fun InviteNewTeamMember(navController: NavHostController, team: Team) {

    ShowLinkAndQRcode(navController, url = team.link)
}

@Composable
fun ShowLinkAndQRcode(navController: NavHostController, url: String) {
    Surface(
        Modifier.fillMaxSize(),
        color = Purple40
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            IconButton(onClick = {navController.popBackStack()} ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(0.70f)
            ) {
                Text(
                    text = "Invite new members to the Team!",
                    textAlign = TextAlign.Center,
                    style =
                    MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                )
            }

            // QRcode
            ShowQrCode(url)

            // URL and copy button
            ShowUrl(url)
        }
    }
}

@Composable
fun ShowQrCode(url: String) {
    Row {
        QrCodeView(
            data = url,
            modifier = Modifier.size(200.dp),
            colors = QrCodeColors(
                background = Color.Black,
                foreground = Color.White
            ),
            dotShape = DotShape.Circle
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Filled.GroupAdd,
                    contentDescription = "Group Icon Add",
                    tint = Purple40,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun ShowUrl(url: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(0.70f)
    ) {
        Box(modifier = Modifier.weight(4f)) {
            Text(
                text = url,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }


        IconButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(url))
                Toast.makeText(context, clipboardManager.getText(), Toast.LENGTH_LONG).show()
            }
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy URL Icon",
                tint = Color.White
            )
        }
    }
}
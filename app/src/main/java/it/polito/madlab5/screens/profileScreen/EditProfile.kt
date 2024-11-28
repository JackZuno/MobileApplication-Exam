package it.polito.madlab5.screens.profileScreen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import it.polito.madlab5.alerts.LoadOrTakeProfileImageAlertProfile
import it.polito.madlab5.alerts.NoPermissionAlertProfile
import it.polito.madlab5.database.DatabaseProfile
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.ui.theme.Purple80
import it.polito.madlab5.ui.theme.backgroundBar
import it.polito.madlab5.ui.theme.lightGrayCustom
import it.polito.madlab5.viewModel.ProfileViewModels.ProfileViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CreateOrEditProfile(
    profileVm: ProfileViewModel = viewModel(),
    navController: NavHostController,
    backRoute: String,
    uid: String
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val dbProfile = DatabaseProfile()

    profileVm.validate()

    if(profileVm.showAllert){
        LoadOrTakeProfileImageAlertProfile(
            onDismiss = {
                profileVm.showAllert = false
                profileVm.showNoPermissionAllert = false
            },
            profileVm
        )
    }

    if(profileVm.showNoPermissionAllert){
        NoPermissionAlertProfile(
            onDismiss = {
                profileVm.showAllert = false
                profileVm.showNoPermissionAllert = false
        })
    }

    Column(  modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .weight(1f, false)
                .verticalScroll(scrollState)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Purple80)

            ) {
                if(profileVm.imageValue != null){
                    Image(
                        bitmap = profileVm.imageValue!!.asImageBitmap(),
                        contentDescription =null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    if(profileVm.nameValue.isNotBlank() && profileVm.lastNameValue.isNotBlank()) {
                        Text(

                            text = profileVm.nameValue.first().uppercase() + profileVm.lastNameValue.first().uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = backgroundBar
                        )
                    }
                }


                Button(colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .alpha(0.5f),
                    onClick = {
                        profileVm.showAllert = true
                    }) {
                }
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            // List of Profile parameters
            // Name

            OutlinedTextField(
                value = profileVm.nameValue,
                onValueChange = profileVm::set_Name,
                label = { Text(text = "Name") },
                isError = profileVm.nameError.isNotBlank(),
                supportingText = {
                    if (profileVm.nameError.isNotBlank())
                        Text(text = profileVm.nameError)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    focusedLeadingIconColor = Purple40,
                    focusedTrailingIconColor = Purple40,
                    cursorColor = Purple40,
                    focusedLabelColor = Purple40,

                )
            )

            // Last name
            OutlinedTextField(
                value = profileVm.lastNameValue,
                onValueChange = profileVm::setLastName,
                label = { Text(text = "Last Name") },
                isError = profileVm.lastNameError.isNotBlank(),
                supportingText = {
                    if (profileVm.lastNameError.isNotBlank())
                        Text(text = profileVm.lastNameError)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    focusedLeadingIconColor = Purple40,
                    focusedTrailingIconColor = Purple40,
                    cursorColor = Purple40,
                    focusedLabelColor = Purple40,

                    )
            )

            // Username
            OutlinedTextField(
                value = profileVm.nicknameValue,
                onValueChange = profileVm::setNickname,
                label = { Text(text = "Username") },
                isError = profileVm.nicknameError.isNotBlank(),
                supportingText = {
                    if (profileVm.nicknameError.isNotBlank())
                        Text(text = profileVm.nicknameError)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    focusedLeadingIconColor = Purple40,
                    focusedTrailingIconColor = Purple40,
                    cursorColor = Purple40,
                    focusedLabelColor = Purple40,

                    )
            )

            // email
            OutlinedTextField(
                value = profileVm.emailValue,
                onValueChange = profileVm::set_Email,
                label = { Text(text = "Email") },
                isError = profileVm.emailError.isNotBlank(),
                enabled = false,
                supportingText = {
                    if (profileVm.emailError.isNotBlank())
                        Text(text = profileVm.emailError)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    focusedLeadingIconColor = Purple40,
                    focusedTrailingIconColor = Purple40,
                    cursorColor = Purple40,
                    focusedLabelColor = Purple40,

                    )
            )

            // location
            OutlinedTextField(
                value = profileVm.locationValue,
                onValueChange = profileVm::set_Location,
                label = { Text(text = "Location") },
                isError = profileVm.locationError.isNotBlank(),
                supportingText = {
                    if (profileVm.locationError.isNotBlank())
                        Text(text = profileVm.locationError)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    focusedLeadingIconColor = Purple40,
                    focusedTrailingIconColor = Purple40,
                    cursorColor = Purple40,
                    focusedLabelColor = Purple40,

                    )
            )

            // Description
            OutlinedTextField(
                value = profileVm.descriptionValue,
                onValueChange = profileVm::set_Description,
                label = { Text(text = "Description") },
                isError = profileVm.descriptionError.isNotBlank(),
                supportingText = {
                    if (profileVm.descriptionError.isNotBlank())
                        Text(text = profileVm.descriptionError)
                },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    focusedLeadingIconColor = Purple40,
                    focusedTrailingIconColor = Purple40,
                    cursorColor = Purple40,
                    focusedLabelColor = Purple40,

                    )
            )

            //Skills
            OutlinedTextField(
                value = profileVm.currentSkillValue,
                onValueChange = profileVm::setCurrentSkill,
                label = { Text(text = "Skill") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple40,
                    focusedLeadingIconColor = Purple40,
                    focusedTrailingIconColor = Purple40,
                    cursorColor = Purple40,
                    focusedLabelColor = Purple40,

                    ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent { keyEvent: KeyEvent ->
                        if (keyEvent.key == Key.Enter) {
                            profileVm.addSkill(profileVm.currentSkillValue)
                            profileVm.setCurrentSkill("")
                            coroutineScope.launch { scrollState.animateScrollTo(scrollState.maxValue) }

                            return@onKeyEvent true
                        }
                        false
                    }
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),

                /*.horizontalScroll(
                    rememberScrollState()
                )*/
            ) {

                profileVm.skillsValue.forEachIndexed { index, item ->
                    AssistChip(
                        onClick = { },
                        label = { Text(text = item.trim(),
                            textAlign = TextAlign.Center,
                            style =
                            MaterialTheme.typography.bodySmall.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp)
                        )
                        },
                        trailingIcon = {
                            IconButton(onClick = { profileVm.removeSkill(index) }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = Color.Black )
                        }/*Icon(imageVector = Icons.Filled.Close, contentDescription = null)*/
                                       },
                        shape = RoundedCornerShape(20),
                        colors = AssistChipDefaults.assistChipColors(containerColor = lightGrayCustom),
                        border = AssistChipDefaults.assistChipBorder(borderColor = Color.LightGray),
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
            }


        }
        // Botton Done
        Button(onClick = {
            profileVm.editDone()
            profileVm.validate()
            navController.navigate(backRoute)    //ProfileScreen.ShowProfile.name
            val profile = hashMapOf(
                "name" to profileVm.nameValue,
                "lastname" to profileVm.lastNameValue,
                "username" to profileVm.nicknameValue,
                "email" to profileVm.emailValue,
                "description" to profileVm.descriptionValue,
                "imageURL" to profileVm.imageURLValue,
                "skills" to profileVm.skillsValue,
                "joineddate" to profileVm.joinedDateValue,
                "location" to profileVm.locationValue

            )
            dbProfile.db.collection("profiles").document(uid).set(profile) },

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .height(60.dp),
            enabled = profileVm.isValid,
            colors = ButtonDefaults.buttonColors(containerColor = Purple40))
           {
            Text(text = "Save",fontSize = 25.sp)

        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Preview
@Composable
fun EditProfileLandscape(vm: ProfileViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    if (vm.showAllert){
        LoadOrTakeProfileImageAlertProfile(
            onDismiss = {
                vm.showAllert = false
            },
            vm
        )
    }

    Row {
        Column(
            modifier = Modifier
                .weight(0.3f)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            // Mettere qui il codice per la vera immagine
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


                Button(colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    modifier = Modifier
                        .size(175.dp)
                        .clip(CircleShape)
                        .alpha(0.5f),
                    onClick = {
                        vm.showAllert= true
                    }) {
                }
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }


        Divider(
            color = Color.LightGray, modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )
        Column (modifier = Modifier.fillMaxWidth(0.7f)){
            Column(
                modifier = Modifier
                    .weight(1f, false)
                    .verticalScroll(scrollState)
                    .padding(top = 16.dp)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // List of Profile parameters
                // Name
                OutlinedTextField(
                    value = vm.nameValue,
                    onValueChange = vm::set_Name,
                    label = { Text(text = "Name") },
                    isError = vm.nameError.isNotBlank(),
                    supportingText = {
                        if (vm.nameError.isNotBlank())
                            Text(text = vm.nameError)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLeadingIconColor = Purple40,
                        focusedTrailingIconColor = Purple40,
                        cursorColor = Purple40,
                        focusedLabelColor = Purple40,

                        )
                )

                // Last name
                OutlinedTextField(
                    value = vm.lastNameValue,
                    onValueChange = vm::setLastName,
                    label = { Text(text = "Last Name") },
                    isError = vm.lastNameError.isNotBlank(),
                    supportingText = {
                        if (vm.lastNameError.isNotBlank())
                            Text(text = vm.lastNameError)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLeadingIconColor = Purple40,
                        focusedTrailingIconColor = Purple40,
                        cursorColor = Purple40,
                        focusedLabelColor = Purple40,

                        )

                )

                // Username
                OutlinedTextField(
                    value = vm.nicknameValue,
                    onValueChange = vm::setNickname,
                    label = { Text(text = "Username") },
                    isError = vm.nicknameError.isNotBlank(),
                    supportingText = {
                        if (vm.nicknameError.isNotBlank())
                            Text(text = vm.nicknameError)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLeadingIconColor = Purple40,
                        focusedTrailingIconColor = Purple40,
                        cursorColor = Purple40,
                        focusedLabelColor = Purple40,

                        )
                )

                // email
                OutlinedTextField(
                    value = vm.emailValue,
                    onValueChange = vm::set_Email,
                    enabled = false,
                    label = { Text(text = "Email") },
                    isError = vm.emailError.isNotBlank(),
                    supportingText = {
                        if (vm.emailError.isNotBlank())
                            Text(text = vm.emailError)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLeadingIconColor = Purple40,
                        focusedTrailingIconColor = Purple40,
                        cursorColor = Purple40,
                        focusedLabelColor = Purple40,

                        )
                )

                // location
                OutlinedTextField(
                    value = vm.locationValue,
                    onValueChange = vm::set_Location,
                    label = { Text(text = "Location") },
                    isError = vm.locationError.isNotBlank(),
                    supportingText = {
                        if (vm.locationError.isNotBlank())
                            Text(text = vm.locationError)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLeadingIconColor = Purple40,
                        focusedTrailingIconColor = Purple40,
                        cursorColor = Purple40,
                        focusedLabelColor = Purple40,

                        )
                )

                // Description
                OutlinedTextField(
                    value = vm.descriptionValue,
                    onValueChange = vm::set_Description,
                    label = { Text(text = "Description") },
                    isError = vm.descriptionError.isNotBlank(),
                    supportingText = {
                        if (vm.descriptionError.isNotBlank())
                            Text(text = vm.descriptionError)
                    },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLeadingIconColor = Purple40,
                        focusedTrailingIconColor = Purple40,
                        cursorColor = Purple40,
                        focusedLabelColor = Purple40,

                        )
                )


                OutlinedTextField(
                    value = vm.currentSkillValue,
                    onValueChange = vm::setCurrentSkill,
                    label = { Text(text = "Skill") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Purple40,
                        focusedLeadingIconColor = Purple40,
                        focusedTrailingIconColor = Purple40,
                        cursorColor = Purple40,
                        focusedLabelColor = Purple40,

                        ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onKeyEvent { keyEvent: KeyEvent ->
                            if (keyEvent.key == Key.Enter) {
                                vm.addSkill(vm.currentSkillValue)
                                coroutineScope.launch { scrollState.animateScrollTo(scrollState.maxValue) }

                                vm.setCurrentSkill("")
                                return@onKeyEvent true
                            }
                            false
                        }
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vm.skillsValue.forEachIndexed { index, item ->
                        AssistChip(
                            onClick = { },
                            label = { Text(text = item.trim(),
                                textAlign = TextAlign.Center,
                                style =
                                MaterialTheme.typography.bodySmall.copy(
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp)
                            )
                            },
                            trailingIcon = {
                                IconButton(onClick = { vm.removeSkill(index) }) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = Color.Black )
                                }/*Icon(imageVector = Icons.Filled.Close, contentDescription = null)*/
                            },
                            shape = RoundedCornerShape(20),
                            colors = AssistChipDefaults.assistChipColors(containerColor = lightGrayCustom),
                            border = AssistChipDefaults.assistChipBorder(borderColor = Color.LightGray),
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }


            }
            Button(onClick = {
                vm.editDone()
                vm.validate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                enabled = vm.isValid,
                colors = ButtonDefaults.buttonColors(containerColor = Purple40)) {
                Text(text = "Save", fontSize = 25.sp)
            }
        }

    }
}

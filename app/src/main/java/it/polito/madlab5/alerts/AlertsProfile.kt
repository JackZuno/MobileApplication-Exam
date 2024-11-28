package it.polito.madlab5.alerts

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.storage.FirebaseStorage
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.viewModel.ProfileViewModels.ProfileViewModel
import it.polito.madlab5.viewModel.TeamViewModels.TeamViewModel
import java.io.ByteArrayOutputStream

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LoadOrTakeProfileImageAlertProfile(
    onDismiss: () -> Unit,
    profileVM: ProfileViewModel
){
    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val openCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ){
        if (it != null) {
            manageImageUploadProfile(profileVM, it, context)
        }
        profileVM.showAllert = false
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
            openCamera.launch()

        } else {
            // Handle permission denial
            profileVM.showNoPermissionAllert = true

        }
    }

    var galleryPermissionState = rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
        galleryPermissionState = rememberPermissionState (Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val openGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ){uri: Uri? ->
        uri?.let {
            handleGalleryImageProfile(context, it, profileVM)
        }
    }

    val requestGalleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
            openGallery.launch("image/*")
        } else {
            profileVM.showNoPermissionAllert = true

        }
    }

    AlertDialog(onDismissRequest = onDismiss, confirmButton = {  },
        modifier = Modifier.height(300.dp),
        title ={
            Row (verticalAlignment = Alignment.CenterVertically)    {
                Icon(imageVector = Icons.Filled.Info, contentDescription = null)

            }
        },
        text = {
            Column (modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){

                Button(onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        openCamera.launch()
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                    modifier = Modifier
                        .width(180.dp)
                        .padding(6.dp)) {
                    Text(text = "Camera")
                }
                Button(onClick = {
                    if (galleryPermissionState.status.isGranted) {
                        openGallery.launch("image/*")
                    } else {
                        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                            requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }else{
                            requestGalleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                    modifier = Modifier
                        .width(180.dp)
                        .padding(6.dp)) {
                    Text(text = "Gallery")
                }
                Button(onClick = {
                    // Make the remove from the storage of the previous image
                    if(profileVM.imageURLValue != null) {
                        val storage = FirebaseStorage.getInstance()
                        val imageRef = storage.getReferenceFromUrl(profileVM.imageURLValue!!)
                        deleteImageFromTheStorage(imageRef)
                    }
                    profileVM.setImageURLFromStorage(null)
                    profileVM.setImageBitmap(null)
                    onDismiss()
                }, colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                    modifier = Modifier
                        .width(180.dp)
                        .padding(6.dp)) {
                    Text(text = "Remove Photo")
                }
            }
        }
    )
}

@Composable
fun NoPermissionAlertProfile(onDismiss: ()->Unit){  //, vm: ProfileViewModel
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismiss ,
        confirmButton = {  },
        modifier = Modifier.height(250.dp),
        title ={
            Row (verticalAlignment = Alignment.CenterVertically)    {

                Icon(imageVector = Icons.Filled.Error, contentDescription = null)
                Text(text = "Oops!", modifier = Modifier.padding(start = 5.dp))
            }
        },
        text = {
            Column (modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){

                Text(
                    text = "To continue enable Camera/Gallery permission in Settings",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    openAppSettings(context)
                }, colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                    modifier = Modifier
                        .width(180.dp)
                        .padding(10.dp)) {
                    Text(text = "Settings")
                }

            }
        }
    )
}

@SuppressLint("CheckResult")
fun manageImageUploadProfile(profileVM: ProfileViewModel, b: Bitmap, context: Context) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference.child("profile_images/${System.currentTimeMillis()}.jpg") // Generate unique filename with timestamp

    // Make the remove from the storage of the previous image
    if(profileVM.imageURLValue != null) {
        val imageRef = storage.getReferenceFromUrl(profileVM.imageURLValue!!)
        deleteImageFromTheStorage(imageRef)
    }

    val baos = ByteArrayOutputStream()
    val quality = 75
    b.compress(Bitmap.CompressFormat.JPEG, quality, baos)
    val data = baos.toByteArray()

    val uploadTask = storageRef.putBytes(data)

    uploadTask.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()

                profileVM.setImageURLFromStorage(downloadUrl)
                profileVM.setImageBitmap(b)

                Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Image upload failed
            val exception = task.exception
            Log.e("EXCEPTION", "manageImageUpload: $exception")
            Toast.makeText(context, "$exception", Toast.LENGTH_SHORT).show()
        }
    }
}

fun handleGalleryImageProfile(context: Context, uri: Uri, profileVM: ProfileViewModel) {
    val bitmap = uriToBitmap(context, uri)

    bitmap?.let {b ->
        try {
            val resizedBitmap = resizeBitmap(b, maxWidth = 1024, maxHeight = 1024)
            manageImageUploadProfile(profileVM, resizedBitmap, context)
            profileVM.showAllert = false
        } catch (e: RuntimeException) {
            // If there's an exception (e.g., bitmap is too large), resize and try again
            val resizedBitmap = resizeBitmap(b, maxWidth = 1024, maxHeight = 1024)
            manageImageUploadProfile(profileVM, resizedBitmap, context)
            profileVM.showAllert = false
        }
    }
}
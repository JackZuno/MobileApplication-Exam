package it.polito.madlab5.alerts

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import com.bumptech.glide.Glide
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.madlab5.ui.theme.Purple40
import it.polito.madlab5.viewModel.ProfileViewModels.ProfileViewModel
import it.polito.madlab5.viewModel.TeamViewModels.TeamViewModel
import java.io.ByteArrayOutputStream


@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LoadOrTakeTeamImageAlert(tvm: TeamViewModel, showAlert: MutableState<Boolean>, showNoPermissionAllert: MutableState<Boolean>, onDismiss: ()->Unit){  //, vm: ProfileViewModel
    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val openCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ){
        if (it != null) {
            manageImageUpload(tvm, it, context)
        }
        showAlert.value = false
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
            openCamera.launch()
        } else {
            showNoPermissionAllert.value = true
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
            handleGalleryImage(context, uri, tvm, showAlert)
        }
    }
    val requestGalleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
            openGallery.launch("image/*")
        } else {
            showNoPermissionAllert.value = true
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
                    if(tvm.imageURLValue != null) {
                        val storage = FirebaseStorage.getInstance()
                        val imageRef = storage.getReferenceFromUrl(tvm.imageURLValue!!)
                        deleteImageFromTheStorage(imageRef)
                    }
                    tvm.setImageURLFromStorage(null)
                    tvm.setImageBitMap(null)
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

fun handleGalleryImage(context: Context, uri: Uri, tvm: TeamViewModel, showAlert: MutableState<Boolean>) {
    val bitmap = uriToBitmap(context, uri)

    bitmap?.let {b ->
        try {
            val resizedBitmap = resizeBitmap(b, maxWidth = 1024, maxHeight = 1024)
            manageImageUpload(tvm, resizedBitmap, context)
            showAlert.value = false
        } catch (e: RuntimeException) {
            // If there's an exception (e.g., bitmap is too large), resize and try again
            val resizedBitmap = resizeBitmap(b, maxWidth = 1024, maxHeight = 1024)
            manageImageUpload(tvm, resizedBitmap, context)
            showAlert.value = false
        }
    }
}

@SuppressLint("CheckResult")
fun manageImageUpload(tvm: TeamViewModel, b: Bitmap, context: Context) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference.child("team_images/${System.currentTimeMillis()}.jpg") // Generate unique filename with timestamp

    // Make the remove from the storage of the previous image
    if(tvm.imageURLValue != null) {
        val imageRef = storage.getReferenceFromUrl(tvm.imageURLValue!!)
        deleteImageFromTheStorage(imageRef)
    }

    val baos = ByteArrayOutputStream()
    val quality = 75
    b.compress(Bitmap.CompressFormat.JPEG, quality, baos)
    val data = baos.toByteArray()

    val uploadTask = storageRef.putBytes(data)

    uploadTask
        .addOnCompleteListener { task ->
            //Toast.makeText(context, "Uploading Image...", Toast.LENGTH_SHORT).show()
            if (task.isSuccessful) {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    tvm.setImageURLFromStorage(downloadUrl)
                    tvm.setImageBitMap(b)

                    // Use the downloadUrl with Glide to load and display the image
                    //Glide.with(context)
                      //  .load(downloadUrl)
                        //.error(R.drawable.img) // Set an error image if loading fails
                        //.into(binding.image)

                    Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Image upload failed
                val exception = task.exception
                Log.e("EXCEPTION", "manageImageUpload: $exception")
                Toast.makeText(context, "$exception", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            Log.d("TAG", "Upload is $progress% done")
        }
}

fun deleteImageFromTheStorage(storageRef: StorageReference) {
    storageRef.delete().addOnSuccessListener {
        // Previous image deleted successfully
        Log.d("TAG", "Previous image deleted")
    }.addOnFailureListener {
        // Handle deletion failure (optional)
        Log.w("TAG", "Failed to delete previous image", it)
    }
}

fun resizeBitmap(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    var width = source.width
    var height = source.height
    if (width > maxWidth || height > maxHeight) {
        val aspectRatio: Float = width.toFloat() / height.toFloat()
        if (aspectRatio > 1) {
            width = maxWidth
            height = (width / aspectRatio).toInt()
        } else {
            height = maxHeight
            width = (height * aspectRatio).toInt()
        }
    }
    return Bitmap.createScaledBitmap(source, width, height, true)
}

@Composable
fun NoPermissionAlert(onDismiss: ()->Unit){ //, vm: ProfileViewModel
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
                Spacer(modifier =Modifier.weight(1f))
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
fun openAppSettings(context: Context) {
    val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options, 1024, 1024)

        options.inJustDecodeBounds = false
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

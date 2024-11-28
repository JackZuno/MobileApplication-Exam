package it.polito.madlab5

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.madlab5.ui.theme.Lab4Theme
import it.polito.madlab5.ui.theme.overDueBg

class LoginActivity : ComponentActivity() {

    private companion object LoginActivity {
        private const val TAG = "LoginActivity"
    }

    private lateinit var auth: FirebaseAuth
    private val isConnected = mutableStateOf(true)
    private lateinit var googleSignInClient: GoogleSignInClient

    private val isLoading = mutableStateOf(true)

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("971470859327-mqa0vmacrt8lru2gm5tcla29k6vl04a5.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val lastAccount = GoogleSignIn.getLastSignedInAccount(this)
        Log.w("LOGIN", lastAccount.toString())
        if(lastAccount != null){
            try {
                // Google Sign In was successful, authenticate with Firebase
                //val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + lastAccount.id)
                firebaseAuthWithGoogle(lastAccount.idToken!!)
            } catch (e: ApiException) {
                isConnected.value = false
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }else{
            isLoading.value = false
        }



        setContent{
            Lab4Theme(darkTheme = false) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoading.value) {
                        LoadingScreen()
                    } else {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "Welcome back", fontSize = 44.sp)
                            }
                            /*if (!isConnected.value) {
                                Row(
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Something went wrong ...",
                                        fontSize = 28.sp,
                                        color = overDueBg
                                    )
                                }
                            }*/

                            Button(onClick = {
                                handleSingIn.launch(googleSignInClient.signInIntent)
                            }) {
                                Text(text = "Login", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun updateUI(user: FirebaseUser?) {
        if (user == null) {
            isConnected.value = false
            Log.w(TAG, "user not signed in..")
            isLoading.value = false
            return
        }

        val mainAct = Intent(this, MainActivity::class.java)
        mainAct.putExtra("email", user.email)
        mainAct.putExtra("uid", user.uid)
        mainAct.apply { data = intent.data }
        Log.d("INTENT LOGIN", mainAct.data.toString())
        startActivity(mainAct)
        finish()
        // Navigate to MainActivity
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val handleSingIn = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                isConnected.value = false
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }
}
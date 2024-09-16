package com.giovdellap.onsitehelper.signin

import android.content.Context
import android.content.Intent
import android.credentials.GetCredentialException
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CustomCredential
import androidx.lifecycle.lifecycleScope
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.projects.ProjectsActivity
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID
import androidx.credentials.CredentialManager as CredentialManager

class SignInActivity : AppCompatActivity() {

    var auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Generate a nonce (a random number used once)
    val ranNonce: String = UUID.randomUUID().toString()
    val bytes: ByteArray = ranNonce.toByteArray()
    val md: MessageDigest = MessageDigest.getInstance("SHA-256")
    val digest: ByteArray = md.digest(bytes)
    val hashedNonce: String = digest.fold("") { str, it -> str + "%02x".format(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d("TAG", "Initialization completed")

        val sharedPreferences = this.getSharedPreferences("OnSiteHelper", Context.MODE_PRIVATE)
        val user = sharedPreferences.getString("email", "")
        if (user != null && user != "") {
            val intent = Intent(this, ProjectsActivity::class.java)
            startActivity(intent)
        }

        val credentialManager: CredentialManager = CredentialManager.create(this)
        Log.d("TAG", "create credential manager")

        findViewById<View>(R.id.google_sign_in_button).setOnClickListener {
            val googleIdOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder("626011010926-jhl0pjflckdpctmabcjmia2q8qprq1n6.apps.googleusercontent.com")
                //.setNonce(<nonce string to use when generating a Google ID token>)
                .setNonce(hashedNonce)
                .build()
            Log.d("TAG", "Google ID Option created")

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            Log.d("TAG", "Request created")

            val context = this.applicationContext
            lifecycleScope.launch {
                Log.d("TAG", "Inside launch")
                try {
                    Log.d("TAG", "Inside try")
                    val result = credentialManager.getCredential(
                        // Use an activity-based context to avoid undefined system UI
                        // launching behavior.
                        context = context,
                        request = request
                    )
                    handleSignIn(result)
                } catch (e : GetCredentialException) {
                    Log.d("TAG", "Launch catch")
                }
            }
        }
    }

    fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        Log.d("TAG", "inside handle sing in")

        val credential = result.credential
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d("AO", googleIdTokenCredential.idToken)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                Log.d("TAG", firebaseCredential.toString())
                Log.d("TAG", firebaseCredential.signInMethod)

                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success")
                            val user = auth.currentUser
                            if(user != null) {
                                val email = user.getEmail()
                                if (email != null) {
                                    val sharedPreferences = this.getSharedPreferences("OnSiteHelper", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().putString("uid", user.uid).apply()
                                    sharedPreferences.edit().putString("email", email).apply()

                                    //val ao = sharedPreferences.getString("email", "")

                                    val intent = Intent(this, ProjectsActivity::class.java)
                                    startActivity(intent)
                                }
                                Log.d("TAG", user.uid)

                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.exception)
                        }
                    }
                        // Send googleIdTokenCredential to your server for validation and authentication
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("TAG", "Received an invalid google id token response", e)
            }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e("TAG", "Unexpected type of credential")
                }

    }
}

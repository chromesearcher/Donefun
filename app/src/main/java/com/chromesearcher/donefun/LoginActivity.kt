package com.chromesearcher.donefun

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import io.fabric.sdk.android.Fabric


import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private val DEFAULT_BOARD_ID: String = "main"
    private val DEFAULT_PREV_BOARD_ID: String = "login"

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val TAG: String = "myLogs"
    private val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        Fabric.with(this, Crashlytics.Builder().core(core).build())
        setContentView(R.layout.activity_login)


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // TODO: insert from GCP concole??
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        val signinButton = findViewById<Button>(R.id.sign_in_button) as SignInButton
        // Set the dimensions of the sign-in button.
        signinButton.setSize(SignInButton.SIZE_STANDARD)
        signinButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onStart() {
        super.onStart()

//        // Check for existing Google Sign In account, if the user is already signed in
//        // the GoogleSignInAccount will be non-null.
//        val account = GoogleSignIn.getLastSignedInAccount(this)
//
//        if (account != null) {
//            // TODO: go to board activity with account info
//            updateUI(account)
//        } else {
//            // TODO: hanlde
//        }

        val curUser = auth.currentUser
        updateUI(curUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult (completedTask: Task<GoogleSignInAccount>) {

        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)


            // Signed in successfully, show authenticated UI.
//            updateUI(account)
        } catch (ex: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + ex.statusCode)
//            updateUI(null)
        }
    }

    private fun firebaseAuthWithGoogle(acc: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acc.id!!)

        val credential = GoogleAuthProvider.getCredential(acc.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
//                    Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {


        if (user == null) {
            // TODO: handle
            Toast.makeText(this, "auth fail, user is null", Toast.LENGTH_SHORT).show()
            return
        }
//        user.uid
//        auth.uid


        
        Toast.makeText(applicationContext, user!!.uid, Toast.LENGTH_SHORT).show()
//        Toast.makeText(applicationContext, user!!.displayName, Toast.LENGTH_SHORT).show()
//        Toast.makeText(applicationContext, user!!.getIdToken(true).toString(), Toast.LENGTH_SHORT).show()


        // TODO: go to board screen
        // invoke BoardActivity
        var newIntent = Intent(this, BoardActivity::class.java)
        newIntent.putExtra("user", user!!.uid)
        startActivity(newIntent)
    }

//    private fun updateUI(account: GoogleSignInAccount?) {
//        // TODO: go to board screen
//        // invoke BoardActivity
//        var newIntent = Intent(this, BoardActivity::class.java)
//        newIntent.putExtra("board", DEFAULT_BOARD_ID)
//        startActivity(newIntent)
//    }
}
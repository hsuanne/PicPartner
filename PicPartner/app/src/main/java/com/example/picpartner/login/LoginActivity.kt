package com.example.picpartner.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.picpartner.MainActivity
import com.example.picpartner.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*


class LoginActivity : AppCompatActivity() {
    private val transY = 300f
    private val alpha = 0f
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private val RC_SIGN_IN = 1
    private val TAG = "LoginActivity"
    private lateinit var auth:FirebaseAuth

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser!=null){
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tabLayout: TabLayout = findViewById(R.id.login_tab)
        val viewPager2: ViewPager2 = findViewById(R.id.login_viewpager)
        val fb: FloatingActionButton = findViewById(R.id.fab_fb)
        val google: FloatingActionButton = findViewById(R.id.fab_google)
        val guest: FloatingActionButton = findViewById(R.id.fab_guest)
        val progressBar:ProgressBar = findViewById(R.id.login_progressBar)

        val loginAdapter: LoginAdapter = LoginAdapter(supportFragmentManager, lifecycle)
        viewPager2.adapter = loginAdapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = resources.getString(R.string.login)
                }
                1 -> {
                    tab.text = resources.getString(R.string.register)
                }
            }
        }.attach()

        fb.translationY = transY
        google.translationY = transY
        guest.translationY = transY
        tabLayout.translationX = transY

        fb.alpha = alpha
        google.alpha = alpha
        guest.alpha = alpha
        tabLayout.alpha = alpha

        fb.animate().translationY(0f).alpha(1f).setDuration(1000).setStartDelay(400).start()
        google.animate().translationY(0f).alpha(1f).setDuration(1000).setStartDelay(600).start()
        guest.animate().translationY(0f).alpha(1f).setDuration(1000).setStartDelay(800).start()
        tabLayout.animate().translationX(0f).alpha(1f).setDuration(1000).setStartDelay(100).start()

        // initialize firebase authentication
        auth = FirebaseAuth.getInstance()

        // establish google configuration
        createRequest()
        google.setOnClickListener {
            signIn()
        }

        // facebook login
        fb.setOnClickListener {
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult?> {
                    override fun onSuccess(loginResult: LoginResult?) {
                        handleFacebookAccessToken(loginResult!!.accessToken)
                    }

                    override fun onCancel() {
                        // App code
                    }

                    override fun onError(exception: FacebookException) {
                        // App code
                    }
                })
        }

        // guest login
        guest.setOnClickListener {
            auth.signInAnonymously().addOnCompleteListener {
                progressBar.visibility = View.VISIBLE
                if (it.isSuccessful){
                    Toast.makeText(baseContext, "Guest Authentication Success.",
                        Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(this, "guest login failed:" + it.exception, Toast.LENGTH_SHORT).show()
                }
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun createRequest() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn(){
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Toast.makeText(this, "firebaseAuthWithGoogle:" + account.id, Toast.LENGTH_SHORT).show()
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed:" + e.message, Toast.LENGTH_SHORT).show()
            }
        } else if (callbackManager!=null) {
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Toast.makeText(baseContext, "FaceBook Authentication Success.",
                        Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "FaceBook Authentication failed:" + task.exception,
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Google Login Success" , Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "signInWithCredential:failure:" + task.exception, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
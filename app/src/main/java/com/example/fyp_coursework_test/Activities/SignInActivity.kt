package com.example.fyp_coursework_test.Activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class SignInActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var messaging: FirebaseMessaging

    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInRequestCode = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {


            // Link the Activity to the XML Layout
            binding = ActivitySignInBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setAnimations()

            // Initialise Firebase Tools
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")
            isSignedIn()

            buttonOnClickListeners()

        }
    }

    private fun isSignedIn() {
        if (auth.currentUser!=null) {
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Set all the onClickListeners for the buttons
    private fun buttonOnClickListeners() {

        // Sign in with email if fields are valid
        binding.signInButton.setOnClickListener {
            if (checkEntryFields()) signInEmail()
        }

        // Sign in with Google
        binding.googleSignInButton.setOnClickListener {
            setupGoogleSignInClient()
            googleSignInClient.signOut()
            signInGoogle()
        }

        // Navigate to Sign Up Activity
        binding.createAccountButton.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // Ensure entry field inputs are valid, returns boolean
    private fun checkEntryFields(): Boolean {
        val email = binding.emailInputEditText.text.toString().trim()
        val password = binding.passwordInputEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@SignInActivity, "Please fill in all fields", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this@SignInActivity, "Please enter a valid email address", Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    // set up the Google google sign in client
    private fun setupGoogleSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    // Open Google's account authentication menu
    private fun signInGoogle() {
        val intent = googleSignInClient.signInIntent
        startActivityForResult(intent, googleSignInRequestCode)
    }

    // Check sign in details to sign in with Firebase
    private fun signInEmail() {

        // Attempt Firebase Authentication with email/password
        auth.signInWithEmailAndPassword(
            binding.emailInputEditText.text.toString(), binding.passwordInputEditText.text.toString())
            .addOnCompleteListener { task ->

                // If successful, sign in and go to MainActivity
                if (task.isSuccessful) {
                    Toast.makeText(this@SignInActivity, "Signed In", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@SignInActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    Toast.makeText(
                        this@SignInActivity,
                        task.exception?.message?: "Unknown error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Override existing function to check result of external Activities we implemented
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            // Result of Google Sign In Activity
            googleSignInRequestCode -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)

                    // Get resulting google account and try to Firebase sign in
                    firebaseAuthWithGoogle(account)

                }
                catch (e: ApiException) {
                    Toast.makeText(this@SignInActivity,
                        "Failed Google Sign In",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Google Sign In with Firebase Authentication
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        // Attempt Firebase Authentication with Google credential
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                // If successful, add Google User to Firebase Realtime Database
                if (task.isSuccessful) {
                    val googleUser = auth.currentUser
                    val userRef = database.reference.child("Users").child(googleUser!!.uid)

                    // Check if user data already exists in the database
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            // Does NOT exist
                            if (!snapshot.exists()) {

                                // Navigate to page 2 of the sign-up page
                                val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
                                // Pass extras to indicate to SignUpActivity to start from page 2
                                intent.putExtra("isGoogleSignUp", true)
                                intent.putExtra("googleAccount", account)
                                startActivity(intent)
                            }
                            // User DOES exist
                            else {
                                // Sign in normally and navigate to the main activity
                                Toast.makeText(this@SignInActivity,
                                    "Signed In",
                                    Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@SignInActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                        // Handle database failure
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@SignInActivity,
                                "Failed: ${error.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                // Handle authentication failure
                else {
                    Toast.makeText(this@SignInActivity,
                        task.exception?.message ?: "Unknown error",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Handles the initial animations of the page
    private fun setAnimations() {
        val topAnim = AnimationUtils.loadAnimation(this, R.anim.fade_down_anim)
        val bottomAnim = AnimationUtils.loadAnimation(this, R.anim.fade_up_anim)

        binding.siLogoLayout.animation = topAnim
        binding.welcomeText.animation = topAnim
        binding.signInLayout.animation = bottomAnim
    }

}

package com.example.fyp_coursework_test.Activities

import android.app.Activity
import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_coursework_test.Models.UserModel
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.Utility.CryptoUtil
import com.example.fyp_coursework_test.databinding.ActivitySignUpBinding
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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var storage : FirebaseStorage

    private val pickImageRequestCode = 1
    private var profileImageUri: Uri? = null
    private var profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/fyp-coursework-test.appspot.com/o/profile_images%2Fdefault_image%2Fprofile-circle-svgrepo-com.svg?alt=media&token=f9ec9de9-5579-4151-b1b3-118d0f1518fc"

    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInRequestCode = 2
    private var account: GoogleSignInAccount? = null
    private var isGoogleSignUp = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Link the Activity to the XML Layout
            binding = ActivitySignUpBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setAnimations()

            // Initialise Firebase Tools
            auth = FirebaseAuth.getInstance()
            storage = FirebaseStorage.getInstance()
            database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

            // If user attempted new Google sign in, switch to page 2
            isGoogleSignUp = intent.getBooleanExtra("isGoogleSignUp", false)
            if (isGoogleSignUp) {
                account = intent.getParcelableExtra<GoogleSignInAccount>("googleAccount")!!
                setPage2()
            }
            else setPage1()

            buttonOnClickListeners()
        }
    }

    // Set all the onClickListeners for the buttons
    private fun buttonOnClickListeners() {

        // Switch to page 2
        binding.nextPageButton1.setOnClickListener{
            if (checkPage1()) setPage2()
        }

        // Switch back to page 1
        binding.prevPageButton.setOnClickListener{
            isGoogleSignUp = false
            account = null
            setPage1()
        }

        // Open image picker on page 2
        binding.suAddProfileImageButton.setOnClickListener{
            openImagePicker()
        }

        // Complete and create account
        binding.finishRegistrationButton.setOnClickListener {
            createAccount()
        }

        // GOOGLE Sign In Button Functionality //
        binding.googleSignUpButton.setOnClickListener {
            setupGoogleSignInClient()
            googleSignInClient.signOut()
            signInGoogle()
        }

        // Navigate back to Sign In Activity
        binding.backToSignInButton.setOnClickListener {
            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun setPage1() {
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
        binding.signUpPage2.startAnimation(slideOut)
        binding.signUpPage2.visibility = View.GONE

        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        binding.signUpPage1.startAnimation(slideIn)
        binding.signUpPage1.visibility = View.VISIBLE
    }

    private fun setPage2() {
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left)
        binding.signUpPage1.startAnimation(slideOut)
        binding.signUpPage1.visibility = View.GONE

        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        binding.signUpPage2.startAnimation(slideIn)
        binding.signUpPage2.visibility = View.VISIBLE

        if (isGoogleSignUp) {
            val profilePicUrl = account!!.photoUrl?.toString()
            profilePicUrl?.let {
                Picasso.get()
                    .load(profilePicUrl)
                    .placeholder(R.drawable.profile_circle_svgrepo_com)
                    .into(binding.suProfileImage)
            }
        }
        else {
            binding.suProfileImage.setImageResource(R.drawable.ic_baseline_account_circle_24_offwhite)
        }
    }


    private fun checkPage1(): Boolean {
        val email = binding.suEmailInputEditText.text.toString().trim()
        val password = binding.suPasswordInputEditText.text.toString().trim()
        val confirmPassword = binding.suConfirmPasswordInputEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this@SignUpActivity, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this@SignUpActivity, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this@SignUpActivity, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun checkPage2(): Boolean {
        val username = binding.suUsernameInputEditText.text.toString().trim()
        val firstname = binding.suFirstnameInputEditText.text.toString().trim()
        val lastname = binding.suLastnameInputEditText.text.toString().trim()

        if (username.isEmpty() || firstname.isEmpty() || lastname.isEmpty()) {
            Toast.makeText(this@SignUpActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun createAccount() {

        if (isGoogleSignUp) {
            if (checkPage2()) {
                firebaseAuthWithGoogle(account!!)
            }
        }

        else {
            if (checkPage1() && checkPage2()) {
                firebaseAuthWithEmail()
            }
        }
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

    // Google Sign In with Firebase Authentication and save details to Realtime Database
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val username = binding.suUsernameInputEditText.text.toString().trim()
        val firstname = binding.suFirstnameInputEditText.text.toString().trim()
        val lastname = binding.suLastnameInputEditText.text.toString().trim()

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

                                // Generate public and private encryption keys
                                val publicKey = CryptoUtil.generateKeyPairAndStoreInKeystore(googleUser.uid)
                                val publicKeyString = CryptoUtil.encodeKeyToString(publicKey.encoded)

                                // save details to database
                                val user = UserModel(
                                    username = username,
                                    email = googleUser.email ?: "",
                                    firstname =  firstname,
                                    lastname = lastname,
                                    profile_Pic = googleUser.photoUrl.toString(),
                                    user_ID = googleUser.uid,
                                    publicKey = publicKeyString
                                )
                                userRef.setValue(user)

                                Toast.makeText(this@SignUpActivity,
                                    "Account Created Successfully",
                                    Toast.LENGTH_SHORT).show()

                            }
                            // User DOES exist
                            else {
                                Toast.makeText(this@SignUpActivity,
                                    "Signed In",
                                    Toast.LENGTH_SHORT).show()
                            }

                            // Navigate to the Main Activity
                            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                        // Handle database failure
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@SignUpActivity,
                                "Failed: ${error.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                // Handle authentication failure
                else {
                    Toast.makeText(this@SignUpActivity,
                        task.exception?.message ?: "Unknown error",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Check details and save to Auth, Database and Storage
    private fun firebaseAuthWithEmail() {
        val progressDialog = ProgressDialog(this@SignUpActivity)
        progressDialog.setMessage("Signing up...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val username = binding.suUsernameInputEditText.text.toString().trim()
        val email = binding.suEmailInputEditText.text.toString().trim()
        val firstname = binding.suFirstnameInputEditText.text.toString().trim()
        val lastname = binding.suLastnameInputEditText.text.toString().trim()
        val password = binding.suPasswordInputEditText.text.toString().trim()

        // Attempt Firebase Authentication with email/password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                // If successful, try save profile image to Storage, then save details to Database.
                if (task.isSuccessful) {
                    val emailUser = auth.currentUser
                    val user_ID: String = emailUser!!.uid

                    // Generate public and private encryption keys
                    val publicKey = CryptoUtil.generateKeyPairAndStoreInKeystore(user_ID)
                    val publicKeyString = CryptoUtil.encodeKeyToString(publicKey.encoded)

                    // If user picks a valid image, attempt upload to Storage
                    if (profileImageUri != null) {
                        val profileImageRef = storage.reference
                            .child("profile_images/$user_ID/${profileImageUri?.lastPathSegment}")

                        val uploadTask = profileImageUri?.let { profileImageRef.putFile(it) }

                        uploadTask?.continueWithTask { task ->

                            // If upload fails, display message
                            if (!task.isSuccessful) {
                                throw task.exception ?: Exception("Image upload failed")
                            }

                            // Else, call the saved image URL
                            profileImageRef.downloadUrl

                        }?.addOnCompleteListener { downloadTask ->

                            // if image URL is valid, save user details to firebase with imageURL
                            if (downloadTask.isSuccessful) {
                                profileImageUrl = downloadTask.result.toString()
                                saveUserData(username, email, firstname, lastname, profileImageUrl, user_ID, publicKeyString)

                                progressDialog.dismiss()

                                val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                                startActivity(intent)
                                finish()

                                Log.d("SignUpImageUpload", "Image uploaded successfully. Download URL: $profileImageUrl")
                            }
                            else {
                                Log.e("SignUpImageUpload", "Failed to retrieve download URL: ${downloadTask.exception?.message}")
                            }

                        }?.addOnFailureListener { exception ->
                            Log.e("SignUpImageUpload", "Image upload failed: ${exception.message}")
                        }
                    }

                    // If image storage fails for anu reason, save user details without Profile image
                    else {
                        saveUserData(username, email, firstname, lastname, profileImageUrl, user_ID, publicKeyString)

                        progressDialog.dismiss()

                        val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                // Authenication failed
                else {
                    progressDialog.dismiss()

                    Toast.makeText(
                        this@SignUpActivity,
                        task.exception?.message ?: "Unknown error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Save user details to Realtime Database
    private fun saveUserData(username: String, email: String, firstname: String, lastname: String, profileImageUrl: String, user_ID: String, publicKeyString: String) {
        val user = UserModel(
            username = username,
            email = email,
            firstname = firstname,
            lastname = lastname,
            profile_Pic = profileImageUrl,
            user_ID = user_ID,
            publicKey = publicKeyString
        )

        database.reference
            .child("Users")
            .child(user_ID)
            .setValue(user)
    }

    // Handles the initial animations of the page
    private fun setAnimations() {
        val topAnim = AnimationUtils.loadAnimation(this, R.anim.fade_down_anim)
        val bottomAnim = AnimationUtils.loadAnimation(this, R.anim.fade_up_anim)

        binding.welcomeRegisterText.animation = topAnim
        binding.signUpMainLayout.animation = bottomAnim
    }

    // Open Phone's image picker
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, pickImageRequestCode)
    }

    // Override existing function to check result of external Activities we implemented
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            // Result of Image selection
            pickImageRequestCode -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val imageUri: Uri = data.data!!
                    profileImageUri = imageUri

                    Picasso.get()
                        .load(profileImageUri)
                        .placeholder(R.drawable.profile_circle_svgrepo_com)
                        .into(binding.suProfileImage)
                }
            }

            // Result of Google Sign In Activity
            googleSignInRequestCode -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    account = task.getResult(ApiException::class.java)
                    isGoogleSignUp = true
                    setPage2()

                } catch (e: ApiException) {
                    Toast.makeText(this@SignUpActivity,
                        "Failed Google Account Register",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}







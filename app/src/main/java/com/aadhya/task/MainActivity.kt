package com.aadhya.task

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aadhya.task.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN: Int = 123
    private lateinit var verificationId: String
    private lateinit var number: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificationId = ""
        number = ""
        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_client_id)).requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(this.applicationContext , gso)

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            println("=====USer ${currentUser.uid} ${currentUser.email}")
            val intent = Intent(this@MainActivity , SecondActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.googleButton.setOnClickListener {
            googlSignIn()
        }
        binding.btnGetOtp.setOnClickListener {
            if (binding.edtMobileNumber.text.toString().isEmpty()) {
                Toast.makeText(this , "Enter mobile number" , Toast.LENGTH_SHORT).show()
            } else {
                number = "+91${binding.edtMobileNumber.text}"
                println("Number is $number")
                if (number.isNotEmpty()) {
                    sendCode(number)
                    Toast.makeText(this , "Otp sent Successfully.." , Toast.LENGTH_SHORT).show()
                    binding.layoutSetOtp.visibility = View.VISIBLE
                }
            }
        }
        binding.btnSetOtp.setOnClickListener {
            val otp = binding.otpView.otp.toString()
            Log.d("OTP" , "OTP IS $otp")
            if (otp.isEmpty()) {
                Toast.makeText(this , "Please enter OTP" , Toast.LENGTH_SHORT).show()
            } else {
                verifyCode(otp)
            }
        }
    }

    private fun verifyCode(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId , otp)
        signInWithCredential(credential)
    }

    private fun sendCode(number: String) {
        sendVerificationCode(number)

    }

    private fun sendVerificationCode(phone: String) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(phone)
            .setTimeout(120L , TimeUnit.SECONDS).setActivity(this).setCallbacks(mCallback).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this , "Welcome.." , Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(this@MainActivity , SecondActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this , "Invalid OTP. Please try again." , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(
            verificationId: String ,
            token: PhoneAuthProvider.ForceResendingToken ,
        ) {
            super.onCodeSent(verificationId , token)
            this@MainActivity.verificationId = verificationId
        }

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            val code = credential.smsCode
            if (code != null) {
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@MainActivity , e.message , Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int , resultCode: Int , data: Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Toast.makeText(this , "Google sign in failed: ${e.message}" , Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken , null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this , "Sign in successful!" , Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity , SecondActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(
                    this , "Authentication failed: ${task.exception?.message}" , Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun googlSignIn() {
        val intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent , RC_SIGN_IN)
    }

}
package com.aadhya.task

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aadhya.task.databinding.ActivitySecondBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            if (currentUser.photoUrl == null) {
                Glide.with(this).load(R.drawable.bg).into(binding.imgView)
            } else {
                Glide.with(this).load(currentUser.photoUrl).into(binding.imgView)
            }

            binding.textView2.text =
                currentUser.displayName + currentUser.email + currentUser.phoneNumber.toString()
        }
    }
}
package com.example.chathive.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri



object UserRepository {

    fun uploadProfileImage(uri:Uri , onComplete: (String?) -> Unit) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = FirebaseStorage.getInstance().getReference("profile_pics/$uid.jpg")

        ref.putFile(uri)
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { url ->
                FirebaseDatabase.getInstance().getReference("users/$uid/profileImageUrl")
                    .setValue(url.toString())
                onComplete(url.toString())
            }
    }
    fun saveUserName(name: String) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        FirebaseDatabase.getInstance().getReference("users/$uid/name").setValue(name)
    }



}
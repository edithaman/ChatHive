package com.example.chathive.repository


import android.content.Context
import android.net.Uri
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.chathive.datamodel.Message
import com.example.chathive.datamodel.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import com.android.volley.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import java.util.UUID

object ChatRepository {
    private val db = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val currentUid = FirebaseAuth.getInstance().uid ?: ""

    private fun getChatRef(chatUid: String): DatabaseReference {
        return db.getReference("chat_rooms/$currentUid/$chatUid/messages")
    }

    private fun getOtherChatRef(chatUid: String): DatabaseReference {
        return db.getReference("chat_rooms/$chatUid/$currentUid/messages")
    }

    fun sendText(chatUid: String, text: String) {
        val message = Message(
            senderId = currentUid,
            text = text,
            isRead = false
        )
        val key = getChatRef(chatUid).push().key ?: return
        getChatRef(chatUid).child(key).setValue(message)
        getOtherChatRef(chatUid).child(key).setValue(message)

        sendTyping(chatUid, false)
    }

    fun sendImage(chatUid: String, uri: Uri) {
        val ref = storage.getReference("chat_images/$currentUid/${UUID.randomUUID()}.jpg")
        ref.putFile(uri)
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { url ->
                val msg = Message(senderId = currentUid, imageUrl = url.toString())
                val key = getChatRef(chatUid).push().key ?: return@addOnSuccessListener
                getChatRef(chatUid).child(key).setValue(msg)
                getOtherChatRef(chatUid).child(key).setValue(msg)
            }
    }

    fun observeMessages(chatUid: String, onMessages: (List<Message>) -> Unit) {
        getChatRef(chatUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                snapshot.children.forEach {
                    it.getValue(Message::class.java)?.let { msg -> list.add(msg) }
                }
                onMessages(list.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun setTyping(chatUid: String, isTyping: Boolean) {
        db.getReference("typing_status/$chatUid/$currentUid").setValue(isTyping)
    }

    fun sendTyping(chatUid: String, isTyping: Boolean) = setTyping(chatUid, isTyping)

    fun observeTypingStatus(chatUid: String, onTyping: (Boolean) -> Unit) {
        db.getReference("typing_status/$currentUid/$chatUid").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onTyping(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun markMessagesAsRead(chatUid: String) {
        getOtherChatRef(chatUid).get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { snap ->
                snap.ref.child("isRead").setValue(true)
            }
        }
    }
}
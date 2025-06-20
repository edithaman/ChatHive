package com.example.chathive.screens


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chathive.R
import com.example.chathive.datamodel.Message
import com.example.chathive.repository.AuthRepository
import com.example.chathive.repository.ChatRepository
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(navController: NavController, chatUid: String) {
    val currentUid = AuthRepository.currentUser?.uid ?: return
    val messages = remember { mutableStateListOf<Message>() }
    val text = remember { mutableStateOf("") }
    val isTyping = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            ChatRepository.sendImage(chatUid, it)
        }
    }

    // Real-time message observation
    LaunchedEffect(chatUid) {
        ChatRepository.observeMessages(chatUid) {
            messages.clear()
            messages.addAll(it)
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size)
            }
        }
        ChatRepository.observeTypingStatus(chatUid) {
            isTyping.value = it
        }
        ChatRepository.markMessagesAsRead(chatUid)
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            contentPadding = PaddingValues(8.dp)
        ) {
            items(messages) { msg ->
                val sentByMe = msg.senderId == currentUid
                val bubbleColor = if (sentByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val alignment = if (sentByMe) Alignment.End else Alignment.Start

                Column(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalAlignment = alignment
                ) {
                    Box(
                        modifier = Modifier
                            .background(bubbleColor, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            if (msg.text.isNotBlank()) {
                                Text(msg.text, color = Color.White)
                            }
                            msg.imageUrl?.let {
                                Spacer(Modifier.height(6.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                        }
                    }
                    if (sentByMe) {
                        val status = if (msg.isRead) "✅ Read" else "🕓 Sent"
                        Text(status, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }

        if (isTyping.value) {
            Text("$chatUid is typing...", fontStyle = FontStyle.Italic, modifier = Modifier.padding(start = 12.dp))
        }

        Row(modifier = Modifier.padding(8.dp)) {
            IconButton(onClick = { launcher.launch("image/*") }) {
                Icon(painter = painterResource(R.drawable.baseline_image_24), contentDescription = "Send Image")
            }

            TextField(
                value = text.value,
                onValueChange = {
                    text.value = it
                    ChatRepository.setTyping(chatUid, it.isNotBlank())
                },
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {
                if (text.value.isNotBlank()) {
                    ChatRepository.sendText(chatUid, text.value)
                    text.value = ""
                    ChatRepository.setTyping(chatUid, false)
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}



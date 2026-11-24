
package com.mitra.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MitraAIApp()
        }
    }
}

@Composable
fun MitraAIApp() {
    var input by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("नमस्ते! मैं Mitra AI हूँ। आपकी क्या मदद करूँ?", false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(12.dp)
    ) {
        Text(
            "Mitra AI",
            fontSize = 28.sp,
            color = Color(0xFF5533AA)
        )

        Text(
            "आपका अपना AI सहायक",
            color = Color.Gray
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.isUser)
                        Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (message.isUser)
                            Color(0xFF6545C5) else Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            message.text,
                            color = if (message.isUser)
                                Color.White else Color.Black,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("अपना सवाल लिखें...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        messages.add(ChatMessage(input, true))
                        messages.add(
                            ChatMessage(
                                "आपका संदेश मिल गया! Gemini AI कनेक्शन अभी सेट करना बाकी है।",
                                false
                            )
                        )
                        input = ""
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}


package com.mitra.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    var message by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(listOf("Namaste! Main Mitra AI hoon. Aapki kya madad kar sakta hoon?"))
    }

    val bg = Color(0xFF101522)
    val purple = Color(0xFF7657F6)

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(16.dp)
        ) {
            Text(
                text = "Mitra AI",
                color = Color.White,
                fontSize = 28.sp
            )

            Text(
                text = "Aapka smart AI assistant",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages.size) { index ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF242D40)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = messages[index],
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Mitra se kuch poocho...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            messages = messages + "You: $message"
                            message = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purple
                    )
                ) {
                    Text("Send")
                }
            }
        }
    }
}

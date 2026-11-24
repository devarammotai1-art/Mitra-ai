
package com.mitra.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        setContent {
            MaterialTheme {
                MitraAIApp()
            }
        }
    }
}

@Composable
fun MitraAIApp() {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("नमस्ते! मैं Mitra AI हूँ। मुझसे कुछ भी पूछिए।", false)
        )
    }

    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mitra AI",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                Text(
                    text = (if (message.isUser) "आप: " else "Mitra AI: ") +
                        message.text
                )
            }
        }

        if (loading) {
            CircularProgressIndicator()
            Text("जवाब तैयार हो रहा है...")
        }

        Row {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("अपना सवाल लिखें") },
                enabled = !loading
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                enabled = !loading && input.isNotBlank(),
                onClick = {
                    val question = input.trim()
                    input = ""
                    messages.add(ChatMessage(question, true))
                    loading = true

                    scope.launch {
                        try {
                            val model = Firebase.ai(
                                backend = GenerativeBackend.googleAI()
                            ).generativeModel("gemini-3.5-flash")

                            val response = model.generateContent(question)
                            val answer = response.text
                                ?: "अभी जवाब नहीं मिल पाया।"

                            messages.add(ChatMessage(answer, false))
                        } catch (e: Exception) {
                            messages.add(
                                ChatMessage(
                                    "त्रुटि: ${e.message ?: "कनेक्शन नहीं हुआ"}",
                                    false
                                )
                            )
                        } finally {
                            loading = false
                        }
                    }
                }
            ) {
                Text("भेजें")
            }
        }
    }
}

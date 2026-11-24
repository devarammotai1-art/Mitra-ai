
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray

private const val API_KEY = AIzaSyBUXIYCjUHFl55BQYTVhZc2BcAzaQRzHLo

data class ChatMessage(val text: String, val isUser: Boolean)

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
        Text("Mitra AI", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                Text(
                    text = (if (message.isUser) "आप: " else "Mitra AI: ") + message.text
                )
            }
        }

        if (loading) {
            CircularProgressIndicator()
        }

        Row {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("अपना सवाल लिखें") }
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
                        val answer = withContext(Dispatchers.IO) {
                            askGemini(question)
                        }
                        messages.add(ChatMessage(answer, false))
                        loading = false
                    }
                }
            ) {
                Text("भेजें")
            }
        }
    }
}

fun askGemini(question: String): String {
    return try {
        val client = OkHttpClient()

        val part = JSONObject().put("text", question)
        val parts = JSONArray().put(part)
        val content = JSONObject().put("parts", parts)
        val contents = JSONArray().put(content)
        val json = JSONObject().put("contents", contents)

        val body = json.toString().toRequestBody(
            "application/json".toMediaType()
        )

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent")
            .addHeader("x-goog-api-key", API_KEY)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val result = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                "समस्या आई: $result"
            } else {
                val data = JSONObject(result)
                data.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            }
        }
    } catch (e: Exception) {
        "कनेक्शन में समस्या: ${e.message}"
    }
}


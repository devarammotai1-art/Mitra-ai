
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val API_KEY = "AIzaSyBUXIYCjUHFl55BQYTVhZc2BcAzaQRzHLo"

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(120, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            ChatMessage(
                "नमस्ते! मैं Mitra AI हूँ। मुझसे कुछ भी पूछिए।",
                false
            )
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
            "Mitra AI",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                Text(
                    text = (if (message.isUser) "आप: " else "Mitra AI: ") +
                        message.text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (loading) {
            CircularProgressIndicator()
            Text("Mitra AI जवाब तैयार कर रहा है...")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
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

    if (API_KEY == "AIzaSyBUXIYCjUHFl55BQYTVhZc2BcAzaQRzHLo") {
        return "AIzaSyBUXIYCjUHFl55BQYTVhZc2BcAzaQRzHLo"
    }

    return try {
        val part = JSONObject().put("text", question)
        val parts = JSONArray().put(part)
        val content = JSONObject().put("parts", parts)
        val contents = JSONArray().put(content)
        val json = JSONObject().put("contents", contents)

        val body = json.toString().toRequestBody(
            "application/json; charset=utf-8".toMediaType()
        )

        val request = Request.Builder()
            .url(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"
            )
            .addHeader("x-goog-api-key", API_KEY)
            .post(body)
            .build()

        var lastError = "अभी जवाब नहीं मिल पाया।"

        for (attempt in 1..3) {

            httpClient.newCall(request).execute().use { response ->

                val result = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val data = JSONObject(result)

                    return data
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                }

                lastError = when (response.code) {
                    429 -> "Gemini की Free Tier सीमा पूरी हुई है। थोड़ी देर बाद कोशिश करें।"
                    503 -> "Gemini सर्वर व्यस्त है। दोबारा कोशिश हो रही है।"
                    else -> "API Error ${response.code}: $result"
                }

                if (response.code != 503 && response.code != 429) {
                    return lastError
                }
            }

            if (attempt < 3) {
                Thread.sleep(attempt * 3000L)
            }
        }

        lastError + " कृपया थोड़ी देर बाद फिर कोशिश करें।"

    } catch (e: Exception) {
        "कनेक्शन में समस्या: ${e.message}"
    }
}

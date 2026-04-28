package com.example.caquickpoll

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AiChatActivity : AppCompatActivity() {

    private lateinit var chatBox: LinearLayout
    private lateinit var input: EditText
    private lateinit var sendBtn: Button
    private lateinit var useBtn: Button
    private lateinit var scrollView: ScrollView

    private val GEMINI_API_KEY = "AIzaSyA0yRHk4kiOav-PM4uPyIsCm81fOFyk4HU"

    private var lastGeneratedJson: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)

        chatBox = findViewById(R.id.chatContainer)
        input = findViewById(R.id.chatInput)
        sendBtn = findViewById(R.id.sendBtn)
        useBtn = findViewById(R.id.useBtn)
        scrollView = findViewById(R.id.scrollView)

        sendBtn.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                addMessage("You: $text")
                input.text.clear()
                callGemini(text)
            }
        }

        useBtn.setOnClickListener {
            if (lastGeneratedJson.isEmpty()) {
                toast("Generate first ❌")
                return@setOnClickListener
            }

            val intent = Intent()
            intent.putExtra("ai_data", lastGeneratedJson)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun addMessage(msg: String) {
        val tv = TextView(this)
        tv.text = msg
        tv.textSize = 16f
        tv.setPadding(16, 16, 16, 16)
        chatBox.addView(tv)

        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun callGemini(promptText: String) {

        addMessage("AI: Generating... 🤖")

        val client = OkHttpClient()
        val prompt = """
            Return ONLY valid JSON array. No explanation, no text.

            Format:
            [
              {
                "question": "Question text",
                "options": ["Option 1", "Option 2", "Option 3"]
              }
            ]

            Generate exactly 3 questions for topic: $promptText
            Each question must have exactly 3 options.
        """.trimIndent()

        val json = JSONObject()
        val part = JSONObject().put("text", prompt)
        val parts = JSONArray().put(part)
        val content = JSONObject().put("parts", parts)
        val contents = JSONArray().put(content)
        json.put("contents", contents)

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$GEMINI_API_KEY")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { addMessage("AI: Failed ❌") }
            }

            override fun onResponse(call: Call, response: Response) {

                val res = response.body?.string()

                runOnUiThread {
                    try {
                        val text = JSONObject(res)
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        addMessage("AI:\n$text")


                        var clean = text.trim()


                        clean = clean.replace("```json", "")
                            .replace("```", "")
                            .trim()


                        val start = clean.indexOf("[")
                        val end = clean.lastIndexOf("]")

                        if (start != -1 && end != -1) {
                            clean = clean.substring(start, end + 1)
                            JSONArray(clean)

                            lastGeneratedJson = clean
                        } else {
                            toast("Invalid AI format ❌")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        addMessage("AI RAW:\n$res")
                    }
                }
            }
        })
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
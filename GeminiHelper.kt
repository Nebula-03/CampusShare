package com.example.caquickpoll

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GeminiHelper {

    private const val GEMINI_API_KEY = "AIzaSyA0yRHk4kiOav-PM4uPyIsCm81fOFyk4HU"

    fun generatePoll(topic: String, callback: (String?) -> Unit) {

        val client = OkHttpClient()

        val prompt = """
            Return ONLY JSON.
            Generate 3 poll questions about $topic.
            Each must have exactly 3 options.

            [
              {"question":"...","options":["...","...","..."]}
            ]
        """.trimIndent()

        val part = JSONObject()
        part.put("text", prompt)

        val partsArray = JSONArray()
        partsArray.put(part)

        val content = JSONObject()
        content.put("parts", partsArray)

        val contentsArray = JSONArray()
        contentsArray.put(content)

        val json = JSONObject()
        json.put("contents", contentsArray)

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$GEMINI_API_KEY")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                callback(res)
            }
        })
    }
}
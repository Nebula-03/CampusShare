package com.example.caquickpoll

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EnterPollActivity : AppCompatActivity() {

    lateinit var input: EditText
    lateinit var openBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_poll)

        input = findViewById(R.id.pollIdInput)
        openBtn = findViewById(R.id.openBtn)

        openBtn.setOnClickListener {

            val pollId = input.text.toString().trim()

            if (pollId.isEmpty()) {
                Toast.makeText(this, "Enter Poll ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, VotePollActivity::class.java)
            intent.putExtra("pollId", pollId)
            startActivity(intent)
        }
    }
}
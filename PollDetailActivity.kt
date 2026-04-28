package com.example.caquickpoll

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class PollDetailActivity : AppCompatActivity() {

    lateinit var container: LinearLayout
    lateinit var editBtn: ImageView
    lateinit var deleteBtn: ImageView

    val db = FirebaseFirestore.getInstance()
    lateinit var currentPoll: Poll

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poll_detail)

        container = findViewById(R.id.container)
        editBtn = findViewById(R.id.editBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
        val shareBtn = findViewById<ImageView>(R.id.shareBtn)

        // ✅ SAFE FETCH
        val pollId = intent.getStringExtra("pollId")

        if (pollId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Poll ID ❌", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPoll(pollId)

        shareBtn.setOnClickListener {
            val shareText = "Vote on my poll:\n\nOpen app → Enter Poll ID: $pollId"

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareText)

            startActivity(Intent.createChooser(intent, "Share Poll"))
        }
    }

    private fun loadPoll(id: String) {

        db.collection("Polls").document(id).get()
            .addOnSuccessListener {

                val poll = it.toObject(Poll::class.java)

                if (poll == null) {
                    Toast.makeText(this, "Poll not found ❌", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                currentPoll = poll
                currentPoll.id = it.id

                displayPoll()

                // ✅ EDIT SAFE
                editBtn.setOnClickListener {
                    val intent = Intent(this, CreatePollActivity::class.java)
                    intent.putExtra("pollId", currentPoll.id)
                    startActivity(intent)
                }

                // ✅ DELETE SAFE
                deleteBtn.setOnClickListener {
                    deletePoll(currentPoll.id)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load poll ❌", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayPoll() {

        container.removeAllViews()

        for ((index, q) in currentPoll.questions.withIndex()) {

            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(20, 20, 20, 20)
            card.setBackgroundResource(R.drawable.card_bg)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 24)
            card.layoutParams = params

            val title = TextView(this)
            title.text = "Question ${index + 1}"
            title.textSize = 14f
            title.setTextColor(0xFF6D4C41.toInt())

            val question = TextView(this)
            question.text = q["question"]?.toString() ?: ""
            question.textSize = 18f
            question.setTextColor(0xFF4E342E.toInt())
            question.setPadding(0, 8, 0, 12)

            card.addView(title)
            card.addView(question)

            // ✅ SAFE OPTIONS (NO CRASH)
            val optionsRaw = q["options"] as? ArrayList<*>

            if (optionsRaw != null) {
                for (item in optionsRaw) {

                    val optionBox = TextView(this)
                    optionBox.text = item.toString()
                    optionBox.textSize = 15f
                    optionBox.setTextColor(0xFF4E342E.toInt())
                    optionBox.setPadding(16, 12, 16, 12)
                    optionBox.setBackgroundResource(R.drawable.edit_bg)

                    val optParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    optParams.setMargins(0, 6, 0, 6)
                    optionBox.layoutParams = optParams

                    card.addView(optionBox)
                }
            }

            container.addView(card)
        }
    }

    private fun deletePoll(id: String) {

        db.collection("Polls").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Poll deleted 🗑️", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Delete failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
package com.example.caquickpoll

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class VotePollActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var submitBtn: Button

    private val db = FirebaseFirestore.getInstance()

    private var selectedAnswers = HashMap<Int, Int>() // questionIndex -> optionIndex
    private lateinit var pollId: String
    private lateinit var poll: Poll

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote_poll)

        container = findViewById(R.id.container)
        submitBtn = findViewById(R.id.submitBtn)

        pollId = intent.getStringExtra("pollId") ?: ""

        if (pollId.isEmpty()) {
            Toast.makeText(this, "Invalid Poll", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPoll()

        submitBtn.setOnClickListener {

            if (selectedAnswers.isEmpty()) {
                Toast.makeText(this, "Please select at least one option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitVote()
        }
    }

    // 🔥 LOAD POLL
    private fun loadPoll() {

        db.collection("Polls").document(pollId).get()
            .addOnSuccessListener {

                val data = it.toObject(Poll::class.java)

                if (data == null) {
                    Toast.makeText(this, "Poll not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                poll = data
                displayPoll()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load poll", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔥 DISPLAY POLL (UI)
    private fun displayPoll() {

        container.removeAllViews()

        for ((index, q) in poll.questions.withIndex()) {

            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(30, 30, 30, 30)
            card.setBackgroundResource(R.drawable.card_bg)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            card.layoutParams = params

            val question = TextView(this)
            question.text = "Q${index + 1}. ${q["question"]}"
            question.textSize = 18f
            question.setTextColor(0xFF4E342E.toInt())

            card.addView(question)

            val radioGroup = RadioGroup(this)

            val options = q["options"] as List<String>

            for ((i, opt) in options.withIndex()) {

                val radio = RadioButton(this)
                radio.text = opt
                radio.setTextColor(0xFF4E342E.toInt())

                radioGroup.addView(radio)
            }

            // 🔥 HANDLE SELECTION
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                val selectedIndex =
                    radioGroup.indexOfChild(radioGroup.findViewById(checkedId))
                selectedAnswers[index] = selectedIndex
            }

            card.addView(radioGroup)
            container.addView(card)
        }
    }

    // 🔥 SUBMIT VOTE (FULLY FIXED)
    private fun submitVote() {

        val updatedQuestions = ArrayList<HashMap<String, Any>>()

        for ((qIndex, q) in poll.questions.withIndex()) {

            val newQ = HashMap<String, Any>()

            val questionText = q["question"] as String
            val options = q["options"] as List<String>
            val oldVotes = q["votes"] as List<*>

            val votes = ArrayList<Long>()

            for (v in oldVotes) {
                votes.add((v as Number).toLong())
            }

            if (selectedAnswers.containsKey(qIndex)) {
                val selectedIndex = selectedAnswers[qIndex]!!
                votes[selectedIndex] = votes[selectedIndex] + 1
            }

            newQ["question"] = questionText
            newQ["options"] = options
            newQ["votes"] = votes

            updatedQuestions.add(newQ)
        }

        Toast.makeText(this, "Submitting...", Toast.LENGTH_SHORT).show()

        val updatedPoll = hashMapOf(
            "pollName" to poll.pollName,
            "createdBy" to poll.createdBy,
            "questions" to updatedQuestions,
            "votedUsers" to poll.votedUsers,
            "imageUrl" to poll.imageUrl,
            "expiryTime" to poll.expiryTime,
            "type" to "published"
        )

        db.collection("Polls").document(pollId)
            .set(updatedPoll) // 🔥 KEY FIX
            .addOnSuccessListener {

                Toast.makeText(this, "Poll submitted 🎉", Toast.LENGTH_LONG).show()

                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("pollId", pollId)
                startActivity(intent)

                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
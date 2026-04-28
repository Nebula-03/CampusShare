package com.example.caquickpoll

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ResultDetailActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var title: TextView

    private val db = FirebaseFirestore.getInstance()
    private lateinit var pollId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_detail)

        container = findViewById(R.id.resultContainer)
        title = findViewById(R.id.pollTitle)

        pollId = intent.getStringExtra("pollId")!!

        loadResults()
    }

    private fun loadResults() {

        db.collection("Polls").document(pollId).get()
            .addOnSuccessListener { doc ->

                val poll = doc.toObject(Poll::class.java) ?: return@addOnSuccessListener

                title.text = poll.pollName

                container.removeAllViews()

                // 🔥 LOOP THROUGH ALL QUESTIONS (not just first)
                for ((qIndex, q) in poll.questions.withIndex()) {

                    // 🔥 QUESTION TITLE CARD
                    val questionTitle = TextView(this)
                    questionTitle.text = "Q${qIndex + 1}. ${q["question"]}"
                    questionTitle.textSize = 18f
                    questionTitle.setTextColor(0xFF4E342E.toInt())
                    questionTitle.setPadding(0, 10, 0, 10)

                    container.addView(questionTitle)

                    val options = q["options"] as? List<String> ?: continue
                    val votes = q["votes"] as? List<Long> ?: continue

                    val totalVotes = votes.sum().toInt()

                    for (i in options.indices) {

                        val view = layoutInflater.inflate(
                            R.layout.item_result_option,
                            container,
                            false
                        )

                        val percent =
                            if (totalVotes == 0) 0
                            else ((votes[i] * 100) / totalVotes).toInt()

                        // 🔥 SET DATA
                        view.findViewById<TextView>(R.id.optionText).text = options[i]
                        view.findViewById<TextView>(R.id.percentText).text = "$percent%"
                        view.findViewById<ProgressBar>(R.id.progressBar).progress = percent

                        // 🔥 NEW INFO (IMPORTANT)
                        view.findViewById<TextView>(R.id.voteCount).text =
                            "Votes: ${votes[i]}"

                        view.findViewById<TextView>(R.id.totalVotes).text =
                            "Total Votes: $totalVotes"

                        container.addView(view)
                    }
                }
            }
            .addOnFailureListener {
                title.text = "Failed to load results"
            }
    }
}
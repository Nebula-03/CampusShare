package com.example.caquickpoll

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ResultActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ResultAdapter

    private val db = FirebaseFirestore.getInstance()
    private val list = ArrayList<Poll>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        recycler = findViewById(R.id.recyclerView)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = ResultAdapter(list) { poll ->
            val intent = Intent(this, ResultDetailActivity::class.java)
            intent.putExtra("pollId", poll.id)
            startActivity(intent)
        }

        recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadPolls()
    }

    private fun loadPolls() {

        db.collection("Polls")
            .whereEqualTo("type", "published")
            .get()
            .addOnSuccessListener {

                list.clear()

                for (doc in it) {
                    val poll = doc.toObject(Poll::class.java)
                    poll.id = doc.id
                    list.add(poll)
                }

                adapter.notifyDataSetChanged()
            }
    }
}
package com.example.caquickpoll

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DraftActivity : AppCompatActivity() {

    lateinit var recycler: RecyclerView

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val list = ArrayList<Poll>()
    lateinit var adapter: DraftAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draft)

        recycler = findViewById(R.id.draftRecycler)

        recycler.layoutManager = LinearLayoutManager(this)

        adapter = DraftAdapter(this, list)
        recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadDrafts()
    }

    private fun loadDrafts() {

        list.clear()

        db.collection("Polls")
            .whereEqualTo("createdBy", auth.currentUser?.uid)
            .whereEqualTo("type", "draft")
            .get()
            .addOnSuccessListener {

                for (doc in it) {
                    val poll = doc.toObject(Poll::class.java)
                    poll.id = doc.id
                    list.add(poll)
                }

                adapter.notifyDataSetChanged()
            }
    }
}
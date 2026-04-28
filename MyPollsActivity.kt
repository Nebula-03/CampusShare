package com.example.caquickpoll

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyPollsActivity : AppCompatActivity() {

    lateinit var activeRecycler: RecyclerView
    lateinit var expiredRecycler: RecyclerView

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val activeList = ArrayList<Poll>()
    val expiredList = ArrayList<Poll>()

    lateinit var activeAdapter: MyPollAdapter
    lateinit var expiredAdapter: MyPollAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_polls)

        activeRecycler = findViewById(R.id.activeRecycler)
        expiredRecycler = findViewById(R.id.expiredRecycler)

        activeRecycler.layoutManager = LinearLayoutManager(this)
        expiredRecycler.layoutManager = LinearLayoutManager(this)

        activeAdapter = MyPollAdapter(this, activeList, false)
        expiredAdapter = MyPollAdapter(this, expiredList, true)

        activeRecycler.adapter = activeAdapter
        expiredRecycler.adapter = expiredAdapter
    }

    override fun onResume() {
        super.onResume()
        loadPolls()
    }

    private fun loadPolls() {

        val userId = auth.currentUser?.uid

        activeList.clear()
        expiredList.clear()

        db.collection("Polls")
            .whereEqualTo("createdBy", userId)
            .get()
            .addOnSuccessListener { result ->

                for (doc in result) {

                    val poll = doc.toObject(Poll::class.java)
                    poll.id = doc.id

                    // ✅ ONLY SHOW PUBLISHED
                    if (poll.type == "published") {

                        val expiry = poll.expiryTime // ✅ FIXED

                        if (System.currentTimeMillis() > expiry) {
                            expiredList.add(poll)
                        } else {
                            activeList.add(poll)
                        }
                    }
                }

                activeAdapter.notifyDataSetChanged()
                expiredAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
}
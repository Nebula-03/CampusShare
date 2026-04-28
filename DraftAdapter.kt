package com.example.caquickpoll

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class DraftAdapter(
    val context: Context,
    val list: ArrayList<Poll>
) : RecyclerView.Adapter<DraftAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.pollTitle)
        val card: LinearLayout = view as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_my_poll, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val poll = list[position]

        holder.title.text = poll.pollName

        holder.itemView.setOnClickListener {

            val intent = Intent(context, CreatePollActivity::class.java)
            intent.putExtra("editPoll", poll)
            context.startActivity(intent)
        }
    }
}
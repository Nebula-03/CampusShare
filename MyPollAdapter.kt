package com.example.caquickpoll

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyPollAdapter(
    val context: Context,
    val list: ArrayList<Poll>,
    val isExpired: Boolean
) : RecyclerView.Adapter<MyPollAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.pollTitle)
        val status: TextView = view.findViewById(R.id.pollStatus)
        val image: ImageView = view.findViewById(R.id.pollImage)
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

        if (isExpired) {
            holder.status.text = "Expired"
            holder.status.setTextColor(0xFFD32F2F.toInt())
        } else {
            holder.status.text = "Active"
            holder.status.setTextColor(0xFF388E3C.toInt())
        }


        if (!poll.imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(poll.imageUrl)
                .into(holder.image)
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PollDetailActivity::class.java)
            intent.putExtra("pollId", poll.id)
            context.startActivity(intent)
        }
    }
}
package com.example.talksafe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth


class MessageAdapter(val context: Context, val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVED = 1
    val ITEM_SENT = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 1){
            // inflate receive
            val view: View = LayoutInflater.from(context).inflate(R.layout.received,parent,false)
            return ReceivedViewHolder(view)
        }else {
            // inflate sent
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent,parent,false)
            return SentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder.javaClass == SentViewHolder::class.java) {
            // sent view holder
            holder as SentViewHolder
            buildMessage(currentMessage)
            holder.sentMessage.text = buildMessage(currentMessage)

        } else {
            holder as ReceivedViewHolder
            holder.receivedMessage.text = buildMessage(currentMessage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderID))
            return ITEM_SENT
        else {
            return ITEM_RECEIVED
        }
    }
    override fun getItemCount(): Int {
    return messageList.size
    }

    private fun buildMessage(msg:Message):String{
        if (msg.timed == true) {
            val builder = StringBuilder()
            builder.append("[")
                .append(msg.timeLimit)
                .append("s] ")
                .append(msg.message)
            return builder.toString()
        } else {
            return msg.message!!
        }
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage = itemView.findViewById<TextView>(R.id.t_sent)
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage = itemView.findViewById<TextView>(R.id.t_received)
    }

}
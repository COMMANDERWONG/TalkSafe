package com.example.talksafe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Chat : AppCompatActivity() {
    private lateinit var chatView: RecyclerView
    private lateinit var msgBox: EditText
    private lateinit var sendBtn: ImageView
    private lateinit var msgAdapter: MessageAdapter
    private lateinit var msgList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    var receiverRoom: String? = null
    var senderRoom: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val name = intent.getStringExtra("name")
        val receiverUID = intent.getStringExtra("uid")

        val senderUID = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = receiverUID + senderUID
        receiverRoom = senderUID + receiverUID
        supportActionBar?.title = name

        chatView = findViewById(R.id.chat_view)
        msgBox = findViewById(R.id.msg_box)
        sendBtn = findViewById(R.id.send_btn)
        msgList = ArrayList()
        msgAdapter = MessageAdapter(this, msgList)

        chatView.layoutManager = LinearLayoutManager(this)
        chatView.adapter = msgAdapter

        // get messages from database
        mDbRef.child("chat").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    msgList.clear()
                    for (ps in snapshot.children) {
                        val message = ps.getValue(Message::class.java)
                        msgList.add(message!!)
                    }
                    msgAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {


                }
            })// adding message to database
        sendBtn.setOnClickListener {

            val message = msgBox.text.toString()
            val messageObj = Message(message, senderUID)

            mDbRef.child("chat").child(senderRoom!!).child("messages").push()
                .setValue(messageObj).addOnSuccessListener {
                    mDbRef.child("chat").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObj)
                }
            msgBox.setText("")
        }
    }
}

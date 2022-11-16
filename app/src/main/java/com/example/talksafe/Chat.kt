package com.example.talksafe

import android.content.Intent
import android.content.IntentSender.OnFinished
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class Chat : AppCompatActivity() {
    private lateinit var chatView: RecyclerView
    private lateinit var msgBox: EditText
    private lateinit var msgTimer: EditText
    private lateinit var sendBtn: ImageView
    private lateinit var msgAdapter: MessageAdapter
    private lateinit var msgList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

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
        msgTimer = findViewById(R.id.msg_timer)
        sendBtn = findViewById(R.id.send_btn)
        mAuth = FirebaseAuth.getInstance()
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
            })

        // adding message to database
        sendBtn.setOnClickListener {
            val message = msgBox.text.toString()
            if (message.isNotEmpty()) {
                val timer = msgTimer.text.toString()
                if ((timer.isEmpty()) || (timer.isNotEmpty() && timer.isDigitsOnly() && timer.toInt() >= 0 && timer.toInt() <= 600)) {
                    val messageObj = Message(message, senderUID, false)

                    if (timer.isNotEmpty()) {
                        messageObj.timed = true
                        messageObj.timeLimit = timer.toInt()
                    }

                    mDbRef.child("chat").child(senderRoom!!).child("messages").push()
                        .setValue(messageObj).addOnSuccessListener {
                            mDbRef.child("chat").child(receiverRoom!!).child("messages").push()
                                .setValue(messageObj)
                        }
                    msgBox.setText("")
                    msgTimer.setText("")

                    if (messageObj.timed == true) {
                        var temp = messageObj.timeLimit!!.toLong()
                        object : CountDownTimer(temp, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                //temp = temp.minus(1)
                                //println(temp)
                            }

                            override fun onFinish() {
                                //mDbRef.child("chat").child(senderRoom!!).child("messages").
                            }
                        }.start()
                    }
                } else {
                    Toast.makeText(
                        this@Chat,
                        "The timer accepts integer (0 - 600) input only!",
                        Toast.LENGTH_SHORT
                    ).show()
                    msgTimer.setText("")
                }
            } else {
                Toast.makeText(
                    this@Chat,
                    "Please type a message before sending it!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.removeChat -> {
                mDbRef.child("chat").child(senderRoom!!).child("messages").removeValue()
                mDbRef.child("chat").child(receiverRoom!!).child("messages").removeValue()
                Toast.makeText(
                    this@Chat,
                    "All messages deleted",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@Chat, MainActivity::class.java)
                finish()
                startActivity(intent)
            }
            R.id.logout -> {
                mAuth.signOut()
                val intent = Intent(this@Chat, LogIn::class.java)
                finish()
                startActivity(intent)
                return true
            }
        }

        return true
    }


}

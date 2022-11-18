package com.example.talksafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Chat : AppCompatActivity() {
    private lateinit var chatView: RecyclerView
    private lateinit var bigTimerView: TextView
    private lateinit var msgBox: EditText
    private lateinit var msgTimer: EditText
    private lateinit var sendBtn: ImageView
    private lateinit var msgAdapter: MessageAdapter
    private lateinit var msgList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    var receiverRoom: String? = null
    var senderRoom: String? = null
    var isTimeLimitSet: Boolean = false
    var msgCountDown: Int = 0

    var rName: String? = null
    var SID: String? = null
    var RID: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rName = intent.getStringExtra("name")
        RID = intent.getStringExtra("uid")

        SID = FirebaseAuth.getInstance().currentUser?.uid


        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = RID + SID
        receiverRoom = SID + RID
        supportActionBar?.title = rName

        chatView = findViewById(R.id.chat_view)
        bigTimerView = findViewById(R.id.big_timer)
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
                        val msg = ps.getValue(Message::class.java)
                        val msgID = ps.key.toString()
                        msgList.add(msg!!)

                        if (msg.timed == true && msgCountDown == 0 && msg.senderID == RID) {
                            val temp = msg.timeLimit!!.toLong().times(1000)
                            msgCountDown = msg.timeLimit!!.toInt()
                            isTimeLimitSet = true
                            object : CountDownTimer(temp, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    val builder = StringBuilder()
                                    builder.append("Message: ")
                                        .append(msg.message)
                                        .append(", Time: ")
                                        .append(msgCountDown)

                                    bigTimerView.text = builder
                                    msgCountDown -= 1
                                }

                                override fun onFinish() {
                                    msgCountDown = 0
                                    bigTimerView.text = "Message: None, Time: None"

                                    mDbRef.child("chat").child(senderRoom!!).child("messages")
                                        .child(msgID).removeValue()

                                    isTimeLimitSet = false
                                }
                            }.start()
                        }
                    }
                    msgAdapter.notifyDataSetChanged()
                }


                override fun onCancelled(error: DatabaseError) {

                }
            })

        // adding message to database
        sendBtn.setOnClickListener {
            val message = msgBox.text.toString()
            val timer = msgTimer.text.toString()

            if (message.isNotEmpty()) {

                if (!timer.isDigitsOnly()) {
                    Toast.makeText(
                        this@Chat,
                        "Only accept integer input",
                        Toast.LENGTH_SHORT
                    ).show()
                    msgTimer.setText("")
                } else if (timer.isNotEmpty() && (timer.toInt() < 10 || timer.toInt() > 60)) {
                    Toast.makeText(
                        this@Chat,
                        "Input out of range! (10 - 60)",
                        Toast.LENGTH_SHORT
                    ).show()
                    msgTimer.setText("")
                } else {
                    val messageObj = Message(message, SID, false)
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
                }
            } else {
                Toast.makeText(
                    this@Chat,
                    "Please type a message before sending it",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            R.id.exportChat -> {
                saveTextFile()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTextFile() {

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        val filename = "$formatted.txt"
        val filepath = "TalkSafe/messages"


        val list = ArrayList<String>()

        var meg: String
        var fileContent = ""
        mDbRef.child("chat").child(receiverRoom!!).child("messages").get().addOnSuccessListener {
            if (it.exists()) {
                for (ps in it.children) {

                    if ((ps.child("senderID").value).toString() == SID) {
                        meg = "You: "
                        meg += (ps.child("message").value).toString()
                        list.add(meg)

                    } else {
                        meg = rName.toString()
                        meg += ": "
                        meg += (ps.child("message").value).toString()
                        list.add(meg)
                    }
                }

                fileContent += list.joinToString(
                    prefix = "",
                    separator = "\r\n",
                    postfix = "",
                )
                if (isStoragePermissionGranted()) {
                    if (fileContent != "") {
                        val myExternalFile = File(getExternalFilesDir(filepath), filename)
                        val fos: FileOutputStream?
                        try {
                            // Instantiate the FileOutputStream object and pass myExternalFile in constructor
                            fos = FileOutputStream(myExternalFile)
                            // Write to the file

                            fos.write(fileContent.toByteArray())

                            // Close the stream
                            fos.close()
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        Toast.makeText(
                            this@Chat,
                            "Information saved to SD card",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@Chat,
                    "There's no chat to be exported",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }


    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                //Permission is granted
                true
            } else {
                //Permission is revoked
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            //Permission is granted
            true
        }
    }

}


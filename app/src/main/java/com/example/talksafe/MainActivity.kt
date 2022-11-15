package com.example.talksafe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var userView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var tEmail: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnRemove: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        userList = ArrayList()
        adapter = UserAdapter(this, userList)
        userView = findViewById(R.id.user_view)
        tEmail = findViewById(R.id.t_email)
        btnSearch = findViewById(R.id.btn_search)
        btnRemove = findViewById(R.id.btn_remove)
        userView.layoutManager = LinearLayoutManager(this)
        userView.adapter = adapter
        userList.clear()
        getFriends()

        btnSearch.setOnClickListener {
            val email = tEmail.text.toString()
            checkAddFriend(email)
        }

        btnRemove.setOnClickListener {
            val email = tEmail.text.toString()
            checkRemoveFriend(email)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                mAuth.signOut()
                val intent = Intent(this@MainActivity, LogIn::class.java)
                finish()
                startActivity(intent)
                return true
            }
            R.id.removeAllFd -> {
                removeAllFriends()
                val intent = Intent(this@MainActivity, MainActivity::class.java)
                finish()
                startActivity(intent)
                return true
            }
        }


        return true
    }

    private fun checkRemoveFriend(email: String) {
        if (friendExists(email)) {
            for (ps in userList) {
                if (ps.email == email) {
                    userList.remove(ps)
                    mDbRef.child("user").child(mAuth.uid!!).child("friends").setValue(userList)
                }
            }
            adapter.notifyDataSetChanged()

        } else {
            Toast.makeText(
                this@MainActivity,
                "This user is not in your friend list",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun checkAddFriend(email: String) {
        var exist = true
        if (checkInput(email)) {
            mDbRef.child("user")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        exist = false
                        for (ps in snapshot.children) {
                            val chkUser = ps.getValue(User::class.java)
                            if (chkUser?.email == email) {
                                exist = true

                                if (friendExists(chkUser.email)) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Friend " + chkUser.name + " is already in your friend list",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                } else {
                                    userList.add(chkUser)
                                    mDbRef.child("user").child(mAuth.uid!!).child("friends")
                                        .setValue(userList)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Friend " + chkUser.name + " added to your friend list successfully",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    adapter.notifyDataSetChanged()
                                }

                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@MainActivity,
                            "Database Error",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })
        }

        if (!exist) {
            Toast.makeText(
                this@MainActivity,
                "No user found with this email",
                Toast.LENGTH_SHORT
            )
                .show()
        }

    }

    private fun checkInput(input: String): Boolean {
        var valid = false
        when (input) {
            "" -> {
                Toast.makeText(this@MainActivity, "Please enter user email", Toast.LENGTH_SHORT)
                    .show()
            }
            mAuth.currentUser?.email -> {
                Toast.makeText(
                    this@MainActivity,
                    "You can't add yourself as a friend",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            else -> {
                valid = true
            }
        }
        return valid
    }

    private fun removeAllFriends() {
        mDbRef.child("user").child(mAuth.uid!!).child("friends")
            .removeValue()
    }

    private fun getFriends() {
        mDbRef.child("user").child(mAuth.uid!!).child("friends")
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ps in snapshot.children) {
                        val chkUser = ps.getValue(User::class.java)
                        userList.add(chkUser!!)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun friendExists(email: String?): Boolean {
        var exist = false

        for (ps in userList) {
            if (ps.email == email) {
                exist = true
                break
            }
        }
        return exist
    }


}
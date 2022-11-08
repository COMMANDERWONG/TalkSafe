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
    private lateinit var btnSearch:Button

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
        userView.layoutManager = LinearLayoutManager(this)
        userView.adapter = adapter
        btnSearch.setOnClickListener {
            val email = tEmail.text.toString()
            if (email == "") {
                Toast.makeText(this@MainActivity, "Please enter user email", Toast.LENGTH_SHORT)
                    .show()
            } else {
                mDbRef.child("user").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var userExist = false
                        for(ps in snapshot.children){
                            val currentUser = ps.getValue(User::class.java)
                            if (currentUser?.email == email) {
                                userList.add(currentUser!!)
                                userExist = true
                            }
                        }
                        if(userExist){
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this@MainActivity, "No user found with this email", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Database Error", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            mAuth.signOut()
            val intent = Intent(this@MainActivity, LogIn::class.java)
            finish()
            startActivity(intent)
            return true
        }
        return true
    }

//    private fun getFriends(userList: ArrayList<User>){
//
//        userList.add()
//    }
}
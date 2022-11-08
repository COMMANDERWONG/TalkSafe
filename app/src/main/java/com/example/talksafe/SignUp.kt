package com.example.talksafe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    private lateinit var tEmail: EditText
    private lateinit var tPassword: EditText
    private lateinit var tUsername: EditText
    private lateinit var bSignUp: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        mAuth = FirebaseAuth.getInstance()

        tEmail = findViewById(R.id.t_email)
        tPassword = findViewById(R.id.t_password)
        tUsername = findViewById(R.id.t_username)
        bSignUp = findViewById(R.id.b_signUp)

        bSignUp.setOnClickListener {
            val name = tUsername.text.toString()
            val email = tEmail.text.toString()
            val password = tPassword.text.toString()
            val friendList:ArrayList<String> = ArrayList()
            signUp(name, email, password,friendList)
        }


    }

    private fun signUp(name: String, email: String, password: String, friendList:ArrayList<String>) {

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, jump to home
                    addToDatabase(name, email, mAuth.currentUser?.uid!!)
                    val intent = Intent(this@SignUp, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        this@SignUp,
                        "Error occurred, Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()


                }
            }
    }

    private fun addToDatabase(name: String, email: String, uid: String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(uid).setValue(User(name,email,uid))

    }
}
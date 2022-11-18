package com.example.talksafe

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            signUp(name, email, password)
        }


    }

    private fun signUp(name: String, email: String, password: String) {

        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, jump to home
                        addToDatabase(name, email, mAuth.currentUser?.uid!!)
                        val intent = Intent(this@SignUp, MainActivity::class.java)
                        finish()
                        startActivity(intent)
                    } else if (password.length < 6) {
                        Toast.makeText(
                            this@SignUp,
                            "Password must consist at least 6 characters",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (!isValidEmail(email)){
                        Toast.makeText(
                            this@SignUp,
                            "Please enter a valid email",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@SignUp,
                            "This email is already used by another account",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(
                this@SignUp,
                "Please fill in all fields",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun addToDatabase(name: String, email: String, uid: String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(uid).setValue(User(name, email, uid))

    }
}
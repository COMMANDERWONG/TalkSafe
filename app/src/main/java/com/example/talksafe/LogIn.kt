package com.example.talksafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LogIn : AppCompatActivity() {
    private lateinit var tEmail: EditText
    private lateinit var tPassword: EditText
    private lateinit var bLogIn: Button
    private lateinit var bSignUp: Button
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        mAuth = FirebaseAuth.getInstance()

        tEmail = findViewById(R.id.t_email)
        tPassword = findViewById(R.id.t_password)
        bLogIn = findViewById(R.id.b_logIn)
        bSignUp = findViewById(R.id.b_signUp)


        bSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            finish()
            startActivity(intent)
        }

        bLogIn.setOnClickListener {
            val email = tEmail.text.toString()
            val password = tPassword.text.toString()
            if (email == "" || password == "") {
                Toast.makeText(this@LogIn, "Please enter email and password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                loginAuth(email, password)
            }
        }
    }

    private fun loginAuth(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@LogIn, MainActivity::class.java)
                    startActivity(intent)

                } else {
                    Toast.makeText(this@LogIn, "Email or password error", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
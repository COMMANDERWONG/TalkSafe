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
    private lateinit var bForget: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        mAuth = FirebaseAuth.getInstance()

        tEmail = findViewById(R.id.t_email)
        tPassword = findViewById(R.id.t_password)
        bLogIn = findViewById(R.id.b_logIn)
        bSignUp = findViewById(R.id.b_signUp)
        bForget = findViewById(R.id.b_forget)



        bSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            finish()
            startActivity(intent)
        }

        bLogIn.setOnClickListener {
            email = tEmail.text.toString()
            password = tPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@LogIn, "Please enter email and password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                loginAuth(email, password)
            }
        }

        bForget.setOnClickListener {
            email = tEmail.text.toString()
            password = tPassword.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(
                    this@LogIn,
                    "Please enter email to reset your password",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                forgetPassword(email)
            }
        }

    }

    private fun forgetPassword(email: String){
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener { it ->
            if (it.isSuccessful) {
                Toast.makeText(
                    this@LogIn,
                    "Please visit your email to reset your password",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@LogIn,
                    "Account with entered email not found",
                    Toast.LENGTH_SHORT
                ).show()
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
                    Toast.makeText(this@LogIn, "Wrong email or password", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
package com.example.caquickpoll

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerText: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 🔥 AUTO LOGIN
        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        setContentView(R.layout.activity_login)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)
        registerText = findViewById(R.id.registerText)

        // 🔥 LOGIN BUTTON
        loginBtn.setOnClickListener {

            val userEmail = email.text.toString().trim()
            val userPass = password.text.toString().trim()

            // Validation
            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                toast("Enter valid email")
                return@setOnClickListener
            }

            if (userPass.isEmpty()) {
                toast("Enter password")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener {

                    if (it.isSuccessful) {

                        val currentUser = auth.currentUser

                        if (currentUser != null && currentUser.isEmailVerified) {

                            toast("Login Successful ✅")
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()

                        } else {
                            toast("Please verify your email 📩")
                            auth.signOut()
                        }

                    } else {
                        val error = it.exception?.message ?: ""

                        when {
                            error.contains("no user record", true) ->
                                toast("User not found. Please register")

                            error.contains("password is invalid", true) ->
                                toast("Wrong password")

                            else ->
                                toast("Login failed")
                        }
                    }
                }
        }

        // 🔥 GO TO REGISTER
        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
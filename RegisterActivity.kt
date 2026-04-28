package com.example.caquickpoll

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerBtn: Button
    private lateinit var resendVerify: TextView
    private lateinit var loginText: TextView

    private lateinit var auth: FirebaseAuth

    private var lastEmail: String = ""
    private var lastPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // 🔥 SAFE VIEW BINDING
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        registerBtn = findViewById(R.id.registerBtn)
        resendVerify = findViewById(R.id.resendVerify)
        loginText = findViewById(R.id.loginText)

        // 🔥 REGISTER
        registerBtn.setOnClickListener {

            val userEmail = email.text.toString().trim()
            val userPass = password.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                toast("Enter valid email")
                return@setOnClickListener
            }

            if (userPass.length < 6) {
                toast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            lastEmail = userEmail
            lastPassword = userPass

            auth.createUserWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val user = auth.currentUser

                        user?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                toast("Verification email sent 📩")
                                auth.signOut()
                            }
                            ?.addOnFailureListener {
                                toast("Failed to send email")
                            }

                    } else {
                        toast(task.exception?.message ?: "Registration Failed")
                    }
                }
        }

        // 🔥 RESEND VERIFICATION
        resendVerify.setOnClickListener {

            if (lastEmail.isEmpty() || lastPassword.isEmpty()) {
                toast("Register first")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(lastEmail, lastPassword)
                .addOnSuccessListener {

                    val user = auth.currentUser

                    user?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            toast("Verification sent again 📩")
                            auth.signOut()
                        }
                        ?.addOnFailureListener {
                            toast("Failed to resend")
                        }
                }
                .addOnFailureListener {
                    toast("Error resending email")
                }
        }

        // 🔥 LOGIN REDIRECT
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
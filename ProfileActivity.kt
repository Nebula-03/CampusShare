package com.example.caquickpoll

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var saveUsernameBtn: Button
    private lateinit var emailText: TextView
    private lateinit var passwordField: EditText
    private lateinit var changePasswordBtn: Button
    private lateinit var savePasswordBtn: Button
    private lateinit var logoutBtn: Button
    private lateinit var darkSwitch: Switch
    private lateinit var root: LinearLayout

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 🔹 INIT VIEWS
        username = findViewById(R.id.username)
        saveUsernameBtn = findViewById(R.id.saveUsernameBtn)
        emailText = findViewById(R.id.emailText)
        passwordField = findViewById(R.id.passwordField)
        changePasswordBtn = findViewById(R.id.changePasswordBtn)
        savePasswordBtn = findViewById(R.id.savePasswordBtn)
        logoutBtn = findViewById(R.id.logoutBtn)
        darkSwitch = findViewById(R.id.darkModeSwitch)
        root = findViewById(R.id.profileRoot)

        val user = auth.currentUser


        emailText.text = user?.email


        loadUsername()
        val sharedPref = getSharedPreferences("settings", MODE_PRIVATE)

        val isDark = sharedPref.getBoolean("darkMode", false)
        darkSwitch.isChecked = isDark
        applyTheme(isDark)

        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("darkMode", isChecked).apply()
            applyTheme(isChecked)
        }


        saveUsernameBtn.setOnClickListener {

            val name = username.text.toString()

            if (name.isEmpty()) {
                toast("Enter username")
                return@setOnClickListener
            }

            db.collection("Users")
                .document(user!!.uid)
                .set(mapOf("username" to name))
                .addOnSuccessListener {
                    toast("Username saved ✅")
                }
        }


        changePasswordBtn.setOnClickListener {
            passwordField.isEnabled = true
            passwordField.setText("")
            savePasswordBtn.visibility = Button.VISIBLE
        }


        savePasswordBtn.setOnClickListener {

            val newPassword = passwordField.text.toString()

            if (newPassword.length < 6) {
                toast("Password must be 6+ chars")
                return@setOnClickListener
            }

            val currentPasswordInput = EditText(this)
            currentPasswordInput.hint = "Enter current password"

            android.app.AlertDialog.Builder(this)
                .setTitle("Re-authenticate")
                .setView(currentPasswordInput)
                .setPositiveButton("Confirm") { _, _ ->

                    val currentPassword = currentPasswordInput.text.toString()

                    if (currentPassword.isEmpty()) {
                        toast("Enter current password")
                        return@setPositiveButton
                    }

                    val user = auth.currentUser!!
                    val email = user.email!!

                    val credential = EmailAuthProvider
                        .getCredential(email, currentPassword)


                    user.reauthenticate(credential)
                        .addOnSuccessListener {


                            user.updatePassword(newPassword)
                                .addOnSuccessListener {

                                    toast("Password changed. Login again 🔐")

                                    auth.signOut()

                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    toast("Failed to update password")
                                }
                        }
                        .addOnFailureListener {
                            toast("Wrong current password ❌")
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


        logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


    private fun loadUsername() {

        val user = auth.currentUser!!

        db.collection("Users").document(user.uid).get()
            .addOnSuccessListener {

                if (it.exists()) {
                    username.setText(it.getString("username"))
                }
            }
    }


    private fun applyTheme(isDark: Boolean) {

        if (isDark) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            )
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
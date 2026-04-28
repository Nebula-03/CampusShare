package com.example.caquickpoll

import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val profileBtn = findViewById<ImageView>(R.id.profileBtn)
        val create = findViewById<LinearLayout>(R.id.createCard)
        val vote = findViewById<LinearLayout>(R.id.draftCard)
        val result = findViewById<LinearLayout>(R.id.resultCard)
        val myPolls = findViewById<LinearLayout>(R.id.myPollsCard)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        myPolls.setOnClickListener {
            startActivity(Intent(this, MyPollsActivity::class.java))
        }

        create.setOnClickListener {
            startActivity(Intent(this, CreatePollActivity::class.java))
        }

        vote.setOnClickListener {
            startActivity(Intent(this, DraftActivity::class.java))
        }

        result.setOnClickListener {
            startActivity(Intent(this, ResultActivity::class.java))
        }

        val enterPoll = findViewById<LinearLayout>(R.id.enterPollCard)

        enterPoll.setOnClickListener {
            startActivity(Intent(this, EnterPollActivity::class.java))
        }
    }

    private val proximityListener = object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) {

            val distance = event.values[0]

            // if near
            if (distance < proximitySensor!!.maximumRange) {

                Toast.makeText(applicationContext, "Phone too close to eyes 👀", Toast.LENGTH_SHORT).show()

                vibratePhone()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onResume() {
        super.onResume()
        proximitySensor?.also {
            sensorManager.registerListener(proximityListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(proximityListener)
    }

    private fun vibratePhone() {

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(500)
        }
    }
}
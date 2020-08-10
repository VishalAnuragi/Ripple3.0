package com.example.ripple20.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ripple20.R
import com.example.ripple20.Services.TimerService
import com.example.ripple20.fragments.SongPlayingFragment


internal class TimerActivity : AppCompatActivity()  {

    var time :TextView ?= null
    var time_input : EditText ?= null
    var time_minutes : String ?= null
    var time_seconds : Int ?= null
    var time_seconds2 : Int ?= null
    var timer_start_btn1 : Button ?= null

    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        time = findViewById<EditText>(R.id.timer_text_minutes)
        timer_start_btn1 = findViewById(R.id.timer_start_btn)
        time_input = findViewById<EditText>(R.id.timer_minutes_input)



        timer_start_btn1?.setOnClickListener( View.OnClickListener {


           time_minutes =  time_input?.text.toString()
            time_seconds = Integer.parseInt(time_minutes!!)*60
            time_seconds2 = time_seconds
            val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val editor = preferences.edit()
            editor.putString("TIME_SECONDS" , time_seconds.toString())
            editor?.apply()

            startService(Intent(this, TimerService::class.java))

           mToastRunnable.run()
        })

    }


    private val mToastRunnable = object:Runnable {
        override fun run() {

           time_seconds2 = time_seconds2?.minus(1)
            if (time_seconds2 == 0){
            SongPlayingFragment.Statified.stop = "STOP"
            }
            time?.text = time_seconds2.toString()
            mHandler.postDelayed( this , 1000)
        }
    }


}
package com.example.ripple20.fragments

import android.R.attr.button
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.example.ripple20.R
import com.example.ripple20.activities.TimerActivity


class SettingsFragment : Fragment() {

    var sleep_timer : RelativeLayout ?= null

    object Statified {
        var MY_PREFS_NAME = "ShakeFeature"
    }

    var myActivity: Activity? = null
    var shakeSwitch: Switch? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        activity?.title = "Settings"

        var view = inflater!!.inflate(R.layout.fragment_settings, container, false)

        shakeSwitch = view?.findViewById(R.id.switchShake)
        sleep_timer = view?.findViewById(R.id.sleeptimer)
        return view


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val prefs = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
        val isAllowed = prefs?.getBoolean("feature", false)
        if (isAllowed as Boolean) {
            shakeSwitch?.isChecked = true
        } else {
            shakeSwitch?.isChecked = false
        }
        shakeSwitch?.setOnCheckedChangeListener({ CompoundButton, b ->
            if (b) {
                val editor = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", true)
                editor?.apply()
            } else {

                val editor = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", false)
                editor?.apply()

            }
        })

        sleep_timer?.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, TimerActivity::class.java)
            startActivity(intent)
        })

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        var item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }
}
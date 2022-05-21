package com.example.notedapp.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.notedapp.R
import me.relex.circleindicator.CircleIndicator3

class OnBoarding : AppCompatActivity() {
    private val fragmentList = ArrayList<Fragment>()
    private lateinit var viewPager: ViewPager2
    private lateinit var indicator: CircleIndicator3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val PREFS_NAME = "MyPrefsFile"

        val settings = getSharedPreferences(PREFS_NAME, 0)

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            // first time task
            setContentView(R.layout.onboarding)
            castView()
            fragmentList.add(Fragment1())
            fragmentList.add(Fragment2())
            fragmentList.add(Fragment3())
            fragmentList.add(Fragment4())
            fragmentList.add(Fragment5())
            fragmentList.add(FragmentLast())
            viewPager.adapter = ViewPager2FragmentAdapter(this, fragmentList)
            viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            indicator.setViewPager(viewPager)

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).apply()
        } else{
            start(null)
        }
    }
    private fun castView() {
        viewPager = findViewById(R.id.view_pager2)
        indicator = findViewById(R.id.indicator)
    }

    private fun start(view: View?){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


}
package com.example.avjindersinghsekhon.minimaltodo

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v4.content.res.ResourcesCompat.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem

class SettingsActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val theme = getSharedPreferences(MainActivity.THEME_PREFERENCES, Context.MODE_PRIVATE).getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME)
        when(theme) {
            MainActivity.LIGHTTHEME -> setTheme(R.style.CustomStyle_LightTheme)
            else -> setTheme(R.style.CustomStyle_DarkTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val backArrow = getDrawable(resources, R.drawable.abc_ic_ab_back_material,null)
        backArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

        supportActionBar?.let{
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(backArrow)
        }

        val fm = fragmentManager
        fm.beginTransaction().replace(R.id.mycontent, SettingsFragment()).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.getParentActivityName(this)?.let{NavUtils.navigateUpFromSameTask(this)}
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}

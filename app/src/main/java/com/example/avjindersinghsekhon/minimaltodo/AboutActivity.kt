package com.example.avjindersinghsekhon.minimaltodo

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v4.content.res.ResourcesCompat.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.TextView

class AboutActivity : AppCompatActivity() {
    private var mVersionTextView: TextView? = null
    private var appVersion = "0.1"
    private var toolbar: Toolbar? = null
    private var contactMe: TextView? = null
    internal var theme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        theme = getSharedPreferences(MainActivity.THEME_PREFERENCES, Context.MODE_PRIVATE).getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME)
        when (theme) {
            MainActivity.DARKTHEME -> {
                Log.d("OskarSchindler", "One")
                setTheme(R.style.CustomStyle_DarkTheme)
            }
            else -> {
                Log.d("OskarSchindler", "One")
                setTheme(R.style.CustomStyle_LightTheme)
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_layout)

        val backArrow = getDrawable(resources, R.drawable.abc_ic_ab_back_material,null)
        backArrow?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        try {
            val info = packageManager.getPackageInfo(packageName, 0)
            appVersion = info.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }


        mVersionTextView = findViewById(R.id.aboutVersionTextView) as TextView
        mVersionTextView!!.text = String.format(resources.getString(R.string.app_version), appVersion)
        toolbar = findViewById(R.id.toolbar) as Toolbar
        contactMe = findViewById(R.id.aboutContactMe) as TextView

        setSupportActionBar(toolbar)
        supportActionBar.let{
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(backArrow)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                NavUtils.getParentActivityName(this)?.let {
                    NavUtils.navigateUpFromSameTask(this)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

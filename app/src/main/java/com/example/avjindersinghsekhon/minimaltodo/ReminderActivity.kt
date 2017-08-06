package com.example.avjindersinghsekhon.minimaltodo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import fr.ganfra.materialspinner.MaterialSpinner
import org.json.JSONException
import java.io.IOException
import java.util.*

class ReminderActivity : AppCompatActivity() {

    private lateinit var mSnoozeSpinner: MaterialSpinner
    private lateinit var storeRetrieveData: StoreRetrieveData
    private lateinit var mToDoItems: ArrayList<ToDoItem>
    private lateinit var mItem: ToDoItem

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = getSharedPreferences(MainActivity.THEME_PREFERENCES, Context.MODE_PRIVATE).getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME)
        when (theme) {
            MainActivity.LIGHTTHEME -> setTheme(R.style.CustomStyle_LightTheme)
            else -> setTheme(R.style.CustomStyle_DarkTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reminder_layout)
        storeRetrieveData = StoreRetrieveData(this, MainActivity.FILENAME)
        mToDoItems = MainActivity.getLocallyStoredData(storeRetrieveData)

        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)


        val i = intent
        val id = i.getSerializableExtra(TodoNotificationService.TODOUUID) as UUID
        val mItem = mToDoItems.firstOrNull { it.identifier == id }
        val snoozeOptionsArray = resources.getStringArray(R.array.snooze_options)

        val mRemoveToDoButton = findViewById(R.id.toDoReminderRemoveButton) as Button
        val mtoDoTextTextView = findViewById(R.id.toDoReminderTextViewBody) as TextView
        val mSnoozeTextView = findViewById(R.id.reminderViewSnoozeTextView) as TextView
        mSnoozeSpinner = findViewById(R.id.todoReminderSnoozeSpinner) as MaterialSpinner

        mtoDoTextTextView.text = mItem!!.toDoText

        when (theme) {
            MainActivity.LIGHTTHEME -> mSnoozeTextView.setTextColor(ResourcesCompat.getColor(resources, R.color.secondary_text, null))
            else -> {
                mSnoozeTextView.setTextColor(Color.WHITE)
                mSnoozeTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_snooze_white_24dp, 0, 0, 0
                )
            }
        }

        mRemoveToDoButton.setOnClickListener {
            mToDoItems.remove(mItem)
            changeOccurred()
            saveData()
            closeApp()
        }


        val adapter = ArrayAdapter(this, R.layout.spinner_text_view, snoozeOptionsArray!!)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        mSnoozeSpinner.adapter = adapter

    }

    private fun closeApp() {
        val i = Intent(this@ReminderActivity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_DATA_SET_CHANGED, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(EXIT, true)
        editor.apply()
        startActivity(i)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reminder, menu)
        return true
    }

    private fun changeOccurred() {
        val sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_DATA_SET_CHANGED, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(MainActivity.CHANGE_OCCURED, true)
        editor.apply()
    }

    private fun addTimeToDate(min: Int): Date {
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, min)
        return calendar.time
    }

    private fun valueFromSpinner(): Int {
        when (mSnoozeSpinner.selectedItemPosition) {
            0 -> return 10
            1 -> return 30
            2 -> return 60
            else -> return 0
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toDoReminderDoneMenuItem -> {
                val date = addTimeToDate(valueFromSpinner())
                mItem.toDoDate = date
                mItem.setHasReminder(true)
                Log.d("OskarSchindler", "Date Changed to: " + date)
                changeOccurred()
                saveData()
                closeApp()
                //foo
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun saveData() {
        try {
            storeRetrieveData.saveToFile(mToDoItems)
        } catch (e: Exception) {
            when (e) {
                is JSONException,
                is IOException -> e.printStackTrace()
                else -> throw e
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        val EXIT = "com.avjindersekhon.exit"
    }
}


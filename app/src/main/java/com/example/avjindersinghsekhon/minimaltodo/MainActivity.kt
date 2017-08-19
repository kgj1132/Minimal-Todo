package com.example.avjindersinghsekhon.minimaltodo

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.content.res.ResourcesCompat.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator

import org.json.JSONException

import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import java.util.Date

class MainActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerViewEmptySupport? = null
    private var mAddToDoItemFAB: FloatingActionButton? = null
    private var mToDoItemsArrayList: ArrayList<ToDoItem>?=null
    private var mCoordLayout: CoordinatorLayout? = null
    private var adapter: BasicListAdapter? = null
    private var mJustDeletedToDoItem: ToDoItem? = null
    private var mIndexOfDeletedToDoItem: Int = 0
    private var storeRetrieveData: StoreRetrieveData? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var customRecyclerScrollViewListener: CustomRecyclerScrollViewListener? = null
    private var mTheme = -1
    private var theme: String? = "name_of_the_theme"
    private val testStrings = arrayOf("Clean my room", "Water the plants", "Get car washed", "Get my dry cleaning")

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(ReminderActivity.EXIT, false)) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(ReminderActivity.EXIT, false)
            editor.apply()
            finish()
        }
        /*
        We need to do this, as this activity's onCreate won't be called when coming back from SettingsActivity,
        thus our changes to dark/light mode won't take place, as the setContentView() is not called again.
        So, inside our SettingsFragment, whenever the checkbox's value is changed, in our shared preferences,
        we mark our recreate_activity key as true.

        Note: the recreate_key's value is changed to false before calling recreate(), or we woudl have ended up in an infinite loop,
        as onResume() will be called on recreation, which will again call recreate() and so on....
        and get an ANR

         */
        if (getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE).getBoolean(RECREATE_ACTIVITY, false)) {
            val editor = getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE).edit()
            editor.putBoolean(RECREATE_ACTIVITY, false)
            editor.apply()
            recreate()
        }


    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(CHANGE_OCCURED, false)) {

            mToDoItemsArrayList = getLocallyStoredData(storeRetrieveData)
            adapter = BasicListAdapter(mToDoItemsArrayList!!)
            mRecyclerView!!.adapter = adapter
            setAlarms()

            val editor = sharedPreferences.edit()
            editor.putBoolean(CHANGE_OCCURED, false)
            //            editor.commit();
            editor.apply()


        }
    }

    private fun setAlarms() {
        if (mToDoItemsArrayList != null) {
            for (item in mToDoItemsArrayList!!) {
                if (item.hasReminder() && item.toDoDate != null) {
                    if (item.toDoDate.before(Date())) {
                        item.toDoDate = null
                        continue
                    }
                    val i = Intent(this, TodoNotificationService::class.java)
                    i.putExtra(TodoNotificationService.TODOUUID, item.identifier)
                    i.putExtra(TodoNotificationService.TODOTEXT, item.toDoText)
                    createAlarm(i, item.identifier.hashCode(), item.toDoDate.time)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //We recover the theme we've set and setTheme accordingly
        theme = getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE).getString(THEME_SAVED, LIGHTTHEME)

        if (theme == LIGHTTHEME) {
            mTheme = R.style.CustomStyle_LightTheme
        } else {
            mTheme = R.style.CustomStyle_DarkTheme
        }
        this.setTheme(mTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val sharedPreferences = getSharedPreferences(SHARED_PREF_DATA_SET_CHANGED, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(CHANGE_OCCURED, false)
        editor.apply()

        storeRetrieveData = StoreRetrieveData(this, FILENAME)
        mToDoItemsArrayList = getLocallyStoredData(storeRetrieveData)
        adapter = BasicListAdapter(mToDoItemsArrayList!!)
        setAlarms()


        //        adapter.notifyDataSetChanged();
        //        storeRetrieveData = new StoreRetrieveData(this, FILENAME);
        //
        //        try {
        //            mToDoItemsArrayList = storeRetrieveData.loadFromFile();
        ////            Log.d("OskarSchindler", "Arraylist Length: "+mToDoItemsArrayList.size());
        //        } catch (IOException | JSONException e) {
        ////            Log.d("OskarSchindler", "IOException received");
        //            e.printStackTrace();
        //        }
        //
        //        if(mToDoItemsArrayList==null){
        //            mToDoItemsArrayList = new ArrayList<>();
        //        }
        //

        //        mToDoItemsArrayList = new ArrayList<>();
        //        makeUpItems(mToDoItemsArrayList, testStrings.length);

        val toolbar = findViewById(R.id.toolbar) as android.support.v7.widget.Toolbar
        setSupportActionBar(toolbar)



        mCoordLayout = findViewById(R.id.myCoordinatorLayout) as CoordinatorLayout
        mAddToDoItemFAB = findViewById(R.id.addToDoItemFAB) as FloatingActionButton

        mAddToDoItemFAB!!.setOnClickListener {
            val newTodo = Intent(this@MainActivity, AddToDoActivity::class.java)
            val item = ToDoItem("", false, null)
            val color = ColorGenerator.MATERIAL.randomColor
            item.todoColor = color

            //                String color = getResources().getString(R.color.primary_ligher);
            newTodo.putExtra(TODOITEM, item)
            //                View decorView = getWindow().getDecorView();
            //                View navView= decorView.findViewById(android.R.id.navigationBarBackground);
            //                View statusView = decorView.findViewById(android.R.id.statusBarBackground);
            //                Pair<View, String> navBar ;
            //                if(navView!=null){
            //                    navBar = Pair.create(navView, navView.getTransitionName());
            //                }
            //                else{
            //                    navBar = null;
            //                }
            //                Pair<View, String> statusBar= Pair.create(statusView, statusView.getTransitionName());
            //                ActivityOptions options;
            //                if(navBar!=null){
            //                    options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, navBar, statusBar);
            //                }
            //                else{
            //                    options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, statusBar);
            //                }

            //                startActivity(new Intent(MainActivity.this, TestLayout.class), options.toBundle());
            //                startActivityForResult(newTodo, REQUEST_ID_TODO_ITEM, options.toBundle());

            startActivityForResult(newTodo, REQUEST_ID_TODO_ITEM)
        }


        //        mRecyclerView = (RecyclerView)findViewById(R.id.toDoRecyclerView);
        mRecyclerView = findViewById(R.id.toDoRecyclerView) as RecyclerViewEmptySupport
        if (theme == LIGHTTHEME) {
            mRecyclerView!!.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.primary_lightest,null))
        }
        mRecyclerView!!.setEmptyView(findViewById(R.id.toDoEmptyView))
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)



        customRecyclerScrollViewListener = object : CustomRecyclerScrollViewListener() {
            override fun show() {

                mAddToDoItemFAB!!.animate().translationY(0f).setInterpolator(DecelerateInterpolator(2f)).start()
                //                mAddToDoItemFAB.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2.0f)).start();
            }

            override fun hide() {

                val lp = mAddToDoItemFAB!!.layoutParams as CoordinatorLayout.LayoutParams
                val fabMargin = lp.bottomMargin
                mAddToDoItemFAB!!.animate().translationY((mAddToDoItemFAB!!.height + fabMargin).toFloat()).setInterpolator(AccelerateInterpolator(2.0f)).start()
            }
        }
        mRecyclerView!!.addOnScrollListener(customRecyclerScrollViewListener)


        val callback = ItemTouchHelperClass(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper!!.attachToRecyclerView(mRecyclerView)


        mRecyclerView!!.adapter = adapter
        //        setUpTransitions();


    }

    fun addThemeToSharedPreferences(theme: String) {
        val sharedPreferences = getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(THEME_SAVED, theme)
        editor.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.aboutMeMenuItem -> {
                val i = Intent(this, AboutActivity::class.java)
                startActivity(i)
                return true
            }

            R.id.preferences -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode != Activity.RESULT_CANCELED && requestCode == REQUEST_ID_TODO_ITEM) {
            val item = data.getSerializableExtra(TODOITEM) as ToDoItem
            if (item.toDoText.isEmpty()) {
                return
            }
            var existed = false

            if (item.hasReminder() && item.toDoDate != null) {
                val i = Intent(this, TodoNotificationService::class.java)
                i.putExtra(TodoNotificationService.TODOTEXT, item.toDoText)
                i.putExtra(TodoNotificationService.TODOUUID, item.identifier)
                createAlarm(i, item.identifier.hashCode(), item.toDoDate.time)
                //                Log.d("OskarSchindler", "Alarm Created: "+item.getToDoText()+" at "+item.getToDoDate());
            }

            for (i in mToDoItemsArrayList!!.indices) {
                if (item.identifier == mToDoItemsArrayList!![i].identifier) {
                    mToDoItemsArrayList!![i] = item
                    existed = true
                    adapter!!.notifyDataSetChanged()
                    break
                }
            }
            if (!existed) {
                addToDataStore(item)
            }


        }
    }

    private val alarmManager: AlarmManager
        get() = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun doesPendingIntentExist(i: Intent, requestCode: Int): Boolean {
        val pi = PendingIntent.getService(this, requestCode, i, PendingIntent.FLAG_NO_CREATE)
        return pi != null
    }

    private fun createAlarm(i: Intent, requestCode: Int, timeInMillis: Long) {
        val am = alarmManager
        val pi = PendingIntent.getService(this, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT)
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi)
        //        Log.d("OskarSchindler", "createAlarm "+requestCode+" time: "+timeInMillis+" PI "+pi.toString());
    }

    private fun deleteAlarm(i: Intent, requestCode: Int) {
        if (doesPendingIntentExist(i, requestCode)) {
            val pi = PendingIntent.getService(this, requestCode, i, PendingIntent.FLAG_NO_CREATE)
            pi.cancel()
            alarmManager.cancel(pi)
            Log.d("OskarSchindler", "PI Cancelled " + doesPendingIntentExist(i, requestCode))
        }
    }

    private fun addToDataStore(item: ToDoItem) {
        mToDoItemsArrayList!!.add(item)
        adapter!!.notifyItemInserted(mToDoItemsArrayList!!.size - 1)

    }


    fun makeUpItems(items: ArrayList<ToDoItem>, len: Int) {
        for (testString in testStrings) {
            val item = ToDoItem(testString, false, Date())

            //            item.setTodoColor(getResources().getString(R.color.red_secondary));
            items.add(item)
        }

    }

    inner class BasicListAdapter internal constructor(private val items: ArrayList<ToDoItem>) : RecyclerView.Adapter<BasicListAdapter.ViewHolder>(), ItemTouchHelperClass.ItemTouchHelperAdapter {

        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            if (fromPosition < toPosition) {
                for (i in fromPosition..toPosition - 1) {
                    Collections.swap(items, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(items, i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemRemoved(position: Int) {
            //Remove this line if not using Google Analytics

            mJustDeletedToDoItem = items.removeAt(position)
            mIndexOfDeletedToDoItem = position
            val i = Intent(this@MainActivity, TodoNotificationService::class.java)
            deleteAlarm(i, mJustDeletedToDoItem!!.identifier.hashCode())
            notifyItemRemoved(position)

            //            String toShow = (mJustDeletedToDoItem.getToDoText().length()>20)?mJustDeletedToDoItem.getToDoText().substring(0, 20)+"...":mJustDeletedToDoItem.getToDoText();
            val toShow = "Todo"
            Snackbar.make(mCoordLayout!!, "Deleted " + toShow, Snackbar.LENGTH_SHORT)
                    .setAction("UNDO") {
                        //Comment the line below if not using Google Analytics
                        items.add(mIndexOfDeletedToDoItem, mJustDeletedToDoItem!!)
                        if (mJustDeletedToDoItem!!.toDoDate != null && mJustDeletedToDoItem!!.hasReminder()) {
                            Intent(this@MainActivity, TodoNotificationService::class.java)
                                    .putExtra(TodoNotificationService.TODOTEXT, mJustDeletedToDoItem!!.toDoText)
                                    .putExtra(TodoNotificationService.TODOUUID, mJustDeletedToDoItem!!.identifier)
                            createAlarm(i, mJustDeletedToDoItem!!.identifier.hashCode(), mJustDeletedToDoItem!!.toDoDate.time)
                        }
                        notifyItemInserted(mIndexOfDeletedToDoItem)
                    }.show()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasicListAdapter.ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.list_circle_try, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: BasicListAdapter.ViewHolder, position: Int) {
            val item = items[position]
            //            if(item.getToDoDate()!=null && item.getToDoDate().before(new Date())){
            //                item.setToDoDate(null);
            //            }
            val sharedPreferences = getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE)
            //Background color for each to-do item. Necessary for night/day mode
            val bgColor: Int
            //color of title text in our to-do item. White for night mode, dark gray for day mode
            val todoTextColor: Int
            if (sharedPreferences.getString(THEME_SAVED, LIGHTTHEME) == LIGHTTHEME) {
                bgColor = Color.WHITE
                todoTextColor = getColor(resources,R.color.secondary_text,null)
            } else {
                bgColor = Color.DKGRAY
                todoTextColor = Color.WHITE
            }
            holder.linearLayout.setBackgroundColor(bgColor)

            if (item.hasReminder() && item.toDoDate != null) {
                holder.mToDoTextview.maxLines = 1
                holder.mTimeTextView.visibility = View.VISIBLE
                //                holder.mToDoTextview.setVisibility(View.GONE);
            } else {
                holder.mTimeTextView.visibility = View.GONE
                holder.mToDoTextview.maxLines = 2
            }
            holder.mToDoTextview.text = item.toDoText
            holder.mToDoTextview.setTextColor(todoTextColor)
            //            holder.mColorTextView.setBackgroundColor(Color.parseColor(item.getTodoColor()));

            //            TextDrawable myDrawable = TextDrawable.builder().buildRoundRect(item.getToDoText().substring(0,1),Color.RED, 10);
            //We check if holder.color is set or not
            //            if(item.getTodoColor() == null){
            //                ColorGenerator generator = ColorGenerator.MATERIAL;
            //                int color = generator.getRandomColor();
            //                item.setTodoColor(color+"");
            //            }
            //            Log.d("OskarSchindler", "Color: "+item.getTodoColor());
            val myDrawable = TextDrawable.builder().beginConfig()
                    .textColor(Color.WHITE)
                    .useFont(Typeface.DEFAULT)
                    .toUpperCase()
                    .endConfig()
                    .buildRound(item.toDoText.substring(0, 1), item.todoColor)

            //            TextDrawable myDrawable = TextDrawable.builder().buildRound(item.getToDoText().substring(0,1),holder.color);
            holder.mColorImageView.setImageDrawable(myDrawable)
            item.toDoDate?.let{
                val timeToShow: String = if (android.text.format.DateFormat.is24HourFormat(this@MainActivity)) {
                    AddToDoActivity.formatDate(MainActivity.DATE_TIME_FORMAT_24_HOUR, item.toDoDate)
                } else {
                    AddToDoActivity.formatDate(MainActivity.DATE_TIME_FORMAT_12_HOUR, item.toDoDate)
                }
                holder.mTimeTextView.text = timeToShow
            }


        }

        override fun getItemCount(): Int=items.size

        inner class ViewHolder
        //            int color = -1;

        (mView: View) : RecyclerView.ViewHolder(mView) {
            internal var linearLayout: LinearLayout
            internal var mToDoTextview: TextView
            //            TextView mColorTextView;
            internal var mColorImageView: ImageView
            internal var mTimeTextView: TextView

            init {
                mView.setOnClickListener {
                    val item = items[this@ViewHolder.adapterPosition]
                    val i = Intent(this@MainActivity, AddToDoActivity::class.java)
                    i.putExtra(TODOITEM, item)
                    startActivityForResult(i, REQUEST_ID_TODO_ITEM)
                }
                mToDoTextview = mView.findViewById<View>(R.id.toDoListItemTextview) as TextView
                mTimeTextView = mView.findViewById<View>(R.id.todoListItemTimeTextView) as TextView
                //                mColorTextView = (TextView)v.findViewById(R.id.toDoColorTextView);
                mColorImageView = mView.findViewById<View>(R.id.toDoListItemColorImageView) as ImageView
                linearLayout = mView.findViewById<View>(R.id.listItemLinearLayout) as LinearLayout
            }


        }
    }

    //Used when using custom fonts
    //    @Override
    //    protected void attachBaseContext(Context newBase) {
    //        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    //    }

    private fun saveDate() {
        try {
            storeRetrieveData!!.saveToFile(mToDoItemsArrayList)
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onPause() {
        super.onPause()
        try {
            storeRetrieveData!!.saveToFile(mToDoItemsArrayList)
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    override fun onDestroy() {

        super.onDestroy()
        mRecyclerView!!.removeOnScrollListener(customRecyclerScrollViewListener)
    }


    companion object {
        @JvmField
        val TODOITEM = "com.avjindersinghsekhon.com.avjindersinghsekhon.minimaltodo.MainActivity"
        private val REQUEST_ID_TODO_ITEM = 100
        @JvmField
        val DATE_TIME_FORMAT_12_HOUR = "MMM d, yyyy  h:mm a"
        @JvmField
        val DATE_TIME_FORMAT_24_HOUR = "MMM d, yyyy  k:mm"
        @JvmField
        val FILENAME = "todoitems.json"
        @JvmField
        val SHARED_PREF_DATA_SET_CHANGED = "com.avjindersekhon.datasetchanged"
        @JvmField
        val CHANGE_OCCURED = "com.avjinder.changeoccured"
        @JvmField
        val THEME_PREFERENCES = "com.avjindersekhon.themepref"
        @JvmField
        val RECREATE_ACTIVITY = "com.avjindersekhon.recreateactivity"
        @JvmField
        val THEME_SAVED = "com.avjindersekhon.savedtheme"
        @JvmField
        val DARKTHEME = "com.avjindersekon.darktheme"
        @JvmField
        val LIGHTTHEME = "com.avjindersekon.lighttheme"

        @JvmStatic
        fun getLocallyStoredData(storeRetrieveData: StoreRetrieveData?): ArrayList<ToDoItem> {
            var items: ArrayList<ToDoItem> = ArrayList()

            try {
                items = storeRetrieveData!!.loadFromFile()

            } catch (e : Exception) {
                when(e) {
                    is IOException,
                    is JSONException -> e.printStackTrace()
                }
            }
            return items
        }
    }
}
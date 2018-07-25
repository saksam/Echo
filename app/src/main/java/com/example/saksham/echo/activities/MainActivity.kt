package com.example.saksham.echo.activities

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.example.saksham.echo.R
import com.example.saksham.echo.activities.MainActivity.Statified.drawerLayout
import com.example.saksham.echo.adapter.NavigationDrawerAdapter
import com.example.saksham.echo.fragment.MainScreenFragment
import com.example.saksham.echo.fragment.SongPlayingFragment

class MainActivity : AppCompatActivity() {

    var imagesForNavDrawer= intArrayOf(R.drawable.navigation_allsongs,R.drawable.navigation_favorites
        ,R.drawable.navigation_settings
        ,R.drawable.navigation_aboutus)

    object Statified{
        var drawerLayout:DrawerLayout?=null
        var notificationManager: NotificationManager?=null

    }

    var navigationDrawerIconsList:ArrayList<String> = arrayListOf()

    var trackNotificationBuilder: Notification?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout=findViewById(R.id.drawer_layout)

        navigationDrawerIconsList.add("All Songs")
        navigationDrawerIconsList.add("Favorites")
        navigationDrawerIconsList.add("Settings")
        navigationDrawerIconsList.add("About Us")



        val toggle=ActionBarDrawerToggle(this@MainActivity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()

        val mainScreenFragment= MainScreenFragment()
        this.supportFragmentManager
                .beginTransaction()
                .add(R.id.details_fragment,mainScreenFragment,"MainScreenFragment")
                .commit()

        var _navigationAdapter= NavigationDrawerAdapter(navigationDrawerIconsList,imagesForNavDrawer,this)
        _navigationAdapter.notifyDataSetChanged()

        var navigation_recycler_view=findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navigation_recycler_view.layoutManager = LinearLayoutManager(this)

        navigation_recycler_view.itemAnimator=DefaultItemAnimator()

        navigation_recycler_view.adapter=_navigationAdapter
        navigation_recycler_view.setHasFixedSize(true)

        val intent= Intent(this@MainActivity,MainActivity::class.java)
        var pIntent= PendingIntent.getActivity(this@MainActivity,System.currentTimeMillis().toInt(),
                intent, 0)
        trackNotificationBuilder=Notification.Builder(this)
                .setContentTitle("A Track is playing in background")
                .setSmallIcon(R.drawable.echo_logo)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .build()
        Statified.notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    }

    override fun onStart() {
        super.onStart()
        try{
            Statified.notificationManager?.cancel(1978)

        }catch(e:Exception){
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try{
            if(SongPlayingFragment.stat.mediaPlayer?.isPlaying as Boolean){
                Statified.notificationManager?.notify(1978,trackNotificationBuilder)
            }

        }catch(e:Exception){
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try{
            Statified.notificationManager?.cancel(1978)

        }catch(e:Exception){
            e.printStackTrace()
        }
    }
}

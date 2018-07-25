package com.example.saksham.echo.fragment


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.*
import com.example.saksham.echo.CurrentSongHelper
import com.example.saksham.echo.R
import com.example.saksham.echo.Songs
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.sql.Time
import java.util.*
import java.util.concurrent.TimeUnit
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.saksham.echo.activities.MainActivity
import com.example.saksham.echo.databases.EchoDatabase
import com.example.saksham.echo.fragment.SongPlayingFragment.stat.seekbar


/**
 * A simple [Fragment] subclass.
 */
class SongPlayingFragment : Fragment(), SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

        if(p2) {
            stat.seekbar?.setProgress(p1)
            stat.mediaPlayer?.seekTo(p1 as Int)
        }
    }
    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }


    object stat {

        var MY_PREFS_NAME = "ShakeFeature"
        var mediaPlayer: MediaPlayer? = null
        var myActivity: Activity? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null

        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playPauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var seekbar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var shuffleImageButton: ImageButton? = null
        var fab: ImageButton? = null

        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null

        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        var favoriteContent: EchoDatabase? = null

        var updateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = stat.mediaPlayer?.currentPosition
                stat.startTimeText?.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                        (TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong()) -
                                TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong()))
                )%60))
                stat.seekbar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this, 1000)
            }

        }
    }


    object staticated {
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun onSongComplete() {
            if (stat.currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                stat.currentSongHelper?.isPlaying = true
            } else {
                if (stat.currentSongHelper?.isLoop as Boolean) {

                    stat.currentSongHelper?.isPlaying = true
                    var nextSong = stat.fetchSongs?.get(stat.currentPosition)
                    stat.currentSongHelper?.songTitle = nextSong?.songTitle
                    stat.currentSongHelper?.songPath = nextSong?.songData
                    stat.currentSongHelper?.currentPosition = stat.currentPosition
                    stat.currentSongHelper?.songId = nextSong?.songID as Long

                    updateTextView(stat.currentSongHelper?.songTitle as String, stat.currentSongHelper?.songArtist as String)

                    stat.mediaPlayer?.reset()
                    try {
                        stat.mediaPlayer?.setDataSource(stat.myActivity, Uri.parse(stat.currentSongHelper?.songPath))
                        stat.mediaPlayer?.prepare()
                        stat.mediaPlayer?.start()
                        processInformation(stat.mediaPlayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    playNext("PlayNextNormal")
                    stat.currentSongHelper?.isPlaying = true
                }
            }
            if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_on))

            } else {
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_off))

            }
        }

        fun updateTextView(songTitle: String, songArtist: String) {
            var songT=songTitle
            var songA=songArtist
            if(songTitle.equals("<unknown>",true)){
                songT="unknown"
            }
            if(songArtist.equals("<unknown>",true)){
                songA="unknown"
            }
            stat.songTitleView?.setText(songT)
            stat.songArtistView?.setText(songA)
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            stat.seekbar?.max = finalTime
            stat.startTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong() as Long),
                    (TimeUnit.MILLISECONDS.toSeconds(startTime.toLong() as Long) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong() as Long)))%60))

            stat.endTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong() as Long) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong() as Long))))

            stat.seekbar?.setProgress(startTime)
            Handler().postDelayed(stat.updateSongTime, 0)
        }

        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                stat.currentPosition = stat.currentPosition + 1;
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(stat.fetchSongs?.size?.plus(1) as Int)
                stat.currentPosition = randomPosition
            }
            if (stat.currentPosition == stat.fetchSongs?.size) {
                stat.currentPosition = 0
            }

            stat.currentSongHelper?.isLoop = false

            var nextSong = stat.fetchSongs?.get(stat.currentPosition)
            stat.currentSongHelper?.songPath = nextSong?.songData
            stat.currentSongHelper?.songTitle = nextSong?.songTitle
            stat.currentSongHelper?.songId = nextSong?.songID as Long
            stat.currentSongHelper?.currentPosition = stat.currentPosition
            stat.currentSongHelper?.songArtist=nextSong?.artist

            updateTextView(stat.currentSongHelper?.songTitle as String, stat.currentSongHelper?.songArtist as String)

            stat.mediaPlayer?.reset()
            try {
                stat.mediaPlayer?.setDataSource(stat.myActivity, Uri.parse(stat.currentSongHelper?.songPath))
                stat.mediaPlayer?.prepare()
                stat.mediaPlayer?.start()
                processInformation(stat.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_on))

            } else {
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_off))

            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        stat.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        stat.myActivity = activity
    }

    override fun onPause() {
        super.onPause()
        stat.audioVisualization?.onPause()
        stat.mSensorManager?.unregisterListener(stat.mSensorListener)
    }

    override fun onResume() {
        super.onResume()
        stat.audioVisualization?.onResume()
        stat.mSensorManager?.registerListener(stat.mSensorListener, stat.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onDestroyView() {
        stat.audioVisualization?.release()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stat.mSensorManager = stat.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item?.isVisible = true

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            R.id.action_redirect->{
                stat.myActivity?.onBackPressed()
                return false
            }


        }
        return false

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        stat.favoriteContent = EchoDatabase(stat.myActivity)
        stat.currentSongHelper = CurrentSongHelper()
        stat.currentSongHelper?.isPlaying = true
        stat.currentSongHelper?.isShuffle = false
        stat.currentSongHelper?.isLoop = false

        var path: String? = null
        var songTitle: String? = null
        var songArtist: String? = null
        var songId: Long = 0

        try {

            path = arguments.getString("path")
            songTitle = arguments.getString("songTitle")
            songArtist = arguments.getString("songArtist")
            songId = arguments.getInt("songId").toLong()
            stat.currentPosition = arguments.getInt("songPosition")
            stat.fetchSongs = arguments.getParcelableArrayList("songData")

            stat.currentSongHelper?.songPath = path
            stat.currentSongHelper?.songArtist = songArtist
            stat.currentSongHelper?.songTitle = songTitle
            stat.currentSongHelper?.songId = songId
            stat.currentSongHelper?.currentPosition = stat.currentPosition

            staticated.updateTextView(stat.currentSongHelper?.songTitle as String, stat.currentSongHelper?.songArtist as String)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromFavBottomBar = arguments.get("FavBottomBar") as? String
        if (fromFavBottomBar != null) {
            stat.mediaPlayer = FavoriteFragment.statified.musicPlayer

        } else {

            stat.mediaPlayer?.release()
            stat.mediaPlayer = MediaPlayer()
            stat.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                stat.mediaPlayer?.setDataSource(stat.myActivity, Uri.parse(path))
                stat.mediaPlayer?.prepare()

            } catch (e: Exception) {
                e.printStackTrace()
            }
            stat.mediaPlayer?.start()
        }
        staticated.processInformation(stat.mediaPlayer as MediaPlayer)

        if (stat.currentSongHelper?.isPlaying as Boolean) {
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        stat.mediaPlayer?.setOnCompletionListener {

            staticated.onSongComplete()
        }
        clickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(stat.myActivity as Context, 0)
        stat.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            stat.currentSongHelper?.isShuffle = true
            stat.currentSongHelper?.isLoop = false
            stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)

        } else {
            stat.currentSongHelper?.isShuffle = false
            stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)

        }

        var prefsForLoop = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            stat.currentSongHelper?.isShuffle = false
            stat.currentSongHelper?.isLoop = true
            stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            stat.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)

        } else {
            stat.currentSongHelper?.isLoop = false
            stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)

        }

        if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_on))

        } else {
            stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_off))

        }
        stat.seekbar?.setOnSeekBarChangeListener(this)



    }



    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater!!.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity.title="Now Playing"
        stat.seekbar = view?.findViewById(R.id.seekBar)
        stat.startTimeText = view?.findViewById(R.id.startTime)
        stat.endTimeText = view?.findViewById(R.id.endTime)
        stat.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        stat.nextImageButton = view?.findViewById(R.id.nextButton)
        stat.previousImageButton = view?.findViewById(R.id.previousButton)
        stat.loopImageButton = view?.findViewById(R.id.loopButton)
        stat.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        stat.songArtistView = view?.findViewById(R.id.songArtist)
        stat.songTitleView = view?.findViewById(R.id.songTitle)

        stat.glView = view?.findViewById(R.id.visualizer_view)
        stat.fab = view?.findViewById(R.id.favoriteIcon)
        stat.fab?.alpha = 0.8f



        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stat.audioVisualization = stat.glView as AudioVisualization
    }

    fun clickHandler() {

        stat.fab?.setOnClickListener({
            if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean) {

                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_off))
                stat.favoriteContent?.deleteFavourite(stat.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(stat.myActivity, "Removed from Favorites", Toast.LENGTH_SHORT).show()

            } else {
                stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_on))
                stat.favoriteContent?.storeAsFavorite(stat.currentSongHelper?.songId?.toInt()
                        , stat.currentSongHelper?.songArtist
                        , stat.currentSongHelper?.songTitle
                        , stat.currentSongHelper?.songPath)
                Toast.makeText(stat.myActivity, "Added to Favorites", Toast.LENGTH_SHORT).show()


            }

        })

        stat.shuffleImageButton?.setOnClickListener({

            var editorShuffle = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (stat.currentSongHelper?.isShuffle as Boolean) {
                stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                stat.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                stat.currentSongHelper?.isShuffle = true
                stat.currentSongHelper?.isLoop = false
                stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }

        })
        stat.nextImageButton?.setOnClickListener({

            stat.currentSongHelper?.isPlaying = true
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if (stat.currentSongHelper?.isShuffle as Boolean) {
                staticated.playNext("PlayNextLikeNormalShuffle")
            } else {
                staticated.playNext("PlayNextNormal")
            }

        })

        stat.previousImageButton?.setOnClickListener({
            stat.currentSongHelper?.isPlaying = true
            if (stat.currentSongHelper?.isLoop as Boolean) {
                stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()

        })

        stat.loopImageButton?.setOnClickListener({

            var editorShuffle = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = stat.myActivity?.getSharedPreferences(staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()


            if (stat.currentSongHelper?.isLoop as Boolean) {
                stat.currentSongHelper?.isLoop = false
                stat.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                stat.currentSongHelper?.isLoop = true
                stat.currentSongHelper?.isShuffle = false
                stat.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                stat.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }


        })

        stat.playPauseImageButton?.setOnClickListener({

            if (stat.mediaPlayer?.isPlaying as Boolean) {
                stat.mediaPlayer?.pause()
                stat.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                stat.currentSongHelper?.isPlaying = false
            } else {
                stat.mediaPlayer?.start()
                stat.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                stat.currentSongHelper?.isPlaying = true
            }

        })
    }


    fun playPrevious() {

        stat.currentPosition = stat.currentPosition - 1
        if (stat.currentPosition == -1) {
            stat.currentPosition = 0
        }
        if (stat.currentSongHelper?.isPlaying as Boolean) {
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            stat.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        stat.currentSongHelper?.isLoop = false
        val nextSong = stat.fetchSongs?.get(stat.currentPosition)
        stat.currentSongHelper?.songTitle = nextSong?.songTitle
        stat.currentSongHelper?.songPath = nextSong?.songData
        stat.currentSongHelper?.currentPosition = stat.currentPosition
        stat.currentSongHelper?.songId = nextSong?.songID as Long
        stat.currentSongHelper?.songArtist=nextSong?.artist

        staticated.updateTextView(stat.currentSongHelper?.songTitle as String, stat.currentSongHelper?.songArtist as String)

        stat.mediaPlayer?.reset()
        try {
            stat.mediaPlayer?.setDataSource(activity, Uri.parse(stat.currentSongHelper?.songPath))
            stat.mediaPlayer?.prepare()
            stat.mediaPlayer?.start()
            staticated.processInformation(stat.mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (stat.favoriteContent?.checkifIdExists(stat.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_on))

        } else {
            stat.fab?.setImageDrawable(ContextCompat.getDrawable(stat.myActivity, R.drawable.favorite_off))

        }
    }

    fun bindShakeListener() {

        /*The sensor listener has two methods used for its implementation i.e. OnAccuracyChanged() and onSensorChanged*/
        stat.mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

                /*We do noot need to check or work with the accuracy changes for the sensor*/
            }

            override fun onSensorChanged(event: SensorEvent) {

                /*We need this onSensorChanged function
                * This function is called when there is a new sensor event*/
                /*The sensor event has 3 dimensions i.e. the x, y and z in which the changes can occur*/
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                /*Now lets see how we calculate the changes in the acceleration*/
                /*Now we shook the phone so the current acceleration will be the first to start with*/
                mAccelerationLast = mAccelerationCurrent

                /*Since we could have moved the phone in any direction, we calculate the Euclidean distance to get the normalized distance*/
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()

                /*Delta gives the change in acceleration*/
                val delta = mAccelerationCurrent - mAccelerationLast

                /*Here we calculate thelower filter
                * The written below is a formula to get it*/
                mAcceleration = mAcceleration * 0.9f + delta

                /*We obtain a real number for acceleration
                * and we check if the acceleration was noticeable, considering 12 here*/
                if (mAcceleration > 12) {

                    /*If the accel was greater than 12 we change the song, given the fact our shake to change was active*/
                    val prefs = stat.myActivity?.getSharedPreferences(stat.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        staticated.playNext("PlayNextNormal")
                    }
                }
            }
        }
    }

}
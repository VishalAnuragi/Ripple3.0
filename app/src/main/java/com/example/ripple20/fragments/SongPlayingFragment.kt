package com.example.ripple20.fragments

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.Context.INPUT_METHOD_SERVICE
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
import android.preference.PreferenceManager
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.ripple20.R
import com.example.ripple20.Services.OnClearFromRecentService
import com.example.ripple20.activities.CreateNotification
import com.example.ripple20.databases.RippleDatabase
import com.example.ripple20.fragments.SongPlayingFragment.Statified.isPlaying
import com.example.ripple20.fragments.SongPlayingFragment.Statified.notificationManager
import com.example.ripple20.fragments.SongPlayingFragment.Statified.play
import com.example.ripple20.fragments.SongPlayingFragment.Statified.position
import com.example.ripple20.fragments.SongPlayingFragment.Statified.title
import com.example.ripple20.fragments.SongPlayingFragment.Statified.track
import com.example.ripple20.utils.CurrentSongHelper
import com.example.ripple20.utils.Playable
import com.example.ripple20.utils.Songs
import com.example.ripple20.utils.Track
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class SongPlayingFragment : Fragment() , Playable {



    object Statified {

        var stop : String ?= null
        var play : ImageView ? = null
        var title :TextView ? = null
        var position : Int = 0
        var isPlaying : Boolean = true

        var notificationManager : NotificationManager ? = null
        lateinit var track : List<Track>

        var myActivity: Activity? = null

        var x : Int = 0

        var mediaPlayer: MediaPlayer? = null

        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playPauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null
        var seekbar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null

        var currentPosition: Int? = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null

        var fab: ImageView? = null
        var favoriteContent: RippleDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"

        var song_count : TextView ?= null

        var context2 :Context ?= null

        var updateSongTime = object : Runnable {
            override fun run() {
                val getcurrent = mediaPlayer?.currentPosition

                val seconds = ((getcurrent?.rem(60000))?.div(1000))

                val ss = (TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long)).toString() + ":" + "0"+ seconds.toString()
                if (seconds != null) {
                    if (seconds < 10){

                        startTimeText?.setText(ss)
                    }
                    else{
                        startTimeText?.setText(String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes(getcurrent.toLong() as Long)
                            , seconds ))
                    }
                }

                Handler().postDelayed(this, 1000)
            }
        }
    }


    object Staticated {

        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun onSongComplete(context1 :Context) {

            Statified.context2 = context1
            CreateNotification.createNotification( context1 , track.get(0) , R.drawable.play , 1 , track.size -1 )


            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying = true
            } else {
                if (Statified.currentSongHelper?.isLoop as Boolean) {
                    Statified.currentSongHelper?.isPlaying = true

                    var nextSong = Statified.fetchSongs?.get(Statified.currentPosition!!)
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.songArtist = nextSong?.artist
                    Statified.currentSongHelper?.songId = nextSong?.songId as Long
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition!!
                    updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

                    Statified.mediaPlayer?.reset()
                    try {
                        Statified.mediaPlayer?.setDataSource(Statified.myActivity as Context, Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))

            }
        }

        fun updateTextViews(songTitle: String, songArtist: String) {

            track = ArrayList<Track>()


            var songTitleUpdated = songTitle
            var songArtistUpdated = songArtist
            if (songTitle.equals("<unknown>")) {
                songTitleUpdated = "unknown"
            }
            if (songArtistUpdated.equals("<unknown>")) {
                songArtistUpdated = "unknown"
            }
            Statified.songTitleView?.setText(songTitleUpdated)
            Statified.songArtistView?.setText(songArtistUpdated)

            (track as ArrayList<Track>).add(Track ( songTitleUpdated , songArtistUpdated , (R.drawable.wr).toString() ))
            CreateNotification.createNotification( Statified.context2 , track.get(0) , R.drawable.play , 1 , track.size -1 )


        }

        fun processInformation(mediaPlayer: MediaPlayer) {

            val finalTime = mediaPlayer.duration
            var startTime = mediaPlayer.currentPosition

            Statified.seekbar?.max = finalTime

            val es = (finalTime.rem(60000)).div(1000)
            var ss1 = (TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()).toString() + ":" + "0" + es.toString()).toString()

            if (es < 10){

                Statified.endTimeText?.setText(ss1)

            }
            else{
                Statified.endTimeText?.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong() as Long)
                    , TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong() as Long) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong() as Long))))
            }

            Statified.seekbar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }

        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                Statified.currentPosition = Statified.currentPosition?.plus(1)
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition

            }
            if (Statified.currentPosition == Statified.fetchSongs?.size) {
                Statified.currentPosition = 0
            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition!!)

            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.songId = nextSong?.songId as Long
            updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity as Context, Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)


            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))

            }
        }
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater!!.inflate(R.layout.fragment_song_playing, container, false)

        setHasOptionsMenu(true)

        activity?.title = "Now Playing"
        Statified.seekbar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favouriteButton)
        Statified.fab?.alpha = 0.8f
        Statified.song_count = view?.findViewById(R.id.song_count)

        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val value =  preferences.getString("SONG_SIZE" , "0")
        Statified.song_count?.text = value



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            createChannel()
            context?.registerReceiver(broadcastReciever, IntentFilter("TRACKS_TRACKS"))
            context?.startService(Intent(activity, OnClearFromRecentService::class.java))
        }

       hideKeyboard(requireActivity())
        seekBarHandler()



        return view
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            var channel = NotificationChannel(CreateNotification.CHANNEL_ID,
                "Ripple", NotificationManager.IMPORTANCE_LOW)

            notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (notificationManager != null){
                notificationManager!!.createNotificationChannel(channel)
            }
        }
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        
        var view = activity.currentFocus

        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun seekBarHandler() {
        Statified.seekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) Statified.mediaPlayer?.seekTo(progress)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener,
            Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        Statified.audioVisualization?.onPause()
        super.onPause()

        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onDestroyView() {
        Statified.audioVisualization?.release()
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager?.cancelAll()
        }
      context?.unregisterReceiver(broadcastReciever)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.context2 = requireContext()

            Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()

        GLAudioVisualizationView.Builder(requireContext())
            .setBackgroundColorRes(android.R.color.darker_gray)
            .setLayerColors(R.array.rainbow_default)
            .build()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


       initializeSeekBar()

        Statified.favoriteContent = RippleDatabase(Statified.myActivity)
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long? = 0
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")?.toLong()
            Statified.currentPosition = arguments?.getInt("songPosition")
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")

            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.songId = songId!!
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition!!

            Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromFavBottomBar != null) {
            Statified.mediaPlayer = FavoriteFragment.Statified.mediaPlayer
        } else {

            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity as Context, Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Statified.mediaPlayer?.start()

        }

        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause)

        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongComplete( requireContext())
        }
        clickHandler()
        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0)
        Statified.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_on)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_off)
        } else {
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_off)
        }
        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop = true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_off)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_on)
        } else {
            Statified.currentSongHelper?.isLoop = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_off)
        }
        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))

        }
    }

    private fun initializeSeekBar() {
         val handler = Handler()
        handler.postDelayed( object : Runnable{
            override fun run(){
                try {
                    seekBar.progress = Statified.mediaPlayer!!.currentPosition

                    handler.postDelayed( this , 1000)
                }catch (e: Exception){

                }

            }
        }, 0 )
    }

    fun clickHandler() {
        Statified.fab?.setOnClickListener({
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))
                Statified.favoriteContent?.deletefavorites(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity, "Removed From Favorites", Toast.LENGTH_SHORT).show()
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
                Statified.favoriteContent?.storeAsFavorite(Statified.currentSongHelper?.songId?.toInt(), Statified.currentSongHelper?.songArtist,
                    Statified.currentSongHelper?.songTitle, Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity, "Added to Favorites", Toast.LENGTH_SHORT).show()


            }
        })
        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Statified.currentSongHelper?.isShuffle = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_off)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_on)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_off)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        })
        Statified.nextImageButton?.setOnClickListener({


            Statified.currentSongHelper?.isPlaying = true
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause)
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Staticated.playNext("PlayNextLikeNormalShuffle")
            } else {
                Staticated.playNext("PlayNextNormal")
            }

        })
        Statified.previousImageButton?.setOnClickListener({


            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_off)
            }
            playPrevious()

        })
        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_off)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_on)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_off)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }

        })
        Statified.playPauseImageButton?.setOnClickListener({

            if (Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play)

                CreateNotification.createNotification( context , track[position], android.R.drawable.ic_media_play,
                    position , track.size.minus(0))


            } else {
                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause)

                CreateNotification.createNotification( context , track[position], android.R.drawable.ic_media_pause,
                    position , track.size.minus(0))


            }

        })


    }

    fun playPrevious() {



        Statified.currentPosition = Statified.currentPosition?.minus(1)
        if (Statified.currentPosition == -1) {
            Statified.currentPosition = 0
        }
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause)
        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play)
        }
        Statified.currentSongHelper?.isLoop = false
        var nextSong = Statified.fetchSongs?.get(Statified.currentPosition!!)

        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songArtist = nextSong?.artist
        Statified.currentSongHelper?.songId = nextSong?.songId as Long
        Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        Statified.mediaPlayer?.reset()
        try {
            Statified.mediaPlayer?.setDataSource(Statified.myActivity as Context, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)


        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))

        }
    }

    fun bindShakeListener() {
        Statified.mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent?) {
                val x = event!!.values[0]
                val y = event!!.values[1]
                val z = event!!.values[2]
                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast

                mAcceleration = mAcceleration * 0.9f + delta


                if (mAcceleration > 12) {

                    val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")
                    }
                }

            }
        }
    }

    var broadcastReciever: BroadcastReceiver = object:BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var action : String? = intent?.getStringExtra("actionname")


            when (action){
              CreateNotification.ACTION_PREVIOUS -> onTrackPrevious()
              CreateNotification.ACTION_PLAY ->
                    if (isPlaying){
                        onTrackPause()
                    }
                    else {
                        onTrackPlay()
                    }
              CreateNotification.ACTION_NEXT -> onTrackNext()
            }
        }
    }

    override fun onTrackPrevious() {

        CreateNotification.createNotification( context , track[position], android.R.drawable.ic_media_pause,
            position , track.size.minus(1))
        Statified.currentSongHelper?.isPlaying = true
        title?.text = track[position.minus(1)].title
        if (Statified.currentSongHelper?.isLoop as Boolean) {
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_off)
        }
        playPrevious()

    }

    override fun onTrackPause() {

        CreateNotification.createNotification( context , track[position], android.R.drawable.ic_media_play,
            position , track.size.minus(0))
        play?.setImageResource(android.R.drawable.ic_media_play)
        title?.text = track[position].title
        isPlaying = false

        Statified.mediaPlayer?.pause()
        Statified.currentSongHelper?.isPlaying = false
        Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play)
    }

    override fun onTrackPlay() {


        CreateNotification.createNotification( context , track[position], android.R.drawable.ic_media_pause,
            position , track.size.minus(0))
        play?.setImageResource(android.R.drawable.ic_media_pause)
        title?.text = track[position].title
        isPlaying = true
        Statified.mediaPlayer?.start()
        Statified.currentSongHelper?.isPlaying = true
        Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause)
    }

    override fun onTrackNext() {

        CreateNotification.createNotification( context , track[position], android.R.drawable.ic_media_pause,
            position , track.size.plus(1))
        Statified.currentSongHelper?.isPlaying = true
        Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause)
        title?.text = track[position.plus(1)].title
        if (Statified.currentSongHelper?.isShuffle as Boolean) {
            Staticated.playNext("PlayNextLikeNormalShuffle")
        } else {
            Staticated.playNext("PlayNextNormal")
        }

    }

}
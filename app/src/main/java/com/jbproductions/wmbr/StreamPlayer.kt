package com.jbproductions.wmbr

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.Handler
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import java.io.File
import java.util.concurrent.TimeUnit

class StreamPlayer

    constructor(context: Context){

        private val MEDIA_VOLUME_DEFAULT: Float = 1.0f
        private val MEDIA_VOLUME_DUCK: Float = 0.2f

        private var mPlayOnAudioFocus: Boolean = false

        private var currentContext: Context? = null
        private val callbacks = ArrayList<StreamPlayerCallback>()
        private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        private var focusRequest: AudioFocusRequest? = null
        private val handler = Handler()

        private var mMediaPlayer: MediaPlayer? = null
        private val mediaPlayer: MediaPlayer?
            get() {
                if (mMediaPlayer == null) {
                    val attributes = AudioAttributes.Builder()
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build()
                    mMediaPlayer = MediaPlayer()
                    mMediaPlayer?.setWakeMode(currentContext, PowerManager.PARTIAL_WAKE_LOCK)
                    mMediaPlayer?.setAudioAttributes(attributes)
                    mMediaPlayer?.setOnCompletionListener(mediaPlayerCompletionListener)
                    mMediaPlayer?.setOnPreparedListener(mediaPlayerPreparedListener)
                    mMediaPlayer?.setOnErrorListener(mediaPlayerErrorListener)
                }
                return mMediaPlayer
            }

        private var mMediaSession: MediaSessionCompat? = null
        private val mediaSession : MediaSessionCompat?
            get() {
                if(mMediaSession == null) {
                    mMediaSession = MediaSessionCompat(currentContext, "MusicService")
                    mMediaSession?.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
                    //mMediaSession?.setCallback(object : MediaSessionCompat.Callback() { })
                }
                return mMediaSession
            }

        /**
         * Once mediaplayer completes, inform all the callbacks
         */
        private val mediaPlayerCompletionListener = MediaPlayer.OnCompletionListener {
            for (callback in callbacks) {
                callback.itemComplete()
            }
        }

        /**
         * Once mediaplayer is prepared, inform all the callbacks
         */
        private val mediaPlayerPreparedListener = MediaPlayer.OnPreparedListener {
            for (callback in callbacks) {
                callback.playerPrepared()
            }
            mMediaPlayer!!.start()
        }

        /**
         * Once mediaplayer hits error, inform all the callbacks
         */
        private val mediaPlayerErrorListener = MediaPlayer.OnErrorListener { mp, what, extra ->
            for (callback in callbacks) {
                callback.playerError()
            }
            false
        }

        private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    Log.d("AUDIOFOCUS CHANGE", "LOSS")
                    // Permanent loss of audio focus
                    // Pause playback immediately
                    //audioManager.abandonAudioFocus()
                    mPlayOnAudioFocus = false
                    mediaPlayer!!.stop()
                    //mediaController.transportControls.pause()
                    // Wait 30 seconds before stopping playback
                    handler.postDelayed(delayedStopRunnable, TimeUnit.SECONDS.toMillis(30))
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    Log.d("AUDIOFOCUS CHANGE", "LOSS_TRANSIENT")
                    // Pause playback
                    if(mediaPlayer!!.isPlaying) {
                        mPlayOnAudioFocus = true
                        mediaPlayer!!.pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    Log.d("AUDIOFOCUS CHANGE", "LOSS_TRANSIENT_CAN_DUCK")
                    // Lower the volume, keep playing
                    mediaPlayer!!.setVolume(MEDIA_VOLUME_DUCK, MEDIA_VOLUME_DUCK)
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    Log.d("AUDIOFOCUS CHANGE", "GAIN")
                    // Your app has been granted audio focus again
                    // Raise volume to normal, restart playback if necessary
                    if (mPlayOnAudioFocus && !mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.prepareAsync()
                    } else if (mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.setVolume(MEDIA_VOLUME_DEFAULT, MEDIA_VOLUME_DEFAULT)
                    }
                    mPlayOnAudioFocus = false
                }
            }
            for (callback in callbacks) {
                callback.audioFocusChange()
            }
        }

        private var delayedStopRunnable = Runnable {
            mediaPlayer!!.stop()
            //mediaController.transportControls.stop()
        }

        // Set the context
        init {
            currentContext = context.applicationContext
        }

        //region Media player state functions
        /**
         * Check whether the MediaPlayer is currently playing
         * @return true if playing, false not
         */
        val isPlaying: Boolean
        get() = mediaPlayer!!.isPlaying

        /**
         * Request our MediaPlayer to play an item
         */
        private fun play(url: String) {
            if (isPlaying) {
                Log.w("Plat=yer", "Already playing an item, did you mean to play another?")
            }
            if (mediaPlayer!!.isPlaying) {
                // Stop the current playing content
                mediaPlayer!!.stop()
            }

            //reset our player
            mediaPlayer!!.reset()

            //Set the url to play
            mediaPlayer!!.setDataSource(url)

            //prepare the player, once prepared the respective listener is called
            requestAudioFocus()
        }

        private fun requestAudioFocus() {

            val result: Int

            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                // If running on Android N or below, we request audio focus through AudioManager's built-in function
                result = audioManager.requestAudioFocus(
                        afChangeListener,
                        // Use the music stream.
                        AudioManager.STREAM_MUSIC,
                        // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN
                )

            } else {
                // If running on Android O or above, first use an AudioFocusRequest Builder to create the request
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_GAME)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    setAcceptsDelayedFocusGain(true)
                    setOnAudioFocusChangeListener(afChangeListener, handler)
                    build()
                }

                result = audioManager.requestAudioFocus(focusRequest!!)
            }

            val focusLock = Any()

            var playbackDelayed = false
            var playbackNowAuthorized = false

            synchronized(focusLock) {
                playbackNowAuthorized = when (result) {
                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                        try {
                            mediaPlayer!!.prepareAsync()
                        } catch (e: IllegalStateException) {

                        }
                        true
                    }
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        playbackDelayed = true
                        false
                    }
                    else -> false
                }
            }
        }

        private fun abandonAudioFocus() {

        }

        /**
         * Public function to pause playback
         */
        fun pause() {
            mediaPlayer!!.pause()
        }

        /**
         * Public function to start playback
         */
        fun playNew() {
            mediaPlayer!!.start()
        }

        /**
         * Public function to stop playback
         */
        fun stop() {
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocus(afChangeListener)
            }
            mediaPlayer!!.stop()
        }

        /**
         * Public function to play a specify playback
         * [url] is set for playback
         */
        fun playItem(url:String) {
            play(url)
        }
        /**
         * Public function to release mediaplayer
         */
        fun release() {
            if (mMediaPlayer != null) {
                if (mMediaPlayer!!.isPlaying) {
                    mMediaPlayer!!.stop()
                }
                mMediaPlayer!!.reset()
                mMediaPlayer!!.release()
            }
            mMediaPlayer = null
        }
        //endregion

        /**
         * Add a [callback] to our StreamPlayer
         * @param callback Callback that listens to the events
         */
        fun addCallback(callback: StreamPlayerCallback) {
            synchronized(callbacks) {
                if (!callbacks.contains(callback)) {
                    callbacks.add(callback)
                }
            }
        }

        /**
         * Remove a [callback] from our StreamPlayer, this is removed from our list of callbacks
         * @param callback Callback that listens to the events
         */
        fun removeCallback(callback: StreamPlayerCallback) {
            synchronized(callbacks) {
                callbacks.remove(callback)
            }
        }
        //region Interface to the Activity
        /**
         * An interface callback to keep track of the state of the MediaPlayer
         */
        interface StreamPlayerCallback {
            fun playerPrepared()
            fun playerProgress(offsetInMilliseconds: Long, percent: Float)
            fun itemComplete()
            fun playerError()
            fun audioFocusChange()
        }
        //endregion

        //region Singleton Instance
        companion object {

            val TAG = "StreamPlayer"

            private var sharedInstance: StreamPlayer? = null

            /**
             * Get a reference to the StreamPlayer instance, if it's null, we will create a new one
             * with the supplied context.
             * @param [context] any context
             * @return instance of the [StreamPlayer]
             */
            fun getInstance(context: Context): StreamPlayer {
                if (sharedInstance == null) {
                    sharedInstance = StreamPlayer(context)
                    clearCache(context)
                }
                return sharedInstance!!
            }

            private fun clearCache(context: Context) {
                try {
                    val dir = context.cacheDir
                    if (dir != null && dir.isDirectory) {
                        deleteDir(dir)
                    }
                } catch (e: Exception) {
                    // TODO: handle exception
                }

            }

            private fun deleteDir(dir: File?): Boolean {
                if (dir != null && dir.isDirectory) {
                    val children = dir.list()
                    for (i in children!!.indices) {
                        val success = deleteDir(File(dir, children[i]))
                        if (!success) {
                            return false
                        }
                    }
                }
                return dir?.delete() ?: false
            }
        }
        //endregion

    }

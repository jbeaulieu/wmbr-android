package com.jbproductions.wmbr.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.media.MediaBrowserServiceCompat

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class MediaPlaybackService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    //private val audioManager: AudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val streamDescription: MediaDescriptionCompat = MediaDescriptionCompat.Builder()
        .setMediaId("001")
        .setTitle("Now Streaming")
        .setSubtitle("WMBR 88.1 FM")
        .setDescription("Live")
        .build()

    private val streamMediaItem: MediaBrowserCompat.MediaItem = MediaBrowserCompat.MediaItem(streamDescription, FLAG_PLAYABLE)

    private val intentFilter = IntentFilter(ACTION_AUDIO_BECOMING_NOISY)

    // Defined elsewhere...
    private lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener

    private var audioNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                player.pause()
            }
        }
    }

//    private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()
    //private lateinit var myPlayerNotification: MediaStyleNotification
    private var player: MediaPlayer = MediaPlayer().apply {
        setAudioAttributes(AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build())
    }

    private lateinit var audioFocusRequest: AudioFocusRequest


    private val callback = object: MediaSessionCompat.Callback() {
        override fun onPlay() {
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Request audio focus for playback, this registers the afChangeListener

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setOnAudioFocusChangeListener(AudioFocusHelper())
                setAudioAttributes(AudioAttributes.Builder().run {
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                build()
            }
            val result = am.requestAudioFocus(audioFocusRequest)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start the service
                startService(Intent(applicationContext, MediaPlaybackService::class.java))
                // Set the session active  (and update metadata and state)
                mediaSession!!.isActive = true
                // start the player (custom call)
                player.start()
                // Register BECOME_NOISY BroadcastReceiver
                registerReceiver(audioNoisyReceiver, intentFilter)
                // Put the service in the foreground, post notification
//                startForeground(801, NotificationCompat.Builder(this@MediaPlaybackService, "229")
//                    .setContentTitle("WMBR 88.1 FM")
//                    .setContentText("Now Playing")
//                    .setVisibility(VISIBILITY_PUBLIC)
//                    .setSubText("Live")
//                    .build())
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MediaPlaybackService MediaSession").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                    or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(callback)

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }
    }


//    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
//    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        /* return if (allowBrowsing(clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            MediaBrowserServiceCompat.BrowserRoot(MY_MEDIA_ROOT_ID, null)
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            MediaBrowserServiceCompat.BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        }*/
    }

    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        val mediaItems = emptyList<MediaBrowserCompat.MediaItem>().toMutableList()

        if (MY_MEDIA_ROOT_ID == parentMediaId) {
            mediaItems += streamMediaItem
        }
        result.sendResult(mediaItems)
    }

    inner class AudioFocusHelper : AudioManager.OnAudioFocusChangeListener {

        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val focusRequest: AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener(this)
            .build()

//        fun requestAudioFocus(): Boolean {
//            val result: Int = audioManager.requestAudioFocus(focusRequest)
//
//            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
//        }
//
//        fun abandonAudioFocus() {
//            audioManager.abandonAudioFocusRequest(focusRequest)
//        }

        override fun onAudioFocusChange(focusChange: Int) {
            when(focusChange) {

                AudioManager.AUDIOFOCUS_GAIN -> {
                    player.start()
//                    if (playOnAudioFocus && !isPlaying()) {
//                        play()
//                    }
////                    } else if (isPlaying()) {
////                        setVolume(MEDIA_VOLUME_DEFAULT)
////                    }
//                    playOnAudioFocus = false
                }
//                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
//                    setVolume(MEDIA_VOLUME_DUCK)
//                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    player.stop()
//                    if (isPlaying()) {
//                        playOnAudioFocus = true
//                        pause()
//                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    player.stop()
                    // TODO
//                    audioManager.abandonAudioFocus {  }
//                    playOnAudioFocus = false
//                    stop()
                }
            }
        }
    }
}
package com.jbproductions.wmbr.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat

import androidx.media.MediaBrowserServiceCompat
import com.jbproductions.wmbr.R
import com.jbproductions.wmbr.service.notifications.MediaNotificationManager
import com.jbproductions.wmbr.service.players.MediaPlayerAdapter

class StreamService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var playback: PlayerAdapter
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var notificationManager: MediaNotificationManager
    private lateinit var callback: MediaSessionCallback
    private var serviceInStartedState: Boolean = false

    override fun onCreate() {
        super.onCreate()

        callback = MediaSessionCallback()

        mediaSession = MediaSessionCompat(this, "StreamService").apply {
            setCallback(callback)
            setFlags(FLAG_HANDLES_QUEUE_COMMANDS)

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())
            setSessionToken(sessionToken)
        }

        //sessionToken = mediaSession!!.sessionToken

        notificationManager = MediaNotificationManager(this)

        playback = MediaPlayerAdapter(this, MediaPlayerListener())
        Log.d("StreamService", "onCreate: MusicService creating MediaSession, and MediaNotificationManager");
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        notificationManager.onDestroy()
        playback.stop()
        mediaSession?.release()
        Log.d("StreamService", "onDestroy: MediaPlayerAdapter stopped, and MediaSession released")
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }

    // MediaSession Callback: Transport Controls -> MediaPlayerAdapter
    inner class MediaSessionCallback: MediaSessionCompat.Callback() {

        private var playlist: MutableList<MediaSessionCompat.QueueItem> = mutableListOf()
        private var queueIndex: Int = -1
        private lateinit var preparedMedia: MediaMetadataCompat

        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            playlist.add(MediaSessionCompat.QueueItem(description, description.hashCode().toLong()))
            queueIndex = if (queueIndex == -1) 0 else queueIndex
            mediaSession!!.setQueue(playlist)
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            playlist.remove(MediaSessionCompat.QueueItem(description, description.hashCode().toLong()))
            queueIndex = if (playlist.isEmpty()) -1 else queueIndex
            mediaSession!!.setQueue(playlist)
        }

        override fun onPrepare() {
            if (queueIndex < 0 && playlist.isEmpty()) return

            val mediaId: String? = playlist.get(queueIndex).description.mediaId
            preparedMedia = MediaMetadataCompat.Builder()
                .putString("source", "http://wmbr.org:8000/hi")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Live")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, "WMBR 88.1 FM")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Now Playing")
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Radio")
                .build()

            mediaSession?.setMetadata(preparedMedia)

            if (!mediaSession?.isActive!!) mediaSession?.isActive = true
        }

        override fun onPlay() {
            Log.d("onPlay", isReadyToPlay().toString())
            //if (!isReadyToPlay()) return

            preparedMedia = MediaMetadataCompat.Builder()
                .putString("source", "http://wmbr.org:8000/hi")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Live")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, "WMBR 88.1 FM")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Now Playing")
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Radio")
                .build()

            mediaSession?.setMetadata(preparedMedia)

            playback.playFromMedia(preparedMedia)
            Log.d("StreamService", "onPlayFromMediaId()")
        }

        override fun onPause() {
            playback.pause()
        }

        override fun onStop() {
            playback.stop()
            mediaSession!!.isActive = false
        }

        fun isReadyToPlay(): Boolean {
            return (playlist.isNotEmpty())
        }
    }

    // MediaPlayerAdapter Callback: MediaPlayerAdapter state -> StreamService
    inner class MediaPlayerListener : PlaybackInfoListener() {

        private var serviceManager: ServiceManager

        init {
            serviceManager = ServiceManager()
        }

        override fun onPlaybackStateChange(state: PlaybackStateCompat) {

            // Report the state to the MediaSession
            mediaSession?.setPlaybackState(state)

            // Manage the started state of this service
            when(state.state) {
                PlaybackStateCompat.STATE_PLAYING -> serviceManager.moveServiceToStartedState(state)
                PlaybackStateCompat.STATE_PAUSED -> serviceManager.updateNotificationForPause(state)
                PlaybackStateCompat.STATE_STOPPED -> serviceManager.moveServiceOutOfStartedState(state)
                else -> {}
            }
        }

        inner class ServiceManager {

            fun moveServiceToStartedState(state: PlaybackStateCompat) {

                var notification: Notification = notificationManager.getNotification(playback.getCurrentMedia(), state,
                    sessionToken!!
                )

                Log.d("NOTIFICATION", notification.channelId)

                if (!serviceInStartedState) {
                    startForegroundService(Intent(this@StreamService, StreamService::class.java))
                }

                startForeground(801, notification)
            }

            fun updateNotificationForPause(state: PlaybackStateCompat) {
                stopForeground(false)
                val notification: Notification = notificationManager.getNotification(playback.getCurrentMedia(), state, sessionToken!!)
                notificationManager.getNotificationManager().notify(801, notification)
            }

            fun moveServiceOutOfStartedState(state: PlaybackStateCompat) {
                stopForeground(true)
                stopSelf()
                serviceInStartedState=false
            }
        }
    }
}
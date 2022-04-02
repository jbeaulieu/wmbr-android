package com.jbproductions.wmbr.service.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.jbproductions.wmbr.MainActivity
import com.jbproductions.wmbr.NavigationActivity

import com.jbproductions.wmbr.R
import com.jbproductions.wmbr.service.StreamService

/**
 * Keeps track of a notification and updates it automatically for a given MediaSession. This is
 * required so that the music service is not killed during playback.
 */
class MediaNotificationManager(service: StreamService) {

    public val NOTIFICATION_ID: Int = 801

    private val TAG: String = MediaNotificationManager::class.simpleName!!
    private val CHANNEL_ID: String = "com.jbproductions.wmbr.stream.channel"
    private val REQUEST_CODE: Int = 229

    private val streamService: StreamService = service

    private val playAction: NotificationCompat.Action =
        NotificationCompat.Action(R.drawable.btn_playback_play, "play",
            MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PLAY))
    private val pauseAction: NotificationCompat.Action =
        NotificationCompat.Action(R.drawable.btn_playback_pause, "pause",
            MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_PAUSE))
    private val stopAction: NotificationCompat.Action =
        NotificationCompat.Action(R.drawable.btn_playback_stop, "stop",
            MediaButtonReceiver.buildMediaButtonPendingIntent(service, PlaybackStateCompat.ACTION_STOP))

    private val notificationManager: NotificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        // Cancel all notifications to handle the case where the service was killed and
        // restarted by the system.
        notificationManager.cancelAll()
    }

    fun onDestroy() {
        Log.d(TAG, "onDestroy()")
    }

    fun getNotificationManager() : NotificationManager {
        return notificationManager
    }

    fun getNotification(metadata: MediaMetadataCompat,
                        @NonNull state: PlaybackStateCompat,
                        token: MediaSessionCompat.Token) : Notification {

        val isPlaying: Boolean = state.state == PlaybackStateCompat.STATE_PLAYING
        val description: MediaDescriptionCompat = metadata.description
        val builder: NotificationCompat.Builder = buildNotification(state, token, isPlaying, description)

        return builder.build()
    }

    private fun buildNotification(@NonNull state: PlaybackStateCompat,
                                  token: MediaSessionCompat.Token,
                                  isPlaying: Boolean,
                                  description: MediaDescriptionCompat): NotificationCompat.Builder {

        // Create a notification channel if running Android Oreo or higher
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        //}

        //val largeicon: Bitmap = BitmapFactory.decodeResource(Resources(), R.drawable.artwork)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(streamService, CHANNEL_ID)
        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0)//, 1, 2)
                // For backwards compatibility with Android L and earlier
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                    streamService, PlaybackStateCompat.ACTION_STOP
                )))
            .setColor(Color.GREEN)
            .setSmallIcon(R.drawable.ic_notification)
            //.setLargeIcon(R.drawable.ic_notification)
            // Pending intent that is fired when user clicks on notification.
            .setContentIntent(createContentIntent())
            // Title - Usually Song name
            .setContentTitle(description.title)
            // Subtitle - Usually Artist name
            .setContentText(description.subtitle)
            // When notification is deleted (when playback is paused and notification can be
            // deleted) fire MediaButtonPendingIntent with ACTION_STOP
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(streamService, PlaybackStateCompat.ACTION_STOP))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

/*        // If skip to next action is enabled
        if ((state.actions? & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            builder.addAction()
        }*/

        builder.addAction(if (isPlaying) pauseAction else playAction)

/*        // If skip to previous action is enabled
        if ((state.actions && PlaybackStateCompat.ACTION_SKIP_TO_NEXT)) != 0) {
            builder.addAction()
        }*/

        return builder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            // User-visible name of the channel
            val name: CharSequence = "MediaSession"
            // User-visible description of the channel
            val description: String = "MediaSession and MediaPlayer"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel: NotificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            // Configure the notification channel
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "createChannel: New Channel Created")
        } else {
            Log.d(TAG, "createChannel: Existing Channel Reused")
        }
    }

    private fun createContentIntent(): PendingIntent {
        val openUI: Intent = Intent(streamService, NavigationActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(streamService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}
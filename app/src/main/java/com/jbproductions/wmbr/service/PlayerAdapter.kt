package com.jbproductions.wmbr.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.NonNull
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat

/**
 * Abstract player implementation that handles playing music with proper handling of headphones
 * and audio focus.
 */
abstract class PlayerAdapter(@NonNull private val context: Context) {

    private val MEDIA_VOLUME_DEFAULT: Float = 1.0f
    private val MEDIA_VOLUME_DUCK = 0.2f

    private val AUDIO_NOISY_INTENT_FILTER: IntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private var audioNoisyReceiverRegistered: Boolean = false

    private var audioNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                if (isPlaying()) pause();
            }
        }
    }

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioFocusHelper: AudioFocusHelper

    private var playOnAudioFocus: Boolean = false

    init {
        audioFocusHelper = AudioFocusHelper()
    }

    public abstract fun playFromMedia(metadata: MediaMetadataCompat)
    public abstract fun getCurrentMedia(): MediaMetadataCompat
    public abstract fun isPlaying(): Boolean

    public final fun play() {
        if (audioFocusHelper.requestAudioFocus()) {
            registerAudioNoisyReceiver()
            onPlay()
        }
    }

    /**
     * Called when media is ready to be played and indicates the app has audio focus.
     */
    protected abstract fun onPlay()

    public final fun pause() {
        if (!playOnAudioFocus) {
            audioFocusHelper.abandonAudioFocus()
        }
        unregisterAudioNoisyReceiver()
        onPause()
    }

    /**
     * Called when media must be paused.
     */
    protected abstract fun onPause()

    public final fun stop() {
        audioFocusHelper.abandonAudioFocus()
        unregisterAudioNoisyReceiver()
        onStop()
    }

    /**
     * Called when the media must be stopped. The player should clean up resources at this
     * point.
     */
    protected abstract fun onStop()

    public abstract fun seekTo(position: Long)
    public abstract fun setVolume(volume: Float)

    private fun registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            context.registerReceiver(audioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER)
            audioNoisyReceiverRegistered = true
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (audioNoisyReceiverRegistered) {
            context.unregisterReceiver(audioNoisyReceiver)
            audioNoisyReceiverRegistered = false
        }
    }

    /**
     * Helper class for managing audio focus related tasks.
     */
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

        fun requestAudioFocus(): Boolean {
            val result: Int = audioManager.requestAudioFocus(focusRequest)

            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

        fun abandonAudioFocus() {
            audioManager.abandonAudioFocusRequest(focusRequest)
        }

        override fun onAudioFocusChange(focusChange: Int) {
            when(focusChange) {

                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (playOnAudioFocus && !isPlaying()) {
                        play()
                    }
//                    } else if (isPlaying()) {
//                        setVolume(MEDIA_VOLUME_DEFAULT)
//                    }
                    playOnAudioFocus = false
                }
//                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
//                    setVolume(MEDIA_VOLUME_DUCK)
//                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (isPlaying()) {
                        playOnAudioFocus = true
                        pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // TODO
                    audioManager.abandonAudioFocus {  }
                    playOnAudioFocus = false
                    stop()
                }
            }
        }
    }
}
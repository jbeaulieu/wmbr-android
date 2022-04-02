package com.jbproductions.wmbr.service.players

import android.content.Context
import android.media.MediaPlayer
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.jbproductions.wmbr.service.PlaybackInfoListener
import com.jbproductions.wmbr.service.PlayerAdapter


class MediaPlayerAdapter(context: Context, listener: PlaybackInfoListener) : PlayerAdapter(context) {

    private var mediaPlayer: MediaPlayer
    private var state: Int = 0
    private lateinit var fileName: String
    private lateinit var currentMedia: MediaMetadataCompat
    private var playedToCompletion: Boolean = false

    private var seekWhileNotPlaying: Int = -1
    private lateinit var playbackListener: PlaybackInfoListener


    init {
        mediaPlayer = MediaPlayer()
        val mContext = context.applicationContext
        playbackListener = listener
    }

    /**
     * Once the {@link MediaPlayer} is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the {@link MainActivity} the {@link MediaPlayer} is
     * released. Then in the onStart() of the {@link MainActivity} a new {@link MediaPlayer}
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     */
    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener { play() }
    }

    override fun playFromMedia(metadata: MediaMetadataCompat) {
        currentMedia = metadata
        val mediaId: String? = metadata.getString("source")
        //mediaPlayer.setDataSource(medi)
        playFile()
    }

    private fun playFile() {
        initializeMediaPlayer()

        mediaPlayer.setDataSource("http://wmbr.org:8000/hi")

        try {
            mediaPlayer.prepareAsync()
        } catch (ex: Exception) {
            throw RuntimeException("Failed to open source")
        }

        //play()
    }

    override fun getCurrentMedia(): MediaMetadataCompat {
        return currentMedia
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun onPlay() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    override fun onPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            setNewState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    override fun onStop() {
        setNewState(PlaybackStateCompat.STATE_STOPPED)
        release()
    }

    private fun release() {
        mediaPlayer.release()
    }

    fun setNewState(newState: Int) {

        if (state == PlaybackStateCompat.STATE_STOPPED) {
            playedToCompletion = true
        }

        var reportPosition: Long = mediaPlayer.currentPosition.toLong()

        val stateBuilder = PlaybackStateCompat.Builder()
        stateBuilder.setState(
            newState,
            reportPosition,
            1.0f,
            SystemClock.elapsedRealtime()
        )

        playbackListener.onPlaybackStateChange(stateBuilder.build())
    }

    override fun seekTo(position: Long) {
        TODO("Not yet implemented")
    }

    override fun setVolume(volume: Float) {
        TODO("Not yet implemented")
    }


}
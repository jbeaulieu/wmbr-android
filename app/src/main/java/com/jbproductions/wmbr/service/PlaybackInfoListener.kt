package com.jbproductions.wmbr.service

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.MediaSessionCompat

/**
 * Listener to provide state updates from {@link MediaPlayerAdapter} (the media player)
 * to {@link StreamService} (the service that holds our {@link MediaSessionCompat}.
 */
abstract class PlaybackInfoListener {

    public abstract fun onPlaybackStateChange(state: PlaybackStateCompat)

    public fun onPlaybackCompleted() {}
}
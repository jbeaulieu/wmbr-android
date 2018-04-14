package com.jbproductions.wmbr;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public class StreamRadio extends Service implements MediaPlayer.OnErrorListener{
    private static final String TAG = "Information progress";
    public MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource("http://wmbr.org:8000/hi");
            Log.d(TAG, "Connected to http://wmbr.org:8000/hi");
            mediaPlayer.prepareAsync();
            Log.d(TAG, "Preparing Async");
        } catch (IOException e) {
            Log.e("TAG", "IOException");
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d(TAG, "Mediaplayer prepared");
                mediaPlayer.start();
            }
        });

        Log.d(TAG, "Nearly finished");

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onDestroy() {
/*        if (mediaPlayer != null) {
            Log.d(TAG, "Test MP Null");
            if (mediaPlayer.isPlaying()) {
                Log.d(TAG, "MediaPlayer registered as playing - attempting to stop");
                mediaPlayer.stop();
            }
            Log.d(TAG, "Resetting & releasing MP");
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }*/

        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;

        super.onDestroy();
    }
}

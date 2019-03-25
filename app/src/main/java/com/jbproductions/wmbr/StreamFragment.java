package com.jbproductions.wmbr;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import static android.view.View.VISIBLE;
import static android.view.View.INVISIBLE;

public class StreamFragment extends Fragment {

    public StreamFragment() {
        // Required empty public constructor
    }

    private final String streamUrl = "http://wmbr.org:8000/hi";
    private StreamPlayer audioPlayer;
    Resources res;
    MaterialButton streamButton;
    ProgressBar bufferProgressBar;
    TextView showNameTextView;
    TextView showHostsTextView;
    TextView timeTextView;
    TextView temperatureTextView;
    TextView weatherTextView;
    ImageView weatherIcon;

    private class streamAudioPlayerCallback implements StreamPlayer.StreamPlayerCallback {
        @Override
        public void playerPrepared() {
            // Player is prepared, hide the buffer progress wheel
            showBufferProgress(false);
        }

        @Override
        public void playerProgress(long offsetInMilliseconds, float percent) {
            //TODO("not implemented") - progress record
        }

        @Override
        public void itemComplete() {
            //TODO("not implemented") - finished playing
        }

        @Override
        public void playerError() {
            //TODO("not implemented") - error while playing
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_stream, container, false);
        streamButton = view.findViewById(R.id.streamButton);
        bufferProgressBar = view.findViewById(R.id.bufferProgress);
        showNameTextView = view.findViewById(R.id.showNameTextView);
        showHostsTextView = view.findViewById(R.id.showHostsTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        temperatureTextView = view.findViewById(R.id.temperatureTextView);
        weatherTextView = view.findViewById(R.id.weatherTextView);
        weatherIcon = view.findViewById(R.id.weatherImageView);

        // Get resources to dynamically set drawable objects
        res = getActivity().getResources();

        // Set up new singleton instance of audioPlayer and add callbacks
        audioPlayer = StreamPlayer.Companion.getInstance(getContext());
        audioPlayer.addCallback(new streamAudioPlayerCallback());

        //Download current metadata for show and host information
        new DownloadMetadataTask().execute();

        //SparseArray<Show> showDB = XmlParser.getShowInfo();

        // Check if the stream is already playing. If it is, change the button icon to 'stop'
        if(audioPlayer.isPlaying()) {
            streamButton.setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_stop_black_24dp, null));
        }

        /* Clicking the play/stop button should toggle the stream on/off, switch the play/stop icon,
            and display the "buffering" message and buffer wheel if we're toggling on */
        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePlayback();
            }
        });

        return view;
    }

    /**
     * Toggles livestream playback on/off
     * Used by the play/stop button within this fragment. This function starts/stops the stream, changes the button
     * icon to stop/play respectively, and if necessary displays the buffering icon & message
     */
    private void togglePlayback() {
        if(audioPlayer.isPlaying()) {
            audioPlayer.stop();
            streamButton.setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_play_arrow_black_24dp, null));
        }
        else {
            audioPlayer.playItem(streamUrl);
            showToast(getString(R.string.buffer_message));
            showBufferProgress(true);
            streamButton.setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_stop_black_24dp, null));
        }
    }

    /**
     * Hide/Unhide the progress bar used to show that the stream is buffering
     * @param visible Boolean for if the bar should display or not
     */
    private void showBufferProgress(Boolean visible) {
        if(visible) {
            this.bufferProgressBar.setVisibility(VISIBLE);
        } else {
            this.bufferProgressBar.setVisibility(INVISIBLE);
        }
    }

    /**
     * Convenience function for easily displaying a message via toast
     * @param message String to be toasted
     */
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * ASyncTask to obtain & display metadata about what's playing on WMBR.
     * The actual downloading and parsing is handled in the XmlParser class to avoid duplicate
     * methods across different parsing tasks.
      */
    private class DownloadMetadataTask extends AsyncTask<Void, Void, Boolean> {

        Map wmbrStatus;

        @Override
        protected void onPreExecute() {
            bufferProgressBar.setVisibility(VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            wmbrStatus = XmlParser.getStreamMetadata();
            return Boolean.TRUE;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

        @Override
        protected void onPostExecute(Boolean downloadSuccess) {
            showNameTextView.setText(wmbrStatus.get("showName").toString());
            showHostsTextView.setText(wmbrStatus.get("showHosts").toString());
            timeTextView.setText(wmbrStatus.get("time").toString());
            temperatureTextView.setText(wmbrStatus.get("temperature").toString());
            String weather = wmbrStatus.get("wx").toString();
            weatherTextView.setText(weather);
            LoadWeatherIcon(weather);
            bufferProgressBar.setVisibility(INVISIBLE);
        }
    }

    private void LoadWeatherIcon(String w) {

        String weather = w.toLowerCase();
        int drawable = R.drawable.wx_unknown;

        switch (weather) {
            case "sunny":
                drawable = R.drawable.wx_sunny;
                break;
            case "clear":
                drawable = R.drawable.wx_nt_clear;
                break;
            case "cloudy":
            case "overcast":
                drawable = R.drawable.wx_cloudy;
                break;
            case "mostly clear":
                drawable = R.drawable.wx_nt_mostlysunny;
                break;
            case "mostly cloudy":
                drawable = R.drawable.wx_mostlycloudy;
                break;
            case "mostly sunny":
            case "fair":
                drawable = R.drawable.wx_mostlysunny;
                break;
            case "partly cloudy":
                drawable = R.drawable.wx_partlycloudy;
                break;
            case "partly sunny":
                drawable = R.drawable.wx_partlysunny;
                break;
            case "light rain":
            case "showers":
                drawable = R.drawable.wx_chancerain;
                break;
            case "rain":
                drawable = R.drawable.wx_rain;
                break;
            case "snow":
                drawable = R.drawable.wx_snow;
                break;
            case "thunderstorms":
            case "thundershowers":
                drawable = R.drawable.wx_tstorms;
                break;
        }

        weatherIcon.setImageResource(drawable);
    }
}
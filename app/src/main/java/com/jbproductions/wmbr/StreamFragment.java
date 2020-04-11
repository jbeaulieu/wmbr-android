package com.jbproductions.wmbr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import static android.view.View.VISIBLE;
import static android.view.View.INVISIBLE;

import static com.jbproductions.wmbr.MediaPlayerService.ACTION_PREPARED;
import static com.jbproductions.wmbr.MediaPlayerService.ACTION_STOP;
import static com.jbproductions.wmbr.MediaPlayerService.ACTION_UNBIND;

public class StreamFragment extends Fragment {

    public StreamFragment() {
        // Required empty public constructor
    }

    // Temporary testing url
    private static final String STREAM_URL = "http://wmbr.org:8000/hi";

    NavigationActivity mainActivity;
    Resources res;
    MaterialCardView streamCard;
    MaterialCardView wxCard;
    MaterialButton streamButton;
    ProgressBar bufferProgressBar;
    TextView showNameTextView;
    TextView showHostsTextView;
    TextView timeTextView;
    TextView temperatureTextView;
    TextView weatherTextView;
    ImageView weatherIcon;

    private class MediaPlayerBroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BROADCAST RECEIVED", intent.getAction());

            switch (intent.getAction()) {
                case ACTION_PREPARED:
                    showBufferProgress(false);
                    streamButton.setClickable(true);
                    break;
                case ACTION_STOP:
                case ACTION_UNBIND:
                    streamButton.setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_play_arrow_black_24dp, null));
                    break;
                default:
                    showToast(intent.getAction());
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_stream, container, false);
        streamCard = view.findViewById(R.id.streaming_card);
        wxCard = view.findViewById(R.id.wmbr_state_card);
        streamButton = view.findViewById(R.id.streamButton);
        bufferProgressBar = view.findViewById(R.id.bufferProgress);
        showNameTextView = view.findViewById(R.id.showNameTextView);
        showHostsTextView = view.findViewById(R.id.showHostsTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        temperatureTextView = view.findViewById(R.id.temperatureTextView);
        weatherTextView = view.findViewById(R.id.weatherTextView);
        weatherIcon = view.findViewById(R.id.weatherImageView);

        // Get Activity to access MediaPlayerService functionality
        mainActivity = (NavigationActivity) getActivity();

        // Get resources to dynamically set drawable objects
        res = getActivity().getResources();

        //Download current metadata for show and host information
        new DownloadMetadataTask().execute();

        // Check if the stream is already playing. If it is, change the button icon to 'stop'
        if(mainActivity.isPlaying()) {
            streamButton.setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_stop_black_24dp, null));
        }

        BroadcastReceiver broadcastReceiver = new MediaPlayerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PREPARED);
        intentFilter.addAction(ACTION_STOP);
        intentFilter.addAction(ACTION_UNBIND);
        getContext().registerReceiver(broadcastReceiver, intentFilter);

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

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(R.string.wmbr);
    }

    /**
     * Toggles livestream playback on/off
     * Used by the play/stop button within this fragment. This function starts/stops the stream, changes the button
     * icon to stop/play respectively, and if necessary displays the buffering icon & message
     */
    private void togglePlayback() {
        if(mainActivity.isPlaying()) {
            // Stop the player and switch to a play button
            mainActivity.stopAudio();
            streamButton.setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_play_arrow_black_24dp, null));
        }
        else {
            // Start the player, alert to buffering, and switch to a stop button
            mainActivity.playAudio(STREAM_URL, true);
            //showToast(getString(R.string.buffer_message));
            showBufferProgress(true);
            streamButton.setIcon(ResourcesCompat.getDrawable(res, R.drawable.ic_stop_black_24dp, null));
        }
    }

    /**
     * Hide/Unhide the progress bar used to show that the stream is buffering
     * @param showProgress Boolean for if the bar should display or not
     */
    private void showBufferProgress(Boolean showProgress) {
        if(showProgress) {
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
            loadWeatherIcon(weather);
            bufferProgressBar.setVisibility(View.GONE);
            streamCard.setVisibility(VISIBLE);
            wxCard.setVisibility(VISIBLE);
        }
    }

    private void loadWeatherIcon(String w) {

        String weather = w.toLowerCase();
        // Crop any extra weather descriptors that come after a comma
        String parseWeather = (weather.indexOf(',') == -1) ? weather : weather.substring(0, weather.indexOf(','));
        int drawable = R.drawable.wx_unknown;
        boolean useNightIcons = false;

        // If the current time in EDT is prior to 5am or after 8pm, use the night icons
        TimeZone edt = TimeZone.getTimeZone("GMT-04:00");
        Calendar cal = Calendar.getInstance(edt);
        if(cal.get(Calendar.HOUR_OF_DAY) < 5 || cal.get(Calendar.HOUR_OF_DAY) > 20) {
            useNightIcons = true;
        }

        switch (parseWeather) {
            case "sunny":
                drawable = R.drawable.wx_sunny;
                break;
            case "clear":
                drawable = useNightIcons ? R.drawable.wx_nt_clear : R.drawable.wx_sunny;
                break;
            case "cloudy":
            case "overcast":
                drawable = R.drawable.wx_cloudy;
                break;
            case "mostly cloudy":
            case "partly sunny":
            case "partly clear":
                drawable = useNightIcons ? R.drawable.wx_nt_mostlycloudy : R.drawable.wx_mostlycloudy;
                break;
            case "mostly sunny":
            case "mostly clear":
            case "partly cloudy":
            case "fair":
                drawable = useNightIcons ? R.drawable.wx_nt_mostlysunny : R.drawable.wx_mostlysunny;
                break;
            case "light rain":
            case "light drizzle":
            case "chance of rain":
            case "chance of showers":
            case "drizzle":
            case "showers":
                drawable = R.drawable.wx_chancerain;
                break;
            case "rain":
                drawable = R.drawable.wx_rain;
                break;
            case "chance of flurries":
                drawable = R.drawable.wx_chanceflurries;
                break;
            case "light snow":
            case "flurries":
                drawable = R.drawable.wx_flurries;
                break;
            case "chance of snow":
                drawable = R.drawable.wx_chancesnow;
                break;
            case "snow":
                drawable = R.drawable.wx_snow;
                break;
            case "thunderstorms":
            case "thundershowers":
                drawable = R.drawable.wx_tstorms;
                break;
            case "isolated thunderstorms":
            case "chance of thunderstorms":
            case "chance of thundershowers":
                drawable = R.drawable.wx_chancetstorms;
                break;
            case "freezing rain":
            case "sleet":
            case "hail":
                drawable = R.drawable.wx_sleet;
                break;
            case "chance of sleet":
            case "chance of hail":
                drawable = R.drawable.wx_chancesleet;
                break;
            case "fog":
            case "foggy":
            case "haze":
            case "hazy":
            case "mist":
                drawable = R.drawable.wx_fog;
        }

        weatherIcon.setImageResource(drawable);
    }
}
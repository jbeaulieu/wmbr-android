package com.jbproductions.wmbr;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    Resources res;
    private final String streamUrl = "http://wmbr.org:8000/hi";
    private StreamPlayer audioPlayer;
    MaterialButton streamButton;
    ProgressBar bufferProgressBar;
    TextView showNameTextView;
    TextView showHostsTextView;
    TextView timeTextView;
    TextView temperatureTextView;
    TextView weatherTextView;

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
        res = getActivity().getResources();
        streamButton = view.findViewById(R.id.streamButton);
        bufferProgressBar = view.findViewById(R.id.bufferProgress);
        showNameTextView = view.findViewById(R.id.showNameTextView);
        showHostsTextView = view.findViewById(R.id.showHostsTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        temperatureTextView = view.findViewById(R.id.temperatureTextView);
        weatherTextView = view.findViewById(R.id.weatherTextView);

        // Set up new singleton instance of audioPlayer and add callbacks
        audioPlayer = StreamPlayer.Companion.getInstance(getContext());
        audioPlayer.addCallback(new streamAudioPlayerCallback());

        Map wmbrStatus = XmlParser.getStreamMetadata();
        showNameTextView.setText(wmbrStatus.get("showName").toString());
        showHostsTextView.setText(wmbrStatus.get("showHosts").toString());
        timeTextView.setText(wmbrStatus.get("time").toString());
        temperatureTextView.setText(wmbrStatus.get("temperature").toString());
        weatherTextView.setText(wmbrStatus.get("wx").toString());

        SparseArray<Show> showDB = XmlParser.getShowInfo();

        /* When the page loads, it needs to check if the stream is already playing, and if so, set
        the stream button to the stop icon*/
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
}
package com.jbproductions.wmbr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.VISIBLE;
import static android.view.View.INVISIBLE;

public class StreamFragment extends Fragment {

    public StreamFragment() {
        // Required empty public constructor
    }

    private StreamPlayer audioPlayer;

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

    private final String streamUrl = "http://wmbr.org:8000/hi";

    ImageButton streamButton;
    ImageButton stopButton;
    ProgressBar bufferProgressBar;
    TextView showTitleTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_stream, container, false);

        streamButton = view.findViewById(R.id.streamButton);
        stopButton = view.findViewById(R.id.stopButton);
        bufferProgressBar = view.findViewById(R.id.bufferProgress);
        showTitleTextView = view.findViewById(R.id.showTitleTextView);

        // Set up new singleton instance of audioPlayer and add callbacks
        audioPlayer = StreamPlayer.Companion.getInstance(getContext());
        audioPlayer.addCallback(new streamAudioPlayerCallback());

        XmlParser.getStreamMetadata();

        /* Clicking the 'start button should start the stream, display the "buffering" message,
            and show the progress wheel to indicate buffering
         */
        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.playItem(streamUrl);
                showToast(getString(R.string.buffer_message));
                showBufferProgress(true);
            }
        });

        // Clicking the stop button should stop the stream - no other calls necessary atm
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.stop();
            }
        });

        // Parse current show info XML
        //parseXML();

        //showTitleTextView.setText(showname + " " + showhosts + " " + temp);

        return view;
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
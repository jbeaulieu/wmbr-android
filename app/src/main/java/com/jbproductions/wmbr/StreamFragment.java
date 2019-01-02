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

    private class streamAudioPlayerCallback implements StreamPlayer.StreamPlayerCallback
    {
        @Override
        public void playerPrepared() {
            showToast("PLAYER PREPARED");
            showBufferProgress(false);
        }

        @Override
        public void playerProgress(long l, float f) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        @Override
        public void itemComplete() {
            showToast("FINISHED PLAYING......");
        }

        @Override
        public void playerError() {
            showToast("Error while playing......");
        }
    }

    final String streamUrl = "http://wmbr.org:8000/hi";

    ImageButton streamButton;
    ImageButton stopButton;
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

        audioPlayer = new StreamPlayer(getContext());
        //audioPlayer.addCallback(streamAudioPlayerCallback);
        show("BUFFERING......");
        showProgress(false);

        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Just a sec, buffering");
                audioPlayer.playItem(streamUrl);
                showBufferProgress(true);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.stop();
            }
        });


        return view;
    }

    public void showBufferProgress(Boolean status) {
        if(status) {
            this.bufferProgressBar.setVisibility(VISIBLE);
        } else {
            this.bufferProgressBar.setVisibility(INVISIBLE);
        }
    }

    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

}
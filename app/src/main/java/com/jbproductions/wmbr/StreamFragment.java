package com.jbproductions.wmbr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class StreamFragment extends Fragment {

    public StreamFragment() {
        // Required empty public constructor
    }

    private StreamPlayer audioPlayer;

    private class streamAudioPlayerCallback implements StreamPlayer.StreamPlayerCallback
    {
        @Override
        public void playerPrepared() {
            show("BUFFERING COMPLETE......");
            showProgress(false);
        }

        @Override
        public void playerProgress(long l, float f) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        @Override
        public void itemComplete() {
            show("FINISHED PLAYING......");
        }

        @Override
        public void playerError() {
            show("Error while playing......");
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
        showTitleTextView = view.findViewById(R.id.showTitleTextView);

        audioPlayer = new StreamPlayer(getContext());
        //audioPlayer.addCallback(streamAudioPlayerCallback);
        show("BUFFERING......");
        showProgress(false);

        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show("BUFFERING......");
                audioPlayer.playItem(streamUrl);
                showProgress(true);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer.stop();
                showProgress(false);
            }
        });


        return view;
    }

    public void showProgress(Boolean status) {

    }

    public void show(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

}
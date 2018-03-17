package com.jbproductions.wmbr;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.jbproductions.wmbr.IcyStreamMeta;

public class StreamFragment extends Fragment {

    public StreamFragment() {
        // Required empty public constructor
    }

    final MediaPlayer mp = new MediaPlayer();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_stream, container, false);

        final Button streamButton = (Button) view.findViewById(R.id.button);
        final TextView showTitleTextView = (TextView) view.findViewById(R.id.showTitle);

        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mp.setDataSource("http://wmbr.org:8000/hi");
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    mp.prepare();
                    mp.start();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String showTitle = "SHOWTITLE";

                try {
                    URL wmbr = new URL("http://wmbr.org:8000/hi");
                    IcyStreamMeta streamMeta;
                    streamMeta = new IcyStreamMeta(wmbr);

                    try {
                        streamMeta.refreshMeta();
                        showTitle = streamMeta.getTitle().toString();
                        showTitleTextView.setText(showTitle);
                        Log.e("mytag", showTitle);
                        //showTitleTextView.setText(showTitle);
                        Log.e("mytag", streamMeta.getTitle());
                    } catch (IOException e) {
                        showTitleTextView.setText("Unable to get title metadata");
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        });

        return view;
    }

}
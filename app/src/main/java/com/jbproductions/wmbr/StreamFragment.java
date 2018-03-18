package com.jbproductions.wmbr;

import android.media.MediaPlayer;
import android.os.AsyncTask;
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
import java.util.Timer;
import java.util.TimerTask;

public class StreamFragment extends Fragment {

    public StreamFragment() {
        // Required empty public constructor
    }

    final MediaPlayer mp = new MediaPlayer();

    IcyStreamMeta streamMeta;
    MetadataTask2 metadataTask2;
    String title_artist;
    Button streamButton;
    TextView showTitleTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_stream, container, false);

        streamButton = view.findViewById(R.id.button);
        showTitleTextView = view.findViewById(R.id.showTitleTextView);

        final String streamUrl = "http://wmbr.org:8000/hi";

        streamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mp.setDataSource(streamUrl);
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

                streamMeta = new IcyStreamMeta();
                try {
                    streamMeta.setStreamUrl(new URL(streamUrl));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                metadataTask2 =new MetadataTask2();
                try {
                    metadataTask2.execute(new URL(streamUrl));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                Timer timer = new Timer();
                MyTimerTask task = new MyTimerTask();
                timer.schedule(task,100, 10000);

            }
        });

        return view;
    }

    protected class MetadataTask2 extends AsyncTask<URL, Void, IcyStreamMeta>
    {
        @Override
        protected IcyStreamMeta doInBackground(URL... urls)
        {
            try
            {
                streamMeta.refreshMeta();
                Log.e("Retrieving MetaData","Refreshed Metadata");
            }
            catch (IOException e)
            {
                Log.e(MetadataTask2.class.toString(), e.getMessage());
            }
            return streamMeta;
        }

        @Override
        protected void onPostExecute(IcyStreamMeta result)
        {
            try
            {
                title_artist=streamMeta.getStreamTitle();
                Log.e("Retrieved title_artist", title_artist);
                if(title_artist.length()>0)
                {
                    showTitleTextView.setText(title_artist);
                }
            }
            catch (IOException e)
            {
                Log.e(MetadataTask2.class.toString(), e.getMessage());
            }
        }
    }

    class MyTimerTask extends TimerTask {
        public void run() {
            try {
                streamMeta.refreshMeta();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                String title_artist=streamMeta.getStreamTitle();
                Log.i("ARTIST TITLE", title_artist);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

}
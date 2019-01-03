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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
        parseXML();

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

    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     * @param urlString String representation of URL for XML file
     * @return InputStream
     * @throws IOException //TODO:document
     */
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    private void parseXML() {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream xmlInputStream = downloadUrl("http://wmbr.org/cgi-bin/xmlinfo");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(xmlInputStream, null);
            processParsing(parser);
        } catch (XmlPullParserException e) {
            //TODO: Handle exception
        } catch (IOException i) {
            //TODO: Handle exception
        }
    }

    private void processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        int eventType = parser.getEventType();

        String showname = "";
        String showhosts = "";
        String temp = "";

        while(eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = null;

            switch(eventType) {
                case XmlPullParser.START_TAG:

                    tagName = parser.getName();

                     if ("showname".equals(tagName)) {
                         showname = parser.nextText();
                     } else if ("showhosts".equals(tagName)) {
                         showhosts = parser.nextText();
                     } else if ("temp".equals(tagName)) {
                         temp = parser.nextText();
                     }
                    break;
            }

            eventType = parser.next();
        }

        showTitleTextView.setText(showname + " " + showhosts + " " + temp);
    }
}
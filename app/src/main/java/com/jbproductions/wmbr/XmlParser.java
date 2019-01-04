package com.jbproductions.wmbr;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class XmlParser {

    public XmlParser() {

    }

    private static final String streamMeta = "http://wmbr.org/cgi-bin/xmlinfo";
    private static final String archiveMeta = "http://wmbr.org/cgi-bin/xmlarch";
    private static final String scheduleMeta = "http://wmbr.org/cgi-bin/xmlsched";
    private static final String trackblasterMeta = "http://www.track-blaster.com/wmbr/pl_recent_songs.php";

    public static void getStreamMetadata() {
        parseXML(streamMeta);
    }


    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     * @param urlString String representation of URL for XML file
     * @return InputStream
     * @throws IOException //TODO:document
     */
    private static InputStream downloadUrl(String urlString) throws IOException {
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

    private static void parseXML(String resourceURL) {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream xmlInputStream = downloadUrl(resourceURL);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(xmlInputStream, null);
            processParsing(parser);
        } catch (XmlPullParserException e) {
            //TODO: Handle exception
        } catch (IOException i) {
            //TODO: Handle exception
        }
    }

    private static void processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
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
    }
}

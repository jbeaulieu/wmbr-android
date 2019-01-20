package com.jbproductions.wmbr;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class XmlParser {

    public XmlParser() {

    }

    private static final String streamMeta = "http://wmbr.org/cgi-bin/xmlinfo";
    private static final String archiveMeta = "http://wmbr.org/cgi-bin/xmlarch";
    private static final String scheduleMeta = "http://wmbr.org/cgi-bin/xmlsched";
    private static final String trackblasterMeta = "http://www.track-blaster.com/wmbr/pl_recent_songs.php";


    /**
     * Pulls in current information (show, host, weather, etc) from WMBR's xml API.
     * @return HashMap with values mapped to their tags as seen at 'http://wmbr.org/cgi-bin/xmlinfo'
     */
    static Map getStreamMetadata() {
        Map<String, String> wmbrStatusMap = new HashMap<>();
        XmlPullParser parser = setupXmlParser(streamMeta);

        try {
            int eventType = parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT) {
                String tagName;

                if(eventType == XmlPullParser.START_TAG) {

                    tagName = parser.getName();

                    if ("time".equals(tagName)) {
                        wmbrStatusMap.put("time", parser.nextText());
                    } else if ("showname".equals(tagName)) {
                        wmbrStatusMap.put("showName", parser.nextText());
                    } else if ("showhosts".equals(tagName)) {
                        wmbrStatusMap.put("showHosts", parser.nextText());
                    } else if ("showid".equals(tagName)) {
                        wmbrStatusMap.put("showID", parser.nextText());
                    } else if ("showurl".equals(tagName)) {
                        wmbrStatusMap.put("showUrl", parser.nextText());
                    } else if ("temp".equals(tagName)) {
                        wmbrStatusMap.put("temperature", parser.nextText());
                    } else if ("wx".equals(tagName)) {
                        wmbrStatusMap.put("wx", parser.nextText());
                    }
                }

                eventType = parser.next();  // Advance the parser to the next tag
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wmbrStatusMap;

    }

    /**
     * Configures an XmlPullParser object to pull xml data from a given URL
     * @param resourceURL String of the URL to the xml that will be parsed
     * @return XmlPullParser object that can be stepped through and parsed
     */
    private static XmlPullParser setupXmlParser(String resourceURL) {
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream xmlInputStream = downloadUrl(resourceURL);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(xmlInputStream, null);
            return parser;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Given a string representation of a URL, sets up a connection and gets an input stream.
     * @param urlString String representation of URL for XML file
     * @return InputStream
     * @throws IOException when connection fails
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
}

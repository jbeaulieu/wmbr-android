package com.jbproductions.wmbr;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArchiveParser {

    private static final String ARCHIVE_URL = "https://wmbr.org/cgi-bin/xmlarch";

    static List<Show> parseArchives() {

        XmlPullParser parser = XmlParser.setupXmlParser(ARCHIVE_URL);
        List<Show> returnList = new ArrayList<>();
        Show currentShow = null;
        Archive currentArchive = null;
        int showid;

        try {
            int eventType = parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT) {
                String tagName;

                if (eventType == XmlPullParser.START_TAG) {

                    tagName = parser.getName();

                    if("show".equals(tagName)) {
                        currentShow = new Show();
                        showid = Integer.parseInt(parser.getAttributeValue(0));
                        currentShow.setID(showid);
                        returnList.add(currentShow);
                    } else if (currentShow != null) {

                        if("name".equals(tagName)) {
                            currentShow.setName(parser.nextText());
                        } else if ("hosts".equals(tagName)) {
                            currentShow.setHosts(parser.nextText());
                        } else if ("archive".equals(tagName)) {
                            Log.d("Archive", "Found new Archive");
                            currentArchive = new Archive();
                            parser.next();
                            tagName = parser.getName();
                            while(!("archive".equals(tagName) && eventType == XmlPullParser.END_TAG)) {
                                if("url".equals(tagName)) {
                                    currentArchive.setUrl(parser.nextText());
                                } else if ("date".equals(tagName)) {
                                    currentArchive.setDate(parser.nextText());
                                }
                                eventType = parser.next();
                                tagName = parser.getName();
                            }
                            currentShow.addArchive(currentArchive);
                        }
                    }
                }
                eventType = parser.next();  // Advance the parser to the next tag
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnList;
    }
}

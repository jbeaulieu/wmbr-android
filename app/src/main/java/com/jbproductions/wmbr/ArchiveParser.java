package com.jbproductions.wmbr;

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
        int showid;

        try {
            int eventType = parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT) {
                String tagName;

                if(eventType == XmlPullParser.START_TAG) {

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
                        } else if ()
                    }
                }
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

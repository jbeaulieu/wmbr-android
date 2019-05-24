package com.jbproductions.wmbr;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ScheduleParser {

    private static final String SCHEDULE_URL = "https://wmbr.org/cgi-bin/xmlsched";

    static ArrayList<Show>[] getWeekScheduleArray() {
        ArrayList[] weeklyScheduleArray = new ArrayList[7];
        Show selectedShow;

        List<Show> fullShowList = parseFullShowList();

        // First need to initialize each arrayList in weeklyScheduleArray
        for(int i=0; i<7; i++) {
            weeklyScheduleArray[i] = new ArrayList<Show>();
        }

        for(int i=0; i < fullShowList.size(); i++) {     // Iterate through all shows in the showDatabase
            selectedShow = fullShowList.get(i);
            if(selectedShow.getDay() == 7)
            {
                // If a show has getDay() == 7 (weekdays), add it to the list for Monday-Friday
                for(int j=1; j<=5; j++){
                    weeklyScheduleArray[j].add(selectedShow);
                }
            }
            else {
                // Otherwise, add it to the list for the day that it airs
                weeklyScheduleArray[selectedShow.getDay()].add(selectedShow);
            }
        }

        return weeklyScheduleArray;
    }

    static List<Show> parseFullShowList() {

        XmlPullParser parser = XmlParser.setupXmlParser(SCHEDULE_URL);
        List<Show> returnList = new ArrayList<>();
        Show currentShow = null;
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
                        } else if ("day".equals(tagName)) {
                            currentShow.setDay(Integer.parseInt(parser.nextText()));
                        } else if ("time".equals(tagName)) {
                            currentShow.setTime(Integer.parseInt(parser.nextText()));
                        } else if ("length".equals(tagName)) {
                            currentShow.setLength(Integer.parseInt(parser.nextText()));
                        } else if ("alternates".equals(tagName)) {
                            currentShow.setAlternates(Integer.parseInt(parser.nextText()));
                        } else if ("hosts".equals(tagName)) {
                            currentShow.setHosts(parser.nextText());
                        } else if ("producers".equals(tagName)) {
                            currentShow.setProducers(parser.nextText());
                        } else if ("url".equals(tagName)) {
                            currentShow.setUrl(parser.nextText());
                        } else if ("email".equals(tagName)) {
                            currentShow.setEmail(parser.nextText());
                        } else if ("description".equals(tagName)) {
                            currentShow.setDescription(parser.nextText());
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

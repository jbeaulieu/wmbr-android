package com.jbproductions.wmbr;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * ViewModel class enabling access to WMBR schedule data from multiple places within the app
 */
class ScheduleViewModel extends ViewModel {

    private static final String SCHEDULE_URL = "https://wmbr.org/cgi-bin/xmlsched";

    private MutableLiveData<ShowDatabase> showDatabase;

    LiveData<ShowDatabase> getShowDatabase() {
        if(showDatabase == null) {
            showDatabase = new MutableLiveData<>();
            loadShows();
        }
        return showDatabase;
    }

    // Asynchronous operation to fetch show data
    private void loadShows() {

        ShowDatabase db = new ShowDatabase();
        XmlPullParser parser = XmlParser.setupXmlParser(SCHEDULE_URL);
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
                        db.put(showid, currentShow);
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

        showDatabase.setValue(db);
    }
}

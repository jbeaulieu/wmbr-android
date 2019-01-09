package com.jbproductions.wmbr;

import java.util.ArrayList;

public class Show {

    public Show() {

    }

    private int id, day, time, length, alternates;
    private String name, hosts, producers, url, email, description;

    public void setID(int showID) {
        id = showID;
    }

    public void setDay(int dayOfWeek) {
        day = dayOfWeek;
    }

    public void setTime(String showTime) {
        time = convertTo24HourTime(showTime);
    }

    public void setLength(int showLength) {
        length = showLength;
    }

    public void setAlternates(int alternateID) {
        alternates = alternateID;
    }

    public void setName(String showName) {
        name = showName;
    }

    public void setHosts(String showHosts) {
        hosts = showHosts;
    }

    /**
     * This function is necessary because the WMBR website keeps track of showtimes as strings
     * with a colon and a suffix of "a", "p", "m", or "n" for AM, PM, midnight, and noon.
     * Examples: "7:00a" -> 0700 hours
     *          "10:30p" -> 2230 hours
     *          "12:00m" -> 0000 hours (midnight)
     *          "12:00n" -> 1200 hours (noon)
     * The below function converts the aforementioned time string to an integer representation of
     * a 24-hour time. On a personal note, this method of timekeeping on the WMBR seems website
     * seems exceptionally silly and I look forward to it being changed.
     * @param showTime String value of the show's start time, scraped from wmbr.org
     * @return int in the range [0,2400] representing the show's start time
     */
    public int convertTo24HourTime(String showTime) {

        // Deal with hardcoded "12:00m" or "12:00n" values for noon and midnight shows
        if(showTime == "12:00m") {
            return 0;
        }
        else if(showTime == "12:00n") {
            return 1200;
        }
        else {
            String removeColon = showTime.replace(":", "");
            int time = Integer.parseInt(removeColon.substring(0, removeColon.length()-1));

            if(removeColon.substring(removeColon.length()-1).equals("p")) {
                time += 1200;
            }

            return time;
        }
    }
}

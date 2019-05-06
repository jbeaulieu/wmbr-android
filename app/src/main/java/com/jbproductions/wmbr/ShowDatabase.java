package com.jbproductions.wmbr;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

public class ShowDatabase extends SparseArray<Show> {

    public ShowDatabase() {
    }

    /***
     * Returns a List of all Shows which air on a particular day of the week.
     * @param dayOfWeek int in range [0,6] corresponding to [Sunday, Saturday]
     * @return Unordered List containing all Shows that air on the input day
     */
    public ArrayList<Show> getShowsOnDayOfWeek(int dayOfWeek) {
        ArrayList<Show> returnList = new ArrayList<>();

        for(int i=0; i < size(); i++) {         // Iterate through all shows in DB
            Show show = valueAt(i);
            if(show.getDay() == dayOfWeek) {    // If show day equals input day, add to returnList
                returnList.add(show);
            } else if(show.getDay() == 7 && dayOfWeek > 0 && dayOfWeek < 6) {
                returnList.add(show);           // If show airs on weekdays and user requested a weekday [Monday-Friday], add show to returnList
            }
        }

        return returnList;
    }

    public SparseArray<Show> getOrderedShowsOnDayOfWeek(int dayOfWeek) {
        SparseArray<Show> returnArray = new SparseArray<>();

        for(int i=0; i < size(); i++) {
            Show show = valueAt(i);
            if(show.getDay() == dayOfWeek) {        // If show day equals input day, add to returnList
                if(show.getAlternates() == 2) {
                    returnArray.put(show.getTime() + 2, show);
                }
                else {
                    returnArray.put(show.getTime(), show);
                }
            } else if(show.getDay() == 7 && dayOfWeek > 0 && dayOfWeek < 6) {       // If show airs on weekdays and user requested a weekday [Monday-Friday], add show to returnList
                if(show.getAlternates() == 2) {
                    returnArray.put(show.getTime() + 2, show);
                }
                else {
                    returnArray.put(show.getTime(), show);
                }
            }
        }

        return returnArray;
    }

    /***
     * Compiles array of daily show lists, by calling getShowsOnDayOfWeek 7 times, once per day of the week.
     * @return Array of length 7, where each entry is a List<Show> representing all shows airing on the nth day of the week
     */
    public ArrayList<Show>[] buildWeeklyScheduleArray() {

        ArrayList[] weeklyScheduleArray = new ArrayList[7];

        for(int i=0; i < 7; i++) {
            weeklyScheduleArray[i] = getShowsOnDayOfWeek(i);
        }

        return weeklyScheduleArray;
    }
}

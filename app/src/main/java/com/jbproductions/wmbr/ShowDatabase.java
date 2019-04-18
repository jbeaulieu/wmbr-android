package com.jbproductions.wmbr;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

public class ShowDatabase extends SparseArray<Show> {

    public ShowDatabase() {
    }

    private SparseArray<Show> database;

    public Show getShow(int id) {
        return database.valueAt(id);
    }
    
    public List<Show> getShowsOnDayOfWeek(int dayOfWeek) {
        List<Show> returnList = new ArrayList<>();

        for(int i=0; i < database.size(); i++) {
            Show show = database.valueAt(i);
            if(show.getDay() == dayOfWeek) {
                returnList.add(show);
            }
        }

        return returnList;
    }
}

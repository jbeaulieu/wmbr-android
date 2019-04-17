package com.jbproductions.wmbr;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.SparseArray;

/**
 * ViewModel class enabling access to WMBR schedule data from multiple places within the app
 */
public class ScheduleViewModel extends ViewModel {

    private MutableLiveData<SparseArray<Show>> showDB;

    public LiveData<SparseArray<Show>> getShows() {
        if(showDB == null) {
            showDB = new MutableLiveData<>();
            loadShows();
        }
        return showDB;
    }

    // Asynchronous operation to fetch show data
    private void loadShows() {
        showDB.setValue(XmlParser.getShowInfo());
    }
}

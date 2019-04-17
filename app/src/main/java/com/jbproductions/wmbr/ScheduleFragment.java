package com.jbproductions.wmbr;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScheduleFragment extends Fragment {

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_schedule, container, false);

        ScheduleViewModel scheduleModel = ViewModelProviders.of(getActivity()).get(ScheduleViewModel.class);
        SparseArray<Show> showDB = scheduleModel.getShows().getValue();

        return view;
    }

}

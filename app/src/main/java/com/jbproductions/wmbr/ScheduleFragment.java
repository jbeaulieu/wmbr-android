package com.jbproductions.wmbr;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScheduleFragment extends Fragment {

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_schedule, container, false);
        TextView textView = view.findViewById(R.id.testText);

        ScheduleViewModel scheduleModel = ViewModelProviders.of(getActivity()).get(ScheduleViewModel.class);
        ShowDatabase showDatabase = scheduleModel.getShowDatabase().getValue();

        textView.setText(showDatabase.get(5943).getName());

        for(int i=0; i < showDatabase.size(); i++) {
            Show show = showDatabase.valueAt(i);
            if(show.getDay() == 1) {
                textView.setText(textView.getText() + "\n" + show.getName());
            }
        }

        return view;
    }

}

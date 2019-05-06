package com.jbproductions.wmbr;

import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment {

    public ScheduleFragment() {
        // Required empty public constructor
    }

    ProgressBar loadingProgressBar;
    TextView testText;

    ShowDatabase showDatabase;
    ArrayList[] weekScheduleArray = new ArrayList[7];

    //RecyclerView recyclerView;
    ShowAdapter showAdapter = new ShowAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_schedule, container, false);
        testText = view.findViewById(R.id.testText);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        ScheduleViewModel scheduleModel = ViewModelProviders.of(getActivity()).get(ScheduleViewModel.class);
        showDatabase = scheduleModel.getShowDatabase().getValue();

        new ScheduleDataTask().execute();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(showAdapter);

        return view;
    }

    private class ScheduleDataTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            weekScheduleArray = showDatabase.buildWeeklyScheduleArray();
            return Boolean.TRUE;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

        @Override
        protected void onPostExecute(Boolean downloadSuccess) {
            loadingProgressBar.setVisibility(View.GONE);

            showAdapter.addAll(weekScheduleArray[0]);

/*            for(int i=0; i < 7; i++) {
                ArrayList<Show> dailySparseArray = weekScheduleArray[i];
                for(int j=0; j < dailySparseArray.size(); j++) {
                    testText.append("\n" + dailySparseArray.get(j).getName());
                }
                testText.append("\n");
            }*/
            testText.append("DONE");
        }
    }
}

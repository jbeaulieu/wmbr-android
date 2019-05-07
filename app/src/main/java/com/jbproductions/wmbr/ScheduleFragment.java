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

import java.util.ArrayList;

public class ScheduleFragment extends Fragment {

    public ScheduleFragment() {}

    ProgressBar loadingProgressBar;

    ShowDatabase showDatabase;
    ArrayList[] weekScheduleArray = new ArrayList[7];
    RecyclerView recyclerView;
    ShowAdapter showAdapter = new ShowAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_schedule, container, false);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        ScheduleViewModel scheduleModel = ViewModelProviders.of(getActivity()).get(ScheduleViewModel.class);
        showDatabase = scheduleModel.getShowDatabase().getValue();
        new ScheduleDataTask().execute();

        recyclerView = view.findViewById(R.id.recycler_view);
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
        }
    }
}

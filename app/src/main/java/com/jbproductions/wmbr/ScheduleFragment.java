package com.jbproductions.wmbr;

import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment {

    public ScheduleFragment() {}

    static final int NUM_ITEMS = 7;

    ShowDatabase showDatabase;
    static ArrayList[] weekScheduleArray = new ArrayList[7];


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_schedule, container, false);

        ScheduleViewModel scheduleModel = ViewModelProviders.of(getActivity()).get(ScheduleViewModel.class);
        showDatabase = scheduleModel.getShowDatabase().getValue();
        new ScheduleDataTask().execute();

        SchedulePagerAdapter mAdapter = new SchedulePagerAdapter(getFragmentManager());

        ViewPager mPager = view.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mPager);

        return view;
    }

    private class ScheduleDataTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            //loadingProgressBar.setVisibility(View.VISIBLE);
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
        }
    }

    public static class SchedulePagerAdapter extends FragmentPagerAdapter {

        public SchedulePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            return RecycleViewFragment.newInstance(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "S";
                case 1:
                    return "M";
                case 2:
                    return "T";
                case 3:
                    return "W";
                case 4:
                    return "T";
                case 5:
                    return "F";
                case 6:
                    return "S";
                default:
                    return "/";
            }
        }
    }

    public static class RecycleViewFragment extends Fragment {
        int mNum;

        RecyclerView recyclerView;
        ShowAdapter showAdapter = new ShowAdapter();

        /**
         * Create a new instance of RecycleViewFragment, providing "num"
         * as an argument to indicate which day of the week it should reflect.
         */
        static RecycleViewFragment newInstance(int num) {
            RecycleViewFragment f = new RecycleViewFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_pager_list, container, false);
            View tv = view.findViewById(R.id.text);
            recyclerView = view.findViewById(R.id.recycle_view);
            ((TextView)tv).setText("Fragment #" + mNum);

            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(showAdapter);

            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            new LoadScheduleToRecycleView().execute();
        }

        private class LoadScheduleToRecycleView extends AsyncTask<Void, Void, Boolean> {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                showAdapter.addAll(weekScheduleArray[mNum]);
                return Boolean.TRUE;
            }

            @Override
            protected void onProgressUpdate(Void... values) {}

            @Override
            protected void onPostExecute(Boolean downloadSuccess) {

            }
        }
    }
}

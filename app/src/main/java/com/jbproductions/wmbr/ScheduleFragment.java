package com.jbproductions.wmbr;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment {

    public ScheduleFragment() {}

    static final int NUM_ITEMS = 7;
    static ArrayList[] weekScheduleArray = new ArrayList[7];

    TabLayout tabLayout;
    ViewPager mPager;
    SchedulePagerAdapter mAdapter;
    ProgressBar loadingSpinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_schedule, container, false);
        mPager = view.findViewById(R.id.pager);
        tabLayout = view.findViewById(R.id.tab_layout);
        loadingSpinner = view.findViewById(R.id.loading_spinner);

        new ScheduleDataTask().execute();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(R.string.schedule);
    }

    /**
     * AsyncTask to pull and parse schedule data from wmbr.org.
     * Calls to ScheduleParser for xml parsing operations.
     * When finished, sets up the tabLayout and ViewPager to display schedule data
     */
    private class ScheduleDataTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mPager.setVisibility(View.GONE);
            loadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            weekScheduleArray = ScheduleParser.getWeekScheduleArray();
            return Boolean.TRUE;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

        @Override
        protected void onPostExecute(Boolean downloadSuccess) {
            mAdapter = new SchedulePagerAdapter(getChildFragmentManager());
            mPager.setAdapter(mAdapter);
            tabLayout.setupWithViewPager(mPager);
            loadingSpinner.setVisibility(View.GONE);
            mPager.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Adapter to connect the outer ScheduleFragment with the ViewPager it contains
     * Each page of the ViewPager will contain a RecyclerViewFragment
     */
    public static class SchedulePagerAdapter extends FragmentPagerAdapter {

        private final String[] dayOfWeekInitials = {"S", "M", "T", "W", "T", "F", "S"};

        SchedulePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            return RecyclerViewFragment.newInstance(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int index) {
            return dayOfWeekInitials[index];
        }
    }

    /**
     * Fragment to display within each page of the PagerAdapter
     * Each contains a RecyclerView that will list the shows on the schedule for a particular day
     */
    public static class RecyclerViewFragment extends Fragment {
        int mNum;

        RecyclerView recyclerView;
        ShowAdapter showAdapter;

        /**
         * Create a new instance of RecyclerViewFragment, providing "num"
         * as an argument to indicate which day of the week it should reflect.
         */
        static RecyclerViewFragment newInstance(int num) {
            RecyclerViewFragment f = new RecyclerViewFragment();

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
            showAdapter = new ShowAdapter(getActivity());
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_pager_list, container, false);
            recyclerView = view.findViewById(R.id.recycle_view);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(showAdapter);
            showAdapter.addAll(weekScheduleArray[mNum]);
        }
    }
}

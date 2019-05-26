package com.jbproductions.wmbr;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ArchiveFragment extends Fragment {

    public ArchiveFragment() {}

    List<Show> showArchives = new ArrayList<>();

    RecyclerView recyclerView;
    ArchiveAdapter archiveAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        archiveAdapter = new ArchiveAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        recyclerView = view.findViewById(R.id.archive_recycle_view);
        new LoadArchiveDataTask().execute();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(archiveAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(R.string.archives);
    }

    /**
     * AsyncTask to pull and parse archive data from wmbr.org.
     * Calls to ArchiveParser for xml parsing operations.
     */
    private class LoadArchiveDataTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {}

        @Override
        protected Boolean doInBackground(Void... voids) {
            showArchives = ArchiveParser.parseArchives();
            return Boolean.TRUE;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}

        @Override
        protected void onPostExecute(Boolean downloadSuccess) {
/*            for(Show show:showArchives) {
                archiveText.append("\n" + show.getName() + "\t" + show.getArchiveList().size());
                for(Archive archive:show.getArchiveList()) {
                    archiveText.append("\n" + archive.getUrl());
                }
            }*/
            archiveAdapter.addAll(showArchives);
        }
    }
}

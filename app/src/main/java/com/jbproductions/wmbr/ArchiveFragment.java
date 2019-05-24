package com.jbproductions.wmbr;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ArchiveFragment extends Fragment {

    public ArchiveFragment() {}

    TextView archiveText;
    List<Show> showArchives = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        archiveText = view.findViewById(R.id.archive_text);
        new LoadArchiveDataTask().execute();

        return view;
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
            for(Show show:showArchives) {
                archiveText.append("\n" + show.getName() + "\t" + show.getArchiveList().size());
                for(Archive archive:show.getArchiveList()) {
                    archiveText.append("\n" + archive.getUrl());
                }
            }
        }
    }
}

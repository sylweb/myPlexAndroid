package com.sylweb.myplex;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;


public class LibraryViewFragment extends Fragment {

    // the fragment initialization parameters
    private static final String LIBRARY_ID = "libraryId";

    public String libraryId;
    private GridView myGridView;

    public LibraryViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            libraryId = getArguments().getString(LIBRARY_ID);
        }
        View view = inflater.inflate(R.layout.fragment_library_full_view, container, false);
        this.myGridView = view.findViewById(R.id.library_content);

        ArrayList<VideoEntry> test = new ArrayList<>();
        for(int i=0; i < 100; i++) {
            test.add(new VideoEntry("test"+i, "", ""));
        }

        this.myGridView.setAdapter(new LibraryBaseAdapter(getActivity(), test));

        return view;
    }

}

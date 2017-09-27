package com.sylweb.myplex;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;


public class LibraryContentFragment extends Fragment implements View.OnClickListener {

    public Context context;
    public Integer libraryId;
    private GridView myGridView;
    private ImageView syncButton;

    private View view;

    private ArrayList<VideoEntry> videos = new ArrayList<>();

    public LibraryContentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.view = inflater.inflate(R.layout.fragment_library_full_view, container, false);
        this.myGridView = view.findViewById(R.id.library_content);
        this.syncButton = view.findViewById(R.id.syncImage);
        this.syncButton.setOnClickListener(this);

        loadLibraryVideos();

        return view;
    }

    public void loadLibraryVideos() {

        videos = VideoModel.getAllForLibrary(libraryId);
        this.myGridView.setAdapter(new LibraryContentAdapter(getActivity(), videos));
    }

    @Override
    public void onClick(View view) {
        if(view.equals(this.syncButton)) {
            LibraryUtils utils = new LibraryUtils();
            utils.updateLibrary(context,this.libraryId);
        }
    }
}

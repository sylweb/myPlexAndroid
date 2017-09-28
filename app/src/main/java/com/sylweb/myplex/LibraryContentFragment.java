package com.sylweb.myplex;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;


public class LibraryContentFragment extends Fragment implements View.OnClickListener , AdapterView.OnItemClickListener{

    public Context context;
    public Integer libraryId;

    private GridView myGridView;
    private boolean updating = false;


    private ImageView syncButton;

    protected MessageReceiver refreshReceiver;

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

        this.videos = VideoModel.getAllForLibrary(libraryId);
        this.myGridView.setAdapter(new LibraryContentAdapter(context, videos));
        this.myGridView.setOnItemClickListener(this);

        this.refreshReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this.context).registerReceiver(refreshReceiver,
                new IntentFilter("LibUpdate"));

        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.equals(this.syncButton) && !updating) {
            updating = true;
            RotateAnimation anim= new RotateAnimation(0f,350f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            anim.setInterpolator(new LinearInterpolator());
            anim.setRepeatCount(Animation.INFINITE);
            anim.setDuration(700);

            // Start animating the image
            syncButton.startAnimation(anim);

            LibraryUtils utils = new LibraryUtils();
            utils.updateLibrary(context,this.libraryId);
        }
    }

    private void updateData(Integer libId) {
        if(libId == this.libraryId) {
            this.videos = VideoModel.getAllForLibrary(libraryId);
            ((LibraryContentAdapter) this.myGridView.getAdapter()).data = this.videos;
            ((LibraryContentAdapter) this.myGridView.getAdapter()).notifyDataSetChanged();
            this.updating = false;
            syncButton.setAnimation(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this.context).unregisterReceiver(refreshReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.refreshReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this.context).registerReceiver(refreshReceiver,
                new IntentFilter("LibUpdate"));
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        VideoEntry video = (VideoEntry) adapterView.getItemAtPosition(i);
        Intent intent = new Intent(this.context, VideoDetailsActivity.class);
        intent.putExtra("SELECTED_VIDEO", video);
        intent.putExtra("LIBRARY_ID", this.libraryId);
        startActivity(intent);
    }

    //Message receiver
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().toString().equals("LibUpdate")) {
                updateData(intent.getIntExtra("libId", 0));
            }
        }
    }
}

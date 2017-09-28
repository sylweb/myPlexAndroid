package com.sylweb.myplex;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;


public class LibraryContentFragment extends Fragment implements AdapterView.OnItemClickListener{

    public Context context;
    public Integer libraryId;

    private GridView myGridView;

    protected MessageReceiver messageReceiver;

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

        this.myGridView.setAdapter(new LibraryContentAdapter(context, videos));
        this.myGridView.setOnItemClickListener(this);

        this.messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this.context).registerReceiver(messageReceiver,
                new IntentFilter("LIBRARY_SYNC_FINISHED"));
        LocalBroadcastManager.getInstance(this.context).registerReceiver(messageReceiver,
                new IntentFilter("VIDEO_DATA_READY"));

        updateData(this.libraryId);

        return view;
    }

    private void updateData(Integer libId) {
        if(libId == this.libraryId) {
            VideoModel.askForUpdate(this.context, this.libraryId);
            ((MainActivity) getActivity()).synchroFinished();
        }
    }

    private void displayData(Integer libraryId, ArrayList<VideoEntry> data) {
        if(libraryId == this.libraryId) {
            this.videos = data;
            ((LibraryContentAdapter) this.myGridView.getAdapter()).data = this.videos;
            ((LibraryContentAdapter) this.myGridView.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this.context).unregisterReceiver(messageReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this.context).registerReceiver(messageReceiver,
                new IntentFilter("LIBRARY_SYNC_FINISHED"));
        LocalBroadcastManager.getInstance(this.context).registerReceiver(messageReceiver,
                new IntentFilter("VIDEO_DATA_READY"));
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

            if(intent.getAction().toString().equals("LIBRARY_SYNC_FINISHED")) {
                updateData(intent.getIntExtra("LIBRARY_ID", 0));
            } else if(intent.getAction().toString().equals("VIDEO_DATA_READY")) {
                displayData(intent.getIntExtra("LIBRARY_ID", 0), (ArrayList<VideoEntry>)intent.getSerializableExtra("VIDEO_DATA"));
            }
        }
    }
}

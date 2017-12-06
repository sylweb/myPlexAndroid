package com.sylweb.myplex;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;


public class LibraryContentFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, TimePickerDialog.OnTimeSetListener{

    public Context context;
    public Integer libraryId;
    private TextView nbOfVideosTextView;
    public int gridPosition;

    private GridView myGridView;

    protected MessageReceiver messageReceiver;

    private ExplosionField mExplosion;

    private View view;

    private VideoEntry lastSelectedItem = null;

    public LibraryContentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.view = inflater.inflate(R.layout.fragment_library_full_view, container, false);
        this.nbOfVideosTextView = view.findViewById(R.id.nbOfVideos);
        this.myGridView = view.findViewById(R.id.library_content);
        this.myGridView.setOnItemClickListener(this);
        this.myGridView.setOnItemSelectedListener(this);

        this.mExplosion = ExplosionField.attach2Window(getActivity());
        return view;
    }

    private void updateData(Integer libId) {
        if(libId == this.libraryId) {
            VideoModel mod = new VideoModel();
            mod.askForUpdate(this.context, this.libraryId);
        }
    }

    private void displayData(Integer libraryId, ArrayList<VideoEntry> data) {
        if(libraryId == this.libraryId) {
            this.nbOfVideosTextView.setText(""+data.size()+" videos");
            if(this.myGridView == null) {

            }
            this.myGridView.setAdapter(new LibraryContentAdapter(context, data));


            if(this.gridPosition != 0) {
                for(int i=0; i < data.size(); i++) {
                    VideoEntry video = data.get(i);
                    if(video.id == this.gridPosition) {
                        this.myGridView.setSelection(i);
                        ((LibraryContentAdapter)this.myGridView.getAdapter()).setSelected(this.myGridView.getFirstVisiblePosition());
                        ((LibraryContentAdapter)this.myGridView.getAdapter()).notifyDataSetChanged();
                        break;
                    }
                }
            }


        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.context).unregisterReceiver(messageReceiver);
        this.myGridView.setAdapter(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this.context).registerReceiver(messageReceiver, new IntentFilter("LIBRARY_SYNC_FINISHED"));
        LocalBroadcastManager.getInstance(this.context).registerReceiver(messageReceiver, new IntentFilter("VIDEO_DATA_READY"));
        LocalBroadcastManager.getInstance(this.context).registerReceiver(messageReceiver, new IntentFilter("NEW_VIDEO_AVAILABLE"));
        LocalBroadcastManager.getInstance(this.context).registerReceiver(messageReceiver, new IntentFilter("EXPLOSION_FINISHED"));
        updateData(this.libraryId);
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        //Nice explosion effect -> then we will receive a message indicating the animation is finished (EXPLOSION_FINISHED)
        this.lastSelectedItem = (VideoEntry) adapterView.getItemAtPosition(i);
        this.mExplosion.explode(view);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        ((LibraryContentAdapter)this.myGridView.getAdapter()).setSelected(i);
        ((LibraryContentAdapter)this.myGridView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        Log.d("TEST","COUCOU");
    }

    //Message receiver
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().toString().equals("LIBRARY_SYNC_FINISHED")) {
                updateData(intent.getIntExtra("LIBRARY_ID", 0));
                if(intent.getExtras().getString("ERROR") != null) {
                    Toast.makeText(context,intent.getExtras().getString("ERROR"), Toast.LENGTH_LONG).show();
                }
                if(intent.getExtras().getString("ERROR2") != null) {
                    Toast.makeText(context,intent.getExtras().getString("ERROR2"), Toast.LENGTH_LONG).show();
                }

            }else if(intent.getAction().toString().equals("VIDEO_DATA_READY")) {
                displayData(intent.getIntExtra("LIBRARY_ID", 0), (ArrayList<VideoEntry>)intent.getSerializableExtra("VIDEO_DATA"));
            }
            else if(intent.getAction().toString().equals("NEW_VIDEO_AVAILABLE")) {
                if(myGridView.getAdapter() != null) {
                    ((LibraryContentAdapter) myGridView.getAdapter()).data.add((VideoEntry)intent.getSerializableExtra("NEW_VIDEO"));
                    ((LibraryContentAdapter) myGridView.getAdapter()).notifyDataSetChanged();
                    nbOfVideosTextView.setText(""+((LibraryContentAdapter) myGridView.getAdapter()).data.size());
                }
            }else if (intent.getAction().toString().equals("EXPLOSION_FINISHED")) {
                Intent myIntent = new Intent(context, VideoDetailsActivity.class);
                myIntent.putExtra("SELECTED_VIDEO", lastSelectedItem);
                myIntent.putExtra("LIBRARY_ID", libraryId);
                myIntent.putExtra("POSITION", lastSelectedItem.id);
                startActivity(myIntent);
            }
        }
    }
}

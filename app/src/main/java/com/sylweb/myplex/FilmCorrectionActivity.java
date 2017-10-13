package com.sylweb.myplex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class FilmCorrectionActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private EditText searchField;
    private ListView searchResultView;
    private Button searchButton;
    private MessageReceiver messageReceiver;
    private VideoEntry selectedVideo = new VideoEntry();
    private int libraryId;
    private int position;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_correction);

        this.searchField = (EditText)findViewById(R.id.searchFilm);
        this.searchResultView = (ListView)findViewById(R.id.filmList);
        this.searchButton = (Button)findViewById(R.id.searchButton);
        this.searchButton.setEnabled(true);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.progressBar.setVisibility(View.INVISIBLE);

        this.selectedVideo = (VideoEntry)getIntent().getExtras().getSerializable("SELECTED_VIDEO");
        this.libraryId = getIntent().getExtras().getInt("LIBRARY_ID");
        this.position = getIntent().getExtras().getInt("POSITION");

        this.searchField.setText(this.selectedVideo.file_url);
    }



    @Override
    public void onClick(View view) {
        if(this.searchButton.isEnabled()) {;
            this.searchButton.setEnabled(false);
            this.searchResultView.setAdapter(new SearchResultAdapter(this, new ArrayList<VideoEntry>()));
            this.progressBar.setVisibility(View.VISIBLE);
            LibraryUtils libUtil = new LibraryUtils();
            libUtil.getFilmsByName(this, this.searchField.getText().toString());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SearchResultAdapter adapt = (SearchResultAdapter) this.searchResultView.getAdapter();
        VideoEntry newVid = (VideoEntry) adapt.getItem(i);
        this.selectedVideo.name = newVid.name;
        this.selectedVideo.year = newVid.year;
        this.selectedVideo.overview = newVid.overview;
        this.selectedVideo.genres = newVid.genres;
        this.selectedVideo.tmdb_id = newVid.tmdb_id;

        try {

            File myjpg = new File(getString(R.string.image_location), newVid.tempPosterName);
            File directory = new File(getString(R.string.image_location) + "/");
            if (!directory.exists()) directory.mkdir();
            if (!myjpg.exists()) myjpg.createNewFile();
            OutputStream outputstream = new FileOutputStream(myjpg);
            newVid.tempImage.compress(Bitmap.CompressFormat.JPEG, 80, outputstream);
            outputstream.close();

            newVid.jpg_url = getString(R.string.image_location) + newVid.tempPosterName;

            this.selectedVideo.jpg_url = newVid.jpg_url;
            VideoModel mod = new VideoModel();
            mod.saveEntry(this.selectedVideo);
        }
        catch(Exception ex) {
            Toast.makeText(this, "ERREUR LORS DE LA MODIFICATION",Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Message receiver
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().toString().equals("SEARCH_FINISHED")) {
                searchButton.setEnabled(true);
                progressBar.setVisibility(View.INVISIBLE);
                searchResultView.setAdapter(new SearchResultAdapter(context, (ArrayList<VideoEntry>)intent.getExtras().getSerializable("DATA")));
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.messageReceiver);
        this.searchResultView.setAdapter(null);
        this.searchResultView.setOnItemClickListener(null);
        this.searchButton.setOnClickListener(null);
    }

    public void onResume() {
        super.onResume();
        this.messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("SEARCH_FINISHED"));
        this.searchResultView.setOnItemClickListener(this);
        this.searchButton.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, VideoDetailsActivity.class);
        intent.putExtra("SELECTED_VIDEO", this.selectedVideo);
        intent.putExtra("LIBRARY_ID", libraryId);
        intent.putExtra("POSITION", this.position);
        startActivity(intent);
    }
}

package com.sylweb.myplex;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class VideoDetailsActivity extends AppCompatActivity implements View.OnClickListener{

    private VideoEntry video;
    private int libraryId;
    private ImageView playButton;
    private ImageView poster;
    private int lastGridPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Détails du film");

        Intent intent = getIntent();
        this.video= (VideoEntry) intent.getExtras().getSerializable("SELECTED_VIDEO");
        this.libraryId = intent.getIntExtra("LIBRARY_ID", 0);
        this.lastGridPosition = intent.getIntExtra("POSITION", 0);


        loadData();
    }

    private void loadData() {
        ((TextView) findViewById(R.id.filmTitle)).setText(video.name);
        ((TextView) findViewById(R.id.filmYear)).setText(video.year);
        ((TextView) findViewById(R.id.filmOverview)).setText(video.overview);
        try {
            Drawable d = Drawable.createFromPath(video.jpg_url);
            this.poster = (ImageView) findViewById(R.id.detailPoster);
            this.poster.setImageDrawable(d);
            this.poster.setOnClickListener(this);
        }
        catch (Exception ex) {
            Log.e("ERROR", ex.getClass().getName());
        }

        this.playButton = (ImageView) findViewById(R.id.playButton);
        this.playButton.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("LIBRARY_ID", libraryId);
        intent.putExtra("POSITION", this.lastGridPosition);
        startActivity(intent);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("LIBRARY_ID", libraryId);
        intent.putExtra("POSITION", this.lastGridPosition);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if(view.equals(this.playButton) || view.equals(this.poster)) {

            //Play video using VLC, VLC is cool, it can play lot of different files and it can resume play where user stopped it
            if(video.file_url != null && !video.file_url.equals("")) {
                File f = new File(video.file_url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("org.videolan.vlc");
                intent.putExtra("from_start", false); //Get back where we stopped last time
                intent.setData(Uri.fromFile(f));
                startActivity(intent);
            }
        }
    }
}

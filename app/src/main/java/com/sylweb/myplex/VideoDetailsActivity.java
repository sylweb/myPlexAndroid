package com.sylweb.myplex;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class VideoDetailsActivity extends AppCompatActivity implements View.OnClickListener, MenuItem.OnMenuItemClickListener{

    private VideoEntry video;
    private int libraryId;
    private ImageView playButton;
    private ImageView poster;
    private int lastGridPosition;
    private MenuItem changeFilmItem;
    private MenuItem deleteFilmItem;

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

        GenreModel mod = new GenreModel();
        ArrayList<GenreEntry> genres = mod.getGenreForVideo(video.id);

        if(genres != null && genres.size() > 0) {

            String genreTxt = "Genre(s) : ";
            for(GenreEntry genre : genres) {
                genreTxt+=genre.name;
                genreTxt += ",";
            }
            genreTxt = genreTxt.substring(0,genreTxt.lastIndexOf(","));
            ((TextView) findViewById(R.id.filmGenres)).setText(genreTxt);
        }
        else {
            ((TextView) findViewById(R.id.filmGenres)).setText("Genre : inconnu");
        }
        ((TextView) findViewById(R.id.filmYear)).setText(video.year);
        ((TextView) findViewById(R.id.filmOverview)).setText("Synopsis : \r\n\r\n"+video.overview);
        this.poster = (ImageView) findViewById(R.id.detailPoster);
        try {
            if(!video.jpg_url.equals("")) {
                Bitmap myBitmap = BitmapFactory.decodeFile(video.jpg_url);
                this.poster.setImageBitmap(myBitmap);

            }else {
                this.poster.setImageResource(R.mipmap.icon_dvd);
            }
            this.poster.setOnClickListener(this);
        }
        catch (Exception ex) {
            Log.e("ERROR", ex.getClass().getName());
        }

        this.playButton = (ImageView) findViewById(R.id.playButton);
        this.playButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        this.changeFilmItem = menu.findItem(R.id.change_film);
        this.changeFilmItem.setVisible(true);
        this.changeFilmItem.setOnMenuItemClickListener(this);

        this.deleteFilmItem = menu.findItem(R.id.delete_film);
        this.deleteFilmItem.setVisible(true);
        this.deleteFilmItem.setOnMenuItemClickListener(this);

        return true;
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.poster = null;
        this.playButton = null;
        this.video = null;
        Runtime.getRuntime().gc();
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onClick(View view) {
        if(view.equals(this.playButton) || view.equals(this.poster)) {

            //Play video using VLC, VLC is cool, it can play lot of different files and it can resume play where user stopped it
            if(video.file_url != null && !video.file_url.equals("")) {
                try {
                    File f = new File(video.file_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("org.videolan.vlc");
                    intent.putExtra("from_start", false); //Get back where we stopped last time
                    intent.setData(Uri.fromFile(f));
                    startActivity(intent);
                }
                catch(ActivityNotFoundException ex) {
                    Toast.makeText(this, "Veuillez installer VLC", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        if(menuItem.equals(this.changeFilmItem)) {
            Intent intent = new Intent(this, FilmCorrectionActivity.class);
            intent.putExtra("LIBRARY_ID", this.libraryId);
            intent.putExtra("POSITION", this.lastGridPosition);
            intent.putExtra("SELECTED_VIDEO", video);
            startActivity(intent);
        }
        else if(menuItem.equals(this.deleteFilmItem)) {
            VideoModel mod = new VideoModel();
            mod.deleteEntry(this.video);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("LIBRARY_ID", libraryId);
            intent.putExtra("POSITION", this.lastGridPosition);
            startActivity(intent);
        }
        return false;
    }
}

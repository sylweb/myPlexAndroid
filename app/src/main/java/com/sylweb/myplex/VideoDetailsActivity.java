package com.sylweb.myplex;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

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
    private MenuItem markAsSeenItem;
    private MenuItem markAsUnseenItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.video_details_activity_title));

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

            String genreTxt = getString(R.string.gender_text)+" : ";
            for(GenreEntry genre : genres) {
                genreTxt+=genre.name;
                genreTxt += ", ";
            }
            genreTxt = genreTxt.substring(0,genreTxt.lastIndexOf(","));
            ((TextView) findViewById(R.id.filmGenres)).setText(genreTxt);
        }
        else {
            ((TextView) findViewById(R.id.filmGenres)).setText(getString(R.string.gender_text)+" : inconnu");
        }
        ((TextView) findViewById(R.id.filmYear)).setText(getString(R.string.year_text)+ " : "+ video.year);
        ((TextView) findViewById(R.id.filmOverview)).setText(getString(R.string.overview_text)+" : \r\n\r\n"+video.overview);
        this.poster = (ImageView) findViewById(R.id.detailPoster);
        try {
            if(!video.big_jpg_url.equals("")) {
                Bitmap myBitmap = BitmapFactory.decodeFile(video.big_jpg_url);
                this.poster.setImageBitmap(myBitmap);

            }else {
                this.poster.setImageResource(R.mipmap.icon_dvd);
            }

        }
        catch (Exception ex) {
            Log.e("ERROR", ex.getClass().getName());
        }

        this.playButton = (ImageView) findViewById(R.id.playButton);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        this.changeFilmItem = menu.findItem(R.id.change_film);
        this.changeFilmItem.setVisible(true);

        this.deleteFilmItem = menu.findItem(R.id.delete_film);
        this.deleteFilmItem.setVisible(true);

        this.markAsSeenItem = menu.findItem(R.id.mark_as_seen);
        this.markAsSeenItem.setVisible(true);

        this.markAsUnseenItem = menu.findItem(R.id.mark_as_unseen);
        this.markAsUnseenItem.setVisible(true);

        this.changeFilmItem.setOnMenuItemClickListener(this);
        this.deleteFilmItem.setOnMenuItemClickListener(this);
        this.markAsSeenItem.setOnMenuItemClickListener(this);
        this.markAsUnseenItem.setOnMenuItemClickListener(this);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        backToPreviousScreen();
        return true;
    }

    @Override
    public void onBackPressed() {
        backToPreviousScreen();
    }

    private void backToPreviousScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("LIBRARY_ID", libraryId);
        intent.putExtra("POSITION", this.lastGridPosition);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.playButton.setOnClickListener(this);
        this.poster.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.playButton.setOnClickListener(null);
        this.poster.setOnClickListener(null);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("KEY EVENT","Key code = "+keyCode);

        switch (keyCode) {
            case 66 : //ENTER
                this.onClick(this.playButton);
                break;
            case 67 : //BACK
                this.onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if(view.equals(this.playButton) || view.equals(this.poster)) {

            //Play video using VLC, VLC is cool, it can play lot of different files and it can resume play where user stopped it
            if(video.file_url != null && !video.file_url.equals("")) {
                try {
                    File f = new File(video.file_url);
                    /*Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("org.videolan.vlc");
                    intent.putExtra("from_start", false); //Get back where we stopped last time
                    intent.setData(Uri.fromFile(f));
                    startActivity(intent);*/

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(f));
                    intent.setDataAndType(Uri.fromFile(f), "video/*");
                    startActivity(intent);

                    VideoModel mod = new VideoModel();
                    mod.tagVideoAsViewed(video);
                }
                catch(ActivityNotFoundException ex) {
                    Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
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
        else if(menuItem.equals(this.markAsSeenItem)) {
            VideoModel mod = new VideoModel();
            mod.tagVideoAsViewed(video);
        }
        else if(menuItem.equals(this.markAsUnseenItem)) {
            VideoModel mod = new VideoModel();
            mod.tagVideoAsNotViewed(video);
        }
        return false;
    }
}

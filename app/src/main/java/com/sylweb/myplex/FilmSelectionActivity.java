package com.sylweb.myplex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class FilmSelectionActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText searchField;
    private ListView searchResultView;
    private Button searchButton;
    private MessageReceiver messageReceiver;
    private boolean searchRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_selection);

        this.searchField = (EditText)findViewById(R.id.searchFilm);
        this.searchResultView = (ListView)findViewById(R.id.filmList);

        getIntent().getExtras().getString("FILE_NAME");
        this.searchField.setText(getIntent().getExtras().getString("FILE_NAME"));

        this.searchButton = (Button)findViewById(R.id.searchButton);
        this.searchButton.setOnClickListener(this);

        this.messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("SEARCH_FINISHED"));
    }



    @Override
    public void onClick(View view) {
        if(!this.searchRunning) {
            this.searchRunning = true;
            LibraryUtils libUtil = new LibraryUtils();
            libUtil.getFilmsByName(this, this.searchField.getText().toString());
        }
    }

    //Message receiver
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().toString().equals("SEARCH_FINISHED")) {
                searchRunning = false;
                searchResultView.setAdapter(new SearchResultAdapter(context, (ArrayList<VideoEntry>)intent.getExtras().getSerializable("DATA")));
            }

        }
    }
}

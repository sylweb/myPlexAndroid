package com.sylweb.myplex;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private ArrayList<LibraryEntry> libraryList;
    private ListView myList;
    private ImageView syncImage;
    private int currentLibraryId;
    private boolean synchroRunning;
    private MessageReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First init local DB
        DBManager db = new DBManager();
        db.initDB(this.getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadLibraryList();

        if(getIntent() != null && getIntent().getExtras() != null) {
            int libId = getIntent().getIntExtra("LIBRARY_ID", 0);
            if(libId > 0) {
                int position = getIntent().getIntExtra("POSITION", 0);
                showLibFragment(libId, position);
            }
        }
        else {
            if(this.libraryList != null && this.libraryList.size() > 0) {
                showLibFragment(libraryList.get(0).id, 0);
            }
        }

        this.messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("LIBRARY_SYNC_FINISHED"));
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("LIBRARY_LIST_MODIFIED"));

        this.synchroRunning = false;
    }

    private void showLibFragment(int libId, int position) {
        this.currentLibraryId = libId;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        LibraryContentFragment frag = new LibraryContentFragment();
        frag.libraryId = libId;
        frag.context = this.getApplicationContext();
        frag.gridPosition = position;
        ft.replace(R.id.content_frame, frag);
        ft.commit();
    }

    private void loadLibraryList() {

        LibraryModel mod = new LibraryModel();
        this.libraryList = mod.getAll();
        if(libraryList != null) {
            this.myList = (ListView) findViewById(R.id.library_list);
            this.myList.setAdapter(new LibraryListAdapter(this, this.libraryList));
            this.myList.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        LibraryEntry item = (LibraryEntry) adapterView.getItemAtPosition(i);
        showLibFragment(item.id, 0);
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        this.syncImage = new ImageView(this);
        this.syncImage.setImageResource(R.mipmap.download);
        this.syncImage.setColorFilter(Color.parseColor("#FFFFFFFF"), PorterDuff.Mode.SRC_ATOP);
        this.syncImage.setOnClickListener(this);
        (menu.findItem(R.id.sync)).setActionView(this.syncImage);
        (menu.findItem(R.id.sync)).setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_library) {
            startActivity(new Intent(this, LibraryCreationActivity.class));
        } else if (id == R.id.delete_library) {
            RemoveLibraryDialog diag = new RemoveLibraryDialog(this);
            diag.show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onClick(View view) {

        if(view.equals(this.syncImage) && !synchroRunning && currentLibraryId != 0) {

            this.synchroRunning = true;
            RotateAnimation anim = new RotateAnimation(0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setInterpolator(new LinearInterpolator());
            anim.setRepeatCount(Animation.INFINITE);
            anim.setDuration(700);

            this.syncImage.startAnimation(anim);

            LibraryUtils utils = new LibraryUtils();
            utils.updateLibrary(this.getApplicationContext(),this.currentLibraryId);
        }
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
                new IntentFilter("LIBRARY_SYNC_FINISHED"));
    }

    //Message receiver
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().toString().equals("LIBRARY_SYNC_FINISHED")) {
                synchroRunning = false;
                syncImage.setAnimation(null);
            }
            else if(intent.getAction().toString().equals("LIBRARY_LIST_MODIFIED")) {
                LibraryModel mod = new LibraryModel();
                libraryList = mod.getAll();
                ((LibraryListAdapter)myList.getAdapter()).data = libraryList;
                ((LibraryListAdapter)myList.getAdapter()).notifyDataSetChanged();
            }
        }
    }

}

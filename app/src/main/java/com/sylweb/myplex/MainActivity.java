package com.sylweb.myplex;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    private ArrayList<LibraryEntry> libraryList;
    private ListView myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //First init local DB
        DBManager.initDB(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.content_frame);

        loadLibraryList();

        if(getIntent() != null && getIntent().getExtras() != null) {
            int libId = getIntent().getIntExtra("LIBRARY_ID", 0);
            if(libId > 0) {
                showLibFragment(libId);
            }
        }
    }

    private void showLibFragment(int libId) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        LibraryContentFragment frag = new LibraryContentFragment();
        frag.libraryId = libId;
        frag.context = this;
        ft.replace(R.id.content_frame, frag);
        ft.commit();
    }

    private void loadLibraryList() {

        this.libraryList = LibraryModel.getAll();
        if(libraryList != null) {
            this.myList = (ListView) findViewById(R.id.library_list);
            this.myList.setAdapter(new LibraryListAdapter(this, this.libraryList));
            this.myList.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        LibraryEntry item = (LibraryEntry) adapterView.getItemAtPosition(i);
        showLibFragment(item.id);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_library) {
            startActivity(new Intent(this, LibraryCreationActivity.class));
        } else if (id == R.id.delete_library) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class TestThread extends Thread {

        @Override
        public void run() {

            //Example de recherche de tout les films du nom de fight club
            //String url = "https://api.themoviedb.org/3/search/movie?api_key=c15ed3307384c1d73034f5fe889cd871&query=fight+club";

            //Example de lecture de la fiche d'un film dont l'id est 550
            //String url = "https://api.themoviedb.org/3/movie/550?api_key=c15ed3307384c1d73034f5fe889cd871&language=fr";

        }

    }
}

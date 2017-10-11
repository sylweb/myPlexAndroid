package com.sylweb.myplex;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sylvain on 27/09/2017.
 */

public class VideoModel {

    public void saveEntry(VideoEntry vid) {

        String query = "SELECT * FROM video WHERE id = %d AND library_id = %d";
        query =String.format(query, vid.id, vid.library_id) ;
        DBManager db = new DBManager();
        ArrayList results = db.executeQuery(query);
        if(results ==null || results.size() < 1) {
            insertEntry(vid);
        }else updateEntry(vid);
    }

    public void updateEntry(VideoEntry vid) {

        vid.name = vid.name.replace("'","''");
        vid.overview = vid.overview.replace("'","''");
        vid.file_url = vid.file_url.replace("'", "''");

        String query = "UPDATE video SET library_id=%d, tmdb_id = %d, name='%s', overview='%s',year='%s',file_url='%s',jpg_url='%s' WHERE id = %s";
        query = String.format(query, vid.library_id, vid.tmdb_id, vid.name, vid.overview, vid.year, vid.file_url,vid.jpg_url, vid.id);

        DBManager db = new DBManager();
        db.executeQuery(query);

        for(GenreEntry entry : vid.genres) {
            GenreModel mod = new GenreModel();
            mod.saveGenreForVideo(entry, vid.id);
        }
    }

    public void insertEntry(VideoEntry vid) {

        vid.name = vid.name.replace("'","''");
        vid.overview = vid.overview.replace("'","''");
        vid.file_url = vid.file_url.replace("'", "''");

        String query = "INSERT INTO video(tmdb_id,library_id,name,overview,year,file_url,jpg_url,forced) VALUES(%d,%d,'%s','%s','%s','%s','%s',%d)";
        query = String.format(query, vid.tmdb_id, vid.library_id, vid.name, vid.overview, vid.year, vid.file_url,vid.jpg_url,0);
        DBManager db = new DBManager();
        db.executeQuery(query);

        int newId = 0;
        query = "SELECT max(id) FROM video";
        ArrayList result = db.executeQuery(query);
        if(result != null && result.size() > 0) {
            HashMap record = (HashMap) result.get(0);
            newId = Integer.valueOf((String)record.get("max(id)"));
        }

        for(GenreEntry entry : vid.genres) {
            GenreModel mod = new GenreModel();
            mod.saveGenreForVideo(entry, newId);
        }
    }

    public void deleteEntry(VideoEntry vid) {
        String query = "DELETE FROM video WHERE id = "+vid.id;
        DBManager db = new DBManager();
        db.executeQuery(query);
    }

    public ArrayList<VideoEntry> getAllForLibrary(Integer libraryId) {
        String query = "SELECT * FROM video WHERE library_id = %d ORDER BY name ASC";
        query = String.format(query, libraryId);
        DBManager db = new DBManager();
        ArrayList results = db.executeQuery(query);
        ArrayList<VideoEntry> all = new ArrayList<>();
        if(results != null && results.size() > 0) {
            for(int i=0; i < results.size(); i++) {
                all.add(new VideoEntry(results.get(i)));
            }
        }
        return all;
    }

    public void askForUpdate(Context c, int libraryId) {
        new ReadAllThread(c,libraryId).start();
    }

    public class ReadAllThread extends Thread {

        private int libraryId;
        private Context context;

        public ReadAllThread(Context c, int libraryId) {
            this.context = c;
            this.libraryId = libraryId;
        }

        @Override
        public void run() {
            ArrayList<VideoEntry> videos = getAllForLibrary(this.libraryId);
            Intent intent = new Intent("VIDEO_DATA_READY");
            intent.putExtra("VIDEO_DATA", videos);
            intent.putExtra("LIBRARY_ID", this.libraryId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

}

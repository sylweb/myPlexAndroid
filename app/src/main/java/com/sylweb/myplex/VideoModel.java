package com.sylweb.myplex;

import java.util.ArrayList;

/**
 * Created by sylvain on 27/09/2017.
 */

public class VideoModel {

    public static void saveEntry(VideoEntry vid) {

        String query = "SELECT * FROM video WHERE tmdb_id = " + vid.tmdb_id;
        ArrayList results = DBManager.executeQuery(query);
        if(results ==null || results.size() < 1) {
            insertEntry(vid);
        }else updateEntry(vid);
    }

    public static void updateEntry(VideoEntry vid) {
        String query = "UPDATE video SET library_id=%d, name='%s', overview='%s',year='%s',file_url='%s',jpg_url='%s' WHERE tmdb_id = %d";
        query = String.format(query, vid.library_id, vid.name, vid.overview, vid.year, vid.file_url,vid.jpg_url, vid.tmdb_id);
        DBManager.executeQuery(query);
    }

    public static void insertEntry(VideoEntry vid) {
        String query = "INSERT INTO video(tmdb_id,library_id,name,overview,year,file_url,jpg_url) VALUES(%d,%d,'%s','%s','%s','%s','%s')";
        query = String.format(query, vid.tmdb_id, vid.library_id, vid.name, vid.overview, vid.year, vid.file_url,vid.jpg_url);
        DBManager.executeQuery(query);
    }

    public static ArrayList<VideoEntry> getAllForLibrary(Integer libraryId) {
        String query = "SELECT * FROM video WHERE library_id = %d ORDER BY name DESC";
        query = String.format(query, libraryId);
        ArrayList results = DBManager.executeQuery(query);
        ArrayList<VideoEntry> all = new ArrayList<>();
        if(results != null && results.size() > 0) {
            for(int i=0; i < results.size(); i++) {
                all.add(new VideoEntry(results.get(i)));
            }
        }
        return all;
    }

}

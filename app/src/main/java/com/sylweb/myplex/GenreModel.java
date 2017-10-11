package com.sylweb.myplex;

import java.util.ArrayList;

/**
 * Created by sylvain on 29/09/2017.
 */

public class GenreModel {

    GenreEntry getGenre(int id) {
        String query = "SELECT * FROM genre WHERE id = %d";
        query = String.format(query, id);
        DBManager db = new DBManager();
        ArrayList results = db.executeQuery(query);
        if(results != null && results.size() > 0) return new GenreEntry(results.get(0));
        else return null;
    }

    void save(GenreEntry entry) {
        String query = "SELECT id FROM genre WHERE id = "+entry.id;
        DBManager db = new DBManager();
        ArrayList results = db.executeQuery(query);
        if(results != null && results.size() > 0) update(entry);
        else insert(entry);
    }

    void update(GenreEntry entry) {
        String query = "UPDATE genre SET name = '%s' WHERE id = %d";
        query = String.format(query, entry.name, entry.id);
        DBManager db = new DBManager();
        db.executeQuery(query);
    }

    void insert(GenreEntry entry) {
        String query = "INSERT INTO genre(id, name) VALUES(%d,'%s')";
        query = String.format(query, entry.id, entry.name);
        DBManager db = new DBManager();
        db.executeQuery(query);
    }

    ArrayList<GenreEntry> getGenreForVideo(int videoId) {
        String query = "SELECT * FROM genre INNER JOIN video_genres WHERE video_genres.genre_id = genre.id AND video_genres.video_id = %d";
        query = String.format(query, videoId);
        DBManager db = new DBManager();
        ArrayList results = db.executeQuery(query);
        ArrayList<GenreEntry> genres = new ArrayList();
        if(results != null) {
            for(int i=0; i < results.size(); i++) {
                genres.add(new GenreEntry(results.get(i)));
            }
        }
        return genres;
    }

    void saveGenreForVideo(GenreEntry entry, int videoId) {
        String query = "SELECT * FROM video_genres WHERE video_id = %d AND genre_id = %d";
        query = String.format(query, videoId, entry.id);
        DBManager db = new DBManager();
        ArrayList results = db.executeQuery(query);
        if(results != null && results.size() > 0) {
            query = "DELETE FROM video_genres WHERE video_id = %s";
            query = String.format(query, videoId);
            db.executeQuery(query);
        }
        query = "INSERT INTO video_genres(video_id,genre_id) VALUES(%d,%d)";
        query = String.format(query, videoId, entry.id);
        db.executeQuery(query);
    }

}

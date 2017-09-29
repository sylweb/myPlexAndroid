package com.sylweb.myplex;

import java.util.ArrayList;

/**
 * Created by sylvain on 29/09/2017.
 */

public class GenreModel {

    static GenreEntry getGenre(int id) {
        String query = "SELECT * FROM genre WHERE id = %d";
        query = String.format(query, id);
        ArrayList results = DBManager.executeQuery(query);
        if(results != null && results.size() > 0) return new GenreEntry(results.get(0));
        else return null;
    }

    static void save(GenreEntry entry) {
        String query = "SELECT id FROM genre WHERE id = "+entry.id;
        ArrayList results = DBManager.executeQuery(query);
        if(results != null && results.size() > 0) update(entry);
        else insert(entry);
    }

    static void update(GenreEntry entry) {
        String query = "UPDATE genre SET name = '%s' WHERE id = %d";
        query = String.format(query, entry.name, entry.id);
        DBManager.executeQuery(query);
    }

    static void insert(GenreEntry entry) {
        String query = "INSERT INTO genre(id, name) VALUES(%d,'%s')";
        query = String.format(query, entry.id, entry.name);
        DBManager.executeQuery(query);
    }

    static ArrayList<GenreEntry> getGenreForVideo(int tmdbId) {
        String query = "SELECT * FROM genre INNER JOIN video_genres WHERE video_genres.genre_id = genre.id AND video_genres.video_id = %d";
        query = String.format(query, tmdbId);
        ArrayList results = DBManager.executeQuery(query);
        ArrayList<GenreEntry> genres = new ArrayList();
        if(results != null) {
            for(int i=0; i < results.size(); i++) {
                genres.add(new GenreEntry(results.get(i)));
            }
        }
        return genres;
    }

    static void saveGenreForVideo(GenreEntry entry, int tmdbId) {
        String query = "SELECT * FROM video_genres WHERE video_id = %d AND genre_id = %d";
        query = String.format(query, tmdbId, entry.id);
        ArrayList results = DBManager.executeQuery(query);
        if(results == null || results.size() == 0) {
            query = "INSERT INTO video_genres(video_id,genre_id) VALUES(%d,%d)";
            query = String.format(query, tmdbId, entry.id);
            DBManager.executeQuery(query);
        }
    }

}

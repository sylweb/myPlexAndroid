package com.sylweb.myplex;

import java.util.ArrayList;

/**
 * Created by sylvain on 27/09/2017.
 */

public class LibraryModel {

    public static void saveEntry(LibraryEntry lib) {
        String query = "INSERT INTO Library(name,url) VALUES('%s','%s')";
        query = String.format(query, lib.name, lib.url);
        DBManager.executeQuery(query);
    }

    public static LibraryEntry getFromId(Integer id) {
        String query = "SELECT * FROM library WHERE id = %d LIMIT 1";
        query = String.format(query, id);
        ArrayList results = DBManager.executeQuery(query);
        if(results!= null && results.size() > 0) {
            return new LibraryEntry(results.get(0));
        }
        else return null;
    }

    public static boolean isNameAvailable(String name) {
        String query = "SELECT id FROM library WHERE name LIKE '%s'";
        query = String.format(query, name);
        ArrayList results = DBManager.executeQuery(query);
        if(results != null && results.size() > 0) return false;
        else return true;
    }

    public static ArrayList<LibraryEntry> getAll() {
        String query = "SELECT * FROM library ORDER BY name ASC";
        ArrayList results = DBManager.executeQuery(query);
        ArrayList<LibraryEntry> all = new ArrayList<>();
        if(results!= null && results.size() > 0) {
            for(int i=0; i < results.size(); i++) {
                all.add(new LibraryEntry(results.get(i)));
            }
        }
        return all;
    }

    public static void removeLibrary(Integer libId) {
        String query = "DELETE FROM video WHERE library_id = %d";
        query = String.format(query, libId);
        DBManager.executeQuery(query);

        query = "DELETE FROM library WHERE id = %d";
        query = String.format(query, libId);
        DBManager.executeQuery(query);
    }
}

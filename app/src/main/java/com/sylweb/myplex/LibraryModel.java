package com.sylweb.myplex;

import java.util.ArrayList;

/**
 * Created by sylvain on 27/09/2017.
 */

public class LibraryModel {

    public void saveEntry(LibraryEntry lib) {
        String query = "INSERT INTO Library(name,url) VALUES('%s','%s')";
        query = String.format(query, lib.name, lib.url);
        DBManager db = new DBManager();
        db .executeQuery(query);
    }

    public LibraryEntry getFromId(Integer id) {
        String query = "SELECT * FROM library WHERE id = %d LIMIT 1";
        query = String.format(query, id);
        DBManager db =new DBManager();
        ArrayList results = db.executeQuery(query);
        if(results!= null && results.size() > 0) {
            return new LibraryEntry(results.get(0));
        }
        else return null;
    }

    public boolean isNameAvailable(String name) {
        String query = "SELECT id FROM library WHERE name LIKE '%s'";
        query = String.format(query, name);
        DBManager db = new DBManager();
        ArrayList results = db.executeQuery(query);
        if(results != null && results.size() > 0) return false;
        else return true;
    }

    public ArrayList<LibraryEntry> getAll() {
        String query = "SELECT * FROM library ORDER BY name ASC";
        DBManager db = new DBManager();
        ArrayList results = db.executeQuery(query);
        ArrayList<LibraryEntry> all = new ArrayList<>();
        if(results!= null && results.size() > 0) {
            for(int i=0; i < results.size(); i++) {
                all.add(new LibraryEntry(results.get(i)));
            }
        }
        return all;
    }

    public void removeLibrary(Integer libId) {
        String query = "DELETE FROM video WHERE library_id = %d";
        query = String.format(query, libId);
        DBManager db = new DBManager();
        db.executeQuery(query);

        query = "DELETE FROM library WHERE id = %d";
        query = String.format(query, libId);
        db.executeQuery(query);
    }
}

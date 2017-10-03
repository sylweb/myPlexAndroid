package com.sylweb.myplex;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sylvain on 26/09/2017.
 */

public class VideoEntry implements Serializable{

    public Integer id;
    public Integer tmdb_id;
    public Integer library_id;
    public String name;
    public String overview;
    public String year;
    public String file_url;
    public String jpg_url;

    public ArrayList<GenreEntry> genres = new ArrayList<GenreEntry>();

    public VideoEntry(Object data) {
        HashMap entry = (HashMap) data;
        this.id = Integer.valueOf((String)entry.get("id"));
        this.tmdb_id = Integer.valueOf((String) entry.get("tmdb_id"));
        this.library_id = Integer.valueOf((String) entry.get("library_id"));
        this.name = (String) entry.get("name");
        this.overview = (String) entry.get("overview");
        this.year = (String) entry.get("year");
        this.file_url = (String) entry.get("file_url");
        this.jpg_url = (String) entry.get("jpg_url");
    }

    public VideoEntry() {

    }
}

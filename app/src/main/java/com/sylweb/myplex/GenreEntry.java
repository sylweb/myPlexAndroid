package com.sylweb.myplex;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by sylvain on 29/09/2017.
 */

public class GenreEntry {

    public int id;
    public String name;

    public GenreEntry(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public GenreEntry(Object data) {
        HashMap entry = (HashMap) data;
        this.id = Integer.valueOf((String)entry.get("id"));
        this.name = (String) entry.get("name");
    }

    public GenreEntry(JSONObject data) {
        try {
            this.id = (int)data.get("id");
            this.name = (String)data.get("name");
        }
        catch(Exception ex) {

        }
    }

}

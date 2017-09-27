package com.sylweb.myplex;

import java.util.HashMap;

/**
 * Created by sylvain on 27/09/2017.
 */

public class LibraryEntry {

    public Integer id;
    public String name;
    public String url;

    public LibraryEntry(String name, String sourceDirectory) {
        this.name = name;
        this.url = sourceDirectory;
    }

    public LibraryEntry(Object data) {
        HashMap entry = (HashMap) data;
        this.id = Integer.valueOf((String)entry.get("id"));
        this.name = (String)entry.get("name");
        this.url =(String)entry.get("url");
    }
}

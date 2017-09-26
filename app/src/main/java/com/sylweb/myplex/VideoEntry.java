package com.sylweb.myplex;

import java.util.ArrayList;

/**
 * Created by sylvain on 26/09/2017.
 */

public class VideoEntry {

    public String name;
    public String summary;
    public String year;
    public ArrayList categories;

    public VideoEntry(String name, String summary, String year) {
        this.name = name;
        this.summary = summary;
        this.year = year;
        this.categories = new ArrayList();
    }
    

}

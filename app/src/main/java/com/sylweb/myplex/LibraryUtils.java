package com.sylweb.myplex;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by sylvain on 27/09/2017.
 */

public class LibraryUtils {

    private Context context;

    public void updateLibrary(Context context, Integer libraryId) {

        this.context = context;
        UpdateThread thread = new UpdateThread(libraryId);
        thread.start();
    }

    public class UpdateThread extends Thread {

        private LibraryEntry lib;

        public UpdateThread(Integer id) {
            lib = LibraryModel.getFromId(id);
        }

        @Override
        public void run() {

            //First update genre database from the movie DB
            updateGenreDB();

            //Then find all files in the library directory
            ArrayList<String> files = getAllFiles();


            ArrayList<Integer> readIds = new ArrayList<>();
            ArrayList<String> unidentifiedFiles = new ArrayList<>();

            //For each found file...
            for(String filename : files) {

                //If we already have this file in DB for this library then skip it
                String query = "SELECT id FROM video WHERE library_id = %d AND file_url like'%s'";
                String fileURL = lib.url+"/"+filename;
                query = String.format(query, lib.id, fileURL);
                ArrayList results = DBManager.executeQuery(query);
                if(results != null && results.size()>0) continue;

                //If we don't have this video yet, then try to gather information about it from TheMovieDB
                VideoEntry newVid = getFilmWithInfo(filename);
                if(newVid != null) {

                    //Complete with some local info
                    newVid.library_id = lib.id;
                    newVid.file_url = fileURL;

                    //Insert movie in local DB
                    VideoModel.saveEntry(newVid);

                    readIds.add(newVid.tmdb_id);
                }

                else unidentifiedFiles.add(filename);
            }

            //Job finished so tell the observer(s)
            Intent intent = new Intent("LIBRARY_SYNC_FINISHED");
            intent.putExtra("LIBRARY_ID", lib.id);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            //We also built a list of all files that couldn't be identified so display it in google keep
            if(unidentifiedFiles.size() > 0) {
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Impossible d'identifier ces fichers");
                intent.setPackage("com.google.android.keep");
                String unidentied = "";
                for (String filename : unidentifiedFiles) {
                    unidentied = unidentied + filename + "\r\n";
                }
                intent.putExtra(Intent.EXTRA_TEXT, unidentied);

                context.startActivity(Intent.createChooser(intent, "Afficher un texte"));
            }
        }

        private void updateGenreDB() {

            String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=c15ed3307384c1d73034f5fe889cd871&language=fr-FR";

            try {
                JSONObject answer = HttpRequestHelper.executeGET(url);
                JSONArray results = null;
                if (answer != null) results = answer.getJSONArray("genres");
                if (results != null && results.length() > 0) {


                    for(int i=0; i < results.length(); i++) {

                        JSONObject entry = (JSONObject) results.get(i);
                        int id = (int) entry.get("id");
                        String name = (String) entry.get("name");

                        GenreEntry genre = new GenreEntry(id,name);
                        GenreModel.save(genre);
                    }

                }
            }
            catch (Exception ex) {

            }
        }

        private ArrayList<String> getAllFiles() {
            ArrayList<String> files = new ArrayList<>();
            File directory = new File(lib.url);
            File[] f = directory.listFiles();
            for (int i = 0; i < f.length; i++)
            {
                if(!f[i].isDirectory()) files.add(f[i].getName());
            }
            return files;
        }

        private VideoEntry getFilmWithInfo(String filename) {

            String file_name = filename;

            if(filename.lastIndexOf(".") > 0) file_name = file_name.substring(0, filename.lastIndexOf("."));
            file_name = file_name.replace("[","");
            file_name = file_name.replace("]","");
            file_name = file_name.replace("{","");
            file_name = file_name.replace("}","");
            file_name = file_name.replace("."," ");
            file_name = file_name.replace("_"," ");
            file_name = file_name.replace(" ", "%20");

            String url = "https://api.themoviedb.org/3/search/movie?api_key=c15ed3307384c1d73034f5fe889cd871&language=fr&query="+file_name;

            try {
                JSONObject answer = HttpRequestHelper.executeGET(url);
                JSONArray results = null;
                if(answer != null) results = answer.getJSONArray("results");
                if(results != null && results.length() > 0) {

                    file_name = file_name.replace("%20", " ");
                    file_name = file_name.toUpperCase();

                    int diff = 1000000;
                    Integer chosenId = -1;

                    for(int i=0; i < results.length(); i++) {

                        JSONObject entry = (JSONObject) results.get(i);
                        String title = ((String) entry.get("title")).toUpperCase();
                        String originalTitle = ((String) entry.get("original_title")).toUpperCase();

                        int res1 = levenshteinDistance(title, file_name);
                        int res2 = levenshteinDistance(originalTitle,file_name);

                        int mini = res1;
                        if(res2 < mini) mini = res2;

                        if (mini < diff) {
                            chosenId = i;
                            diff = mini;
                        }
                    }

                    if (chosenId > -1) {

                        JSONObject entry = (JSONObject) results.get(chosenId);

                        VideoEntry vid = new VideoEntry();
                        vid.tmdb_id = (Integer) entry.get("id");
                        vid.name = (String) entry.get("title");
                        vid.name = vid.name.replace("'", "''");
                        vid.year = ((String) entry.get("release_date")).substring(0, 4);
                        vid.overview = (String) entry.get("overview");
                        vid.overview = vid.overview.replace("'", "''");

                        JSONArray genres = entry.getJSONArray("genre_ids");
                        for(int i=0; i < genres.length(); i++) {
                            int gId = genres.getInt(i);
                            GenreEntry ge = GenreModel.getGenre(gId);
                            if(ge != null) vid.genres.add(ge);
                        }

                        String poster = (String) entry.get("poster_path");
                        poster = poster.replace("/", "");

                        Bitmap mybitmap = HttpRequestHelper.getPicture("https://image.tmdb.org/t/p/w500/" + poster);

                        File myjpg = new File(context.getString(R.string.image_location), poster);

                        File directory = new File(context.getString(R.string.image_location) + "/");
                        if (!directory.exists()) directory.mkdir();
                        if (!myjpg.exists()) myjpg.createNewFile();
                        OutputStream outputstream = new FileOutputStream(myjpg);
                        mybitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputstream);
                        outputstream.close();

                        vid.jpg_url = context.getString(R.string.image_location) + poster;

                        return vid;
                    }
                }
            }
            catch (Exception ex) {
                    return null;
            }

            return null;
        }


        /**
         * Un algorithme très utile qui compare la proximité de deux chaines
         * @param s première chaine
         * @param t seconde chaine
         * @return un indice de proximité : plus le chiffre est faible, plus les chaines sont proches l'une de l'autre.
         */
        private int levenshteinDistance(String s, String t) {
            // for all i and j, d[i,j] will hold the Levenshtein distance between
            // the first i characters of s and the first j characters of t
            // note that d has (m+1)*(n+1) values
            int d[][] = new int[s.length()][t.length()];

            for(int i=0; i < s.length(); i++) {
                for(int j=0; j < t.length(); j++) {
                    d[i][j]=0;
                }
            }

            // source prefixes can be transformed into empty string by
            // dropping all characters
            for(int i=1 ; i < s.length(); i++) d[i][0] = i;

            // target prefixes can be reached from empty source prefix
            // by inserting every character
            for(int j=1 ; j < t.length(); j++) d[0][j] = j;

            for(int j=1 ; j < t.length(); j++) {
                for(int i=1 ; i < s.length(); i++) {
                    int substitutionCost = 0;
                    if(s.getBytes()[i] == t.getBytes()[j]) substitutionCost = 0;
                    else substitutionCost = 1;

                    int res1 = d[i-1][j] + 1;
                    int res2 = d[i][j-1] + 1;
                    int res3 = d[i-1][j-1] + substitutionCost;

                    int mini = res1;
                    if(res2 < mini) mini = res2;
                    if(res3 < mini) mini = res3;

                    d[i][j] = mini;
                }
            }

            return d[s.length()-1][t.length()-1];
        }


    }



}

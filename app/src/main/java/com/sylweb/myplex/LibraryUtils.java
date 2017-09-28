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
            ArrayList<String> files = getAllFiles();
            ArrayList<Integer> readIds = new ArrayList<>();
            for(String filename : files) {

                //Get video informations from TheMovieDB
                VideoEntry newVid = getFilmWithInfo(filename);
                if(newVid != null) {

                    //Complete with some local info
                    newVid.library_id = lib.id;
                    newVid.file_url = lib.url+"/"+filename;

                    //Insert movie in local DB
                    VideoModel.saveEntry(newVid);

                    readIds.add(newVid.tmdb_id);
                }
            }

            //Purge old entries
            String query = "";
            if (readIds.size() > 0) {
                query = "DELETE FROM video WHERE library_id =%d and tmdb_id not in(%s)";
                if (readIds.size() > 1) {
                    String ids = "";
                    for (int i = 0; i < readIds.size() - 1; i++) {
                        ids = ids + readIds.get(i) + ",";
                    }
                    ids = ids + readIds.get(readIds.size() - 1);
                    query = String.format(query, lib.id, ids);
                } else {
                    String idToDelete = "" + readIds.get(readIds.size() - 1);
                    query = String.format(query, lib.id, idToDelete);
                }
            } else {
                query = "DELETE FROM video WHERE id > 0";
            }
            DBManager.executeQuery(query);


            //Job finished so tell the observer(s)
            Intent intent = new Intent("LibUpdate");
            intent.putExtra("libId", lib.id);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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

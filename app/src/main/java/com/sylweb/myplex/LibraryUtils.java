package com.sylweb.myplex;

import android.content.Context;
import android.graphics.Bitmap;

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
            String query = "DELETE FROM video WHERE library_id =%d and tmdb_id not in(%s)";
            String ids = "";
            for(int i=0; i < readIds.size()-1;i++) {
                ids = ids+readIds.get(i)+",";
            }
            ids = ids+readIds.get(readIds.size()-1);
            query = String.format(query,lib.id,ids);
            DBManager.executeQuery(query);
        }

        private ArrayList<String> getAllFiles() {
            ArrayList<String> files = new ArrayList<>();
            File directory = new File(lib.url);
            File[] f = directory.listFiles();
            for (int i = 0; i < f.length; i++)
            {
                files.add(f[i].getName());
            }
            return files;
        }

        private VideoEntry getFilmWithInfo(String filename) {

            filename = filename.substring(0, filename.lastIndexOf("."));
            filename = filename.replace("[","");
            filename = filename.replace("]","");
            filename = filename.replace("{","");
            filename = filename.replace("}","");
            filename = filename.replace("."," ");
            filename = filename.replace("_"," ");
            filename = filename.replace(" ", "+");

            String url = "https://api.themoviedb.org/3/search/movie?api_key=c15ed3307384c1d73034f5fe889cd871&language=fr&query="+filename;

            try {
                JSONObject answer = HttpRequestHelper.executeGET(url);
                JSONArray results = null;
                if(answer != null) results = answer.getJSONArray("results");
                if(results != null && results.length() > 0) {
                    VideoEntry vid = new VideoEntry();
                    vid.tmdb_id = (Integer)((JSONObject)results.get(0)).get("id");
                    vid.name = (String) ((JSONObject)results.get(0)).get("title");
                    vid.name = vid.name.replace("'","''");
                    vid.year= ((String) ((JSONObject)results.get(0)).get("release_date")).substring(0,4);
                    vid.overview = (String) ((JSONObject)results.get(0)).get("overview");
                    vid.overview = vid.overview.replace("'","''");


                    String poster = (String) ((JSONObject)results.get(0)).get("poster_path");
                    poster = poster.replace("/","");

                    Bitmap mybitmap = HttpRequestHelper.getPicture("https://image.tmdb.org/t/p/w500/"+poster);

                    File myjpg = new File(context.getString(R.string.image_location),poster);
                    try{
                        File directory = new File(context.getString(R.string.image_location)+"/");
                        if(!directory.exists()) directory.mkdir();
                        if(!myjpg.exists()) myjpg.createNewFile();
                        OutputStream outputstream = new FileOutputStream(myjpg);
                        mybitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputstream);
                        outputstream.close();
                    }catch(Exception e){ e.printStackTrace(); }
                    vid.jpg_url = context.getString(R.string.image_location)+poster;

                    return vid;
                }
            }
            catch (Exception ex) {
                    return null;
            }

            return null;
        }
    }

}

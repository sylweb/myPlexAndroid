package com.sylweb.myplex;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
        UpdateThread thread = new UpdateThread(libraryId,context);
        thread.start();
    }

    public class UpdateThread extends Thread {

        private LibraryEntry lib;
        private Context context;

        public UpdateThread(Integer id, Context c) {
            context = c;
            LibraryModel mod = new LibraryModel();
            lib = mod.getFromId(id);
        }

        @Override
        public void run() {
            try {
                //First update genre database from the movie DB
                updateGenreDB();

                //Then find all files in the library directory
                ArrayList<String> files = getAllFiles();


                ArrayList<Integer> readIds = new ArrayList<>();
                ArrayList<String> unidentifiedFiles = new ArrayList<>();

                //For each found file...
                for (String filename : files) {

                    //If we already have this file in DB for this library then skip it
                    String query = "SELECT id FROM video WHERE library_id = %d AND file_url like'%s'";
                    String fileURL = lib.url + "/" + filename;
                    fileURL = fileURL.replace("'","''");
                    query = String.format(query, lib.id, fileURL);
                    DBManager db = new DBManager();
                    ArrayList results = db.executeQuery(query);
                    if (results != null && results.size() > 0) continue;

                    //If we don't have this video yet, then try to gather information about it from TheMovieDB
                    VideoEntry newVid = getFilmWithInfo(filename);
                    if (newVid != null) {

                        //Complete with some local info
                        newVid.library_id = lib.id;
                        newVid.file_url = fileURL;
                        newVid.file_url = newVid.file_url.replace("'","''");

                        //Insert movie in local DB
                        VideoModel mod = new VideoModel();
                        mod.saveEntry(newVid);

                        readIds.add(newVid.tmdb_id);
                    } else {
                        unidentifiedFiles.add(filename);
                    }
                }

                //Job finished so tell the observer(s)
                Intent intent = new Intent("LIBRARY_SYNC_FINISHED");
                intent.putExtra("LIBRARY_ID", lib.id);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                //We also built a list of all files that couldn't be identified so display it in google keep
                if (unidentifiedFiles.size() > 0) {
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Impossible d'identifier ces fichers");
                    //intent.setPackage("com.google.android.keep");
                    String unidentied = "";
                    for (String filename : unidentifiedFiles) {
                        unidentied = unidentied + filename + "\r\n";
                    }
                    intent.putExtra(Intent.EXTRA_TEXT, unidentied);

                    this.context.startActivity(Intent.createChooser(intent, "Afficher un texte"));
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        private void updateGenreDB() throws Exception{

            String url = "https://api.themoviedb.org/3/genre/movie/list?api_key=c15ed3307384c1d73034f5fe889cd871&language=fr-FR";

                HttpRequestHelper http = new HttpRequestHelper();
                JSONObject answer = http.executeGET(url);
                JSONArray results = null;
                if (answer != null) results = answer.getJSONArray("genres");
                if (results != null && results.length() > 0) {


                    for(int i=0; i < results.length(); i++) {

                        JSONObject entry = (JSONObject) results.get(i);
                        int id = (int) entry.get("id");
                        String name = (String) entry.get("name");

                        GenreEntry genre = new GenreEntry(id,name);
                        GenreModel mod = new GenreModel();
                        mod.save(genre);
                    }

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

            //TODO remove after debug
            files.add("3 Amis.avi");
            files.add("quatre garçons pleins d'avenir.avi");
            files.add("8 mm.avi");
            files.add("8.Mile.avi");
            files.add("13_FANTOMES.AVI");
            files.add("13.avi");
            files.add("13.Jeux.De.Mort.avi");
            files.add("20.Ans.d.Ecart.avi");
            files.add("21 grammes.avi");
            files.add("27 ROBES.avi");
            files.add("28.jours.Plus.tard.avi");
            files.add("28.Semaines.Plus.Tard.avi");
            files.add("40 ans toujours puceau.avi");
            files.add("Colt 45.avi");
            files.add("51.avi");
            files.add("60 secondes chrono.avi");
            files.add("99 Francs.avi");
            files.add("500.Days.Of.Summer.avi");
            files.add("600 KG d'Or Pur.avi");
            files.add("2012.mkv");
            files.add("5150.Rue.Des.Ormes.avi");
            files.add("10000.avi");
            files.add("A la ferme.avi");
            files.add("A la recherche du bonheur.avi");
            files.add("A Ton Image.avi");
            files.add("A.Bittersweet.Life.avi");
            files.add("A.Guide.To.Recognizing.Your.Saints.avi");
            files.add("A.La.Derive.avi");
            files.add("A.Perfect.Getaway.avi");
            files.add("Ace Ventura en Afrique.avi");
            files.add("Alice.in.Wonderland.avi");
            files.add("Amanda.Knox..avi");
            files.add("American History X.avi");
            files.add("American Psycho 2.avi");
            files.add("American Psycho.avi");
            files.add("American Sniper.avi");
            files.add("American_Beauty.avi");
            files.add("American.Gangster.avi");
            files.add("American.Pie.Band.Camp.avi");
            files.add("Angle d'attaque.avi");
            files.add("Animal Factory.avi");
            files.add("Ao.Le.Dernier.Neandertal.avi");
            files.add("Arbitrage..avi");
            files.add("Argo.avi");
            files.add("Arnaques, Crimes et Botaniques.avi");
            files.add("Arrête-moi si tu peux.AVI");
            files.add("Assaut sur le central 13.avi");
            files.add("Asterix Le Domaine Des Dieux.mkv");
            files.add("Asterix.Aux.Jeux.Olympiques.Film.avi");
            files.add("Au nom du chanvre.avi");
            files.add("Babel.avi");
            files.add("Babysitting.avi");
            files.add("Bad.Boys I.avi");
            files.add("Bad.Boys.II.avi");
            files.add("Bad.Cop.avi");
            files.add("Bad.Teacher.avi");
            files.add("Bandidas.avi");
            files.add("Banlieue.13.Ultimatum.avi");
            files.add("Baraka.avi");
            files.add("Basic.avi");
            files.add("battle los angeles.avi");
            files.add("Beyond.A.Reasonable.Doubt.avi");
            files.add("Bienvenu à Gattaca.mkv");
            files.add("Bienvenue.Chez.Les.Chtits.avi");
            files.add("Big.City.avi");
            files.add("Bigard.Fete.Ses.60.Ans.avi");
            files.add("Bigard.met.le.paquet.avi");
            files.add("Black.avi");
            files.add("Black.Swan.avi");
            files.add("Bliss.avi");
            files.add("Blitz.avi");
            files.add("Blood.And.Bone.avi");
            files.add("Blood.Stains.avi");
            files.add("Bloodworth.avi");
            files.add("Blow.avi");
            files.add("Borat.avi");
            files.add("Braquage.à.l'Anglaise.avi");
            files.add("Braquage.à.l'italienne.avi");
            files.add("Brassens.La.Mauvaise.Reputation.avi");
            files.add("Bright.Star.avi");
            files.add("Bronson.avi");
            files.add("Burn after reading.avi");
            files.add("Byzantium.avi");
            files.add("C.est Pas.Moi.Je.le.Jure.avi");
            files.add("C'est.Arrivé.Près.de.Chez.Vous.mkv");
            files.add("Camping.2.avi");
            files.add("Captifs.avi");
            files.add("Captivity.avi");
            files.add("Carton.Rouge.AVI");
            files.add("Casablanca.Driver.avi");
            files.add("Case.39.avi");
            files.add("Case.Depart.avi");
            files.add("Casino.Royale.avi");
            files.add("Cellular .avi");
            files.add("Charlie et la chocolaterie.avi");
            files.add("CHE 1 PARTIE L'ARGENTINE.avi");
            files.add("CHE 2 PARTIE.Guerrilla.CD1.avi");
            files.add("CHE 2 PARTIE.Guerrilla.CD2.avi");
            files.add("Chernobyl.Diaries.avi");
            files.add("Chez Gino.avi");
            files.add("Chouchou.avi");
            files.add("Cinquième.Element.avi");
            files.add("Coach.Carter.avi");
            files.add("Coco.avi");
            files.add("Collateral.avi");
            files.add("Colombiana 2011.avi");
            files.add("Colombiana.avi");
            files.add("Columbus.Circle.avi");
            files.add("Conan.The.Barbarian.avi");
            files.add("Confession.of.Murder.avi");
            files.add("Constantine.avi");
            files.add("Constantinople.avi");
            files.add("Contagion.avi");
            files.add("Contre Enquete.mkv");
            files.add("Contrebande.mkv");
            files.add("Cracks.avi");
            files.add("Crossfire.avi");
            files.add("Crows.Zero.avi");
            files.add("Cyprien.avi");
            files.add("Danny.The.Dog.avi");
            files.add("Date limite.mkv");
            files.add("Dead.Heads.avi");
            files.add("Death.Proof.mkv");
            files.add("Dédales.avi");
            files.add("Deppression et des pots.avi");
            files.add("Die hard 5.avi");
            files.add("Dikkenek.avi");
            files.add("dikkenek.mp4");
            files.add("Dirty.Dancing.avi");
            files.add("Dirty.Dancing.Havana.Nights.avi");
            files.add("Disjoncte by Evick.avi");
            files.add("Disturbia..avi");
            files.add("Divorces.avi");
            files.add("Django Unchained .mkv");
            files.add("Dobermann.avi");
            files.add("Dodgeball.avi");
            files.add("Dogma.avi");
            files.add("Dogville - Cd1.avi");
            files.add("Dogville - Cd2.avi");
            files.add("Domino.avi");
            files.add("Dragon.Rouge.avi");
            files.add("Dreed.avi");
            files.add("Drive.Angry.avi");
            files.add("Dumb and Dumber.avi");
            files.add("Dumb.and.Dumber.To.avi");
            files.add("Edge.Of.Darkness.avi");
            files.add("En Territoire Ennemi.avi");
            files.add("Enfermes.Dehors.avi");
            files.add("Entre.Les.Murs..avi");
            files.add("Entretien avec un vampire.avi");
            files.add("Equilibrium.avi");
            files.add("Esprits Rebelles.avi");
            files.add("Esther.PROPER.CD1.avi");
            files.add("Esther.PROPER.CD2.avi");
            files.add("Existenz.avi");
            files.add("Extracted..avi");
            files.add("F.B.I Fausses Blondes.avi");
            files.add("Fame.avi");
            files.add("Fanny.avi");
            files.add("Faster.avi");

            return files;
        }

        private VideoEntry getFilmWithInfo(String filename) throws Exception {

            String file_name = filename;

            if(filename.lastIndexOf(".") > 0) file_name = file_name.substring(0, filename.lastIndexOf("."));
            file_name = file_name.replace("[","");
            file_name = file_name.replace("]","");
            file_name = file_name.replace("{","");
            file_name = file_name.replace("}","");
            file_name = file_name.replace("."," ");
            file_name = file_name.replace("_"," ");
            file_name = file_name.replace("."," ");
            file_name = file_name.replace(","," ");
            file_name = file_name.replace(" ", "%20");
            file_name = file_name.replace("'","%27");
            file_name = file_name.trim();

            String url = "https://api.themoviedb.org/3/search/movie?api_key=c15ed3307384c1d73034f5fe889cd871&language=fr&query="+file_name;

                HttpRequestHelper http = new HttpRequestHelper();
                JSONObject answer = http.executeGET(url);
                JSONArray results = null;
                if(answer != null) results = answer.getJSONArray("results");
                if(results != null && results.length() > 0) {

                    file_name = file_name.replace("%20", " ");
                    file_name = filename.replace("%27","'");
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
                        vid.name = vid.name.replace("'","''");
                        vid.year = ((String) entry.get("release_date")).substring(0, 4);
                        vid.overview = (String) entry.get("overview");
                        vid.overview = vid.overview.replace("'", "''");

                        JSONArray genres = entry.getJSONArray("genre_ids");
                        for(int i=0; i < genres.length(); i++) {
                            int gId = genres.getInt(i);

                            GenreModel mod = new GenreModel();
                            GenreEntry ge = mod.getGenre(gId);
                            if(ge != null) vid.genres.add(ge);
                        }

                        String poster = (String) entry.get("poster_path");
                        poster = poster.replace("/", "");

                        Bitmap mybitmap = http.getPicture("https://image.tmdb.org/t/p/w500/" + poster);

                        File myjpg = new File(context.getString(R.string.image_location), poster);

                        File directory = new File(context.getString(R.string.image_location) + "/");
                        if (!directory.exists()) directory.mkdir();
                        if (!myjpg.exists()) myjpg.createNewFile();
                        OutputStream outputstream = new FileOutputStream(myjpg);
                        mybitmap = getResizedBitmap(mybitmap, 240,360);
                        mybitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputstream);
                        outputstream.close();

                        vid.jpg_url = context.getString(R.string.image_location) + poster;

                        return vid;
                    }
                }

            Log.d("Library utils ",file_name);
            return null;
        }

        public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);

            // "RECREATE" THE NEW BITMAP
            Bitmap resizedBitmap = Bitmap.createBitmap(
                    bm, 0, 0, width, height, matrix, false);
            return resizedBitmap;
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

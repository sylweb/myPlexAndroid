package com.sylweb.myplex;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by sylvain on 10/08/2017.
 */

public class DBManager {

    private static String DB_PATH = "/data/data/com.sylweb.myplex/databases/";
    private static String DB_NAME = "local_video_db.sqlite";
    private static SQLiteDatabase myDB;
    public static boolean dbOpened;

    public static boolean initDB(Context context)
    {
        boolean test =checkDB(context);
        openDB();
        return test;
    }

    /** This method open database for operations **/
    private static void openDB() {
        String mPath = DB_PATH + DB_NAME;
        myDB = SQLiteDatabase.openDatabase(mPath, null,
                SQLiteDatabase.OPEN_READWRITE);
        dbOpened = true;
    }

    /** This method close database connection and released occupied memory **/
    public static void closeDB() {
        if (myDB != null)
            myDB.close();
        SQLiteDatabase.releaseMemory();
        dbOpened = false;
    }

    public static ArrayList executeQuery(String query, String... args) {

        ArrayList entries = new ArrayList();
        Cursor results = myDB.rawQuery(query, args);
        while(results.moveToNext()) {
            HashMap entry = new HashMap();
            for(String columnName:results.getColumnNames()) {
                entry.put(columnName, results.getString(results.getColumnIndex(columnName)));
            }
            entries.add(entry);
        }
        results.close();
        return entries;
    }

    public static String executeInsert(String tableName, ContentValues values) {
        openDB();
        long insertedId = myDB.insert(tableName, null, values);
        closeDB();
        return String.format("%ld", insertedId);
    }

    private static boolean checkDB(Context context) {

        boolean createOK = createDBIfNeeded();
        if(!createOK) return false;

        myDB = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
        if(myDB == null) return false;
        else {
            closeDB();
        }

        //Compare db_version in String.xml to
        //Apply updates if necessary or copy DB from bundle if it's a new install

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int currentDBVersion = prefs.getInt("DB_VERSION", 0);
        int bundleDBVersion = context.getResources().getInteger(R.integer.db_version);

        if(currentDBVersion > 0) {

            //Update database
            int updatesToApply = bundleDBVersion - currentDBVersion;
            for(int i=0; i < updatesToApply; i++) {
                int patchNumber = currentDBVersion + i;
                updateDB(patchNumber, context);
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("DB_VERSION",bundleDBVersion);
            editor.commit();
            return true;
        }
        else {
            try {
                boolean result = copyDB(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("DB_VERSION",bundleDBVersion);
                editor.commit();
                return result;
            } catch (IOException mIOException) {
                mIOException.printStackTrace();
                return false;
            }

        }
    }

    private static void updateDB(int patchVersion, Context context) {
        switch(patchVersion) {
            case 1 : {
                dbPatch_V2();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);;
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("FORCE_FULL_UPDATE", true);
                editor.commit();
                break;
            }

            case 2 : {
                dbPatch_V3();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);;
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("FORCE_FULL_UPDATE", true);
                editor.commit();
                break;
            }
        }
    }

    private static void dbPatch_V2() {

    }

    private static void dbPatch_V3() {

    }

    private static boolean copyDB(Context context) throws IOException {
        try {

            InputStream mInputStream = context.getAssets().open(DB_NAME);
            String outFileName = DB_PATH + DB_NAME;
            OutputStream mOutputStream = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = mInputStream.read(buffer)) > 0) {
                mOutputStream.write(buffer, 0, length);
            }
            mOutputStream.flush();
            mOutputStream.close();
            mInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean createDBIfNeeded() {
        try {
            final String mPath = DB_PATH + DB_NAME;
            final File file = new File(mPath);
            if (file.exists()) return true;
            else return createDBFile();
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createDBFile() {
        try {
            File file = new File(DB_PATH+File.separator);
            if(!file.exists()) file.mkdirs();
            file = new File(DB_PATH+DB_NAME);
            file.createNewFile();
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }



}

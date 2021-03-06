package com.sylweb.myplex;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sylvain on 23/08/2017.
 */

public class HttpRequestHelper {

    private int timeout = 10000;

    public JSONObject executeGET(String request) {
        URL url = null;
        JSONObject results = null;
        try {
            url = new URL(request);
        } catch (Exception ex) {

        }

        InputStream stream = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            // Timeout for reading InputStream arbitrarily set to 60s
            connection.setReadTimeout(timeout);

            // Timeout for connection.connect() arbitrarily set to 60s
            connection.setConnectTimeout(timeout);

            //GET, PUT or POST
            connection.setRequestMethod("GET");

            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);

            // Open communications link (network traffic occurs here).
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            if (stream != null) {
                // Converts Stream to String
                String result = readStream(stream);
                return new JSONObject(result);
            }
        } catch (Exception ex) {
            Log.e("TAG", ex.getLocalizedMessage());
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ex) {

                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return results;
    }

    public Bitmap getPicture(String request) {
        URL url = null;
        try {
            url = new URL(request);
        } catch (Exception ex) {

        }

        InputStream stream = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            // Timeout for reading InputStream arbitrarily set to 60s
            connection.setReadTimeout(timeout);

            // Timeout for connection.connect() arbitrarily set to 60s
            connection.setConnectTimeout(timeout);

            //GET, PUT or POST
            connection.setRequestMethod("GET");

            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);

            // Open communications link (network traffic occurs here).
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            if (stream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                return bitmap;
            }
        } catch (Exception ex) {
            Log.e("TAG", ex.getLocalizedMessage());
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ex) {

                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;

    }

    private String readStream(InputStream stream) throws IOException {

        Reader reader = new InputStreamReader(stream, "UTF-8");

        char[] rawBuffer = new char[10];
        int readSize;
        StringBuffer buffer = new StringBuffer();
        while (((readSize = reader.read(rawBuffer)) != -1)) {
            buffer.append(rawBuffer, 0, readSize);
        }
        return buffer.toString();
    }

}

package com.sylweb.myplex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sylvain on 26/09/2017.
 */

public class LibraryContentAdapter extends BaseAdapter {

    public ArrayList<VideoEntry> data;

    private LayoutInflater inflater = null;

    public LibraryContentAdapter(Context context, ArrayList<VideoEntry> data) {
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View vi = view;

        if (vi == null) vi = inflater.inflate(R.layout.video_item_layout, null);
        ((TextView)(vi.findViewById(R.id.filmName))).setText(data.get(i).name);

        try {
            Bitmap myBitmap = BitmapFactory.decodeFile(data.get(i).jpg_url);
            ((ImageView)(vi.findViewById(R.id.affiche))).setImageBitmap(myBitmap);
        }
        catch (Exception ex) {

            Log.e("ERROR LOADING POSTER", ex.getClass().getName());
        }



        return vi;
    }
}

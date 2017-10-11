package com.sylweb.myplex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.internal.ParcelableSparseArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by sylvain on 10/10/2017.
 */

public class SearchResultAdapter extends BaseAdapter {

    public ArrayList<VideoEntry> data;
    private LayoutInflater inflater;

    public SearchResultAdapter(Context context, ArrayList<VideoEntry> data) {
        this.data = data;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.data.size();
    }

    @Override
    public Object getItem(int i) {
        return this.data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;

        if (vi == null) vi = inflater.inflate(R.layout.film_search_item_layout, null);
        ((TextView)vi.findViewById(R.id.titleField)).setText(data.get(i).name);
        ((TextView)vi.findViewById(R.id.yearField)).setText(data.get(i).year);
        try {
            ImageView poster = vi.findViewById(R.id.posterField);
            poster.setImageBitmap(data.get(i).tempImage);
        }
        catch (Exception ex) {
            Log.e("ERROR", ex.getClass().getName());
        }


        return vi;
    }
}

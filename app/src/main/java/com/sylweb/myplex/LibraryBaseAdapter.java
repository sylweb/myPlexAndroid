package com.sylweb.myplex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sylvain on 26/09/2017.
 */

public class LibraryBaseAdapter extends BaseAdapter {

    private ArrayList<VideoEntry> data;

    private static LayoutInflater inflater = null;

    public LibraryBaseAdapter(Context context, ArrayList<VideoEntry> data) {
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
        if (vi == null) vi = inflater.inflate(R.layout.video_entry_layout, null);
        ((TextView)(vi.findViewById(R.id.filmName))).setText(data.get(i).name);
        return vi;
    }
}

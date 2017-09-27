package com.sylweb.myplex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sylvain on 27/09/2017.
 */

public class LibraryListAdapter extends BaseAdapter {

    private ArrayList<LibraryEntry> data;
    private static LayoutInflater inflater = null;

    public LibraryListAdapter(Context context, ArrayList<LibraryEntry> data) {
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
        return data.get(i).id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;
        if (vi == null) vi = inflater.inflate(R.layout.library_item_layout, null);
        ((TextView)(vi.findViewById(R.id.itemName))).setText(data.get(i).name);
        return vi;
    }
}
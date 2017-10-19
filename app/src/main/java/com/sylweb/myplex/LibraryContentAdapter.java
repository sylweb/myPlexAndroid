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

    private int selectedPosition;

    private LayoutInflater inflater = null;

    public LibraryContentAdapter(Context context, ArrayList<VideoEntry> data) {
        this.data = data;
        this.selectedPosition = 0;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setSelected(int position) {
        this.selectedPosition = position;
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
            if(!data.get(i).small_jpg_url.equals("")) {
                Bitmap myBitmap = BitmapFactory.decodeFile(data.get(i).small_jpg_url);
                ((ImageView) (vi.findViewById(R.id.affiche))).setImageBitmap(myBitmap);
            }
            else {
                ((ImageView) (vi.findViewById(R.id.affiche))).setImageResource(R.mipmap.icon_dvd);
            }

            if(selectedPosition == i) {
                vi.setBackgroundColor(vi.getResources().getColor(R.color.myLightGray));
            }else {
                vi.setBackgroundColor(vi.getResources().getColor(R.color.myLightBlack));
            }

            ImageView corner = vi.findViewById(R.id.cornerImage);
            corner.setImageResource(R.mipmap.corner);
            if(data.get(i).viewed == 0) {
                corner.setVisibility(View.VISIBLE);
            }else corner.setVisibility(View.INVISIBLE);
        }
        catch (Exception ex) {

            Log.e("ERROR LOADING POSTER", ex.getClass().getName());
        }



        return vi;
    }
}

package com.sylweb.myplex;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by sylvain on 29/09/2017.
 */

public class RemoveLibraryDialog extends Dialog implements View.OnClickListener {

    private Button cancelButton;
    private Button deleteButton;
    private Spinner libSelector;

    private ArrayList<LibraryEntry> libraries;

    public RemoveLibraryDialog(Activity a) {
        super(a);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.remove_library_dialog);

        //Prevent screen from becoming darker when displaying the dialog
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.dimAmount = 0.0f;

        //Get UI and attach listeners
        this.cancelButton = findViewById(R.id.libCancelButton);
        this.cancelButton.setOnClickListener(this);
        this.deleteButton = findViewById(R.id.libDeleteButton);
        this.deleteButton.setOnClickListener(this);
        this.libSelector = findViewById(R.id.libSelector);

        //Load library list
        this.libraries = LibraryModel.getAll();

        //Populate the spinner
        this.libSelector.setAdapter(new LibraryListAdapter(this.getContext(), libraries));
        this.libSelector.setSelection(0);
    }

    @Override
    public void onClick(View view) {
        if(view.equals(this.cancelButton)) {
            this.dismiss();
        }
        else if(view.equals(deleteButton)) {
            LibraryModel.removeLibrary(((LibraryEntry)this.libSelector.getSelectedItem()).id);
            Intent intent = new Intent("LIBRARY_LIST_MODIFIED");
            LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(intent);
            this.dismiss();
        }
    }
}

package com.sylweb.myplex;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

public class LibraryCreationActivity extends AppCompatActivity implements View.OnClickListener {

    private Button okButton;
    private Button cancelButton;
    private Button directoryButton;
    private EditText libraryNameField;
    private EditText libraryDirectoryField;

    private final int REQUEST_DIRECTORY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_creation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Créer une bibliothèque");

        this.okButton = (Button) findViewById(R.id.okButton);
        this.okButton.setOnClickListener(this);
        this.cancelButton = (Button) findViewById(R.id.cancelButton);
        this.cancelButton.setOnClickListener(this);
        this.directoryButton = (Button) findViewById(R.id.directoryChooserButton);
        this.directoryButton.setOnClickListener(this);
        this.libraryNameField = (EditText) findViewById(R.id.libraryName);
        this.libraryDirectoryField = (EditText) findViewById(R.id.libraryDirectory);

        Drawable drawable = this.libraryNameField.getBackground(); // get current EditText drawable
        drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP); // change the drawable color

        if(Build.VERSION.SDK_INT > 16) {
            this.libraryNameField.setBackground(drawable); // set the new drawable to EditText
        }else{
            this.libraryNameField.setBackgroundDrawable(drawable); // use setBackgroundDrawable because setBackground required API 16
        }

        drawable = this.libraryDirectoryField.getBackground(); // get current EditText drawable
        drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP); // change the drawable color

        if(Build.VERSION.SDK_INT > 16) {
            this.libraryDirectoryField.setBackground(drawable); // set the new drawable to EditText
        }else{
            this.libraryDirectoryField.setBackgroundDrawable(drawable); // use setBackgroundDrawable because setBackground required API 16
        }

    }

    @Override
    public void onClick(View view) {
        if(view.equals(this.directoryButton)) {

            //Select library directory with an external library
            final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);

            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                    .newDirectoryName("DirChooserSample")
                    .initialDirectory("/")
                    .allowReadOnlyDirectory(true)
                    .allowNewDirectoryNameModification(true)
                    .build();

            chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);

            startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
        }
        else if(view.equals(this.cancelButton)) {
            returnToPreviousActivity();
        }
        else if(view.equals(this.okButton)) {
            //Check that library name is ok
            LibraryModel mod = new LibraryModel();
            if(mod.isNameAvailable(this.libraryNameField.getText().toString()) && !this.libraryNameField.getText().toString().equals("")) {
                LibraryEntry newLib = new LibraryEntry(this.libraryNameField.getText().toString(), this.libraryDirectoryField.getText().toString());
                mod.saveEntry(newLib);
                returnToPreviousActivity();
            }
            else {
                Toast.makeText(this, "Cette bibliothèque existe déjà ou le nom est incorrect.", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIRECTORY) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                this.libraryDirectoryField.setText(data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
            } else {
                this.libraryDirectoryField.setText("");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        returnToPreviousActivity();
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    private void returnToPreviousActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}

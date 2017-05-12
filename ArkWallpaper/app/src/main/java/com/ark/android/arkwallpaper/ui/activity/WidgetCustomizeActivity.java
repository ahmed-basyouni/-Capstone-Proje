package com.ark.android.arkwallpaper.ui.activity;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.ark.android.arkwallpaper.Constants;
import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.arkwallpaper.utils.WidgetUtils;
import com.ark.android.arkwallpaper.widget.WallpaperWidget;
import com.ark.android.arkwallpaper.widget.WidgetService;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class WidgetCustomizeActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener , LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {

    private static final int LOADER_ID = 21;
    @BindView(R.id.widgetOptionsGroup)
    RadioGroup customizeOptionsGroup;
    @BindView(R.id.save)
    Button saveBtn;
    @BindView(R.id.albumSpinner)
    Spinner albumSpinner;
    @BindView(R.id.switchToAlbum)
    RadioButton switchToAlbum;

    private Constants.CHANGE_MODE change_mode = Constants.CHANGE_MODE.NEXT_WALLPAPER;
    private String albumName;
    private String prefName;
    private int mAppWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_customize_activty);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ButterKnife.bind(this);
        setResult(RESULT_CANCELED);
        showAppWidget();
        customizeOptionsGroup.setOnCheckedChangeListener(this);
        saveBtn.setOnClickListener(this);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void showAppWidget() {

        mAppWidgetId = INVALID_APPWIDGET_ID;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);
            prefName = "Pref" + mAppWidgetId;
        }
        if (mAppWidgetId == INVALID_APPWIDGET_ID) {
            finish();
        }

    }

    private void setupAlbumSpinner(List<String> albums) {

        String albumName = WidgetUtils.getSelectedAlbum(prefName);

        if(albumName != null){
            int index = albums.indexOf(albumName);
            if(index != -1)
                albumSpinner.setSelection(index);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, albums);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        albumSpinner.setAdapter(adapter);

        albumSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.nextWallpaper:
                change_mode = Constants.CHANGE_MODE.NEXT_WALLPAPER;
                break;
            case R.id.nextAlbum:
                change_mode = Constants.CHANGE_MODE.NEXT_ALBUM;
                break;
            case R.id.switchToAlbum:
                change_mode = Constants.CHANGE_MODE.SELECT_ALBUM;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        WidgetUtils.setChangeMode(change_mode, prefName);
        if(albumSpinner.getVisibility() == View.VISIBLE){
            if(albumName == null)
                albumName = albumSpinner.getAdapter().getItem(0).toString();
            WidgetUtils.setSelectedAlbum(albumName, prefName);
        }
        Intent startService = new Intent(WidgetCustomizeActivity.this,
                WidgetService.class);
        startService.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, startService);
        startService(startService);

        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, GallaryDataBaseContract.AlbumsTable.CONTENT_URI, new String[]{GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME}
                , null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.getCount() > 0){
            getDataFromCursor(data);
        }else{
            albumSpinner.setVisibility(View.GONE);
            switchToAlbum.setVisibility(View.GONE);
        }
    }

    private void getDataFromCursor(final Cursor data) {
        WallpaperApp.getWallpaperApp().runInBackGround(new Runnable() {
            @Override
            public void run() {
                data.moveToFirst();
                final List<String> albums = new ArrayList<String>();
                while (!data.isAfterLast()){
                    albums.add(data.getString(data.getColumnIndex(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME)));
                    data.moveToNext();
                }

                WallpaperApp.getWallpaperApp().runOnUI(new Runnable() {
                    @Override
                    public void run() {
                        setupAlbumSpinner(albums);
                    }
                });
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    int check = 0;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (++check > 1) {
            albumName = (String) albumSpinner.getAdapter().getItem(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

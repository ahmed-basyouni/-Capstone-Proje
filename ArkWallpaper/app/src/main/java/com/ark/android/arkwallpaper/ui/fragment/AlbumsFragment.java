package com.ark.android.arkwallpaper.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.presenter.contract.AlbumFragmentContract;
import com.ark.android.arkwallpaper.presenter.contract.HomeContract;
import com.ark.android.arkwallpaper.presenter.presenterImp.AlbumsPresenter;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public class AlbumsFragment extends Fragment implements
        HomeContract.OnHomePagerChange, AlbumFragmentContract.IAlbumsView,LoaderManager.LoaderCallbacks<Cursor>{

    @BindView(R.id.albumsList) RecyclerView albumList;
    @BindView(R.id.addAlbum)
    FloatingActionButton addAlbum;
    private AlbumsPresenter albumsPresenter;
    int count = 0;
    private static final int LOADER_ID = 214;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.albums_fragment, container, false);
        ButterKnife.bind(this,rootView);

        albumsPresenter = new AlbumsPresenter(this);

        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        addAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showAddAlbumDialog();
            }
        });

        return rootView;
    }

    private void showAddAlbumDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_album_dialog);

        final EditText albumNameField = (EditText) dialog.findViewById(R.id.albumNameField);
        Button okButton = (Button) dialog.findViewById(R.id.addAlbum);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!albumNameField.getText().toString().isEmpty()){
                    albumsPresenter.addAlbum(albumNameField.getText().toString(), GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_GALLERY);
                    dialog.dismiss();
                }else{
                    Toast.makeText(getActivity(), getString(R.string.no_name_provided), Toast.LENGTH_SHORT).show();
                }
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        dialog.show();
    }

    @Override
    public void onFragmentSelected() {

    }

    @Override
    public RecyclerView getAlbumList() {
        return albumList;
    }

    @Override
    public Activity getActivityContext() {
        return getActivity();
    }

    @Override
    public void showSnackWithMsg(String msg) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), GallaryDataBaseContract.AlbumsTable.CONTENT_URI, new String[]{BaseColumns._ID, GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI
                , GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_ENABLED, GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TYPE, GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT}, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            albumList.setVisibility(View.VISIBLE);
            albumsPresenter.onAlbumsLoaded(data);
        }else{
            albumList.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

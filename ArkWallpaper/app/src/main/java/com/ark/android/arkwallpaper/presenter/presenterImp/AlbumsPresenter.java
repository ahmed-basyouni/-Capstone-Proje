package com.ark.android.arkwallpaper.presenter.presenterImp;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.LinearLayoutManager;

import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.arkwallpaper.data.manager.AlbumsManager;
import com.ark.android.arkwallpaper.data.model.AlbumObject;
import com.ark.android.arkwallpaper.presenter.contract.AlbumFragmentContract;
import com.ark.android.arkwallpaper.ui.adapter.AlbumsAdapter;

import java.util.List;

/**
 * Created by ahmed-basyouni on 4/22/17.
 */

public class AlbumsPresenter implements AlbumFragmentContract.IAlbumsPresenter {

    private final AlbumFragmentContract.IAlbumsView iAlbumsView;
    private final AlbumsManager iAlbumsModel;
    private List<AlbumObject> albums;

    public AlbumsPresenter(AlbumFragmentContract.IAlbumsView iAlbumsView) {
        this.iAlbumsView = iAlbumsView;
        this.iAlbumsModel = new AlbumsManager();
    }

    @Override
    public void onAlbumsLoaded(final Cursor data) {
        if (data != null && data.getCount() > 0) {
            WallpaperApp.getWallpaperApp().runInBackGround(new Runnable() {
                @Override
                public void run() {
                    albums = AlbumObject.getAlbumsFromCursor(data);
                    WallpaperApp.getWallpaperApp().runOnUI(new Runnable() {
                        @Override
                        public void run() {
                            if (!albums.isEmpty())
                                setupAdapter();
                            else
                                onAlbumsEmpty();
                        }
                    });

                }
            });
        }
    }

    private void setupAdapter() {
        AlbumsAdapter albumsAdapter = new AlbumsAdapter(albums, iAlbumsView.getActivityContext(), this);
        iAlbumsView.getAlbumList().setLayoutManager(new LinearLayoutManager(iAlbumsView.getActivityContext()));
        iAlbumsView.getAlbumList().setAdapter(albumsAdapter);
    }

    @Override
    public void onAlbumsEmpty() {

    }

    @Override
    public void onAlbumExist() {

    }

    @Override
    public void addAlbum(String albumName, int type) {
        if (albums != null && !albums.isEmpty())
            for (AlbumObject albumObject : albums) {
                if (albumObject.getAlbumName().equalsIgnoreCase(albumName)) {
                    onAlbumExist();
                    return;
                }
            }
        this.iAlbumsModel.addAlbum(albumName, type);
    }

    @Override
    public void enableAlbum(boolean enabled, String albumName) {
        iAlbumsModel.enableAlbum(enabled, albumName);
    }

    @Override
    public void editAlbumName(String oldName, String newName) {
        iAlbumsModel.editAlbumName(oldName, newName);
    }

    @Override
    public void deleteAlbum(String albumName) {
        iAlbumsModel.deleteAlbum(albumName);
    }

    @Override
    public void handleChooserResult(int requestCode, int resultCode, Intent result) {

    }
}

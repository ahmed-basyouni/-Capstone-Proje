package com.ark.android.arkwallpaper.presenter.presenterImp;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.arkwallpaper.data.manager.AlbumsManager;
import com.ark.android.arkwallpaper.data.model.AlbumObject;
import com.ark.android.arkwallpaper.presenter.contract.AlbumFragmentContract;
import com.ark.android.arkwallpaper.ui.adapter.AlbumsAdapter;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;

import java.util.Arrays;
import java.util.List;

import rx.functions.Action1;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public class AlbumsPresenter implements AlbumFragmentContract.IAlbumsPresenter {

    private final AlbumFragmentContract.IAlbumsView iAlbumsView;
    private final AlbumsManager iAlbumsModel;
    private List<AlbumObject> albums;
    private boolean shouldAdd500PxObserver;
    private boolean shouldAddTumblrObserver;

    public AlbumsPresenter(AlbumFragmentContract.IAlbumsView iAlbumsView) {
        this.iAlbumsView = iAlbumsView;
        this.iAlbumsModel = new AlbumsManager();
    }

    @Override
    public void onAlbumsLoaded(final Cursor data) {
        shouldAdd500PxObserver = false;
        shouldAddTumblrObserver = false;
        if (data != null && data.getCount() > 0) {
            WallpaperApp.getWallpaperApp().runInBackGround(new Runnable() {
                @Override
                public void run() {
                    albums = AlbumObject.getAlbumsFromCursor(data);
                    if(WallPaperUtils.getCurrentAlbum() == null){
                        WallPaperUtils.setCurrentAlbum(albums.get(0).getAlbumName());
                        if(WallPaperUtils.isLiveWallpaperActive())
                            WallPaperUtils.changeWallpaperBroadCast(null);
                    }
                    shouldAddObservers();
                    WallpaperApp.getWallpaperApp().runOnUI(new Runnable() {
                        @Override
                        public void run() {
                            if (!albums.isEmpty()) {
                                setupAdapter();
                                iAlbumsView.checkFloatingAlbumState();
                                iAlbumsView.registerObservers(shouldAdd500PxObserver,shouldAddTumblrObserver);
                            }else
                                onAlbumsEmpty();
                        }
                    });

                }
            });
        }
    }

    private void shouldAddObservers() {
        if(albums != null && !albums.isEmpty()){
            for(AlbumObject albumObject : albums){
                if(albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_PX){
                    shouldAdd500PxObserver = true;
                }else if(albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_TUMBLR){
                    shouldAddTumblrObserver = true;
                }
            }
        }
    }

    private void setupAdapter() {
        AlbumsAdapter albumsAdapter = new AlbumsAdapter(albums, iAlbumsView.getActivityContext(), this);
        iAlbumsView.getAlbumList().setLayoutManager(new LinearLayoutManager(iAlbumsView.getActivityContext()));
        iAlbumsView.getAlbumList().setAdapter(albumsAdapter);
    }

    @Override
    public void onAlbumsEmpty() {
        iAlbumsView.onEmptyAlbums();
    }

    @Override
    public void onAlbumExist() {

    }

    @Override
    public void addAlbum(String albumName, int type, String fivePxCategoryName, String tumblrBlogName) {
        if (albums != null && !albums.isEmpty())
            for (AlbumObject albumObject : albums) {
                if (albumObject.getAlbumName().equalsIgnoreCase(albumName)) {
                    onAlbumExist();
                    return;
                }
            }
        this.iAlbumsModel.addAlbum(albumName, type, fivePxCategoryName, tumblrBlogName);
    }

    public List<AlbumObject> getAlbums() {
        return albums;
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

    @Override
    public AlbumObject get500PxAlbum() {
        if(albums != null && !albums.isEmpty()){
            for(AlbumObject albumObject : albums){
                if(albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_PX){
                    return albumObject;
                }
            }
        }
        return null;
    }

    @Override
    public AlbumObject getTumblrAlbum() {
        if(albums != null && !albums.isEmpty()){
            for(AlbumObject albumObject : albums){
                if(albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_TUMBLR){
                    return albumObject;
                }
            }
        }
        return null;
    }

    @Override
    public void showEditTumblrDialog(String tumblrBlogName, Action1<String> action1) {
        iAlbumsView.showAddAlbumDialog(true, tumblrBlogName, action1);
    }

    @Override
    public void clearAlbums() {
        if(albums != null)
            albums.clear();
    }

    @Override
    public void showAdd500PxDialog(String catName, Action1<String> action1) {
        List<String> categories = Arrays.asList(iAlbumsView.getActivityContext().getResources().getStringArray(R.array.fivePx_Cat));

        for(int x = 0; x < categories.size(); x++){
            if(categories.get(x).equals(catName)){
                iAlbumsView.showAdd500PxDialog(x, action1);
                break;
            }
        }
    }
}

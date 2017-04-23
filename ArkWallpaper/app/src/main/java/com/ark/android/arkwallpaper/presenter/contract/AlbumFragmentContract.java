package com.ark.android.arkwallpaper.presenter.contract;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

import com.ark.android.arkwallpaper.data.model.AlbumObject;

import java.util.List;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public interface AlbumFragmentContract {

    interface IAlbumsView{
        RecyclerView getAlbumList();
        Activity getActivityContext();
        void showSnackWithMsg(String msg);
    }

    interface IAlbumsPresenter{
        void onAlbumsLoaded(Cursor cursor);
        void onAlbumsEmpty();
        void onAlbumExist();
        void addAlbum(String albumName, int type);
        void enableAlbum(boolean enabled, String albumName);
        void editAlbumName(String oldName, String newName);
        void deleteAlbum(String albumName);
        void handleChooserResult(int requestCode, int resultCode, Intent result);
    }

    interface IAlbumsModel{
        void editAlbumName(String oldName, String newName);
        void deleteAlbum(String albumName);
        void addAlbum(String albumName, int type);
        void enableAlbum(boolean enabled, String albumName);
    }
}

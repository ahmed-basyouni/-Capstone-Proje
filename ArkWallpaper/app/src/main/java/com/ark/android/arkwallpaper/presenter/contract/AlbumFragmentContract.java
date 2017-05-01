package com.ark.android.arkwallpaper.presenter.contract;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ark.android.arkwallpaper.data.model.AlbumObject;

import java.util.List;

import rx.functions.Action1;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public interface AlbumFragmentContract {

    interface IAlbumsView{
        RecyclerView getAlbumList();
        Activity getActivityContext();
        void showSnackWithMsg(String msg);
        void checkFloatingAlbumState();
        void onEmptyAlbums();
        void registerObservers(boolean shouldAdd500PxObserver, boolean shouldAddTumblrObserver);
        void showAddAlbumDialog(final boolean isTumblr, String editFieldText, Action1<String> action1);
        void showAdd500PxDialog(int index, Action1<String> action1);
    }

    interface IAlbumsPresenter{
        void onAlbumsLoaded(Cursor cursor);
        void onAlbumsEmpty();
        void onAlbumExist();
        void addAlbum(String albumName, int type, String fivePxCategoryName, String tumblrBlogName);
        void enableAlbum(boolean enabled, String albumName);
        void editAlbumName(String oldName, String newName);
        void deleteAlbum(String albumName);
        void handleChooserResult(int requestCode, int resultCode, Intent result);
        AlbumObject getTumblrAlbum();
        AlbumObject get500PxAlbum();
        void showEditTumblrDialog(String tumblrBlogName, Action1<String> action1);
        void showAdd500PxDialog(String catName, Action1<String> action1);

        void clearAlbums();
    }

    interface IAlbumsModel{
        void editAlbumName(String oldName, String newName);
        void deleteAlbum(String albumName);
        void addAlbum(String albumName, int type, String fivePxCategoryName, String tumblrBlogName);
        void enableAlbum(boolean enabled, String albumName);
    }
}

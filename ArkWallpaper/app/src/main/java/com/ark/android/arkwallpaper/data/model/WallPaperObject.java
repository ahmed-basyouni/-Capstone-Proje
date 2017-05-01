package com.ark.android.arkwallpaper.data.model;

import java.io.Serializable;

/**
 * Created by ahmed-basyouni on 4/30/17.
 */

public class WallPaperObject implements Serializable {

    private String albumName;
    private String imageUri;
    private int objectId;

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }
}

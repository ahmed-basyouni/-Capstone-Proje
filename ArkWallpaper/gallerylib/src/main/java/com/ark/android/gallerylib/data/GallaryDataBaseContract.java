package com.ark.android.gallerylib.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 *
 * Created by ahmed-basyouni on 4/22/17.
 */

public class GallaryDataBaseContract {

    public static final String GALLERY_AUTHORITY = "com.ark.android.gallerylib.galleryAuthority";

    private static final String SCHEME = "content://";

    public static final class GalleryTable implements BaseColumns {
        /**
         * Column name of the chosen photo's URI.
         * <p>Type: TEXT (URI)
         */
        public static final String COLUMN_NAME_URI = "uri";

        public static final String COLUMN_ALBUM_NAME = "albumName";

        public static final String COLUMN_PARENT_URI = "parent_uri";

        /**
         * The MIME type of {@link #CONTENT_URI} providing a single chosen photo.
         */
        static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ark.android.gallerylib.gallery_table";
        /**
         * The default sort order for this table
         */
        static final String DEFAULT_SORT_ORDER = BaseColumns._ID;
        /**
         * The table name offered by this provider.
         */
        static final String TABLE_NAME = "gallery_table";

        private GalleryTable() {
        }

        /**
         * The content:// style URL for this table.
         */
        public static final Uri CONTENT_URI = Uri.parse(GallaryDataBaseContract.SCHEME + GallaryDataBaseContract.GALLERY_AUTHORITY
                + "/" + GalleryTable.TABLE_NAME);
    }

    public static final class AlbumsTable implements BaseColumns {

        public static final int ALBUM_TYPE_GALLERY = 0x01;

        public static final int ALBUM_TYPE_PX = 0x02;

        public static final int ALBUM_TYPE_TUMBLR = 0x03;
        /**
         * Column name of the photo's URI.
         * <p>Type: TEXT (URI)
         */
        public static final String COLUMN_ALBUM_IMAGE_URI = "uri";
        /**
         * Column name for when this photo was taken
         * <p>Type: LONG (in milliseconds)
         */
        public static final String COLUMN_ALBUM_NAME = "albumName";

        public static final String COLUMN_ALBUM_ENABLED = "enabled";

        public static final String COLUMN_ALBUM_COUNT = "image_count";

        public static final String COLUMN_ALBUM_TYPE = "album_type";
        /**
         * The MIME type of {@link #CONTENT_URI} providing metadata.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.android.apps.muzei.gallery.metadata_cache";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " DESC";
        /**
         * The table name offered by this provider.
         */
        public static final String TABLE_NAME = "albums";

        /**
         * This class cannot be instantiated
         */
        private AlbumsTable() {
        }

        /**
         * The content:// style URL for this table.
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + GALLERY_AUTHORITY
                + "/" + AlbumsTable.TABLE_NAME);
    }

}

package com.ark.android.gallerylib.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * Created by ahmed-basyouni on 4/21/17.
 */

public class GallaryUtils {

    public static List<Uri> getImagesFromTreeUri(Context context, final Uri treeUri, final int maxImages) {
        List<Uri> images = new ArrayList<>();
        Queue<String> directories = new LinkedList<>();
        directories.add(DocumentsContract.getTreeDocumentId(treeUri));
        while (images.size() < maxImages && !directories.isEmpty()) {
            String parentDocumentId = directories.poll();
            final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri,
                    parentDocumentId);
            Cursor children;
            try {
                children = context.getContentResolver().query(childrenUri,
                        new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE},
                        null, null, null);
            } catch (SecurityException e) {
                // No longer can read this URI, which means no images from this URI
                // This a temporary state as the next onLoadFinished() will remove this item entirely
                children = null;
            }
            if (children == null) {
                continue;
            }
            while (children.moveToNext()) {
                String documentId = children.getString(
                        children.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                String mimeType = children.getString(
                        children.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)) {
                    directories.add(documentId);
                } else if (mimeType != null && mimeType.startsWith("image/")) {
                    // Add images to the list
                    images.add(DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId));
                }
                if (images.size() == maxImages) {
                    break;
                }
            }
            children.close();
        }
        return images;
    }

}

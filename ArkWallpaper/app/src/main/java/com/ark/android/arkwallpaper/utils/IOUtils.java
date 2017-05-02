package com.ark.android.arkwallpaper.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * A utility class that can be used to copy image from internal storage to sdcard
 * used for saving image function
 * Created by ahmed-basyouni on 5/1/17.
 */

public class IOUtils {

    public static File exportFile(File src) throws IOException {

        final String CACHE_FOLDER = "ArkWallpaper";
        final String DESTINATION_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + CACHE_FOLDER + File.separator;

        //if folder does not exist
        File cacheFolder = new File(DESTINATION_FOLDER);

        if(!cacheFolder.exists())
            if(!cacheFolder.mkdir())
                return null;

        File dst = new File(DESTINATION_FOLDER + File.separator + src.getName());

        FileChannel inChannel;
        FileChannel outChannel;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                try {
                    inChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            if (outChannel != null)
                try {
                    outChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

        }

        return dst;
    }
}

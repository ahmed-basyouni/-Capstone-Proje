package com.ark.android.arkwallpaper.data.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


import android.content.Context;
import android.os.Parcelable;

import com.ark.android.arkanalytics.GATrackerManager;

/**
 * 
 * @author Basyouni
 *
 */
public class InternalFileSaveDataLayer {

	public static void saveObject(Context context, String fileName,
			Object object) throws FileNotFoundException, IOException {

		if (!(object instanceof Serializable)
				&& !(object instanceof Parcelable))
			throw new RuntimeException(); // TODO

		FileOutputStream stream = context.openFileOutput(fileName,
				Context.MODE_PRIVATE);
		ObjectOutputStream writer = new ObjectOutputStream(stream);

		writer.writeObject(object);
		writer.flush();
		writer.close();
		
		
		

	}
	
	public static Object getObject(Context context, String fileName)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		Object returnObject = null;

		FileInputStream stream = context.openFileInput(fileName);
		ObjectInputStream writer = new ObjectInputStream(stream);

		returnObject = writer.readObject();
		writer.close();

		return returnObject;
	}

	/**
	 * @param path
	 * @return
	 */
	public static boolean deleteObjectFile(String path) {
			File file;
			try {
				file = new File(path);
			} catch (NullPointerException e) {
				GATrackerManager.getInstance().trackException(e);
				return false;
			}

			if (file.exists()) {
				return file.delete();
			}
			return false;
		}
}

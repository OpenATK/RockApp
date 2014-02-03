package com.openatk.rockapp.libcommon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageTools {
	
	public static Bitmap sizePicture(String imageFile, int targetH, int targetW) {
		
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		
		BitmapFactory.decodeFile(imageFile, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
		
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		
		Bitmap bitmap = BitmapFactory.decodeFile(imageFile, bmOptions);
		
		return bitmap;
	}
}

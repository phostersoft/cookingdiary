package com.phostersoft.cookingdiary.utils;

import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;

public class ImageUtils {
	private static int REQUIRED_SIZE = 200;
	
	public static void init(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		REQUIRED_SIZE = metrics.widthPixels;
	}
	
	public static Bitmap decodeImage(ContentResolver cr, Uri imageUri) {
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(cr.openInputStream(imageUri), null, o);

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        return BitmapFactory.decodeStream(
	        		cr.openInputStream(imageUri), null, o2);
	    } catch (FileNotFoundException e) {}
	    return null;
	}
}

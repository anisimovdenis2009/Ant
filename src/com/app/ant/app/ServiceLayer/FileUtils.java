/**
 * 
 */
package com.app.ant.app.ServiceLayer;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.util.Calendar;


public class FileUtils
{

	public static byte[]	alteredImageData	= null;

	public static byte[] saveImageToByteArray(byte[] image)
	{
		// Start sampling and compression into a picture format
		int quality = 90;
		int sampleSize = 1;
		Bitmap myImage = null;

		try
		{
			// Create a BitmapFactory options for sampling
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = sampleSize;

			// decode byte array received
			myImage = BitmapFactory.decodeByteArray(image, 0, image.length, options);

			// Create a new ByteArrayOutputStream to be used for compression
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// compress with quality value into a ByteArrayOutputStream with JPEG format
			myImage.compress(CompressFormat.JPEG, quality, baos);
			baos.flush();

			// Assign alteredImageData to modified picture
			alteredImageData = baos.toByteArray();
			baos.close();
		}
		catch (FileNotFoundException ex)
		{
			ErrorHandler.CatchError("FileUtils.saveImageToByteArray", ex);
		}
		catch (IOException ex)
		{
			ErrorHandler.CatchError("FileUtils.saveImageToByteArray", ex);
		}
		return alteredImageData;
	}
	
	public static String saveImageToSD(String path, byte[] image)
	{				
		int quality = 90;
		int sampleSize = 1;
		
		String result = null;
		String sdcard = Environment.getExternalStorageDirectory().getPath();

		Log.i("FileUtils", "Sd card path is " + sdcard);

		File dir = new File(sdcard + "/" + path);
		dir.mkdirs();

		String fileName = Calendar.getInstance().getTimeInMillis() + ".jpg";

		File file = new File(dir, fileName);
		
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = sampleSize;
			
			FileOutputStream fos = new FileOutputStream(file);
			Bitmap bitMap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
			bitMap.compress(CompressFormat.JPEG, quality, fos);
		}
		catch (FileNotFoundException ex)
		{
			ErrorHandler.CatchError("FileUtils.saveImage", ex);
		}
		result = file.getAbsolutePath();
		
		return result;
	}
	
	public static File saveByteArrayToFile(String path, byte[] image, String fileName)
	{
		File file = null;
		String rootDir = Synchronizer.SD_FILES_EXPORT_PATH;
				
		File dir = new File(rootDir + path);
		dir.mkdirs();
		
		file = new File(dir, fileName);
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(file);
			fos.write(image);
			fos.close();
		}
		catch (FileNotFoundException ex)
		{
			ErrorHandler.CatchError("FileUtils.saveToFileToSD", ex);
		}
		catch (IOException ex)
		{
			ErrorHandler.CatchError("FileUtils.saveToFileToSD", ex);
		}
		return file;
	}

}

package com.app.ant.app.ServiceLayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Archivator
{
	public static void zipFile(String from, String to) throws IOException
	{
		FileInputStream in = new FileInputStream(from);
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(to, true));
		ZipEntry entry = new ZipEntry("ant.db");
		out.putNextEntry(entry);
		byte[] buffer = new byte[4096];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) != -1)
		{
			out.write(buffer, 0, bytesRead);
		}
		
		in.close();
		out.close();
	}	

	/**
	 * Decompress firtst file of the archive to a file with the replacement
	 * 
	 * @param fromZipFile - path to archive
	 * @param toFilePath - path to target file
	 * @return path to target file
	 * @throws IOException
	 */
	public static String unZipFile(String fromZipFile, String toFilePath) throws IOException
	{		 											        	       	        
		FileOutputStream out = null;
		File inFile = null;
		ZipFile zipFile = null;
		File toFile = null;
		
		try
		{
			inFile = new File(fromZipFile);	        
	        zipFile = new ZipFile(inFile);        
	        
	        Enumeration<? extends ZipEntry> entries = zipFile.entries();
	
	        if(entries.hasMoreElements()) 
	        {
	        	ZipEntry entry = (ZipEntry)entries.nextElement();
	        	InputStream in = zipFile.getInputStream(entry);
	        	
	        	toFile = new File(toFilePath);	        	
	        	if (toFile.exists()) toFile.delete();
	        	
	        	out = new FileOutputStream(toFilePath, true);	        	
	    		byte[] buffer = new byte[4096];
	    		
	    		int bytesRead;
	    		while ((bytesRead = in.read(buffer)) != -1)
	    		{
	    			out.write(buffer, 0, bytesRead);
	    		}
	        }	        			        
		}
		catch(Exception ex)		
		{
			ErrorHandler.CatchError("Error in Archivator::UnzipFile", ex);
		}
		finally
		{
    		out.flush();
    		out.close();
			zipFile.close();
		}
		return toFilePath;
	}
	
	/**
	 * Decompress the archive to a folder with the replacement of files
	 * 
	 * @param fromZipFile - path to archive
	 * @param toPath - path to target directory (with '/' on the end)
	 * @throws IOException
	 */
	public static void unZipAll(String fromZipFile, String toPath) throws IOException
	{		 											        	       	        
		FileOutputStream out = null;
		File inFile = null;
		ZipFile zipFile = null;
		File toFile = null;
		
		try
		{
			inFile = new File(fromZipFile);	        
	        zipFile = new ZipFile(inFile);        
	        
	        Enumeration<? extends ZipEntry> entries = zipFile.entries();
	
	        while(entries.hasMoreElements()) 
	        {
	        	ZipEntry entry = (ZipEntry)entries.nextElement();
	        	InputStream in = zipFile.getInputStream(entry);
	        	
	        	String toFilePath = toPath + entry.getName();
	        	toFile = new File(toFilePath);
	        	
	        	if (toFile.exists()) toFile.delete();
	        	
	        	out = new FileOutputStream(toFile);	        	
	    		byte[] buffer = new byte[4096];
	    		
	    		int bytesRead;
	    		while ((bytesRead = in.read(buffer)) != -1)
	    		{
	    			out.write(buffer, 0, bytesRead);
	    		}
	        }	        			        
		}
		catch(Exception ex)		
		{
			ErrorHandler.CatchError("Error in Archivator::unZipAll", ex);
		}
		finally
		{
    		out.flush();
    		out.close();
			zipFile.close();
		}
	}
}

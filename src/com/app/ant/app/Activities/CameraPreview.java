package com.app.ant.app.Activities;

import java.io.IOException;

import com.app.ant.R;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.FileUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;

public class CameraPreview extends Activity implements SurfaceHolder.Callback, OnClickListener
{

	private static final String	TAG				= "CameraPreview";
	Camera						mCamera;
	boolean						mPreviewRunning	= false;

	Long						id				= null;

	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		Log.e(TAG, "onCreate");
		
		Bundle params = getIntent().getExtras();
		id = params.getLong("id");

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera);

		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		mSurfaceView.setOnClickListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
	}

	Camera.PictureCallback	mPictureCallback	= new Camera.PictureCallback()
	{
		public void onPictureTaken(byte[] imageData, Camera c)
		{
			if (imageData != null)
			{				
				byte[] image = FileUtils.saveImageToByteArray(imageData);
				AntContext.getInstance().setLastCameraImage(image);
				Intent resultIntent = new Intent();
				
				resultIntent.putExtra("id", id);			
				
				setResult(RESULT_OK, resultIntent);
				
				finish();
			}
		}
	};

	protected void onResume()
	{
		Log.e(TAG, "onResume");
		super.onResume();
	}

	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
	}

	protected void onStop()
	{
		Log.e(TAG, "onStop");        
		super.onStop();
	}
	
	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.e(TAG, "surfaceCreated");

		mCamera = Camera.open();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		Log.e(TAG, "surfaceChanged");

		// XXX stopPreview() will crash if preview is not running
		if (mPreviewRunning)
		{
			mCamera.stopPreview();
		}

		Camera.Parameters p = mCamera.getParameters();

		p.setPreviewSize(w, h);

		mCamera.setParameters(p);

		try
		{
			mCamera.setPreviewDisplay(holder);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		mCamera.startPreview();

		mPreviewRunning = true;
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.e(TAG, "surfaceDestroyed");
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
	}

	private SurfaceView		mSurfaceView;
	private SurfaceHolder	mSurfaceHolder;

	public void onClick(View arg0)
	{
		mCamera.takePicture(null, mPictureCallback, mPictureCallback);
	}
	
	protected void onDestroy()
	{
		Log.e(TAG, "onDestroy");
		super.onDestroy();
	}	
}

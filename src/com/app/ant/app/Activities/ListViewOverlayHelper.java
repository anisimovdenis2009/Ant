package com.app.ant.app.Activities;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.app.ant.R;


public class ListViewOverlayHelper
{
    private RemoveWindow mRemoveWindow;
    private Handler mHandler;
    private WindowManager mWindowManager;
    private TextView mDialogText;
    private boolean mShowing;
    private boolean mReady;
    private char mPrevLetter = Character.MIN_VALUE;

    public void onResume()  { mReady = true; }   
    public void onPause() 	{ removeWindow(); mReady = false; }
    public void onDestroy() { mWindowManager.removeView(mDialogText);  mReady = false; }
    
    //--------------------------------------------------------------
    private final class RemoveWindow implements Runnable 
    {
        public void run()
        {
            removeWindow();
        }
    }
    
    //--------------------------------------------------------------
    public ListViewOverlayHelper(Context context)
    {
    	mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);    	
    	mRemoveWindow = new RemoveWindow();
    	mHandler = new Handler();

    	LayoutInflater inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDialogText = (TextView) inflate.inflate(R.layout.list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);
    	
        mHandler.post(new Runnable() 
        {
            public void run() 
            {
                mReady = true;
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                        				WindowManager.LayoutParams.TYPE_APPLICATION,
                        				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        				PixelFormat.TRANSLUCENT);
                mWindowManager.addView(mDialogText, lp);
            }
         });
    }
    
    //--------------------------------------------------------------
    public void onScroll(char firstLetter /*AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount*/) 
    {
        //int lastItem = firstVisibleItem + visibleItemCount - 1;
        if (mReady) 
        {
            //char firstLetter = mStrings[firstVisibleItem].charAt(0);
            
            if (!mShowing && firstLetter != mPrevLetter) {

                mShowing = true;
                mDialogText.setVisibility(View.VISIBLE);
               

            }
            mDialogText.setText(((Character)firstLetter).toString());
            mHandler.removeCallbacks(mRemoveWindow);
            mHandler.postDelayed(mRemoveWindow, 2000);
            mPrevLetter = firstLetter;
        }
    }

    //--------------------------------------------------------------
    private void removeWindow() 
    {
        if (mShowing) 
        {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }
}
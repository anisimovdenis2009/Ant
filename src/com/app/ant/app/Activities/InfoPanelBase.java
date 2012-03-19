package com.app.ant.app.Activities;

import com.app.ant.app.ServiceLayer.ErrorHandler;
import android.app.Activity;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.view.MotionEvent;


public abstract class InfoPanelBase
{
	//handle gestures
	protected GestureDetector.OnGestureListener mGestureListener;
	protected GestureDetector mGestureDetector;
	
	protected float mScrollRemainder;		
	protected ViewGroup infoPanelLayout;
	
	//limits
	protected int rowHeight = 20;
	protected int maxHeight = rowHeight*5;
	protected int minHeight = 30;

	/** ���������� ���������� �� �������������� ������*/ 
	public abstract void displayTotals();

	/** ��������� ������ �� �������� � ���������� �� �����.
	 * @param context ��������
	 * @param prevPanel �������� �������, ����������� ��� �������� ������
	 */
	//public abstract void loadInfoPanel(Context context, InfoPanelBase prevPanel);
	
	/*protected void loadInfoPanel( Context context, InfoPanelBase prevPanel, int infoPanelPlacementResId, int infoPanelLayoutResId, int infoPanelLayoutMainViewResId,
							   int rowHeight, int maxHeight, int minHeight)
	{
		int prevPanelHeight = -1;
		Activity activity = (Activity)context;
		
		ViewGroup infoPanelPlacement = (ViewGroup) activity.findViewById(infoPanelPlacementResId);

		//clear previous child view from infoPanelPlacement (if any) 
		if(prevPanel!=null && prevPanel.infoPanelLayout!=null)
		{
			prevPanelHeight = prevPanel.infoPanelLayout.getHeight();
			infoPanelPlacement.removeView(prevPanel.infoPanelLayout);
			prevPanel.infoPanelLayout = null;
		}

		//load new view
		LayoutInflater inflater = activity.getLayoutInflater();
		infoPanelLayout = (ViewGroup)inflater.inflate(infoPanelLayoutResId, (ViewGroup) activity.findViewById(infoPanelLayoutMainViewResId));			
		infoPanelPlacement.addView( infoPanelLayout, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
		
		this.rowHeight = rowHeight;
		this.maxHeight = maxHeight;
		this.minHeight = minHeight;
		
		//set panel size
		if(prevPanelHeight!=-1)
		{
			prevPanelHeight = Math.max( minHeight, Math.min( maxHeight, prevPanelHeight));
			infoPanelLayout.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, prevPanelHeight));
		}
		
		mGestureListener = new GestureDetector.OnGestureListener()
		{
			@Override public void onLongPress(MotionEvent e) {}
			@Override public void onShowPress(MotionEvent e) {}
			@Override public boolean onSingleTapUp(MotionEvent e) 
			{  
				int curHeight = infoPanelLayout.getHeight();
				
				if(curHeight == InfoPanelBase.this.maxHeight) //jump from max to min
				{
					infoPanelLayout.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
							InfoPanelBase.this.minHeight));
				}
				else if(curHeight < InfoPanelBase.this.maxHeight)
				{
					infoPanelLayout.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
														InfoPanelBase.this.maxHeight));
				}
				
				return true; 
			}
			@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {  mScrollRemainder = 0.0f; return true; }
			@Override public boolean onDown(MotionEvent e)	{ mScrollRemainder = 0.0f; return true; }
			
			@Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
			{
				try
				{
					int curHeight = infoPanelLayout.getHeight();
					//curHeight = Math.max(minHeight, curHeight + (int)distanceY );				
					//statPanelLayout.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, curHeight));
										
				    distanceY += mScrollRemainder;
				    int deltaRows = (int) (distanceY / InfoPanelBase.this.rowHeight);
				    mScrollRemainder = distanceY - deltaRows * InfoPanelBase.this.rowHeight;
					
				    curHeight = Math.max( InfoPanelBase.this.minHeight, Math.min( InfoPanelBase.this.maxHeight, curHeight + deltaRows*InfoPanelBase.this.rowHeight));
				    infoPanelLayout.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, curHeight));
					
				}
				catch(Exception ex)
				{
					ErrorHandler.CatchError("Exception in DocSaleForm.InfoPanel.onScroll", ex);
				}
			    
			    return true;
			}
		};
		
		mGestureDetector = new GestureDetector(mGestureListener);
		
		View.OnTouchListener statPanelClickListener = new View.OnTouchListener()
		{				
			@Override public boolean onTouch(View v, MotionEvent ev)
			{
				mGestureDetector.onTouchEvent(ev);					
				return true;
			}
		};
		
		infoPanelLayout.setOnTouchListener(statPanelClickListener);
		
	}*/
}

package com.app.ant.app.Controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.app.ant.R;

public class ChartView extends View 
{
	private final int BORDER_WIDTH = 1;
	private final int BAR_PADDING = 1;
	
	int backgroundColor = Color.TRANSPARENT;
	int borderColor = Color.DKGRAY;
	int smallValueColor = Color.RED;
	int midValueColor = Color.YELLOW;
	int highValueColor = Color.GREEN;
	
	float maxValue = 100.f;
	float[] values = { 0.f, 0.f, 0.f };
	   
	private Paint mRectPaint;
	private Context mContext;
	
	public void setValues(float[] values, float maxValue) { this.values = values; this.maxValue = maxValue; }
	public void setColors(int borderColor, int smallValueColor, int midValueColor, int highValueColor)
			{ this.borderColor = borderColor; this.smallValueColor = smallValueColor; this.midValueColor = midValueColor; this.highValueColor = highValueColor; }
       
	//--------------------------------------------------------------
	public ChartView(Context context, int resImage) 
	{
       super(context);
       mContext = context;

       init();
	}
  
	//--------------------------------------------------------------
	public ChartView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		mContext = context;
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChartView);	   
		a.recycle();
 			
        init();
	}
	
	//--------------------------------------------------------------
	private void init()
	{
        setBackgroundColor(backgroundColor);        
        mRectPaint = new Paint();		
		setPadding(3, 3, 3, 3);  			
	}

	//--------------------------------------------------------------
	protected void onDraw(Canvas canvas)
	{
	   if(values.length==0)
		   return;
		   
	   int width = getMeasuredWidth();
	   int height = getMeasuredHeight();
	   
	   float barWidth = ((float)height)/values.length - BAR_PADDING;
	   
	   for(int i=0; i<values.length; i++)
	   {
		   float barValue = Math.max( Math.min(((float)values[i])/maxValue,1f) , 0.1f);
		   float barHeight = barValue * width; 
		   
		   float left = 0;
		   float right = barHeight;   
		   float top = (float)i*(barWidth + BAR_PADDING);
		   float bottom = top + barWidth;
		   
		   mRectPaint.setStrokeWidth(BORDER_WIDTH);
		   mRectPaint.setColor(borderColor);
		   canvas.drawRect(left, top, right, bottom, mRectPaint);    		   

		   if(barValue<0.33)
			   mRectPaint.setColor(smallValueColor);
		   else if(barValue<0.66)
			   mRectPaint.setColor(midValueColor);
		   else
			   mRectPaint.setColor(highValueColor);
		   
		   mRectPaint.setStrokeWidth(0);
		   float fillLeft 	= Math.min(left+BORDER_WIDTH,width);
		   float fillTop 	= Math.min(top+BORDER_WIDTH,height);
		   float fillRight 	= Math.max(right-BORDER_WIDTH,0);
		   float fillBottom = Math.max(bottom-BORDER_WIDTH,0);
		   
		   canvas.drawRect(fillLeft, fillTop, fillRight, fillBottom, mRectPaint);    		   
	   }
   }

    //--------------------------------------------------------------
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		 setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	//--------------------------------------------------------------
	private int measureWidth(int measureSpec)
	{
		 int preferred = 40;
	     return getMeasurement(measureSpec, preferred);
	}

	//--------------------------------------------------------------
	private int measureHeight(int measureSpec)
	{
	   int preferred = 40;
	   return getMeasurement(measureSpec, preferred);
	}

	//--------------------------------------------------------------
	private int getMeasurement(int measureSpec, int preferred)
	{
         int specSize = MeasureSpec.getSize(measureSpec);
         int measurement = 0;
        
         switch(MeasureSpec.getMode(measureSpec))
         {
             case MeasureSpec.EXACTLY:
                 // This means the width of this view has been given.
		         measurement = specSize;
		         break;
		     case MeasureSpec.AT_MOST:
		         // Take the minimum of the preferred size and what
		         // we were told to be.
                 measurement = Math.min(preferred, specSize);
                 break;
             default:
                 measurement = preferred;
                 break;
         }
    
         return measurement;
	}

 }
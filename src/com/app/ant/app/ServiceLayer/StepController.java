package com.app.ant.app.ServiceLayer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.Activities.ClientListForm;
import com.app.ant.app.ServiceLayer.TabController.BackEventFlags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;


public abstract class StepController
{
	/** ������ ����������� �����: ������������� ��� �����������*/
	public enum StepPanelType { HORIZONTAL, VERTICAL }; 
	
	public static final int DOC_SALE_MODE_REMNANTS = 0;
	public static final int DOC_SALE_MODE_BASIC = 1;
	
	public static final int STEP_DEFAULT = 0;
	public static final int STEP_ACTION = 1;
	public static final int STEP_INVISIBLE = 2;
	public static final int STEP_FILL_PARENT = 4;
	public static final int STEP_TEXT_ONLY = 8;
	public static final int STEP_MENU = 16;

	public int mCurrentStepId;

	private Context mContext = null;
	private ViewGroup mContainerView = null;
	
	ArrayList<Activity> cachedActivities;
	
	protected LinkedHashMap<Integer, StepInfo> steps = new LinkedHashMap<Integer, StepInfo>();
	
	//could be overriden in child where needed
	protected boolean checkCanFinish(Context context, int stepId) { return true; }
	protected boolean checkNeedTheStep(int stepId) { return true; }

	
	public int getCurrentStepId() {	return mCurrentStepId; }
	
    //--------------------------------------------------------------
	/** ��������� ��� �������������� � ������� ����*/
    public interface IStep 
    {    	
    	abstract boolean haveUnsavedChanges();
    }
    //--------------------------------------------------------------
	/** ��������� ��������� - �������� ��� ��� �������� �� ������� ��������� ���� ��� �������� �� ������ ����*/
    public interface ICacheableStep
    {    	
    }
    
    //--------------------------------------------------------------
    /** ���������� � ���� ���������� ���������������� ��������.*/
    public static class StepInfo
    {
    	int stepId;
    	int labelResourceId = 0;
    	int picResourceId;
    	int flags;
    	boolean fulfilled;
    	String label;
    	boolean isMandatory = false;
    	
    	public StepInfo( int stepId, int labelResourceId, int picResourceId, int flags, boolean isMandatory )
    	{
    		this.stepId = stepId;
    		this.labelResourceId = labelResourceId;
    		this.picResourceId = picResourceId;
    		this.flags = flags;
    		this.fulfilled = false;
    		this.isMandatory = isMandatory;
    	};
    	
    	public StepInfo( int stepId, String label, int picResourceId, int flags, boolean isMandatory )
    	{
    		this.stepId = stepId;
    		this.label = label;
    		this.picResourceId = picResourceId;
    		this.flags = flags;
    		this.fulfilled = false;
    		this.isMandatory = isMandatory;
    	}
    };
	
    //--------------------------------------------------------------
	public	StepController()
	{
		mCurrentStepId = -1;		
		cachedActivities = new ArrayList<Activity>();
	}
	
    //--------------------------------------------------------------	
	public StepInfo findStepByID(int stepId)
	{
		return steps.get(stepId);
	}

    //--------------------------------------------------------------	
	public void addStep(StepInfo step)
	{
		if(step == null)
			return;
		
		steps.put(step.stepId, step);
	}
	
    //--------------------------------------------------------------
	public boolean startStep(Context context, int stepId, boolean fromTabController, Bundle params)
	{
		StepInfo step = findStepByID(stepId);
		
		if(step == null)
			return false;
		
		if(	(step.flags & STEP_ACTION) == 0 )
		{
			mCurrentStepId = stepId;
		}
		
		return true;
	}
	
	//--------------------------------------------------------------
	private class StepButtonInfo
	{
		int stepId;
		StepPanelType panelType;
		
		StepButtonInfo(int stepId, StepPanelType panelType) { this.stepId = stepId; this.panelType = panelType; }
	}
	
    //--------------------------------------------------------------
	public void CreateButtons(Context context, ViewGroup containerView, StepPanelType panelType)
	{
		mContext = context;
		mContainerView = containerView;
		
		View.OnClickListener stepClickListener = new View.OnClickListener() 
		{				
			@Override public void onClick(View v) 
			{		
				try
				{
					StepButtonInfo buttonInfo = (StepButtonInfo) v.getTag();			
					if(buttonInfo.stepId == mCurrentStepId)
						return;
					
					//deselect all buttons except one  
					if(buttonInfo.panelType == StepPanelType.HORIZONTAL)
					{
						for(int i = 0; i<mContainerView.getChildCount();i++)
						{					
							View stepLayout = mContainerView.getChildAt(i);
							ImageButton button = (ImageButton) stepLayout.findViewById(R.id.buttonStep);
							View selectionLayout = (View) stepLayout.findViewById(R.id.selectionLayout);
							int currentStepId = ((StepButtonInfo)button.getTag()).stepId;
							
							StepInfo stepInfo = findStepByID(buttonInfo.stepId); 
								
							if(currentStepId == buttonInfo.stepId && (stepInfo.flags & STEP_ACTION) ==0)
								selectionLayout.setBackgroundResource(R.drawable.border1);
							else
								selectionLayout.setBackgroundResource(0);
						}
					}
					
					startStep(mContext, buttonInfo.stepId, false, null);
				}
				catch(Exception ex)
				{					
					ErrorHandler.CatchError("Exception in StepController.onClick", ex);
				}				
			}
		};
		
		boolean disableNextButtons = false;

		Iterator<StepInfo> it = steps.values().iterator();
		while(it.hasNext())
		{
			StepInfo stepInfo = it.next();
			
			if( (stepInfo.flags&STEP_INVISIBLE)!=0)
				continue;
			
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			String buttonText = (stepInfo.labelResourceId!=0) ? context.getResources().getString(stepInfo.labelResourceId) : stepInfo.label; 
			
			if(panelType == StepPanelType.HORIZONTAL)
			{
				//add button to panel
				View layout = inflater.inflate(R.layout.step_button, null);
				ImageButton button = (ImageButton) layout.findViewById(R.id.buttonStep);			
				TextView label = (TextView) layout.findViewById(R.id.textStep);				
				label.setText(buttonText);
				button.setImageResource(stepInfo.picResourceId);
				button.setTag(new StepButtonInfo(stepInfo.stepId, panelType));
				containerView.addView(layout);
				
				if(disableNextButtons)
				{
					button.setEnabled(false);
					label.setTextColor(0xFF909090);
				}
				else
				{
					button.setOnClickListener(stepClickListener);
				}
				
				//draw a mark that step is selected
				View selectionLayout = (View) layout.findViewById(R.id.selectionLayout);
				if(stepInfo.stepId == mCurrentStepId && (stepInfo.flags & STEP_ACTION) ==0 )
					selectionLayout.setBackgroundResource(R.drawable.border1);
				else
					selectionLayout.setBackgroundResource(0); //remove any background
			}
			else
			{
				//add button to panel				
				View layout = inflater.inflate(R.layout.step_button_wide, null);
				Button button = (Button) layout.findViewById(R.id.buttonStep);
				button.setText(buttonText);
				button.setCompoundDrawablesWithIntrinsicBounds(stepInfo.picResourceId,0,0,0);
				button.setTag(new StepButtonInfo(stepInfo.stepId, panelType));
				
				ImageButton buttonNotFulfilled = (ImageButton) layout.findViewById(R.id.buttonStepNotFulfilled);
				ImageButton buttonFulfilled = (ImageButton) layout.findViewById(R.id.buttonStepFulfilled);
				
				if(stepInfo.fulfilled)
				{
					buttonNotFulfilled.setVisibility(View.GONE);
					buttonFulfilled.setVisibility(View.VISIBLE);
				}
				else
				{
					buttonNotFulfilled.setVisibility(View.VISIBLE);
					buttonFulfilled.setVisibility(View.GONE);
				}
				
				containerView.addView(layout);
				
				if(disableNextButtons)
				{
					button.setEnabled(false);
					button.setTextColor(0xFF909090);					
				}
				else
				{
					button.setOnClickListener(stepClickListener);
				}
			}
			
			if(stepInfo.isMandatory && !stepInfo.fulfilled)
				disableNextButtons = true;
		}
	}

    //--------------------------------------------------------------
	protected void FinishPrevActivity(Context context)
	{
		if( !(context instanceof ClientListForm) && !(context instanceof ICacheableStep))
			((Activity)context).finish();
	}
	
	//--------------------------------------------------------------
	public void onBackPressed(Context context)
	{
		onBackPressed(context, BackEventFlags.LAUNCH_NEW_ACTIVITY);	
	}
	
	public void onBackPressed(Context context, BackEventFlags backFlags)
	{
		//check if activity want to be finished
		if( checkCanFinish(context, mCurrentStepId) == false)
			return;
		
		int menuStep = -1;
	
		Iterator<StepInfo> it = steps.values().iterator();
		while(it.hasNext())
		{
			StepInfo stepInfo = it.next();
			if( (stepInfo.flags&STEP_MENU) != 0 )
			{
				menuStep = stepInfo.stepId;
				break;
			}			
		}
		
		if( menuStep!=-1 && mCurrentStepId != menuStep )
		{
			if(backFlags == BackEventFlags.LAUNCH_NEW_ACTIVITY)
				startStep(context, menuStep, false, null);
			else
			{
				if((context instanceof ICacheableStep))
					finishCachedActivities();
				mCurrentStepId = menuStep; //do not launch any activity, just remember current step
			}
		}
		else
		{
			if((context instanceof ICacheableStep))
				finishCachedActivities();
			else					
				((Activity)context).finish();
		}
	}

	//--------------------------------------------------------------
	public void registerActivityOnLaunch(Context context)	
	{
		if(context instanceof ICacheableStep)
			cachedActivities.add((Activity) context);
	}
	
	//--------------------------------------------------------------
	public void finishCachedActivities()
	{
		for(int i=0; i<cachedActivities.size(); i++)
		{
			Activity activity = cachedActivities.get(i);
			activity.finish();
		}
		
		cachedActivities.clear();
	}
	
	//---------------------------------------------------------------
	public boolean haveUnsavedChanges()
	{
		for(int i=0; i<cachedActivities.size(); i++)
		{
			Activity activity = cachedActivities.get(i);
			if( activity instanceof IStep && ((IStep)activity).haveUnsavedChanges())
				return true;
		}		
		return false;
	}	
	
	//--------------------------------------------------------------
	public void onNextStepPressed(Context context)
	{
		//check if activity want to be finished
		if( checkCanFinish(context, mCurrentStepId) == false)
			return;
	
		int curStepId = mCurrentStepId;

		//iterate through steps, skip unneeded steps
		boolean reachedCurrentStep = false;
		StepInfo step = null;
		
		Iterator<StepInfo> it = steps.values().iterator();
		while(it.hasNext())
		{
			StepInfo stepInfo = it.next();
			if(stepInfo.stepId == curStepId)
			{
				reachedCurrentStep = true;
				continue;
			}
			
			//start check of the steps after current; skip invisible steps; check if next step is needed
			if(reachedCurrentStep && (stepInfo.flags & STEP_INVISIBLE) == 0 && checkNeedTheStep(stepInfo.stepId)) 
			{				
				step = stepInfo;	//found next step
				break;
			}
		}	

		if(step!=null)
		{
			if((context instanceof ICacheableStep))
				finishCachedActivities();
			
			startStep(context, step.stepId, false, null);
		}
		else
		{
			onBackPressed(context, BackEventFlags.LAUNCH_NEW_ACTIVITY);		
		}
	}
	
	
}	
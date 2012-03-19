package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.app.ant.R;

import java.util.ArrayList;


public class TabController
{
	/** �������� �������� �� ������ Back*/
	public enum BackEventFlags { LAUNCH_NEW_ACTIVITY, DO_NOT_LAUNCH_NEW_ACTIVITY };
	
	public static final int TAB_FLAGS_DEFAULT = 0;
	public static final int TAB_FLAGS_MENU = 1;
	
	public int currentTab;

	private Context mContext = null;
	private ViewGroup mContainerView = null;

	ArrayList< TabItem > mTabs;
	
	//--------------------------------------------------------------
	/** Callback: �������� � ������������� ��������*/
    public interface ITabEventListener
    {    	
        abstract void onTabSelected(int tab);
    } 
    private ITabEventListener tabEventListener = null;
    public void setTabEventListener(ITabEventListener tabEventListener) { this.tabEventListener = tabEventListener; }
	//--------------------------------------------------------------
    public int getSelectedTab() { return currentTab; }    
	
	//--------------------------------------------------------------
	public StepController getStepController(int tabNum)
	{
		if(tabNum == -1)
			return null;
		
		return mTabs.get(tabNum).stepController;
	}
	
    //--------------------------------------------------------------	
	public StepController getStepControllerByType(int tabType)
	{
		for(int i=0; i<mTabs.size(); i++)
			if(mTabs.get(i).type == tabType)
				return mTabs.get(i).stepController; 
			
		return null;			
	}
	
    //--------------------------------------------------------------
	public class TabItem
	{		
		public int type;
		public String name;
		public StepController stepController;
		public int flags;
		
		public TabItem( int type, String name, StepController stepController, int flags )
		{
			this.type = type;
			this.name = name;
			this.stepController = stepController;
			this.flags = flags;
		}
	}
    //--------------------------------------------------------------
	public	TabController()
	{
		mTabs = new ArrayList<TabItem>();
		currentTab = -1;
	}
	
	//--------------------------------------------------------------
	public void removeAllTabs()
	{
		mTabs.clear();
		currentTab = -1;
	}
	
	//--------------------------------------------------------------
	public void removeCurrentTab()
	{
		if(currentTab!=-1)
			mTabs.remove(currentTab);
		
		if(mTabs.size()!=0)
			selectTab(0);
	}	
	

    //--------------------------------------------------------------
	public void startTab(Context context, int tabType, int stepNum, Bundle params)
	{			
		//currentTab = AddTab(context, tabType);
		int destTab = findTab(tabType);
		if(destTab!=-1)
		{
			StepController sc = getStepController(destTab);
			if(sc!=null)
			{
				boolean stepResult = sc.startStep(context, stepNum, true, params);
				if(stepResult==true)
					currentTab = destTab;
			}
			else
				currentTab = destTab; 
		}
	}

	//--------------------------------------------------------------
	public int findTab(int tabType)
	{
		int tabNum = -1;
		
		for(int i=0; i<mTabs.size(); i++)
		{
			if(mTabs.get(i).type == tabType)
			{
				tabNum = i;
				return tabNum;				
			}
		}
		
		return tabNum;
	}	
	//--------------------------------------------------------------
	public int addTab(Context context, int tabType, StepController stepController, int tabTextId, int tabFlags)
	{
		int addedTab = findTab(tabType);
	
		if(addedTab == -1)
		{
			String tabText = context.getResources().getString(tabTextId);
			mTabs.add( new TabItem(tabType, tabText, stepController, tabFlags));
			
			addedTab = mTabs.size()-1;
		}
		
		return addedTab;
	}

    //--------------------------------------------------------------
	public void selectTab(int tabNum)
	{
		currentTab = tabNum;
		StepController sc = getStepController(currentTab);
		if(sc!=null)
		{
			int stepId = (sc.getCurrentStepId()!=-1) ? sc.getCurrentStepId():0;		
			sc.startStep(mContext, stepId, true, null);
		}
		
		if(tabEventListener!=null)
			tabEventListener.onTabSelected(currentTab);
	}
	//--------------------------------------------------------------
	private void markTab(int tabNum)
	{
		//deselect all tabs except one  
		for(int i = 0; i<mContainerView.getChildCount();i++)
		{					
			View tabLayout = mContainerView.getChildAt(i);
			//View selectionLayout = (View) stepLayout.findViewById(R.id.selectionLayout);
			if(i == tabNum)
			{
				tabLayout.setBackgroundResource(R.drawable.tab_selected);
				((TextView)tabLayout).setTextColor(Color.WHITE);
			}
			else
			{
				tabLayout.setBackgroundResource(R.drawable.tab_unselected);
				((TextView)tabLayout).setTextColor(Color.BLACK);
			}
		}		
	}	
	
	//--------------------------------------------------------------
	public void refreshTabs(Context context, ViewGroup containerView)
	{
		mContext = context;
		mContainerView = containerView;

		markTab(currentTab);
	}	
    //--------------------------------------------------------------
	public void createTabs(Context context, ViewGroup containerView)
	{
		mContext = context;
		mContainerView = containerView;
		
		View.OnClickListener tabClickListener = new View.OnClickListener() 
		{				
			@Override public void onClick(View v) 
			{				
				try
				{
					int tabNum = Integer.valueOf(v.getTag().toString());
					
					if(tabNum == currentTab)
						return;
					
					markTab(tabNum);				
					selectTab(tabNum);
				}
				catch(Exception ex)
				{					
					ErrorHandler.CatchError("Exception in TabController.onClick", ex);
				}
			}
		};
		
		if (mTabs.size() > 1)
		{
			for(int i = 0; i < mTabs.size(); i++)
			{
				TabItem tabItem = mTabs.get(i);			
				TextView label = new TextView(mContext);
				
				if(tabItem == null || label == null)
		    		continue;
				
				label.setText(tabItem.name);
				label.setPadding(0, 0, 0, 0);
				label.setTag(i);
				label.setOnClickListener( tabClickListener);
				label.setTextSize(12);
				label.setGravity(Gravity.CENTER);
				label.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
	
				//label.setTypeface(Typeface.DEFAULT_BOLD);
				
				if(i == currentTab)
				{
					label.setBackgroundResource(R.drawable.tab_selected);
					label.setTextColor(Color.WHITE);
				}				
				else
				{
					label.setBackgroundResource(R.drawable.tab_unselected);
					label.setTextColor(Color.BLACK);
				}
				
				mContainerView.addView(label);
			}
		}
	}

	//--------------------------------------------------------------
	public void onBackPressed(Context context)
	{
		onBackPressed(context, BackEventFlags.LAUNCH_NEW_ACTIVITY);
	}
	
	public void onBackPressed(Context context, BackEventFlags backFlags )
	{
		int menuTab = -1;
	
		for(int i=0; i<mTabs.size(); i++)
		{
			TabItem tabItem = mTabs.get(i);

			if( (tabItem.flags & TAB_FLAGS_MENU)!=0)
			{
				menuTab = i;
				break;
			}
		}
		
		if(menuTab!=-1 && currentTab!=menuTab)
		{
			//menu tab exists and do not selected. select it
			selectTab(menuTab);			
		}
		else
		{
			//menu tab is current tab; pass backPressed event to it
			StepController sc = getStepController(currentTab);
			if(sc!=null)			
				getStepController(currentTab).onBackPressed(context, backFlags);
		}		
	}

	//--------------------------------------------------------------
	public void onNextStepPressed(Context context)
	{
		StepController sc = getStepController(currentTab);
		if(sc!=null)			
			getStepController(currentTab).onNextStepPressed(context);
		
	}
	
}
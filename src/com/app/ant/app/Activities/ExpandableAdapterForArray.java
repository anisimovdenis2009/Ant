package com.app.ant.app.Activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import java.util.ArrayList;
import java.util.Map;

public class ExpandableAdapterForArray extends BaseExpandableListAdapter 
{
    //--------------------------------------------
	/** ���������, ������� ������ ����������� ����� ������������� ��������*/
    public interface IGroupItem
    {
    	public Long getID();    	
    	public void fillView(View view, int viewId, boolean isExpanded);
    	public boolean isEnabled();
    }
    
    /** ���������, ������� ������ ����������� ����� ������������ ��������*/
    public interface IChildItem extends IGroupItem
    {
    }
    
    //--------------------------------------------
    public class ViewHolder
    {
    	ArrayList<View> views;
    	
    	public ViewHolder(View parentView, int[] viewIds)
    	{
    		views = new ArrayList<View>();
    		
    		for(int i=0; i<viewIds.length; i++)
    		{
    			View view = parentView.findViewById(viewIds[i]);
    			if(view!=null)
    				views.add(view);
    		}
    	}
    }
    
    //--------------------------------------------
    
	private LayoutInflater mInflater;
    
	//data
    private ArrayList<IGroupItem> groups;
    Map<Long, ArrayList<IChildItem> > children;
    int groupResourceID;
    int childResourceID;
    int[] groupViewIds;
    int[] childViewIds;
	
    //--------------------------------------------
    public ExpandableAdapterForArray(Context context, ArrayList<IGroupItem> groups, Map<Long, ArrayList<IChildItem> > children,
    									int groupResourceID, int childResourceID, int[] groupViewIds, int[] childViewIds) 
    {
        mInflater = LayoutInflater.from(context);
        this.groups = groups;
        this.children = children;
        this.groupResourceID = groupResourceID;
        this.childResourceID = childResourceID;
        this.groupViewIds = groupViewIds;
        this.childViewIds = childViewIds;
    }
    
    public Object getChild(int groupPosition, int childPosition) 
    { 
    	try
    	{
	    	long groupID = groups.get(groupPosition).getID();
	    	if(children.get(groupID)!=null)
	    		return children.get(groupID).get(childPosition);
	    	else
	    		return null;
       	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in ExpandableAdapterForArray.getChild", ex);
		}	
		
		return null;	    	
    }
    
    public int getChildrenCount(int groupPosition) 
    { 
    	try
    	{
	    	long groupID = groups.get(groupPosition).getID();
	    	ArrayList<IChildItem> childItems = children.get(groupID); 
	    	if( childItems != null)
	    		return childItems.size();
	    	else
	    		return 0;
       	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in ExpandableAdapterForArray.getChildrenCount", ex);
		}	
	    	
		return 0;
    }
    
    public long getChildId(int groupPosition, int childPosition) { return childPosition; }        
    public Object getGroup(int groupPosition) { return groups.get(groupPosition); }
    public int getGroupCount() {  return groups.size();  }
    public long getGroupId(int groupPosition) {  return groupPosition; }
    public boolean isChildSelectable(int groupPosition, int childPosition) {  return true;  }
    public boolean hasStableIds() { return true; }

    //--------------------------------------------        
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            					View convertView, ViewGroup parent) 
    {
    	try
    	{
	    	Object childObject = getChild(groupPosition, childPosition);
	    	
	    	ViewHolder holder; 
	
	        if (convertView == null) 
	        {
	            convertView = mInflater.inflate(childResourceID, null);
	            holder = new ViewHolder( convertView, childViewIds);
	            convertView.setTag(holder);
	        } 
	        else 
	        {
	            holder = (ViewHolder) convertView.getTag();
	        }
	
	        // Bind the data with the holder.
	        for(int i=0; i<holder.views.size(); i++)
	        {
	        	View view = holder.views.get(i);
	        	int viewId = view.getId();
	        	((IChildItem) childObject).fillView(view, viewId, false);	        	
	        }
       	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in ExpandableAdapterForArray.getChildView", ex);
		}	


        return convertView;
    }

    //--------------------------------------------
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            						ViewGroup parent) 
    {
    	try
    	{
	    	Object groupObject = getGroup(groupPosition);
	    	
	    	ViewHolder holder; 
	
	        if (convertView == null) 
	        {
	            convertView = mInflater.inflate(groupResourceID, null);
	            holder = new ViewHolder( convertView, groupViewIds);
	            convertView.setTag(holder);
	        } 
	        else 
	        {
	            holder = (ViewHolder) convertView.getTag();
	        }
	
	        
	        for(int i=0; i<holder.views.size(); i++)
	        {
	        	View view = holder.views.get(i);        	
	        	int viewId = view.getId();
	        	
	        	((IGroupItem) groupObject).fillView(view, viewId, isExpanded);	        	
	        }            
       	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in ExpandableAdapterForArray.getGroupView", ex);
		}	
        
        return convertView;
    }
}    

package com.app.ant.app.Activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IGroupItem;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;


public class HierarchicalListAdapter extends BaseAdapter 
{
	//-------------------------------------------------------
	public static class TreeItem
	{
		Long id;
		Long parentId;
		int depth = 0;		
		boolean isExpanded = false;
		boolean isMandatory = false;
		IGroupItem displayableItem;
		
		ArrayList<TreeItem> children = new ArrayList<TreeItem>();

		public int getChildCount()
		{
			return children.size();
		}
		
		public TreeItem(Long id, Long parentId, boolean isExpanded, boolean isMandatory, IGroupItem displayableItem)
		{
			this.id = id;
			this.parentId = parentId;
			this.isExpanded = isExpanded;
			this.isMandatory = isMandatory;
			this.displayableItem = displayableItem;
			
			if(parentId == null)
				depth = 0;
		}
	};
	
	//-------------------------------------------------------	
	public static class TreeItems
	{
		private LinkedHashMap<Long, TreeItem> itemMap = new LinkedHashMap<Long, TreeItem>();

		public Iterator<TreeItem> iterator() { return itemMap.values().iterator(); }
		
		public void add(TreeItem item)
		{
			itemMap.put(item.id, item);
			
			//�������� ������� � ������ ����� ������������� ��������
    		if(item.parentId!=null)
    		{
    			TreeItem parentItem = get(item.parentId);
    			if(parentItem!=null)
    			{
    				parentItem.children.add(item);
    				//item.depth = parentItem.depth+1;
    			}
    		}
    		
    		//�������� �� ������� ������������ ���������, �������� �� � ������ �����, ���� ������� ������� �������� �� ���������
			Iterator<TreeItem> it = itemMap.values().iterator();			
	        while (it.hasNext()) 
	        {
	        	TreeItem oldItem = it.next();
	        	
	        	if(oldItem.parentId!=null && oldItem.parentId.equals(item.id)) 
        		{
        			item.children.add(oldItem);
        			//oldItem.depth = item.depth + 1;
        		}
        	}    		
		}
		
		public TreeItem get(Long key)
		{
			return itemMap.get(key);
		}
		
		public boolean containsKey(Object key)
		{
			return itemMap.containsKey(key);
		}
		
		public boolean isEmpty()
		{
			return itemMap.isEmpty();
		}

		private void getChildren(int getDepth, boolean visibleOnly, ArrayList<TreeItem> items, 
										TreeItem parent, int parentDepth)
		{
			for(TreeItem item: parent.children)
			{
				item.depth = parentDepth+1;
				/*��������� ������ �������� ��������������� ������������� ������� ������, ��� ��� ���� getDepth = -1*/
				if(getDepth==-1 || item.depth==getDepth) 
					items.add(item);
				if(item.isExpanded || !visibleOnly) /*���������� isExpanded ���� ��������� ��� ��������*/
					getChildren(getDepth, visibleOnly, items, item, item.depth);
			}
		}
		
        public ArrayList<TreeItem> getItems(int getDepth, boolean visibleOnly)
        {        	
        	ArrayList<TreeItem> items = new ArrayList<TreeItem>();

			Iterator<TreeItem> it = itemMap.values().iterator();
	        while (it.hasNext()) 
	        {
	        	TreeItem item = it.next();
	        	if(item.parentId == null) //top-level items, depth = 0
        		{
	        		if(getDepth==-1 || item.depth==getDepth)
	        			items.add(item);
        			if(getDepth!=0 && (item.isExpanded || !visibleOnly))
        				getChildren(getDepth, visibleOnly, items, item, 0);
        		}
        	}
	        
	        return items; 
        }
        
        public TreeItem getParentItem(Long itemId)
        {
        	TreeItem item = get(itemId);
        	return getParentItem(item);
        }
        
        public TreeItem getParentItem(TreeItem item)
        {
        	if(item==null)
        		return null;
        	
			Long parentId = item.parentId;
			if(parentId!=null && item.id != parentId) //check to avoid endless cycle
			{
				TreeItem parentItem = get(parentId);
				return parentItem;
			}
        	
			return null;
        }		
        
        public boolean expandOrCollapseNode(Long itemId)
        {
			TreeItem item = get(itemId);
			if(item!=null)
			{
				item.isExpanded = !item.isExpanded;
				return true;
			}								
        	return false;
        }
        
        public void expandBranchContainingItem(Long itemId)
        {
			TreeItem parentItem = getParentItem(itemId);			
			while(parentItem!=null)
			{
				parentItem.isExpanded = true;
				parentItem = getParentItem(parentItem);
			}      	
        }
        
        public Long getItemTopParentId(Long itemId, int depth)
        {
        	while(true)
        	{
        		TreeItem item = get(itemId);
        		if(itemId == item.parentId) //wrong, return to avoid endless cycle
        			return null;
        		else if(item.depth == depth)
        			return item.id;			//we've found top level item
        		else
        			itemId = item.parentId; //advance one level higher and continue search
        	}
        }
        
        public TreeItem getItemTopParent(Long itemId, int depth)
        {
        	Long id = getItemTopParentId(itemId, depth);
        	if(id!=null)
        		return get(id);
        		
        	return null;	
        }
        
	}
	
	//-------------------------------------------------------
    private LayoutInflater mInflater;
    private Context mContext;
    
    TreeItems items;
    ArrayList<TreeItem> visibleItems = new ArrayList<TreeItem>();
    int rowResourceID;
    int[] rowViewIds;
    
	//-------------------------------------------------------	
    private static class TreeItemViewHolder 
    {        
        TextView text;
        ImageView imgExpand;
        ImageView imgCollapse;
        ViewGroup hierarchyLayoutItem;
        
    	ArrayList<View> views;
    	
    	public TreeItemViewHolder(View parentView, int[] viewIds)
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
 
	//-------------------------------------------------------
    public HierarchicalListAdapter(Context context, TreeItems items, int rowResourceID, int[] rowViewIds) 
    {
    	try
    	{
			mContext = context;        	            
            mInflater = LayoutInflater.from(context);
            this.items = items;
            this.rowResourceID = rowResourceID;
            this.rowViewIds = rowViewIds;
            
            visibleItems = items.getItems(-1, true);
    	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in TreeListAdapter", ex);
		}			
    }

	//-------------------------------------------------------
    public int getCount() { return visibleItems.size(); }
    public Object getItem(int position) {  return position;  }
    public long getItemId(int position) {  return position;  }

	//-------------------------------------------------------
    public View getView(int position, View convertView, ViewGroup parent) 
    {
    	try
    	{      		        	
    		//LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_group_list_item_hierarchical, (ViewGroup) findViewById(R.id.hierarchyLayoutItem));	
    	   	TreeItemViewHolder holder;

            if (convertView == null) 
            {
                convertView = mInflater.inflate(rowResourceID, null);
                holder = new TreeItemViewHolder(convertView, rowViewIds);
                holder.text = (TextView) convertView.findViewById(R.id.textGroupName);
                holder.imgExpand = (ImageView) convertView.findViewById(R.id.imgExpand);
                holder.imgCollapse = (ImageView) convertView.findViewById(R.id.imgCollapse);
                holder.hierarchyLayoutItem = (ViewGroup) convertView.findViewById(R.id.hierarchyLayoutItem);
                
                convertView.setTag(holder);
            } 
            else 
            {
                holder = (TreeItemViewHolder) convertView.getTag();
            }
        	
            TreeItem item = visibleItems.get(position);
            
            if(item.displayableItem == null)
            	holder.text.setText("" + item.id + "->" + item.parentId + "->" + item.depth + "->" + item.getChildCount());	//for debugging
            
            holder.imgExpand.setTag(item.id);
            holder.imgCollapse.setTag(item.id);
            
            holder.hierarchyLayoutItem.setPadding(36*item.depth, holder.hierarchyLayoutItem.getPaddingTop(), 
        											holder.hierarchyLayoutItem.getPaddingRight(), holder.hierarchyLayoutItem.getPaddingBottom());
            
            if(item.getChildCount()>0)
            {
	            holder.imgCollapse.setVisibility(item.isExpanded? View.VISIBLE : View.GONE);
            	holder.imgExpand.setVisibility(item.isExpanded? View.GONE : View.VISIBLE);
            	
            	View.OnClickListener expandClickListener = new View.OnClickListener()
            	{
					@Override public void onClick(View v) 
					{	 
						try
						{
							Long id = (Long)v.getTag();
							if(items.expandOrCollapseNode(id))
							{
					            visibleItems = items.getItems(-1, true);
								notifyDataSetChanged();									
							}										
						}
						catch(Exception ex)
						{							
							ErrorHandler.CatchError("Exception in expandClickListener.onClick", ex);							
						}
					}	            		
            	};

            	if( item.displayableItem==null || (item.displayableItem!=null && item.displayableItem.isEnabled()))
            		holder.imgExpand.setOnClickListener(expandClickListener);
            	else
            		holder.imgExpand.setOnClickListener(null);
            	
            	holder.imgCollapse.setOnClickListener(expandClickListener);
            }
            else
            {
            	holder.imgExpand.setVisibility(View.INVISIBLE);
            	holder.imgCollapse.setVisibility(View.GONE);
            }
            
            //let the item display itself
            if(item.displayableItem!=null)
            {
		        for(int i=0; i<holder.views.size(); i++)
		        {
		        	View view = holder.views.get(i);        	
		        	int viewId = view.getId();
		        	
		        	((IGroupItem) item.displayableItem).fillView(view, viewId, item.isExpanded);	        	
		        }
            }
        }
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in aimsForm.AimsAdapter.getView", ex);
		}				
		
		return convertView;
    }        
}


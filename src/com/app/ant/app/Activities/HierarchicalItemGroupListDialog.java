package com.app.ant.app.Activities;

import com.app.ant.R;
import com.app.ant.app.Activities.HierarchicalListAdapter.TreeItem;
import com.app.ant.app.Activities.HierarchicalListAdapter.TreeItems;
import android.os.Bundle;
import android.widget.ListView;


public class HierarchicalItemGroupListDialog extends AntActivity
{
	//-------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_group_list_hierarchical);
		
        ListView itemGroupList = ((ListView) findViewById(R.id.itemGroupList));
		
		//-------------------------------------------------------	
        TreeItems itemGroups = new TreeItems();
    	
    	// Put elements to the map
    	itemGroups.add(new TreeItem(1L, null, false, false, null));
    	itemGroups.add(new TreeItem(11L, 1L, false, false, null));
    	itemGroups.add(new TreeItem(12L, 1L, false, false, null));
    	itemGroups.add(new TreeItem(111L, 11L, false, false, null));
    	itemGroups.add(new TreeItem(112L, 11L, false, false, null));  	
    	itemGroups.add(new TreeItem(2L, null, false, false, null));
    	itemGroups.add(new TreeItem(3L, null, false, false, null));
    	itemGroups.add(new TreeItem(4L, null, false, false, null));
    	
		//-------------------------------------------------------    	

    	int[] rowViewIds = new int[] { R.id.textGroupName };    	
    	HierarchicalListAdapter adapter = new HierarchicalListAdapter(this, itemGroups,
    																R.layout.item_group_list_item_hierarchical, rowViewIds);
        itemGroupList.setAdapter(adapter);

	}	
}

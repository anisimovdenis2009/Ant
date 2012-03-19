package com.app.ant.app.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IChildItem;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IGroupItem;
import com.app.ant.app.Activities.HierarchicalListAdapter.TreeItem;
import com.app.ant.app.Activities.HierarchicalListAdapter.TreeItems;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import java.util.ArrayList;


public class DocSaleSelectGroupDialog extends DialogBase 
{
	public static final int DICTIONARY_IDX_CATEGORIES 	= 0;
	public static final int DICTIONARY_IDX_CHANNELS 	= 1;
	public static final int DICTIONARY_IDX_CLOSED_ORDER	= 2;
	public static final int DICTIONARY_IDX_ITEM_LIST 	= 3;
	
    public static enum SelectorType 
    {
        order, closedOrder, itemList 
    }

	public static final int GROUP_TYPE_DOC_STEP	= 4;
	public static final int GROUP_TYPE_DOC_STEP1 = 5;
	
	public static final int GROUP_FLAG_HIDDEN = 1;
	public static final int GROUP_FLAG_ALWAYS_ENABLED = 2;
	
	/** ��������� ��� ��������� ������ - ���������� ��������� ������ �������*/
    public interface OnSelectGroupListener 
    {    	
        abstract void onGroupSelected(ItemGroup itemGroup, boolean fromDialog);
    } 

    ListView mGroupList;    
    ViewGroup switchDictionary;
    
    private TextView mTextCategoriesLabel;
    private TextView mTextChannelsLabel;
    
    boolean getForCurrentDocument;
    long docID;
    ItemGroupSelector itemGroupSelector;
    int initialDictionaryIdx;
	
	//--------------------------------------------------------------	
	public Dialog onCreate(final Context context, final DialogInterface.OnClickListener cancelClickListener, boolean getForCurrentDocument, 
							long docID, ItemGroupSelector itemGroupSelector, ItemGroup prevSelectedGroup)
    {
    	try
    	{
    		this.itemGroupSelector = itemGroupSelector;
    		//this.cancelClickListener = cancelClickListener;
    		this.getForCurrentDocument = getForCurrentDocument;
    		this.docID = docID;
    		
    		initialDictionaryIdx = itemGroupSelector.currentDictionaryIdx;

			//create dialog
    		String title = context.getResources().getString(R.string.form_title_selectItemGroup);
			Dialog dlg = super.onCreate(context, R.layout.doc_sale_select_group, R.id.docSaleSelectGroup, 
 										title, DIALOG_FLAG_HAVE_CANCEL_BUTTON | DIALOG_FLAG_NO_TITLE );			
    		
			super.setCancelClickListener(new DialogInterface.OnClickListener() 
			{					
				@Override public void onClick(DialogInterface dialog, int which) 
				{
					//restore back dictionary if not confirmed
					DocSaleSelectGroupDialog.this.itemGroupSelector.currentDictionaryIdx = initialDictionaryIdx; 
					
					if(cancelClickListener != null)
						cancelClickListener.onClick(dialog, which);
				}
			});		
			
			mGroupList = ((ListView) super.findViewById(R.id.docSaleSelectGroupList));
		    mTextCategoriesLabel = ((TextView) super.findViewById(R.id.textCategories));
		    mTextChannelsLabel = ((TextView) super.findViewById(R.id.textChannels));

			//switch between item dictionaries
	        switchDictionary = (ViewGroup) super.findViewById(R.id.switchDictionary);
	        if (itemGroupSelector.getCount() > 1)
	        {
		        switchDictionary.setOnClickListener( new ViewGroup.OnClickListener() 
				{			
					@Override public void onClick(View v) 
					{
						try
						{
							DocSaleSelectGroupDialog.this.itemGroupSelector.currentDictionaryIdx = 
												(DocSaleSelectGroupDialog.this.itemGroupSelector.currentDictionaryIdx == 0)?1:0;
							updateDictionaryLabels();
							
							fill(context, null);						
						}
						catch(Exception ex)
						{
							ErrorHandler.CatchError("Exception in switchDictionary.onClick", ex);			
						}
					}
				});
	        }
	        else
	        {
	        	switchDictionary.setVisibility(View.INVISIBLE);
	        }
	        
	        updateDictionaryLabels();
	
			fill(context, prevSelectedGroup);
			
			return dlg;
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in docSaleSelectGroupDialog.onCreate", ex);
		}
    	
    	return null;
    }
    //------------------------------------------------------------------------------------
    public void updateDictionaryLabels()
    {
    	boolean categories = (itemGroupSelector.currentDictionaryIdx == 0);
		mTextCategoriesLabel.setTextColor(categories ? 0xFF00FF00:0xFF000000);
		mTextChannelsLabel.setTextColor(categories ? 0xFF000000:0xFF00FF00);
    }
    
    //--------------------------------------------------------------
    /** ��������� ������ ������������ ����� ����������� �������� ����� �������*/
    public static class ItemGroupSelector
    {
    	Context context;
    	private ItemGroupDictionary[] dictionaries;
    	private int currentDictionaryIdx;
    	
    	ItemGroupDictionary getCurrentDictionary() { return dictionaries[currentDictionaryIdx]; }
    	private OnSelectGroupListener selectGroupListenerExternal = null;
    	
    	public ItemGroupSelector(Context context, OnSelectGroupListener selectGroupListener, SelectorType selectorType)
    	{
    		this.context = context;    		
    		this.selectGroupListenerExternal = selectGroupListener;
    	
    		if(selectorType == SelectorType.order)
    		{
    			currentDictionaryIdx = 1;
    			dictionaries = new ItemGroupDictionary[2];
    		
	    		dictionaries[0] = new ItemGroupDictionary(DICTIONARY_IDX_CATEGORIES, this.selectGroupListener, 1);
	    		dictionaries[1] = new ItemGroupDictionary(DICTIONARY_IDX_CHANNELS, this.selectGroupListener, 0);
    		}
    		else if(selectorType == SelectorType.closedOrder)
    		{
        		currentDictionaryIdx = 0;
        		dictionaries = new ItemGroupDictionary[1];    		
        		dictionaries[0] = new ItemGroupDictionary(DICTIONARY_IDX_CLOSED_ORDER, this.selectGroupListener, 0);
    		}    		
    		else if(selectorType == SelectorType.itemList)
    		{
        		currentDictionaryIdx = 0;
        		dictionaries = new ItemGroupDictionary[1];    		
        		dictionaries[0] = new ItemGroupDictionary(DICTIONARY_IDX_ITEM_LIST, this.selectGroupListener, -1);    			
    		}
    	}
    	
    	/* ������������� callback. ������ ��������� � �������� ������� callback*/
    	private	DocSaleSelectGroupDialog.OnSelectGroupListener selectGroupListener = new DocSaleSelectGroupDialog.OnSelectGroupListener() 
    	{					
    		@Override public void onGroupSelected(ItemGroup itemGroup, boolean fromDialog) 
    		{
    			try
    			{
    				TreeItem selectedItem = getCurrentDictionary().mItemGroups.get(itemGroup.id);
    				if(selectedItem.depth < getCurrentDictionary().depthOfSteps)
    				{
    					//substitute parent group with first child of this group
    					TreeItem newItemGroup = getCurrentDictionary().switchToNextItemGroup(itemGroup, true);
    					if(newItemGroup!=null)
    						itemGroup = (ItemGroup)newItemGroup.displayableItem;
    				}
    				
    				selectGroupListenerExternal.onGroupSelected(itemGroup, fromDialog);
    				
    			}
    			catch(Exception ex)
    			{
    		    	MessageBox.show(context, context.getResources().getString(R.string.form_title_docSale), context.getResources().getString(R.string.doc_sale_exceptionDisplay));
    				ErrorHandler.CatchError("Exception in DocSaleForm.onGroupSelected", ex);
    			}								
    		}
    	};
    	
    	public void switchToPrevItemGroup(ItemGroup currentGroup)
    	{
            ItemGroupDictionary currentDictionary = getCurrentDictionary();
            TreeItem treeItem = currentDictionary.switchToPrevItemGroup(currentGroup);
            ItemGroup itemGroup = (ItemGroup) treeItem.displayableItem;
    		
    		if(itemGroup!=null)
    			selectGroupListenerExternal.onGroupSelected(itemGroup, false);
    	}   	
    	
    	public void switchToNextItemGroup(ItemGroup currentGroup)
    	{
    		ItemGroup itemGroup = (ItemGroup) getCurrentDictionary().switchToNextItemGroup(currentGroup).displayableItem;
    		
    		if(itemGroup!=null)
    			selectGroupListenerExternal.onGroupSelected(itemGroup, false);
    	}
    	
    	public int getCount()
    	{
    		return dictionaries == null ? 0 : dictionaries.length;
    	}
    }   
    
    //--------------------------------------------------------------
    /** ������ ����� �������. ������������� ��������� ����� �������, � ����� ������ ��������� ��������� ����� �������� �������*/
    public static class ItemGroupDictionary
    {
    	TreeItems mItemGroups = new TreeItems();
    	int depthOfSteps = -1;  
    	
    	/* 
    	 * @param depthOfSteps -1 �������� ��� ��� ����� �����, ����� �������� ������� ���� ������ 0 - ������� ������ �� �������� ������
    	 */
    	public ItemGroupDictionary(int dictionaryIdx, OnSelectGroupListener selectGroupListener, int depthOfSteps)
    	{
    		this.depthOfSteps = depthOfSteps;
			DocSaleSelectGroupDialog.getDataFromDb(mItemGroups, selectGroupListener, dictionaryIdx);	
    	}
    	
    	public ItemGroup getDefaultGroup()
    	{
    		ItemGroup itemGroup = null;    		
            ArrayList<TreeItem> visibleItems = mItemGroups.getItems(depthOfSteps, true);
    		
            if(visibleItems.size()>0)
            	itemGroup = (ItemGroup)visibleItems.get(0).displayableItem;
    		
			return itemGroup;
    	}
    	
    	public ItemGroup getGroupParent(ItemGroup itemGroup)
    	{
    		TreeItem treeItem = mItemGroups.getItemTopParent(itemGroup.id, 0);
    		return treeItem==null ? null : (ItemGroup)treeItem.displayableItem;
    	}
    	
    	public void expandBranchContainingItem(ItemGroup itemGroup)
    	{
    		mItemGroups.expandBranchContainingItem(itemGroup.id);
    	}
    	
    	/*������� � ���������� ������ ������� � ������*/
    	public TreeItem switchToPrevItemGroup(ItemGroup currentGroup)
    	{
        	TreeItem itemGroup = null;        	
        	
            ArrayList<TreeItem> items = mItemGroups.getItems(-1, false);
            int index = DocSaleSelectGroupDialog.findGroupIndexByID(items, currentGroup.id);
            
            if(index!=-1)
            {
            	TreeItem currentItem = items.get(index);           	
            	
            	for(int i=index-1; i>=0; i--)
            	{
            		TreeItem item = items.get(i);
            		if(item.depth == depthOfSteps)
            		{
            			if(currentItem.depth>=depthOfSteps && depthOfSteps>0)
            			{
            				//������� ������� ��������� ������ ���� ��� ����� ������� �����. ��������� �������������� ��������
            				//���������, ��������� �� ��������� ������� � ��� �� ����� ��� ��������� �����            			
                			long curParentID = mItemGroups.getItemTopParentId(currentItem.id, depthOfSteps-1);
                			if(item.parentId == curParentID)
                			{
                				//������� ��������� � ��� �� �����, �������� ���
                    			itemGroup = item;
                    			break;              				
                			}
            				
            			}
            			
            			//���� ������� ��������� � ������� ������, �������� ���
            			if(item.depth == 0)
            			{
                			itemGroup = item;
                			break;              				
            			}
            			
            			//��� ����� ������������� �� ���������� �����. ������� ������������ �� ������ ������� � �����
            			//������� ������ ������� �� ����� ��������
            			TreeItem itemParent = mItemGroups.get(item.parentId);
            			if(itemParent.children.size()!=0)
            			{
            				itemGroup = itemParent.children.get(0);
            				break;
            			}
            		}
            	}
            }

			return itemGroup;
    	}
    	
    	/*������� � ��������� ������ ������� � ������*/
    	public TreeItem switchToNextItemGroup(ItemGroup currentGroup)
    	{
    		return switchToNextItemGroup(currentGroup, false);
    	}
    	
        public TreeItem switchToNextItemGroup(ItemGroup currentGroup, boolean insideBranch)
        {       	
        	TreeItem itemGroup = null;
        	
            ArrayList<TreeItem> items = mItemGroups.getItems(-1, false);

            int index = DocSaleSelectGroupDialog.findGroupIndexByID(items, currentGroup.id);
            if(index!=-1)
            {
            	TreeItem currentItem = items.get(index);
            	for(int i=index+1; i<items.size(); i++)
            	{
            		TreeItem item = items.get(i);
            		if(item.depth == depthOfSteps)
            		{
            			if(insideBranch)
            			{
            				//check if item belongs to another branch
            				long id = mItemGroups.getItemTopParentId(item.id, currentItem.depth);
            				if(id!=currentItem.id)
            					break;
            			}
            			itemGroup = item;
            			break;
            		}
            	}
            }

			return itemGroup;
        }        
    }    
    
	//--------------------------------------------------------------
    /** ������ �������*/
    public static class ItemGroup
    {
    	public long id;
        public String name;
        public String docGridCondition;
        public boolean haveDocGridCondition;
        public boolean enabled;
        public int groupType;
        public boolean isMandatory;
        public boolean isPredictOrder;
        public Long parentId;        
        public boolean visited;
        public long flags;
        
        //stats
        public long orders;
        public double sum;
        public double MSU;
        public boolean haveStats;
        
        public ItemGroup(long id, String name, boolean haveDocGridCondition, String docGridCondition, 
   						 boolean enabled, int groupType, boolean isMandatory, boolean isPredictOrder, 
   						 Long parentId, long flags, long orders, double sum, double MSU, boolean haveStats) 
        {
            this.name = name;
            this.id = id;
            this.haveDocGridCondition = haveDocGridCondition;
            this.docGridCondition = docGridCondition;
            this.enabled = enabled;
            this.groupType = groupType;
            this.isMandatory = isMandatory;
            this.isPredictOrder = isPredictOrder;
            this.parentId = parentId;
            this.flags = flags;
            
            this.orders = orders;
            this.sum = sum;
            this.MSU = MSU;
            this.haveStats = haveStats;
            this.visited = false;
        }
        
        public boolean isChild()
        {
        	return parentId!=null;
        }
        
        public boolean isAlwaysEnabled()
        {
        	return (flags&GROUP_FLAG_ALWAYS_ENABLED)>0;
        }
    }

	//--------------------------------------------------------------
    /** Wrapper ��� ������ ������ ������, ��������� ���������� ���������� � ������ ������ �� ��������� UI*/
    public static class ItemGroupObject extends ItemGroup implements IGroupItem, IChildItem 
    {
    	private OnSelectGroupListener selectGroupListener = null;
    	
    	public ItemGroupObject(long id, String name, boolean haveDocGridCondition, String docGridCondition, 
    								boolean enabled, int groupType, boolean isMandatory, boolean isPredictOrder,
    								Long parentId, long flags, OnSelectGroupListener selectGroupListener, 
    								long orders, double sum, double MSU, boolean haveStats)
    	{
    		super(id, name, haveDocGridCondition, docGridCondition, enabled, groupType, isMandatory, isPredictOrder, parentId, flags, orders, sum, MSU, haveStats);    		
    		this.selectGroupListener = selectGroupListener;
    	}
    	
        public Long getID() { return id; }
        
        public boolean isEnabled() { return enabled; }
        
    	public void fillView(View view, int viewId, boolean isExpanded)
    	{
    		if(viewId == R.id.textGroupName && view instanceof TextView)
    		{
    			((TextView) view).setText(name);
    			
    			if(enabled)
    				((TextView) view).setTextColor(0xFF000000);
    			else
    				((TextView) view).setTextColor(0xFFAAAAAA);    			
    			
    		}
    		
    		/*if(viewId == R.id.textGroupName || viewId == R.id.textPadding || viewId == R.id.itemLayout)
    		{
    			if(groupType == GROUP_TYPE_DOC_STEP)
    				view.setBackgroundColor(Color.rgb(241, 240, 156));
    			else
    				view.setBackgroundColor(0xFFFFFFFF);    			
    		}*/
    		
    		if(viewId == R.id.textGroupName && view instanceof TextView)
    		{
    			if(groupType == GROUP_TYPE_DOC_STEP)
    				((TextView) view).setTypeface(Typeface.DEFAULT_BOLD);
    			else
    				((TextView) view).setTypeface(Typeface.DEFAULT);
    		}
			
			//if(viewId == R.id.textGroupName)
    		if(viewId == R.id.itemLayout)
			{
				view.setTag(this);
	            view.setOnClickListener(
			            new View.OnClickListener() 
			    		{	
			    			@Override public void onClick(View v) 
			    			{
			    				ItemGroup itemGroup = (ItemGroup) v.getTag();
			    				
			    				if(itemGroup.enabled && selectGroupListener!=null)
			    				{
			    					selectGroupListener.onGroupSelected(itemGroup, true);
			    				}
			    			}
			    		});
			}
			
			if(viewId == R.id.textOrders && view instanceof TextView)
			{
				if(haveStats)
					((TextView) view).setText(String.format("%d", orders));
			}
			else if(viewId == R.id.textSum && view instanceof TextView)
			{
				if(haveStats)
					((TextView) view).setText(Convert.moneyToString(sum));
			}
			else if(viewId == R.id.textMSU && view instanceof TextView)
			{
				if(haveStats)
					((TextView) view).setText(Convert.msuToString(MSU));
			}
			else if(viewId == R.id.statsLayout)
			{
				view.setVisibility(haveStats? View.VISIBLE:View.GONE);
			}
    	}
    }
    //--------------------------------------------------------------
    /*������ �� ���� ������ ���������� � ������ ����� �������*/
    public static void getDataFromDb(TreeItems groups,
    								OnSelectGroupListener selectGroupListener, 
    								//boolean ignoreDynamicGroups, 
    								int dictionaryIdx)
    {
		//create array of item groups
		String sql =" SELECT g.ItemGroupID, g.ParentGroupID, g.ItemGroupName, g.DocGridCondition, g.GroupType, g.IsMandatory, g.Flags, " +
					" 		parent.GroupType AS ParentGroupType, g.IsPredictOrder " +
					" FROM ItemGroups g "  +
					 	" LEFT JOIN ItemGroups parent ON g.ParentGroupID = parent.ItemGroupID and parent.Flags&" + GROUP_FLAG_HIDDEN + " = 0" +
					" WHERE g.SetID = " + dictionaryIdx +
					"		and g.Flags&" + GROUP_FLAG_HIDDEN + " = 0" +					
					" ORDER BY g.SortID";

		Cursor groupsCursor = Db.getInstance().selectSQL(sql);
		
		if(groupsCursor==null)
			return;
		
		int idxItemGroupID = groupsCursor.getColumnIndex("ItemGroupID");
		int idxParentGroupID = groupsCursor.getColumnIndex("ParentGroupID");
		int idxItemGroupName = groupsCursor.getColumnIndex("ItemGroupName");
		int idxItemGroupDocGridCondition = groupsCursor.getColumnIndex("DocGridCondition");
		int idxItemGroupType = groupsCursor.getColumnIndex("GroupType");
		int idxParentGroupType = groupsCursor.getColumnIndex("ParentGroupType");
		int idxMandatory = groupsCursor.getColumnIndex("IsMandatory");
		int idxPredictOrder = groupsCursor.getColumnIndex("IsPredictOrder");
		int idxFlags = groupsCursor.getColumnIndex("Flags");
		
		for(int i = 0; i < groupsCursor.getCount(); i++)
		{	
			groupsCursor.moveToPosition(i);
			
			long itemGroupID = groupsCursor.getInt(idxItemGroupID);			
			String itemGroupName = groupsCursor.getString(idxItemGroupName);			
			boolean haveDocGridCondition = !groupsCursor.isNull(idxItemGroupDocGridCondition);
			String docGridCondition = haveDocGridCondition ? groupsCursor.getString(idxItemGroupDocGridCondition):"";
			int itemGroupType = groupsCursor.getInt(idxItemGroupType);
			int parentGroupType = groupsCursor.getInt(idxParentGroupType);
			boolean isMandatory = groupsCursor.getString(idxMandatory).equals("true") /*&& !ignoreMandatoryFlag*/ ? true:false;
			Long parentGroupID = groupsCursor.isNull(idxParentGroupID) ? null:groupsCursor.getLong(idxParentGroupID); 
			boolean isPredictOrder = groupsCursor.getLong(idxPredictOrder)>0;
			long flags = groupsCursor.getLong(idxFlags);
			
//			//skip dynamic groups if requested
//			if(ignoreDynamicGroups && haveDocGridCondition)
//				continue;
			
			//
			//look for group stats
			//
			boolean haveStats = false;
			long orders = 0;
			double sum = 0;
			double MSU = 0;
			
			ItemGroupObject itemGroup = new ItemGroupObject( itemGroupID, itemGroupName, haveDocGridCondition, 
													docGridCondition, true, itemGroupType, isMandatory, isPredictOrder, 
													parentGroupID, flags, selectGroupListener, orders, sum, MSU, haveStats);

			groups.add(new TreeItem(itemGroupID, parentGroupID, false, isMandatory, itemGroup));
 
		}//for
		
		if(groupsCursor!=null)
			groupsCursor.close();    	
    }
    //--------------------------------------------------------------
    /*public static TreeItem findGroupByID(ArrayList<TreeItem> groups, long groupID)
    {
    	int idx = findGroupIndexByID(groups, groupID);
    	if(idx!=-1)
    		return groups.get(idx);
    	
    	return null;
    }*/

    //--------------------------------------------------------------
    /* ����� ������ ������ � ������ �� ��������������*/
    public static int findGroupIndexByID(ArrayList<TreeItem> groups, long groupID)
    {
    	int groupIndex = -1;
    	for(int i=0; i<groups.size(); i++)
    	{
    		TreeItem itemGroup = groups.get(i);
    		if(itemGroup.id == groupID)
    		{
    			groupIndex = i;
    			break;
    		}
    	}
    	
    	return groupIndex;
    }

    //--------------------------------------------------------------
    /*private void addDocumentStatsToGroup(   Map<Long, Long > docOrders,
    										Map<Long, Double > docSums,
    										Map<Long, Double > docMSUs,
    										ItemGroup itemGroup)
    {
    	long itemGroupID = itemGroup.id;    
    	itemGroup.haveStats = false;

		if(	docOrders.containsKey(itemGroupID))
		{
			itemGroup.orders = docOrders.get(itemGroupID);
			itemGroup.haveStats = true;
		}
		
		if(docSums.containsKey(itemGroupID))
		{
			itemGroup.sum =  docSums.get(itemGroupID);
			itemGroup.haveStats = true;
		}
		
		if(docMSUs.containsKey(itemGroupID))
		{
			itemGroup.MSU = docMSUs.get(itemGroupID);
			itemGroup.haveStats = true;
		}
    }    										
    
    //--------------------------------------------------------------    
    private void addDocumentStats(long docID, ItemGroupDictionary dictionary)
    {
    	//
    	// calculate group statistics to use later
    	//
    	Map<Long, Long > docOrders = new HashMap< Long, Long > ();
    	Map<Long, Double > docSums = new HashMap< Long, Double > ();
    	Map<Long, Double > docMSUs = new HashMap< Long, Double > ();
    	
   		PlansForm.getTodayStats(docOrders, docSums, docMSUs, false, 0, true, docID);

		ArrayList<IGroupItem> parentGroups = dictionary.mItemGroups;
		
		for(int i=0; i<parentGroups.size(); i++)
		{
			ItemGroup itemGroup = (ItemGroup) parentGroups.get(i);
			addDocumentStatsToGroup( docOrders, docSums, docMSUs, itemGroup);

			ArrayList<IChildItem> values = dictionary.mChildGroups.get(itemGroup.id);
        	if(values!=null)
        	{
	        	for(int j=0; j<values.size(); j++)
	        	{
	        		ItemGroup childGroup = (ItemGroup) values.get(j);
	        		addDocumentStatsToGroup( docOrders, docSums, docMSUs, childGroup);
	        	}
        	}
			
		}    		
    }   */

    //--------------------------------------------------------------
    private void disableSteps(ArrayList<TreeItem> treeItems)
    {
		int lastVisitedIndex = -1;
		boolean enableNonSteps = true;
		
		for(int i=0; i<treeItems.size(); i++)
		{
			ItemGroup itemGroup = (ItemGroup) treeItems.get(i).displayableItem;
			
			boolean enabled = true;
			
			if(!itemGroup.isAlwaysEnabled())
			{
        		if(itemGroup.visited || (i-lastVisitedIndex)<=1)
        		{
        			if(itemGroup.visited)
        				lastVisitedIndex = i;

    				//if non-step is enabled switch flag enableNonSteps to true
    				//if(itemGroup.groupType != GROUP_TYPE_DOC_STEP &&  itemGroup.groupType != GROUP_TYPE_DOC_STEP1)
        			if(!itemGroup.isMandatory)
    					enableNonSteps = true;        			
        		}
        		//else if(itemGroup.groupType == GROUP_TYPE_DOC_STEP || itemGroup.groupType == GROUP_TYPE_DOC_STEP1 || enableNonSteps==false )
        		else if(itemGroup.isMandatory || enableNonSteps==false )
				{        					
					enabled = false;
					enableNonSteps = false;
				}
			}
    		
    		itemGroup.enabled = enabled;
		}	
    	
    }
    
    //--------------------------------------------------------------
    private void fill(Context context, ItemGroup prevSelectedGroup)
    {
    	// ������� ����������� ������ ������� � ����������� �� ����������� �����

    	int depthOfSteps = itemGroupSelector.getCurrentDictionary().depthOfSteps;
    	if(depthOfSteps == 0)
    	{
        	// ��������� ����� �������� ������
            ArrayList<TreeItem> topItems = itemGroupSelector.getCurrentDictionary().mItemGroups.getItems(0, true);
            disableSteps(topItems);
    	}
    	else if(depthOfSteps > 0)
    	{
    		//��������� ����� ������ �������
    		ArrayList<TreeItem> parentItems = itemGroupSelector.getCurrentDictionary().mItemGroups.getItems(depthOfSteps-1, true);
    		for(int i=0; i<parentItems.size(); i++)
    		{
    			ArrayList<TreeItem> childItems = parentItems.get(i).children;
    			if(childItems!=null)
    				disableSteps(childItems);		
    		}
    	}
    	
    	/*if(getForCurrentDocument)
    		addDocumentStats(docID, itemGroupSelector.getCurrentDictionary());*/
    	
    	if(prevSelectedGroup!=null)
    	{
    		itemGroupSelector.getCurrentDictionary().expandBranchContainingItem(prevSelectedGroup);
    	}
		
		//
		//create adapter
		//
		
		int[] rowViewIds = new int[] { R.id.textGroupName, R.id.itemLayout };
    	HierarchicalListAdapter itemGroupsAdapter = new HierarchicalListAdapter(context, itemGroupSelector.getCurrentDictionary().mItemGroups,
									R.layout.item_group_list_item_hierarchical, rowViewIds);		
		
		mGroupList.setAdapter(itemGroupsAdapter);
    }
}


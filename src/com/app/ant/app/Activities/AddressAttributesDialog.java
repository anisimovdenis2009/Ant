package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 14:35
 * To change this template use File | Settings | File Templates.
 */
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import com.app.ant.R;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IChildItem;
import com.app.ant.app.Activities.ExpandableAdapterForArray.IGroupItem;
import com.app.ant.app.Activities.HierarchicalListAdapter.TreeItem;
import com.app.ant.app.Activities.HierarchicalListAdapter.TreeItems;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/** Всплывающий диалог выбора атрибутов адреса в виде иерархического списка с чекбоксами*/
public class AddressAttributesDialog extends DialogBase
{
	/** Интерфейс для обратного вызова - возвращает выбранные аттрибуты*/
    public interface OnSelectAttributes
    {
        abstract void onAttributesSelected(Attributes attributes);
    }

	public static enum AttributeType { addressAttribute, additionalFilterDebt };

	TreeItems items;
    ListView mItemList;
    HierarchicalListAdapter attributesAdapter;
    boolean editAddrAttributes;

    public static class Attributes
    {
    	TreeItems addressAttributeIds = new TreeItems();
    	Collection<AttributeType> additionalFilters = new HashSet<AttributeType>();
    }

	//--------------------------------------------------------------
	public Dialog onCreate(final Context context, Attributes initialAttributes,
							final OnSelectAttributes selectAttributesListener,
							final DialogInterface.OnClickListener cancelClickListener,
							boolean editAddrAttributes)
    {
    	try
    	{
    		this.editAddrAttributes = editAddrAttributes;

			//create dialog
    		String title = context.getResources().getString( editAddrAttributes ? R.string.addr_type_dlg_title : R.string.form_title_filtersSelect );
			Dialog dlg = super.onCreate(context, R.layout.address_attributes_dialog, R.id.selectAttributes,
 										title, DIALOG_FLAG_HAVE_OK_BUTTON | DIALOG_FLAG_HAVE_CANCEL_BUTTON | DIALOG_FLAG_NO_TITLE );

			TextView textDialogTitle = (TextView) super.findViewById(R.id.textDialogTitle);
			textDialogTitle.setText(title);

			super.setCancelClickListener(cancelClickListener);

			super.setOkClickListener(new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					Attributes attributes = new Attributes();

					if(items!=null)
					{
						Iterator<TreeItem> it = items.iterator();
				        while (it.hasNext())
				        {
				        	TreeItem item = it.next();
			        		AddressAttributeDisplayable attr = (AddressAttributeDisplayable)item.displayableItem;
			        		if(attr.checkable && attr.checked)
			        		{
			        			if(attr.attrType == AttributeType.addressAttribute)
			        			{
			        				//здесь многоуровневое дерево преобразуется в двухуровневое (для облегчения использования результатов)

			        				//добавляем родительский элемент если его нет
			        				//элементы верхнего уровня с parentId = null добавляем в отдельную ветку с parentId = -1
			        				Long parentId = (item.parentId==null ? -1 : item.parentId);
			        				if(!attributes.addressAttributeIds.containsKey(parentId))
			        					attributes.addressAttributeIds.add(new TreeItem(parentId, null, false, false, null)); //parent = null т.к. верхний уровень

			        				attributes.addressAttributeIds.add(new TreeItem(item.id, parentId, false, false, null));
			        			}
			        			else
			        				attributes.additionalFilters.add(attr.attrType);
			        		}
				    	}
					}

					if(selectAttributesListener!=null)
						selectAttributesListener.onAttributesSelected(attributes);
				}
			});

			View buttonDeselectAll = super.findViewById(R.id.buttonDeselectAll);
			buttonDeselectAll.setVisibility(editAddrAttributes? View.GONE:View.VISIBLE);
			if(!editAddrAttributes)
			{
				buttonDeselectAll.setOnClickListener(
				            new View.OnClickListener()
				    		{
				    			@Override public void onClick(View v)
				    			{
				    		    	try
				    		    	{
				    		    		if(items!=null)
				    		    		{
				    						Iterator<TreeItem> it = items.iterator();
				    				        while (it.hasNext())
				    				        {
				    				        	TreeItem item = it.next();
				    			        		AddressAttributeDisplayable attr = (AddressAttributeDisplayable)item.displayableItem;
				    			        		attr.checked = false;
				    				    	}

				    				        if(attributesAdapter!=null)
				    				        	attributesAdapter.notifyDataSetChanged();
				    		    		}
				    		    	}
				    		    	catch(Exception ex)
				    		    	{
				    		    		ErrorHandler.CatchError("Exception in AddressAttributesDialog.buttonDeselectAll.onClick", ex);
				    		    	}
				    			}
				    		});
			}

			mItemList = (ListView) super.findViewById(R.id.itemList);

			fill(context, initialAttributes);

			return dlg;
    	}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in AddressAttributesDialog.onCreate", ex);
		}

    	return null;
    }

	//--------------------------------------------------------------
    /** Аттрибут адреса или дополнительный фильтр*/
    public static class AddressAttribute
    {
    	public long id;
    	public AttributeType attrType;
        public String name;
        public Long parentId;
        public boolean checkable;
        public boolean checked;
        public boolean displayAddrCount;
        public int addrCount;

        public AddressAttribute(long id, AttributeType attrType, String name, Long parentId,
        						boolean checkable, boolean checked, boolean displayAddrCount, int addrCount)
        {
            this.name = name;
            this.id = id;
            this.attrType = attrType;
            this.parentId = parentId;
            this.checkable = checkable;
            this.checked = checked;
            this.displayAddrCount = displayAddrCount;
            this.addrCount = addrCount;
        }
    }

	//--------------------------------------------------------------
    /** Wrapper для класса аттрибута адреса, способный отображать информацию о аттрибуте на элементах UI*/
    public static class AddressAttributeDisplayable extends AddressAttribute implements IGroupItem, IChildItem
    {
    	public AddressAttributeDisplayable(long id, AttributeType attrType, String name, Long parentId,
    							boolean checkable, boolean checked, boolean displayAddrCount, int addrCount)
    	{
    		super(id, attrType, name, parentId, checkable, checked, displayAddrCount, addrCount);
    	}

        public Long getID() { return id; }

        public boolean isEnabled() { return true; }

    	public void fillView(View view, int viewId, boolean isExpanded)
    	{
    		if(viewId == R.id.textAttributeName && view instanceof TextView)
    		{
    			String strCount = displayAddrCount ? String.format(" (%d)", addrCount):"";
    			((TextView) view).setText(name + strCount);
    		}
    		else if(viewId == R.id.chkBoxEnable && view instanceof CheckBox)
    		{
    			CheckBox chkBox = (CheckBox)view;

    			view.setVisibility(checkable? View.VISIBLE:View.GONE);
    			chkBox.setOnCheckedChangeListener(null);
    			chkBox.setChecked(checked);

    			chkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						checked = isChecked;
					}
				});
    		}
    	}
    }
    //--------------------------------------------------------------
    /*Читает из базы данных информацию о дереве аттрибутов адреса*/
    public static void getDataFromDb(Context context, TreeItems attrTree, Attributes initialAttributes, boolean editAddrAttributes)
    {
    	boolean expandAtStart = false;
    	boolean displayAddrCount = !editAddrAttributes;
    	long maxId = 0;

    	String addrSelect = "";
    	String addrJoin = "";

    	if(editAddrAttributes)
    	{
    		addrSelect = " , CASE WHEN aa.AttrID IS NULL THEN '0' ELSE '1' END AS AttrEnabled ";
    		addrJoin = String.format( " LEFT JOIN AddressAttributes aa ON attrs.AttrID=aa.AttrID AND aa.State != '%s' AND aa.AddrID = %d ",
    											Q.RECORD_STATE_DELETED, AntContext.getInstance().getAddress().addrID);
    	}

    	String addrCountSelect = "";
    	if(displayAddrCount)
    	{
    		addrCountSelect =
    			", (SELECT count(*) FROM Addresses a WHERE EXISTS " +
    			" (SELECT aaFilter.AddrID FROM AddressAttributes aaFilter " +
    			" WHERE aaFilter.AddrID = a.AddrID AND aaFilter.AttrID = attrs.AttrID ) ) AS AddrCount ";
    	}

    	String sql = " SELECT attrs.AttrID, attrs.ParentID, attrs.AttrDescription as AttrName " + addrSelect + addrCountSelect +
    				 " FROM Attributes attrs " +
    				 addrJoin;

		Cursor cursor = Db.getInstance().selectSQL(sql);

		if(cursor==null)
			return;

		int idxAttrID = cursor.getColumnIndex("AttrID");
		int idxParentID = cursor.getColumnIndex("ParentID");
		int idxAttrName = cursor.getColumnIndex("AttrName");
		int idxAttrEnabled = editAddrAttributes ? cursor.getColumnIndex("AttrEnabled"):-1;
		int idxAddrCount = displayAddrCount ? cursor.getColumnIndex("AddrCount"):-1;

		for(int i = 0; i < cursor.getCount(); i++)
		{
			cursor.moveToPosition(i);

			Long attrID = cursor.getLong(idxAttrID);
			Long parentID = cursor.isNull(idxParentID)? null:cursor.getLong(idxParentID);
			String attrName = cursor.getString(idxAttrName);
			boolean checked = editAddrAttributes ? cursor.getInt(idxAttrEnabled)>0 : false;
			int addrCount = displayAddrCount ? cursor.getInt(idxAddrCount):0;

			AddressAttributeDisplayable attr = new AddressAttributeDisplayable(attrID, AttributeType.addressAttribute, attrName, parentID,
																					false, checked, displayAddrCount, addrCount);
			attrTree.add(new TreeItem(attrID, parentID, expandAtStart, false, attr));

			maxId = Math.max(attrID, maxId);
		}//for

		if(cursor!=null)
			cursor.close();

		//
		// проходим по массиву элементов дерева, включаем чекбоксы для элементов нижнего уровня (не имеющих детей)
		//
		Iterator<TreeItem> it = attrTree.iterator();
        while (it.hasNext())
        {
        	TreeItem item = it.next();

        	if(item.children.isEmpty())
        	{
        		AddressAttributeDisplayable attr = (AddressAttributeDisplayable)item.displayableItem;
        		attr.checkable = true;

        		if(!editAddrAttributes && initialAttributes!=null && initialAttributes.addressAttributeIds.containsKey(attr.id))
        			attr.checked = true;

        		if(attr.checked)
        			attrTree.expandBranchContainingItem(item.id); // для включенных элементов следует раскрыть родительские ветки

        		if(attr.displayAddrCount)
        		{
        			//добавляем значение addrCount родительским элементам
        			TreeItem parentItem = attrTree.getParentItem(item);
        			int valueToAdd = attr.addrCount;

        			while(parentItem!=null)
        			{
        				AddressAttributeDisplayable parentAttr = (AddressAttributeDisplayable)parentItem.displayableItem;
        				if(parentAttr!=null)
        					parentAttr.addrCount += valueToAdd;

        				parentItem = attrTree.getParentItem(parentItem);
        			}
        		}
        	}
    	}

		//
		// добавляем доп. фильтры
		//

        if(!editAddrAttributes)
        {
			maxId++;
			boolean checked = (initialAttributes!=null) ? initialAttributes.additionalFilters.contains(AttributeType.additionalFilterDebt):false;

			//calculate count of clients having saldo>0
			long defaultDirectionID = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_DEFAULT_DIRECTION_ID, 0);
			String sqlCount = String.format(" SELECT count(*) " +
											" FROM Clients c " +
											" WHERE (select round(max(Saldo),2) from ClientSaldo where ClientID = c.ClientID and SaldoTypeID=2 and DirectionID=%d ) >0 ",
											defaultDirectionID);
			long clientCount = Db.getInstance().getDataLongValue(sqlCount,0);

			AddressAttributeDisplayable attr = new AddressAttributeDisplayable(maxId, AttributeType.additionalFilterDebt,
																				context.getResources().getString(R.string.filter_debt), null,
																				true, checked, true, (int)clientCount);
			attrTree.add(new TreeItem(maxId, null, expandAtStart, false, attr));
			maxId++;
        }
    }

    //--------------------------------------------------------------
    private void fill(Context context, Attributes initialFilters)
    {
    	items = new TreeItems();
    	getDataFromDb(context, items, initialFilters, editAddrAttributes);

		//
		//create adapter
		//

		int[] rowViewIds = new int[] { R.id.textAttributeName, R.id.itemLayout, R.id.chkBoxEnable };
    	attributesAdapter = new HierarchicalListAdapter(context, items,
														R.layout.address_attributes_list_item_hierarchical, rowViewIds);

		mItemList.setAdapter(attributesAdapter);
    }
}
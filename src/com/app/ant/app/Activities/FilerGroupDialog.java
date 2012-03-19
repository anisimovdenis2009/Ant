package com.app.ant.app.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Filter;

import java.util.HashMap;
import java.util.Map;

public class FilerGroupDialog {

    private String[] names;
    private String[] docGridConditions;
    private boolean[] checked;
    boolean booking = false;
    boolean listFlag = false;

    public interface FilterGroupSelectListener {
        abstract void onDocSaleSelected(String docGridCondition, boolean booking, HashMap<Integer, Filter> groupsChecked, boolean listFlag);
    }

    public FilerGroupDialog(boolean booking) {
        this.booking = booking;
    }

    public FilerGroupDialog(boolean listFlag, boolean booking) {
        this.listFlag = listFlag;
        this.booking = booking;
    }

    public void show(final Context context, final FilterGroupSelectListener resultListener, final HashMap<Integer, Filter> groupsChecked) {
        int k = 0;
        int count = groupsChecked.size();
        names = new String[count];
        docGridConditions = new String[count];
        checked = new boolean[count];
        for (Map.Entry item : groupsChecked.entrySet()) {
            Filter value = (Filter) item.getValue();
            names[k] = value.getName();
            docGridConditions[k] = value.getDocGridCondition();
            if (booking)
                checked[k] = value.isBooking();
            else
                checked[k] = value.getChecked();
            k++;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.doc_sale_filter_dialog)
                .setMultiChoiceItems(
                        names,
                        checked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
                                checked[whichButton] = isChecked;
                                if (booking)
                                    groupsChecked.get(whichButton).setBooking(isChecked);
                                else
                                    groupsChecked.get(whichButton).setChecked(isChecked);
                            }
                        })
                .setPositiveButton(R.string.dialog_base_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String result = "";
                        //boolean multiCheck = false;

                        int j = 0;
                        for (boolean check : checked) {
                            if (check) {
                                j++;
                            }
                        }
                        if (resultListener != null && (j > 0)) {
                            int b = 0;
                            for (int i = 0; i < checked.length; i++)
                                if (checked[i]) {
                                    if (b == 0)
                                        result = docGridConditions[i];
                                    else if (listFlag)
                                        result = result + " or " + docGridConditions[i];
                                    else
                                        result = result + " , " + docGridConditions[i];
                                    b++;
                                }
                            if (listFlag)
                                result = " and (" + result + ")";
                            else
                                result = " and it.ItemTypeID in (" + result + ")  ";
                            resultListener.onDocSaleSelected(result, booking, groupsChecked, listFlag);
                        } else {
                            resultListener.onDocSaleSelected(result, booking, groupsChecked, listFlag);
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_base_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create();
        alertDialog.show();
    }
}


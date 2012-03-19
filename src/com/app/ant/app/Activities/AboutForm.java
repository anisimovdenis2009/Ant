package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 14:35
 * To change this template use File | Settings | File Templates.
 */
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;

import com.app.ant.R;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.Api;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;

/** Форма отображения информации о продукте. Выводит HTML-текст с помощью контрола WebView*/

public class AboutForm extends Activity
{

	private WebView webView;

	/** Инициализация формы	 */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	try
    	{
	        super.onCreate(savedInstanceState);

	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.about);

	        webView = (WebView) findViewById(R.id.about_webView);
	        webView.setInitialScale(120);

	        TextView tvVersion = (TextView)findViewById(R.id.about_version);
	        TextView tvDeviceId = (TextView)findViewById(R.id.about_device_id);
	        TextView tvSalerName = (TextView)findViewById(R.id.about_saler_name);

	        tvVersion.setText(getResources().getString(R.string.about_version) + Api.getVersionName() + ".");
	        tvDeviceId.setText(getResources().getString(R.string.about_device_id) + Api.getDeviceID() + ".");
	        tvSalerName.setText(getResources().getString(R.string.about_saler_name) + Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_SALER_NAME, "") + ".");

	        String sql = "select VersionDescr from Versions order by VersionID desc";

	        StringBuilder sb = new StringBuilder();

	        Cursor cursor = Db.getInstance().selectSQL(sql);

			if(cursor != null && cursor.getCount() > 0)
			{
				int idx = cursor.getColumnIndex("VersionDescr");

				for(int i = 0; i < cursor.getCount(); i++)
				{
					cursor.moveToPosition(i);
					if (!cursor.isNull(idx))
					{
						String versionDescr = cursor.getString(idx);
						sb.append(versionDescr);
					}
				}
			}

			String header = "<h2>" + getResources().getString(R.string.about_history_of_changes) + "</h2>";
			String html = header + sb.toString();

//			String file = "file:///sdcard/data/data/com.app.ant/files/products1.jpg";
//			String img = "<img src=\"" + file +  "\" style=\"margin: 5px; float: left; width: 80px; height: 110px;\" />";
//
//			String href = "<a style=\"margin:5;\" href=\"" + img + "\" >" + img + " </a>";

	        String summary = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><html><head></head><body>" + html + "</body></html>";
	        webView.loadDataWithBaseURL("file:///sdcard/data/data/com.app.ant/", summary, "text/html", "utf-8", null);
	        //zoom
	        webView.getSettings().setSupportZoom(true);
	        webView.getSettings().setBuiltInZoomControls(true);
    	}
    	catch(Exception ex)
    	{
    		ErrorHandler.CatchError("AboutForm.onCreate", ex);
    	}
    }

    /** Очистка ресурсов */
    @Override public void onDestroy()
    {
    	try
    	{
    		webView.clearCache(true);
    		webView.clearView();
    		super.onDestroy();
    	}
    	catch(Exception ex)
    	{
    		ErrorHandler.CatchError("AboutForm.onDestroy", ex);
    	}

    }
}

package com.app.ant.app.Activities;

import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;


public class MessageProactiveForm extends AntActivity 
{
	boolean fromVisit;
	private WebView webView;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {           
    	try
    	{
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        overridePendingTransition(0,0);
	        getWindow().requestFeature(Window.FEATURE_PROGRESS);
	        setContentView(R.layout.message_proactive);
	        	        
	        //check form params
	        Bundle params = getIntent().getExtras();	        
			if(params!=null && params.containsKey(Document.PARAM_NAME_FROM_VISIT))
	        {
	        	fromVisit = params.getBoolean(Document.PARAM_NAME_FROM_VISIT); 
	        }
	        
	        webView = (WebView) findViewById(R.id.webView);
	        webView.setInitialScale(120);
	        
			if(fromVisit)
			{
				ImageButton buttonNextStep = (ImageButton) findViewById(R.id.buttonNextStep);
				buttonNextStep.setVisibility(View.VISIBLE);
		        buttonNextStep.setOnClickListener( new View.OnClickListener() 
				{			
					@Override public void onClick(View v) 
					{	 
				    	try
				    	{
				    		AntContext.getInstance().getTabController().onNextStepPressed(MessageProactiveForm.this);
				    	}
						catch(Exception ex)
						{
							ErrorHandler.CatchError("Exception in MessageProactiveForm.buttonNextStep.onClick", ex);
						}					
					}
				});			
			}			

	        webView.getSettings().setJavaScriptEnabled(true);

	        final Activity activity = this;
	        webView.setWebChromeClient(new WebChromeClient() {
	          public void onProgressChanged(WebView view, int progress) {
	            // Activities and WebViews measure progress with different scales.
	            // The progress meter will automatically disappear when we reach 100%
	            activity.setProgress(progress * 1000);
	          }
	        });
			webView.setWebViewClient(new WebViewClient()
			{
				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
				{
					Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
				}
			});
	        
	        webView.setWebViewClient(new WebViewClient() {  
	            @Override  
	            public void onPageFinished(WebView view, String url)  
	            {  
	                //webView.loadUrl("javascript:(function() { document.getElementsByTagName('body')[0].style.color = 'red'; })()");  
	            }  
	        });	       
	        	
	        long addrID = AntContext.getInstance().getAddrID();

	        String sql = " SELECT Body, Name " +
	        			 " FROM MessagesProactive mp" +
	        			 	" INNER JOIN AddrChannels ac ON mp.ChannelID=ac.ChannelID AND ac.AddrID = " + addrID +
	        			 	" INNER JOIN Channels c ON ac.ChannelID=c.ChannelID AND c.ChannelTypeID = 3 ";
	        String[] messages1 = Db.getInstance().selectRowValues(sql);
	        
	        sql = " SELECT Body, Name " +
			 			" FROM MessagesProactive " +
			 			" WHERE ChannelID IS NULL ";
	        String[] messages2 = Db.getInstance().selectRowValues(sql);

	        boolean displayMess1 = (messages1 != null && !Convert.isNullOrBlank(messages1[0]));
	        boolean displayMess2 = (messages2 != null && !Convert.isNullOrBlank(messages2[0]));
	        
	        String html = 
	        		(displayMess1? 
						        	"<a style=\"margin:5;\" href=\"#\"  onclick=\"javascript:(function() { " +
									" document.getElementById('a1').style.display = 'block'; " +
									(displayMess2 ? " document.getElementById('a2').style.display = 'none'; " : "") +
									"})()\">" + messages1[1]+ " </a> "
									: "" ) + 
					(displayMess2 ? 
								" <a style=\"margin:5;\" href=\"#\"  onclick=\"javascript:(function() { " +
								(displayMess1 ? " document.getElementById('a1').style.display = 'none'; ":"") +
								" document.getElementById('a2').style.display = 'block'; " +
								"})()\">" + messages2[1]+ "</a> "
								: "" ) +							
							" <hr/> " + 
							(displayMess1 ? " <div id='a1' style=\"display:block;\"> " + messages1[0] + " 	</div> " : "" ) +
	        				(displayMess2 ? " <div id='a2' style=\"display:block;\"> " + messages2[0] + " 	</div> " : "" ) +
					        " <hr/> ";
	       
	        	        
	        //String summary = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><html><head></head><body>" + URLEncoder.encode(html, "utf-8").replaceAll("\\+"," ") + "</body></html>";
	        String summary = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><html><head></head><body>" + html + "</body></html>";
	        //(summary, "text/html", "utf-8");
	        webView.loadDataWithBaseURL("file:///sdcard/data/data/com.app.ant/", summary, "text/html", "utf-8", null);
	        webView.getSettings().setSupportZoom(true);
	        webView.getSettings().setBuiltInZoomControls(true);

    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in MessageProactiveForm.onCreate", ex);
		}  	
    }

    //--------------------------------------------------------------
    @Override public void onBackPressed() 
    {
    	try
    	{
    		//float scale = 100 * webView.getScale();
    		if(fromVisit)	
    			AntContext.getInstance().getTabController().onBackPressed(this);
    		else
    			this.finish();
    	}
		catch(Exception ex)
		{			
			ErrorHandler.CatchError("Exception in MessageProactiveForm.onBackPressed", ex);
		}   	
    }    
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
    		ErrorHandler.CatchError("MessageProactiveForm::onDestroy", ex);
    	}
    	
    }
    
}

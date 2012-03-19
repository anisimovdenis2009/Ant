package com.app.ant.app.ServiceLayer;

import android.content.Context;
import com.app.ant.R;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public abstract class PrintableDocument  
{
	private final int PAGE_LINES = 55;
	private final boolean LAST_PAGE_FORM_FEED = true;
	
	Map<String, String> docProps = new HashMap<String, String>();
	
	//to be implemented in child
	public abstract void loadDocument(Context context, long docID);
	
	//---------------------------------------------------------------
	public class ColumnInfo
	{
		public String field = "";
		public String header = "";
		public int maxWidth = 10;
		public boolean needSum = false;
		public boolean isTextField = false;
		public double sum = 0;
	};
	
	//---------------------------------------------------------------
	public class DocumentSections 
	{
		String pageHeader;
		String header;
		ArrayList<ColumnInfo> columns = new ArrayList<ColumnInfo>(); 
		String footer; 
		String pageFooter;
	};
	
	DocumentSections docSections = new DocumentSections();
	//---------------------------------------------------------------
	public static String padLeft(String value, char pad, int width)
	{
		int padCount = width - value.length();
		for(int i=0; i<padCount; i++)
			value = String.format("%s%s", pad, value);
		
		return value;
	}

	//---------------------------------------------------------------
	public static String padRight(String value, char pad, int width)
	{
		int padCount = width - value.length();		
		for(int i=0; i<padCount; i++)
			value = String.format("%s%s", value, pad);
		
		return value;
	}
	
	//---------------------------------------------------------------
	// table part of document
	public class DocumentDetails
	{
		int rowCount = 0;
		int columnWidthSum = 0;
		String rows = "";
		String header = "";
		String delimiter = "";
		String summary = "";
		
		public String formatColumnItem(String item, int maxWidth, boolean isText)
		{
			if(item == null)
				item = "";
			
			//longer text field should be trimmed, longer numeric field should be changed to *************
			if(item !=null && item.length() > maxWidth)	
				item = isText? item.substring(0, maxWidth) : padLeft("", '*', maxWidth); 

			item = isText ? padRight(item, ' ', maxWidth) : padLeft(item, ' ', maxWidth);
			
			return item;
		}
		
		public void addRow(String row)
		{
			rows = rows + row + "\r\n";
			rowCount++;
		}
		
		public void fillHeader(ArrayList<ColumnInfo> columns)
		{
			for(int i=0; i<columns.size(); i++)
			{
				ColumnInfo column = columns.get(i);				
				String columnHeader = formatColumnItem(column.header, column.maxWidth, true);
				
				header += columnHeader;
				columnWidthSum += column.maxWidth;
			}

			header += "\r\n";
		}
		
		public void fillSummary(ArrayList<ColumnInfo> columns)
		{
			for(int i=0; i<columns.size(); i++)
			{
				ColumnInfo column = columns.get(i);			
				String sum = column.needSum ? Convert.moneyToString(column.sum): "";				
				sum = formatColumnItem(sum, column.maxWidth, false);				
				summary += sum;
			}

			summary += "\r\n";
		}

		public void fillDelimiter()
		{
			delimiter = padRight("", '-', columnWidthSum);
			delimiter += "\r\n";
		}
		
	};
	
	DocumentDetails docDetails = new DocumentDetails();
	
	//---------------------------------------------------------------
	public String replaceTagWithProperty(String document, String tag, String dbField)
	{
		String value = docProps.get(dbField);		
		return document.replaceAll(tag, value!=null? value:"" );
	}
	
	//---------------------------------------------------------------
	public String replaceTagWithString(String document, String tag, String replacement)
	{
		return document.replaceAll(tag, replacement!=null? replacement:"" );
	}
	
	//---------------------------------------------------------------
	public String replaceTags(Context context, String document)
	{
		String today = Convert.dateTimeToString(Calendar.getInstance()); 
		String saler = Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_SALER_NAME, "");
		String van = Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_VAN, "");
		
		document = replaceTagWithString(document, "%CurrentDate%", today);
		document = replaceTagWithString(document, "%SalePerson%", saler);
		document = replaceTagWithString(document, "%VAN%", van);
		
		return document;
	}
	
	//---------------------------------------------------------------
    public void loadXML(InputStream is) 
    {
    	//
    	//parse xml using DOM model
    	//
    	
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try 
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(is);
            Element root = dom.getDocumentElement();
            NodeList items = root.getChildNodes();
            for (int i=0;i<items.getLength();i++)
            {
                Node item = items.item(i);
                NodeList properties = item.getChildNodes();
                String name = item.getNodeName();
                
                if(name.equalsIgnoreCase("pageheader"))
                {
                	docSections.pageHeader = item.getTextContent();
                }
                else if(name.equalsIgnoreCase("header"))
                {
                	docSections.header = item.getTextContent();
                }
                else if(name.equalsIgnoreCase("footer"))
                {
                	docSections.footer = item.getTextContent();
                }
                else if(name.equalsIgnoreCase("pagefooter"))
                {
                	docSections.pageFooter = item.getTextContent();
                }
                else if(name.equalsIgnoreCase("column"))
        		{       
                	ColumnInfo column = new ColumnInfo();                	
                	NamedNodeMap attribs = item.getAttributes();
                	
                	for(int k=0; k<attribs.getLength(); k++)
                	{
                		Node attr = attribs.item(k);
                		String attrName = attr.getNodeName(); 
                		String attrValue = attr.getTextContent();
                		
                		if(attrName.equalsIgnoreCase("field"))
                        	column.field = attrValue;
                        else if(attrName.equalsIgnoreCase("header"))
                        	column.header = attrValue;
                        else if(attrName.equalsIgnoreCase("maxwidth"))
                        	column.maxWidth = Convert.toInt(attrValue, 10);
                        else if(attrName.equalsIgnoreCase("sum"))
                        	column.needSum = attrValue.equalsIgnoreCase("1") | attrValue.equalsIgnoreCase("true") ? true:false;
                        else if(attrName.equalsIgnoreCase("textfield"))
                        	column.isTextField = attrValue.equalsIgnoreCase("1") | attrValue.equalsIgnoreCase("true") ? true:false;
                	}
                    
                    docSections.columns.add(column);
        		}
            }
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        } 
    }
	//---------------------------------------------------------------
    private String breakIntoPages(String document, String pageHeader, String pageFooter, String tableHeader, int tableRowsStartIndex, int tableRowsEndIndex)
    {
    	ArrayList<Integer> pageStarts = new ArrayList<Integer>();
    	
    	int pageStart = 0;
    	int lineCount = 0;
    	int lineStart = pageStart;
    	
    	//
    	//find indexes of page starts
    	//
    	for(;;)
    	{
    		int lineBreak = document.indexOf('\n', lineStart);
    		if(lineBreak!=-1)	
    		{
    			lineCount ++;
    			lineStart = lineBreak+1;
    			
    			if(lineCount>= PAGE_LINES)
    			{
    				pageStarts.add(pageStart);    				
    				lineCount=0;
    				pageStart = lineStart;
    			}  			
    		}
    		else //last line reached
    		{
    			if(lineCount!=0)
    				pageStarts.add(pageStart);
    			break;
    		}
    	}

    	//
    	//insert page breaks, page headers and page footers
    	//
    	int pageCount = pageStarts.size();
    	StringBuilder pagedDocument = new StringBuilder(document);
    	
    	for(int i=pageCount-1; i>=0; i--) //iterating backwards to not confuse indexes 
    	{
    		String header = pageHeader.replaceAll("%Page%", Convert.toString(i+1, "") );
    		header = header.replaceAll("%MaxPage%", Convert.toString(pageCount, "") );
    		
    		int curPageStart = pageStarts.get(i);    		
    		
    		//in case if we break table part, reinsert table header
    		if( curPageStart>tableRowsStartIndex && curPageStart< tableRowsEndIndex)
    			pagedDocument = pagedDocument.insert(curPageStart, tableHeader);
    		
    		pagedDocument = pagedDocument.insert(curPageStart, header);
    		
   			if(i!=0) //do not add page break and footer for the first page  			
   			{
   				pagedDocument = pagedDocument.insert(curPageStart, '\u000C');	//page break
   				pagedDocument = pagedDocument.insert(curPageStart, pageFooter);
   			}    		
    	}
    	
		pagedDocument.append(pageFooter);
		if(LAST_PAGE_FORM_FEED)
			pagedDocument.append('\u000C');

    
    	return pagedDocument.toString();
    }
	
	//---------------------------------------------------------------
	public String getPrintableDocument(Context context, long docID)
	{	
		try
		{
			//
			// Read XML template
			//
			InputStream is = context.getResources().openRawResource(R.raw.invoice);
			loadXML(is);
			is.close();
			
			//
			// Load document
			//
			loadDocument(context, docID);

			//
			//Substitute %value% strings with actual values
			//
			docSections.pageHeader = replaceTags(context, docSections.pageHeader);
			docSections.header = replaceTags(context, docSections.header);
			docSections.footer = replaceTags(context, docSections.footer);
			docSections.pageFooter = replaceTags(context, docSections.pageFooter);
			
			//
			// prepare table part of document
			//
			docDetails.fillHeader(docSections.columns);
			docDetails.fillSummary(docSections.columns);
			docDetails.fillDelimiter();
			
			//
			// Construct a whole uninterrupted document 
			//
			
			String document = docSections.header + docDetails.header;
			int tableRowsStartIndex = document.length();
			document += docDetails.rows + docDetails.delimiter + docDetails.summary;
			int tableRowsEndIndex = document.length();
			document += docSections.footer;
  
			//
			// break document into pages
			//
			document = breakIntoPages(document, docSections.pageHeader, docSections.pageFooter, docDetails.header, tableRowsStartIndex, tableRowsEndIndex);
			
			return document;
			
			/*return docSections.pageHeader +
					docSections.header + 
					docDetails.rows +
					docDetails.delimiter +
					docDetails.summary +
					docSections.footer + 
					docSections.pageFooter;*/
			
		}
		catch(Exception ex)
		{
			
		}
		
		return "";
	}
}

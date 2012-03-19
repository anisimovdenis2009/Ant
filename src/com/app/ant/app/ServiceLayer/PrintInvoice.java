package com.app.ant.app.ServiceLayer;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import com.app.ant.R;
import com.app.ant.app.BusinessLayer.Document;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.Printer.PrintableDocumentType;

import java.util.Calendar;


public class PrintInvoice extends PrintableDocument  
{
	PrintableDocumentType docType = PrintableDocumentType.Undefined;
	double vat = 0;
	
	public PrintInvoice(PrintableDocumentType docType, double vat)
	{
		this.vat = vat;
		this.docType = docType;
	}
	
	//-----------------------------------------------------------
	public String replaceTags(Context context, String document)
	{
		Resources resources = context.getResources();
		
		document = replaceTagWithProperty(document, "%DocNumber%", "DocNumberFormatted");
		document = replaceTagWithProperty(document, "%DocDate%", "DocDateFormatted");

		document = replaceTagWithProperty(document, "%SaleCompany%", "FirmNamePrint");
		document = replaceTagWithProperty(document, "%SaleAddress%", "FirmAddrName");
		document = replaceTagWithProperty(document, "%SaleCodeVAT%", "FirmTaxNo");
		document = replaceTagWithProperty(document, "%SaleLic%", "FirmSubjCode");
		document = replaceTagWithProperty(document, "%SaleMainAccount%", "FirmBankInfo");
		document = replaceTagWithProperty(document, "%SalePhone%", "FirmAddrPhone");
		
		document = replaceTagWithProperty(document, "%Client%", "ClientNamePrint");
		document = replaceTagWithProperty(document, "%ClientAddress%", "ClientAddrName");
		document = replaceTagWithProperty(document, "%ClientCodeVAT%", "ClientTaxNo");
		document = replaceTagWithProperty(document, "%ClientLic%", "ClientSubjCode");
		
		document = replaceTagWithProperty(document, "%Respite%", "Respite");
		
		document = replaceTagWithProperty(document, "%SumWOVAT%", "SumWOVAT");
		document = replaceTagWithProperty(document, "%VAT%", "SumVAT");
		document = replaceTagWithProperty(document, "%SumWVAT%", "SumAll");
		document = replaceTagWithProperty(document, "%SumSymbolic%", "SumAllSymbolic");
		
		document = replaceTagWithProperty(document, "%FullCaseQuantity%", "FullCaseQuantity");
		
        
		if(docType == PrintableDocumentType.Bill)
		{
			document = replaceTagWithString(document, "%DocType%", resources.getString(R.string.printer_bill));
			document = replaceTagWithString(document, "%SubjRole%", resources.getString(R.string.printer_bill_client_role));
		}
		else if(docType == PrintableDocumentType.Invoice)
		{
			document = replaceTagWithString(document, "%DocType%", resources.getString(R.string.printer_invoice));
			document = replaceTagWithString(document, "%SubjRole%", resources.getString(R.string.printer_invoice_client_role));
		}

		document = super.replaceTags(context, document);
		
		return document;
	}
	
	//-----------------------------------------------------------
	public void loadDocument(Context context, long docID)
	{
		loadDocumentHeader(context,docID);
		//for(int i=0; i<3; i++)
			loadDocumentDetails(context, docID);
	}
	
	//-----------------------------------------------------------
	private void loadDocumentHeader(Context context, long docID)
	{		
		char BWG = Document.DOC_COLOR_UNKNOWN;
		
		String companyID = Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_COMPANY_ID, "55404");
		String companyAddrID = Settings.getInstance().getPropertyFromSettings(Settings.PROPNAME_COMPANY_ADDR_ID, "125049");

		String sql = "SELECT d.DocType, d.DocNumber, d.DocDate, d.State, d.DocTax, d.SumAll, d.SumWOVAT, d.SumVAT, " +
					" d.Comments, d.ClientID, d.Respite, d.BWG, d.AddrID, " +
					" c.NamePrint AS ClientNamePrint, c.SubjCode AS ClientSubjCode, c.TaxNo AS ClientTaxNo, " +
					" a.AddrName AS ClientAddrName, a.AddrPhone AS ClientAddrPhone, " +
					" firm.NamePrint AS FirmNamePrint, firm.SubjCode AS FirmSubjCode, firm.TaxNo AS FirmTaxNo, firm.BankInfo AS FirmBankInfo, " +
					" fa.AddrName AS FirmAddrName, fa.AddrPhone AS FirmAddrPhone " +					
					" FROM Documents d " +
					" LEFT JOIN Clients c ON d.ClientID=c.ClientID " +
					" LEFT JOIN Addresses a ON d.AddrID=a.AddrID " +
					" LEFT JOIN Clients firm ON firm.ClientID =" + companyID + 
					" LEFT JOIN Addresses fa ON fa.AddrID = " + companyAddrID +  
					" WHERE d.DocID = " + docID;
		
    	Cursor cursor = Db.getInstance().selectSQL(sql);
    	
    	if(cursor!=null)
    	{
    		if(cursor.getCount()!=0)
    		{
    			cursor.moveToPosition(0);    			
    			BWG = Convert.getDocColorFromString(cursor.getString(cursor.getColumnIndex("BWG")));
    			Calendar docDate = Convert.getDateFromString(cursor.getString(cursor.getColumnIndex("DocDate")));
    			String docNumber = cursor.getString(cursor.getColumnIndex("DocNumber")); 
    				
    			docProps.put("DocDateFormatted", Convert.dateToString(docDate));
    			docProps.put("DocNumberFormatted", vat==0 ? docNumber+"H" : docNumber);
    			
    			for(int i=0;i<cursor.getColumnCount(); i++)
    			{
    				docProps.put(cursor.getColumnName(i), cursor.getString(i));
    			}
    		}
    		
    		cursor.close();
    		cursor = null;
    	}
    	
    	//print client info only if document "white"
    	if(BWG!=Document.DOC_COLOR_WHITE)
    	{
    		//erase info about client
    		docProps.put("ClientNamePrint", context.getResources().getString(R.string.printer_client_name_replacement));
    		docProps.put("ClientSubjCode", "");
    		docProps.put("ClientTaxNo", "");
    		docProps.put("ClientAddrName", context.getResources().getString(R.string.printer_client_address_replacement));
    		docProps.put("ClientAddrPhone", "");    		
    	}
		
		
	}
	
	//-----------------------------------------------------------
	private void loadDocumentDetails(Context context, long docID)
	{
        String sql = " SELECT i.ItemName, dets.Discount, dets.Price, dets.Orders, dets.ItemID, i.ItemTax, " +
        				" coalesce(cash.CashCode, i.CashName) as CheckName, i.PerCase, i.ItemExt, i.License " +  	
        				" FROM Items i " + 
        				" INNER JOIN DocDetails dets on i.ItemID = dets.ItemID " +
        				" LEFT JOIN CashCodes cash on cash.ItemID = i.ItemID " +  
        				" WHERE dets.DocID = " + docID + " AND dets.Orders > 0 AND i.ItemTax = " + Convert.roundUpMoney(vat) +
        				" ORDER BY i.SortId, i.ItemTypeID";

    	Cursor cursor = Db.getInstance().selectSQL(sql);
    	
    	if(cursor!=null)
    	{
    		double fullCaseQuantity = 0;
    		double sumWOVAT = 0;
    		
    		for(int i=0; i<cursor.getCount(); i++)
    		{
    			cursor.moveToPosition(i);
    			
    			double price = cursor.getDouble(cursor.getColumnIndex("Price"));
    			int quantity = cursor.getInt(cursor.getColumnIndex("Orders"));
    			int perCase = cursor.getInt(cursor.getColumnIndex("PerCase"));
    			fullCaseQuantity += Convert.roundUpMoney((double)quantity/(double)perCase);    			

    			String strRow = "";
    			
    			for(int j=0; j<docSections.columns.size(); j++)
    			{
    				ColumnInfo column = docSections.columns.get(j);
    				String strItem = "";    				
    				
    				if(column.field.equalsIgnoreCase("Index"))
    				{
    					strItem = Convert.toString(i+1, "");
    				}
    				else if(column.field.equalsIgnoreCase("Quantity"))
    				{
    					if(column.needSum)
    						column.sum += quantity;
    					
    					strItem = Convert.toString(quantity, "");
    				}
    				else if(column.field.equalsIgnoreCase("Price"))
    				{
    					double discount = cursor.getDouble(cursor.getColumnIndex("Discount"));
    					
    					price = Convert.roundUpMoney(price*(1.0 - discount/100.0));
    					strItem = Convert.moneyToString(price);
    				}
    				else if(column.field.equalsIgnoreCase("SumWOVAT"))
    				{
    					double sum = quantity*price;
    					
    					if(column.needSum)
    						column.sum += sum;

    					strItem = Convert.moneyToString(sum);
    					sumWOVAT +=sum;
    				}
    				else if(column.field.equalsIgnoreCase("PriceWVAT"))
    				{
    					double priceWvat = Convert.roundUpMoney(price * (1 + vat));
    					strItem = Convert.moneyToString(priceWvat); 
    				}
    				else if(column.field.equalsIgnoreCase("SumWVAT"))
    				{
    					double sumWvat = quantity * price * (1 + vat);

    					if(column.needSum)
    						column.sum += sumWvat;

    					strItem = Convert.moneyToString(sumWvat);
    				}
    				else 
    				{
    					if(column.field.length()!=0)
    					{
	    					int columnIdx = cursor.getColumnIndex(column.field);
	    					if(columnIdx!=-1)
	    						strItem = cursor.getString(columnIdx);
    					}
    				}
   					
        			strRow += docDetails.formatColumnItem(strItem, column.maxWidth, column.isTextField);
    			}
    		
    			docDetails.addRow(strRow);
    		}

    		docProps.put("FullCaseQuantity", Convert.toString(Convert.roundUpMoney(fullCaseQuantity), "0"));
    		
			double sumVat = Convert.roundUpMoney(sumWOVAT * vat);
			double sumAll = Convert.roundUpMoney(sumWOVAT + sumVat);
			String sumAllInWords = Convert.moneyToStringInWords(context.getResources(), sumAll);
			docProps.put("SumWOVAT", Convert.moneyToString(sumWOVAT));
			docProps.put("SumVAT", Convert.moneyToString(sumVat));
			docProps.put("SumAll", Convert.moneyToString(sumAll));
			docProps.put("SumAllSymbolic", sumAllInWords);
			
    		cursor.close();
    		cursor = null;
    	}
        
	}
}

package com.app.ant.app.BusinessLayer;

import android.content.Context;
import android.content.res.Resources;
import com.app.ant.R;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.Settings;

import java.util.Calendar;


public class Document 
{
	//------------------------- Parameter names for doc open (to be packed in Bundle) ----------------------------------------	
	public static final String	PARAM_NAME_DOCID			= "docId";
	public static final String	PARAM_NAME_DOCTYPE			= "docType";
	public static final String	PARAM_NAME_BASEDOCID		= "baseDocId";
	public static final String	PARAM_NAME_CREATE_NEW		= "newDocument";
	public static final String	PARAM_NAME_RETURN_TO_DEBTS	= "returnToDebts";
	public static final String	PARAM_NAME_FROM_VISIT		= "fromVisit";
	public static final String	PARAM_NAME_VISIT_SUMMARIES	= "visitSummaries";
	public static final String	PARAM_NAME_VISIT_TASKS		= "visitTasks";
	public static final String	PARAM_NAME_CLIENT_ID		= "clientID";
	
	//------------------------- Document colors ----------------------------------------	
	public static final char	DOC_COLOR_BLACK				= 'B';
	public static final char	DOC_COLOR_WHITE				= 'W';
	public static final char	DOC_COLOR_UNKNOWN			= 'U';
	
	//------------------------- Document types ----------------------------------------
	public static final char	DOC_TYPE_CLAIM				= 'Z';
	public static final char	DOC_TYPE_SALE				= 'B';
	public static final char	DOC_TYPE_PAYMENT			= 'S';
	public static final char	DOC_TYPE_REMNANTS			= 'R';
	public static final char	DOC_TYPE_UNKNOWN			= 'U';
	public static final char	DOC_TYPE_SDO				= 'F';
	public static final char	DOC_TYPE_PSDO				= 'K';
	public static final char	DOC_TYPE_DEBT_NOTIFICATION	= 'D';
	
	//------------------------- Document sources ----------------------------------------
	public static final int		DOC_SOURCE_DOCUMENTS		= 0;
	public static final int		DOC_SOURCE_DOC_DETAILS		= 1;
	
	public static int getDocReadableTypeResId(char docType)
	{
		switch(docType)
		{
			case DOC_TYPE_CLAIM:
				return R.string.document_docType_claim;
			case DOC_TYPE_SALE:				
				return R.string.document_docType_sale;
			case DOC_TYPE_PAYMENT:
				return R.string.document_docType_payment;
			case DOC_TYPE_REMNANTS:
				return R.string.document_docType_remnants;
			case DOC_TYPE_SDO:
				return R.string.document_docType_sdo;
			case DOC_TYPE_PSDO:
				return R.string.document_docType_psdo;
			case DOC_TYPE_DEBT_NOTIFICATION:
				return R.string.document_docType_debtNotification;
			case DOC_TYPE_UNKNOWN:				
			default:	
				return R.string.document_docType_unknown;				
		}
	}	

	//------------------------- Document states ----------------------------------------
	public static final char	DOC_STATE_NEW		= 'N';
	public static final char	DOC_STATE_PREVIOUS	= 'O';
	public static final char	DOC_STATE_CLOSED	= 'C';
	public static final char	DOC_STATE_SENT		= 'S';
	public static final char	DOC_STATE_FINISHED	= 'E';
	public static final char	DOC_STATE_DELETED	= 'D';
	public static final char	DOC_STATE_UNKNOWN	= 'U';
	
	public static int getDocReadableStateResId(char docState)
	{
		switch(docState)
		{
			case DOC_STATE_NEW:
				return R.string.document_docState_new;
			case DOC_STATE_PREVIOUS:				
				return R.string.document_docState_previous;
			case DOC_STATE_CLOSED:
				return R.string.document_docState_closed;
			case DOC_STATE_SENT:
				return R.string.document_docState_sent;
			case DOC_STATE_FINISHED:
				return R.string.document_docState_finished;
			case DOC_STATE_DELETED:
				return R.string.document_docState_deleted;
			case DOC_STATE_UNKNOWN:				
			default:	
				return R.string.document_docState_unknown;				
		}
	}	
	
	//------------------------------ Document Id generation -------------------------------------------
	public static long getNewCurDocID()
	{
		return getMinCurDocID()-1;
	}
	
	public static long getMinCurDocID()
    {
		String sql = "SELECT min(DocID) AS DocID FROM CurDocuments";		
		long minDoc = Db.getInstance().getDataLongValue(sql, 0);
		
		sql = "SELECT min(DocID) AS DocID FROM CurDocDetails";
		long minDocDets = Db.getInstance().getDataLongValue(sql, 0);		
		
		long res = Math.min(minDoc, minDocDets);		
        return res > 0 ? res*-1 : res;
    }
	
	public static long getMinDocId()
    {
		String sql = "SELECT min(DocID) AS DocID FROM Documents";
		long minDoc = Db.getInstance().getDataLongValue(sql, 0);
		
		sql = "SELECT min(DocID) AS DocID FROM DocDetails";
		long minDocDets = Db.getInstance().getDataLongValue(sql, 0);
		
		long res = Math.min(minDoc, minDocDets);		
        return res > 0 ? res*-1 : res;
    } 
	
	//------------------------------ Document number generation -------------------------------------------
	static final int DOC_NUMBER_MAX_LENGTH = 32;
	public static String getDocNumber(char docType)
	{		
		long salerId = Settings.getInstance().getLongPropertyFromSettings(Settings.PROPNAME_SALER_ID);
		Calendar date = Calendar.getInstance();
		String docNum = String.format("%d%c%d", salerId, docType, date.getTimeInMillis());	
		String shorterNum = docNum.substring(0,Math.min(DOC_NUMBER_MAX_LENGTH, docNum.length()));
		
		return shorterNum;
	}
	
	public static String addColorToDocNumber(String docNumber, char docColor)
	{
		//add doc color char to the end of document number
		String shorterNum = docNumber.substring(0,Math.min(DOC_NUMBER_MAX_LENGTH-1, docNumber.length()));
		String newNum = String.format("%s%c", shorterNum, docColor);
		
		return newNum;
	}
	
	public static String removeDocColorFromDocNumber(String docNumber)
	{
		String newNum = docNumber.substring(0, docNumber.length()-1);
		return newNum;
	}
	
	//------------------------------ Get/set data from/to doc header -----------------------------------------------------
	public static double getHeaderDoubleValue(long docId, String field, double defaultValue )
	{
		String sql = String.format("SELECT %s FROM documents WHERE DocID = %d", field, docId);
		double value = Db.getInstance().getDataDoubleValue( sql, defaultValue);
		return value;
	}		
	
	public static long getBaseDocId(long docId)
	{
		//ID of base docSale could be retrieved from docLinks table
		//missing record in DocLinks doesn't always mean error, because there could be payment documents which are not linked to sale docs.
		String sql = "SELECT t1.DocID FROM DocLinks t1 WHERE t1.LinkDocID = " + docId;
		long baseDocId = Db.getInstance().getDataLongValue( sql, 0);			
		return baseDocId;
	}
	
	public static void setHeaderValue(long docId, String field, Object value) 
	{
		String sql = String.format( "UPDATE Documents SET %s = ? WHERE DocID = ?", field);
		Object[] bindArgs = new Object[] { value, docId }; 
		Db.getInstance().execSQL(sql, bindArgs);
	}		
	
	
	//------------------------------ Delete document ------------------------------------------------------
	public static void deleteDocument(long docID, char docType, long parentDocID, char BWG, int docSource)
	{
		try
		{
			Db.getInstance().beginTransaction();
			
			//
			//mark documents with State = deleted
			//
			{
				String sql = String.format("UPDATE %s SET State = ? WHERE DocID = ?", docSource==DOC_SOURCE_DOCUMENTS ? "Documents":"CurDocuments");
				Object[] bindArgs = new Object[] { Document.DOC_STATE_DELETED, docID};
				Db.getInstance().execSQL(sql, bindArgs);
			}

			//
			//need to null corresponding column in CurDocDetails table, so when the undeleted pair document is being
			//loaded for editing, column will be empty
			//
			/*//COMMENTED OUT BECAUSE NOW WE RECREATE PARENT DOCUMENT ON EDIT
			if(parentDocID!=0)
			{
				if(BWG == Document.DOC_COLOR_WHITE )
				{
					String sql = String.format("UPDATE CurDocDetails SET OrdersI = null, DiscountI = null WHERE DocID=%d", parentDocID);
					Db.getInstance().execSQL(sql);	
				}
				else if(BWG == Document.DOC_COLOR_BLACK)
				{
					String sql = String.format("UPDATE CurDocDetails SET OrdersII = null, DiscountII = null WHERE DocID=%d", parentDocID);
					Db.getInstance().execSQL(sql);
				}				
			}*/
			
			//
			// make the rest modifications to database depending on document type
			//
			
			if(docSource == DOC_SOURCE_DOCUMENTS)
			{
				if(docType == DOC_TYPE_SALE || docType == Document.DOC_TYPE_CLAIM)
					DocSale.rollbackAddressItemsSaledQnt(docID, true); //descrease saled quantity in AddressItems table
				
				if(docType == DOC_TYPE_SALE)
				{
					//descrease saled quantity 
					String sql = "REPLACE INTO Rest (ItemID, Rest, SaledQnt) " +  
					" SELECT r.ItemID, r.Rest, coalesce(r.SaledQnt,0) - (coalesce(d.Orders,0)) " +  
					" FROM DocDetails d " + 
						" INNER JOIN Rest r ON r.ItemID=d.ItemID " +     
					" WHERE d.DocID=" + docID;
				
					Db.getInstance().execSQL(sql);				
				}			
				else if(docType == DOC_TYPE_PAYMENT)
				{
					long baseDocID = getBaseDocId(docID);
					double docSum = getHeaderDoubleValue(docID, "SumAll", 0); 
		
					if(baseDocID != 0 && docSum != 0)
					{
						//decrease sum that is already paid (SumLinked)
						double sumLinked = getHeaderDoubleValue(baseDocID, "SumLinked", 0); 
						sumLinked -= docSum;
		
						if(sumLinked < 0)
							sumLinked = 0;
		
						setHeaderValue(baseDocID, "SumLinked", sumLinked);
					}			
				}
				
				//recalc plans
		        //Long addrID = Db.getInstance().getDataLongValue(String.format("select AddrId from Documents where DocID = %s", docID), AntContext.getInstance().getAddrID());
		        Plans.recalcFacts();
			}
				
	        Db.getInstance().commitTransaction();
	        	        
		}
		catch(Exception ex)
		{
			ErrorHandler.CatchError("Exception in Document.deleteDocument", ex);
			throw new RuntimeException(ex);
		}						
		finally
		{
			Db.getInstance().endTransaction();
		}			
			
	}
	
	//------------------ Copy document from Documents to Current documents, return new ID ------------------------------------------------------
	public static void copyDocFromDocuments2CurDocuments(long newDocID, long docID)
	{		
        String sql = 
        	String.format(" INSERT OR REPLACE into CurDocuments (DocID, DocNumber, DocType, ClientID, AddrID, SalerID, DocDate, CreateDate, CloseDate, " +
        							" State, Respite, SumWOVAT, SumVAT, SumAll, PriceID, VisitID, ContactID, " +
        							" Comments, Comments2, SpecMarks, SpecMarks2) " + 
        				  "SELECT %d, MIN(SUBSTR(DocNumber, 1, LENGTH(DocNumber)-1)), MIN(DocType), MIN(ClientID), MAX(AddrID,0), MIN(SalerID), MIN(DocDate), MIN(CreateDate), MIN(CloseDate), " +
                          		"MIN(State), MIN(Respite), SUM(SumWOVAT), SUM(SumVAT), SUM(SumAll), MIN(PriceID), MIN(VisitID), MIN(ContactID)," +
                          		"MAX( CASE BWG WHEN 'W' THEN Comments ELSE '' END) AS Comments, MAX( CASE BWG WHEN 'B' THEN Comments ELSE '' END) AS Comments2, " +
                          		"MAX( CASE BWG WHEN 'W' THEN SpecMarks ELSE '' END) AS SpecMarks, MAX( CASE BWG WHEN 'B' THEN SpecMarks ELSE '' END) AS SpecMarks2 " + 
                          " FROM Documents " +  
                          " WHERE ParentDocID = %d AND State!='%s' AND State!='%s'", newDocID, newDocID, Document.DOC_STATE_PREVIOUS, Document.DOC_STATE_DELETED);
        
        //gives error "RIGHT and FULL OUTER JOINs are not currently supported"
        /*String selectString = String.format(" SELECT                    %d, coalesce(d1.DocNumber,d2.DocNumber), coalesce(d1.DocType,d2.DocType)," +
        		" coalesce(d1.ClientID,d2.ClientID), coalesce(d1.AddrID,d2.AddrID), coalesce(d1.SalerID,d2.SalerID), coalesce(d1.DocDate,d2.DocDate), " +
        		" coalesce(d1.CreateDate,d2.CreateDate), coalesce(d1.CloseDate,d2.CloseDate), coalesce(d1.State, d2.State), " +
				" coalesce(d1.Respite, d2.Respite), coalesce(d1.SumWOVAT,0) + coalesce(d2.SumWOVAT,0), coalesce(d1.SumVAT,0) + coalesce(d2.SumVAT,0), " +
				" coalesce(d1.SumAll,0)+coalesce(d2.SumAll,0), coalesce(d1.PriceID, d2.PriceID), coalesce(d1.VisitID, d2.VisitID), " +
				" d1.Comments, d1.SpecMarks, d2.Comments, d2.SpecMarks " + 
                " FROM Documents d1" +
                " 		OUTER JOIN Documents d2 ON d2.ParentDocID = %d AND d2.BWG='%c' AND d2.State!='%s' AND d2.State!='%s'" +  
                " WHERE d1.ParentDocID = %d AND d1.BWG='%c' AND d1.State!='%s' AND d1.State!='%s'",
                newDocID,
                newDocID, DOC_COLOR_BLACK, Document.DOC_STATE_PREVIOUS, Document.DOC_STATE_DELETED,
                newDocID, DOC_COLOR_WHITE, Document.DOC_STATE_PREVIOUS, Document.DOC_STATE_DELETED);*/        
        
        Db.getInstance().execSQL(sql);
        
        //---------------------------------

        sql = String.format( 
        		" INSERT into CurDocDetails (DocID, ItemID, OrdersI, OrdersII, DiscountI, DiscountII, Orders, Price, CurRemnant, ExpDate) " + 
        		" SELECT d.ParentDocID, dets.ItemID, " +
        			" SUM( CASE d.BWG WHEN 'W' THEN dets.Orders END) AS OrdersI, " +
        			" SUM( CASE d.BWG WHEN 'B' THEN dets.Orders END) AS OrdersII, " + 
        			" SUM( CASE d.BWG WHEN 'W' THEN dets.Discount END) AS DiscountI, " +
        			" SUM( CASE d.BWG WHEN 'B' THEN dets.Discount END) AS DiscountII, " +
        			" SUM(dets.Orders), MAX(dets.Price), SUM(dets.Remnant), MIN(dets.ExpDate) " +  
        		" FROM Documents d " +
        			" INNER JOIN DocDetails dets ON dets.DocID=d.DocID " +   
        		" WHERE  d.ParentDocID = %d AND d.State!='%s' AND d.State!='%s'" +
        		" GROUP BY d.ParentDocID, dets.ItemID ", newDocID, Document.DOC_STATE_PREVIOUS, Document.DOC_STATE_DELETED);   

        Db.getInstance().execSQL(sql);

	}
	//------------------ Copy document from Documents to Current documents, return new ID ------------------------------------------------------	
	public static void deleteFromCurDocuments(long docID)
	{
		String sqlDeleteDoc = "DELETE FROM CurDocuments WHERE DocID=" + docID;
		Db.getInstance().execSQL(sqlDeleteDoc);
		
		String sqlDeleteDets = "DELETE FROM CurDocDetails WHERE DocID=" + docID;
		Db.getInstance().execSQL(sqlDeleteDets);
	}
	
    //--------------------------------------------------------------
    public static String getReadableDocTypes(Context context, String strTableAlias)
    {
    	Resources res = context.getResources();
        return String.format(" CASE %s.DocType when '%s' then '%s' "
											  +" when '%s' then '%s'" 
											  +" when '%s' then '%s'"
											  +" when '%s' then '%s'"
											  +" when '%s' then '%s'"
											  +" when '%s' then '%s'"
											  +" when '%s' then '%s'"
											  +" else '' end AS ReadableDocType ", 
											   strTableAlias,
											   Document.DOC_TYPE_CLAIM, res.getString( Document.getDocReadableTypeResId(Document.DOC_TYPE_CLAIM)),
											   Document.DOC_TYPE_SALE, res.getString( Document.getDocReadableTypeResId(Document.DOC_TYPE_SALE)),
											   Document.DOC_TYPE_PAYMENT, res.getString( Document.getDocReadableTypeResId(Document.DOC_TYPE_PAYMENT)),
											   Document.DOC_TYPE_REMNANTS, res.getString( Document.getDocReadableTypeResId(Document.DOC_TYPE_REMNANTS)),
											   Document.DOC_TYPE_SDO, res.getString( Document.getDocReadableTypeResId(Document.DOC_TYPE_SDO)),
											   Document.DOC_TYPE_PSDO, res.getString( Document.getDocReadableTypeResId(Document.DOC_TYPE_PSDO)),
											   Document.DOC_TYPE_DEBT_NOTIFICATION, res.getString( Document.getDocReadableTypeResId(Document.DOC_TYPE_DEBT_NOTIFICATION)),
											   Document.DOC_TYPE_UNKNOWN, res.getString( Document.getDocReadableTypeResId(Document.DOC_TYPE_UNKNOWN))
											   );
        
    }
    //--------------------------------------------------------------------
    public static String getReadableDocStates(Context context, String strTableAlias)
    {
    	Resources res = context.getResources();
        return String.format(" CASE %s.State when '%s' then '%s' "
											  +" when '%s' then '%s'" 
											  +" when '%s' then '%s'"
											  +" when '%s' then '%s'"
											  +" when '%s' then '%s'"
											  +" when '%s' then '%s'"
											  +" when '%s' then '%s'"
											  +" else '' end AS ReadableDocState ", 
											   strTableAlias,
											   Document.DOC_STATE_NEW, res.getString( Document.getDocReadableStateResId(Document.DOC_STATE_NEW)),
											   Document.DOC_STATE_PREVIOUS, res.getString( Document.getDocReadableStateResId(Document.DOC_STATE_PREVIOUS)),
											   Document.DOC_STATE_CLOSED, res.getString( Document.getDocReadableStateResId(Document.DOC_STATE_CLOSED)),
											   Document.DOC_STATE_SENT, res.getString( Document.getDocReadableStateResId(Document.DOC_STATE_SENT)),
											   Document.DOC_STATE_FINISHED, res.getString( Document.getDocReadableStateResId(Document.DOC_STATE_FINISHED)),
											   Document.DOC_STATE_DELETED, res.getString( Document.getDocReadableStateResId(Document.DOC_STATE_DELETED)),
											   Document.DOC_STATE_UNKNOWN, res.getString( Document.getDocReadableStateResId(Document.DOC_STATE_UNKNOWN))
											   );
    }
	
    public static boolean hasLinesWithVat(long docId, double vatId)
    {
    	String sql = " SELECT count(dets.ItemID) " +
    				 " FROM Items i, DocDetails dets " +
    				 " WHERE dets.DocID = " + docId + " AND i.ItemID = dets.ItemID AND i.ItemTax = " + vatId;
    	
    	long itemCount = Db.getInstance().getDataLongValue(sql, 0);
    					
    	return (itemCount!=0);
    }    
}
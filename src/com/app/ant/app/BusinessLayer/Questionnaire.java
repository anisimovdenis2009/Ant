/**
 * 
 */
package com.app.ant.app.BusinessLayer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;


public class Questionnaire
{
	public static final int ACTIVITY_ID = 12345;

	private Long		id;
	private Long 		visitId;
	private Visit		visit;
	private String		name;
	private String		description;
	private boolean		isSaved = false;
	private boolean		byAddress = false;
	
	private Long[]	questionIds;
	private Map<Long, Question> questions;

	public Questionnaire(Long id, Visit visit, boolean byAddress)
	{
		String sql = "SELECT QuestionnaireID, Name, Description FROM Questionnaires WHERE QuestionnaireID = " + id;		
		Map<String, String> values = Db.getInstance().selectRowValuesInMap(sql);
		
		this.id = id;
		this.visit = visit;
		this.visitId = this.visit == null ? null : this.visit.getVisitID();
		/*this.name = values.get("Name");
		this.description = values.get("Description");*/
        this.name = " ";
		this.description = " ";
		this.byAddress = byAddress;
		
		values = null;
		
		questions = new LinkedHashMap<Long, Question>();
		
		if (!byAddress)
		{		
			sql = "SELECT q.QuestionID, q.QuestionTypeID, q.QuestionGroupID, q.QuestionText, q.QuestionCode, q.QuestionFormula, q.QuestionCondition, q.QuestionList, "
			 			+ " (SELECT count(QuestionID) FROM Questions WHERE QuestionGroupID = q.QuestionID AND QuestionTypeID = " + Question.TYPE_OPTION + ") as OptionCnt, "
		 				+ " q.CountOfImages, q.IsMandatory, q.SortCode, q.TargetValue, Flags, GetFieldQuery, SetFieldQuery, ValidateFieldQuery "
			  + " FROM Questions q "
			  + " WHERE QuestionnaireID = " + this.id + " AND QuestionGroupID is null"
			  + " ORDER BY q.SortCode";
		}
		else
		{
						
			//int attrTradeType = visit.getAddress().getTradeTypeAttribute();
			
			//String addonSql = attrTradeType > 0 ? String.format(" and exists (select QuestionID from QuestionAttributes qa where qa.AttrID = %s and q.QuestionID = qa.QuestionID)", attrTradeType) : "";
			sql = "SELECT q.QuestionID, q.QuestionTypeID, q.QuestionGroupID, q.QuestionText, q.QuestionCode, q.QuestionFormula, q.QuestionCondition, q.QuestionList, "
		 			+ " (SELECT count(QuestionID) FROM Questions WHERE QuestionGroupID = q.QuestionID AND QuestionTypeID = " + Question.TYPE_OPTION + ") as OptionCnt, "
	 				+ " q.CountOfImages, q.IsMandatory, q.SortCode, coalesce(qt.TargetValue, q.TargetValue) as TargetValue, Flags, GetFieldQuery, SetFieldQuery, ValidateFieldQuery "
			  + " FROM Questions q "
	 		  + "      INNER JOIN QuestionTargets qt ON q.QuestionID = qt.QuestionID AND qt.AddrID = " + visit.getAddress().addrID //+ " AND qt.AttrID = " + attrTradeType 	
			  + " WHERE QuestionnaireID = " + this.id + " AND QuestionGroupID is null"
			  //+ addonSql 
			  + " ORDER BY q.SortCode";
		}
				
		Cursor cursor = Db.getInstance().selectSQL(sql);
		questionIds = new Long[cursor.getCount()];
		
		for (int i = 0; i < cursor.getCount(); i++)
		{
			cursor.moveToPosition(i);					
			
			Question q = new Question(cursor, this, null);
			questionIds[i] = q.getId();
			questions.put(q.getId(), q);
		}
		cursor.close();
	}
	
	private static View getLine(Context context, int color, int height)
	{
		TextView line = new TextView(context);
		line.setBackgroundColor(color);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, height);
		params.setMargins(0, 0, 0, 4);
		line.setLayoutParams(params);		
		return line;
	}
	
	private static View getLine(Context context, int color)
	{		
		return getLine(context, color, 1);
	}	
	
	public void addViews(Context context, ViewGroup parentLayout, String title)
	{
		//
		//��������� ��������� ���������
		//
		
    	TextView tvHeader = new TextView(context);
    	tvHeader.setPadding(10, 0, 10, 16);
    	tvHeader.setBackgroundColor(Color.rgb(255, 255, 200));
    	
    	String text = (title == null? ("<b>" + getName() + "</b><br>" + getDescription()) : title);
    	Spannable sText = (Spannable) Html.fromHtml(text);        	
    	tvHeader.setText(sText);
    	tvHeader.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
    	parentLayout.addView(getLine(context, Color.BLACK, 2));
    	parentLayout.addView(tvHeader);
    	parentLayout.addView(getLine(context, Color.BLACK, 2));
	
    	//
    	//��������� �������� � ��������� � ������ �����
    	//    	
    	
        if (getQuestions()!=null && !getQuestions().isEmpty())
        {
        	int i=0; 
        	for(Question q : getQuestions().values())
			{
        		if (q != null && ((q.isParent() && q.getTypeId() > Question.TYPE_NO_ANSWER) || q.isGroup()))
				{
					parentLayout.addView(q.getView(context, ACTIVITY_ID));							
	        		parentLayout.addView(getLine(context, i < getQuestions().size() - 1 ? Color.LTGRAY : Color.WHITE));
				}        		
        		i++;
			}
        }
        	
	}

	public Long getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}
	
	public Long[] getQuestionIds()
	{
		return questionIds;
	}

	public boolean isSaved()
	{
		return isSaved;
	}
	
	public void setSaved(boolean isSaved)
	{
		this.isSaved = isSaved;
	}
	
	public Map<Long, Question> getQuestions()
	{		
		return questions;
	}
	
	public Question findQuestion(Long id)
	{
//		for (Map.Entry entry: map.entrySet())    
//			    doSomething(entry.getValue());
		
		Question q = null;		
		q = getQuestions().get(id);
		
		if (q == null)
		{
			for (int i = 0; i < this.getQuestionIds().length; i++)
			{
				Long mainId  = Convert.toLong(this.getQuestionIds()[i], 0);
        		Question mainQuestion = getQuestions().get(mainId);
        		
        		q = mainQuestion.getSubQuestions().get(id);
        		if (q != null) break;         		        	
			}
		}		
		return q;
	}
	
	public boolean validate()
	{
		boolean result = true;
		
		for (int i = 0; i < this.getQuestionIds().length; i++)
		{
			if (this.getQuestions() != null)
			{
        		Long id  = Convert.toLong(this.getQuestionIds()[i], 0);
        		Question q = this.getQuestions().get(id);	        		
				
        		if (q != null)
				{        			
        			result = q.validate(true);
				}
			}
			if (!result) break;
		}
		return result;
	}
	
	public boolean save()
	{
		boolean result = true;
		
		try
		{
			for (int i = 0; i < this.getQuestionIds().length; i++)
			{
				Long id  = Convert.toLong(this.getQuestionIds()[i], 0);
				Question q = this.getQuestions().get(id);	        		
				
				if (q != null)
				{						
					if (q.isGroup())
					{
						for (int j = 0; j < q.getSubQuestionIds().length; j++)
						{
							Long subId = Convert.toLong(q.getSubQuestionIds()[j], 0);
							Question subQ = q.getSubQuestions().get(subId);
							
							QuestionResult qResult = subQ.getResult(this.visitId);
							if (qResult != null) result = qResult.save();
						}
					}
					else
					{
						QuestionResult qResult = q.getResult(this.visitId);
						if (qResult != null) result = qResult.save();		
					}						
				}								
				if (!result) break;
			}
		}
		catch (Exception ex)
		{
			result = false;
			ErrorHandler.CatchError("Questionnaire.save", ex);
		}
		
		isSaved = result;
		
		return result;
	}

	public Visit getVisit()
	{
		return visit;
	}
	
	public boolean isByAddress()
	{
		return byAddress;
	}

	public void saveValuesToDb(Map<String,String> queryParams)
	{
		Collection<Question> questions = getQuestions().values();	    					
		for(Question question:questions)
		{
			question.saveValueToDb(queryParams);
		}
	}
	
	public void readValuesFromDb(Map<String,String> queryParams)
	{
		Collection<Question> questions = getQuestions().values();	    					
		for(Question question:questions)
		{
			question.readValueFromDb(queryParams);
		}
	}

}

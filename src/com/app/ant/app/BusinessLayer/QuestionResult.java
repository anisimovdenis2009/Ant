/**
 * 
 */
package com.app.ant.app.BusinessLayer;

import android.database.SQLException;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class QuestionResult
{
	private Long			visitId;
	private Long			questionId;
	private String			resultValue;
	private Date			startDate;
	private Date			endDate;
	private List<String>	attachedFiles;

	public QuestionResult(Long visitId, Long questionId, String resultValue, List<String> attachedFiles, Date startDate, Date endDate)
	{
		this.visitId = visitId;
		this.questionId = questionId;
		this.resultValue = resultValue;
		this.attachedFiles = attachedFiles;
		this.startDate = startDate == null ? new Date() : startDate;
		this.endDate = endDate == null ? new Date() : endDate;
	}

	public boolean save()
	{
		boolean result = true;
		try
		{			
			String value = resultValue == null ? "null" : "'" + resultValue + "'";
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	        
			String formattedStartDate = formatter.format(startDate);
			String formattedEndDate = formatter.format(endDate);
			String sql = String.format("replace into VisitQuestionResults (VisitID, QuestionID, Result, StartDate, EndDate, Sent) values (%s, %s, %s, '%s', '%s', 0)", visitId, questionId, value, formattedStartDate, formattedEndDate);
			Db.getInstance().execSQL(sql);
			
			if (attachedFiles != null) saveFiles();
		}
		catch (Exception ex)
		{
			result = false;
			ErrorHandler.CatchError("QuestionResult.save:q.id=" + questionId, ex);
		}
		return result;
	}

	private void saveFiles()
	{
		if (attachedFiles.size() > 0)
		{
			String sql = "select min(ID) from Files";
			Long minFileId = Db.getInstance().getDataLongValue(sql, -1);
					
			for (String fileName : attachedFiles)
			{
				try
				{
					//ID, FilePath, FileDescr, ToExport, Sent
					sql = String.format("select min(ID) from Files where FilePath = '%s'", fileName);
					Long findedId = Db.getInstance().getDataLongValue(sql, 0);
					Long id = findedId == 0 ? minFileId += -1 : findedId;			
	
					sql = String.format("replace into Files(ID, FilePath, FileDescr, ToExport, Sent) values (%s, '%s', null, 1, 0)", id, fileName);
					Db.getInstance().execSQL(sql);
					
					sql = String.format("replace into VisitQuestionFiles (VisitID, QuestionID, FileID, Sent) values (%s, %s, %s, 0)", visitId, questionId, id);
					Db.getInstance().execSQL(sql);
				}
				catch (SQLException ex)
				{
					ErrorHandler.CatchError("QuestionResult.saveFiles", ex);
				}
			}
		}
	}
	
	public Long getVisitId()
	{
		return visitId;
	}

	public void setVisitId(Long visitId)
	{
		this.visitId = visitId;
	}

	public Long getQuestionId()
	{
		return questionId;
	}

	public void setQuestionId(Long questionId)
	{
		this.questionId = questionId;
	}

	public String getResultValue()
	{
		return resultValue;
	}

	public void setResultValue(String resultValue)
	{
		this.resultValue = resultValue;
	}

}

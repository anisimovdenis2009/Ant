package com.app.ant.app.BusinessLayer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import com.app.ant.app.Activities.CameraPreview;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;

import java.util.*;


public class Question
{
	public static final long		TYPE_NO_ANSWER		= 0;
	public static final long		TYPE_INT			= 1;
	public static final long		TYPE_DECIMAL		= 2;
	public static final long		TYPE_BOOL			= 3;
	public static final long		TYPE_OPTION			= 4;
	public static final long		TYPE_COMBO			= 5;
	public static final long		TYPE_STRING			= 6;
	public static final long		TYPE_TEXT			= 7;
	public static final long		TYPE_DATE			= 8;
	public static final long		TYPE_LONG_STRING	= 9;

	public static final int			FLAG_MANDATORY		= 1;
	public static final int			FLAG_DYNAMIC		= 2;
	public static final int			FLAG_EQUAL_TARGET	= 4;
	public static final int			FLAG_ENABLED		= 8;

	public static final int			ID_TEXT				= 1;
	public static final int			ID_EDIT				= 2;
	public static final int			ID_PHOTO			= 3;
	public static final int			ID_TARGET			= 4;
	public static final int			ID_CHECK			= 5;
	public static final int			ID_OPTION			= 6;
	public static final int			ID_PHOTO_LABEL		= 7;
	public static final int			ID_SPINNER			= 8;
	public static final int			ID_DATE_TEXT		= 9;	

	protected Questionnaire			questionnaire;
	protected Long					id;
	protected Long					typeId;
	protected Long					groupId;
	protected String				text;
	protected String				code;
	protected String				formula;
	protected String				startCondition;
	protected String				listOfAnswerValues;
	protected Long[]				subQuestionsIds;
	protected boolean				isParent;
	protected boolean				isRadioGroup;
	protected Integer				needCntOfImages;
	protected boolean				isMandatory;
	protected String				targetValue;
	protected String				currentValue;
	protected Calendar				currentDateValue;
	protected List<String>			attachedFiles;
	protected Date					startDate;
	protected Date					endDate;
	protected Map<Long, Question>	subQuestions;
	protected Long					flags;
	protected String 				getFieldQuery;
	protected String 				setFieldQuery;
	protected String 				validateFieldQuery;

	protected QuestionResult		questionResult		= null;
	protected View					view;
	
	public List<String> getAttachedImages()
	{
		if (attachedFiles == null) attachedFiles = new ArrayList<String>();
		return attachedFiles;
	}

	public void addAttachedImages(String images)
	{		
		getAttachedImages().add(images);		
	}
	
	public Question(Long id, Questionnaire questionnaire)
	{
		String sql = "   SELECT QuestionID, QuestionTypeID, QuestionGroupID, QuestionText, QuestionCode, QuestionFormula, QuestionCondition, QuestionList, "
					 + " (SELECT count(QuestionID) FROM Questions WHERE QuestionGroupID = q.QuestionID AND QuestionTypeID = " + Question.TYPE_OPTION + ") as OptionCnt, "
					 + " CountOfImages, IsMandatory, SortCode, TargetValue, Flags, GetFieldQuery, SetFieldQuery, ValidateFieldQuery "
					 + " FROM Questions q " 
					 + " WHERE QuestionID = " + id
					 + " ORDER BY SortCode";

		try
		{
			Map<String, String> values = Db.getInstance().selectRowValuesInMap(sql);

			this.questionnaire = questionnaire;
			this.id = id;
			this.typeId = Convert.toLong(values.get("QuestionTypeID"), 0);
			this.groupId = Convert.toLong(values.get("QuestionGroupID"), 0);
			this.isParent = this.groupId == 0;
			this.text = values.get("QuestionText");
			this.code = values.get("QuestionCode");
			this.formula = values.get("QuestionFormula");
			this.startCondition = values.get("QuestionCondition");
			this.listOfAnswerValues = values.get("QuestionList");
			this.isRadioGroup = Convert.toInt(values.get("OptionCnt"), 0) > 0;
			this.needCntOfImages = Convert.toInt(values.get("CountOfImages"), 0);
			this.isMandatory = Convert.toInt(values.get("IsMandatory"), 0) == 1;
			this.targetValue = values.get("TargetValue");
			this.flags = Convert.toLong(values.get("Flags"), 0);
			this.getFieldQuery = values.get("GetFieldQuery");
			this.setFieldQuery = values.get("SetFieldQuery");
			this.validateFieldQuery = values.get("ValidateFieldQuery");
			
			values = null;

		}
		catch (Exception ex)
		{
			ErrorHandler.CatchError("Question.Constructor", ex);
		}
	}

	public Question(Cursor cursor, Questionnaire questionnaire, Long visitId)
	{														
		this.questionnaire = questionnaire;
		this.startDate = new Date();
		
		fillFromCursor(cursor);
					
	}
	
	protected void fillFromCursor(Cursor cursor)
	{
		try
		{	
			this.id = cursor.getLong(cursor.getColumnIndex("QuestionID"));
			this.typeId = cursor.getLong(cursor.getColumnIndex("QuestionTypeID"));
			this.groupId = cursor.getLong(cursor.getColumnIndex("QuestionGroupID"));
			this.isParent = this.groupId == 0;
			this.text = cursor.getString(cursor.getColumnIndex("QuestionText"));
			this.code = cursor.getString(cursor.getColumnIndex("QuestionCode"));
			this.formula = cursor.getString(cursor.getColumnIndex("QuestionFormula"));
			this.startCondition = cursor.getString(cursor.getColumnIndex("QuestionCondition"));
			this.listOfAnswerValues = cursor.getString(cursor.getColumnIndex("QuestionList"));
			this.isRadioGroup = cursor.getInt(cursor.getColumnIndex("OptionCnt")) > 0;
			this.needCntOfImages = cursor.getInt(cursor.getColumnIndex("CountOfImages"));
			this.isMandatory = cursor.getInt(cursor.getColumnIndex("IsMandatory")) == 1;
			this.targetValue = cursor.getString(cursor.getColumnIndex("TargetValue"));
			this.flags = cursor.getLong(cursor.getColumnIndex("Flags"));
			this.getFieldQuery = cursor.getString(cursor.getColumnIndex("GetFieldQuery"));
			this.setFieldQuery = cursor.getString(cursor.getColumnIndex("SetFieldQuery"));
			this.validateFieldQuery = cursor.getString(cursor.getColumnIndex("ValidateFieldQuery"));
			
		}
		catch (Exception ex)
		{			
			ErrorHandler.CatchError("Question.fillFromCursor", ex);
		}
	}
	
	public Long getId()
	{
		return id;
	}

	public Long getTypeId()
	{
		return typeId;
	}

	public Long getGroupId()
	{
		return groupId;
	}

	public String getText()
	{
		return text;
	}

	public String getCode()
	{
		return code;
	}

	public String getFormula()
	{
		return formula;
	}

	public String getStartCondition()
	{
		return startCondition;
	}
	
	public boolean isRadioGroup()
	{
		return isRadioGroup;
	}
	
	public Integer getNeedCntOfImages()
	{
		return needCntOfImages;
	}

	public void setNeedCntOfImages(Integer cnt)
	{
		this.needCntOfImages = cnt;
	}

	public boolean isMandatory()
	{
		return isMandatory;
	}

	public String[] getMultipleAnswersArray()
	{
		return listOfAnswerValues.split(";");
	}
	
	private void populateSubQuestions()
	{
		if (subQuestionsIds == null && isParent())
		{
			subQuestions = new HashMap<Long, Question>();
			
			String sql ="";
			
			if (!questionnaire.isByAddress())
			{
			sql = " SELECT QuestionID, QuestionTypeID, QuestionGroupID, QuestionText, QuestionCode, QuestionFormula, QuestionCondition, QuestionList, "
					 + " (SELECT count(QuestionID) FROM Questions WHERE QuestionGroupID = q.QuestionID AND QuestionTypeID = " + Question.TYPE_OPTION + ") as OptionCnt, "
					 + " CountOfImages, IsMandatory, SortCode, TargetValue, Flags, GetFieldQuery, SetFieldQuery, ValidateFieldQuery "
					 + " FROM Questions q " 
					 + " WHERE QuestionGroupID = " + this.id
					 + " ORDER BY SortCode";
			}
			else
			{
				//int attrTradeType = questionnaire.getVisit().getAddress().getTradeTypeAttribute();
				
				//String addonSql = attrTradeType > 0 ? String.format(" and exists (select QuestionID from QuestionAttributes qa where qa.AttrID = %s and q.QuestionID = qa.QuestionID)", attrTradeType) : "";
				sql = "SELECT q.QuestionID, q.QuestionTypeID, q.QuestionGroupID, q.QuestionText, q.QuestionCode, q.QuestionFormula, q.QuestionCondition, q.QuestionList, "
			 			+ " (SELECT count(QuestionID) FROM Questions WHERE QuestionGroupID = q.QuestionID AND QuestionTypeID = " + Question.TYPE_OPTION + ") as OptionCnt, "
		 				+ " q.CountOfImages, q.IsMandatory, q.SortCode, coalesce(qt.TargetValue, q.TargetValue) as TargetValue, Flags, GetFieldQuery, SetFieldQuery, ValidateFieldQuery "
				  + " FROM Questions q "
		 		  + "      INNER JOIN QuestionTargets qt ON q.QuestionID = qt.QuestionID AND qt.AddrID = " + questionnaire.getVisit().getAddress().addrID //+ " AND qt.AttrID = " + attrTradeType 	
				  + " WHERE QuestionGroupID = " + this.id 
				  //+ addonSql 
				  + " ORDER BY q.SortCode";
			}
			
			Cursor cursor = Db.getInstance().selectSQL(sql);
			subQuestionsIds = new Long[cursor.getCount()];
			
			for (int i = 0; i < cursor.getCount(); i++)
			{
				cursor.moveToPosition(i);					
				
				Question q = new Question(cursor, this.questionnaire, null);			
				subQuestionsIds[i] = q.getId();
				subQuestions.put(q.getId(), q);
			}
			
			cursor.close();
		}	
	}
	
	public Long[] getSubQuestionIds()
	{
		populateSubQuestions();
		return subQuestionsIds;
	}
	
	public Map<Long, Question> getSubQuestions()
	{
		populateSubQuestions();
		return subQuestions;
	}
	
	public boolean isParent()
	{
		return isParent;
	}
	
	public boolean isGroup()
	{
		return getSubQuestionIds() != null && getSubQuestionIds().length > 0; 
	}
	
	protected Button getPhotoButton(final Context context, final int id, final int requestCode, Long questionId)
	{
    	Button btnPhoto = new Button(context);
    	btnPhoto.setText("����"); //TODO resources, image
    	btnPhoto.setId(id);
    	
    	btnPhoto.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(context, CameraPreview.class);
				intent.putExtra("id", id);
				((Activity)context).startActivityForResult(intent, requestCode);
			}
		});
		return btnPhoto;
	}
	
	protected TextView getImageLabel(Context context, String text)
	{
		TextView tv = new TextView(context);
		tv.setText(text);
		return tv;
	}
	
	public View getView(final Context context, int requestCode)
	{				
        if (view == null)
        {
        	RelativeLayout qLayout = new RelativeLayout(context);
        	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        	qLayout.setLayoutParams(params);
        	view = qLayout;
        	
        	TextView qvText = new TextView(context);
	        qvText.setId(ID_TEXT);
	        if (this.isGroup()) qvText.setTypeface(Typeface.DEFAULT_BOLD);
	        qvText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, this.isParent ? 14 : 13);
	        qvText.setPadding(10, 0, 0, 0);
	        
	        final TextView qvTargetValue = new TextView(context);
	        qvTargetValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
	        qvTargetValue.setTextColor(Color.GRAY);
	        if (!Convert.isNullOrBlank(this.getTargetValue())) qvTargetValue.setText("������� ��������: " + this.getTargetValue()); //TODO resources
	        qvTargetValue.setId(ID_TARGET);
	        qvTargetValue.setPadding(11, 0, 0, 0);
	        
	        TextView vPhotoLabel = new TextView(context);
	        vPhotoLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
	        vPhotoLabel.setTextColor(Color.BLUE);
	        vPhotoLabel.setId(ID_PHOTO_LABEL);
	        vPhotoLabel.setTypeface(Typeface.DEFAULT_BOLD);
	        
	        TextView vMandatoryLabel = new TextView(context);
	        vMandatoryLabel.setText("*"); //TODO resources
	        vMandatoryLabel.setTextColor(Color.RED);
	        RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
	        vMandatoryLabel.setLayoutParams(lParams);
	        qLayout.addView(vMandatoryLabel);
	        
	        if ((isGroup() && !isRadioGroup()) || !isMandatory()) vMandatoryLabel.setVisibility(View.INVISIBLE);
	        
	        if (this.typeId == Question.TYPE_INT || this.typeId == Question.TYPE_DECIMAL || this.typeId == Question.TYPE_STRING || this.typeId == Question.TYPE_TEXT) //input
	        {	        		        		        	
	        	String prefix = this.isParent ? "" : " - ";
	        	qvText.setText(prefix + this.getText());
	        	
	        	final EditText editText = new EditText(context);
	        	/*if(currentValue!=null )
	        		editText.setText(currentValue);*/
	        	        	
	        	if (this.typeId != Question.TYPE_TEXT) editText.setSingleLine();
	        	
	        	editText.setId(ID_EDIT);
	        	editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
	        	
	        	if (this.typeId == Question.TYPE_INT)
	        	{
	        		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
	        	}
	        	else if (this.typeId == Question.TYPE_DECIMAL)
	        	{
	        		editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
	        	}
	        	        	
				editText.addTextChangedListener(new TextWatcher()
				{
					public void afterTextChanged(Editable s)
					{
						currentValue = s.toString();
						if (!Convert.isNullOrBlank(targetValue) && !Convert.isNullOrBlank(currentValue))
						{
							if (typeId == Question.TYPE_INT)
				        	{			        		
								int target = Convert.toInt(targetValue, 0);
				        		int current = Convert.toInt(currentValue, 0);
				        		qvTargetValue.setTextColor(current >= target ? Color.rgb(0, 150, 0) : Color.rgb(255, 0, 128));				        		
				        	}
				        	else if (typeId == Question.TYPE_DECIMAL)
				        	{
				        		double target = Convert.toDouble(targetValue, 0.0);
				        		double current = Convert.toDouble(currentValue, 0.0);
				        		qvTargetValue.setTextColor(current >= target ? Color.rgb(0, 150, 0) : Color.rgb(255, 0, 128));
				        	}
						}
						else
						{
							qvTargetValue.setTextColor(Color.GRAY);
						}
						endDate = new Date();
						questionnaire.setSaved(false);
					}
	
					public void beforeTextChanged(CharSequence s, int start, int count, int after)
					{}
	
					public void onTextChanged(CharSequence s, int start, int before, int count)
					{}
				});				
	
	        	Button btnPhoto = getPhotoButton(context, ID_PHOTO, requestCode, this.id);
	        	
	        	params = new RelativeLayout.LayoutParams(350, LayoutParams.WRAP_CONTENT);        	
	        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        	qvText.setLayoutParams(params);
	        	
	        	params = new RelativeLayout.LayoutParams(175, LayoutParams.WRAP_CONTENT);        	
	        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        	params.addRule(RelativeLayout.BELOW, qvText.getId());
	        	qvTargetValue.setLayoutParams(params);
	        	
	        	params = new RelativeLayout.LayoutParams(175, LayoutParams.WRAP_CONTENT);
	        	params.addRule(RelativeLayout.RIGHT_OF, qvTargetValue.getId());
	        	params.addRule(RelativeLayout.ALIGN_TOP, qvTargetValue.getId());
	        	vPhotoLabel.setLayoutParams(params);
	        	
	        	if (this.typeId == Question.TYPE_TEXT)
	        	{
		        	params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		        	params.addRule(RelativeLayout.BELOW, qvTargetValue.getId());
	        	}
	        	else
	        	{
		        	params = new RelativeLayout.LayoutParams(80, 50);
		        	params.addRule(RelativeLayout.LEFT_OF, btnPhoto.getId());
		        	params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
	        	}	        	
	        	editText.setLayoutParams(params);
	        		        	
				if (this.typeId == Question.TYPE_TEXT)
				{
					params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 50);
					params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					params.addRule(RelativeLayout.ALIGN_TOP, qvText.getId());
					params.addRule(RelativeLayout.ABOVE, editText.getId());
				}
				else
				{
					params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 50);
					params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
				}
	        	btnPhoto.setLayoutParams(params);
	        	
	        	qLayout.addView(qvText); 
	        	qLayout.addView(qvTargetValue);
	        	qLayout.addView(editText);
	        	qLayout.addView(vPhotoLabel);
	        	qLayout.addView(btnPhoto);
	        	
				if (needCntOfImages == 0)
				{
					btnPhoto.setVisibility(View.INVISIBLE);
				}
	        }
	        else if (this.typeId == Question.TYPE_BOOL) //checkBox
	        {
	        	CheckBox checkBox = new CheckBox(context);
	        	checkBox.setId(ID_CHECK);  
	        	Button btnPhoto = getPhotoButton(context, ID_PHOTO, requestCode, this.id);
	        	qvText.setText(this.getText());
	        	
	        	/*if(currentValue!=null && currentValue.equals("1"))
	        		checkBox.setChecked(true);*/
	        	
	        	checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{				
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						CheckBox checkBox = (CheckBox)buttonView;
						currentValue = checkBox.isChecked() ? "1" : "0";
						endDate = new Date();
						questionnaire.setSaved(false);
					}
				});
	        	
	        	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);        	
	        	params.addRule(RelativeLayout.LEFT_OF, btnPhoto.getId());
	        	params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
	        	checkBox.setLayoutParams(params);	        	
	        	
	        	params = new RelativeLayout.LayoutParams(340, LayoutParams.WRAP_CONTENT);        	
	        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        	qvText.setLayoutParams(params);
	        	
	        	params = new RelativeLayout.LayoutParams(170, LayoutParams.WRAP_CONTENT);        	
	        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        	params.addRule(RelativeLayout.BELOW, qvText.getId());
	        	qvTargetValue.setLayoutParams(params);
	        	
	        	params = new RelativeLayout.LayoutParams(100, LayoutParams.WRAP_CONTENT);
	        	params.addRule(RelativeLayout.RIGHT_OF, qvTargetValue.getId());
	        	params.addRule(RelativeLayout.ALIGN_TOP, qvTargetValue.getId());
	        	vPhotoLabel.setLayoutParams(params);
	        	
	        	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 50);        	
	        	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
	        	params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);        	
	        	btnPhoto.setLayoutParams(params);
	        	        	
	        	qLayout.addView(checkBox);
	        	qLayout.addView(qvText);
	        	qLayout.addView(qvTargetValue);
	        	qLayout.addView(vPhotoLabel);
	        	qLayout.addView(btnPhoto);
	        	
				if (needCntOfImages == 0)
				{
					btnPhoto.setVisibility(View.INVISIBLE);
				}        	
	        }
	        else if (this.typeId == Question.TYPE_COMBO) //spinner
	        {
	        	final Spinner spinner = new Spinner(context);
	        	spinner.setId(ID_SPINNER);
	        	Button btnPhoto = getPhotoButton(context, ID_PHOTO, requestCode, this.id);
	        	qvText.setText(this.getText());
	        	
	        	spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
				{
					@Override public void onItemSelected(AdapterView<?> arg0, View view, final int position, long arg3)
					{
						currentValue = Convert.toString(spinner.getSelectedItem(), null); 
						endDate = new Date();
						questionnaire.setSaved(false);
					}
	
					@Override public void onNothingSelected(AdapterView<?> arg0){}
				});
	        	
	        	ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context,   android.R.layout.simple_spinner_item, this.getMultipleAnswersArray());
	        	spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
	        	spinner.setAdapter(spinnerArrayAdapter);
	        	
	        	params = new RelativeLayout.LayoutParams(200, LayoutParams.WRAP_CONTENT);        	
	        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
	        	params.addRule(RelativeLayout.BELOW, qvText.getId());
	        	params.addRule(RelativeLayout.LEFT_OF, btnPhoto.getId());
	        	spinner.setLayoutParams(params); 
	        	
	        	params = new RelativeLayout.LayoutParams(340, LayoutParams.WRAP_CONTENT);        	
	        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        	qvText.setLayoutParams(params);
	        	
	        	params = new RelativeLayout.LayoutParams(170, LayoutParams.WRAP_CONTENT);        	
	        	params.addRule(RelativeLayout.RIGHT_OF, qvText.getId());
	        	qvTargetValue.setLayoutParams(params);
	        	
	        	params = new RelativeLayout.LayoutParams(170, LayoutParams.WRAP_CONTENT);
	        	params.addRule(RelativeLayout.RIGHT_OF, qvText.getId());
	        	params.addRule(RelativeLayout.ABOVE, spinner.getId());
	        	vPhotoLabel.setLayoutParams(params);
	        	
	        	params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 50);
	        	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
	        	params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
	        	btnPhoto.setLayoutParams(params);
	        	
	        	qLayout.addView(spinner);
	        	qLayout.addView(qvText);
	        	qLayout.addView(qvTargetValue);
	        	qLayout.addView(vPhotoLabel);
	        	qLayout.addView(btnPhoto);
	        	
	        }
	        else if (this.typeId == Question.TYPE_OPTION) //radioButton
	        {
	        	RadioButton option = new RadioButton(context); 
	        	
	        	String text = "" + this.getText() + "<br>";
	        	Spannable sText = (Spannable) Html.fromHtml(text);
	        	
	        	if (!Convert.isNullOrBlank(this.getTargetValue()))
	        	{
		        	text = text + "<small>" + this.getTargetValue() + "</small>";
	        		sText = (Spannable) Html.fromHtml(text);
		        	int start = this.getText().length();
		        	int end = start + this.getTargetValue().length() + 1;
		        	sText.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, 0);
	        	}
	        	option.setText(sText);
	        	
	        	/*if(currentValue!=null && currentValue.equals("1"))
	        		option.setChecked(true);*/
	        	
	        	option.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
	        	
	        	option.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{				
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						RadioButton radioButton = (RadioButton)buttonView;
						currentValue = radioButton.isChecked() ? "1" : "0";
						endDate = new Date();
						questionnaire.setSaved(false);
					}
				});
	        	
	        	view = option;
	        }
	        else if (this.typeId == Question.TYPE_DATE) 
	        {
	        	qvText.setText(this.getText());
	        	
	        	EditText dateText = new EditText(context);
	        	dateText.setId(ID_DATE_TEXT);
	        	dateText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
	        	dateText.setInputType(InputType.TYPE_NULL);
	        	dateText.setFocusable(false);
	        	
	        	//set layout params for dateText
				params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 50);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.addRule(RelativeLayout.ALIGN_TOP, qvText.getId());				
				dateText.setLayoutParams(params);
				
				OnClickListener	dateClickListener =	new OnClickListener()
				{					
					@Override
					public void onClick(View v)
					{
						Calendar date = currentDateValue!=null ? currentDateValue:Calendar.getInstance();
						
						Dialog dlg = new DatePickerDialog(context, 
				        		new DatePickerDialog.OnDateSetListener() 
				        		{
				                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
				                    {
				                        Calendar date = Calendar.getInstance();
				                        date.set(year, monthOfYear, dayOfMonth);

				                        currentDateValue = date;
				                        String value = Convert.dateToString(currentDateValue);
				                        setValue(value);
				                    }
				        		}, 
				        		date.get(Calendar.YEAR), 
				        		date.get(Calendar.MONTH), 
				        		date.get(Calendar.DAY_OF_MONTH));
						
						dlg.show();
					}
				};
				
				view.setOnClickListener(dateClickListener);
				qvText.setOnClickListener(dateClickListener);
				dateText.setOnClickListener(dateClickListener);
				
	        	qLayout.addView(qvText); 
	        	qLayout.addView(dateText);	        	
	        }        
	        
	        else //other
	        {        		        	
	        	qvText.setText(this.getText());
	        	qLayout.addView(qvText);
	        	
	        	RadioGroup radioGroup = new RadioGroup(context);
	        	if (isRadioGroup)
	        	{
	        		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);        	
    	        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    	        	params.addRule(RelativeLayout.BELOW, qvText.getId());
    	        	radioGroup.setLayoutParams(params);	        		
    	        	qLayout.addView(radioGroup);
	        	}
	            		
				if (isGroup())
	    		{	    			
					for (int j = 0; j < this.getSubQuestionIds().length; j++)
	    			{
	    				Long subId  = Convert.toLong(this.getSubQuestionIds()[j], 0);
	            		Question subQ = this.getSubQuestions().get(subId);
	            		if (isRadioGroup)
	                	{
	            			radioGroup.addView(subQ.getView(context, requestCode));
	                	}
	            		else
	            		{	            			
	            			View tmpView = subQ.getView(context, requestCode);
	            			int tempId = j + 100;
	            			tmpView.setId(tempId);
	            			
	            			params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);        	
	        	        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	        	        	params.addRule(RelativeLayout.BELOW, (j == 0 ? qvText.getId() : tempId - 1));
	        	        	tmpView.setLayoutParams(params);
	            			
	            			qLayout.addView(tmpView);
	            		}
	    			}
	    		}
	        }
        }
        return view;
	}

	public QuestionResult getResult(Long visitId)
	{
		//TODO radiogroup
		if(this.typeId == TYPE_DATE)
		{
			String value = currentDateValue == null ? "" : Convert.getSqlDateFromCalendar(currentDateValue); 
			questionResult = new QuestionResult(visitId, this.id, value, attachedFiles, startDate, endDate);
		}
		else
		{
			questionResult = new QuestionResult(visitId, this.id, currentValue, attachedFiles, startDate, endDate);
		}
		return questionResult;
	}
	
	public boolean validate(boolean mark)
	{
		boolean isValid = !isMandatory || typeId == Question.TYPE_NO_ANSWER;
		
		if (isGroup() && !isRadioGroup())
		{
			for (int j = 0; j < this.getSubQuestionIds().length; j++)
			{
				Long subId = Convert.toLong(this.getSubQuestionIds()[j], 0);
				Question subQ = this.getSubQuestions().get(subId);

				isValid = subQ.validate(mark);

				if (!isValid) break;
			}
		}
		else if (isRadioGroup())
		{
			isValid = false;
			for (int j = 0; j < this.getSubQuestionIds().length; j++)
			{
				Long subId = Convert.toLong(this.getSubQuestionIds()[j], 0);
				Question subQ = this.getSubQuestions().get(subId);

				isValid = subQ.validate(false);

				if (isValid) break;
			}
		}
		else
		{
			if (!isValid)
				isValid = (currentValue != null && currentValue.length() > 0);
		}

		if (mark && view != null) view.setBackgroundColor(isValid ? Color.WHITE : Color.RED);
		
		return isValid; 
	}
	
	public void updatePhotoInfo()
	{
		if (view != null)
		{
			TextView tv = (TextView) view.findViewById(ID_PHOTO_LABEL); 
			if (tv != null) 
			{
				int i = getAttachedImages() == null ? 0 : getAttachedImages().size();
				String lbl = "<u>����������:" + i + "</u>";
				tv.setText(Html.fromHtml(lbl));
			}
		}
	}

	public String getTargetValue()
	{
		return targetValue;
	}

	public void setTargetValue(String targetValue)
	{
		this.targetValue = targetValue;
	}
	
	public void setValue(String value)
	{
		currentValue = value;
		
		if(view == null)
			return;
		
		if (this.typeId == Question.TYPE_INT || this.typeId == Question.TYPE_DECIMAL || this.typeId == Question.TYPE_STRING || this.typeId == Question.TYPE_TEXT)
		{
			TextView text = (TextView) view.findViewById(ID_EDIT);
			text.setText(value);
		}
		else if(this.typeId == Question.TYPE_OPTION)
		{
			RadioButton radio =	(RadioButton) view;
			radio.setChecked(value.equals("1"));
		}
		else if(this.typeId == Question.TYPE_BOOL)
		{
			CheckBox chk = (CheckBox) view.findViewById(ID_CHECK);
			chk.setChecked(value.equals("1"));
		}
		else if(this.typeId == Question.TYPE_DATE)
		{
			TextView text = (TextView) view.findViewById(ID_DATE_TEXT);
			text.setText(value);
		}
	}

	private String replaceQueryTokens(String query, Map<String,String> queryParams)
	{
		String result = query;
		for(Map.Entry<String, String> param:queryParams.entrySet())
		{
			result = result.replaceAll("%"+param.getKey()+"%", param.getValue());			
		}
		return result;
	}

	/*
	 *��������� ������ SQL ��� ���������� ���������� 
	 */	
	public void saveValueToDb(Map<String,String> queryParams)
	{
		if(setFieldQuery == null)
			return;
		
		String sql = replaceQueryTokens(setFieldQuery, queryParams);
		QuestionResult qr = getResult(-1L);
		if(qr.getResultValue()!=null)
		{
			Object[] bindArgs = new Object[] { qr.getResultValue() };
			Db.getInstance().execSQL(sql, bindArgs);
		}		
	}
	
	/*
	 *��������� ������ SQL ��� �������� ���������� �������� 
	 */	
	public void readValueFromDb(Map<String,String> queryParams)
	{
		if(getFieldQuery == null)
			return;
		
		String sql = replaceQueryTokens(getFieldQuery, queryParams);
		String value = Db.getInstance().getDataStringValue(sql, "");
		if(this.typeId == Question.TYPE_DATE && value!=null && value.length()!=0 )
		{
			try
			{
				currentDateValue = Convert.getDateFromString(value);
				value = Convert.dateToString(currentDateValue);
			}
			catch(Exception ex)
			{
				ErrorHandler.CatchError("Question.readValueFromDb", ex);
				value = "";
			}
		}				
		setValue(value);		
	}
	
}

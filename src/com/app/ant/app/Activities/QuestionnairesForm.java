package com.app.ant.app.Activities;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 23.01.12
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.*;
import com.app.ant.R;
import com.app.ant.app.Activities.MessageBox.MessageBoxButton;
import com.app.ant.app.BusinessLayer.Question;
import com.app.ant.app.BusinessLayer.Questionnaire;
import com.app.ant.app.DataLayer.Db;
import com.app.ant.app.DataLayer.Q;
import com.app.ant.app.ServiceLayer.AntContext;
import com.app.ant.app.ServiceLayer.Convert;
import com.app.ant.app.ServiceLayer.ErrorHandler;
import com.app.ant.app.ServiceLayer.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author perevertaylo.y
 *
 */
public class QuestionnairesForm extends AntActivity
{


    private Questionnaire test = null;
    private int prevPosition = 0;
    private Long visitId = null;
    private Long questId = null;

    private boolean isMandatory = false;
    private boolean byAddress = false;
    private boolean isFromSteps = false;
    private boolean startNextStep = false;

	@Override
    public void onCreate(Bundle savedInstanceState)
    {
    	try
    	{
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        overridePendingTransition(0, 0);

	        final LinearLayout formLayout = new LinearLayout(this);
	        formLayout.setOrientation(LinearLayout.VERTICAL);

			final Spinner spnQuestionnairies = new Spinner(this);
			final ScrollView scrollView = new ScrollView(this);

			Bundle extras = this.getIntent().getExtras();
			if (extras != null)
			{
				visitId = extras.getLong("visitId");
				questId = extras.getLong("questId");
				isMandatory = extras.getBoolean("isMandatory");
				byAddress = extras.getBoolean("byAddress");
				isFromSteps = true;
			}

			if (visitId == null || questId == null)
			{
				visitId = 1L;
				questId = 1L;

				Cursor cur = Db.getInstance().selectSQL("select QuestionnaireID as _id, Name from Questionnaires");
				try
				{
			        String[] from = new String[] { "Name" };
			        int[] to = new int[] { android.R.id.text1 };

					SimpleCursorAdapter curAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cur, from, to);

					curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spnQuestionnairies.setAdapter(curAdapter);
				}
				catch(Exception ex)
				{
					ErrorHandler.CatchError("AndActivity.onCreate", ex);
				}

				spnQuestionnairies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
				{
					@Override public void onItemSelected(AdapterView<?> arg0, View view, final int position, long arg3)
					{
						try
						{
							if (test == null || test.isSaved())
							{
								questId = spnQuestionnairies.getSelectedItemId();
								test = new Questionnaire(questId, AntContext.getInstance().getVisit(), byAddress);
								scrollView.removeAllViews();
								scrollView.addView(getViewByQuestionnaire(test));
								prevPosition = position;
							}
							else
							{
								if (spnQuestionnairies.getSelectedItemId() != test.getId())
								{
									// TODO rework on resources
									MessageBoxButton[] buttons = new MessageBoxButton[] {
											new MessageBoxButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.contact_delete_yes),
													new DialogInterface.OnClickListener()
													{
														@Override
														public void onClick(DialogInterface dialog, int which)
														{
															questId = spnQuestionnairies.getSelectedItemId();
															test = new Questionnaire(questId, AntContext.getInstance().getVisit(), byAddress);
															scrollView.removeAllViews();
															scrollView.addView(getViewByQuestionnaire(test));
															prevPosition = position;
														}
													}),
											new MessageBoxButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.contact_delete_no),
													new DialogInterface.OnClickListener()
													{
														@Override
														public void onClick(DialogInterface dialog, int which)
														{
															spnQuestionnairies.setSelection(prevPosition);
														}
													})};
									MessageBox.show(view.getContext(), "Внимание!", "Данные опроса будут утеряны! Продолжить?", buttons);
								}
							}
						}
						catch(Exception ex)
						{
							ErrorHandler.CatchError("QuestionnairesForm.spnQuestionnairies.onItemSelected", ex);
						}
					}

					@Override public void onNothingSelected(AdapterView<?> arg0){}
				});

		        formLayout.addView(spnQuestionnairies);
			}
			else
			{
				test = new Questionnaire(questId, AntContext.getInstance().getVisit(), byAddress);
				scrollView.removeAllViews();
				scrollView.addView(getViewByQuestionnaire(test));
			}
	        formLayout.addView(scrollView);

			this.setContentView(formLayout);
    	}
    	catch (Exception ex)
    	{
    		MessageBox.show(this, "Error", ex.toString());
    	}
    }


	private View getViewByQuestionnaire(Questionnaire quest)
	{
		final LinearLayout questLayout = new LinearLayout(this);
        questLayout.setOrientation(LinearLayout.VERTICAL);

    	/*TextView tvHeader = new TextView(this);
    	tvHeader.setPadding(10, 0, 10, 16);
    	tvHeader.setBackgroundColor(Color.rgb(255, 255, 200));

    	String text = "<b>" + test.getName() + "</b><br>" + test.getDescription();
    	Spannable sText = (Spannable) Html.fromHtml(text);
    	tvHeader.setText(sText);
    	tvHeader.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
    	questLayout.addView(getLine(Color.BLACK, 2));
    	questLayout.addView(tvHeader);
    	questLayout.addView(getLine(Color.BLACK, 2));*/


        if (quest != null && quest.getQuestions().size() > 0)
        {
        	//
        	// Добавление заголовка опроса и вопросов
        	//
        	quest.addViews(this, questLayout, null);

        	/*for (int i = 0; i < test.getQuestionIds().length; i++)
			{
				if (test.getQuestions() != null)
				{
	        		Long id  = Convert.toLong(test.getQuestionIds()[i], 0);
	        		Question q = test.getQuestions().get(id);

	        		if (q != null && ((q.isParent() && q.getTypeId() > Question.TYPE_NO_ANSWER) || q.isGroup()))
					{
						questLayout.addView(q.getView(this, ACTIVITY_ID));
		        		questLayout.addView(getLine(i < test.getQuestionIds().length - 1 ? Color.LTGRAY : Color.WHITE));
					}
				}
			}*/

        	//
        	//Добавление кнопки "Сохранить"
        	//


        	RelativeLayout footer = new RelativeLayout(this);

        	Button btnSave = new Button(this);
        	btnSave.setText(getResources().getString(R.string.questionnaires_save));
        	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        	params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        	btnSave.setLayoutParams(params);
        	btnSave.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					boolean result = true;
					if (result = test.validate()) result = test.save();

					String success = getResources().getString(R.string.questionnaires_success);
					String failed = getResources().getString(R.string.questionnaires_fail);
					String msg = result ? success : failed;
					String title = getResources().getString(R.string.questionnaires_storecheck);

					MessageBox.show(QuestionnairesForm.this, title, msg);

					if (result)
					{
						startNextStep = isFromSteps;
						onBackPressed();
					}
				}
			});

        	btnSave.setId(1);
        	footer.addView(btnSave);

        	//
        	//Добавление кнопки "Заполнить позже"
        	//

        	if (isFromSteps)
        	{
	        	Button btnPostpone = new Button(this);
	        	btnPostpone.setText(getResources().getString(R.string.questionnaires_postpone));
	        	params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	        	params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	        	params.addRule(RelativeLayout.BELOW, btnSave.getId());
	        	btnPostpone.setLayoutParams(params);
	        	btnPostpone.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
							AntContext.getInstance().getTabController().onNextStepPressed(QuestionnairesForm.this);
					}
				});

	        	footer.addView(btnPostpone);
        	}
        	questLayout.addView(footer);
        }
        return questLayout;

	}

	@Override
	public void onBackPressed()
	{
		boolean result = false;

		if (!isMandatory || test == null)
			result = true;
		else if (test != null && !test.isSaved())
			if (result = test.validate()) result = test.save();

		if ((test != null && test.isSaved()) || result)
		{
    		if(startNextStep)
    			AntContext.getInstance().getTabController().onNextStepPressed(this);
    		else if (isFromSteps)
    			AntContext.getInstance().getStepController().onBackPressed(this);
    		else
    			super.onBackPressed();
		}
		else
		{
			String msg = getResources().getString(R.string.questionnaires_fail);
			Toast.makeText(QuestionnairesForm.this, msg, Toast.LENGTH_SHORT).show();
		}


	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == Questionnaire.ACTIVITY_ID)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");

				String dateDir = Q.getSqlDay(Calendar.getInstance().getTime());
				Long addrId = visitId == null ? 0 : AntContext.getInstance().getAddrID();
				Long qId = data.getExtras().getLong("id");
				String fileName = addrId + "_" + qId + "_" + df.format(Calendar.getInstance().getTime()) + ".jpg";

				byte[] image = AntContext.getInstance().getLastCameraImage();
				File file = FileUtils.saveByteArrayToFile(dateDir, image, fileName);
				if (file != null && file.exists() && file.length() > 1)
				{
					if (qId > 0)
					{
						Question q = test.findQuestion(qId);
						if (q != null)
						{
							q.addAttachedImages(fileName);
							q.updatePhotoInfo();
						}
					}
					MessageBox.show(this, "Photo success!", file.getAbsolutePath() + ": " + Convert.toString(image.length, "") + " bytes"); //TODO resources
				}
				else
				{
					MessageBox.show(this, "Photo failed!", fileName); //TODO resources
				}
				AntContext.getInstance().setLastCameraImage(null);
			}
		}
	}
}

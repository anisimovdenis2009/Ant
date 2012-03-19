package com.app.ant.app.AddressBook.gui.creditask;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.BaseGUIActivity;
import com.app.ant.app.AddressBook.gui.components.*;
import com.app.ant.app.AddressBook.options.Options;
import com.app.ant.app.AddressBook.util.GUIFactory;
import com.app.ant.app.AddressBook.util.IOUtil;
import com.app.ant.app.AddressBook.xmlfeatures.JDEOptions;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 29.09.11
 * Time: 10:02
 * To change this template use File | settings | File Templates.
 */
public class CreditAskController extends BaseGUIActivity implements ItemSaveListener, Preference.OnPreferenceClickListener {

    public static final String D2D = "Количество дней между 2 отгрузками в однй ТТ";
    public static final String PLAN_DAYS = "Плановое количество дней отсрочки";
    public static final String TTQUANTITY = "Количество торговых точек";
    public static final String AVERAGETT = "Средний заказ в ТТ";
    public static final String CURRENT_DEFERRAl = "Текущая отсрочка";
    public static final String CREDIT_LIMIT_CURRENT = "Текущий кредитный лимит";
    public static final String CREDIT_LIMIT_COUNT = "Расчётный кредитный лимит";
    public static final String CREDIT_LIMIT_REQUIRED = "Требуемый кредитный лимит";


    //CreditAskModel m;
    private PreferenceScreen preferenceScreen;
    private CreditAskModel m;

    private MyEditPreference customerCode;
    //private MyListPreference lowFrom;
    private MyEditPreference lowFrom;
    private MyEditPreference name;
    private MyEditPreference sailPointName;
    private MyEditPreference inn;

    private MyListPreference currentDeferral;
    private MyCheckBoxPreference bankExistence;
    private MyEditPreference averageBooking;
    private MyEditPreference salePointsQuantity;
    private MyListPreference planDaysDeferral;
    private MyEditPreference between;
    private MyEditPreference currentCreditLimit;
    private MyPreference calculatedCreditLimit;
    private MyEditPreference requiredCreditLimit;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(Common.CREDIT_ASK_EXTRA))
            m = (CreditAskModel) getIntent().getSerializableExtra(Common.CREDIT_ASK_EXTRA);
        else
            m = new CreditAskModel();
        setM(m);
        setType(1);
        checkAndCreaterUuid();
        init();
        populate();
        setPreferenceScreen(preferenceScreen);
    }

    protected void populate() {
        preferenceScreen.addPreference(customerCode);
        preferenceScreen.addPreference(lowFrom);
        preferenceScreen.addPreference(name);
        preferenceScreen.addPreference(sailPointName);
        preferenceScreen.addPreference(inn);
        preferenceScreen.addPreference(currentDeferral);
        preferenceScreen.addPreference(bankExistence);

        preferenceScreen.addPreference(averageBooking);
        preferenceScreen.addPreference(salePointsQuantity);

        preferenceScreen.addPreference(planDaysDeferral);
        preferenceScreen.addPreference(between);
        preferenceScreen.addPreference(currentCreditLimit);
        preferenceScreen.addPreference(calculatedCreditLimit);
        preferenceScreen.addPreference(requiredCreditLimit);
    }

    protected void init() {
        preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
        int number = 0;
        customerCode = GUIFactory.myEditPreference(this, "Код плательщика", number++);
        customerCode.setSummary(m.getCustomerCode());
        //Counter counter = new Counter(number);
        //First screen(Copy Past From Client card)
        number = firstScreenCP(number);
        //Second screen
        number = secondScreen(number);

        /*//auto
        for (String title : m.getTitles()) {
            MyEditPreference t = new MyEditPreference(this);
            t.setTitle(title);
            t.setNumber(number);
            //t.setOnItemSaveListener(this);
            preferenceScreen.addPreference(t);
            number++;
        }*/
    }

    private int firstScreenCP(int number) {
        lowFrom = GUIFactory.myEditPreference(this, "Организационно-правовая форма", number++);
        lowFrom.setSummary(m.getLawForm());
        lowFrom.setValidation(true);
        lowFrom.setAllCapitals(true);

        /*String lawForm = m.getLawForm();
        if (lawForm != null) {
            lowFrom.setValue(lawForm);
            lowFrom.setSummary(lowFrom.getEntry());
        }*/

        name = new MyEditPreference(this);
        name.setTitle("Наименование. Вводить без ковычек. Для ИП вводит ФИО");
        name.setNumber(number++);
        name.setOnItemSaveListener(this);
        name.setSummary(m.getName());
        name.setValidation(true);

        sailPointName = new MyEditPreference(this);
        sailPointName.setTitle("Название торговой точки");
        sailPointName.setNumber(number++);
        sailPointName.setOnItemSaveListener(this);
        sailPointName.setSummary(m.getSalePointName());
        sailPointName.setValidation(true);

        inn = new MyEditPreference(this);
        if (Options.isRussian) inn.setTitle("ИНН");
        else inn.setTitle("УНП");
        inn.setNumber(number++);
        inn.setOnItemSaveListener(this);
        inn.setSummary(m.getInn());
        return number;
    }

    private int secondScreen(int number) {
        currentDeferral = new MyListPreference(this);
        if (Options.isRussian) {
            currentDeferral.setEntries(JDEOptions.getEntries(9));
            currentDeferral.setEntryValues(JDEOptions.getEnKeys(9));
        } else {
            currentDeferral.setEntries(new String[]{"Оплата по факту", " Банк ПРЕДОПЛАТА", " Кредит 7 дней", " Кредит 14 дней"});
            currentDeferral.setEntryValues(new String[]{"1", "0", "7", "14"});
        }
        currentDeferral.setTitle(CURRENT_DEFERRAl);
        currentDeferral.setOnItemSaveListener(this);
        currentDeferral.setNumber(number++);
        currentDeferral.setOnItemSaveListener(this);
        String currentDeferral1 = m.getCurrentDeferral();
        if (currentDeferral1 != null) {
            currentDeferral.setValue(currentDeferral1);
            currentDeferral.setSummary(currentDeferral.getEntry());
        }

        bankExistence = GUIFactory.myCheckBoxPreference(this, "Способ оплаты", number++, "Наличный", "Банковский");
        bankExistence.setChecked(m.isBankExistence());

        averageBooking = GUIFactory.myEditPreference(this, AVERAGETT, number++);
        averageBooking.setSummary(String.valueOf(m.getAverageBooking()));
        salePointsQuantity = GUIFactory.myEditPreference(this, TTQUANTITY, number++);
        salePointsQuantity.setSummary(String.valueOf(m.getSalePointsQuantity()));

        planDaysDeferral = new MyListPreference(this);
        if (Options.isRussian) {
            planDaysDeferral.setEntries(JDEOptions.getEntries(9));
            planDaysDeferral.setEntryValues(JDEOptions.getEnKeys(9));
        } else {
            planDaysDeferral.setEntries(new String[]{"Оплата по факту", " Банк ПРЕДОПЛАТА", " Кредит 7 дней", " Кредит 14 дней"});
            planDaysDeferral.setEntryValues(new String[]{"1", "0", "7", "14"});
        }

        planDaysDeferral.setTitle(PLAN_DAYS);
        planDaysDeferral.setOnItemSaveListener(this);
        planDaysDeferral.setNumber(number++);
        planDaysDeferral.setOnItemSaveListener(this);
        String planDaysDeferral1 = m.getPlanDaysDeferral();
        if (planDaysDeferral1 != null) {
            planDaysDeferral.setValue(planDaysDeferral1);
            planDaysDeferral.setSummary(planDaysDeferral.getEntry());
        }

        between = GUIFactory.myEditPreference(this, D2D, number++);
        between.setSummary(String.valueOf(m.getBetween()));

        currentCreditLimit = GUIFactory.myEditPreference(this, CREDIT_LIMIT_CURRENT, number++);
        currentCreditLimit.setSummary(m.getCurrentCreditLimit());

        calculatedCreditLimit = GUIFactory.myPreference(this, CREDIT_LIMIT_COUNT);
        calculatedCreditLimit.setOnPreferenceClickListener(this);
        calculatedCreditLimit.setSummary(String.valueOf(m.getCalculatedCreditLimit()));

        requiredCreditLimit = GUIFactory.myEditPreference(this, CREDIT_LIMIT_REQUIRED, number++);
        requiredCreditLimit.setSummary(m.getRequiredCreditLimit()
        );
        return number;
    }


    @Override
    public void onSaveItem(Preference parent, int number, String title) {
        switch (number) {
            case 0:
                m.setCustomerCode(title);
                break;
            case 1:
                m.setLawForm(title);
                break;
            case 2:
                m.setName(title);
                break;
            case 3:
                m.setSalePointName(title);
                break;
            case 4:
                boolean b;
                if (Options.isRussian)
                    b = IOUtil.validateInn(true, this, parent, title);
                else
                    b = IOUtil.validateBELInn(true, this, parent, title);
                if (b)
                    m.setInn(title);
                break;
            case 5:
                m.setCurrentDeferral(((ListPreference) parent).getValue());
                break;
            case 6:
                m.setBankExistence(Boolean.valueOf(title));
                break;
            case 7:
                if (IOUtil.tryToParse(this, parent, title)) {
                    m.setAverageBooking(Integer.parseInt(title));
                    calcLimit();
                }
                break;
            case 8:
                if (IOUtil.tryToParse(this, parent, title)) {
                    m.setSalePointsQuantity(Integer.parseInt(title));
                    calcLimit();
                }
                break;
            case 9:
                m.setPlanDaysDeferral(((ListPreference) parent).getValue());
                break;
            case 10:
                if (IOUtil.tryToParse(this, parent, title)) {
                    m.setBetween(Integer.parseInt(title));
                    calcLimit();
                }
                break;
            case 11:
                m.setCurrentCreditLimit(title);
                break;
            case 12:
                m.setRequiredCreditLimit(title);
                break;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        calcLimit();
        return false;
    }

    private void calcLimit() {
        double v = m.getAverageBooking() * m.getSalePointsQuantity() * 1.3;
        String currentDeferral1 = m.getCurrentDeferral();
        double a = 0;
        if (currentDeferral1 != null) {
            double l = Double.parseDouble(currentDeferral1) / m.getBetween();
            double v1 = l + 1;
            a = v * v1;
        }
        m.setCalculatedCreditLimit((long) a);
        calculatedCreditLimit.setSummary(String.valueOf(a));
    }

    @Override
    public void onSaveItem(int number, Calendar title) {

    }
}

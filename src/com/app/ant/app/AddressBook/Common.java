package com.app.ant.app.AddressBook;

import android.os.Environment;
import com.app.ant.app.AddressBook.gui.clientcard.ClientCardModel;
import com.app.ant.app.AddressBook.gui.creditask.CreditAskModel;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 26.09.11
 * Time: 11:55
 * To change this template use File | settings | File Templates.
 */
public class Common {


    public static final String TAG = "ALIDI";

    //SD-CARD values
    public static final String SENDED = "/sended";
    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String ALIDI_DATA_PATH = SDCARD_PATH + "/alidi";
    public static final String FILE_NAME_PROPERTIES = ALIDI_DATA_PATH + "/proredties.txt";
    public static final String ADRESS_BOOK_APK = "/AdressBook.apk";
    public static final String ALIDI_UPDATE = ALIDI_DATA_PATH + ADRESS_BOOK_APK;
    public static final String ALIDI_OUTBOUND_PATH = ALIDI_DATA_PATH + "/outbound";
    public static final String ALIDI_INBOUND_PATH = ALIDI_DATA_PATH + "/inbound";
    public static final String ALIDI_MESSAGES_PATH = ALIDI_INBOUND_PATH + "/messages";
    public static final String ALIDI_MESSAGES_BUFFER_PATH = ALIDI_INBOUND_PATH + "/buffer";
    public static final String ALIDI_MESSAGES_SENDED_PATH = ALIDI_MESSAGES_PATH + SENDED;

    public static final String CLIENT_CARD_OUTBOUND_PATH = ALIDI_OUTBOUND_PATH + "/clientcard";
    public static final String CLIENT_CARD_OUTBOUND_PATH_SENDED = CLIENT_CARD_OUTBOUND_PATH + SENDED;
    public static final String CREDIT_ASK_OUTBOUND_PATH = ALIDI_OUTBOUND_PATH + "/creditask";
    public static final String CREDIT_ASK_OUTBOUND_PATH_SENDED = CREDIT_ASK_OUTBOUND_PATH + SENDED;

    //GUI
    public static final String ALIDI_GUI_PATH = ALIDI_DATA_PATH + "/gui";
    public static final String CREDIT_CARD_OPTIONS = ALIDI_GUI_PATH + "/creditcard.xml";
    public static final String LAW_ADDRESS_OPTIONS = ALIDI_GUI_PATH + "/lawaddress.xml";
    public static final String ADD_CONTACT = ALIDI_GUI_PATH + "/addcontact.xml";
    public static final String SETTINGS = ALIDI_GUI_PATH + "/settigs.xml";
    public static final String KK = ALIDI_GUI_PATH + "/kk.xml";

    public static final String DELIVERY_PATH = ALIDI_GUI_PATH + "/delivery";
    public static final String KIOSK = DELIVERY_PATH + "/kiosk.xml";
    public static final String SHOP = DELIVERY_PATH + "/shop.xml";
    public static final String MARKET = DELIVERY_PATH + "/market.xml";

    public static final File file = new File("C:\\Alidi\\1.xml");
    public static final File AKML_FILE = new File(Common.ALIDI_DATA_PATH + "/a.xml");
    public static final String AKML_FILE_PATH = AKML_FILE.getAbsolutePath();

    //Extensions
    public static final String JPG_EXTENSION = ".JPG";
    public static final String JPEG_EXTENSION = ".JPEG";
    public static final String PNG_EXTENSION = ".PNG";
    public static final String GIF_EXTENSION = ".GIF";
    public static final String MPO_EXTENSION = ".MPO";
    public static final String JSON_EXTENSION = ".JSON";
    public static final String AKML_EXTENSION = ".AKML";
    public static final String CASH_PATH = "";

    //Database
    public static final String DATABASE_NAME = ALIDI_DATA_PATH + "/AddressBook.db";
    public static final int DATABASE_VERSION = 1;

    //FTPVALUES
    public static final String AK_EXCHANGE = "/AkExchange";
    public static final String AK_EXCHANGE_ANDROID = AK_EXCHANGE + "/android/";
    public static final String AK_EXCHANGE_ANDROID_DELIVERY = AK_EXCHANGE_ANDROID + "delivery/";

    public static final String AK_EXCHANGE_ANDROID_KK = AK_EXCHANGE + "/KK/kk.xml";
    public static final String AK_EXCHANGE_ANDROID_ADDCONTACT = AK_EXCHANGE_ANDROID + "addcontact.xml";
    public static final String AK_EXCHANGE_ANDROID_LAW_ADDRESS = AK_EXCHANGE_ANDROID + "lawaddress.xml";
    public static final String AK_EXCHANGE_ANDROID_KIOSK = AK_EXCHANGE_ANDROID_DELIVERY + "kiosk.xml";
    public static final String AK_EXCHANGE_ANDROID_MARKET = AK_EXCHANGE_ANDROID_DELIVERY + "market.xml";
    public static final String AK_EXCHANGE_ANDROID_SHOP = AK_EXCHANGE_ANDROID_DELIVERY + "shop.xml";


    public static final String AK_EXCHANGE_INBOUND = AK_EXCHANGE + "/inbound/";
    public static final String AK_EXCHANGE_INBOUND_CUSTOMER_CARD = AK_EXCHANGE_INBOUND + "CustomerCard/";
    public static final String AK_EXCHANGE_INBOUND_CREDIT_REQUEST = AK_EXCHANGE_INBOUND + "CreditRequest/";
    public static final String AK_EXCHANGE_OUTBOUND = AK_EXCHANGE + "/outbound/";
    public static final String AK_EXCHANGE_UPDATE = AK_EXCHANGE + "/updates/AdressBook.apk";

    public static final String FTPUSER = "ftpuser";
    public static final String FTP_ADMIN_PASS = "FtpAdmin";
    public static final String FTPHOSTNAME = "81.18.128.74";

    //EXTRAS
    public static final String CONTACT_EXTRA = "CONTACT";
    public static final String CONTACT_EXTRA_RESULT = "CONTACT_RESULT";
    public static final String POINT_TYPE = "POINT_TYPE";
    public static final String LAW_ADDRESS_RESULT_EXTRA = "LARE";
    public static final String LAW_ADDRESS_EDIT_EXTRA = "LAEE";
    public static final String DELIVERY_ADDRESS_EXTRA = "DAE";
    public static final String DELIVERY_ADDRESS_EXTRA_RESULT = "DAER";
    public static final String COMMENT_EXTRA = "COMMENT_EXTRA";
    public static final String JDE_DATA_EXTRA_RESULT = "JDER";
    public static final String JDE_DATA_EXTRA = "JDE";
    public static final String PRICE_EXTRA = "PRICES";
    public static final String CLIENT_CARD_EXTRA = ClientCardModel.class.toString() + "EXTRA";
    public static final String CREDIT_ASK_EXTRA = CreditAskModel.class.toString() + "EXTRA";
    public static final String CLIENT_CARD_EXTRA_CONTACTS = ClientCardModel.class.toString() + "EXTRA_CONTACTS";


    //AKML VALUES

    public static final String MOB_VER = "MobVer";
    public static final String MOB_ID = "Mob_Id";
    public static final String ID = "Id";

    public static final String F_UFLAG = "f_Uflag";
    public static final String F_FORM_OF_LAW = "f_FormOfLaw";
    public static final String F_NAME = "f_Name";
    public static final String F_NAME_SALES_POINT = "f_NameSalesPoint";
    public static final String F_NET_ATTRIBUTE = "f_NetAttribute";
    public static final String F_OGRN = "f_Ogrn";
    public static final String F_INN = "f_Inn";
    public static final String F_KPP = "f_Kpp";
    public static final String F_IS_NEW = "f_IsNew";
    public static final String POST_INDEX = "PostIndex";
    public static final String REGION = "Region";
    public static final String DISTRICT = "District";
    public static final String CITY = "City";
    public static final String STREET = "Street";
    public static final String HOUSE = "House";
    public static final String CORPS = "Corps";
    public static final String FLAT = "Flat";
    public static final String OFFICE = "Office";
    public static final String FACT_POST_INDEX = "FactPostIndex";
    public static final String FACT_REGION = "FactRegion";
    public static final String FACT_DISTRICT = "FactDistrict";
    public static final String FACT_CITY = "FactCity";
    public static final String F_POINT_TYPE = "f_PointType";

    public static final String SC_NAME = "SCName";
    public static final String SC_FLOOR = "SCFloor";
    public static final String SC_SHOP_NUM = "SCShopNum";
    public static final String PT_MARKET_NUM = "PtMarketNum";
    public static final String MARKET_NAME = "MarketName";
    public static final String FACT_HOUSE = "FactHouse";
    public static final String FACT_STREET = "FactStreet";
    public static final String FACT_CORP = "FactCorp";
    public static final String EX_GUID = "ExGuid";

    public static final String INIT_CUSTOMER = "InitCustomer";
    public static final String CUSTOMER_PHONE = "CustomerPhone";
    public static final String CUSTOMER_G_PHONE = "CustomerGPhone";
    public static final String CUSTOMER_EMAIL = "CustomerEmail";

    public static final String PRICE_NAME = "PriceName";
    public static final String CALC_PRICE = "CalcPrice";
    public static final String EXPIRATION_LICENSE = "ExpirationLicense";
    public static final String LICENSE_DATE = "LicenseDate";
    public static final String LICENSE_SERIE = "LicenseSerie";
    public static final String LICENSE_NUM = "LicenseNum";
    public static final String ISSUE = "issue";

    public static final String QNTOULET = "Qntoulet";
    public static final String COVERING_TYPE_04 = "CoveringType04";
    public static final String COVERING_TYPE_06 = "CoveringType06";
    public static final String CHANEL_TYPE_NESTLE = "ChanelTypeNestle";
    public static final String CUST_TYPE_PG = "CustTypePG";
    public static final String SECTOR_PG = "SectorPG";
    public static final String BELONG_CUST = "BelongCust";
    public static final String REMOTE_FILIAL = "RemoteFilial";
    public static final String CHANEL_TYPE_PG = "ChanelTypePG";
    public static final String CHANEL_TYPE_PURINA = "ChanelTypePurina";
    public static final String TOP_CUST = "TopCust";
    public static final String SUPPLIER_CODE = "SupplierCode";
    public static final String F_CHANEL_ALK_MIX = "f_ChanelAlkMix";
    public static final String KK_08 = "KK08";
    public static final String F_PRINT_COMPLECT = "f_PrintComplect";
    public static final String F_COMMENT = "f_Comment";
    public static final String CUSTOMER_CARD = "CustomerCard";

    //Constants
    public static final String SHOP_CONST = "Магазин";
    public static final String KIOSK_CONST = "Киоск или Минимаркет";
    public static final String MARKET_CONST = "Точка на рынке";
}

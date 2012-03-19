package com.app.ant.app.AddressBook.xmlfeatures;

import android.location.Location;
import android.os.Parcelable;
import com.app.ant.app.AddressBook.Common;
import com.app.ant.app.AddressBook.gui.BaseModel;
import com.app.ant.app.AddressBook.gui.DataModel;
import com.app.ant.app.AddressBook.gui.clientcard.ClientCardController;
import com.app.ant.app.AddressBook.gui.clientcard.ClientCardModel;
import com.app.ant.app.AddressBook.gui.clientcard.jdeoptions.JDEDataModel;
import com.app.ant.app.AddressBook.gui.clientcard.pricesandlicenses.PriceAndLicenseModel;
import com.app.ant.app.AddressBook.options.Options;
import com.app.ant.app.AddressBook.pojos.Contact;
import com.app.ant.app.AddressBook.pojos.DeliveryAddress;
import com.app.ant.app.AddressBook.pojos.LawAddress;
import com.app.ant.app.AddressBook.pojos.delivery.Kiosk;
import com.app.ant.app.AddressBook.pojos.delivery.Market;
import com.app.ant.app.AddressBook.pojos.delivery.Shop;
import org.jdom.Document;
import org.jdom.Element;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: anisimov.da
 * Date: 10.10.11
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class ClientCardXMLCreator extends BaseXMLCreator {
    public static final String GPS_COORDINATES = "GPS Coordinates";

    Object model;

    //First Screen
    Element f_Uflag;
    Element f_FormOfLaw;
    Element f_Name;
    Element f_NameSalesPoint;
    Element f_NetAttribute;
    Element f_Ogrn;
    Element f_Inn;
    Element f_Kpp;
    Element f_IsNew;

    //Second Screen
    //If point is new we have this
    Element postIndex;
    Element region;
    Element district;
    Element city;
    Element street;

    Element house;
    Element corps;
    Element flat;
    Element office;

    //Third screen
    Element factPostIndex;
    Element factRegion;
    Element factDistrict;
    Element factCity;
    Element f_PointType;

    //Delivary adrress and contacts
    Element sCName;
    Element sCFloor;
    Element sCShopNum;
    Element ptMarketNum;
    Element marketName;
    Element factHouse;
    Element factStreet;
    Element factCorp;
    Element exGuid;

    //Fiveth screen
    Element initCustomer;
    Element customerPhone;
    Element customerGPhone;
    Element customerEmail;

    //Sixth screen
    Element priceName;
    Element calcPrice;
    Element expirationLicense;
    Element licenseDate;
    Element licenseSerie;
    Element licenseNum;
    Element issue;

    /*JDE KK*/
    Element qntoulet;
    Element coveringType04;
    Element coveringType06;
    Element chanelTypeNestle;
    Element custTypePG;
    Element sectorPG;
    Element belongCust;
    Element remoteFilial;
    Element chanelTypePG;
    Element chanelTypePurina;

    Element topCust;
    Element supplierCode;
    Element f_ChanelAlkMix;
    Element kK08;
    Element f_PrintComplect;
    Element f_Comment;



    public ClientCardXMLCreator(BaseModel model) {
        super(model, Common.CUSTOMER_CARD);
        this.model = model;
        init();
        populate();
    }

    protected void init() {
        id = new Element(Common.ID);
        mob_Id = new Element(Common.MOB_ID);
        mobVer = new Element(Common.MOB_VER);

        //First Screen
        f_Uflag = new Element(Common.F_UFLAG);
        f_FormOfLaw = new Element(Common.F_FORM_OF_LAW);
        f_Name = new Element(Common.F_NAME);
        f_NameSalesPoint = new Element(Common.F_NAME_SALES_POINT);
        f_NetAttribute = new Element(Common.F_NET_ATTRIBUTE);
        f_Ogrn = new Element(Common.F_OGRN);
        f_Inn = new Element(Common.F_INN);
        f_Kpp = new Element(Common.F_KPP);
        f_IsNew = new Element(Common.F_IS_NEW);

        //Second Screen
        //If point is new we have this
        postIndex = new Element(Common.POST_INDEX);
        region = new Element(Common.REGION);
        district = new Element(Common.DISTRICT);
        city = new Element(Common.CITY);
        street = new Element(Common.STREET);

        house = new Element(Common.HOUSE);
        corps = new Element(Common.CORPS);
        flat = new Element(Common.FLAT);
        office = new Element(Common.OFFICE);

        //Third screen
        factPostIndex = new Element(Common.FACT_POST_INDEX);
        factRegion = new Element(Common.FACT_REGION);
        factDistrict = new Element(Common.FACT_DISTRICT);
        factCity = new Element(Common.FACT_CITY);
        f_PointType = new Element(Common.F_POINT_TYPE);

        //Delivary adrress and contacts
        sCName = new Element(Common.SC_NAME);
        sCFloor = new Element(Common.SC_FLOOR);
        sCShopNum = new Element(Common.SC_SHOP_NUM);
        ptMarketNum = new Element(Common.PT_MARKET_NUM);
        marketName = new Element(Common.MARKET_NAME);
        factHouse = new Element(Common.FACT_HOUSE);
        factStreet = new Element(Common.FACT_STREET);
        factCorp = new Element(Common.FACT_CORP);
        exGuid = new Element(Common.EX_GUID);

        //Fiveth screen
        initCustomer = new Element(Common.INIT_CUSTOMER);
        customerPhone = new Element(Common.CUSTOMER_PHONE);
        customerGPhone = new Element(Common.CUSTOMER_G_PHONE);
        customerEmail = new Element(Common.CUSTOMER_EMAIL);

        //Sixth screen
        priceName = new Element(Common.PRICE_NAME);
        calcPrice = new Element(Common.CALC_PRICE);
        expirationLicense = new Element(Common.EXPIRATION_LICENSE);
        licenseDate = new Element(Common.LICENSE_DATE);
        licenseSerie = new Element(Common.LICENSE_SERIE);
        licenseNum = new Element(Common.LICENSE_NUM);
        issue = new Element(Common.ISSUE);

        /*JDE KK*/
        qntoulet = new Element(Common.QNTOULET);
        coveringType04 = new Element(Common.COVERING_TYPE_04);
        coveringType06 = new Element(Common.COVERING_TYPE_06);
        chanelTypeNestle = new Element(Common.CHANEL_TYPE_NESTLE);
        custTypePG = new Element(Common.CUST_TYPE_PG);
        sectorPG = new Element(Common.SECTOR_PG);
        belongCust = new Element(Common.BELONG_CUST);
        remoteFilial = new Element(Common.REMOTE_FILIAL);
        chanelTypePG = new Element(Common.CHANEL_TYPE_PG);
        chanelTypePurina = new Element(Common.CHANEL_TYPE_PURINA);

        topCust = new Element(Common.TOP_CUST);
        supplierCode = new Element(Common.SUPPLIER_CODE);
        f_ChanelAlkMix = new Element(Common.F_CHANEL_ALK_MIX);
        kK08 = new Element(Common.KK_08);
        f_PrintComplect = new Element(Common.F_PRINT_COMPLECT);
        f_Comment = new Element(Common.F_COMMENT);
    }


    protected void populate() {
        ClientCardModel m = (ClientCardModel) model;
        firstScreen(m, root);
        //Law Address Second screen
        secondScreen(m, root);
        //Fact Address third screen
        thirdScreen(m, root);
        //Fact Add Fourth screen
        fouthScreen(m, root);
        //Contacts
        fivethScreen(m, root);

        //Sixth screen
        sixthScreen(m, root);

        //Seventh screen Attribute
        JDEDataModel jdeDataModel = m.getJdeDataModel();
        if (jdeDataModel != null) {
            if (jdeDataModel.getAutlet() != null) {
                qntoulet.addContent(jdeDataModel.getAutlet());
                root.addContent(qntoulet);
            }
            if (jdeDataModel.getKk04() != null) {
                coveringType04.setText(jdeDataModel.getKk04());
                root.addContent(coveringType04);
            }
            if (jdeDataModel.getKk06() != null) {
                coveringType06.setText(jdeDataModel.getKk06());
                root.addContent(coveringType06);
            }
            if (jdeDataModel.getKkNestle() != null) {
                chanelTypeNestle.setText(jdeDataModel.getKkNestle());
                root.addContent(chanelTypeNestle);
            }
            if (jdeDataModel.getKkPG() != null) {
                custTypePG.setText(jdeDataModel.getKkPG());
                root.addContent(custTypePG);
            }
            if (jdeDataModel.getKkSectPG() != null) {
                sectorPG.setText(jdeDataModel.getKkSectPG());
                root.addContent(sectorPG);
            }
            if (jdeDataModel.getKk22() != null) {
                belongCust.setText(jdeDataModel.getKk22());
                root.addContent(belongCust);
            }
            if (jdeDataModel.getKk24() != null) {
                remoteFilial.setText(jdeDataModel.getKk24());
                root.addContent(remoteFilial);
            }
            if (jdeDataModel.getKk25() != null) {
                chanelTypePG.setText(jdeDataModel.getKk25());
                root.addContent(chanelTypePG);
            }
            if (jdeDataModel.getKk26() != null) {
                chanelTypePurina.setText(jdeDataModel.getKk26());
                root.addContent(chanelTypePurina);
            }
            if (jdeDataModel.getKk28() != null) {
                topCust.setText(jdeDataModel.getKk28());
                root.addContent(topCust);
            }
            if (jdeDataModel.getShipingCode() != null) {
                supplierCode.setText(jdeDataModel.getShipingCode());
                root.addContent(supplierCode);
            }
            if (jdeDataModel.getKk14() != null) {
                f_ChanelAlkMix.setText(jdeDataModel.getKk14());
                root.addContent(f_ChanelAlkMix);
            }
            if (jdeDataModel.getKk08() != null) {
                kK08.setText(jdeDataModel.getKk08());
                root.addContent(kK08);
            }
            if (jdeDataModel.getKk20() != null) {
                f_PrintComplect.setText(jdeDataModel.getKk20());
                root.addContent(f_PrintComplect);
            }
        }

        String comment = m.getComment();
        f_Comment.setText(comment);
        root.addContent(f_Comment);

        Location gpsLocation = ClientCardController.gpsLocation;
        if (gpsLocation != null) {
            Element latitude = new Element("Latitude");
            latitude.setText(String.valueOf(gpsLocation.getLatitude()));
            Element longtitude = new Element("Longitude");
            longtitude.setText(String.valueOf(gpsLocation.getLongitude()));
            root.addContent(latitude);
            root.addContent(longtitude);
        }
    }

    private void sixthScreen(ClientCardModel m, Element root) {
        PriceAndLicenseModel model = m.getPriceAndLicenseModel();
        if (!Options.isRussian) {
            priceName.setText("BPG_01");
            root.addContent(priceName);
        } else if (model != null) {
            priceName.setText(model.getPriseColumn());
            root.addContent(priceName);

            if (model.isDiscount()) {
                calcPrice.setText(String.valueOf(model.isDiscount()));
                root.addContent(calcPrice);
            }

            Calendar licenseStart = model.getLicenseStart();
            if (licenseStart != null) {
                String date = licenseStart.get(Calendar.YEAR) + "-" + licenseStart.get(Calendar.MONTH) + "-" +
                        licenseStart.get(Calendar.DAY_OF_MONTH) + "-" + "T00:00:00";
                licenseDate.setText(date);
                root.addContent(licenseDate);
            }

            Calendar licenseEnd = model.getLicenseEnd();
            if (licenseEnd != null) {
                String date = licenseEnd.get(Calendar.YEAR) + "-" + licenseEnd.get(Calendar.MONTH) + "-" +
                        licenseEnd.get(Calendar.DAY_OF_MONTH) + "-" + "T00:00:00";
                expirationLicense.setText(date);
                root.addContent(expirationLicense);
            }

            if (model.getLicenseIssue() != null) {
                issue.setText(model.getLicenseIssue());
                root.addContent(issue);
            }
            if (model.getLicenseNumber() != null) {
                licenseNum.setText(model.getLicenseNumber());
                root.addContent(licenseNum);
            }
            if (model.getLicenseSerial() != null) {
                licenseSerie.setText(model.getLicenseSerial());
                root.addContent(licenseSerie);
            }
        }
    }

    private void fivethScreen(ClientCardModel m, Element root) {
        if (m.getContacts() != null) {
            String contactName = null;
            String contactTel = null;
            String contactMTel = null;
            String contactMail = null;
            for (Parcelable contact : m.getContacts()) {

                String firstLastName = ((Contact) contact).getFirstLastName();
                if (firstLastName != null)
                    contactName = firstLastName + "|";

                String telephone = ((Contact) contact).getTelephone();
                if (telephone != null)
                    contactTel = telephone + "|";

                String mobileTelephone = ((Contact) contact).getMobileTelephone();
                if (mobileTelephone != null)
                    contactMTel = mobileTelephone + "|";

                String email = ((Contact) contact).getEmail();
                if (email != null)
                    contactMail = email + "|";
            }

            if (contactName != null) {
                initCustomer.setText(contactName);
                root.addContent(initCustomer);
            }
            if (contactTel != null) {
                customerPhone.setText(contactTel);
                root.addContent(customerPhone);
            }
            if (contactMail != null) {
                customerEmail.setText(contactMail);
                root.addContent(customerEmail);
            }
            if (contactMTel != null) {
                customerGPhone.setText(contactMTel);
                root.addContent(customerGPhone);
            }
        }
    }

    private void fouthScreen(ClientCardModel m, Element root) {

        DeliveryAddress deliveryAddress = m.getDeliveryAddress();
        if (deliveryAddress != null) {
            String indeks = deliveryAddress.getIndeks();
            if (indeks != null) {
                factPostIndex.setText(indeks);
                root.addContent(factPostIndex);
            }

            String area = deliveryAddress.getArea();
            if (area != null) {
                factRegion.setText(area);
                root.addContent(factRegion);
            }

            String department = deliveryAddress.getDepartment();
            if (department != null) {
                factDistrict.setText(department);
                root.addContent(factDistrict);
            }

            String city1 = deliveryAddress.getCity();
            if (city1 != null) {
                factCity.setText(city1);
                root.addContent(factCity);
            }
            switch (deliveryAddress.getType()) {
                case DeliveryAddress.SHOP: {
                    String ctstreet = ((Shop) m.getDeliveryAddress().deliveryAddress).getStreet();
                    if (ctstreet != null) {
                        factStreet.setText(ctstreet);
                        root.addContent(factStreet);
                    }
                    String cthouse = ((Shop) m.getDeliveryAddress().deliveryAddress).getHouse();
                    if (cthouse != null) {
                        factHouse.setText(cthouse);
                        root.addContent(factHouse);
                    }
                    String ctcorp = ((Shop) m.getDeliveryAddress().deliveryAddress).getBuilding();
                    if (ctcorp != null) {
                        factCorp.setText(ctcorp);
                        root.addContent(factCorp);
                    }
                    String name = ((Shop) m.getDeliveryAddress().deliveryAddress).getName();
                    if (name != null) {
                        sCName.setText(name);
                        root.addContent(sCName);
                    }
                    String floor = ((Shop) m.getDeliveryAddress().deliveryAddress).getFloor();
                    if (floor != null) {
                        sCFloor.setText(floor);
                        root.addContent(sCFloor);
                    }
                    String shopnum = ((Shop) m.getDeliveryAddress().deliveryAddress).getNumber();
                    if (shopnum != null) {
                        sCShopNum.setText(shopnum);
                        root.addContent(sCShopNum);
                    }
                }
                break;
                case DeliveryAddress.KIOSK: {
                    String tstreet = ((Kiosk) m.getDeliveryAddress().deliveryAddress).getStreet();
                    if (tstreet != null) {
                        factStreet.setText(tstreet);
                        root.addContent(factStreet);
                    }
                    String thouse = ((Kiosk) m.getDeliveryAddress().deliveryAddress).getHouse();
                    if (thouse != null) {
                        factHouse.setText(thouse);
                        root.addContent(factHouse);
                    }
                    String tcorp = ((Kiosk) m.getDeliveryAddress().deliveryAddress).getBuilding();
                    if (tcorp != null) {
                        factCorp.setText(tcorp);
                        root.addContent(factCorp);
                    }
                    String uid = ((Kiosk) m.getDeliveryAddress().deliveryAddress).getOrientation();
                    if (uid != null) {
                        exGuid.setText(uid);
                        root.addContent(exGuid);
                    }
                }
                break;
                case DeliveryAddress.MARKET: {
                    String rketname = ((Market) m.getDeliveryAddress().deliveryAddress).getMarket();
                    if (rketname != null) {
                        marketName.setText(rketname);
                        root.addContent(marketName);
                    }
                    String ctstreet = ((Market) m.getDeliveryAddress().deliveryAddress).getStreet();
                    if (ctstreet != null) {
                        factStreet.setText(ctstreet);
                        root.addContent(factStreet);
                    }
                    String cthouse = ((Market) m.getDeliveryAddress().deliveryAddress).getHouse();
                    if (cthouse != null) {
                        factHouse.setText(cthouse);
                        root.addContent(factHouse);
                    }
                    String marketnum = ((Market) m.getDeliveryAddress().deliveryAddress).getBuilding();
                    if (marketnum != null) {
                        ptMarketNum.setText(marketnum);
                        root.addContent(ptMarketNum);
                    }

                    String guid = ((Market) m.getDeliveryAddress().deliveryAddress).getOrientation();
                    if (guid != null) {
                        exGuid.setText(guid);
                        root.addContent(exGuid);
                    }
                }
            }
        }
    }

    private void thirdScreen(ClientCardModel m, Element root) {
/*
        String indeks = m.getIndeks();
        if (indeks != null) {
            factPostIndex.setText(indeks);
            root.addContent(factPostIndex);
        }

        if (m.getArea() != null) {
            factRegion.setText(m.getArea());
            root.addContent(factRegion);
        }

        if (m.getDepartment() != null) {
            factDistrict.setText(m.getDepartment());
            root.addContent(factDistrict);
        }

        if (m.getCity() != null) {
            factCity.setText(m.getCity());
            root.addContent(factCity);
        }
*/

        f_PointType.setText(m.getPointTypeString());
        root.addContent(f_PointType);
    }


    private void secondScreen(ClientCardModel m, Element root) {
        LawAddress address = m.getAddress();
        if (address != null) {
            if (address.getIndeks() != null) {
                postIndex.setText(address.getIndeks());
                root.addContent(postIndex);
            }
            if (address.getCity() != null) {
                city.setText(address.getCity());
                root.addContent(city);
            }
            if (address.getDepartment() != null) {
                district.setText(address.getDepartment());
                root.addContent(district);
            }
            if (address.getArea() != null) {
                region.setText(address.getArea());
                root.addContent(region);
            }
            if (address != null) {
                street.setText(address.getStreet());
                root.addContent(street);
            }
            if (address.getHouse() != null) {
                house.setText(address.getHouse());
                root.addContent(house);
            }
            if (address.getBuilding() != null) {
                corps.setText(address.getBuilding());
                root.addContent(corps);
            }
            if (address.getAppartment() != null) {
                flat.setText(address.getAppartment());
                root.addContent(flat);
            }
            if (address.getOffice() != null) {
                office.setText(address.getOffice());
                root.addContent(office);
            }

        }
    }

    private void firstScreen(ClientCardModel m, Element root) {
        f_Uflag.setText(String.valueOf(m.isType()));
        root.addContent(f_Uflag);

        f_FormOfLaw.setText(m.getLawForm());
        root.addContent(f_FormOfLaw);

        f_Name.setText(m.getName());
        root.addContent(f_Name);

        if (m.getSalePointName() != null) {
            f_NameSalesPoint.setText(m.getSalePointName());
            root.addContent(f_NameSalesPoint);
        }

        String nationalNet = m.getNationalNet();
        if (nationalNet != null) {
            f_NetAttribute.setText(nationalNet);
            root.addContent(f_NetAttribute);
        }

        String ogrn = m.getOgrn();
        if (ogrn != null) {
            f_Ogrn.setText(ogrn);
            root.addContent(f_Ogrn);
        }

        f_Inn.setText(m.getInn());
        root.addContent(f_Inn);

        String kpp = m.getKpp();
        if (kpp != null) {
            f_Kpp.setText(kpp);
            root.addContent(f_Kpp);
        }

        if (m.isNewPoint()) {
            String text = String.valueOf(m.isNewPoint());
            f_IsNew.setText(text);
            root.addContent(f_IsNew);
        }
    }

    public Document getDocument() {
        return document;
    }

}

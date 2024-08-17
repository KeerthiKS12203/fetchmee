package com.example.qrcodescanner2;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class DataObject {
    private String QRString;
    private String latitude, longitude;
    private String dateTime;
    private String mode;
    private String comment;
    private String param1, meterMSN, meterYearMonth;

    public DataObject(String  QRString, String latitude, String longitude, String mode, String comment,String dateTime) {
        this.QRString = QRString;
        this.latitude = latitude;
        this.longitude = longitude;
        this.comment = comment;
        this.mode=mode;
        this.dateTime = dateTime;
    }

    public DataObject(String  param1, String make,String model, String meterMSN, String meterYearMonth, String latitude, String longitude, String mode, String comment, String dateTime) {

        this.QRString = param1+make+model+meterMSN+meterYearMonth;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mode=mode;
        this.comment=comment;
        this.dateTime = dateTime;
    }

    public JSONObject createJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("qrstring", QRString);
        jsonObject.put("lat", latitude);
        jsonObject.put("lng", longitude);
        jsonObject.put("mode",mode);
        jsonObject.put("comment", comment);
        jsonObject.put("scantime", dateTime);
        return jsonObject;
    }
}

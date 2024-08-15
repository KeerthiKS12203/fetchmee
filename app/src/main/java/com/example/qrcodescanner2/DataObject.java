package com.example.qrcodescanner2;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class DataObject {
    private String QRString;
    private String latitude, longitude;
    private String dateTime;
    private String mode;
    private String param1, meterMSN, meterYearMonth;

    public DataObject(String  QRString, String latitude, String longitude, String mode,String dateTime) {
        this.QRString = QRString;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mode=mode;
        this.dateTime = dateTime;
    }

    public DataObject(String  param1, String meterMSN, String meterYearMonth, String latitude, String longitude, String mode, String dateTime) {

        this.QRString = param1+meterMSN+meterYearMonth;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mode=mode;
        this.dateTime = dateTime;
    }

    public JSONObject createJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("qrstring", QRString);
        jsonObject.put("lat", latitude);
        jsonObject.put("lng", longitude);
        jsonObject.put("mode",mode);
        jsonObject.put("scantime", dateTime);
        return jsonObject;
    }
}

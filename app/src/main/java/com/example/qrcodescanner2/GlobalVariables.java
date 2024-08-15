package com.example.qrcodescanner2;

import android.app.Application;

public class GlobalVariables  extends Application {
    private static GlobalVariables instance;

    private String AndroidId;
    private String MeterRatings;
    private String CurrentLocation;
    private String SERVER_URL;
    private String Server_domain_name;
    private String Server_port_no;
    private double CurrentLatitude, CurrentLongitude;
    private String AdminUserId="fetchmee";
    private String AdminPassword="fetchmee";

    public String getHTTP_protocol() {
        return HTTP_protocol;
    }

    public void setHTTP_protocol(String HTTP_protocol) {
        this.HTTP_protocol = HTTP_protocol;
    }

    private String HTTP_protocol;


    public String getAdminPassword() {
        return AdminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        AdminPassword = adminPassword;
    }


    public String getAdminUserId() {
        return AdminUserId;
    }

    public void setAdminUserId(String adminUserId) {
        AdminUserId = adminUserId;
    }


    public String getBaseURL() {
        return HTTP_protocol+"://"+Server_domain_name+":"+Server_port_no;
    }

    public double getCurrentLongitude() {
        return CurrentLongitude;
    }

    public void setCurrentLongitude(double currentLongitude) {
        CurrentLongitude = currentLongitude;
    }

    public double getCurrentLatitude() {
        return CurrentLatitude;
    }

    public void setCurrentLatitude(double currentLatitude) {
        CurrentLatitude = currentLatitude;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        instance=this;
        // Initialize the global variable here
        AndroidId = null;

        CurrentLocation=null;
        Server_domain_name="devmdas.fetchmee.in";
        Server_port_no="15000";
        HTTP_protocol="http";
        SERVER_URL="http://devmdas.fetchmee.in:15000/androidId/info/gps/1";

    }
    public static GlobalVariables getInstance() {
        return instance;
    }

    public String getAndroidId() {
        return AndroidId;
    }

    public void setAndroidId(String value) {
        AndroidId = value;
        SERVER_URL=HTTP_protocol+ "://"+Server_domain_name+":"+Server_port_no+"/"+AndroidId+"/info/gps/1";
    }

    public String getMeterRatings() {
        return MeterRatings;
    }

    public void setMeterRatings(String meterRatings) {
        MeterRatings = meterRatings;
    }

    public String getCurrentLocation() {
        return CurrentLocation;
    }

    public void setCurrentLocation(String value) {
        CurrentLocation = value;
    }

    public String getSERVER_URL() {
        return SERVER_URL;
    }

    public void setSERVER_URL(String SERVER_URL) {
        this.SERVER_URL = SERVER_URL;
//        SERVER_URL=HTTP_protocol+"://"+Server_domain_name+":"+Server_port_no+"/"+AndroidId+"/info/gps/1";
    }

    public String getServer_domain_name() {
        return Server_domain_name;
    }

    public void setServer_domain_name(String server_domain_name) {
        Server_domain_name = server_domain_name;
        SERVER_URL=HTTP_protocol+"://"+Server_domain_name+":"+Server_port_no+"/"+AndroidId+"/info/gps/1";
    }

    public String getServer_port_no() {
        return Server_port_no;
    }

    public void setServer_port_no(String server_port_no) {
        Server_port_no = server_port_no;
        SERVER_URL=HTTP_protocol+"://"+Server_domain_name+":"+Server_port_no+"/"+AndroidId+"/info/gps/1";
    }

}
package com.example.qrcodescanner2;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//public class MainActivity extends AppCompatActivity {
//
//    FusedLocationProviderClient fusedLocationProviderClient;
//    Button scan_btn, manual_page_nav;
//    TextView text_api_str, text_str, text_loc, text_android_id;
//    String str, str_loc, loc;
//    boolean locObtained = false, strObtained = false;
//    int i = 0;
//
////    http://devmdas.fetchmee.in:15000/keerthi/info/gps/1
//    private static String SERVER_URL;
//    private String qrCodeString;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        scan_btn = findViewById(R.id.scanner);
//        manual_page_nav = findViewById(R.id.manual_page_nav);
//        text_api_str = findViewById(R.id.text_api_str);
//        text_loc = findViewById(R.id.text_loc);
//        text_str = findViewById(R.id.text_str);
//        text_android_id=findViewById(R.id.text_android_id);
//        text_loc.setText("Location");
//        text_str.setText("QR String");
//        text_api_str.setText("API call:Not Made");
//        text_android_id.setText("android id not obtained");
//
//        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
//
//
//        GlobalVariables globalVariables = GlobalVariables.getInstance();
//        text_android_id.setText(globalVariables.getAndroidId());
//
//        String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
//        globalVariables.setAndroidId(androidId);
//        text_android_id.setText(globalVariables.getAndroidId());
//        //concatenate android id to server url SERVER_URL=http://devmdas.fetchmee.in:15000/androidId/info/gps/1
//        SERVER_URL=globalVariables.getSERVER_URL();
//        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//            }, 100);
//        }
//
//        scan_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
//                intentIntegrator.setPrompt("Scan a QR Code");
//                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
//                intentIntegrator.setOrientationLocked(true);
//                intentIntegrator.initiateScan();
//
//            }
//        });
//
//        manual_page_nav.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, ManualDataEntry.class);
//                startActivity(intent);
//            }
//        });
//    }
//
//    //obtaining the string result
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//
//        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (intentResult != null) {
//            str = intentResult.getContents();
////            text_api_str.setText(str);
//            i=0;
////            getLocation();
//            if (str != null ) {
////                str_loc = str + loc;
//                text_str.setText(str);
//                GlobalVariables globalVariables=GlobalVariables.getInstance();
//                globalVariables.setMeterRatings(str);
//                strObtained=true;
//                getLastLocation();
////                text_api_str.setText(str_loc);
////                sendScannedDataToServer(str_loc);
//
//            } else {
//                System.out.println("String is null");
//                super.onActivityResult(requestCode, resultCode, data);
//            }
//        }
//    }
//
//    private void getLastLocation() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
////            fusedLocationProviderClient.getLastLocation()
////                    .addOnSuccessListener(new OnSuccessListener<Location>() {
////                        @Override
////                        public void onSuccess(Location location) {
////                            if(location!=null){
////                                loc="Latitude:"+location.getLatitude()+"Longitude:"+location.getLongitude();
////                                text_loc.setText(loc);
////                                str_loc=str+loc;
////                                text_api_str.setText(str_loc);
////                            }
////                        }
////                    });
//            // Request high accuracy location
//            Task<Location> locationTask = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null);
//
//// Add a callback to handle the location result
//            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    // Handle the location result
//                    if(location!=null){
//                        loc="Latitude:"+location.getLatitude()+"Longitude:"+location.getLongitude();
//                        GlobalVariables globalVariables=GlobalVariables.getInstance();
//                        globalVariables.setCurrentLocation(loc);
//                        text_loc.setText(globalVariables.getCurrentLocation());
////                        str_loc=str+loc;
////                        text_api_str.setText(str_loc);
////                        sendScannedDataToServer(str_loc);
//                        try {
//                            sendScannedDataToServer(str, String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//
//            });
//
//// Add a callback to handle any errors
//            locationTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    // Handle the error
//                    Log.e("Location", "Error getting location", e);
//                }
//            });
//        }else{
//
//        }
//    }
//
//    private void sendScannedDataToServer(String scannedData, String lat, String lng) throws JSONException {
//        OkHttpClient client = new OkHttpClient();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy HHmmss");
//        // Format the current date and time using the SimpleDateFormat
//        String scanTimeDate = dateFormat.format(new Date());
//        String mode="S";
//        GlobalVariables globalVariables=GlobalVariables.getInstance();
//
//        DataObject wrapper = new DataObject(scannedData, lat,lng, mode,scanTimeDate);
//        JSONObject jsonObject = wrapper.createJsonObject();
//        text_api_str.setText(jsonObject.toString());
//        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json"));
//
//        Request request = new Request.Builder()
//                .url(globalVariables.getSERVER_URL())
//                .post(requestBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    final String responseBody = response.body().string();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            System.out.println("Server response: " + response);
//                            Toast.makeText(MainActivity.this, "Server response: " + response, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            Toast.makeText(MainActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
//                            System.out.println("Server Response: " + response);
//                            Toast.makeText(MainActivity.this, "Server response: " + response, Toast.LENGTH_SHORT).show();
//
//
//                        }
//                    });
//                }
//            }
//        });
//    }
//
//}


//-----------------------------------


public class MainActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    MyViewPageAdapter myViewPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        myViewPageAdapter =new MyViewPageAdapter(this);
        viewPager2.setAdapter(myViewPageAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });



    }
}
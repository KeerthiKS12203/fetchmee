package com.example.qrcodescanner2.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qrcodescanner2.DataObject;
import com.example.qrcodescanner2.GlobalVariables;
import com.example.qrcodescanner2.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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


public class ScanFragment extends Fragment {

    FusedLocationProviderClient fusedLocationProviderClient;
    Button scan_btn;
//    Button manual_page_nav;
    TextView text_api_str, text_str, text_loc, text_android_id, text_api_call_status, api_call_status_label, api_call_status_colon;
    String str, str_loc, loc;
    boolean locObtained = false, strObtained = false;
    int i = 0;

    private static String SERVER_URL;

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        scan_btn = view.findViewById(R.id.scanner);
//        manual_page_nav = view.findViewById(R.id.manual_page_nav);
        text_api_str = view.findViewById(R.id.text_api_str);
        text_loc = view.findViewById(R.id.text_loc);
        text_str = view.findViewById(R.id.text_str);
        text_android_id = view.findViewById(R.id.text_android_id);
        text_api_call_status =  view.findViewById(R.id.text_api_call_status);
        api_call_status_label = view.findViewById(R.id.api_call_status_label);
        api_call_status_colon = view.findViewById(R.id.api_call_status_colon);
        text_loc.setText("Location");
        text_str.setText("QR String");
        text_api_str.setText("API call:Not Made");
        text_android_id.setText("android id not obtained");
        text_api_call_status.setText("API call: pending");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        GlobalVariables globalVariables = GlobalVariables.getInstance();
        text_android_id.setText(globalVariables.getAndroidId());

        String androidId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        globalVariables.setAndroidId(androidId);
        text_android_id.setText(globalVariables.getAndroidId());
        //concatenate android id to server url SERVER_URL=http://devmdas.fetchmee.in:15000/androidId/info/gps/1
        SERVER_URL = globalVariables.getSERVER_URL();
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator=
                        IntentIntegrator.forSupportFragment(ScanFragment.this);
                intentIntegrator.setPrompt("Scan a QR Code");
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.initiateScan();


            }
        });

//        manual_page_nav.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ManualFragment manualFragment = new ManualFragment();
////
////                // Get the FragmentManager
////                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
////
////                // Begin a transaction to replace the current fragment with the new fragment
////                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
////                fragmentTransaction.replace(R.id.view_pager, manualFragment);
////                fragmentTransaction.addToBackStack(null);
////                fragmentTransaction.commit();
//////////
//                FragmentTransaction transaction = getFragmentManager().beginTransaction();
//                transaction.replace(R.id.scan_frame, manualFragment ); // give your fragment container id in first parameter
//                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
//                transaction.commit();
////////
//
//
//                getActivity().getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.tab_layout, manualFragment, "findThisFragment")
//                        .addToBackStack(null)
//                        .commit();
//            }
//        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            str = intentResult.getContents();
            i = 0;
            if (str != null) {
                text_str.setText(str);
                GlobalVariables globalVariables = GlobalVariables.getInstance();
                globalVariables.setMeterRatings(str);
                strObtained = true;
                getLastLocation();
            } else {
                System.out.println("String is null");
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            // Request high accuracy location
            Task<Location> locationTask = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null);

            // Add a callback to handle the location result
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Handle the location result
                    if(location!=null){
                        loc="Latitude    : "+location.getLatitude()+"\nLongitude : "+location.getLongitude();
                        GlobalVariables globalVariables=GlobalVariables.getInstance();
                        globalVariables.setCurrentLatitude(location.getLatitude());
                        globalVariables.setCurrentLongitude(location.getLongitude());
                        globalVariables.setCurrentLocation(loc);
                        text_loc.setText(globalVariables.getCurrentLocation());
                        try {
                            sendScannedDataToServer(globalVariables.getMeterRatings(), String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            });

            // Add a callback to handle any errors
            locationTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle the error
                    Log.e("Location", "Error getting location", e);
                }
            });
        }else{

        }
    }

    private void sendScannedDataToServer(String scannedData, String lat, String lng) throws JSONException {
        OkHttpClient client = new OkHttpClient();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy HHmmss");
        // Format the current date and time using the SimpleDateFormat
        String scanTimeDate = dateFormat.format(new Date());
        String mode="S";
        GlobalVariables globalVariables=GlobalVariables.getInstance();

        DataObject wrapper = new DataObject(scannedData, lat,lng, mode,scanTimeDate);
        JSONObject jsonObject = wrapper.createJsonObject();
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json"));
        text_api_str.setText(jsonObject.toString());

        Request request = new Request.Builder()
                .url(globalVariables.getSERVER_URL())
                .post(requestBody)
                .build();

        api_call_status_label.setVisibility(View.VISIBLE);
        api_call_status_colon.setVisibility(View.VISIBLE);
        text_api_call_status.setVisibility(View.VISIBLE);



        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),  "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        text_api_call_status.setText("Request failed: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Server response: " + responseBody);
                            Toast.makeText(getActivity(), "Request successful. Response code: " + response.code() + " - " + responseBody, Toast.LENGTH_SHORT).show();
                            text_api_call_status.setText(response.code()+", Request successful.");
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("Server response: " + responseBody);
                            Toast.makeText(getActivity(), "Request unsuccessful. Response code: " + response.code() + " - " + responseBody, Toast.LENGTH_SHORT).show();
                            text_api_call_status.setText(response.code()+", Request unsuccessful." );
                        }
                    });
                }
            }
        });
    }
}

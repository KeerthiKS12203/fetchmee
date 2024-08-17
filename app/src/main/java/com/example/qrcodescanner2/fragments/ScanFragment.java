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
import android.widget.FrameLayout;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ScanFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button scanBtn;
    private TextView textStr, textLoc, textAndroidId, textApiCallStatus, apiCallStatusLabel, apiCallStatusColon;
    private String str, loc;
    private boolean locObtained = false, strObtained = false;
    private FrameLayout scannerContainer;
    private CompoundBarcodeView barcodeView;
    private static String SERVER_URL;

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLocationPermission();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        scanBtn = view.findViewById(R.id.scanner);
        textLoc = view.findViewById(R.id.text_loc);
        textStr = view.findViewById(R.id.text_str);
        textAndroidId = view.findViewById(R.id.text_android_id);
        textApiCallStatus = view.findViewById(R.id.text_api_call_status);
        apiCallStatusLabel = view.findViewById(R.id.api_call_status_label);
        apiCallStatusColon = view.findViewById(R.id.api_call_status_colon);
        scannerContainer = view.findViewById(R.id.scanner_container);

        textLoc.setText("Location");
        textStr.setText("QR String");
        textAndroidId.setText("Android ID not obtained");
        textApiCallStatus.setText("API call: pending");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        GlobalVariables globalVariables = GlobalVariables.getInstance();
        String androidId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        globalVariables.setAndroidId(androidId);
        textAndroidId.setText(globalVariables.getAndroidId());

        SERVER_URL = globalVariables.getSERVER_URL();

        setupBarcodeView();
        startScan();

        scanBtn.setOnClickListener(v -> {
            try {
                if (strObtained && locObtained) {
                    sendScannedDataToServer(globalVariables.getMeterRatings(), String.valueOf(globalVariables.getCurrentLatitude()), String.valueOf(globalVariables.getCurrentLongitude()));
                } else {
                    Toast.makeText(getActivity(), "Data not complete. Ensure QR code is scanned and location is obtained.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        return view;
    }

    private void setupBarcodeView() {
        barcodeView = new CompoundBarcodeView(getContext());
        barcodeView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
//        barcodeView.setStatusText("");
        scannerContainer.addView(barcodeView);

        List<BarcodeFormat> formats = Arrays.asList(
                BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E,
                BarcodeFormat.EAN_8,
                BarcodeFormat.EAN_13,
                BarcodeFormat.CODABAR,
                BarcodeFormat.CODE_39,
                BarcodeFormat.CODE_93,
                BarcodeFormat.CODE_128,
                BarcodeFormat.ITF,
                BarcodeFormat.RSS_14,
                BarcodeFormat.RSS_EXPANDED,
                BarcodeFormat.QR_CODE,
                BarcodeFormat.DATA_MATRIX,
                BarcodeFormat.PDF_417,
                BarcodeFormat.AZTEC,
                BarcodeFormat.MAXICODE,
                BarcodeFormat.UPC_EAN_EXTENSION

        );

        // Create a barcode decoder to handle various barcode formats
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                str = result.getText();
                if (str != null) {
                    textStr.setText(str);
                    GlobalVariables globalVariables = GlobalVariables.getInstance();
                    globalVariables.setMeterRatings(str);
                    strObtained = true;
                    getLastLocation();
                    barcodeView.pause(); // Pause scanning to prevent rapid re-scans
                    barcodeView.resume(); // Resume scanning after processing
                } else {
                    Log.d("ScanFragment", "String is null");
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Handle possible result points if needed
            }
        });
    }

    private void startScan() {
        scannerContainer.setVisibility(View.VISIBLE);
    }


    private void checkLocationPermission() {
        boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermissionGranted = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!cameraPermissionGranted || !locationPermissionGranted) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    CAMERA_PERMISSION_REQUEST_CODE); // Use a single request code if handling both permissions together
        } else {
            setupBarcodeView(); // Both permissions granted, proceed with setup
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            boolean cameraPermissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean locationPermissionGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (cameraPermissionGranted && locationPermissionGranted) {
                setupBarcodeView();
                getLastLocation(); // Request location if both permissions are granted
            } else {
                Toast.makeText(getActivity(), "Both camera and location permissions are required.", Toast.LENGTH_LONG).show();
                getActivity().getSupportFragmentManager().popBackStack(); // Optionally navigate away
            }
        }
    }


//    private void checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(),
//                    new String[]{Manifest.permission.CAMERA},
//                    CAMERA_PERMISSION_REQUEST_CODE);
//        }
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{
//                    Manifest.permission.ACCESS_FINE_LOCATION
//            }, LOCATION_PERMISSION_REQUEST_CODE);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                setupBarcodeView();
//                checkLocationPermission();
//            } else {
//                Toast.makeText(getActivity(), "Camera permission is required to scan QR codes.", Toast.LENGTH_LONG).show();
//                getActivity().getSupportFragmentManager().popBackStack(); // Optionally, navigate away or disable functionality
//                checkLocationPermission();
//            }
//        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLastLocation();
//            } else {
//                Toast.makeText(getActivity(), "Location permission is required to obtain location.", Toast.LENGTH_SHORT).show();
//                checkLocationPermission();
//            }
//        }
//    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationTask = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null);

            locationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    loc = "Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude();
                    GlobalVariables globalVariables = GlobalVariables.getInstance();
                    globalVariables.setCurrentLatitude(location.getLatitude());
                    globalVariables.setCurrentLongitude(location.getLongitude());
                    globalVariables.setCurrentLocation(loc);
                    textLoc.setText(globalVariables.getCurrentLocation());
                    locObtained = true;
                    scanBtn.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(e -> Log.e("Location", "Error getting location", e));
        }
    }

    private void sendScannedDataToServer(String scannedData, String lat, String lng) throws JSONException {
        OkHttpClient client = new OkHttpClient();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String scanTimeDate = dateFormat.format(new Date());
        System.out.println("scanTimeDate: "+scanTimeDate);
        String mode = "S";
        GlobalVariables globalVariables = GlobalVariables.getInstance();

        DataObject wrapper = new DataObject(scannedData, lat, lng, mode, scanTimeDate);
        JSONObject jsonObject = wrapper.createJsonObject();
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(globalVariables.getSERVER_URL())
                .post(requestBody)
                .build();

        apiCallStatusLabel.setVisibility(View.VISIBLE);
        apiCallStatusColon.setVisibility(View.VISIBLE);
        textApiCallStatus.setVisibility(View.VISIBLE);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    textApiCallStatus.setText("Request failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Request successful. Response code: " + response.code() + " - " + responseBody, Toast.LENGTH_SHORT).show();
                        textApiCallStatus.setText(response.code() + ", Request successful.");
                    } else {
                        Toast.makeText(getActivity(), "Request unsuccessful. Response code: " + response.code() + " - " + responseBody, Toast.LENGTH_SHORT).show();
                        textApiCallStatus.setText(response.code() + ", Request unsuccessful.");
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }
}

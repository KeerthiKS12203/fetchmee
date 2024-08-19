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

import com.example.qrcodescanner2.BaseFragment;
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


public class ScanFragment extends BaseFragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button scanBtn;
    private TextView textStr, textLoc, textAndroidId, text_comment, textApiCallStatus, apiCallStatusLabel, apiCallStatusColon;
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
        // Check for permissions
        checkPermissions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // Initialize your views
        scanBtn = view.findViewById(R.id.scanner);
        textLoc = view.findViewById(R.id.text_loc);
        textStr = view.findViewById(R.id.text_str);
        textAndroidId = view.findViewById(R.id.text_android_id);
        textApiCallStatus = view.findViewById(R.id.text_api_call_status);
        apiCallStatusLabel = view.findViewById(R.id.api_call_status_label);
        apiCallStatusColon = view.findViewById(R.id.api_call_status_colon);
        scannerContainer = view.findViewById(R.id.scanner_container); // Initialize FrameLayout
        text_comment = view.findViewById(R.id.text_comment);
        textApiCallStatus.setTextColor(getResources().getColor(R.color.black) ); // Change text color to red

        // Set initial texts
        textLoc.setText("Location");
        textStr.setText("QR String");
        textAndroidId.setText("Android ID not obtained");
        textApiCallStatus.setText("API call: pending");

        // Initialize location client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Set Android ID in global variables
        GlobalVariables globalVariables = GlobalVariables.getInstance();
        String androidId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        globalVariables.setAndroidId(androidId);
        textAndroidId.setText(globalVariables.getAndroidId());

        // Get server URL from global variables
        SERVER_URL = globalVariables.getSERVER_URL();

        // Setup barcode view and start scanning
        setupBarcodeView();
        startScan();

        // Set button click listener
        scanBtn.setOnClickListener(v -> {
            try {
                if (strObtained && locObtained) {
                    sendScannedDataToServer(globalVariables.getMeterRatings(),
                            String.valueOf(globalVariables.getCurrentLatitude()),
                            String.valueOf(globalVariables.getCurrentLongitude()));

                } else {
                    Toast.makeText(getActivity(),
                            "Data not complete. Ensure QR code is scanned and location is obtained.",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        return view;
    }

    private void setupBarcodeView() {
        if (scannerContainer != null) {
            barcodeView = new CompoundBarcodeView(getContext());
            barcodeView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
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
        } else {
            Log.e("ScanFragment", "scannerContainer is null. Cannot setup barcode view.");
        }
    }

    private void startScan() {
        if (scannerContainer != null) {
            scannerContainer.setVisibility(View.VISIBLE);
        } else {
            Log.e("ScanFragment", "scannerContainer is null. Cannot start scan.");
        }
    }

    private void checkPermissions() {
        boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermissionGranted = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!cameraPermissionGranted || !locationPermissionGranted) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    CAMERA_PERMISSION_REQUEST_CODE);
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

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationTask = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null);

            locationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    loc = "Lat: " + location.getLatitude() + "\nLng: " + location.getLongitude();
                    GlobalVariables globalVariables = GlobalVariables.getInstance();
                    globalVariables.setCurrentLatitude(location.getLatitude());
                    globalVariables.setCurrentLongitude(location.getLongitude());
                    globalVariables.setCurrentLocation(loc);
                    textLoc.setText(globalVariables.getCurrentLocation());
                    locObtained = true;
                    scanBtn.setVisibility(View.VISIBLE);
                    scanBtn.setEnabled(true);
                }
            }).addOnFailureListener(e -> Log.e("Location", "Error getting location", e));
        }
    }

    private void sendScannedDataToServer(String scannedData, String lat, String lng) throws JSONException {
        OkHttpClient client = new OkHttpClient();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String scanTimeDate = dateFormat.format(new Date());
        Log.d("ScanFragment", "scanTimeDate: " + scanTimeDate);
        String mode = "S";
        GlobalVariables globalVariables = GlobalVariables.getInstance();

        DataObject wrapper = new DataObject(scannedData, lat, lng, mode, text_comment.getText().toString(), scanTimeDate);
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
                    Toast.makeText(getActivity(), "Sorry, something went wrong. Please try again later.", Toast.LENGTH_SHORT).show();
                    textApiCallStatus.setTextColor(getResources().getColor(R.color.black) ); // Change text color to red
                    textApiCallStatus.setText("Error: Please try again later.");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseBody = response.body().string();
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Recorded Successfully", Toast.LENGTH_SHORT).show();
                        textApiCallStatus.setTextColor(getResources().getColor(R.color.green) ); // Change text color to red
                        textApiCallStatus.setText("Recorded Successfully");

                    } else {
                        Toast.makeText(getActivity(), "Record Failed", Toast.LENGTH_SHORT).show();
                        textApiCallStatus.setTextColor(getResources().getColor(R.color.red) ); // Change text color to red
                        textApiCallStatus.setText("Record Failed" );
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }
}

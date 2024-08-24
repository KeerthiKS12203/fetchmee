package com.example.qrcodescanner2.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private static final float LIGHT_THRESHOLD = 3.0f; // Threshold for torch operation
    private static final long FLASHLIGHT_DELAY_MS = 2000; // Delay in milliseconds

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button scanBtn;
    private TextView textStr, textLoc, textAndroidId, text_comment, textApiCallStatus, apiCallStatusLabel, apiCallStatusColon;
    private String str, loc;
    private boolean locObtained = false, strObtained = false;
    private ConstraintLayout scannerContainer;
    private CompoundBarcodeView barcodeView;
    private static String SERVER_URL;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean isTorchOn = false; // Track torch state
    private Handler handler; // Handler to manage delayed tasks

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        handler = new Handler(); // Initialize the Handler

        if (savedInstanceState != null) {
            String scannedData = savedInstanceState.getString("scannedData");
            String location = savedInstanceState.getString("location");
            String comment = savedInstanceState.getString("comment");

            // Restore values to the UI components
            if (scannedData != null) textStr.setText(scannedData);
            if (location != null) textLoc.setText(location);
            if (comment != null) text_comment.setText(comment);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        scanBtn = view.findViewById(R.id.scanner);
        textLoc = view.findViewById(R.id.text_loc);
        textStr = view.findViewById(R.id.text_str);
        textAndroidId = view.findViewById(R.id.text_android_id);
        textApiCallStatus = view.findViewById(R.id.text_api_call_status);
        apiCallStatusLabel = view.findViewById(R.id.api_call_status_label);
        apiCallStatusColon = view.findViewById(R.id.api_call_status_colon);
        scannerContainer = view.findViewById(R.id.scanner_container);
        text_comment = view.findViewById(R.id.text_comment);
        textApiCallStatus.setTextColor(getResources().getColor(R.color.black));

        textAndroidId.setText("Android ID not obtained");
        textApiCallStatus.setText("API call: pending");



        GlobalVariables globalVariables = GlobalVariables.getInstance();
        String androidId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        globalVariables.setAndroidId(androidId);
        textAndroidId.setText(globalVariables.getAndroidId());

        SERVER_URL = globalVariables.getSERVER_URL();

        textLoc.setText(globalVariables.getCurrentLocation());
        textStr.setText(globalVariables.getMeterRatings());
        text_comment.setText(globalVariables.getScanComment());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());


        setupBarcodeView();
        startScan();

        scanBtn.setOnClickListener(v -> {
            if (strObtained && locObtained) {
                try {
                    sendScannedDataToServer(globalVariables.getMeterRatings(),
                            String.valueOf(globalVariables.getCurrentLatitude()),
                            String.valueOf(globalVariables.getCurrentLongitude()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "Data not complete. Ensure QR code is scanned and location is obtained.", Toast.LENGTH_LONG).show();
            }
        });

        text_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed while text is changing
            }

            @Override
            public void afterTextChanged(Editable s) {
                globalVariables.setScanComment(text_comment.getText().toString());
            }
        });

        return view;
    }

    private void setupBarcodeView() {
        if (scannerContainer != null) {
            barcodeView = new CompoundBarcodeView(getContext());
            barcodeView.setLayoutParams(new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
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
                        if (str.equals(globalVariables.getMeterRatings())) {
//                            scanBtn.setEnabled(false);
                            Toast.makeText(getActivity(), "QR String is duplicated", Toast.LENGTH_SHORT).show();
                        } else {
                            globalVariables.setMeterRatings(str);
                            apiCallStatusLabel.setVisibility(View.GONE);
                            apiCallStatusColon.setVisibility(View.GONE);
                            textApiCallStatus.setVisibility(View.GONE);
                            textApiCallStatus.setTextColor(getResources().getColor(R.color.black));
                            textApiCallStatus.setText("Upload Pending");
                            strObtained = true;
                            getLastLocation();
                            barcodeView.pause();

                            barcodeView.resume();
                        }

                    } else {
                        Log.d("ScanFragment", "String is null");
                    }
                }

                @Override
                public void possibleResultPoints(List<ResultPoint> resultPoints) {
                }
            });
        } else {
            Log.e("ScanFragment", "scannerContainer is null. Cannot setup barcode view.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Reset str to null when the fragment is destroyed
        GlobalVariables globalVariables=GlobalVariables.getInstance();
        // Additional cleanup if needed
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("scannedData", textStr.getText().toString());
        outState.putString("location", textLoc.getText().toString());
        outState.putString("comment", text_comment.getText().toString());
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
            setupBarcodeView();
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
                getLastLocation();
            } else {
                Toast.makeText(getActivity(), "Both camera and location permissions are required.", Toast.LENGTH_LONG).show();
                getActivity().getSupportFragmentManager().popBackStack();
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
                    globalVariables.setScanComment(text_comment.getText().toString());
                    textLoc.setText(globalVariables.getCurrentLocation());
                    locObtained = true;
                    scanBtn.setVisibility(View.VISIBLE);
                    scanBtn.setEnabled(true);

                    // Post a delayed task to turn on the flashlight after the location is obtained
                    handler.postDelayed(this::turnTorchOn, FLASHLIGHT_DELAY_MS);
                } else {
                    Log.d("ScanFragment", "Location is null");
                }
            }).addOnFailureListener(e -> Log.e("Location", "Error getting location", e));
        } else {
            Toast.makeText(getActivity(), "Location permission is required.", Toast.LENGTH_LONG).show();
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
                    textApiCallStatus.setTextColor(getResources().getColor(R.color.red));
                    textApiCallStatus.setText("Error: Please try again later.");
                    scanBtn.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                getActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Recorded Successfully", Toast.LENGTH_SHORT).show();
                        textApiCallStatus.setTextColor(getResources().getColor(R.color.green));
                        textApiCallStatus.setText("Recorded Successfully");
                        scanBtn.setEnabled(false);
                    } else {
                        Toast.makeText(getActivity(), "Record Failed", Toast.LENGTH_SHORT).show();
                        textApiCallStatus.setTextColor(getResources().getColor(R.color.red));
                        textApiCallStatus.setText("Record Failed");
                        scanBtn.setEnabled(true);
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
            if (lightSensor != null) {
                sensorManager.unregisterListener(lightSensorEventListener);
            }
            if (isTorchOn) {
                turnTorchOff();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
            if (lightSensor != null) {
                sensorManager.registerListener(lightSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private final SensorEventListener lightSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float lightLevel = event.values[0];
            if (lightLevel < LIGHT_THRESHOLD) {
                barcodeView.setTorchOn(); // Turn on the flash
            } else {
                barcodeView.setTorchOff(); // Turn off the flash
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void turnTorchOn() {
        try {
            barcodeView.setTorchOn();
            isTorchOn = true;
        } catch (RuntimeException e) {
            Log.e("CameraManager", "Failed to set torch on", e);
        }
    }

    private void turnTorchOff() {
        try {
            barcodeView.setTorchOff();
            isTorchOn = false;
        } catch (RuntimeException e) {
            Log.e("CameraManager", "Failed to set torch off", e);
        }
    }
}

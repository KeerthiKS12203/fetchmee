package com.example.qrcodescanner2.fragments;

import android.Manifest;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManualFragment  extends BaseFragment {

    FusedLocationProviderClient fusedLocationProviderClient;

    EditText text_msn, text_meter_make, text_meter_model ,text_manufacture_yearmonth;
    Spinner dropdown_meter_type;
    TextView text_meter_ratings,text_loc_manual, text_comment, text_api_call_status, api_call_status_label, api_call_status_colon;
    String meter_ratings, loc;
    Button manual_submit_details;

    public ManualFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manual, container, false);

        dropdown_meter_type=(Spinner)view.findViewById(R.id.dropdown_meter_type);
        SpinnerActivity spinnerActivity=new SpinnerActivity();
        dropdown_meter_type.setOnItemSelectedListener(spinnerActivity);

        text_msn=view.findViewById(R.id.text_msn);
        text_meter_make=view.findViewById(R.id.text_meter_make);
        text_meter_model=view.findViewById(R.id.text_meter_model);
        text_manufacture_yearmonth=view.findViewById(R.id.text_manufacture_monthyear);
        text_meter_ratings=view.findViewById(R.id.text_meter_ratings);
        text_loc_manual=view.findViewById(R.id.text_loc_manual);
        text_comment=view.findViewById(R.id.text_comment);
        api_call_status_label=view.findViewById(R.id.api_call_status_label);
        api_call_status_colon=view.findViewById(R.id.api_call_status_colon);
        text_api_call_status=view.findViewById(R.id.text_api_call_status);
        manual_submit_details=view.findViewById(R.id.manual_submit_details);
        text_meter_ratings.setVisibility(View.GONE);
        text_loc_manual.setVisibility(View.GONE);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());


        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.meter_type_spinner,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        dropdown_meter_type.setAdapter(adapter);
        dropdown_meter_type.setSelection(0);


        manual_submit_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateMeterRatings(dropdown_meter_type.getSelectedItem().toString(), text_msn.getText().toString(), text_meter_make.getText().toString(), text_meter_model.getText().toString(), text_manufacture_yearmonth.getText().toString());
            }
        });

        text_manufacture_yearmonth.addTextChangedListener(new TextWatcher() {
            private boolean mEditing = false;

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
                if (mEditing) return; // Avoid recursive calls

                mEditing = true;

                // Save the current cursor position
                int cursorPosition = text_manufacture_yearmonth.getSelectionStart();

                // Get the current text and remove any non-numeric characters
                String input = s.toString().replaceAll("[^0-9]", "");

                // Limit to 6 characters for MMYYYY
                if (input.length() > 6) {
                    input = input.substring(0, 6);
                }

                // Initialize formatted input
                StringBuilder formattedInput = new StringBuilder();

                if (input.length() > 0) {
                    // Add month part
                    if (input.length() > 2) {
                        formattedInput.append(input.substring(0, 2));
                        // Add slash and year part
                        formattedInput.append("/").append(input.substring(2));
                    } else {
                        // Handle single or partial months
                        String monthPart = input;
                        if (monthPart.length() == 1) {
                            // Single digit month, ensure it has leading zero
                            if(Integer.parseInt(monthPart)>1) {
                                monthPart = "0" + monthPart;
                            }
                        }
                        formattedInput.append(monthPart);
                    }
                }

                // Validate and correct the month range
                if (formattedInput.length() >= 2) {
                    try {
                        int month = Integer.parseInt(formattedInput.substring(0, 2));
                        if (month > 12) {
                            // Set month part to 12 if invalid month is entered
                            formattedInput = new StringBuilder("12");
                            if (formattedInput.length() > 2) {
                                formattedInput.append("/").append(formattedInput.substring(3));
                            }
                        } else if (month < 1) {
                            // Set month part to 01 if invalid month is entered
                            formattedInput = new StringBuilder("01");
                            if (formattedInput.length() > 2) {
                                formattedInput.append("/").append(formattedInput.substring(3));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Handle the exception if parsing fails
                    }
                }

                // Set the formatted text
                s.replace(0, s.length(), formattedInput.toString());
                text_manufacture_yearmonth.setSelection(formattedInput.length());

                mEditing = false;
            }


//            @Override
//            public void afterTextChanged(Editable s) {
//                if (mEditing) return; // Avoid recursive calls
//
//                mEditing = true;
//
//                // Remove any non-numeric characters
//                String input = s.toString().replaceAll("[^0-9]", "");
//
//                // Limit to 6 characters for MMYYYY
//                if (input.length() > 6) {
//                    input = input.substring(0, 6);
//                }
//
//                // Format input as MM/YYYY
//                String formattedInput = "";
//                if (input.length() > 0) {
//                    // Add month part
//                    formattedInput = input.length() > 2 ? input.substring(0, 2) : input;
//                    // Add slash and year part
//                    if (input.length() > 2) {
//                        formattedInput += "/" + input.substring(2);
//                    }
//                }
//
//                // Validate month range
//                if (formattedInput.length() >= 2 && Integer.parseInt(formattedInput.substring(0, 2)) > 12) {
//                    // Set month part to 12 if invalid month is entered
//                    formattedInput = "12/" + formattedInput.substring(3);
//                }
//
//                // Set the formatted text
//                s.clear();
//                s.append(formattedInput);
//
//                // Move cursor to the end of the input
//                int length = s.length();
//                if (length > 0) {
//                    text_manufacture_yearmonth.setSelection(length);
//                }
//
//                mEditing = false;
//            }
        });




        return view;
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item is selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos).
            parent.getItemAtPosition(pos);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback.
        }
    }

    private void validateMeterRatings(String ct_ratings, String msn,String meter_make, String meter_model_type ,String meter_yearmonth) {
        System.out.println("Validating");

        String ct_ratings_pattern = "^([0-9]{3})[/-]([0-9]{0,3})$";
        String meter_yearmonth_pattern = "^(0[1-9]|1[0-2])[/]([0-9]{4})$";
        String msn_pattern = "^[0-9]{7}$";
        String model_pattern = "^[A-Z0-9]{5}$";
        String make_pattern = "^[A-Za-z]+$";


        if(!(ct_ratings.equals("1") || ct_ratings.equals("3"))){
            Toast.makeText(getActivity(), "Meter Type is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Pattern.matches(make_pattern,meter_make)!=true){
            Toast.makeText(getActivity(), "Meter Make is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Pattern.matches(model_pattern,meter_model_type)!=true){
            Toast.makeText(getActivity(), "Please enter a valid 5-character alphanumeric Meter Model", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Pattern.matches(msn_pattern, msn)!=true){
            Toast.makeText(getActivity(), "Please enter a valid 7-digit MSN", Toast.LENGTH_SHORT).show();
            return;
        }
        if(Pattern.matches(meter_yearmonth_pattern,meter_yearmonth)!=true){
            Toast.makeText(getActivity(), "Month/Year is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        meter_ratings=ct_ratings+", "+meter_make+", "+meter_model_type+", "+", "+msn+", "+meter_yearmonth;
        GlobalVariables globalVariables=GlobalVariables.getInstance();
        globalVariables.setMeterRatings(meter_ratings);
        text_meter_ratings.setText(meter_ratings);
        text_meter_ratings.setVisibility(View.VISIBLE);
        getLastLocation();
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
                        text_loc_manual.setText(globalVariables.getCurrentLocation());
                        text_loc_manual.setVisibility(View.VISIBLE);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        // Format the current date and time using the SimpleDateFormat
        String scanTimeDate = dateFormat.format(new Date());
        System.out.println("scanTimeDate: "+scanTimeDate);
        String mode="M";
        GlobalVariables globalVariables=GlobalVariables.getInstance();

        DataObject wrapper = new DataObject(scannedData, lat, lng, mode, text_comment.getText().toString(), scanTimeDate);
        JSONObject jsonObject = wrapper.createJsonObject();
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json"));

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
                        Toast.makeText(getActivity(), "Sorry, something went wrong. Please try again later.", Toast.LENGTH_SHORT).show();
                        text_api_call_status.setTextColor(getResources().getColor(R.color.black) ); // Change text color to red
                        text_comment.setText("Error: Please try again later.");
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
                            Toast.makeText(getActivity(), "Recorded Successfully", Toast.LENGTH_SHORT).show();
                            text_api_call_status.setTextColor(getResources().getColor(R.color.green) ); // Change text color to red
                            text_api_call_status.setText("Recorded Successfully");
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Record Failed", Toast.LENGTH_SHORT).show();
                            text_api_call_status.setTextColor(getResources().getColor(R.color.red) ); // Change text color to red
                            text_api_call_status.setText("Record Failed" );
                        }
                    });
                }
            }
        });
    }
}
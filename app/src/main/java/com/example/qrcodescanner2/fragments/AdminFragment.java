package com.example.qrcodescanner2.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.qrcodescanner2.GlobalVariables;
import com.example.qrcodescanner2.R;

public class AdminFragment extends Fragment {

    private EditText editTextDomain;
    private EditText editTextPort;
    private TextView textViewBaseUrl;
    private Button buttonEdit;
    private LinearLayout usernamePasswordLayout;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonSubmit;
    private Button buttonDone;
    private Spinner dropdown_http_protocol;
    private String http_protocol;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        // Initialize UI components
        editTextDomain = view.findViewById(R.id.editTextDomain);
        editTextPort = view.findViewById(R.id.editTextPort);
        textViewBaseUrl = view.findViewById(R.id.textViewBaseUrl);
        buttonEdit = view.findViewById(R.id.buttonEdit);
        usernamePasswordLayout = view.findViewById(R.id.usernamePasswordLayout);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonDone = view.findViewById(R.id.buttonDone);

        dropdown_http_protocol=view.findViewById(R.id.dropdown_http_protocol);
        SpinnerActivity spinnerActivity=new SpinnerActivity();
        dropdown_http_protocol.setOnItemSelectedListener(spinnerActivity);
        dropdown_http_protocol.setEnabled(false);
        // Initially hide username/password layout and buttons
        usernamePasswordLayout.setVisibility(View.GONE);
        buttonSubmit.setVisibility(View.GONE);
        buttonDone.setVisibility(View.GONE);

        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.url_security_options,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        dropdown_http_protocol.setAdapter(adapter);

        setDefaultSelection();


        GlobalVariables globalVariables=GlobalVariables.getInstance();
        editTextDomain.setText(globalVariables.getServer_domain_name());
        editTextPort.setText(globalVariables.getServer_port_no());
        textViewBaseUrl.setText(globalVariables.getBaseURL());

        // Handle Edit button click
        buttonEdit.setOnClickListener(v -> {
            // Show username and password fields
            buttonEdit.setVisibility(View.GONE);
            usernamePasswordLayout.setVisibility(View.VISIBLE);
            buttonSubmit.setVisibility(View.VISIBLE);
            buttonDone.setVisibility(View.GONE); // Initially hide Done button
        });

        // Handle Submit button click
        buttonSubmit.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.equals(username, globalVariables.getAdminUserId()) && TextUtils.equals(password, globalVariables.getAdminPassword())) {
                // Clear and hide username/password fields
                editTextUsername.setText("");
                editTextPassword.setText("");
                usernamePasswordLayout.setVisibility(View.GONE);
                buttonSubmit.setVisibility(View.GONE);
                buttonEdit.setVisibility(View.GONE);
                // Enable domain and port fields
                editTextDomain.setEnabled(true);
                editTextPort.setEnabled(true);
                dropdown_http_protocol.setEnabled(true);

                // Show Done button
                buttonDone.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Done button click
        buttonDone.setOnClickListener(v -> {
            String domain = editTextDomain.getText().toString().trim();
            String port = editTextPort.getText().toString().trim();

            if (!isValidDomain(domain)) {
                Toast.makeText(getContext(), "Invalid domain name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPort(port)) {
                Toast.makeText(getContext(), "Invalid port number", Toast.LENGTH_SHORT).show();
                return;
            }

            String protocol="http";
            if(http_protocol.equals("Secure (HTTPS)")){protocol="https";}
            else if (http_protocol.equals("Insecure (HTTP)")){protocol="http";}
            // Update base URL

            globalVariables.setHTTP_protocol( protocol);
            String baseUrl = protocol+"://" + domain + ":" + port;
            textViewBaseUrl.setText(baseUrl);
            globalVariables.setServer_domain_name(domain);
            globalVariables.setServer_port_no(port);

            // Disable domain and port inputs
            editTextDomain.setEnabled(false);
            editTextPort.setEnabled(false);
            dropdown_http_protocol.setEnabled(false);
            buttonEdit.setVisibility(View.VISIBLE);
            // Hide Done button
            buttonDone.setVisibility(View.GONE);
        });

        return view;
    }

    // Validate domain name
    private boolean isValidDomain(String domain) {
        return !TextUtils.isEmpty(domain) && Patterns.DOMAIN_NAME.matcher(domain).matches();
    }

    // Validate port number
    private boolean isValidPort(String port) {
        try {
            int portNumber = Integer.parseInt(port);
            return portNumber >= 1 && portNumber <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setDefaultSelection() {
        GlobalVariables globalVariables=GlobalVariables.getInstance();
        String defaultProtocol="Secure (HTTPS)"; //if not secure, then return defaultProtocol ie.https
        if(globalVariables.getHTTP_protocol()=="http"){ defaultProtocol= "Insecure (HTTP)";}
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) dropdown_http_protocol.getAdapter();
        int position = adapter.getPosition(defaultProtocol);
        dropdown_http_protocol.setSelection(position);
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item is selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos).
            parent.getItemAtPosition(pos);

            // Update the URL based on the selection
            http_protocol=((String) parent.getItemAtPosition(pos));


        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback.
            setDefaultSelection();
        }
    }

}

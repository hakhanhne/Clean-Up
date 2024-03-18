package rmit.aad.cleanup.views.cleanup;

import static rmit.aad.cleanup.utils.CommonFunctions.checkRequired;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.model.RecurringInfo;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.utils.PreferenceManager;
import rmit.aad.cleanup.views.account.Dashboard;
import rmit.aad.cleanup.views.account.Login;
import rmit.aad.cleanup.views.account.Register;

public class RegisterCleanUp extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , View.OnClickListener{

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        int request_code;
        public DatePickerFragment() {};


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker.
            final Calendar c = Calendar.getInstance();
            Date date = c.getTime();

            Bundle bundle = this.getArguments();
            if(bundle != null) {
                request_code = bundle.getInt(REQUEST_PICK_DATE_KEY);

                try {
                    if (request_code == REQUEST_PICK_EVENT_DATE && !add_event_date.getText().toString().isEmpty()) {
                        date = CommonFunctions.formatDate(add_event_date.getText().toString());
                    }
                    else if (request_code == REQUEST_PICK_END_DATE && !add_recurring_end_date.getText().toString().isEmpty()) {
                        date = CommonFunctions.formatDate(add_event_date.getText().toString());
                    }
                } catch (ParseException e) {}
            }

            int year = date.getYear();
            int month = date.getMonth();
            int day = date.getDay();

            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), this, year, month, day);
            datePicker.getDatePicker().setMinDate(c.getTime().getTime());
            return datePicker;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String day_str = (day < 10) ? "0".concat(String.valueOf(day)) : String.valueOf(day) ;
            String month_str = (month + 1 < 10) ? "0".concat(String.valueOf(month+1)) : String.valueOf(month+1);
            String date_str = String.valueOf(year).concat("-").concat(month_str).concat("-").concat(day_str);
            if (request_code == REQUEST_PICK_EVENT_DATE) {
                add_event_date.setText(date_str);
            } else if (request_code == REQUEST_PICK_END_DATE) {
                add_recurring_end_date.setText(date_str);
            }
        }
    }

    final String TAG = "RegisterCleanUp";
    PreferenceManager pm;
    final int REQUEST_LOGIN = 1;
    final int REQUEST_REGISTER = 2;
    final static String REQUEST_PICK_DATE_KEY = "PICK_DATE";
    final static int REQUEST_PICK_EVENT_DATE = 3;
    final static int REQUEST_PICK_END_DATE = 4;
    Date eventDate;
    Date endDate;

    Button register_cleanUp;
    TextView href_login;
    TextView href_register_account;
    RadioGroup add_target;
    EditText add_slots;
    EditText add_event_description;
    EditText add_location_name;
    Spinner add_location_type;
    EditText add_location_address;
    EditText add_location_description;
    CheckBox is_private;
    CheckBox is_recurring;
    static EditText add_event_date;
    Spinner add_recurring_frequency;
    static EditText add_recurring_end_date;
    EditText add_start_time;
    EditText add_end_time;
    ImageView img_pick_event_date;
    ImageView img_pick_end_date;
    LinearLayout recurring_info;
    String location_address;
    Double lat;
    Double lng;

    CleanUpSite site;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_cleanup);

        findViewById(R.id.screen_register_cleanup).setVisibility(View.VISIBLE);
        CommonFunctions.setUser(this);
        CommonFunctions.initToolbar_Nav(this, this);
        initPage();


        register_cleanUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pm.isLoggedIn()) {
                    showRequireLogin();
                    return;
                }

                CleanUpSite.TargetCategory target;
                int checked_target = add_target.getCheckedRadioButtonId();
                if (checked_target == -1) {
                    RadioButton btn_target = findViewById(R.id.btn_target_community);
                    btn_target.requestFocus();
                    btn_target.setError("Please select Target");
                    return;
                }
                else if (checked_target == R.id.btn_target_community) {
                    target = CleanUpSite.TargetCategory.Community;
                }
                else {
                    target = CleanUpSite.TargetCategory.Organization;
                }

                String slots_str = checkRequired(add_slots);
                if (slots_str == null) return;
                int slots;
                try {
                    slots = Integer.parseInt(add_slots.getText().toString());
                }
                catch (Exception e) {
                    add_slots.setError("Invalid number");
                    add_slots.requestFocus();
                    return;
                }

                String event_description = add_event_description.getText().toString();

                String location_name = checkRequired(add_location_name);
                if (location_name == null) return;

                location_address = checkRequired(add_location_address);
                if (location_address == null) return;
                if (!checkAddress(location_address)) {
                    return;
                }

                String location_type = add_location_type.getSelectedItem().toString();
                String location_description = add_location_description.getText().toString();

                String event_date_str = checkRequired(add_event_date);
                if (event_date_str == null) return;
                else {
                    try {
                        eventDate = CommonFunctions.formatDate(event_date_str);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        add_event_date.requestFocus();
                        add_event_date.setError("Invalid date format");
                        return;
                    }
                }

                String start_time = checkRequired(add_start_time);
                if (start_time == null) return;

                String end_time = checkRequired(add_end_time);
                if (end_time == null) return;

                Boolean isPrivate = is_private.isChecked();
                Boolean isRecurring = is_recurring.isChecked();

                RecurringInfo recurringInfo = null;
                if(isRecurring) {
                    String recurring_frequency = add_recurring_frequency.getSelectedItem().toString();
                    String end_date_str = checkRequired(add_recurring_end_date);
                    if (end_date_str == null) return;
                    if (end_date_str.compareTo(add_event_date.getText().toString()) < 0) {
                        add_event_date.requestFocus();
                        add_recurring_end_date.requestFocus();
                        add_recurring_end_date.setError("End Date must be behind Event Date");
                    }
                    try {
                        endDate = CommonFunctions.formatDate(end_date_str);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        add_recurring_end_date.requestFocus();
                        add_recurring_end_date.setError("Invalid date format");
                        return;
                    }
                    recurringInfo = new RecurringInfo(RecurringInfo.Frequency.valueOf(recurring_frequency), endDate);
                }

                site = new CleanUpSite();
                site.setState(CleanUpSite.State.Pending);
                site.setOwner(pm.getPrefEmail());
                site.setTargetCategory(target);
                site.setIsPrivate(isPrivate);
                site.setSlots(slots);
                site.setRemainingSlots(slots);
                site.setEventDescription(event_description);
                site.setLocationName(location_name);
                site.setLocationType(CleanUpSite.LocationType.valueOf(location_type));
                site.setLocationAddress(location_address);
                site.setLatitude(lat);
                site.setLongitude(lng);
                site.setLocationDescription(location_description);
                site.setEventDate(eventDate);
                site.setStartTime(start_time);
                site.setEndTime(end_time);
                site.setIsRecurring(isRecurring);
                site.setRecurringInfo(recurringInfo);

                Log.d(TAG, site.getLocationName());
                showConfirmSubmit();
            }
        });


        href_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(REQUEST_LOGIN);
            }
        });

        href_register_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(REQUEST_REGISTER);
            }
        });

        is_recurring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recurring_info.setVisibility(is_recurring.isChecked() ? View.VISIBLE : View.INVISIBLE);
            }
        });

        img_pick_event_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(REQUEST_PICK_EVENT_DATE);
            }
        });

        img_pick_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(REQUEST_PICK_END_DATE);
            }
        });
    }

    public void showRequireLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterCleanUp.this);
        builder.setTitle("Please Login to Register Clean Up Site");

        // add the buttons
        builder
                .setMessage("You need to login to register a Clean Up Site! Please refer to Step 1 for more instruction.")
                .setCancelable(true)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void showConfirmSubmit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterCleanUp.this);
        builder.setTitle("Confirm Submit");

        // add the buttons
        builder
                .setMessage("Please confirm that you have checked all information carefully!")
                .setCancelable(true)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ProgressDialog progressDialog = new ProgressDialog(RegisterCleanUp.this);
                        progressDialog.setMessage("Processing ...");
                        progressDialog.show();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("sites")
                                .add(site)
                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Registered Successfully!", Toast.LENGTH_LONG).show();
                                            String id = task.getResult().getId();
                                            db.collection("sites")
                                                    .document(id)
                                                    .update("id", id);
                                            startActivity(new Intent(RegisterCleanUp.this, Dashboard.class));
                                        }
                                        else {
                                            Log.d(TAG, "failed: " + task.getException().getMessage());
                                            Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDatePicker (int request_code) {
        DatePickerFragment datePicker = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(REQUEST_PICK_DATE_KEY, request_code);
        datePicker.setArguments(bundle);
        datePicker.show(getSupportFragmentManager(), "datePicker");
    }


    private boolean checkAddress(String address) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList.size() > 0) {
                Address a = addressList.get(0);
                result.append(address + ", ");
                if (a.getFeatureName() != null) result.append(a.getFeatureName() + ", ");
                if (a.getThoroughfare() != null) result.append(a.getThoroughfare() + ", ");
                if (a.getCountryName() != null) {
                    Log.d("Country", a.getCountryName());
                    if (!a.getCountryName().contains("Vietnam")) {
                        add_location_address.setError("Location must be in Vietnam only");
                        add_location_address.requestFocus();
                        return false;
                    }
                    result.append(a.getCountryName());
                }
                String result_address = result.toString();
                add_location_address.setText(result_address);
                location_address = result_address;
                lat = a.getLatitude();
                lng = a.getLongitude();
                Toast.makeText(RegisterCleanUp.this, "Address is corrected to: " + location_address, Toast.LENGTH_LONG);
                return true;
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        add_location_address.setError("Invalid address");
        add_location_address.requestFocus();
        return false;
    }


    private void initPage() {
        pm = new PreferenceManager(getApplicationContext());
        register_cleanUp = findViewById(R.id.btn_register_cleanup);
        img_pick_event_date = findViewById(R.id.pick_event_date);
        img_pick_end_date = findViewById(R.id.pick_end_date);
        href_login = findViewById(R.id.href_login);
        href_register_account = findViewById(R.id.href_register_account);
        add_target = findViewById(R.id.add_target);
        add_slots = findViewById(R.id.add_slots);
        add_event_description = findViewById(R.id.add_event_description);
        add_location_name = findViewById(R.id.add_location_name);
        add_location_type = findViewById(R.id.add_location_type);
        add_location_address = findViewById(R.id.add_location_address);
        add_location_description = findViewById(R.id.add_location_description);
        is_private = findViewById(R.id.is_private);
        add_event_date = findViewById(R.id.add_event_date);
        add_start_time = findViewById(R.id.add_start_time);
        add_end_time = findViewById(R.id.add_end_time);
        is_recurring = findViewById(R.id.is_recurring);
        recurring_info = findViewById(R.id.recurring_info);
        add_recurring_frequency = findViewById(R.id.add_recurring_frequency);
        add_recurring_end_date = findViewById(R.id.add_recurring_end_date);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CleanUpSite.locationTypeList());
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        add_location_type.setAdapter(locationAdapter);

        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, RecurringInfo.frequencyList());
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        add_recurring_frequency.setAdapter(frequencyAdapter);
    }


    public void showAlertDialog(int request_code ) {
        Class<?> destination = request_code == REQUEST_LOGIN ? Login.class : Register.class;
        if (pm.isLoggedIn()) {
            String title = request_code == REQUEST_LOGIN ? "Processing to Login" : "Processing to Register Account";
            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterCleanUp.this);
            builder.setTitle(title);

            // add the buttons
            builder
                    .setMessage("This action will log you out. Do you want to continue?")
                    .setCancelable(true)
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CommonFunctions.logout(RegisterCleanUp.this);
                            startActivity(new Intent(RegisterCleanUp.this, destination));
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            startActivity(new Intent(RegisterCleanUp.this, destination));
        }
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return CommonFunctions.navigationItemSelect(item, this);
    }
}
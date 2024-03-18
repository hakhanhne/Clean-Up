package rmit.aad.cleanup.views.cleanup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.model.SiteEnrolment;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.utils.PreferenceManager;
import rmit.aad.cleanup.views.account.Dashboard;
import rmit.aad.cleanup.views.account.Login;
import rmit.aad.cleanup.views.account.Register;

public class JoinCleanUp extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , View.OnClickListener{
    final String TAG = "JoinCleanUp";
    PreferenceManager pm;
    public static CleanUpSite site = null;
    public static LinearLayout selected_site_marker_info;
    public static TextView marker_name;
    public static TextView marker_target;
    public static TextView marker_textJoin;
    public static TextView marker_address;

    final int REQUEST_LOGIN = 1;
    final int REQUEST_REGISTER = 2;
    MapSiteSelectFragment mapSiteSelectFragment;
    TextView href_login;
    TextView href_register_account;
    CheckBox is_checked;
    Button btnConfirmJoin;
    EditText vEmail;
    SiteEnrolment siteEnrolment;
    TextView site_name;
    TextView address;
    TextView target;
    TextView event_description;
    TextView start_time;
    TextView end_time;
    TextView event_date;
    TextView end_date;
    TextView frequency;
    TextView type;

    LinearLayout recurring_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_cleanup);

        findViewById(R.id.screen_join_cleanup).setVisibility(View.VISIBLE);
        CommonFunctions.setUser(this);
        CommonFunctions.initToolbar_Nav(this, this);

        mapSiteSelectFragment = new MapSiteSelectFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_map_cleanup_join, mapSiteSelectFragment)
                .commit();

        initPage();

        marker_textJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pm.isLoggedIn()) {
                    showRequireLogin();
                    return;
                }
                vEmail.setText(pm.getPrefEmail());
                vEmail.setEnabled(false);
                setSiteInfo(site);
            }
        });

        btnConfirmJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pm.isLoggedIn()) {
                    showRequireLogin();
                    return;
                }

                Boolean checked = is_checked.isChecked();
                if (!checked) {
                    is_checked.setError("This field is required");
                    is_checked.requestFocus();
                    return;
                }

                siteEnrolment = new SiteEnrolment(site.getId(), pm.getPrefId());
                siteEnrolment.setUserid(pm.getPrefId());
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
    }

    public void showConfirmSubmit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(JoinCleanUp.this);
        builder.setTitle("Confirm Submit");

        // add the buttons
        builder
                .setMessage("Please confirm that you have checked all information carefully!")
                .setCancelable(true)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ProgressDialog progressDialog = new ProgressDialog(JoinCleanUp.this);
                        progressDialog.setMessage("Processing ...");
                        progressDialog.show();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("enrolments")
                                .add(siteEnrolment)
                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Joined Clean Up Site Successfully!\n", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(JoinCleanUp.this, Dashboard.class));
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


    public void showAlertDialog(int request_code ) {
        Class<?> destination = request_code == REQUEST_LOGIN ? Login.class : Register.class;
        if (pm.isLoggedIn()) {
            String title = request_code == REQUEST_LOGIN ? "Processing to Login" : "Processing to Register Account";
            AlertDialog.Builder builder = new AlertDialog.Builder(JoinCleanUp.this);
            builder.setTitle(title);

            // add the buttons
            builder
                    .setMessage("This action will log you out. Do you want to continue?")
                    .setCancelable(true)
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CommonFunctions.logout(JoinCleanUp.this);
                            startActivity(new Intent(JoinCleanUp.this, destination));
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
            startActivity(new Intent(JoinCleanUp.this, destination));
        }
    }

    private void initPage() {
        pm = new PreferenceManager(getApplicationContext());
        href_login = findViewById(R.id.href_login);
        href_register_account = findViewById(R.id.href_register_account);
        is_checked = findViewById(R.id.is_checked);
        vEmail = findViewById(R.id.join_email);
        site_name = findViewById(R.id.card_site_full_name);
        target = findViewById(R.id.card_site_full_target);
        address = findViewById(R.id.card_site_full_address);
        event_description = findViewById(R.id.card_site_event_description);
        start_time = findViewById(R.id.card_site_start_time);
        end_time = findViewById(R.id.card_site_end_time);
        event_date = findViewById(R.id.card_site_event_date);
        end_date = findViewById(R.id.card_recurring_end_date);
        frequency = findViewById(R.id.card_recurring_frequency);
        type = findViewById(R.id.card_site_location_type);
        recurring_info = findViewById(R.id.card_recurring_info);
        selected_site_marker_info = findViewById(R.id.selected_site_marker_info);
        selected_site_marker_info.setVisibility(View.GONE);
        marker_name = findViewById(R.id.card_site_name);
        marker_address = findViewById(R.id.card_site_address);
        marker_target = findViewById(R.id.card_site_target);
        marker_textJoin = findViewById(R.id.card_site_text_join);
        vEmail.setText("");
        is_checked.setChecked(false);
        btnConfirmJoin = findViewById(R.id.btnConfirmJoin);
        if (site != null) {
            setMarkerInfo();
        }
        else {
            selected_site_marker_info.setVisibility(View.GONE);
        }

    }
    
    void setSiteInfo(CleanUpSite site) {
        if (!site.getIsRecurring()) {
            recurring_info.setVisibility(View.GONE);
        }
        else {
            recurring_info.setVisibility(View.VISIBLE);
            frequency.setText(site.getRecurringInfo().getFrequency().toString());

            String end_date_str = "";
            try {
                end_date_str = CommonFunctions.formatDateToString(site.getRecurringInfo().getEndDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            end_date.setText(end_date_str);
        }
        type.setText(site.getLocationType().toString());
        site_name.setText(site.getLocationName());
        target.setText(site.getTargetCategory().toString());
        address.setText(site.getLocationAddress());
        event_description.setText(site.getEventDescription());
        start_time.setText(site.getStartTime());
        end_time.setText(site.getEndTime());
        String event_date_str = "";
        try {
            event_date_str = CommonFunctions.formatDateToString(site.getEventDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        event_date.setText(event_date_str);
    }

    void setMarkerInfo() {
        marker_name.setText(site.getLocationName());
        marker_address.setText(site.getLocationAddress());
        marker_target.setText(site.getTargetCategory().toString());
        selected_site_marker_info.setVisibility(View.VISIBLE);
    }

    public void showRequireLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(JoinCleanUp.this);
        builder.setTitle("Please Login to Register Clean Up Site");

        // add the buttons
        builder
                .setMessage("You need to login to join a Clean Up Site! Please refer to Step 1 for more instruction.")
                .setCancelable(true)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onClick(View v) {}

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return CommonFunctions.navigationItemSelect(item, this);
    }
}
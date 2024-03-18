package rmit.aad.cleanup.views.cleanup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.List;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.adapter.CleanUpSiteAdapter;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.utils.PreferenceManager;

public class SiteDetails extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , View.OnClickListener{
    final String TAG = "CleanUpSites";
    public final static String SITE_ID_KEY = "SITE_ID";
    String siteid;
    public static CleanUpSite site;
    FirebaseFirestore db;
    PreferenceManager pm;
    MapSiteDetailFragment mapSiteDetailFragment = new MapSiteDetailFragment();

    TextView site_name;
    TextView address;
    TextView target;
    TextView event_description;
    TextView start_time;
    TextView end_time;
    TextView event_date;
    TextView end_date;
    TextView frequency;
    LinearLayout recurring_info;
    TextView type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_details);
        findViewById(R.id.screen_site_details).setVisibility(View.VISIBLE);

        CommonFunctions.setUser(this);
        CommonFunctions.initToolbar_Nav(this, this);
        initPage();

        Intent intent = getIntent();
        siteid = intent.getStringExtra(SITE_ID_KEY);
//        Log.d("HUUHUH", siteid);



        db.collection("sites").document(siteid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            site = task.getResult().toObject(CleanUpSite.class);
                            setSiteInfo(site);

                            site_name.setText(site.getLocationName());
                            mapSiteDetailFragment = new MapSiteDetailFragment();
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_map_site_details, mapSiteDetailFragment)
                                    .commit();
                        }
                    }
                });


    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return CommonFunctions.navigationItemSelect(item, this);
    }

    void initPage() {
        db = FirebaseFirestore.getInstance();
        pm = new PreferenceManager(getApplicationContext());
        site_name = findViewById(R.id.card_site_detail_full_name);
        target = findViewById(R.id.card_site_detail_full_target);
        address = findViewById(R.id.card_site_detail_full_address);
        event_description = findViewById(R.id.card_site_detail_event_description);
        start_time = findViewById(R.id.card_site_detail_start_time);
        end_time = findViewById(R.id.card_site_detail_end_time);
        event_date = findViewById(R.id.card_site_detail_event_date);
        end_date = findViewById(R.id.card_site_detail_recurring_end_date);
        frequency = findViewById(R.id.card_site_detail_recurring_frequency);
        type = findViewById(R.id.card_site_detail_location_type);
        recurring_info = findViewById(R.id.card_site_detail_recurring_info);

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

}
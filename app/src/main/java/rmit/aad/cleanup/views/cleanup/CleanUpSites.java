package rmit.aad.cleanup.views.cleanup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.adapter.CleanUpSiteAdapter;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.utils.PreferenceManager;

public class CleanUpSites extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , View.OnClickListener{
    final String TAG = "CleanUpSites";
    final String DEFAULT_TYPE = "Location type";
    final String DEFAULT_TARGET = "Site target";
    FirebaseFirestore db;
    PreferenceManager pm;
    MapSitesFragment mapSitesFragment;
    Spinner filter_location_type;
    Spinner filter_target;
    SearchView searchView;
    String search_by_name = "";
    String selected_target = "";
    String selected_type = "";
    RecyclerView siteRecyclerView;
    static List<CleanUpSite> sites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleanup_sites);
        findViewById(R.id.screen_cleanup_sites).setVisibility(View.VISIBLE);

        CommonFunctions.setUser(this);
        CommonFunctions.initToolbar_Nav(this, this);


        initPage();
        fetchSites();


        filter_location_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_type = filter_location_type.getSelectedItem().toString();
                fetchSites();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        filter_target.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_target = filter_target.getSelectedItem().toString();
                fetchSites();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search_by_name = searchView.getQuery().toString();
                fetchSites();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
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
        searchView = findViewById(R.id.search_location);
        filter_target = findViewById(R.id.filter_target);
        siteRecyclerView = findViewById(R.id.site_result_list);
        filter_location_type = findViewById(R.id.filter_location_type);

        List<String> options_type = CleanUpSite.locationTypeList();
        options_type.add(0, DEFAULT_TYPE);
        ArrayAdapter<String> adapter_type = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options_type);
        adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filter_location_type.setAdapter(adapter_type);
        filter_location_type.setSelection(options_type.indexOf(DEFAULT_TYPE));


        List<String> options_target = CleanUpSite.targetList();
        options_target.add(0, DEFAULT_TARGET);
        ArrayAdapter<String> adapter_target = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options_target);
        adapter_target.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filter_target.setAdapter(adapter_target);
        filter_target.setSelection(options_target.indexOf(DEFAULT_TARGET));
    }

    void fetchSites() {
        Query query = db.collection("sites");
        if (!(pm.isLoggedIn() && pm.isSuperUser())) {
            query = query.whereEqualTo("isPrivate", false);
        }

        if (search_by_name != null && !search_by_name.isEmpty() ) {
           query = query.whereGreaterThanOrEqualTo("locationName", search_by_name);
        }

        if (selected_target != DEFAULT_TARGET) {
            query = query.whereEqualTo("targetCategory", selected_target);
        }

        if (selected_type != DEFAULT_TYPE) {
            query = query.whereEqualTo("locationType", selected_type);
        }

        query
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            try {
                                sites = result.toObjects(CleanUpSite.class);
                                Log.d(TAG, "SITE NAME: " + sites.get(0).getLocationName());

                                CleanUpSiteAdapter cleanUpSiteAdapter = new CleanUpSiteAdapter(CleanUpSites.this, R.layout.card_site_info_small, sites);
                                LinearLayoutManager linear_sites = new LinearLayoutManager(CleanUpSites.this, LinearLayoutManager.VERTICAL, false);
                                siteRecyclerView.setLayoutManager(linear_sites);
                                siteRecyclerView.setAdapter(cleanUpSiteAdapter);


                                mapSitesFragment = new MapSitesFragment();
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .setReorderingAllowed(true)
                                        .replace(R.id.fragment_map_cleanup, mapSitesFragment)
                                        .commit();
                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        }
                        else {
                            Log.d(TAG, task.getException().toString());
                        }
                    }
                });
    }


}
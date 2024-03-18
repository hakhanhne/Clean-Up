package rmit.aad.cleanup.views.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.utils.PreferenceManager;

public class Dashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener{
    MySiteFragment mySiteFragment = new MySiteFragment();
    MyJoinFragment myJoinFragment = new MyJoinFragment();
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        findViewById(R.id.screen_dashboard).setVisibility(View.VISIBLE);
        CommonFunctions.setUser(this);
        CommonFunctions.initToolbar_Nav(this, this);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.bottom_nav_my_site);
    }


    @Override
    public void onClick(View v) {}

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.bottom_nav_my_join) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_dashboard, myJoinFragment)
                    .commit();
        } else if (id == R.id.bottom_nav_my_site) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_dashboard, mySiteFragment)
                    .commit();
        }
        return CommonFunctions.navigationItemSelect(item, this);
    }

}
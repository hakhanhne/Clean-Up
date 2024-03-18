package rmit.aad.cleanup.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.views.cleanup.CleanUpSites;
import rmit.aad.cleanup.views.cleanup.JoinCleanUp;
import rmit.aad.cleanup.views.cleanup.RegisterCleanUp;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , View.OnClickListener{

    CardView cardExplore;
    CardView cardJoin;
    CardView cardRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViewById(R.id.screen_home).setVisibility(View.VISIBLE);

        CommonFunctions.setUser(this);
        CommonFunctions.initToolbar_Nav(this, this);

        initPage();

        cardExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, CleanUpSites.class));
            }
        });

        cardJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, JoinCleanUp.class));
            }
        });

        cardRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, RegisterCleanUp.class));
            }
        });
    }

    private void initPage() {
        cardExplore = findViewById(R.id.home_card_explore);
        cardJoin = findViewById(R.id.home_card_join_cleaup);
        cardRegister = findViewById(R.id.home_card_register_cleanup);
    }
    private static long back_pressed;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()){
                moveTaskToBack(true);
            }
        }
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return CommonFunctions.navigationItemSelect(item, this);
    }

}
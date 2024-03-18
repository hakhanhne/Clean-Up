package rmit.aad.cleanup.views.info;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.utils.CommonFunctions;

public class Copyright extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , View.OnClickListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copyright);
        findViewById(R.id.screen_copyright).setVisibility(View.VISIBLE);

        CommonFunctions.setUser(this);
        CommonFunctions.initToolbar_Nav(this, this);
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return CommonFunctions.navigationItemSelect(item, this);
    }
}
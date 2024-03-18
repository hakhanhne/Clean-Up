package rmit.aad.cleanup.views;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.model.User;
import rmit.aad.cleanup.utils.PreferenceManager;


public class Splash extends AppCompatActivity {

    public static String TAG = Splash.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initData();

        PreferenceManager pm = new PreferenceManager(getApplicationContext());
        if (!pm.isRemember()) {
            pm.eraseUserCredentials();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Splash.this, Home.class));
                overridePendingTransition(R.anim.fade_in, 0);
                finish();
            }
        }, 3500);
    }


    public void initData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbUsers = db.collection("users");
        CollectionReference dbSites = db.collection("sites");
        CollectionReference dbEnrolments = db.collection("enrolments");
        ObjectMapper mapper = new ObjectMapper();

        dbSites
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getDocuments().isEmpty()) {
                                mapper.registerModule(new JavaTimeModule());

                                ArrayList<CleanUpSite> sites;
                                try {
                                    sites = mapper.readValue(getAssets().open("sites.json"), new TypeReference<ArrayList<CleanUpSite>>() {});

                                    for (CleanUpSite site: sites) {
                                        dbSites
                                                .document(site.getId())
                                                .set(site)
                                                .addOnFailureListener(e -> Log.d("SPLASH", "init data: failed"));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });

        dbUsers
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getDocuments().isEmpty()) {
                                ObjectMapper mapper = new ObjectMapper();
                                mapper.registerModule(new JavaTimeModule());

                                ArrayList<User> users;
                                try {
                                    users = mapper.readValue(getAssets().open("users.json"), new TypeReference<ArrayList<User>>() {});

                                    for (User user: users) {
                                        dbUsers
                                                .document(user.getEmail())
                                                .set(user)
                                                .addOnFailureListener(e -> System.out.printf("Error adding document", e));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }

}

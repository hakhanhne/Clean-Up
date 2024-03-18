package rmit.aad.cleanup.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.views.Home;
import rmit.aad.cleanup.views.account.Dashboard;
import rmit.aad.cleanup.views.account.Login;
import rmit.aad.cleanup.views.account.Profile;
import rmit.aad.cleanup.views.account.Register;
import rmit.aad.cleanup.views.cleanup.JoinCleanUp;
import rmit.aad.cleanup.views.cleanup.RegisterCleanUp;
import rmit.aad.cleanup.views.info.AboutUs;
import rmit.aad.cleanup.views.info.Copyright;
import rmit.aad.cleanup.views.info.PrivacyPolicy;
import rmit.aad.cleanup.views.cleanup.CleanUpSites;


public class CommonFunctions {

    public static String checkRequired(EditText input) {
        String content = input.getText().toString();
        if (content.isEmpty()) {
            input.setError("This field is required");
            input.requestFocus();
            return null;
        }
        return content;
    }

    public static String formatDateToString(Date date) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    public static Date formatDate(String date_str) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return dateFormat.parse(date_str);
    }
    public static void showProgressDialog(Context context, ProgressDialog progressDialog, String message) {
        if (isNetworkAvailable(context)){
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(context,R.style.MyAlertDialogStyle);
                progressDialog.setMessage(message);
                progressDialog.setIndeterminate(true);
                progressDialog.setCanceledOnTouchOutside(false);
            }

            progressDialog.show();
            ProgressDialog finalProgressDialog = progressDialog;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isNetworkAvailable(context)){
                        hideProgressDialog(finalProgressDialog);
                        Toast.makeText(context,"No Internet Connection",Toast.LENGTH_LONG).show();

                    }
                }
            },15000);
        }else {
            Toast.makeText(context,"No Internet Connection",Toast.LENGTH_SHORT).show();
        }
    }

    public static void hideProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo !=null && networkInfo.isConnected();
    }

    public static void initToolbar_Nav(AppCompatActivity activity, NavigationView.OnNavigationItemSelectedListener navListener){
        androidx.appcompat.widget.Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        try {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().setHomeButtonEnabled(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        NavigationView navigationView = activity.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(navListener);

        Menu menu = navigationView.getMenu();
        MenuItem account_anonymous = menu.findItem(R.id.nav_account_anonymous);
        MenuItem account_logged_in = menu.findItem(R.id.nav_account_logged_in);
        PreferenceManager pm = new PreferenceManager(activity.getApplicationContext());
        if (pm.isLoggedIn()) {
            account_logged_in.setVisible(true);
            account_anonymous.setVisible(false);
        }
        else {
            account_logged_in.setVisible(false);
            account_anonymous.setVisible(true);
        }

        DrawerLayout drawerLayout = activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }


    public static boolean navigationItemSelect(MenuItem item, final Activity activity) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            activity.startActivity(new Intent().setClass(activity, Home.class));
        } else if (id == R.id.nav_sites) {
            activity.startActivity(new Intent().setClass(activity, CleanUpSites.class));
        } else if (id == R.id.nav_register_site) {
            activity.startActivity(new Intent().setClass(activity, RegisterCleanUp.class));
        } else if (id == R.id.nav_join_site) {
            activity.startActivity(new Intent().setClass(activity, JoinCleanUp.class));
        } else if (id == R.id.nav_dashboard) {
            activity.startActivity(new Intent().setClass(activity, Dashboard.class));
        } else if (id == R.id.nav_profile) {
            activity.startActivity(new Intent().setClass(activity, Profile.class));
        } else if (id == R.id.nav_logout) {
            logout(activity);
            activity.startActivity(new Intent().setClass(activity, Home.class));
        } else if (id == R.id.nav_register_account) {
            activity.startActivity(new Intent().setClass(activity, Register.class));
        } else if (id == R.id.nav_login) {
            activity.startActivity(new Intent().setClass(activity, Login.class));
        } else if (id == R.id.nav_about) {
            activity.startActivity(new Intent().setClass(activity, AboutUs.class));
        } else if (id == R.id.nav_policy) {
            activity.startActivity(new Intent().setClass(activity, PrivacyPolicy.class));
        } else if (id == R.id.nav_copyright) {
            activity.startActivity(new Intent().setClass(activity, Copyright.class));
        }

            DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }

    public static void setUser(Activity activity){
        FirebaseApp.initializeApp(activity);
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        if (firebaseUser!=null){
            NavigationView navigationView = activity.findViewById(R.id.nav_view);
            LinearLayout drawerHeader = (LinearLayout) navigationView.getHeaderView(0);

            TextView emailTextView = drawerHeader.findViewById(R.id.nav_header_email);
            TextView usernameTextView = drawerHeader.findViewById(R.id.nav_header_username);
            emailTextView.setText(firebaseUser.getEmail());
            usernameTextView.setText(firebaseUser.getDisplayName());

            setUserImageNav(activity, firebaseUser);
        }
    }

    public static void setUserImageNav(Activity activity, FirebaseUser firebaseUser) {
        NavigationView navigationView = activity.findViewById(R.id.nav_view);
        LinearLayout drawerHeader = (LinearLayout) navigationView.getHeaderView(0);
        ImageView userImage = drawerHeader.findViewById(R.id.nav_header_imageView);


        Uri photoUri = firebaseUser.getPhotoUrl();
        if(photoUri == null){
            String userName = firebaseUser.getDisplayName();
            char ch;
            if(userName != null) {
                ch = userName.charAt(0);
                TextDrawable drawable = TextDrawable.builder()
                        .buildRound(String.valueOf(ch), Color.BLUE);
                Bitmap bitmap = drawableToBitmap(drawable);
                Glide.with(activity.getApplicationContext())
                        .load(bitmap)
                        .into(userImage);
            }
        }
        else{
            Glide.with(activity.getApplicationContext())
                    .load(photoUri)
                    .into(userImage);
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 96;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 96;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static BitmapDescriptor getMarkerIconFromDrawable(Context context, int resource) {
        Drawable drawable = context.getResources().getDrawable(resource);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static void logout(Activity activity) {
        PreferenceManager pm = new PreferenceManager(activity.getApplicationContext());
        pm.eraseUserCredentials();
        FirebaseAuth loginAuth = FirebaseAuth.getInstance();
        loginAuth.signOut();
    }
}



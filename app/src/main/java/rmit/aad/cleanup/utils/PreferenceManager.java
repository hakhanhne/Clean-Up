package rmit.aad.cleanup.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PreferenceManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // shared pref mode
    private int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String DEF_USERNAME = "anonymous";
    private static final String PREF_NAME = "iitdh";
    private static final String IS_FIRST_TIME_LAUNCH = "isFirstTimeLaunch";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String IS_FIRST_GOOGLE_LOGIN = "isFirstGoogleLogin";
    private static final String IS_PASSWORD_UPDATED = "isPasswordUpdated";
    private static final String PREF_ID = "id";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_USERNAME = "username";
    private static final String IS_SUPER_USER = "isSuperUser";
    private static final String REMEMBER_ME = "isRemember";

    public PreferenceManager(Context context) {
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    public void setLoginCredentials(String email, String password){
        editor.putString(PREF_EMAIL, email);
        editor.putString(PREF_PASSWORD, password);
        editor.commit();
    }

    public void setUsername(String username){
        editor.putString(PREF_USERNAME, username);
        editor.commit();
    }

    public void setId(String id){
        editor.putString(PREF_ID, id);
        editor.commit();
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    public void setIsSuperUser(boolean isSuperUser) {
        editor.putBoolean(IS_SUPER_USER, isSuperUser);
        editor.commit();
    }

    public void setRememberMe(boolean isRemember) {
        editor.putBoolean(REMEMBER_ME, isRemember);
        editor.commit();
    }

    public void setUserCredentials(String id, String username, Boolean isSuperUser ) {
        setIsLoggedIn(true);
        setId(id);
        setUsername(username);
        setIsSuperUser(isSuperUser);
    }

    public void eraseUserCredentials() {
        setIsLoggedIn(false);
        setLoginCredentials("","");
        setUsername("");
        setIsSuperUser(false);
        setRememberMe(false);
    }

    public void setIsFirstGoogleLogin(boolean googleLogin) {
        editor.putBoolean(IS_FIRST_GOOGLE_LOGIN, googleLogin);
        editor.commit();
    }

    public void setIsPassUpdated(boolean passUpdated) {
        Log.d("ClassName1",context.getClass().getSimpleName());
        editor.putBoolean(IS_PASSWORD_UPDATED, passUpdated);
        editor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public String getPrefEmail(){
        return pref.getString(PREF_EMAIL,"");
    }
    public String getPrefPassword(){
        return pref.getString(PREF_PASSWORD,"");
    }
    public String getPrefUsername(){
        return pref.getString(PREF_USERNAME, DEF_USERNAME);
    }
    public String getPrefId(){
        return pref.getString(PREF_ID,"");
    }
    public boolean isSuperUser() {
        return pref.getBoolean(IS_SUPER_USER, false);
    }
    public boolean isRemember() {
        return pref.getBoolean(REMEMBER_ME, false);
    }

    public boolean isLoggedIn() {
        if (pref.getBoolean(IS_LOGGED_IN, false)) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            return (currentUser != null && currentUser.getEmail().equals(getPrefEmail()));
        }
        return false;
    }
    public boolean isFirstGoogleLogin() {
        return pref.getBoolean(IS_FIRST_GOOGLE_LOGIN, true);
    }

    public boolean isPassUpdated() {
        Log.d("ClassName",context.getClass().getSimpleName());
        return pref.getBoolean(IS_PASSWORD_UPDATED, false); }

    public boolean isFirstTimeLaunch() { return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true); }
}
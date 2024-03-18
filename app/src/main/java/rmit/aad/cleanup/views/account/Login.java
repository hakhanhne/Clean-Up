package rmit.aad.cleanup.views.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.User;
import rmit.aad.cleanup.utils.PreferenceManager;
import rmit.aad.cleanup.views.Home;

public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    private PreferenceManager pm;
    private FirebaseAuth mAuth;
    EditText vEmail;
    EditText vPassword ;
    CheckBox rememberMe;
    Button btnLogin;
    TextView forgotPassword;
    TextView vSignUp;
    String email = "";
    String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        initLogin();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    btnLogin.setClickable(false);
                    email = vEmail.getText().toString();
                    password = vPassword.getText().toString();

                    if(!validateInput()) {
                        btnLogin.setClickable(true);
                        return;
                    }

                    (mAuth.signInWithEmailAndPassword(email, password))
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Boolean emailFlag = checkEmailVerification();
                                    if (emailFlag) {
                                        pm.setLoginCredentials(email, password);
                                        setUserPref();
                                        btnLogin.setClickable(true);
                                    }
                                    else {
                                        Toast.makeText(Login.this, "Email verification is pending", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                        btnLogin.setClickable(true);
                                    }
                                }
                                else {
                                    btnLogin.setClickable(true);
                                    String errorCode = ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode();
                                    Log.d("error code",errorCode);
                                    switch (errorCode) {
                                        case "ERROR_INVALID_CREDENTIAL":
                                            Toast.makeText(Login.this, "Invalid credentials", Toast.LENGTH_LONG).show();
                                            break;

                                        case "ERROR_INVALID_EMAIL":
                                            Toast.makeText(Login.this, "Invalid email", Toast.LENGTH_LONG).show();
                                            vEmail.setError("Invalid email");
                                            vEmail.requestFocus();
                                            break;

                                        default:
                                            Toast.makeText(Login.this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                }
                            }
                        });

                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        vSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
    }

    private boolean validateInput() {
        if ((email == null) || email.equals("") || TextUtils.isEmpty(email) || (email.length() == 0)) {
            vEmail.setError("Please enter email");
            vEmail.requestFocus();
            return false;
        }

        if ( (password == null) || password.equals("") || TextUtils.isEmpty(password) || password.length() == 0) {
            vEmail.setError("Please enter password");
            vEmail.requestFocus();
            return false;
        }

        return true;
    }

    private Boolean checkEmailVerification(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        boolean emailFlag = firebaseUser.isEmailVerified();
        return emailFlag;
    }

    private void setUserPref() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot user = task.getResult();
                            try {
                                String id = user.getString("id");
                                String username = user.getString("username");
                                Boolean isSuperUser = user.getBoolean("isSuperUser");
                                pm.setUserCredentials(id, username, isSuperUser);
                                pm.setRememberMe(rememberMe.isChecked());
                                Toast.makeText(getApplicationContext(), " Signed In as " + username, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Login.this, Home.class));
                                finish();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                btnLogin.setClickable(true);
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Cannot get user. Please try again", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            btnLogin.setClickable(true);
                        }

                    }
                });
    }
    public void initLogin(){
        mAuth = FirebaseAuth.getInstance();
        pm = new PreferenceManager(getApplicationContext());
        vEmail = findViewById(R.id.login_email);
        vPassword = findViewById(R.id.login_password);
        rememberMe=findViewById(R.id.checkbox_remember);
        btnLogin = findViewById(R.id.button_login);
        forgotPassword=findViewById(R.id.forgot_password);
        vSignUp = findViewById(R.id.text_signup);
        vEmail.setText("");
        vPassword.setText("");
        rememberMe.setChecked(false);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

}
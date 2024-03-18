package rmit.aad.cleanup.views.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.User;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.utils.PreferenceManager;


public class Register extends AppCompatActivity {
    public static final String[] SUPER_USERS = {"hakhanhne@gmail.com"};
    private static final String TAG = "Register";
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private PreferenceManager pm;
    private EditText vEmail;
    private EditText vUsername;
    private EditText vPassword;
    private Button btnRegister;
    private TextView loginHere;
    private String email;
    private String username;
    private String password;
    private String errorCode;
    private ProgressDialog progressDialog;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        initRegister();

        loginHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // freeze all buttons while waiting for register process
                buttonsEnabled(false);

                email = vEmail.getText().toString().trim();
                username = vUsername.getText().toString().trim();
                password = vPassword.getText().toString();

                if(!validateInput()){
                    buttonsEnabled(true);
                    return;
                }

                CommonFunctions.showProgressDialog(Register.this, progressDialog, "Creating account ..." );

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            firebaseUser = firebaseAuth.getCurrentUser();
                            pm.setIsPassUpdated(true);
                            pm.setIsFirstGoogleLogin(false);
                            sendEmailVerification();
                        }
                        else {
                            buttonsEnabled(true);
                            errorCode = ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode();
                            Log.d("error code",errorCode);

                            switch (errorCode) {
                                case "ERROR_INVALID_EMAIL":
                                    Toast.makeText(Register.this, "Invalid email", Toast.LENGTH_LONG).show();
                                    vEmail.setError("Invalid email");
                                    vEmail.requestFocus();
                                    break;

                                case "ERROR_EMAIL_ALREADY_IN_USE":
//                                    Toast.makeText(Register.this, "Log In using Google and update your password", Toast.LENGTH_LONG).show();
                                    Toast.makeText(Register.this, "Account already exists", Toast.LENGTH_LONG).show();
                                    vEmail.setError("Account already exists");
                                    vEmail.requestFocus();
                                    break;

                                case "ERROR_WEAK_PASSWORD":
                                    Toast.makeText(Register.this, "Invalid password", Toast.LENGTH_LONG).show();
                                    vPassword.setError("Invalid password. Password must have at least 6 characters");
                                    vPassword.requestFocus();
                                    break;

                                case "ERROR_USER_DISABLED":
                                    Toast.makeText(Register.this, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                                    break;

                                case "ERROR_OPERATION_NOT_ALLOWED":
                                    Toast.makeText(Register.this, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                                    break;

                                default:
                                    Toast.makeText(Register.this, "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    }
                });
            }
        });
    }

    public boolean validateInput(){
        if (TextUtils.isEmpty(email) || email.equals("") || email.length() == 0) {
            vEmail.setError("Please enter password");
            vEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(username) || username.equals("") || username.length() == 0) {
            vUsername.setError("Please enter username");
            vUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || password.equals("") || password.length() == 0) {
            vPassword.setError("Please enter password");
            vPassword.requestFocus();
            return false;
        }

        if (!validateEmail(email)) {
            vEmail.setError("Invalid email");
            vEmail.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            vPassword.setError("Password must be at least 6 characters!");
            vPassword.requestFocus();
            return false;
        }
        return true;
    }

    //send email Verification
    private  void  sendEmailVerification() {
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        addUserToDatabase();
                    } else {
                        Toast.makeText(Register.this, "Verification mail failed to sent.\nCheck your network connection and try again!", Toast.LENGTH_SHORT).show();
                        buttonsEnabled(true);
                    }
                }
            });
        }
    }

    //add Userdata to database
    private void addUserToDatabase(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference dbUsers = db.collection("users");
        try {
            User user = new User(firebaseUser.getUid(), email, username, password);
            if (Arrays.asList(SUPER_USERS).contains(email)) user.setIsSuperUser(true);
            else user.setIsSuperUser(false);

            dbUsers
                .document(email)
                .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(),"Successfully Registered. Verification mail sent!",Toast.LENGTH_LONG).show();

                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            firebaseUser.updateProfile(request);
                            firebaseAuth.signOut();
                            startActivity(new Intent(Register.this, Login.class));
                            finish();
                        }
                    })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error adding document: " + e);
                        firebaseUser.delete();
                        firebaseAuth.signOut();
                        Toast.makeText(Register.this, "Failed to update data", Toast.LENGTH_SHORT).show();
                    }
                });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validateEmail(String email) {
        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void initRegister(){
        firebaseAuth = FirebaseAuth.getInstance();
        pm = new PreferenceManager(getApplicationContext());
        vEmail = findViewById(R.id.register_email);
        vUsername = findViewById(R.id.register_username);
        vPassword = findViewById(R.id.register_password);
        btnRegister = findViewById(R.id.button_register);
        loginHere = findViewById(R.id.register_bottom);
        vEmail.setText("");
        vUsername.setText("");
        vPassword.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        buttonsEnabled(true);
    }

    private void buttonsEnabled(Boolean isEnabled) {
        btnRegister.setEnabled(isEnabled);
        loginHere.setEnabled(isEnabled);
    }

}

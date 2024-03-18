package rmit.aad.cleanup.views.account;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.User;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.utils.PreferenceManager;

public class Profile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , View.OnClickListener{
    private static final String TAG = "Profile";
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    PreferenceManager pm;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    StorageReference userStorageRef;
    private User user = null;
    private Uri filePath;
    private ImageButton profileEditBtn;
    private Button btnEdit;
    private TextView profileUsername;
    private TextView profileEmail;
    private TextView profileGender;
    private Spinner genderSelect;
    private LinearLayout groupGender_view;
    private LinearLayout groupGender_edit;

    private EditText profileName;
    private EditText profilePhone;
    private EditText profileAddress;
    private EditText profileDob;
    private ImageView profilePic;
    private DrawerLayout drawerLayout;
    private final int PICK_AVATAR_REQUEST = 22;
    private final int STATE_VIEW = 1;
    private final int STATE_EDIT = 0;
    private int state = STATE_VIEW;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        pm = new PreferenceManager(getApplicationContext());

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        findViewById(R.id.screen_profile).setVisibility(View.VISIBLE);

        CommonFunctions.setUser(this);
        CommonFunctions.initToolbar_Nav(this, this);
        initPage();

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //View -> Edit
                if (state == STATE_VIEW) {
                    showAlertDialogButtonClicked(btnEdit);
                }
                //Edit -> Save -> View
                else {
                    final ProgressDialog progressDialog = new ProgressDialog(Profile.this);
                    progressDialog.setTitle("Updating profile ...");
                    progressDialog.show();

                    String name = profileName.getText().toString().trim();
                    String address = profileAddress.getText().toString().trim();
                    String phone = profilePhone.getText().toString().trim();
                    String dob = profileDob.getText().toString().trim();
                    String gender = genderSelect.getSelectedItem().toString();

                    User temp = user;
                    temp.setName(name);
                    temp.setAddress(address);
                    temp.setGender(User.Gender.valueOf(gender));
                    //validate phone
                    if (!phone.isEmpty()) {
                        if (phone.length() < 9 || phone.length() > 11) {
                            progressDialog.dismiss();
                            profilePhone.setError("Phone number is badly formatted");
                            profilePhone.requestFocus();
                            return;
                        }
                        temp.setPhone(phone);
                    }

                    //validate dob
                    if (!dob.isEmpty()) {
                        try {
                            Date date = dateFormat.parse(dob);
                            temp.setDob(date);
                        }
                        catch (Exception e) {
                            progressDialog.dismiss();
                            profileDob.setError("Date is badly formatted");
                            profileDob.requestFocus();
                            e.printStackTrace();
                            return;
                        }
                    }

                    DocumentReference docRef = db.collection("users").document(pm.getPrefEmail());
                    Log.d(TAG, "Line 136: " + docRef.toString());
                    docRef
                        .set(temp)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                state = STATE_VIEW;
                                setState();
                                if(task.isSuccessful()) {
                                    user = temp;
                                    Toast.makeText(Profile.this,"Updated successfully",Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(Profile.this,"Opps, please try again",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                }
            }
        });
    }


    public void showAlertDialogButtonClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Update");

        // add the buttons
        builder
                .setMessage("What do you want to update?")
                .setCancelable(true)
                .setPositiveButton("Avatar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        chooseImage(PICK_AVATAR_REQUEST);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Profile Info", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        state = STATE_EDIT;
                        setState();
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void initPage() {
        btnEdit = findViewById(R.id.btn_edit_profile);
        profilePic = findViewById(R.id.profile_pic);
        profileUsername = findViewById(R.id.profile_username);
        profileEmail = findViewById(R.id.profile_email);
        profileName = findViewById(R.id.profile_name);
        profilePhone = findViewById(R.id.profile_phone);
        profileAddress = findViewById(R.id.profile_address);
        profileDob = findViewById(R.id.profile_dob);
        profileGender = findViewById(R.id.profile_gender);
        genderSelect = findViewById(R.id.profile_gender_spinner);
        groupGender_view = findViewById(R.id.group_gender_view);
        groupGender_edit = findViewById(R.id.group_gender_edit);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, User.genderList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSelect.setAdapter(adapter);

        setState();
        initData();
    }

    private void initData() {
        db = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userStorageRef = FirebaseStorage.getInstance().getReference().child(pm.getPrefEmail());

        DocumentReference docRef = db.collection("users").document(pm.getPrefEmail());
        docRef
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    if (user.getUsername() != null) profileUsername.setText(user.getUsername());
                    if (user.getEmail() != null) profileEmail.setText(user.getEmail());
                    if (user.getName() != null) profileName.setText(user.getName());
                    if (user.getPhone() != null) profilePhone.setText(user.getPhone());
                    if (user.getAddress() != null) profileAddress.setText(user.getAddress());
                    if (user.getDob() != null) {
                        try {
                            profileDob.setText(dateFormat.format(user.getDob()));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (user.getGender() != null) profileGender.setText(user.getGender().toString());
                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        RequestOptions requestOption = new RequestOptions()
                                .placeholder(R.drawable.img_avatar)
                                .error(R.drawable.img_avatar)
                                .fitCenter();

                        Glide.with(Profile.this)
                                .load(user.getAvatar())
                                .apply(requestOption)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        Toast.makeText(getApplicationContext(),"Failed to load profile pic",Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .into(profilePic);
                    }
                }
                else Log.d(TAG, "USER IS NULL");
            })
            .addOnFailureListener(e -> {
                Log.d(TAG, "Error getting documents: " + e.getMessage());
            });
    }

    private void chooseImage(int imageReq) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), imageReq);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_AVATAR_REQUEST && resultCode == RESULT_OK  && data != null && data.getData() != null ) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                Glide.with(this)
                        .load(bitmap)
                        .into(profilePic);
                uploadImage();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void uploadImage() {
        if(filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            try {
                StorageReference avatarRef =  userStorageRef.child("images/avatar");

                avatarRef.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(Profile.this, "Uploaded", Toast.LENGTH_SHORT).show();

                                avatarRef
                                        .getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(final Uri uri) {
                                                Log.d("Pic Url Fetching","success");
                                                DocumentReference docRef = db.collection("users").document(pm.getPrefEmail());

                                                docRef
                                                        .update("avatar", uri.toString())
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                user.setAvatar(uri.toString());
                                                                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                                                        .setPhotoUri(uri)
                                                                        .build();
                                                                firebaseUser.updateProfile(request);
                                                                CommonFunctions.setUserImageNav(Profile.this, firebaseUser);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d("Pic Url Uploading","failed");
                                                                Toast.makeText(getApplicationContext(),"Network error!\nFailed to upload pic.",Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                Log.d("Pic Url Fetching","failure");
                                                Toast.makeText(getApplicationContext(),"Network error!\n Failed to upload pic.",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(Profile.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0*snapshot.getBytesTransferred()/snapshot
                                        .getTotalByteCount());
                                progressDialog.setMessage("Uploaded "+(int)progress+"%");
                            }
                        });

            } catch (Exception e) {
                Log.d(TAG, "Line 372: " + e.getMessage());
            }

        }
    }


    private void setState() {
        switch (state) {
            case STATE_VIEW:
                editEnabled(false);
                break;
            case STATE_EDIT:
                editEnabled(true);
                break;
            default:
                editEnabled(false);
                break;
        }
    }

    private void editEnabled(boolean isEnabled) {
        profileName.setEnabled(isEnabled);
        profileAddress.setEnabled(isEnabled);
        profilePhone.setEnabled(isEnabled);
        profileDob.setEnabled(isEnabled);
        if (isEnabled) {
            groupGender_view.setVisibility(View.INVISIBLE);
            groupGender_edit.setVisibility(View.VISIBLE);
            btnEdit.setText("Save changes");
            btnEdit.setBackgroundColor(getResources().getColor(R.color.colorPrimaryVariant));
        }
        else {
            groupGender_view.setVisibility(View.VISIBLE);
            groupGender_edit.setVisibility(View.INVISIBLE);
            btnEdit.setText("Edit profile");
            btnEdit.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }


    @Override
    public void onClick(View v) {}

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return CommonFunctions.navigationItemSelect(item, this);
    }
}
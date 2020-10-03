package com.breejemodi.exxamdesk.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.breejemodi.exxamdesk.MainActivity;
import com.breejemodi.exxamdesk.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsernameActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private Button btnRemove, btnCreateAccount;
    private ProgressBar progressBar;
    private EditText edtUserName, edtBranch, edtCollegeName, edtYear;
    private Uri photoUri;
    private StorageReference storage;
    private FirebaseAuth firebaseAuth;
    private String url="";
    private FirebaseFirestore firestore;

    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        init();
        storage = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dexter.withContext(UsernameActivity.this)
                        .withPermissions(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).withListener(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if(report.areAllPermissionsGranted()){
                            selectImage();
                        }else{
                            Toast.makeText(UsernameActivity.this,"Please Allow Permission",Toast.LENGTH_LONG).show();
                        }

                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                }).check();


            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoUri = null;
                profileImageView.setImageResource(R.drawable.profile);
            }
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtUserName.setError(null);
                edtBranch.setError(null);
                edtCollegeName.setError(null);
                edtYear.setError(null);

                if (edtUserName.getText().toString().isEmpty() || edtUserName.getText().toString().length() < 3) {
                    edtUserName.setError("Username should have more than 3 characters!");
                    return;
                }
//                if (!edtUserName.getText().toString().matches(USERNAME_PATTERN)) {
//                    edtUserName.setError("Invalid Username!");
//                    return;
//                }
                if (edtBranch.getText().toString().isEmpty()) {
                    edtBranch.setError("Invalid Branch!");
                    return;
                }
                if (edtCollegeName.getText().toString().isEmpty()) {
                    edtCollegeName.setError("Invalid College Name!");
                    return;
                }
                if (edtYear.getText().toString().isEmpty() || edtYear.getText().toString().length() > 1) {
                    edtYear.setError("Year Number should be: (1-4)!");
                    return;
                }

                //TO BE DELETED
                Intent mainIntent = new Intent(UsernameActivity.this, MainActivity.class);
                startActivity(mainIntent);
                //TILL HERE


                progressBar.setVisibility(View.VISIBLE);
                firestore.collection("StudentUsers").whereEqualTo("Username", edtUserName.getText().toString()).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                               if (task.isSuccessful()) {
                                   List<DocumentSnapshot> document = task.getResult().getDocuments();
                                   if (document.isEmpty()) {
                                       uploadData();
                                   } else {
                                       progressBar.setVisibility(View.INVISIBLE);
                                       edtUserName.setError("Already Exist!");
                                   }
                               } else {
                                   String error = task.getException().getMessage();
                                   Toast.makeText(UsernameActivity.this, error, Toast.LENGTH_LONG);
                                   progressBar.setVisibility(View.INVISIBLE);

                               }
                           }
                       });

            }
        });
    }

    private void uploadData(){

        if(photoUri!=null){// Upload Photo with Data

            final StorageReference ref = storage.child("profile/"+ firebaseAuth.getCurrentUser().getUid());
            UploadTask uploadTask = ref.putFile(photoUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        String error = task.getException().getMessage();
                        Toast.makeText(UsernameActivity.this, error, Toast.LENGTH_LONG);
                        progressBar.setVisibility(View.INVISIBLE);
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            url = uri.toString();
                        }
                    });
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        UploadUserdata();
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(UsernameActivity.this, error, Toast.LENGTH_LONG);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });

        }else{// Upload only Data
            UploadUserdata();
        }

    }

    private void UploadUserdata(){

        Map<String, Object> map = new HashMap<>();
        map.put("Username",edtUserName.getText().toString());
        map.put("Profile_Url",url);
        map.put("College", edtCollegeName.getText().toString());
        map.put("Batch", edtYear.getText().toString());
        map.put("Branch", edtBranch.getText().toString());

        firestore.collection("StudentUsers").document(firebaseAuth.getCurrentUser().getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent mainIntent = new Intent(UsernameActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(UsernameActivity.this, error, Toast.LENGTH_LONG);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void selectImage() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityMenuIconColor(getResources().getColor(R.color.colorAccent))
                .setBackgroundColor(getResources().getColor(R.color.colorPrimary))
                .setActivityTitle("Profile Image")
                .setFixAspectRatio(true)
                .setAspectRatio(1,1)
                .start(UsernameActivity.this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri photoUri = result.getUri();
                Glide
                        .with(this)
                        .load(photoUri)
                        .centerCrop()
                        .placeholder(R.drawable.profile)
                        .into(profileImageView);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void init(){

        profileImageView = findViewById(R.id.imgProfileImage);
        btnCreateAccount = findViewById(R.id.btnCreateAccountButton);
        btnRemove = findViewById(R.id.btnRemove);
        progressBar = findViewById(R.id.progressBar3);
        edtBranch = findViewById(R.id.edtBranch);
        edtCollegeName = findViewById(R.id.collegeName);
        edtUserName = findViewById(R.id.edtUserNameText);
        edtYear = findViewById(R.id.edtCurrentYear);


    }
}
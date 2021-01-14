package com.example.dlpbgj;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/*
 * Reference taken from this website for uploading the photo inside the database.
 * https://www.geeksforgeeks.org/android-how-to-upload-an-image-on-firebase-storage/
 */


public class UserProfile extends AppCompatActivity {

    private final int REQUEST = 22;
    FirebaseStorage storage;
    StorageReference storageReference;
    private ImageView imageView;
    private Uri path;
    DatePickerDialog.OnDateSetListener listener;
    String DOB;
    TextView UserBirthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        final User user = (User) getIntent().getSerializableExtra("User");
        Button update;
        Button back;
        final FirebaseFirestore userDb;
        final TextInputEditText UserFirstName = findViewById(R.id.UserFirstName);
        final TextInputEditText  UserLastName = findViewById(R.id.UserLastName);
        UserBirthDate = findViewById(R.id.UserBirthDate);
        final TextInputEditText UserName = findViewById(R.id.UserName);
        final TextInputEditText  UserEmail = findViewById(R.id.emailAddress);
        final TextInputEditText  UserPhone = findViewById(R.id.phoneNumber);
        final TextInputEditText  UserGenre = findViewById(R.id.UserFav);
        userDb = FirebaseFirestore.getInstance();
        back = findViewById(R.id.BackButton);
        update = findViewById(R.id.Update);

        Button dateSelect = findViewById(R.id.select_date);
        Button photoSelect = findViewById(R.id.button1);
        Button photoUpload = findViewById(R.id.button2);
        imageView = findViewById(R.id.imgView);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        photoSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPhoto();
            }
        });

        photoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto(user);
            }
        });

        dateSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int date = calendar.get(Calendar.DATE);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog dialog = new DatePickerDialog(UserProfile.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,listener,year,month,date);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                       DOB = i2 + "/" + i1 + "/" + i ;
                       UserBirthDate.setText(DOB);
            }
        };
        final CollectionReference userBookCollectionReference = userDb.collection("Users");
        DocumentReference docRef = userBookCollectionReference.document(user.getUsername());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        user.setFirst_name((String) data.get("First Name"));
                        user.setLast_name((String) data.get("Last Name"));
                        user.setDOB((String) data.get("Date of Birth"));
                        user.setEmail((String) data.get("Email"));
                        user.setPhone((String) data.get("Phone"));
                        user.setGenre((String) data.get("Genre"));
                        UserFirstName.setText(user.getFirst_name());
                        UserLastName.setText(user.getLast_name());
                        UserBirthDate.setText(user.getDOB());
                        UserName.setText(user.getUsername());
                        UserEmail.setText(user.getEmail());
                        UserPhone.setText(user.getPhone());
                        UserGenre.setText(user.getGenre());

                        StorageReference imagesRef = storageReference.child("images/" + user.getUsername());
                        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                Glide
                                        .with(getApplicationContext())
                                        .load(downloadUrl.toString())
                                        .centerCrop()
                                        .into(imageView);
                            }
                        });
                    }
                } else {
                    Log.d("UserProfile", "get failed with ", task.getException());
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),HomePage.class);
                intent.putExtra(MainActivity.EXTRA_MESSAGE1,user);
                startActivity(intent);
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String FirstName = UserFirstName.getText().toString();
                final String LastName = UserLastName.getText().toString();
                final String BirthDate = UserBirthDate.getText().toString();
                final String Email = UserEmail.getText().toString();
                final String Phone = UserPhone.getText().toString();
                final String Genre = UserGenre.getText().toString();
                user.setFirst_name(FirstName);
                user.setLast_name(LastName);
                user.setEmail(Email);
                user.setDOB(BirthDate);
                user.setPhone(Phone);
                user.setGenre(Genre);
                HashMap<String, Object> data = new HashMap<>();
                data.put("First Name", FirstName);
                data.put("Last Name", LastName);
                data.put("Date of Birth", BirthDate);
                data.put("Email", Email);
                data.put("Phone",Phone);
                data.put("Genre",Genre);
                userBookCollectionReference
                        .document(user.getUsername())
                        .update(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("UserProfile", "Data has been updated successfully!");
                                Toast toast = Toast.makeText(getApplicationContext(), "Profile Successfully Updated!", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("UserProfile", "Failed to update the values!");
                                Toast toast = Toast.makeText(getApplicationContext(), "Failed to Update the profile!", Toast.LENGTH_SHORT);
                            }
                        });
            }
        });
    }

    private void SelectPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            path = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadPhoto(User user) {
        if (path != null) {
            final ProgressDialog statusDialog = new ProgressDialog(this);
            statusDialog.setTitle("Uploading");
            statusDialog.show();
            StorageReference ref = storageReference.child("images/" + user.getUsername());
            ref.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    statusDialog.dismiss();
                    Toast.makeText(UserProfile.this, "Uploaded!!", Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            statusDialog.dismiss();
                            Toast.makeText(UserProfile.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            statusDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }
}
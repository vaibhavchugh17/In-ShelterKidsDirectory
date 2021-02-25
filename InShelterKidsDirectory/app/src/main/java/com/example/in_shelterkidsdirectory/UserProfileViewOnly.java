package com.example.in_shelterkidsdirectory;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserProfileViewOnly extends AppCompatActivity {
    User user;
    FirebaseFirestore userDb;
    TextInputEditText UserFirstName;
    TextInputEditText  UserLastName;
    TextInputEditText UserBirthDate;
    TextInputEditText UserName;
    TextInputEditText  UserEmail;
    TextInputEditText  UserPhone;
    TextInputEditText  UserGenre;
    ImageView img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_view_only);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ActionBar actionBar = getSupportActionBar();

        user = (User) getIntent().getSerializableExtra("sendingUser");
        actionBar.setTitle(user.getUsername() + "'s Details");
        UserFirstName = findViewById(R.id.UserFirstNameView);
        UserLastName = findViewById(R.id.UserLastNameView);
        UserBirthDate = findViewById(R.id.UserBirthDateView);
        UserName = findViewById(R.id.UserNameView);
        UserEmail = findViewById(R.id.emailAddressView);
        UserPhone = findViewById(R.id.phoneNumberView);
        UserGenre = findViewById(R.id.UserFavView);
        img = findViewById(R.id.imgViewOnly);

        UserFirstName.setText(user.getFirst_name());
        UserLastName.setText(user.getLast_name());
        UserBirthDate.setText(user.getDOB());
        UserName.setText(user.getUsername());
        UserEmail.setText(user.getEmail());
        UserPhone.setText(user.getPhone());
        UserGenre.setText(user.getGenre());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        StorageReference imagesRef = storageReference.child("images/" + user.getUsername());
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUrl) {
                Glide
                        .with(getApplicationContext())
                        .load(downloadUrl.toString())
                        .centerCrop()
                        .into(img);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
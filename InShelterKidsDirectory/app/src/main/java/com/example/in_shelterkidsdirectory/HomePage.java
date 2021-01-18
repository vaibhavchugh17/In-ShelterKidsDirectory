package com.example.in_shelterkidsdirectory;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

//As soon as the user successfully logs in, this activity gets invoked. This is the home page of the user.
public class HomePage extends AppCompatActivity implements ImageFragment.OnFragmentInteractionListener {
    public static final String EXTRA_MESSAGE2 = "com.example.dlpbgj.MESSAGE2";

    ImageButton info_button;
    ImageButton userProfiles;
    ImageButton KidsButton;
    ImageButton search;
    ImageButton signOut;
    FirebaseStorage storage;
    FirebaseFirestore Userdb;
    private User currentUser;

    /**
     * Activity is launched when a user successfully signs in.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        currentUser = (User) getIntent().getSerializableExtra(MainActivity.EXTRA_MESSAGE1);//Catching the user object given by the MainActivity
        info_button = findViewById(R.id.MyInfo);
        userProfiles = findViewById(R.id.UserProfiles);
        KidsButton = findViewById(R.id.AllKids);
        search = findViewById(R.id.Search);
        signOut = findViewById(R.id.SignOut);
        final ImageView profile = findViewById(R.id.Profile);
        storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReference();
        final TextView userName = findViewById(R.id.MyName);
        Userdb = FirebaseFirestore.getInstance();
        final String success = "Signed Out!";


        final CollectionReference KidCollectionReference = Userdb.collection("Users");
        DocumentReference docRef = KidCollectionReference.document(currentUser.getUsername()); //If username does not exist then prompt for a sign-up
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        String name="";
                        Map<String,Object> data = document.getData();
                        String firstName = (String) data.get("First Name");
                        String lastName = (String) data.get("Last Name");
                        if(firstName==null || lastName==null){
                            name="Unknown";
                        }
                        else {
                            if (firstName.equals("")) {
                                if (lastName.equals("")) {
                                    name = "Unknown";
                                } else {
                                    name = lastName;
                                }
                            } else {
                                if (lastName.equals("")) {
                                    name = firstName;
                                }
                                name = firstName + " " + lastName;
                            }
                        }
                        name += "'s Library";

                        userName.setText(name);
                        StorageReference imagesRef = storageReference.child("images/" + currentUser.getUsername());
                        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                Glide
                                        .with(getApplicationContext())
                                        .load(downloadUrl.toString())
                                        .centerCrop()
                                        .into(profile);
                            }
                        });
                    }
                } else {
                    Log.d("HomePage", "User get failed with ", task.getException());
                }
            }
        });
        /**
         * on press of button Kids the activity to view all the kids
         * is initialized
         */


        KidsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {        //When user clicks this button, a list of all the kids is shown
                Intent intent = new Intent(getApplicationContext(), MyBooks.class);
                intent.putExtra(EXTRA_MESSAGE2, currentUser);   //Sending the current user as a parameter to the MyBooks activity
                startActivity(intent);
            }
        });
        /**
         * on press of signOut, user is signed out and sent back to the main activity
         */

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nIntent = new Intent(view.getContext(), MainActivity.class);
                Toast toast = Toast.makeText(view.getContext(), success, Toast.LENGTH_SHORT);
                toast.show();
                startActivity(nIntent);
            }
        });
        /**
         * on press of button UserProfiles
         * is initialized
         */

        userProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {        //When user clicks this button, a list of all users using the app is shown
                Intent intent = new Intent(getApplicationContext(), allUserProfiles.class);
                intent.putExtra(EXTRA_MESSAGE2, currentUser);   //Sending the current user as a parameter to the allUserProfiles activity
                startActivity(intent);
            }
        });
        /**
         * on press of button info_button the activity to User info
         * is initialized
         */

        info_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);
            }
        });
        /**
         * on press of button Search the activity to search through all the kids
         * is initialized
         */

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Search_by_descr.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);
            }
        });


        /**
         * on press of button my info the activity to configure my info
         * is initialized
         */
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageFragment fragment = ImageFragment.newInstance(currentUser);
                fragment.show(getSupportFragmentManager(), "Profile Picture");
            }
        });


    }

    @Override
    public void onBackPressed() {
        //do nothing
    }
}
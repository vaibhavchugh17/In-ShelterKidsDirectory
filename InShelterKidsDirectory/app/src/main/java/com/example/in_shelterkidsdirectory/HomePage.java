package com.example.in_shelterkidsdirectory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

//As soon as the user successfully logs in, this activity gets invoked. This is the home page of the user.
public class HomePage extends AppCompatActivity implements ImageFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {
    public static final String EXTRA_MESSAGE2 = "com.example.dlpbgj.MESSAGE2";

    ImageButton userProfiles;
    ImageButton KidsButton;
    FirebaseStorage storage;
    FirebaseFirestore Userdb;
    private User currentUser;
    StorageReference storageReference;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;


    /**
     * Activity is launched when a user successfully signs in.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);


        currentUser = (User) getIntent().getSerializableExtra(MainActivity.EXTRA_MESSAGE1);//Catching the user object given by the MainActivity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Welcome!");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        userProfiles = findViewById(R.id.UserProfiles);
        KidsButton = findViewById(R.id.AllKids);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
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
                Intent intent = new Intent(getApplicationContext(), Kids.class);
                intent.putExtra(EXTRA_MESSAGE2, currentUser);   //Sending the current user as a parameter to the MyBooks activity
                startActivity(intent);
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




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main,menu);
        StorageReference imagesRef = storageReference.child("images/" + currentUser.getUsername());
        MenuItem menuItem = menu.findItem(R.id.itemProfile);
        View view = MenuItemCompat.getActionView((MenuItem) menuItem);
        CircleImageView profileImage = view.findViewById(R.id.appbar_profile_image);

        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUrl) {
                Glide
                        .with(getApplicationContext())
                        .load(downloadUrl.toString())
                        .centerCrop()
                        .into(profileImage);
            }
        });


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);

            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

            case R.id.itemSearch:
                Intent intent = new Intent(getApplicationContext(), Search_by_descr.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);
                break;
            case R.id.itemProfile:
                Toast.makeText(this,"profileClicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.itemLogOut:
                Intent nIntent = new Intent(HomePage.this, MainActivity.class);
                Toast toast = Toast.makeText(HomePage.this, "Signed Out!", Toast.LENGTH_SHORT);
                toast.show();
                startActivity(nIntent);
                break;


        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onBackPressed() {
        //do nothing
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_myProfile:
                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);
                break;
            case R.id.nav_allProfiles:
                Intent intent1 = new Intent(getApplicationContext(), allUserProfiles.class);
                intent1.putExtra(EXTRA_MESSAGE2, currentUser);   //Sending the current user as a parameter to the allUserProfiles activity
                startActivity(intent1);
                break;
            case R.id.nav_logout:
                Intent nIntent = new Intent(HomePage.this, MainActivity.class);
                Toast toast = Toast.makeText(HomePage.this, "Signed Out!", Toast.LENGTH_SHORT);
                toast.show();
                startActivity(nIntent);
                break;

        }


        return true;
    }
}
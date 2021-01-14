package com.example.dlpbgj;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class allUserProfiles extends AppCompatActivity {
    ArrayList<String> profileList;
    ArrayAdapter<String> profileAdapter;
    ListView profiles;
    FirebaseFirestore db;
    CollectionReference userCollection;
    DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user_profiles);
        final User user = (User) getIntent().getSerializableExtra(HomePage.EXTRA_MESSAGE2);
        profileList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        userCollection = db.collection("Users");
        userCollection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Log.d("sucesss", doc.getId() + " => " + doc.getData());
                                if (user.getUsername().equals(doc.getId())){
                                    continue;
                                }
                                System.out.println(doc.getId());
                                profileList.add(doc.getId());
                            }
                            profiles = findViewById(R.id.profile_list);
                            profileAdapter = new customProfileAdapter(getApplicationContext(),profileList);
                            profiles.setAdapter(profileAdapter);

                            profiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String username = profileList.get(position);
                                    docRef = userCollection.document(username);
                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Map<String, Object> data = document.getData();
                                                    String pass = (String) data.get("Password");
                                                    String fn = (String) data.get("First Name");
                                                    String ln = (String) data.get("Last Name");
                                                    String dob = (String) data.get("Date of Birth");
                                                    String em = (String) data.get("Email");
                                                    String ph = (String) data.get("Phone");
                                                    String gn = (String) data.get("Genre");
                                                    User newUser = new User(username, pass);
                                                    newUser.setFirst_name(fn);
                                                    newUser.setLast_name(ln);
                                                    newUser.setDOB(dob);
                                                    newUser.setEmail(em);
                                                    newUser.setGenre(gn);
                                                    newUser.setPhone(ph);
                                                    Intent intent = new Intent(getApplicationContext(), UserProfileViewOnly.class);
                                                    intent.putExtra("sendingUser", newUser);   //Sending the current user as a parameter to the UserProfileViewOnly activity
                                                    startActivity(intent);
                                                }
                                            } else {
                                                Log.d("UserProfile", "get failed with ", task.getException());
                                            }
                                        }
                                    });


                                }

                            });

                        }
                            else {
                            Log.d("fail", "Error getting documents: ", task.getException());
                        }
                    }
                });


    }
}
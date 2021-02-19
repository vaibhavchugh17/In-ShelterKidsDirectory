package com.example.in_shelterkidsdirectory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class allUserProfiles extends AppCompatActivity implements CommonFragment.OnFragmentInteractionListener,Serializable {
    ArrayList<Parent> profileList;
    ArrayAdapter<Parent> profileAdapter;
    ListView referrals;
    FirebaseFirestore db;
    String name;
    Kid kid;
    CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user_profiles);
        kid = (Kid) getIntent().getSerializableExtra("Kid");
        referrals = findViewById(R.id.profile_list);
        profileList = new ArrayList<>();
        profileAdapter = new customProfileAdapter(this,profileList);   //Implementing a custom adapter that connects the ListView with the ArrayList using kidcontent.xml layout
        referrals.setAdapter(profileAdapter);
        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Kids");

        name = kid.getFirstName() + kid.getLastName() + kid.getUID();
        profileList.clear();
        profileAdapter.notifyDataSetChanged();
        for (Parent parent : kid.getReferrals()) {
            if (!profileList.contains(parent)){
                profileList.add(parent);
            }
            profileAdapter.notifyDataSetChanged();
        }
        DocumentReference docRef = collectionReference.document(name);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        CollectionReference referralsCollection = db.collection("Kids/" + name + "/Referrals");
                        referralsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                for (QueryDocumentSnapshot referral : value) {
                                    String referral_firstName = (String)referral.getData().get("First Name");    //Title of the kid will be the ID of the document representing the kid inside the sub-collections of MyKids
                                    String referral_lastName = (String)referral.getData().get("Last Name");
                                    String referral_dob = (String)referral.getData().get("DOB");
                                    String referral_phoneNumber = (String)referral.getData().get("Phone Number");
                                    String referral_occupation = (String)referral.getData().get("Occupation");
                                    String referral_home_address = (String)referral.getData().get("Address");
                                    Parent temp = new Parent(referral_firstName,referral_lastName,referral_dob,referral_home_address,referral_occupation,referral_phoneNumber);
                                    profileList.add(temp); // Adding the cities and provinces from FireStore
                                    profileAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    } else {
                        Log.d("Referral","Referral does not exist in database");
                    }
                } else {
                    Log.d("Referrals", "get failed with ", task.getException());
                }
            }
        });
        final FloatingActionButton addReferralButton = findViewById(R.id.add_kid_button);
        addReferralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonFragment fragment = CommonFragment.newInstance(kid, "Referral");
                fragment.show(getSupportFragmentManager(),"Add_Referral");
            }
        });
        referrals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Parent temp = profileList.get(i);
                CommonFragment fragment = CommonFragment.newInstance(kid,temp);
                fragment.show(getSupportFragmentManager(), "ADD_Referral");
            }
        });

    }
    @Override
    public void onAddPressed(){
        for (Parent parent : kid.getReferrals()) {
            if (!profileList.contains(parent)){
                profileList.add(parent);
            }
            profileAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("Kid",kid);
        setResult(RESULT_OK,returnIntent);
        finish();
    }
}

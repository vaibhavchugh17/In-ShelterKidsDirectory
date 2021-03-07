package com.example.in_shelterkidsdirectory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

public class referrals extends AppCompatActivity implements CommonFragment.OnFragmentInteractionListener,Serializable {
    ArrayList<Parent> profileList;
    ArrayAdapter<Parent> profileAdapter;
    ListView referrals;
    FirebaseFirestore db;
    Kid kid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_referrals);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        kid = (Kid) getIntent().getSerializableExtra("Kid");
        String n = kid.getFirstName();

        ActionBar actionBar = getSupportActionBar();
        if(n == null ) {
            actionBar.setTitle("Referrals");
        }
        else{
            actionBar.setTitle(n + "'s Referrals");
        }
        referrals = findViewById(R.id.profile_list);
        profileList = kid.getReferrals();
        profileAdapter = new customReferralAdapter(this,profileList);   //Implementing a custom adapter that connects the ListView with the ArrayList using kidcontent.xml layout
        referrals.setAdapter(profileAdapter);
        db = FirebaseFirestore.getInstance();
        profileAdapter.notifyDataSetChanged();

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
    public void onDeletePressed(Parent referral){
        CollectionReference collectionReference = db.collection("Kids/" + kid.getFirstName()+kid.getLastName()+kid.getUID() + "/Referrals/");
        collectionReference
                .document(referral.getFirstName())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Referral","Delete Success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Referral","Delete failed");
                    }
                });
        profileList.remove(referral);
        profileAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("Kid",kid);
        setResult(RESULT_OK,returnIntent);
        finish();
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

package com.example.in_shelterkidsdirectory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Search_by_descr extends AppCompatActivity implements AddKidFragment.OnFragmentInteractionListener, SelectionFragment.OnFragmentInteractionListener,CommonFragment.OnFragmentInteractionListener{
    ListView kidList;
    String first;
    String last;
    String middle;
    String status;
    String descrInput;
    ArrayAdapter<Kid> kidAdapter;
    ArrayList<Kid> kidDataList;
    ArrayList<Kid> allKids;
    ArrayList<Kid> filteredDataList;
    ArrayAdapter<Kid> filteredKidAdapter;
    FirebaseFirestore db;
    EditText description;
    Button search;
    CollectionReference userKidCollectionReference;
    String TAG = "KidSearch";
    CheckBox checkResidential;
    CheckBox checkOut;
    String residentialConstraint = "Residential";
    String outConstraint = "Out-Reach";
    private User currentUser;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_books);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Search");

        HashMap<String,Object> extras = (HashMap<String, Object>) getIntent().getSerializableExtra("Extras");
        currentUser = (User) extras.get("User");
        allKids = (ArrayList<Kid>) extras.get("Kids");
        kidList = findViewById(R.id.kid_list);
        description = findViewById(R.id.description);
        search = findViewById(R.id.search);
        checkResidential = findViewById(R.id.check_available);
        checkOut = findViewById(R.id.check_borrowed);

        kidDataList = new ArrayList<>();
        filteredDataList = new ArrayList<>();
        kidAdapter = new customKidAdapter(this, kidDataList);
        filteredKidAdapter = new customKidAdapter(this, filteredDataList);
        kidList.setAdapter(kidAdapter);

        db = FirebaseFirestore.getInstance();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kidDataList.clear();
                kidAdapter.notifyDataSetChanged();
                descrInput = description.getText().toString();
                for (Kid kid : allKids){
                    first = kid.getFirstName();
                    middle = kid.getMiddleName();
                    last = kid.getLastName();
                    status = kid.getStatus();
                    if (first.toUpperCase().contains(descrInput.toUpperCase()) || last.toUpperCase().contains(descrInput.toUpperCase()) || middle.toUpperCase().contains(descrInput.toUpperCase()) || first.toLowerCase().contains(descrInput.toLowerCase()) || last.toLowerCase().contains(descrInput.toLowerCase()) || middle.toLowerCase().contains(descrInput.toLowerCase())){
                        kidDataList.add(kid);
                        kidAdapter.notifyDataSetChanged();
                        if (checkResidential.isChecked() && checkOut.isChecked()) {
                            if (!(status.equals(residentialConstraint) || status.equals(outConstraint))) {
                                kidDataList.remove(kid);
                                kidAdapter.notifyDataSetChanged();
                            }
                        }
                        if (checkOut.isChecked() && !checkResidential.isChecked()) {
                            if (!(status.equals(outConstraint))) {
                                kidDataList.remove(kid);
                                kidAdapter.notifyDataSetChanged();
                            }
                        }
                        if (!checkOut.isChecked() && checkResidential.isChecked()) {
                            if (!(status.equals(residentialConstraint))) {
                                kidDataList.remove(kid);
                                kidAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });

        checkResidential.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filteredDataList.clear();
                if (isChecked) {
                    for (int i = 0; i < kidDataList.size(); i++) {
                        Kid kid = kidDataList.get(i);
                        if (checkOut.isChecked()) {
                            if (kid.getStatus().equals(residentialConstraint) || kid.getStatus().equals(outConstraint))
                                filteredDataList.add(kid);
                        } else {
                            if (kid.getStatus().equals(residentialConstraint))
                                filteredDataList.add(kid);
                        }
                    }
                    filteredKidAdapter.notifyDataSetChanged();
                    kidList.setAdapter(filteredKidAdapter);
                } else {
                    if (!checkOut.isChecked())
                        kidList.setAdapter(kidAdapter);
                    else {
                        for (int i = 0; i < kidDataList.size(); i++) {
                            Kid kid = kidDataList.get(i);
                            filteredDataList.add(kid);
                            if (!(kid.getStatus().equals(outConstraint))) {
                                filteredDataList.remove(kid);
                            }

                        }
                        filteredKidAdapter.notifyDataSetChanged();
                        kidList.setAdapter(filteredKidAdapter);
                    }
                }
            }
        });

        checkOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filteredDataList.clear();
                if (isChecked) {
                    for (int i = 0; i < kidDataList.size(); i++) {
                        Kid kid = kidDataList.get(i);
                        if (checkResidential.isChecked()) {
                            if (kid.getStatus().equals(residentialConstraint) || kid.getStatus().equals(outConstraint))
                                filteredDataList.add(kid);
                        } else {
                            if (kid.getStatus().equals(outConstraint))
                                filteredDataList.add(kid);
                        }


                    }
                    filteredKidAdapter.notifyDataSetChanged();
                    kidList.setAdapter(filteredKidAdapter);

                } else {
                    if (!checkResidential.isChecked())
                        kidList.setAdapter(kidAdapter);
                    else {
                        for (int i = 0; i < kidDataList.size(); i++) {
                            Kid kid = kidDataList.get(i);
                            filteredDataList.add(kid);
                            if (!(kid.getStatus().equals(residentialConstraint))) {
                                filteredDataList.remove(kid);
                            }

                        }
                        filteredKidAdapter.notifyDataSetChanged();
                        kidList.setAdapter(filteredKidAdapter);

                    }

                }
            }
        });


        kidList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Kid temp = kidDataList.get(i);
                AddKidFragment fragment = AddKidFragment.newInstance(temp, currentUser);
                fragment.show(getSupportFragmentManager(), "ADD_BOOK");
            }
        });

    }

    @Override
    public void onOkPressed(final Kid newKid) {
    }

    /**
     * This function is used for the fragment that gives the user the option to edit a kid or delete it.
     *
     *
     * @param oldKidName
     */
    @Override
    public void onOkPressed(final Kid kid, final String oldKidName) {
        final HashMap<String, Object> data = new HashMap<>();
        data.put("First Name", kid.getFirstName());
        data.put("Last Name", kid.getLastName());
        data.put("Status", kid.getStatus());
        data.put("Middle Name", kid.getMiddleName());
        data.put("DOB", kid.getDOB());
        data.put("Height", kid.getHeight());
        data.put("Hair Color", kid.getHairColor());
        data.put("Eye Color", kid.getEyeColor());
        data.put("Allergies", kid.getAllergies());
        data.put("Birthmarks", kid.getBirthmarks());
        data.put("Nationality", kid.getNationality());
        data.put("Uid", kid.getUID());
        data.put("DOA",kid.getDOA());
        CollectionReference collectionReference = db.collection("Kids");
        DocumentReference docRef = collectionReference.document(kid.getFirstName()+kid.getUID());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        collectionReference
                                .document(kid.getFirstName()+kid.getUID())
                                .update(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        CollectionReference parentReference = db.collection("Kids/" + kid.getFirstName()+kid.getUID()+"/Parents");
                                        if (kid.getFather() != null){
                                            Parent father = kid.getFather();
                                            HashMap<String,String> fatherData = new HashMap<>();
                                            fatherData.put("First Name", father.getFirstName());
                                            fatherData.put("Last Name", father.getLastName());
                                            fatherData.put("DOB", father.getDOB());
                                            fatherData.put("Occupation", father.getOccupation());
                                            fatherData.put("Address",father.getHomeAddress());
                                            fatherData.put("Phone Number", father.getPhoneNumber());
                                            parentReference
                                                    .document("Father")
                                                    .set(fatherData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Parent","Father added");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Parent","Father failed");
                                                        }
                                                    });
                                        }
                                        else{
                                            HashMap<String,Object> fatherData = new HashMap<>();
                                            fatherData.put("First Name", null);
                                            fatherData.put("Last Name", null);
                                            fatherData.put("DOB", null);
                                            fatherData.put("Occupation", null);
                                            fatherData.put("Address",null);
                                            fatherData.put("Phone Number", null);
                                            parentReference
                                                    .document("Father")
                                                    .set(fatherData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Parent","Father added");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Parent","Father failed");
                                                        }
                                                    });
                                        }
                                        if (kid.getMother() != null){
                                            Parent mother = kid.getMother();
                                            HashMap<String,String> motherData = new HashMap<>();
                                            motherData.put("First Name", mother.getFirstName());
                                            motherData.put("Last Name", mother.getLastName());
                                            motherData.put("DOB", mother.getDOB());
                                            motherData.put("Occupation", mother.getOccupation());
                                            motherData.put("Address",mother.getHomeAddress());
                                            motherData.put("Phone Number", mother.getPhoneNumber());
                                            parentReference
                                                    .document("Mother")
                                                    .set(motherData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Parent","Mother added");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Parent","Mother failed");
                                                        }
                                                    });
                                        }
                                        else{
                                            HashMap<String,String> motherData = new HashMap<>();
                                            motherData.put("First Name", null);
                                            motherData.put("Last Name", null);
                                            motherData.put("DOB", null);
                                            motherData.put("Occupation", null);
                                            motherData.put("Address",null);
                                            motherData.put("Phone Number", null);
                                            parentReference
                                                    .document("Mother")
                                                    .set(motherData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Parent","Father added");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Parent","Father failed");
                                                        }
                                                    });
                                        }
                                        if (kid.getGuardian() != null){
                                            Parent guardian = kid.getGuardian();
                                            HashMap<String,String> guardianData = new HashMap<>();
                                            guardianData.put("First Name", guardian.getFirstName());
                                            guardianData.put("Last Name", guardian.getLastName());
                                            guardianData.put("DOB", guardian.getDOB());
                                            guardianData.put("Occupation", guardian.getOccupation());
                                            guardianData.put("Address",guardian.getHomeAddress());
                                            guardianData.put("Phone Number", guardian.getPhoneNumber());
                                            parentReference
                                                    .document("Guardian")
                                                    .set(guardianData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Parent","Guardian added");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Parent","Guardian failed");
                                                        }
                                                    });
                                        }
                                        else{
                                            HashMap<String,String> guardianData = new HashMap<>();
                                            guardianData.put("First Name", null);
                                            guardianData.put("Last Name", null);
                                            guardianData.put("DOB", null);
                                            guardianData.put("Occupation", null);
                                            guardianData.put("Address",null);
                                            guardianData.put("Phone Number", null);
                                            parentReference
                                                    .document("Guardian")
                                                    .set(guardianData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Parent","Father added");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Parent","Father failed");
                                                        }
                                                    });
                                        }
                                        CollectionReference referralReference = db.collection("Kids/" + kid.getFirstName()+kid.getUID()+"/Referrals");
                                        if (!kid.getReferrals().isEmpty()){
                                            ArrayList<Parent> referrals = kid.getReferrals();
                                            for (Parent referral : referrals){
                                                HashMap<String,String> referralData = new HashMap<>();
                                                referralData.put("First Name", referral.getFirstName());
                                                referralData.put("Last Name", referral.getLastName());
                                                referralData.put("DOB", referral.getDOB());
                                                referralData.put("Occupation", referral.getOccupation());
                                                referralData.put("Address", referral.getHomeAddress());
                                                referralData.put("Phone Number", referral.getPhoneNumber());
                                                referralData.put("Extra Information",referral.getExtraInformation());
                                                referralReference
                                                        .document(referral.getFirstName())
                                                        .set(referralData)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("Referral","Referral added");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d("Referral","Referral failed");
                                                            }
                                                        });
                                            }
                                        }
                                        kidAdapter.notifyDataSetChanged();
                                        Log.d(TAG, "Data has been updated successfully!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Data could not be updated!" + e.toString());
                                    }
                                });
                    } else {
                        collectionReference
                                .document(oldKidName)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        kidAdapter.notifyDataSetChanged();
                                        Log.d(TAG, "user kid data has been deleted");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Failed to delete the user kid data");
                                    }
                                });
                        collectionReference
                                .document(kid.getFirstName()+kid.getLastName()+kid.getUID())
                                .set(data)
                                //Debugging methods
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        CollectionReference fatherReference = db.collection("Kids/" + kid.getFirstName()+kid.getUID()+"/Parents");
                                        CollectionReference motherReference = db.collection("Kids/" + kid.getFirstName()+kid.getUID()+"/Parents");
                                        if (kid.getFather() != null){
                                            Parent father = kid.getFather();
                                            HashMap<String,String> fatherData = new HashMap<>();
                                            fatherData.put("First Name", father.getFirstName());
                                            fatherData.put("Last Name", father.getLastName());
                                            fatherData.put("DOB", father.getDOB());
                                            fatherData.put("Occupation", father.getOccupation());
                                            fatherData.put("Address",father.getHomeAddress());
                                            fatherData.put("Phone Number", father.getPhoneNumber());
                                            fatherReference
                                                    .document("Father")
                                                    .set(fatherData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Parent","Father added");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Parent","Father failed");
                                                        }
                                                    });
                                        }
                                        if (kid.getMother() != null){
                                            Parent mother = kid.getMother();
                                            HashMap<String,String> motherData = new HashMap<>();
                                            motherData.put("First Name", mother.getFirstName());
                                            motherData.put("Last Name", mother.getLastName());
                                            motherData.put("DOB", mother.getDOB());
                                            motherData.put("Occupation", mother.getOccupation());
                                            motherData.put("Address",mother.getHomeAddress());
                                            motherData.put("Phone Number", mother.getPhoneNumber());
                                            motherReference
                                                    .document("Mother")
                                                    .set(motherData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Parent","Father added");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Parent","Father failed");
                                                        }
                                                    });
                                        }
                                        if (kid.getGuardian() != null){
                                            Parent guardian = kid.getGuardian();
                                            HashMap<String,String> guardianData = new HashMap<>();
                                            guardianData.put("First Name", guardian.getFirstName());
                                            guardianData.put("Last Name", guardian.getLastName());
                                            guardianData.put("DOB", guardian.getDOB());
                                            guardianData.put("Occupation", guardian.getOccupation());
                                            guardianData.put("Address",guardian.getHomeAddress());
                                            guardianData.put("Phone Number", guardian.getPhoneNumber());
                                            motherReference
                                                    .document("Guardian")
                                                    .set(guardianData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("Parent","Guardian added");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Parent","Guardian failed");
                                                        }
                                                    });
                                        }
                                        CollectionReference referralReference = db.collection("Kids/" + kid.getFirstName()+kid.getUID()+"/Referrals");
                                        if (!kid.getReferrals().isEmpty()){
                                            ArrayList<Parent> referrals = kid.getReferrals();
                                            for (Parent referral : referrals){
                                                HashMap<String,String> referralData = new HashMap<>();
                                                referralData.put("First Name", referral.getFirstName());
                                                referralData.put("Last Name", referral.getLastName());
                                                referralData.put("DOB", referral.getDOB());
                                                referralData.put("Occupation", referral.getOccupation());
                                                referralData.put("Address", referral.getHomeAddress());
                                                referralData.put("Phone Number", referral.getPhoneNumber());
                                                referralData.put("Extra Information",referral.getExtraInformation());
                                                referralReference
                                                        .document(referral.getFirstName())
                                                        .set(referralData)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("Referral","Referral added");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d("Referral","Referral failed");
                                                            }
                                                        });
                                            }
                                        }
                                        kidAdapter.notifyDataSetChanged();
                                        // These are a method which gets executed when the task is succeeded
                                        Log.d(TAG, "Data has been added successfully!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        kidAdapter.notifyDataSetChanged();
                                        // These are a method which gets executed if there’s any problem
                                        Log.d(TAG, "Data could not be added!" + e.toString());
                                    }
                                });
                    }
                }
            }
        });

    }

    /**
     * When a user wants to delete the selected kid. This function will delete it from the database as well.
     *
     * @param kid
     */

    @Override
    public void onDeletePressed(Kid kid) {
        CollectionReference collectionReference = db.collection("Kids");
        collectionReference
                .document(kid.getFirstName()+kid.getUID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        kidDataList.remove(kid);
                        kidAdapter.notifyDataSetChanged();
                        Log.d(TAG, "user kid data has been deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        kidAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Failed to delete the user kid data");
                    }
                });
        //kidDataList.remove(kid);
        //kidAdapter.notifyDataSetChanged();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAddPressed(){

    }

    @Override
    public void onDeletePressed(Parent Referral){

    }
    @Override
    public void onOkPressed() {
        //Do nothing
    }

    @Override
    public void onBackPressed(){
        //nothing yet
    }
}


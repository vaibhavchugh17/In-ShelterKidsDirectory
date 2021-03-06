package com.example.in_shelterkidsdirectory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import de.hdodenhof.circleimageview.CircleImageView;

//When the user clicks Kids button from HomePage, this activity gets invoked
//To display a list of all the kids. WORKING EDIT TILL HERE.
public class Kids extends AppCompatActivity implements AddKidFragment.OnFragmentInteractionListener, kidImageFragment.OnFragmentInteractionListener,CommonFragment.OnFragmentInteractionListener,SelectionFragment.OnFragmentInteractionListener,NavigationView.OnNavigationItemSelectedListener {
    public static Context contextOfApplication;
    ListView kidList;
    ArrayAdapter<Kid> kidAdapter; //A custom adapter
    ArrayList<Kid> kidDataList;   //List of all the kids user owns
    ArrayAdapter<Kid> filteredKidAdapter; //A custom adapter
    ArrayList<Kid> filteredDataList;
    FirebaseFirestore db;
    StorageReference storageReference;
    FirebaseStorage storage;
    CollectionReference userKidCollectionReference;    //This is the sub-collection reference for the user who's logged in pointing to the collection of owned kids
    CollectionReference arrayReference;
    String TAG = "MyKids";
    CheckBox checkResidential;
    Integer check1 = 0;
    Integer check2 = 0;
    CheckBox checkOut;
    String residentialConstraint = "Residential";
    String outConstraint = "Out-Reach";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    private User currentUser;
    TextView userDisplay;

    public static Context getContextOfApplication() {
        return contextOfApplication;
    }

    /**
     * onCreate Called when Kids activity is launched.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_kids);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        kidList = findViewById(R.id.kid_list);
        contextOfApplication = getApplicationContext();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("List of Kids");
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        currentUser = (User) getIntent().getSerializableExtra(MainActivity.EXTRA_MESSAGE1);  //Catching the object of current user who's logged in

        kidDataList = new ArrayList<>();
        kidAdapter = new customKidAdapter(this, kidDataList);   //Implementing a custom adapter that connects the ListView with the ArrayList using kidcontent.xml layout
        kidList.setAdapter(kidAdapter);

        filteredDataList = new ArrayList<>();
        filteredKidAdapter = new customKidAdapter(this, filteredDataList);

        View headerView = navigationView.getHeaderView(0);
        userDisplay= headerView.findViewById(R.id.userDisplayName);
        String name  = currentUser.getFirst_name();

        if(name == null){
            userDisplay.setText("Welcome");

        }
        else{
            userDisplay.setText("Welcome, " + name);

        }

        final FloatingActionButton addKidButton = findViewById(R.id.add_kid_button);  //Invoking a fragment to add the kids when the FAB is clicked
        addKidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrayReference = db.collection("GlobalArray");
                DocumentReference docRef = arrayReference.document("Array"); //If username does not exist then prompt for a sign-up
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> temp = document.getData();
                                ArrayList<String> name = (ArrayList<String>) temp.get("Array");
                                String uid = Integer.toString(name.size() + 1);
                                AddKidFragment fragment = AddKidFragment.newInstance(uid);
                                fragment.show(getSupportFragmentManager(), "ADD_BOOK");
                            }
                        }
                    }
                    });
            }
        });

        db = FirebaseFirestore.getInstance();
        userKidCollectionReference = db.collection("Kids");//Creating/pointing to a sub-collection of the kids that user owns
        userKidCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                kidDataList.clear();
                kidAdapter.notifyDataSetChanged();
                for (QueryDocumentSnapshot newKid : value) {
                    String kid_firstName = (String)newKid.getData().get("First Name");    //Title of the kid will be the ID of the document representing the kid inside the sub-collections of MyKids
                    String kid_lastName = (String)newKid.getData().get("Last Name");
                    String kid_dob = (String)newKid.getData().get("DOB");
                    String kidStatus = (String)newKid.getData().get("Status");
                    String kid_height = (String)newKid.getData().get("Height");
                    String kid_middleName = (String)newKid.getData().get("Middle Name");
                    String kid_hair = (String)newKid.getData().get("Hair Color");
                    String kid_eye = (String)newKid.getData().get("Eye Color");
                    String kid_allergies = (String)newKid.getData().get("Allergies");
                    String kid_birthmarks = (String)newKid.getData().get("Birthmarks");
                    String kid_nationality = (String)newKid.getData().get("Nationality");
                    String kid_uid = (String) newKid.getData().get("Uid");
                    Kid temp = new Kid(kid_firstName,kid_lastName,kid_middleName,kid_eye,kid_dob,kid_hair,kidStatus,kid_height,kid_nationality,kid_allergies,kid_birthmarks);
                    temp.setUID(kid_uid);
                    DocumentReference fatherReference = db.collection("Kids/" + kid_firstName+kid_lastName+kid_uid +"/Parents").document("Father");
                    DocumentReference motherReference = db.collection("Kids/" + kid_firstName+kid_lastName+kid_uid +"/Parents").document("Mother");
                    DocumentReference guardianReference = db.collection("Kids/" + kid_firstName+kid_lastName+kid_uid +"/Parents").document("Guardian");
                    fatherReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Map<String, Object> data = document.getData();
                                    String father_firstName = (String)data.get("First Name");    //Title of the kid will be the ID of the document representing the kid inside the sub-collections of MyKids
                                    String father_lastName = (String)data.get("Last Name");
                                    String father_dob = (String)data.get("DOB");
                                    String father_number = (String)data.get("Phone Number");
                                    String father_occupation = (String)data.get("Occupation");
                                    String father_address = (String)data.get("Address");
                                    Parent father = new Parent(father_firstName,father_lastName,father_dob,father_address,father_occupation,father_number);
                                    temp.setFather(father);
                                } else {
                                    Log.d("Father","Father does not exist");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                    motherReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Map<String, Object> data = document.getData();
                                    String mother_firstName = (String)data.get("First Name");    //Title of the kid will be the ID of the document representing the kid inside the sub-collections of MyKids
                                    String mother_lastName = (String)data.get("Last Name");
                                    String mother_dob = (String)data.get("DOB");
                                    String mother_number = (String)data.get("Phone Number");
                                    String mother_occupation = (String)data.get("Occupation");
                                    String mother_address = (String)data.get("Address");
                                    Parent mother = new Parent(mother_firstName,mother_lastName,mother_dob,mother_address,mother_occupation,mother_number);
                                    temp.setMother(mother);
                                } else {
                                    Log.d("Mother","Mother does not exist");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                    guardianReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Map<String, Object> data = document.getData();
                                    String guardian_firstName = (String)data.get("First Name");    //Title of the kid will be the ID of the document representing the kid inside the sub-collections of MyKids
                                    String guardian_lastName = (String)data.get("Last Name");
                                    String guardian_dob = (String)data.get("DOB");
                                    String guardian_number = (String)data.get("Phone Number");
                                    String guardian_occupation = (String)data.get("Occupation");
                                    String guardian_address = (String)data.get("Address");
                                    Parent guardian = new Parent(guardian_firstName,guardian_lastName,guardian_dob,guardian_address,guardian_occupation,guardian_number);
                                    temp.setGuardian(guardian);
                                } else {
                                    Log.d("Guardian","Mother does not exist");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                    CollectionReference referralsCollection = db.collection("Kids/" + kid_firstName+kid_lastName+kid_uid + "/Referrals");
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
                                Parent tempReferral = new Parent(referral_firstName,referral_lastName,referral_dob,referral_home_address,referral_occupation,referral_phoneNumber);
                                temp.addReferrals(tempReferral); // Adding the cities and provinces from FireStore
                            }
                        }
                    });
                    kidDataList.add(temp); // Adding the cities and provinces from FireStore
                    kidAdapter.notifyDataSetChanged();
                }
            }
        });
        checkResidential = findViewById(R.id.checkResidential);
        checkOut = findViewById(R.id.checkOut);
        checkResidential.setVisibility(View.GONE);
        checkOut.setVisibility(View.GONE);
        //Code Added to update results depending on whether user wants to see only available or all kids
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

    /**
     * When user wants to add a new kid to their list of kids. No return value.
     *
     *
     */

    @Override
    public void onOkPressed(final Kid newKid) { //Whenever the user adds a kid, this method is called where the added kid is sent as a parameter from the fragment

        final HashMap<String, String> data = new HashMap<>();
        final String kid_firstName = newKid.getFirstName();    //Title of the kid will be the ID of the document representing the kid inside the sub-collections of MyKids
        final String kid_lastName = newKid.getLastName();
        String kid_dob = newKid.getDOB();
        String kidStatus = newKid.getStatus();
        String kid_height = newKid.getHeight();
        String kid_middleName = newKid.getMiddleName();
        String kid_hair = newKid.getHairColor();
        String kid_eye = newKid.getEyeColor();
        String kid_allergies = newKid.getAllergies();
        String kid_birthmarks = newKid.getBirthmarks();
        String kid_nationality = newKid.getNationality();

        if (kid_firstName.length() > 0 && kid_lastName.length() > 0 && kidStatus.length() > 0) {//Data inside the document will consist of the following
            //Adding data inside the hash map
            data.put("First Name", kid_firstName);
            data.put("Last Name", kid_lastName);
            data.put("Status", kidStatus);
            data.put("Middle Name", kid_middleName);
            data.put("DOB", kid_dob);
            data.put("Height", kid_height);
            data.put("Hair Color", kid_hair);
            data.put("Eye Color", kid_eye);
            data.put("Allergies", kid_allergies);
            data.put("Birthmarks", kid_birthmarks);
            data.put("Nationality", kid_nationality);
        }
        CollectionReference collectionReference = db.collection("Kids");
        arrayReference = db.collection("GlobalArray");
        DocumentReference docRef = arrayReference.document("Array"); //If username does not exist then prompt for a sign-up
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> temp = document.getData();
                        ArrayList<String> name = (ArrayList<String>) temp.get("Array");
                        data.put("Uid", Integer.toString(name.size() + 1));
                        name.add(Integer.toString(name.size() + 1));
                        HashMap<String, Object> array = new HashMap<>();
                        array.put("Array", name);
                        arrayReference
                                .document("Array")
                                .update(array)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Array Size successfully updated");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Failed to update Array Size");
                                    }
                                });
                        collectionReference
                                .document(kid_firstName+kid_lastName+data.get("Uid"))
                                .set(data)
                                //Debugging methods
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        CollectionReference parentReference = db.collection("Kids/" + kid_firstName+kid_lastName+data.get("Uid")+"/Parents");
                                        if (newKid.getFather() != null){
                                            Parent father = newKid.getFather();
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
                                            Log.d("Reached Null Father","Reached Null Father");
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
                                        if (newKid.getMother() != null){
                                            Parent mother = newKid.getMother();
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
                                        if (newKid.getGuardian() != null){
                                            Parent guardian = newKid.getGuardian();
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
                                        CollectionReference referralReference = db.collection("Kids/" + kid_firstName+kid_lastName+data.get("Uid")+"/Referrals");
                                        if (!newKid.getReferrals().isEmpty()){
                                            ArrayList<Parent> referrals = newKid.getReferrals();
                                            for (Parent referral : referrals){
                                                HashMap<String,String> referralData = new HashMap<>();
                                                referralData.put("First Name", referral.getFirstName());
                                                referralData.put("Last Name", referral.getLastName());
                                                referralData.put("DOB", referral.getDOB());
                                                referralData.put("Occupation", referral.getOccupation());
                                                referralData.put("Address", referral.getHomeAddress());
                                                referralData.put("Phone Number", referral.getPhoneNumber());
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
                                        /*finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);*/

                                        // These are a method which gets executed when the task is succeeded
                                        Log.d(TAG, "Data has been added successfully!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // These are a method which gets executed if there’s any problem
                                        Log.d(TAG, "Data could not be added!" + e.toString());
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

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
        CollectionReference collectionReference = db.collection("Kids");
        DocumentReference docRef = collectionReference.document(kid.getFirstName()+kid.getLastName()+kid.getUID());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        collectionReference
                                .document(kid.getFirstName()+kid.getLastName()+kid.getUID())
                                .update(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        CollectionReference parentReference = db.collection("Kids/" + kid.getFirstName()+kid.getLastName()+kid.getUID()+"/Parents");
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
                                        CollectionReference referralReference = db.collection("Kids/" + kid.getFirstName()+kid.getLastName()+kid.getUID()+"/Referrals");
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
                                        //kidDataList.remove(kid);
                                        //kidDataList.add(kid);
                                        kidAdapter.notifyDataSetChanged();
                                        /*finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
                                        Log.d(TAG, "Data has been updated successfully!");*/
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
                                        CollectionReference fatherReference = db.collection("Kids/" + kid.getFirstName()+kid.getLastName()+kid.getUID()+"/Parents");
                                        CollectionReference motherReference = db.collection("Kids/" + kid.getFirstName()+kid.getLastName()+kid.getUID()+"/Parents");
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
                                        CollectionReference referralReference = db.collection("Kids/" + kid.getFirstName()+kid.getLastName()+kid.getUID()+"/Referrals");
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
                                        //kidDataList.remove(kid);
                                        //kidDataList.add(kid);
                                        kidAdapter.notifyDataSetChanged();
                                        /*finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);*/
                                        // These are a method which gets executed when the task is succeeded
                                        Log.d(TAG, "Data has been added successfully!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
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
                .document(kid.getFirstName()+kid.getLastName()+kid.getUID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //kidDataList.remove(kid);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main,menu);
        StorageReference imagesRef = storageReference.child("images/" + currentUser.getUsername());
        MenuItem menuItem = menu.findItem(R.id.itemProfile);
        View view = MenuItemCompat.getActionView(menuItem);
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
                HashMap<String,Object> extras = new HashMap<>();
                extras.put("User",currentUser);
                extras.put("Kids",kidDataList);
                intent.putExtra("Extras", extras);
                startActivity(intent);
                break;
            case R.id.itemProfile:
                Toast.makeText(this,"profileClicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.itemLogOut:
                Intent nIntent = new Intent(Kids.this, MainActivity.class);
                Toast toast = Toast.makeText(Kids.this, "Signed Out!", Toast.LENGTH_SHORT);
                toast.show();
                startActivity(nIntent);
                break;
            case R.id.filter1:
                filteredDataList.clear();
                if(item.isChecked())
                {
                    if (check2 == 0)
                        kidList.setAdapter(kidAdapter);
                    else {
                        for (int i = 0; i < kidDataList.size(); i++) {
                            Kid kid = kidDataList.get(i);
                            filteredDataList.add(kid);
                            if (!(kid.getStatus().equals(outConstraint))){
                                filteredDataList.remove(kid);
                            }
                        }
                        filteredKidAdapter.notifyDataSetChanged();
                        kidList.setAdapter(filteredKidAdapter);
                    }
                    check1 = 0;
                    item.setChecked(false);
                }else{
                    for (int i = 0; i < kidDataList.size(); i++) {
                        Kid kid = kidDataList.get(i);
                        if (check2 == 1) {
                            if (kid.getStatus().equals(residentialConstraint) || kid.getStatus().equals(outConstraint))
                                filteredDataList.add(kid);
                        } else {
                            if (kid.getStatus().equals(residentialConstraint))
                                filteredDataList.add(kid);
                        }
                    }
                    filteredKidAdapter.notifyDataSetChanged();
                    kidList.setAdapter(filteredKidAdapter);
                    check1 = 1;
                    item.setChecked(true);
                }
                break;
            case R.id.filter2:
                filteredDataList.clear();
                if(item.isChecked())
                {
                    if (check1 == 0)
                        kidList.setAdapter(kidAdapter);
                    else {
                        for (int i = 0; i < kidDataList.size(); i++) {
                            Kid kid = kidDataList.get(i);
                            filteredDataList.add(kid);
                            if (!(kid.getStatus().equals(residentialConstraint))){
                                filteredDataList.remove(kid);
                            }

                        }
                        filteredKidAdapter.notifyDataSetChanged();
                        kidList.setAdapter(filteredKidAdapter);

                    }
                    check2 = 0;
                    item.setChecked(false);
                }else{
                    for (int i = 0; i < kidDataList.size(); i++) {
                        Kid kid = kidDataList.get(i);
                        if (check1 == 1) {
                            if (kid.getStatus().equals(residentialConstraint) || kid.getStatus().equals(outConstraint))
                                filteredDataList.add(kid);
                        } else {
                            if (kid.getStatus().equals(outConstraint))
                                filteredDataList.add(kid);
                        }


                    }
                    filteredKidAdapter.notifyDataSetChanged();
                    kidList.setAdapter(filteredKidAdapter);
                    check2 = 1;
                    item.setChecked(true);
                }
                break;




        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOkPressed() {
        //Do nothing
    }
    @Override
    public void onAddPressed() {
        //Do nothing
    }

    @Override
    public void onBackPressed(){
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
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
                intent1.putExtra("User", currentUser);   //Sending the current user as a parameter to the allUserProfiles activity
                startActivity(intent1);
                break;

            case R.id.nav_codeList:
                Intent intent2 = new Intent(getApplicationContext(), list_of_codes.class);
                intent2.putExtra("User", currentUser);   //Sending the current user as a parameter to the allUserProfiles activity
                startActivity(intent2);
                break;

            case R.id.nav_logout:
                Intent nIntent = new Intent(Kids.this, MainActivity.class);
                Toast toast = Toast.makeText(Kids.this, "Signed Out!", Toast.LENGTH_SHORT);
                toast.show();
                startActivity(nIntent);
                break;

        }
        return true;
    }

    @Override
    public void onDeletePressed(Parent referral){
        //nothing yet
    }

}




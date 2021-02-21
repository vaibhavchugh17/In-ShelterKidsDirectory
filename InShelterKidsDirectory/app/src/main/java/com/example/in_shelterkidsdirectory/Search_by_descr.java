package com.example.in_shelterkidsdirectory;

import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;

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

public class Search_by_descr extends AppCompatActivity implements AddKidFragment.OnFragmentInteractionListener{
    ListView kidList;
    ArrayAdapter<Kid> kidAdapter;
    ArrayList<Kid> kidDataList;
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
    CollectionReference arrayReference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = (User) getIntent().getSerializableExtra("User");
        setContentView(R.layout.search_books);
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
        final FirebaseFirestore Db = FirebaseFirestore.getInstance();
        final CollectionReference cr = Db.collection("Kids");

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cr.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        kidDataList.clear();
                        kidAdapter.notifyDataSetChanged();
                        final String descrInput = description.getText().toString();
                        for (QueryDocumentSnapshot newKid : value) {
                            String kid_firstName = (String) newKid.getData().get("First Name");
                            String kid_lastName = (String) newKid.getData().get("Last Name");
                            String kid_middleName = (String) newKid.getData().get("Middle Name");
                            if (kid_firstName != null) {
                                if (kid_firstName.toUpperCase().contains(descrInput) || kid_lastName.toUpperCase().contains(descrInput) || kid_middleName.toUpperCase().contains(descrInput) || kid_firstName.toLowerCase().contains(descrInput) || kid_lastName.toLowerCase().contains(descrInput) || kid_middleName.toLowerCase().contains(descrInput)) {
                                    String kid_dob = (String)newKid.getData().get("DOB");
                                    String kidStatus = (String)newKid.getData().get("Status");
                                    String kid_height = (String)newKid.getData().get("Height");
                                    String kid_hair = (String)newKid.getData().get("Hair Color");
                                    String kid_eye = (String)newKid.getData().get("Eye Color");
                                    String kid_allergies = (String)newKid.getData().get("Allergies");
                                    String kid_birthmarks = (String)newKid.getData().get("Birthmarks");
                                    String kid_nationality = (String)newKid.getData().get("Nationality");
                                    String kid_uid = (String) newKid.getData().get("Uid");
                                    Kid temp = new Kid(kid_firstName,kid_lastName,kid_middleName,kid_eye,kid_dob,kid_hair,kidStatus,kid_height,kid_nationality,kid_allergies,kid_birthmarks);
                                    temp.setUID(kid_uid);
                                    kidDataList.add(temp);
                                    kidAdapter.notifyDataSetChanged();
                                        if (checkResidential.isChecked() && checkOut.isChecked()) {
                                            if (!(kidStatus.equals(residentialConstraint) || kidStatus.equals(outConstraint))) {
                                                kidDataList.remove(temp);
                                                kidAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        if (checkOut.isChecked() && !checkResidential.isChecked()) {
                                            if (!(kidStatus.equals(outConstraint))) {
                                                kidDataList.remove(temp);
                                                kidAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        if (!checkOut.isChecked() && checkResidential.isChecked()) {
                                            if (!(kidStatus.equals(residentialConstraint))) {
                                                kidDataList.remove(temp);
                                                kidAdapter.notifyDataSetChanged();
                                            }
                                        }

                                }
                            }

                        }
                    }
                });
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
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
                                        // These are a method which gets executed when the task is succeeded
                                        Log.d(TAG, "Data has been added successfully!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
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
        data.put("Concerns",kid.getConcerns());
        data.put("Referrals",kid.getReferrals());
        //data.put("Guardians",kid.get) Missing getter for guardians
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
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
                                        Log.d(TAG, "Data has been updated successfully!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
                                        Log.d(TAG, "Data could not be updated!" + e.toString());
                                    }
                                });
                    } else {
                        collectionReference
                                .document(oldKidName+kid.getLastName()+kid.getUID())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "user kid data has been deleted");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Failed to delete the user kid data");
                                    }
                                });
                        kidDataList.remove(kid);
                        collectionReference
                                .document(kid.getFirstName()+kid.getLastName()+kid.getUID())
                                .set(data)
                                //Debugging methods
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
                                        // These are a method which gets executed when the task is succeeded
                                        Log.d(TAG, "Data has been added successfully!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                        overridePendingTransition(0, 0);
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
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                        Log.d(TAG, "user kid data has been deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                        Log.d(TAG, "Failed to delete the user kid data");
                    }
                });
        //kidDataList.remove(kid);
        //kidAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOkPressed() {
        //Do nothing
    }
}


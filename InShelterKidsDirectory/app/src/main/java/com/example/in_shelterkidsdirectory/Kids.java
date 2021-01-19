package com.example.in_shelterkidsdirectory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//When the user clicks Kids button from HomePage, this activity gets invoked
//To display a list of all the kids. WORKING EDIT TILL HERE.
public class Kids extends AppCompatActivity implements AddKidFragment.OnFragmentInteractionListener {
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
    CheckBox checkAvail;
    CheckBox checkBorrowed;
    String availableConstraint = "available";
    String borrowedConstraint = "borrowed";
    private User currentUser;
    private Uri path;

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
        kidList = findViewById(R.id.kid_list);
        contextOfApplication = getApplicationContext();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        currentUser = (User) getIntent().getSerializableExtra(HomePage.EXTRA_MESSAGE2);  //Catching the object of current user who's logged in

        kidDataList = new ArrayList<>();
        kidAdapter = new customKidAdapter(this, kidDataList);   //Implementing a custom adapter that connects the ListView with the ArrayList using kidcontent.xml layout
        kidList.setAdapter(kidAdapter);

        filteredDataList = new ArrayList<>();
        filteredKidAdapter = new customKidAdapter(this, filteredDataList);


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
        userKidCollectionReference = db.collection("Users");//Creating/pointing to a sub-collection of the kids that user owns
        userKidCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                kidDataList.clear();
                kidAdapter.notifyDataSetChanged();
                for (QueryDocumentSnapshot d : value) {
                    final String username = d.getId();
                    CollectionReference eachUser = db.collection("Users/" + username + "/MyKids");
                    eachUser.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                            for (QueryDocumentSnapshot newKid : value2) {
                                String owner = (String)newKid.getData().get("Owner");
                                HashMap<String,String> map = (HashMap<String, String>)newKid.getData().get("Requests");
                                String borrower = "";
                                if (map != null){
                                    for (String key : map.keySet()){
                                        if (("Borrowed").equals(map.get(key))){
                                            borrower = key;
                                        }
                                    }
                                }
                                if(owner!=null) {
                                    String current = currentUser.getUsername();
                                    if (owner.equals(current) || borrower.equals(current)) {
                                        String kid_title = newKid.getId();
                                        String kid_author = (String) newKid.getData().get("Kid Author");
                                        String kid_ISBN = (String) newKid.getData().get("Kid ISBN");
                                        String kid_status;
                                        if (borrower.equals(current)) {
                                            kid_status = "Borrowed";
                                        } else {
                                            kid_status = (String) newKid.getData().get("Kid Status");
                                        }
                                        String kid_description = (String) newKid.getData().get("Kid Description");
                                        String kid_owner = (String) newKid.getData().get("Owner");
                                        String kid_uid = (String) newKid.getData().get("Uid");
                                        Kid temp = new Kid("test","test","test","test","test","test","test","test","test","test","test");
                                        temp.setUID(kid_uid);
                                        kidDataList.add(temp); // Adding the cities and provinces from FireStore
                                        kidAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

        checkAvail = findViewById(R.id.checkAvailable);
        checkBorrowed = findViewById(R.id.checkBorrowed);
        //Code Added to update results depending on whether user wants to see only available or all kids
        checkAvail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filteredDataList.clear();
                if (isChecked) {
                    for (int i = 0; i < kidDataList.size(); i++) {
                        Kid kid = kidDataList.get(i);
                        if (checkBorrowed.isChecked()) {
                            if (kid.getStatus().toLowerCase().equals(availableConstraint) || kid.getStatus().toLowerCase().equals(borrowedConstraint))
                                filteredDataList.add(kid);
                        } else {
                            if (kid.getStatus().toLowerCase().equals(availableConstraint))
                                filteredDataList.add(kid);
                        }
                    }
                    filteredKidAdapter.notifyDataSetChanged();
                    kidList.setAdapter(filteredKidAdapter);
                } else {
                    if (!checkBorrowed.isChecked())
                        kidList.setAdapter(kidAdapter);
                    else {
                        for (int i = 0; i < kidDataList.size(); i++) {
                            Kid kid = kidDataList.get(i);
                            filteredDataList.add(kid);
                            if (!(kid.getStatus().toLowerCase().equals(borrowedConstraint))) {
                                filteredDataList.remove(kid);
                            }

                        }
                        filteredKidAdapter.notifyDataSetChanged();
                        kidList.setAdapter(filteredKidAdapter);
                    }
                }
            }
        });

        checkBorrowed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filteredDataList.clear();
                if (isChecked) {
                    for (int i = 0; i < kidDataList.size(); i++) {
                        Kid kid = kidDataList.get(i);
                        if (checkAvail.isChecked()) {
                            if (kid.getStatus().toLowerCase().equals(availableConstraint) || kid.getStatus().toLowerCase().equals(borrowedConstraint))
                                filteredDataList.add(kid);
                        } else {
                            if (kid.getStatus().toLowerCase().equals(borrowedConstraint))
                                filteredDataList.add(kid);
                        }


                    }
                    filteredKidAdapter.notifyDataSetChanged();
                    kidList.setAdapter(filteredKidAdapter);

                } else {
                    if (!checkAvail.isChecked())
                        kidList.setAdapter(kidAdapter);
                    else {
                        for (int i = 0; i < kidDataList.size(); i++) {
                            Kid kid = kidDataList.get(i);
                            filteredDataList.add(kid);
                            if (!(kid.getStatus().toLowerCase().equals(availableConstraint))) {
                                filteredDataList.remove(kid);
                            }

                        }
                        filteredKidAdapter.notifyDataSetChanged();
                        kidList.setAdapter(filteredKidAdapter);

                    }

                }
            }
        });

        kidList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Kid kid = kidDataList.get(i);
                Intent intent = new Intent(view.getContext(), ViewBookDetails.class);
                intent.putExtra("Kid", kid);
                startActivity(intent);
                return false;
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
    public void onOkPressed(final Kid kid) { //Whenever the user adds a kid, this method is called where the added kid is sent as a parameter from the fragment

        /*final HashMap<String, String> data = new HashMap<>();
        final String kidTitle = newKid.getTitle();    //Title of the kid will be the ID of the document representing the kid inside the sub-collections of MyKids
        final String kidAuthor = newKid.getAuthor();
        String kidISBN = newKid.getISBN();
        String kidStatus = newKid.getStatus();
        String kidDescription = newKid.getDescription();
        String kidOwner = currentUser.getUsername();
        if (kidTitle.length() > 0 && kidAuthor.length() > 0 && kidISBN.length() > 0 && kidStatus.length() > 0) {//Data inside the document will consist of the following
            //Adding data inside the hash map
            data.put("Kid Author", kidAuthor);
            data.put("Kid ISBN", kidISBN);
            data.put("Kid Status", kidStatus);
            data.put("Kid Description", kidDescription);
            data.put("Owner", kidOwner);
        }
        CollectionReference collectionReference = db.collection("Users/" + currentUser.getUsername()+"/MyKids");
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
                                .document(kidTitle)
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
        });*/

    }

    /**
     * This function is used for the fragment that gives the user the option to edit a kid or delete it.
     *
     *
     * @param oldKidName
     */
    @Override
    public void onOkPressed(final Kid kid, final String oldKidName) {
       /* final HashMap<String, Object> data = new HashMap<>();
        data.put("Kid Author", newKid.getAuthor());
        data.put("Kid ISBN", newKid.getISBN());
        data.put("Kid Status", newKid.getStatus());
        data.put("Kid Description", newKid.getDescription());
        data.put("Owner", newKid.getOwner());
        data.put("Uid", newKid.getUid());
        CollectionReference collectionReference = db.collection("Users/"+currentUser.getUsername()+"/MyKids");
        DocumentReference docRef = collectionReference.document(newKid.getTitle());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        collectionReference
                                .document(newKid.getTitle())
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
                                .document(oldKidName)
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
                        kidDataList.remove(newKid);
                        collectionReference
                                .document(newKid.getTitle())
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
        });*/

    }

    /**
     * When a user wants to delete the selected kid. This function will delete it from the database as well.
     *
     * @param kid
     */

    @Override
    public void onDeletePressed(Kid kid) {
        /*CollectionReference collectionReference = db.collection("Users/"+currentUser.getUsername()+"/MyKids");
        collectionReference
                .document(kid.getTitle())
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
        //kidAdapter.notifyDataSetChanged();*/
    }

    @Override
    public void onOkPressed() {
        //Do nothing
    }


}




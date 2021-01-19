package com.example.in_shelterkidsdirectory;

import android.content.Intent;
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
import android.widget.Toast;

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

public class Search_by_descr extends AppCompatActivity {
    ListView bookList;
    ArrayAdapter<Kid> bookAdapter;
    ArrayList<Kid> bookDataList;
    ArrayList<Kid> filteredDataList;
    ArrayAdapter<Kid> filteredKidAdapter;
    FirebaseFirestore db;
    EditText description;
    Button search;
    CollectionReference userKidCollectionReference;
    String TAG = "KidSearch";
    CheckBox checkAvail;
    CheckBox checkBorr;
    String availableConstraint = "available";
    String borrowedConstraint = "borrowed";
    private User currentUser;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = (User) getIntent().getSerializableExtra("User");
        setContentView(R.layout.search_books);
        bookList = findViewById(R.id.book_list);
        description = findViewById(R.id.description);
        search = findViewById(R.id.search);
        checkAvail = findViewById(R.id.check_available);
        checkBorr = findViewById(R.id.check_borrowed);

        bookDataList = new ArrayList<>();
        filteredDataList = new ArrayList<>();
        bookAdapter = new customKidAdapter(this, bookDataList);
        filteredKidAdapter = new customKidAdapter(this, filteredDataList);
        bookList.setAdapter(bookAdapter);

        db = FirebaseFirestore.getInstance();
        final FirebaseFirestore Db = FirebaseFirestore.getInstance();
        final CollectionReference cr = Db.collection("Users");

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cr.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        bookDataList.clear();
                        bookAdapter.notifyDataSetChanged();
                        final String descrInput = description.getText().toString();
                        for (QueryDocumentSnapshot d : value) {
                            final String username = d.getId();
                            CollectionReference eachUser = Db.collection("Users/" + username + "/MyKids");
                            eachUser.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                                    for (QueryDocumentSnapshot newKid : value2) {
                                        String book_description = (String) newKid.getData().get("Kid Description");
                                        if (!currentUser.getUsername().equals(newKid.getData().get("Owner"))){
                                            if (book_description != null) {
                                                if (book_description.contains(descrInput)) {
                                                    if (!("Borrowed").equals(newKid.getData().get("Kid Status")) && !("Accepted").equals(newKid.getData().get("Kid Status"))){
                                                        String book_title = newKid.getId();
                                                        String book_author = (String) newKid.getData().get("Kid Author");
                                                        String book_ISBN = (String) newKid.getData().get("Kid ISBN");
                                                        String book_status = (String) newKid.getData().get("Kid Status");
                                                        HashMap<String,String> req = (HashMap<String, String>) newKid.getData().get("Requests");
                                                        Kid thisKid = new Kid("test","test","test","test","test","test","test","test","test","test","test");
                                                        bookDataList.add(thisKid);
                                                        bookAdapter.notifyDataSetChanged();
                                                        if (checkAvail.isChecked() && checkBorr.isChecked()) {
                                                            if (!(book_status.toLowerCase().equals(availableConstraint) || book_status.toLowerCase().equals(borrowedConstraint))) {
                                                                bookDataList.remove(thisKid);
                                                                bookAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                        if (checkBorr.isChecked() && !checkAvail.isChecked()) {
                                                            if (!(book_status.toLowerCase().equals(borrowedConstraint))) {
                                                                bookDataList.remove(thisKid);
                                                                bookAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                        if (!checkBorr.isChecked() && checkAvail.isChecked()) {
                                                            if (!(book_status.toLowerCase().equals(availableConstraint))) {
                                                                bookDataList.remove(thisKid);
                                                                bookAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

    }
}


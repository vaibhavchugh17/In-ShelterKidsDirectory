package com.example.dlpbgj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ReturnBook extends AppCompatActivity {
    String bookISBN;
    User currentUser;
    TextView ISBN;
    Spinner spinner;
    ArrayList<String> owners;
    ArrayList<String> bookNames;
    ArrayList<HashMap<String,String>> maps;
    HashMap<String,String> finalMap;
    String owner;
    String book;
    Button scan;
    Button returnBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_book);
        scan = findViewById(R.id.scanisbn);
        returnBook = findViewById(R.id.returnBook);
        ISBN = findViewById(R.id.ISBN_book);
        spinner = findViewById(R.id.returnList);
        owners = new ArrayList<>();
        bookNames = new ArrayList<>();
        maps = new ArrayList<>();
        returnBook.setText("Return Book");
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), barcode_scanner.class);
                startActivityForResult(intent, 1);
            }
        });

        returnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (owners.size() == 0) {
                    if (bookISBN == null) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Please scan an ISBN code to return the book!", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        owner = null;
                        book = null;
                        bookISBN = null;
                        finalMap = null;
                        ISBN.setText("ISBN-");
                        Toast toast = Toast.makeText(getApplicationContext(), "Scanned ISBN code does not match any borrowed book!\nPlease scan again.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else if (owner == null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please select a user/owner to return the book to!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference collectionReference = db.collection("Users/" + owner + "/MyBooks");
                    HashMap<String, Object> map = new HashMap<>();
                    finalMap.put(currentUser.getUsername(),"Returned");
                    //map.put("Borrower", null);
                    map.put("Book Status", "Returned");
                    collectionReference
                            .document(book)
                            .update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("ReturnBook", "Book values successfully updated!");
                                    Toast toast = Toast.makeText(getApplicationContext(), "Book Successfully returned to " + owner, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("ReturnBook", "Book values failed to update!");
                                    Toast toast = Toast.makeText(getApplicationContext(), "There was an error returning book to " + owner, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
                    finish();
                    startActivity(getIntent());
                }
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                owner = owners.get(i);
                book = bookNames.get(i);
                finalMap = maps.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                owner = null;
                book = null;
                finalMap = null;
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
                bookISBN = data.getStringExtra("ISBN");
                ISBN.setText("ISBN - " + bookISBN);
                System.out.println("The Book ISBN is :" + bookISBN + "\n");
                checkFunc(bookISBN);
            }
        }
    }

    public void checkFunc(final String isbn) {
        owners.clear();
        bookNames.clear();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userCollection = db.collection("Users");
        currentUser = (User) getIntent().getSerializableExtra("User");
        userCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot d : value) {
                    final String username = d.getId();
                    final CollectionReference eachUser = db.collection("Users/" + username + "/MyBooks");
                    eachUser.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                            for (QueryDocumentSnapshot f : value2) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, owners);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
                                String borrower = (String)f.getData().get("Borrower");
                                System.out.println("The borrower is " + borrower);
                                String current = currentUser.getUsername();
                                System.out.println("The current is " + current);
                                System.out.println("The current isbn " + isbn);
                                if (isbn.equals(f.getData().get("Book ISBN"))) {
                                    if (current.equals(borrower)) {
                                        System.out.println("inside if");
                                        final String temp = (String) f.getData().get("Owner");
                                        HashMap<String,String> map = (HashMap<String, String>)f.getData().get("Requests");
                                        owners.add(temp);
                                        bookNames.add(f.getId());
                                        maps.add(map);
                                        setSpinner();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, owners);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

}
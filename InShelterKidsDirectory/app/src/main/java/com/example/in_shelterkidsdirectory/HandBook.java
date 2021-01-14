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

public class HandBook extends AppCompatActivity {
    String bookISBN;
    User currentUser;
    TextView ISBN;
    Spinner spinner;
    ArrayList<String> borrowers;
    ArrayList<String> bookNames;
    String borrower;
    String book;
    Button scan;
    Button returnBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_book);
        currentUser = (User) getIntent().getSerializableExtra("User");
        scan = findViewById(R.id.scanisbn);
        returnBook = findViewById(R.id.returnBook);
        ISBN = findViewById(R.id.ISBN_book);
        spinner = findViewById(R.id.returnList);
        borrowers = new ArrayList<>();
        bookNames = new ArrayList<>();
        returnBook.setText("Hand Over a Book");
        TextView acceptreturn = findViewById(R.id.acceptreturn);
        acceptreturn.setText("Book will be given to: ");
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

                if (borrowers.size() == 0) {
                    if (bookISBN == null) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Please scan an ISBN code to hand over the book!", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        borrower = null;
                        book = null;
                        bookISBN = null;
                        ISBN.setText("ISBN-");
                        Toast toast = Toast.makeText(getApplicationContext(), "Scanned ISBN code either does not match any book or you haven't accepted a request for this book.!\nPlease scan again.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else if (borrower == null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please select a user/borrower to hand over the book to!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference collectionReference = db.collection("Users/" + currentUser.getUsername() + "/MyBooks");
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Borrower", borrower);
                    map.put("Book Status", "Borrowed");
                    collectionReference
                            .document(book)
                            .update(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("ReturnBook", "Book values successfully updated!");
                                    Toast toast = Toast.makeText(getApplicationContext(), "Book " + book +" Successfully handed over to " + borrower, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("ReturnBook", "Book values failed to update!");
                                    Toast toast = Toast.makeText(getApplicationContext(), "There was an error handing over the book to " + borrower, Toast.LENGTH_SHORT);
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
                borrower = borrowers.get(i);
                book = bookNames.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                borrower = null;
                book = null;
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
        borrowers.clear();
        bookNames.clear();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        currentUser = (User) getIntent().getSerializableExtra("User");
        CollectionReference userCollection = db.collection("Users/" + currentUser.getUsername() + "/MyBooks");
        userCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
                    FirebaseFirestoreException error) { //Manages the state of the sub-collection
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, borrowers);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    if (isbn.equals(doc.getData().get("Book ISBN"))){
                        if (("Accepted").equals(doc.getData().get("Book Status"))){
                            HashMap<String,String> temp = (HashMap<String, String>)doc.getData().get("Requests");
                            System.out.println(temp.keySet());
                            for (String key : temp.keySet()){
                                if (("Accepted").equals(temp.get(key))){
                                    System.out.println(key);
                                    borrowers.add(key);
                                    bookNames.add(doc.getId());
                                    setSpinner();
                                }
                            }
                        }
                    }
                }
            }

        });

    }

    public void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, borrowers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

}
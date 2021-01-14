package com.example.dlpbgj;

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

public class Search_by_descr extends AppCompatActivity implements RequestBookFragment.OnFragmentInteractionListener {
    ListView bookList;
    ArrayAdapter<Book> bookAdapter;
    ArrayList<Book> bookDataList;
    ArrayList<Book> filteredDataList;
    ArrayAdapter<Book> filteredBookAdapter;
    FirebaseFirestore db;
    EditText description;
    Button search;
    CollectionReference userBookCollectionReference;
    String TAG = "BookSearch";
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
        bookAdapter = new customBookAdapter(this, bookDataList);
        filteredBookAdapter = new customBookAdapter(this, filteredDataList);
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
                            CollectionReference eachUser = Db.collection("Users/" + username + "/MyBooks");
                            eachUser.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                                    for (QueryDocumentSnapshot newBook : value2) {
                                        String book_description = (String) newBook.getData().get("Book Description");
                                        if (!currentUser.getUsername().equals(newBook.getData().get("Owner"))){
                                            if (book_description != null) {
                                                if (book_description.contains(descrInput)) {
                                                    if (!("Borrowed").equals(newBook.getData().get("Book Status")) && !("Accepted").equals(newBook.getData().get("Book Status"))){
                                                        String book_title = newBook.getId();
                                                        String book_author = (String) newBook.getData().get("Book Author");
                                                        String book_ISBN = (String) newBook.getData().get("Book ISBN");
                                                        String book_status = (String) newBook.getData().get("Book Status");
                                                        HashMap<String,String> req = (HashMap<String, String>) newBook.getData().get("Requests");
                                                        Book thisBook = new Book(book_title, book_author, book_ISBN, book_status, book_description, username, req);
                                                        bookDataList.add(thisBook);
                                                        bookAdapter.notifyDataSetChanged();
                                                        if (checkAvail.isChecked() && checkBorr.isChecked()) {
                                                            if (!(book_status.toLowerCase().equals(availableConstraint) || book_status.toLowerCase().equals(borrowedConstraint))) {
                                                                bookDataList.remove(thisBook);
                                                                bookAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                        if (checkBorr.isChecked() && !checkAvail.isChecked()) {
                                                            if (!(book_status.toLowerCase().equals(borrowedConstraint))) {
                                                                bookDataList.remove(thisBook);
                                                                bookAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                        if (!checkBorr.isChecked() && checkAvail.isChecked()) {
                                                            if (!(book_status.toLowerCase().equals(availableConstraint))) {
                                                                bookDataList.remove(thisBook);
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

        checkAvail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filteredDataList.clear();
                if (isChecked) {
                    for (int i = 0; i < bookDataList.size(); i++) {
                        Book book = bookDataList.get(i);
                        if (checkBorr.isChecked()) {
                            if (book.getStatus().toLowerCase().equals(availableConstraint) || book.getStatus().toLowerCase().equals(borrowedConstraint))
                                filteredDataList.add(book);
                        } else {
                            if (book.getStatus().toLowerCase().equals(availableConstraint))
                                filteredDataList.add(book);
                        }
                    }
                    filteredBookAdapter.notifyDataSetChanged();
                    bookList.setAdapter(filteredBookAdapter);
                } else {
                    if (!checkBorr.isChecked())
                        bookList.setAdapter(bookAdapter);
                    else {
                        for (int i = 0; i < bookDataList.size(); i++) {
                            Book book = bookDataList.get(i);
                            filteredDataList.add(book);
                            if (!(book.getStatus().toLowerCase().equals(borrowedConstraint))) {
                                filteredDataList.remove(book);
                            }
                        }
                        filteredBookAdapter.notifyDataSetChanged();
                        bookList.setAdapter(filteredBookAdapter);
                    }
                }
            }
        });

        checkBorr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filteredDataList.clear();
                if (isChecked) {
                    for (int i = 0; i < bookDataList.size(); i++) {
                        Book book = bookDataList.get(i);
                        if (checkAvail.isChecked()) {
                            if (book.getStatus().toLowerCase().equals(availableConstraint) || book.getStatus().toLowerCase().equals(borrowedConstraint))
                                filteredDataList.add(book);
                        } else {
                            if (book.getStatus().toLowerCase().equals(borrowedConstraint))
                                filteredDataList.add(book);
                        }
                    }
                    filteredBookAdapter.notifyDataSetChanged();
                    bookList.setAdapter(filteredBookAdapter);
                } else {
                    if (!checkAvail.isChecked())
                        bookList.setAdapter(bookAdapter);
                    else {
                        for (int i = 0; i < bookDataList.size(); i++) {
                            Book book = bookDataList.get(i);
                            filteredDataList.add(book);
                            if (!(book.getStatus().toLowerCase().equals(availableConstraint))) {
                                filteredDataList.remove(book);
                            }
                        }
                        filteredBookAdapter.notifyDataSetChanged();
                        bookList.setAdapter(filteredBookAdapter);
                    }
                }
            }
        });


        if(checkAvail.isChecked() || checkBorr.isChecked()){
            bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Book temp = filteredDataList.get(i);
                    RequestBookFragment r = RequestBookFragment.newInstance(temp, currentUser);
                    r.show(getSupportFragmentManager(), "REQUEST_BOOK");
                }
            });
        }
        else {
            bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Book temp = bookDataList.get(i);
                    RequestBookFragment r = RequestBookFragment.newInstance(temp, currentUser);
                    r.show(getSupportFragmentManager(), "REQUEST_BOOK");
                }
            });
        }

    }

    @Override
    public void onOkPressed(final Book book, User user) {
        final HashMap<String, Object> data = new HashMap<>();
        HashMap<String,String> req = book.getRequests();
        /*if (book.getStatus().equals("Accepted")) {
            Toast toast = Toast.makeText(Search_by_descr.this, "CAN'T REQUEST A BORROWED BOOK!! ;)", Toast.LENGTH_SHORT);
            toast.show();
        } else if (currentUser.getUsername().equals(book.getOwner())) {
            Toast toast = Toast.makeText(Search_by_descr.this, "CAN'T REQUEST YOUR OWN BOOK!! :)", Toast.LENGTH_SHORT);
            toast.show();
        }*/ if (!req.containsKey(user.getUsername()) || !("Requested").equals(req.get(user.getUsername()))) {
            req.put(user.getUsername(),"Requested");
            book.addNotification(user.getUsername());
            data.put("Notifications",book.getNotifications());
            data.put("Requests", req);
            data.put("Book Status", "Requested");
            userBookCollectionReference = db.collection("Users/" + book.getOwner() + "/MyBooks");
            DocumentReference docRef = userBookCollectionReference.document(book.getTitle());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        userBookCollectionReference
                                .document(book.getTitle())
                                .update(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        search.performClick();
                                        Log.d(TAG, "Data has been updated successfully!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Data could not be updated!" + e.toString());
                                    }
                                });
                    }
                }
            });
        } else {
            Toast toast = Toast.makeText(Search_by_descr.this, "ALREADY REQUESTED THIS BOOK", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


}


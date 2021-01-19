package com.example.in_shelterkidsdirectory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
public class Kids extends AppCompatActivity implements AddBookFragment.OnFragmentInteractionListener {
    public static Context contextOfApplication;
    ListView kidList;
    ArrayAdapter<Book> kidAdapter; //A custom adapter
    ArrayList<Book> kidDataList;   //List of all the books user owns
    ArrayAdapter<Book> filteredKidAdapter; //A custom adapter
    ArrayList<Book> filteredDataList;
    FirebaseFirestore db;
    StorageReference storageReference;
    FirebaseStorage storage;
    CollectionReference userKidCollectionReference;    //This is the sub-collection reference for the user who's logged in pointing to the collection of owned books
    CollectionReference arrayReference;
    String TAG = "MyBooks";
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
        kidAdapter = new customKidAdapter(this, kidDataList);   //Implementing a custom adapter that connects the ListView with the ArrayList using bookcontent.xml layout
        kidList.setAdapter(kidAdapter);

        filteredDataList = new ArrayList<>();
        filteredKidAdapter = new customKidAdapter(this, filteredDataList);


        final FloatingActionButton addKidButton = findViewById(R.id.add_kid_button);  //Invoking a fragment to add the books when the FAB is clicked
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
                                AddBookFragment fragment = AddBookFragment.newInstance(uid);
                                fragment.show(getSupportFragmentManager(), "ADD_BOOK");
                            }
                        }
                    }
                    });
            }
        });

        db = FirebaseFirestore.getInstance();
        userKidCollectionReference = db.collection("Users");//Creating/pointing to a sub-collection of the books that user owns
        userKidCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                kidDataList.clear();
                kidAdapter.notifyDataSetChanged();
                for (QueryDocumentSnapshot d : value) {
                    final String username = d.getId();
                    CollectionReference eachUser = db.collection("Users/" + username + "/MyBooks");
                    eachUser.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                            for (QueryDocumentSnapshot newBook : value2) {
                                String owner = (String)newBook.getData().get("Owner");
                                HashMap<String,String> map = (HashMap<String, String>)newBook.getData().get("Requests");
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
                                        String book_title = newBook.getId();
                                        String book_author = (String) newBook.getData().get("Book Author");
                                        String book_ISBN = (String) newBook.getData().get("Book ISBN");
                                        String book_status;
                                        if (borrower.equals(current)) {
                                            book_status = "Borrowed";
                                        } else {
                                            book_status = (String) newBook.getData().get("Book Status");
                                        }
                                        String book_description = (String) newBook.getData().get("Book Description");
                                        String book_owner = (String) newBook.getData().get("Owner");
                                        String book_uid = (String) newBook.getData().get("Uid");
                                        Book temp = new Book(book_title, book_author, book_ISBN, book_status, book_description, book_owner);
                                        temp.setUid(book_uid);
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
        //Code Added to update results depending on whether user wants to see only available or all books
        checkAvail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filteredDataList.clear();
                if (isChecked) {
                    for (int i = 0; i < kidDataList.size(); i++) {
                        Book book = kidDataList.get(i);
                        if (checkBorrowed.isChecked()) {
                            if (book.getStatus().toLowerCase().equals(availableConstraint) || book.getStatus().toLowerCase().equals(borrowedConstraint))
                                filteredDataList.add(book);
                        } else {
                            if (book.getStatus().toLowerCase().equals(availableConstraint))
                                filteredDataList.add(book);
                        }
                    }
                    filteredKidAdapter.notifyDataSetChanged();
                    kidList.setAdapter(filteredKidAdapter);
                } else {
                    if (!checkBorrowed.isChecked())
                        kidList.setAdapter(kidAdapter);
                    else {
                        for (int i = 0; i < kidDataList.size(); i++) {
                            Book book = kidDataList.get(i);
                            filteredDataList.add(book);
                            if (!(book.getStatus().toLowerCase().equals(borrowedConstraint))) {
                                filteredDataList.remove(book);
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
                        Book book = kidDataList.get(i);
                        if (checkAvail.isChecked()) {
                            if (book.getStatus().toLowerCase().equals(availableConstraint) || book.getStatus().toLowerCase().equals(borrowedConstraint))
                                filteredDataList.add(book);
                        } else {
                            if (book.getStatus().toLowerCase().equals(borrowedConstraint))
                                filteredDataList.add(book);
                        }


                    }
                    filteredKidAdapter.notifyDataSetChanged();
                    kidList.setAdapter(filteredKidAdapter);

                } else {
                    if (!checkAvail.isChecked())
                        kidList.setAdapter(kidAdapter);
                    else {
                        for (int i = 0; i < kidDataList.size(); i++) {
                            Book book = kidDataList.get(i);
                            filteredDataList.add(book);
                            if (!(book.getStatus().toLowerCase().equals(availableConstraint))) {
                                filteredDataList.remove(book);
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
                Book book = kidDataList.get(i);
                Intent intent = new Intent(view.getContext(), ViewBookDetails.class);
                intent.putExtra("Book", book);
                startActivity(intent);
                return false;
            }
        });

        kidList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book temp = kidDataList.get(i);
                if (temp.getOwner().equals(currentUser.getUsername())){
                    AddBookFragment fragment = AddBookFragment.newInstance(temp, currentUser);
                    fragment.show(getSupportFragmentManager(), "ADD_BOOK");
                }
                else{
                    Toast toast = Toast.makeText(adapterView.getContext(), "Cannot Edit/Delete a Borrowed book :)", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

    /**
     * When user wants to add a new book to their list of books. No return value.
     *
     * @param newBook
     */

    @Override
    public void onOkPressed(final Book newBook) { //Whenever the user adds a book, this method is called where the added book is sent as a parameter from the fragment

        final HashMap<String, String> data = new HashMap<>();
        final String bookTitle = newBook.getTitle();    //Title of the book will be the ID of the document representing the book inside the sub-collections of MyBooks
        final String bookAuthor = newBook.getAuthor();
        String bookISBN = newBook.getISBN();
        String bookStatus = newBook.getStatus();
        String bookDescription = newBook.getDescription();
        String bookOwner = currentUser.getUsername();
        if (bookTitle.length() > 0 && bookAuthor.length() > 0 && bookISBN.length() > 0 && bookStatus.length() > 0) {//Data inside the document will consist of the following
            //Adding data inside the hash map
            data.put("Book Author", bookAuthor);
            data.put("Book ISBN", bookISBN);
            data.put("Book Status", bookStatus);
            data.put("Book Description", bookDescription);
            data.put("Owner", bookOwner);
        }
        CollectionReference collectionReference = db.collection("Users/" + currentUser.getUsername()+"/MyBooks");
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
                                .document(bookTitle)
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
     * This function is used for the fragment that gives the user the option to edit a book or delete it.
     *
     * @param newBook
     * @param oldBookName
     */
    @Override
    public void onOkPressed(final Book newBook, final String oldBookName) {
        final HashMap<String, Object> data = new HashMap<>();
        data.put("Book Author", newBook.getAuthor());
        data.put("Book ISBN", newBook.getISBN());
        data.put("Book Status", newBook.getStatus());
        data.put("Book Description", newBook.getDescription());
        data.put("Owner", newBook.getOwner());
        data.put("Uid", newBook.getUid());
        CollectionReference collectionReference = db.collection("Users/"+currentUser.getUsername()+"/MyBooks");
        DocumentReference docRef = collectionReference.document(newBook.getTitle());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        collectionReference
                                .document(newBook.getTitle())
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
                                .document(oldBookName)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "user book data has been deleted");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Failed to delete the user book data");
                                    }
                                });
                        kidDataList.remove(newBook);
                        collectionReference
                                .document(newBook.getTitle())
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
     * When a user wants to delete the selected book. This function will delete it from the database as well.
     *
     * @param book
     */

    @Override
    public void onDeletePressed(Book book) {
        CollectionReference collectionReference = db.collection("Users/"+currentUser.getUsername()+"/MyBooks");
        collectionReference
                .document(book.getTitle())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                        Log.d(TAG, "user book data has been deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                        Log.d(TAG, "Failed to delete the user book data");
                    }
                });
        //kidDataList.remove(book);
        //kidAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOkPressed() {
        //Do nothing
    }


}




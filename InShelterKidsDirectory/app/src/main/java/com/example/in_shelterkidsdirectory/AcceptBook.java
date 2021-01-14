package com.example.dlpbgj;

import android.annotation.SuppressLint;
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


public class AcceptBook extends AppCompatActivity {
    String bookISBN;
    User currentUser;
    TextView ISBN;
    Spinner spinner;
    ArrayList<String> borrowers;
    ArrayList<String> bookNames;
    ArrayList<HashMap<String,String>> maps;
    String borrower;
    String book;
    HashMap<String,String> finalMap;
    Button scan;
    Button returnBook;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_book);
        scan = findViewById(R.id.scanisbn);
        returnBook = findViewById(R.id.returnBook);
        ISBN = findViewById(R.id.ISBN_book);
        spinner = findViewById(R.id.returnList);
        borrowers = new ArrayList<>();
        bookNames = new ArrayList<>();
        maps = new ArrayList<>();
        returnBook.setText("Confirm Return");
        TextView acceptreturn = findViewById(R.id.acceptreturn);
        acceptreturn.setText("Book will be returned from :");
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * @param view (View)
             * This method takes in the given view, it initialize the new activity
             * To scan the barcode for the isbn purposes.
             * @return null
             * This method initialize another activity and it doesn't return anything.
             */
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), barcode_scanner.class);
                startActivityForResult(intent, 1);
            }
        });

        returnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            /** The given method is initialized when the button "return book"
             * is clicked
             * @param view (View) View according to the context of the environment
             * @return null The method returns nothing but does display important
             * messages for the user. While simultaneously making changes in the firebasedatabase
             * The function primarily serves the important function to display the message to the user and
             * scan the ISBN of the book entered and search through out the entire database.
             */
            public void onClick(View view) {

                if (borrowers.size() == 0) {
                    if (bookISBN == null) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Please scan an ISBN code to confirm book returned!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        borrower = null;
                        book = null;
                        bookISBN = null;
                        ISBN.setText("ISBN-");
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Scanned ISBN code does not match any book that is currently borrowed or returned by the borrower!\nPlease scan again.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } else if (borrower == null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please select a user to get the book from!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference collectionReference = db.collection("Users/" + currentUser.getUsername() + "/MyBooks");
                    HashMap<String, Object> data = new HashMap<>();
                    finalMap.put(borrower,null);
                    data.put("Borrower", null);
                    data.put("Book Status", "Available");
                    data.put("Requests",finalMap);
                    collectionReference
                            .document(book)
                            .update(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Get Book Back", "Book values successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Get Book Back", "Book values failed to update!");
                                }
                            });
                    Toast toast = Toast.makeText(getApplicationContext(), "Book successfully retrieved from" + borrower + " !", Toast.LENGTH_SHORT);
                    toast.show();
                    finish();
                    startActivity(getIntent());
                }

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            /** @param adapterView (AdapterView)
             * @param view (View)
             * @param i (int)
             * @param l (long)
             */
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                borrower = borrowers.get(i);
                book = bookNames.get(i);
                finalMap = maps.get(i);
            }

            @Override
            /**
             * @param adapterView (AdapterView)
             */
            public void onNothingSelected(AdapterView<?> adapterView) {
                borrower = null;
                book = null;
                finalMap = null;
            }
        });

    }

    @SuppressLint("SetTextI18n")
    @Override
    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
                bookISBN = data.getStringExtra("ISBN");
                ISBN.setText("ISBN - " + bookISBN);
                String temp = bookISBN;
                checkFunc(temp);
            }
        }
    }

    /**
     *
     * @param isbn
     */
    public void checkFunc(final String isbn) {
        borrowers.clear();
        bookNames.clear();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        currentUser = (User) getIntent().getSerializableExtra("User");
        final CollectionReference userCollection = db.collection("Users/" + currentUser.getUsername() + "/MyBooks");
        userCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            /**
             * @param QuerySnapshot value
             * @param FirebaseFirestoreException error
             */
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot newBook : value) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, borrowers);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    if ((isbn.equals((String)newBook.getData().get("Book ISBN")))) {
                        if (null != (String)newBook.getData().get("Borrower")) {
                            String temp = (String) newBook.getData().get("Borrower");
                            HashMap<String,String> map = (HashMap<String, String>)newBook.getData().get("Requests");
                            if (("Borrowed").equals(map.get(temp))){
                                borrowers.add(temp);
                                bookNames.add(newBook.getId());
                                maps.add(map);
                                setSpinner();
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     *
     */
    public void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, borrowers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}

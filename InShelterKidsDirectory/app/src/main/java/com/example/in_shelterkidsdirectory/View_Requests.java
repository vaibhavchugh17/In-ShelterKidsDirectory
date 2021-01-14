package com.example.dlpbgj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class View_Requests extends AppCompatActivity implements ViewRequestFragment.OnFragmentInteractionListener{
    ListView bookList;
    ArrayAdapter<Book> bookAdapter;
    ArrayList<Book> bookDataList;
    private User currentUser;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requested_books);
        bookList = findViewById(R.id.book_list);
        bookDataList = new ArrayList<>();
        bookAdapter = new customBookAdapter(this, bookDataList);
        bookList.setAdapter(bookAdapter);
        Button confirmBorrow = findViewById(R.id.ReturnorBorrow);
        confirmBorrow.setText("Confirm Borrow");
        final FirebaseFirestore Db = FirebaseFirestore.getInstance();
        final CollectionReference cr = Db.collection("Users");
        currentUser = (User) getIntent().getSerializableExtra("User");
        cr.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot d : value) {
                    final String username = d.getId();
                    CollectionReference foruser = Db.collection("Users/" + username + "/MyBooks");
                    foruser.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                            for (QueryDocumentSnapshot newBook : value2) {
                                HashMap<String,String> book_requests = (HashMap<String, String>) newBook.getData().get("Requests");
                                if (book_requests != null) {
                                    if (book_requests.containsKey(currentUser.getUsername())) {
                                        String book_title = newBook.getId();
                                        String book_author = (String) newBook.getData().get("Book Author");
                                        String book_ISBN = (String) newBook.getData().get("Book ISBN");
                                        String book_status = book_requests.get(currentUser.getUsername());

                                        String book_description = (String) newBook.getData().get("Book Description");
                                        String pickupLocation = (String) newBook.getData().get("Pickup Location");

                                        Book thisBook = new Book(book_title, book_author, book_ISBN, book_status, book_description, username, book_requests);
                                        thisBook.setLocation(pickupLocation);
                                        bookDataList.add(thisBook);
                                        bookAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
        confirmBorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BorrowBook.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);
            }
        });

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book temp = bookDataList.get(i);
                if (("Accepted").equals(temp.getStatus())){
                    ViewRequestFragment fragment = ViewRequestFragment.newInstance(temp);
                    fragment.show(getSupportFragmentManager(), "View_Location");
                }
            }
        });
    }

    @Override
    public void onViewPressed(String location){
        Intent intent = new Intent(getApplicationContext(), UserLocation.class);
        intent.putExtra("Flag",location);
        startActivity(intent);
    }
}
package com.example.dlpbgj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

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

public class View_Borrowed extends AppCompatActivity {
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
        Button returnBook = findViewById(R.id.ReturnorBorrow);
        returnBook.setText("Return Book");
        final FirebaseFirestore Db = FirebaseFirestore.getInstance();
        final CollectionReference cr = Db.collection("Users");
        currentUser = (User) getIntent().getSerializableExtra("User");
        cr.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                bookDataList.clear();
                for (QueryDocumentSnapshot d : value) {
                    final String username = d.getId();
                    CollectionReference foruser = Db.collection("Users/" + username + "/MyBooks");
                    foruser.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                            for (QueryDocumentSnapshot newBook : value2) {
                                String borrower = (String) newBook.getData().get("Borrower");
                                if (borrower != null) {
                                    if (borrower.equals(currentUser.getUsername())) {
                                        HashMap<String,String> book_requests = (HashMap<String, String>) newBook.getData().get("Requests");
                                        String book_title = newBook.getId();
                                        String book_author = (String) newBook.getData().get("Book Author");
                                        String book_ISBN = (String) newBook.getData().get("Book ISBN");
                                        String book_status = book_requests.get(currentUser.getUsername());
                                        String book_description = (String) newBook.getData().get("Book Description");
                                        System.out.println("Reached compare\n");
                                        Book thisBook = new Book(book_title, book_author, book_ISBN, book_status, book_description, username, book_requests);
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

        returnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReturnBook.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);
            }
        });
    }
}

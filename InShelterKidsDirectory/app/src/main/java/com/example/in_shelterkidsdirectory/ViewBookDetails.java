package com.example.dlpbgj;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewBookDetails extends AppCompatActivity {
    Button backButton;
    TextView title;
    TextView author;
    TextView isbn;
    TextView status;
    ImageView photo;


    /**
     * Created when the user long clicks on a book to view its details
     * This loads up a new activity that shows the details of the selected book.
     *
     * @param savedInstanceState
     */
    TextView description;
    TextView owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_book_details);
        Book book = (Book) getIntent().getSerializableExtra("Book");
        title = findViewById(R.id.Title);
        author = findViewById(R.id.Author);
        isbn = findViewById(R.id.ISBN);
        status = findViewById(R.id.Status);
        backButton = findViewById(R.id.Back);
        description = findViewById(R.id.Description);
        owner = findViewById(R.id.Owner);
        title.setText("Book Title: " + book.getTitle());
        author.setText("Book Author: " + book.getAuthor());
        isbn.setText("Book ISBN: " + book.getISBN());
        status.setText("Book Status: " + book.getStatus());
        description.setText("Book Description: " + book.getDescription());
        owner.setText("Current Owner: " + book.getOwner());
        photo = findViewById(R.id.bookphoto);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        StorageReference imagesRef = storageReference.child("images/" + book.getUid());
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUrl) {
                Glide
                        .with(getApplicationContext())
                        .load(downloadUrl.toString())
                        .centerCrop()
                        .into(photo);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
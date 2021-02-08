package com.example.in_shelterkidsdirectory;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class AddNote extends Activity {
    FirebaseFirestore db;
    EditText noteTitle, noteContent;
    CollectionReference userKidCollectionReference;
    ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        noteTitle = findViewById(R.id.addNoteTitle);
        noteContent = findViewById(R.id.addNoteContent);
        pbar = findViewById(R.id.progressBar);

        //Everything should be inside the onclick listener of FAB
        String title = noteTitle.getText().toString();
        String content = noteContent.getText().toString();
        pbar.setVisibility(View.VISIBLE);


        if(title.isEmpty() || content.isEmpty()){
            Toast.makeText(AddNote.this, "Should not have empty fields",Toast.LENGTH_SHORT).show();
            return;
        }

        db = FirebaseFirestore.getInstance();
        Kid kid = (Kid) getIntent().getSerializableExtra("Kid");
        userKidCollectionReference = db.collection("Kids");
        DocumentReference doc = userKidCollectionReference.document(kid.getFirstName() + kid.getLastName());
        DocumentReference docRef = doc.collection("notes").document();
        Map<String,Object> note = new HashMap<>();
        note.put("title", title);
        note.put("content", content);

        docRef.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AddNote.this, "Note saved",Toast.LENGTH_SHORT).show();
                onBackPressed();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddNote.this, "Error, couldn't save",Toast.LENGTH_SHORT).show();
                pbar.setVisibility(View.INVISIBLE);
            }
        });





 /*       FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }
}
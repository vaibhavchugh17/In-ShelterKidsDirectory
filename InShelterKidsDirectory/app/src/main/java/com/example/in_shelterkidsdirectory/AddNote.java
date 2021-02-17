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
        Kid kid = (Kid) getIntent().getSerializableExtra("Kid");
        Note note = (Note) getIntent().getSerializableExtra("Note");
        if (note != null){
            noteContent.setText(note.getContent());
            noteTitle.setText(note.getTitle());
        }


        FloatingActionButton fab = findViewById(R.id.save_note);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = noteTitle.getText().toString();
                String content = noteContent.getText().toString();
                pbar.setVisibility(View.VISIBLE);


                if(title.isEmpty() || content.isEmpty()){
                    Toast.makeText(AddNote.this, "Should not have empty fields",Toast.LENGTH_SHORT).show();
                    return;
                }

                db = FirebaseFirestore.getInstance();
                userKidCollectionReference = db.collection("Kids");
                DocumentReference doc = userKidCollectionReference.document(kid.getFirstName().toLowerCase() + kid.getMiddleName().toLowerCase() + kid.getLastName().toLowerCase());
                DocumentReference docRef = doc.collection("notes").document();

                if(note != null) {
                    docRef = doc.collection("notes").document(note.getId());
                }

                Map<String,Object> noteMap = new HashMap<>();
                noteMap.put("title", title);
                noteMap.put("content", content);

                docRef.set(noteMap).addOnSuccessListener(new OnSuccessListener<Void>() {
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

            }
        });






    }
}
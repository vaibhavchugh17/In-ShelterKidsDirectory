package com.example.in_shelterkidsdirectory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Notes extends AppCompatActivity {
    Kid kid;
    RecyclerView noteList;
    Adapter adapter;
    FirebaseFirestore db;
    CollectionReference kidNoteCollectionRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }



        kid = (Kid) getIntent().getSerializableExtra(AddKidFragment.EXTRA_MESSAGE3);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(kid.getFirstName() + "'s Notes");
        noteList = findViewById(R.id.noteList);
        ArrayList<Note> kidNotes = new ArrayList<>();

        adapter = new Adapter(kidNotes, kid);
        noteList.setLayoutManager(new LinearLayoutManager(this)); //Vid5
        noteList.setAdapter(adapter);

        final FloatingActionButton addKidButton = findViewById(R.id.add_notes_button);  //Invoking a fragment to add the kids when the FAB is clicked
        addKidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddNote.class);
                intent.putExtra("Kid", kid);
                startActivity(intent);

            }
        });

        db = FirebaseFirestore.getInstance();
        kidNoteCollectionRef = db.collection("Kids").document(kid.getFirstName().toLowerCase() + kid.getMiddleName().toLowerCase() + kid.getLastName().toLowerCase()).collection("notes");
        kidNoteCollectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                kidNotes.clear();
                adapter.notifyDataSetChanged();
                for (QueryDocumentSnapshot newNote : value) {
                    String fireContent = (String) newNote.getData().get("content");
                    String fireTitle = (String) newNote.getData().get("title");
                    Note note = new Note (fireTitle,fireContent);
                    note.setId(newNote.getId().toString());
                    kidNotes.add(note);
                    adapter.notifyDataSetChanged();
                    kid.setNotes(kidNotes);
                }
            }
        });






    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
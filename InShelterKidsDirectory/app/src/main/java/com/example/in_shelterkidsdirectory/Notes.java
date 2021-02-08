package com.example.in_shelterkidsdirectory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class Notes extends AppCompatActivity {
    private Kid kid;
    RecyclerView noteList;
    Adapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        kid = (Kid) getIntent().getSerializableExtra(AddKidFragment.EXTRA_MESSAGE3);
        noteList = findViewById(R.id.noteList);

        List<String> titles = new ArrayList<>();
        List<String> content = new ArrayList<>();

        titles.add("First Note Title");
        content.add("First Note Content");

        adapter = new Adapter(titles,content);
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

    }
}
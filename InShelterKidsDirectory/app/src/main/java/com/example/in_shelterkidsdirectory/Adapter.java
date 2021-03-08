package com.example.in_shelterkidsdirectory;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    ArrayList<Note> kidNotes;
    Kid kid;
    String flag;
    FirebaseFirestore db;
    public Adapter(ArrayList<Note> notes, Kid k, String flag){
        this.kidNotes = notes;
        this.kid = k;
        this.flag = flag;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.noteTitle.setText(kidNotes.get(position).getTitle());
        holder.noteContent.setText(kidNotes.get(position).getContent());


        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), AddNote.class);
                HashMap<String,Object> extras = new HashMap<>();
                extras.put("Kid",kid);
                extras.put("Note",kidNotes.get(position));
                extras.put("Flag",flag);
                i.putExtra("Extras",extras);
                v.getContext().startActivity(i);
            }
        });


        ImageView menuIcon =   holder.view.findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(v.getContext(),v);
                menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        db = FirebaseFirestore.getInstance();
                        DocumentReference dRef;
                        if (flag =="Notes"){
                             dRef =db.collection("Kids").document(kid.getFirstName() + kid.getLastName() + kid.getUID()).collection("notes").document(kidNotes.get(position).getId());
                        }
                        else{
                            dRef =db.collection("Kids").document(kid.getFirstName() + kid.getLastName() + kid.getUID()).collection("Concerns").document(kidNotes.get(position).getId());
                        }
                        dRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if (flag =="Notes"){
                                    Toast.makeText(v.getContext(), "Note deleted" ,Toast.LENGTH_SHORT).show();

                                }
                                else{
                                    Toast.makeText(v.getContext(), "Concern deleted" ,Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(v.getContext(), "Error deleting node" ,Toast.LENGTH_SHORT).show();
                            }
                        });

                        return false;
                    }
                });

                menu.show();

            }
        });
    }



    @Override
    public int getItemCount() {
        return kidNotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle,noteContent;
        View view;
        CardView mCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            mCardView = itemView.findViewById(R.id.noteCard);
            view = itemView;
        }
    }
}

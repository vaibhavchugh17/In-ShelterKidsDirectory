package com.example.in_shelterkidsdirectory;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.awt.font.NumericShaper;
import java.util.ArrayList;

public class customProfileAdapter extends ArrayAdapter<String> {

    private final ArrayList<String> users;
    private final Context context;
    FirebaseStorage storage;
    StorageReference storageReference;


    public customProfileAdapter(Context context, ArrayList<String> users) {
        super(context, 0, users);
        this.users = users;
        this.context = context;
    }

    static class ViewHolder{
        ImageView dispImg;
        TextView dispUser;
    }

    /**
     * Function to use our custom array adapter to show the different profiles of users.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.display_user_profiles, parent, false); //Attaches layout from bookcontent to each item inside the ListView
            viewHolder = new customProfileAdapter.ViewHolder();
            viewHolder.dispUser = view.findViewById(R.id.textViewP);
            viewHolder.dispImg = view.findViewById(R.id.imageViewP);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (customProfileAdapter.ViewHolder)convertView.getTag();
        }

        String username = users.get(position);
        viewHolder.dispUser.setText(username);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        viewHolder.dispImg.setImageResource(R.drawable.load);
        storageReference.child("images/" + username).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Picasso.get().load(uri.toString()).into(viewHolder.dispImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                viewHolder.dispImg.setImageResource(R.drawable.defaultprofile);
            }
        });
    return view;

    }
}

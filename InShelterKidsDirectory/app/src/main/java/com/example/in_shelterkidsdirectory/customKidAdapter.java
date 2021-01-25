package com.example.in_shelterkidsdirectory;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class customKidAdapter extends ArrayAdapter<Kid> {

    private final ArrayList<Kid> kids;
    private final Context context;
    FirebaseStorage storage;
    StorageReference storageReference;

    public customKidAdapter(Context context, ArrayList<Kid> kids) {
        super(context, 0, kids);
        this.kids = kids;
        this.context = context;
    }

    /**
     * Function to use our custom array adapter to show the kids of a user.
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

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.bookcontent, parent, false); //Attaches layout from kidcontent to each item inside the ListView
        }

        Kid kid = kids.get(position);

        ImageView img = view.findViewById(R.id.imageView1);
        TextView kidTitle = view.findViewById(R.id.textView1);
        TextView kidDob = view.findViewById(R.id.textView2);
        TextView kidStatus = view.findViewById(R.id.textView3);

        kidTitle.setText(kid.getFirstName() + " " + kid.getLastName());
        kidDob.setText(kid.getDOB());
        kidStatus.setText(kid.getStatus()); //Setting the values of each textView inside the view in ListView



        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference imagesRef =  storageReference.child("images/");
        final StorageReference defaultRef = imagesRef.child("default.png");
        try {
            final StorageReference ref = imagesRef.child(kid.getUID());
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri downloadUrl) {
                    Glide
                            .with(context)
                            .load(downloadUrl.toString())
                            .centerCrop()
                            .into(img);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("KidImageError", e.getMessage());
                    defaultRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUrl) {

                            Glide
                                    .with(context)
                                    .load(downloadUrl.toString())
                                    .centerCrop()
                                    .into(img);
                        }
                    });


                }
            });
        }
        catch (Exception e){
            defaultRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri downloadUrl) {

                    Glide
                            .with(context)
                            .load(downloadUrl.toString())
                            .centerCrop()
                            .into(img);
                }
            });
        }


        return view;

    }
}

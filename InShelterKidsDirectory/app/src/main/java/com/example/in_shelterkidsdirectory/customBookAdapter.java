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

public class customBookAdapter extends ArrayAdapter<Book> {

    private final ArrayList<Book> books;
    private final Context context;
    FirebaseStorage storage;
    StorageReference storageReference;

    public customBookAdapter(Context context, ArrayList<Book> books) {
        super(context, 0, books);
        this.books = books;
        this.context = context;
    }

    /**
     * Function to use our custom array adapter to show the books of a user.
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
            view = LayoutInflater.from(context).inflate(R.layout.bookcontent, parent, false); //Attaches layout from bookcontent to each item inside the ListView
        }

        Book book = books.get(position);

        ImageView img = view.findViewById(R.id.imageView1);
        TextView bookTitle = view.findViewById(R.id.textView1);
        TextView bookOwner = view.findViewById(R.id.textView2);
        TextView bookStatus = view.findViewById(R.id.textView3);


        bookTitle.setText(book.getTitle());
        bookStatus.setText(book.getStatus()); //Setting the values of each textView inside the view in ListView
        bookOwner.setText(book.getOwner());


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference imagesRef =  storageReference.child("images/");
        final StorageReference defaultRef = imagesRef.child("defaultb.png");
        try {
            final StorageReference ref = imagesRef.child(book.getUid());
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
                    Log.d("BookImageError", e.getMessage());
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

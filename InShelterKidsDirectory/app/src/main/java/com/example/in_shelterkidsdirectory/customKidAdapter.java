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
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

    static class ViewHolder{
        ImageView img;
        TextView kidTitle;
        TextView kidDob;
        TextView kidStatus;
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
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getContext()));
        View view = convertView;
        ViewHolder viewHolder;
        Kid kid = kids.get(position);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference imagesRef =  storageReference.child("images/");
        final StorageReference defaultRef = imagesRef.child("default.png");
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.bookcontent, parent, false);
            //Attaches layout from kidcontent to each item inside the ListView
            viewHolder = new ViewHolder();
            viewHolder.img = view.findViewById(R.id.imageView1);
            viewHolder.kidTitle = view.findViewById(R.id.textView1);
            viewHolder.kidDob = view.findViewById(R.id.textView2);
            viewHolder.kidStatus = view.findViewById(R.id.textView3);
            view.setTag(viewHolder);
        }

        else{
            viewHolder= (ViewHolder)convertView.getTag();
        }
        viewHolder.img.setImageResource(R.drawable.load);
        storageReference.child("images/" + kid.getUID()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Picasso.get().load(uri.toString()).into(viewHolder.img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                viewHolder.img.setImageResource(R.drawable.defaultprofile);
            }
        });
        viewHolder.kidTitle.setText(kid.getFirstName() + " " + kid.getLastName());
        viewHolder.kidDob.setText(kid.getDOB());
        viewHolder.kidStatus.setText(kid.getStatus()); //Setting the values of each textView inside the view in ListView
        return view;

    }
}

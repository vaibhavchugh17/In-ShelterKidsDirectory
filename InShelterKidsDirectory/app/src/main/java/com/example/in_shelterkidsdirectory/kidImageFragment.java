package com.example.in_shelterkidsdirectory;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class kidImageFragment extends DialogFragment implements Serializable {
    FirebaseStorage storage;
    StorageReference storageReference;
    private OnFragmentInteractionListener listener;
    Button picture, picture2, deletePhoto;
    private Kid kid;
    private final int REQUEST = 22;
    private Uri path;
    ImageView profile;




    static kidImageFragment newInstance(Kid kid) {
        Bundle args = new Bundle();
        args.putSerializable("Kid", kid);
        kidImageFragment fragment = new kidImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        System.out.println(context.toString());
        System.out.println(getContext().toString());
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.kid_image_fragment, null);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        profile = view.findViewById(R.id.KidProfilePic);
        picture = view.findViewById(R.id.selectKidPicture);
        picture2 = view.findViewById(R.id.uploadKidPicture);
        deletePhoto = view.findViewById(R.id.deleteKidPicture);
        String title = "Kid Profile Picture";

        if (getArguments() != null) {
            kid = (Kid) getArguments().get("Kid");
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference();
            StorageReference imagesRef = storageReference.child("images/default.png");
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri downloadUrl) {
                    Glide
                            .with(getContext())
                            .load(downloadUrl.toString())
                            .centerCrop()
                            .into(profile);
                }
            });
        }

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectPhoto();
            }
        });
        picture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto(kid);

            }
        });

        deletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (kid.getUID() != null){
                    StorageReference ref = storageReference.child("images/" + kid.getUID());
                    ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast toast = Toast.makeText(getContext(), "Kid Photo Successfully deleted!", Toast.LENGTH_SHORT);
                            toast.show();
                            Fragment currentFragment = getFragmentManager().findFragmentByTag("ADD_BOOK");
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            fragmentTransaction.detach(currentFragment);
                            fragmentTransaction.attach(currentFragment);
                            fragmentTransaction.commit();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast toast = Toast.makeText(getContext(), "Failed to delete kid photo!", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
                }
                else{
                    Toast toast = Toast.makeText(getContext(), "Please upload a kid photo first :)", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }

        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle(title)
                .setPositiveButton("BACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onBackPressed();
                    }
                }).create();
    }


    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            path = data.getData();
            try {
                Context applicationContext = Kids.getContextOfApplication();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(), path);
                profile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void SelectPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST);
    }

    private void uploadPhoto(Kid kid) {
        if (path != null) {
            final ProgressDialog statusDialog = new ProgressDialog(this.getContext());
            statusDialog.setTitle("Uploading");
            statusDialog.show();
            //Log.d("Kid Fragment",kid.getUid());
            StorageReference ref = storageReference.child("images/" + kid.getUID());
            ref.putFile(path).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    statusDialog.dismiss();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            statusDialog.dismiss();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            statusDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#202F65"));
    }

    public interface OnFragmentInteractionListener {
        void onBackPressed();
    }

}

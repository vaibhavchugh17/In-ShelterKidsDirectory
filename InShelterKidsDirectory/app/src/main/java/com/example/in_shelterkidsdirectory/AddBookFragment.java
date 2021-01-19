
package com.example.in_shelterkidsdirectory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class AddBookFragment extends DialogFragment implements Serializable {
    private TextInputEditText kidFirstName;
    private TextInputEditText kidLastName;
    private TextInputEditText kidMiddleName;
    private TextInputEditText kidNationality;
    private TextInputEditText kidHeight;
    private TextInputEditText kidDOB;
    private TextInputEditText kidEyeColor;
    private TextInputEditText kidHairColor;
    private Kid kid;
    private TextView kidStatus;
    private ImageView kidPic;

    //To add. Button for adding parents. To add functionalities for notes, referrals, concerns, allergies, birthmarks, legalGuardians


    private String bookUid;
    private OnFragmentInteractionListener listener;
    private final int REQUEST = 22;
    private Uri path;
    private final String statusStr = "Kid Status -";
    FirebaseStorage storage;
    StorageReference storageReference;


    public interface OnFragmentInteractionListener {
        void onOkPressed(Book newBook);

        void onOkPressed(Book book, String oldBookName);

        void onDeletePressed(Book book);

        void onOkPressed();
    }

    static AddBookFragment newInstance(Book book, User user) {
        Bundle args = new Bundle();
        args.putSerializable("Book", book);
        args.putSerializable("User", user);
        AddBookFragment fragment = new AddBookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    static AddBookFragment newInstance(String uid){
        Bundle args = new Bundle();
        args.putSerializable("Uid",uid);
        AddBookFragment fragment = new AddBookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * context is the host activity. Attaches the fragment to the host activity.
     * This is because this fragment may be used launched by more than one activities.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * When a book is selected, the edit fragment opens up
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.add_book_fragment_layout, null);
        kidFirstName = view.findViewById(R.id.kid_firstName);
        kidLastName = view.findViewById(R.id.kid_lastName);
        kidMiddleName = view.findViewById(R.id.kid_middleName);
        kidNationality = view.findViewById(R.id.kid_nationality);
        kidHeight = view.findViewById(R.id.kid_height);
        kidDOB = view.findViewById(R.id.kidBirthDate);
        kidEyeColor = view.findViewById(R.id.kid_eyeColor);
        kidHairColor = view.findViewById(R.id.kid_hairColor);

        kidPic = view.findViewById(R.id.kidPic);
        kidStatus = view.findViewById(R.id.kid_status_editText);
        final ArrayList<String> validStatus = new ArrayList<String>();
        validStatus.add("Residential");
        validStatus.add("Out-Reach");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        Button picture = view.findViewById(R.id.Picture);
        Button picture2 = view.findViewById(R.id.Picture1);
        Button deletePhoto = view.findViewById(R.id.delete_photo);
        Spinner spinner = view.findViewById(R.id.kid_status);
        final ArrayList<String> Statuses = new ArrayList<>();
        Statuses.add("Select Status:");
        Statuses.add("Residential");
        Statuses.add("Out-Reach");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout, Statuses);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(adapter);

        String title = "Add Kid";

        if (getArguments().get("Book") != null) {
            book = (Book) getArguments().get("Book");
            kid  = (Kid) getArguments().get("Kid");
            title = "Edit Kid";

            kidFirstName.setText(kid.getFirstName());
            kidLastName.setText(kid.getLastName());
            kidMiddleName.setText(kid.getMiddleName());
            kidNationality.setText(kid.getNationality());
            kidHeight.setText(kid.getHeight()); //Convert to string
            kidDOB.setText(kid.getDOB());
            kidEyeColor.setText(kid.getEyeColor());
            kidHairColor.setText(kid.getHairColor());
            kidStatus.setText(kid.getStatus());

            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference();
            if (book.getUid()!=null){
                StorageReference imagesRef = storageReference.child("images/" + book.getUid());
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrl) {
                        Glide
                                .with(getContext())
                                .load(downloadUrl.toString())
                                .centerCrop()
                                .into(bookPic);
                    }
                });
            }

        }
        else if (getArguments().get("Uid")!=null){
            bookUid = (String)getArguments().get("Uid");
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    if (getArguments().get("Book") != null){
                        kidStatus.setText("Kid Status -" + book.getStatus());
                    }
                    else{
                        kidStatus.setText("Kid Status -");
                    }
                }
                else{
                    kidStatus.setText("Kid Status -" + Statuses.get(i));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (getArguments().get("Book") != null){
                    kidStatus.setText("Book Status -" + kid.getStatus());
                }
                else{
                    kidStatus.setText(statusStr);
                }
            }
        });
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectPhoto();
            }
        });
        picture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto(book);

            }
        });
        deletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (book.getUid() != null){
                    StorageReference ref = storageReference.child("images/" + book.getUid());
                    ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast toast = Toast.makeText(getContext(), "Book Photo Successfully deleted!", Toast.LENGTH_SHORT);
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
                                    Toast toast = Toast.makeText(getContext(), "Failed to delete book photo!", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            });
                }
                else{
                    Toast toast = Toast.makeText(getContext(), "Please upload a book photo first :)", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }

        });
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle(title)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Delete", null)
                .setPositiveButton(android.R.string.ok, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button bOk = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                Button bDel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);

                bDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getArguments().get("Book") != null) {
                            book = (Book) getArguments().get("Book");
                            listener.onDeletePressed(book);
                        } else {
                            listener.onOkPressed();
                        }
                        dialog.dismiss();

                    }

                });

                bOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        String first_name = kidFirstName.getText().toString();
                        String last_name = kidLastName.getText().toString();
                        String middle_name = kidMiddleName.getText().toString();
                        String nationality = kidNationality.getText().toString();
                        String height = kidHeight.getText().toString();
                        String dob = kidDOB.getText().toString();
                        String eye_color = kidEyeColor.getText().toString();
                        String hair_color =kidHairColor.getText().toString();
                        String kid_status = kidStatus.getText().toString();
                        kid_status = kid_status.replace(statusStr,"");
                        View focus = null;
                        boolean wrong_input = false;

                        if (first_name.equals("")) { //Mandatory to enter kid's first name
                            kidFirstName.setError("Please enter the first name of the kid");
                            wrong_input = true;
                            focus = kidFirstName;
                        }
                        if (last_name.equals("")) { //Mandatory to enter kid's last name
                            kidLastName.setError("Please enter the last name of the kid");
                            wrong_input = true;
                            focus = kidLastName;
                        }

                        if (dob.equals("")) { //Mandatory to enter kid's DOB
                            kidDOB.setError("Please enter the DOB of the kid");
                            wrong_input = true;
                            focus = kidDOB;
                        }



                        if (!validStatus.contains(kid_status)) { //Input validation for the status
                            kidStatus.setError("Please choose a valid status from drop-down menu");
                            wrong_input = true;
                            focus = spinner;

                        }
                        if (middle_name.equals("")) {
                            middle_name = "";

                        }
                        if (nationality.equals("")) {
                            nationality = "Unknown";
                        }
                        if (height.equals("")) {
                            height = "Unknown";
                        }

                        if (eye_color.equals("")) {
                            eye_color = "Unknown";
                        }
                        if (hair_color.equals("")) {
                            hair_color = "Unknown";
                        }



                        if (wrong_input) {
                            focus.requestFocus();

                        } else if (getArguments().get("Book") != null) {

                            Book book = (Book) getArguments().get("Book");
                            User user = (User) getArguments().get("User");

                            Kid kid = (Kid) getArguments().get("Kid");
                            kid.setFirstName(first_name);
                            kid.setLastName(last_name);
                            kid.setMiddleName(middle_name);
                            kid.setDOB(dob);
                            kid.setEyeColor(eye_color);
                            kid.setHairColor(hair_color);
                            kid.setNationality(nationality);
                            kid.setHeight(height);
                            kid.setStatus(kid_status);







                            String temp = book.getTitle();

                            book.setAuthor(book_author);
                            book.setISBN(book_ISBN);
                            book.setStatus(book_status.replace(statusStr,""));
                            book.setTitle(book_title);
                            book.setDescription(book_description);
                            book.setOwner(user.getUsername());
                            listener.onOkPressed(book, temp);
                            dialog.dismiss();
                        } else {
                            listener.onOkPressed(new Book(book_title, book_author, book_ISBN, book_status, book_description)); //Send the inputted book as a parameter to the main function's implementation of this method
                            dialog.dismiss();
                        }

                    }
                });

            }
        });
        dialog.show();


        return dialog;
    }

    /**
     * gets the result from barcode_scanner class
     * Sets the desired result inside the fragment
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            path = data.getData();
            try {
                Context applicationContext = Kids.getContextOfApplication();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(applicationContext.getContentResolver(), path);
                bookPic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#202F65"));
    }

    private void SelectPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST);
    }

    private void uploadPhoto(Book book) {
        if (path != null) {
            final ProgressDialog statusDialog = new ProgressDialog(this.getContext());
            statusDialog.setTitle("Uploading");
            statusDialog.show();
            //Log.d("Book Fragment",book.getUid());
            StorageReference ref = storageReference.child("images/" + bookUid);
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

}






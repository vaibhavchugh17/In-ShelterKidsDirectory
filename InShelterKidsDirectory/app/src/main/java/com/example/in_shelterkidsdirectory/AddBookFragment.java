
package com.example.dlpbgj;

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
    private TextInputEditText bookTitle;
    private TextInputEditText bookAuthor;
    private TextInputEditText bookISBN;
    private String bookUid;
    private TextView bookStatus;
    private TextInputEditText bookDescription;
    private OnFragmentInteractionListener listener;
    private final int REQUEST = 22;
    private Uri path;
    private ImageView bookPic;
    private Book book;
    private final String statusStr = "Book Status -";
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
        bookPic = view.findViewById(R.id.bookPic);
        bookTitle = view.findViewById(R.id.book_title_editText);
        bookAuthor = view.findViewById(R.id.book_author_editText);
        bookISBN = view.findViewById(R.id.book_ISBN_editText);
        bookStatus = view.findViewById(R.id.book_status_editText);
        bookDescription = view.findViewById(R.id.book_description_editText);
        final ArrayList<String> validStatus = new ArrayList<String>();
        validStatus.add("Available");
        validStatus.add("Borrowed");
        validStatus.add("Accepted");
        validStatus.add("Requested");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        ImageButton scan = view.findViewById(R.id.scan2);
        Button picture = view.findViewById(R.id.Picture);
        Button picture2 = view.findViewById(R.id.Picture1);
        Button deletePhoto = view.findViewById(R.id.delete_photo);
        Spinner spinner = view.findViewById(R.id.book_status);
        final ArrayList<String> Statuses = new ArrayList<>();
        Statuses.add("Select Status:");
        Statuses.add("Available");
        Statuses.add("Borrowed");
        Statuses.add("Requested");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout, Statuses);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(adapter);

        String title = "Add Book";

        if (getArguments().get("Book") != null) {
            book = (Book) getArguments().get("Book");
            title = "Edit Book";
            bookTitle.setText(book.getTitle());
            bookAuthor.setText(book.getAuthor());
            bookISBN.setText(book.getISBN());
            bookStatus.setText(book.getStatus());
            bookDescription.setText(book.getDescription());
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
        /**
         * When scan button is clicked
         * Starts new activity for scanning the barcode
         */
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), barcode_scanner.class);
                startActivityForResult(intent, 1);

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    if (getArguments().get("Book") != null){
                        bookStatus.setText("Book Status -" + book.getStatus());
                    }
                    else{
                        bookStatus.setText("Book Status -");
                    }
                }
                else{
                    bookStatus.setText("Book Status -" + Statuses.get(i));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (getArguments().get("Book") != null){
                    bookStatus.setText("Book Status -" + book.getStatus());
                }
                else{
                    bookStatus.setText(statusStr);
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
                        String book_title = bookTitle.getText().toString();
                        String book_author = bookAuthor.getText().toString();
                        String book_ISBN = bookISBN.getText().toString();
                        String book_status = bookStatus.getText().toString();
                        book_status = book_status.replace(statusStr,"");
                        String book_description = bookDescription.getText().toString();
                        View focus = null;
                        boolean wrong_input = false;
                        if (bookTitle.getText().toString().equals("")) { //Mandatory to enter book's title
                            bookTitle.setError("Please enter the book's title!");
                            wrong_input = true;
                            focus = bookTitle;
                        }
/*
                        if (book_status.equals("")) { //Mandatory to enter book's status
                            bookStatus.setError("Please select the book's status");
                            wrong_input = true;
                            focus = bookStatus;
                        }*/

                        if (book_description.equals("")) {    //Mandatory to enter book's description
                            bookDescription.setError("Please enter the book's description");
                            wrong_input = true;
                            focus = bookDescription;

                        }
                       if (!validStatus.contains(book_status)) { //Input validation for the status
                            bookStatus.setError("Please choose a valid status from drop-down menu");
                            wrong_input = true;
                            focus = spinner;

                        }
                        if (book_author.equals("")) {
                            book_author = "Unknown";

                        }
                        if (book_ISBN.equals("")) {
                            book_ISBN = "Unknown";
                        }

                        if (wrong_input) {
                            focus.requestFocus();

                        } else if (getArguments().get("Book") != null) {

                            Book book = (Book) getArguments().get("Book");
                            User user = (User) getArguments().get("User");

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
        if (requestCode == 1) {
            if (resultCode == -1) {
                String code = data.getStringExtra("ISBN");
                bookISBN.setText(code);
            }
        }
        if (requestCode == REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            path = data.getData();
            try {
                Context applicationContext = MyBooks.getContextOfApplication();
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






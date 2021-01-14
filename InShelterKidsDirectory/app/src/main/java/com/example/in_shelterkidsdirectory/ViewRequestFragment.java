package com.example.dlpbgj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;

public class ViewRequestFragment extends DialogFragment implements Serializable {
    private ViewRequestFragment.OnFragmentInteractionListener listener;
    Book book;

    public interface OnFragmentInteractionListener {
        void onViewPressed(String location);
    }
    static ViewRequestFragment newInstance(Book book) {
        Bundle args = new Bundle();
        args.putSerializable("Book", book);
        ViewRequestFragment fragment = new ViewRequestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ViewRequestFragment.OnFragmentInteractionListener) {
            listener = (ViewRequestFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.image_fragment, null);
        String title = "View Pickup Location";
        if (getArguments() != null) {
            book = (Book) getArguments().get("Book");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle(title)
                .setNegativeButton("Back",null)
                .setPositiveButton("View Location", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onViewPressed(book.getLocation());
                    }
                }).create();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#B59C34"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#B59C34"));
    }

}
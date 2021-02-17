package com.example.in_shelterkidsdirectory;

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

public class SelectionFragment extends DialogFragment implements Serializable, CommonFragment.OnFragmentInteractionListener {
    private SelectionFragment.OnFragmentInteractionListener listener;
    Kid kid;
    static SelectionFragment newInstance(Kid kid) {
        Bundle args = new Bundle();
        args.putSerializable("Kid", kid);
        SelectionFragment fragment = new SelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SelectionFragment.OnFragmentInteractionListener) {
            listener = (SelectionFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.image_fragment, null);
        String title = "Select a Parent";
        if (getArguments() != null) {
            kid = (Kid) getArguments().get("Kid");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle(title)
                .setNegativeButton("Add Mother", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonFragment fragment = CommonFragment.newInstance(kid, "Mother");
                        fragment.show(getFragmentManager(),"Add_Parent");
                    }
                })
                .setPositiveButton("Add Father", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CommonFragment fragment = CommonFragment.newInstance(kid, "Father");
                        fragment.show(getFragmentManager(),"Add_Parent");
                    }
                }).create();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#B59C34"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#B59C34"));
    }

    public interface OnFragmentInteractionListener {
        void onBackPressed();
    }

    @Override
    public void onAddPressed(){
        //nothing yet
    }


}
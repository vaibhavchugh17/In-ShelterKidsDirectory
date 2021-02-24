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
        String mother_title = "Add Mother's Information";
        String father_title = "Add Father's Information";
        String guardian_title = "Add Guardian's Information";
        if (getArguments() != null) {
            kid = (Kid) getArguments().get("Kid");
            if (kid.getMother()!=null){
                if (kid.getMother().getFirstName() != null){
                    mother_title = "View Mother's details";
                }
            }
            if (kid.getFather()!=null){
                if (kid.getFather().getFirstName() != null){
                    father_title = "View Father's details";
                }
            }
            if (kid.getGuardian()!=null){
                if (kid.getGuardian().getFirstName() != null){
                    guardian_title = "View Guardian's details";
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle(title)
                .setNeutralButton(guardian_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonFragment fragment = CommonFragment.newInstance(kid, "Guardian");
                        fragment.show(getFragmentManager(),"Add_Parent");
                    }
                })
                .setNegativeButton(mother_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonFragment fragment = CommonFragment.newInstance(kid, "Mother");
                        fragment.show(getFragmentManager(),"Add_Parent");
                    }
                })
                .setPositiveButton(father_title, new DialogInterface.OnClickListener() {
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
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#202F65"));
    }

    public interface OnFragmentInteractionListener {
        void onBackPressed();
    }

    @Override
    public void onAddPressed(){
        //nothing yet
    }

    @Override
    public void onDeletePressed(Parent referral){
        //nothing yet
    }



}
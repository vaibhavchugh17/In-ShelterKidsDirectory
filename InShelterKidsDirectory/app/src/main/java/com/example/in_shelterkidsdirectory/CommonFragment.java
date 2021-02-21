package com.example.in_shelterkidsdirectory;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.util.Calendar;

public class CommonFragment extends DialogFragment implements Serializable {
    FirebaseStorage storage;
    StorageReference storageReference;
    private CommonFragment.OnFragmentInteractionListener listener;
    private Kid kid;
    private TextInputEditText parentFirstName;
    private TextInputEditText parentLastName;
    private TextInputEditText parentMiddleName;
    private TextInputEditText parentOccupation;
    private TextInputEditText parentAddress;
    private TextInputEditText parentDOB;
    private TextInputEditText parentNumber;
    private DatePickerDialog.OnDateSetListener DateListener;
    private Button dateOfBirth;
    private RelativeLayout r_layout;
    private String title;

    public interface OnFragmentInteractionListener {
        void onAddPressed();
    }

    static CommonFragment newInstance(Kid kid, String flag) {
        Bundle args = new Bundle();
        args.putSerializable("Kid", kid);
        args.putSerializable("Flag",flag);
        CommonFragment fragment = new CommonFragment();
        fragment.setArguments(args);
        return fragment;
    }

    static CommonFragment newInstance(Kid kid, Parent Referral){
        Bundle args = new Bundle();
        args.putSerializable("Kid", kid);
        args.putSerializable("Referral",Referral);
        CommonFragment fragment = new CommonFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CommonFragment.OnFragmentInteractionListener) {
            listener = (CommonFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.common_fragment_layout, null);
        parentFirstName = view.findViewById(R.id.parent_firstName);
        parentMiddleName = view.findViewById(R.id.parent_middleName);
        parentLastName = view.findViewById(R.id.parent_lastName);
        parentDOB = view.findViewById(R.id.parentBirthDate);
        parentNumber = view.findViewById(R.id.parent_number);
        parentOccupation = view.findViewById(R.id.parent_occupation);
        parentAddress = view.findViewById(R.id.parent_home_address);
        dateOfBirth = view.findViewById(R.id.select_date_parent);
        r_layout = view.findViewById(R.id.r_layout);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int date = calendar.get(Calendar.DATE);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog dialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,DateListener,year,month,date);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        DateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                String temp = i2 + "/" + i1 + "/" + i ;
                parentDOB.setText(temp);
            }
        };



        if (getArguments() != null) {
            kid = (Kid) getArguments().get("Kid");
            String flag = (String) getArguments().get("Flag");
            if (flag == null){
                flag = "";
            }
            if (flag.equals("Father")){
                title = "Father Information";
                if (kid.getFather() != null){
                    Parent father = kid.getFather();
                    parentFirstName.setText(father.getFirstName());
                    parentLastName.setText(father.getLastName());
                    parentAddress.setText(father.getHomeAddress());
                    parentOccupation.setText(father.getOccupation());
                    parentNumber.setText(father.getPhoneNumber());
                    parentDOB.setText(father.getDOB());
                }
            }
            else if (flag.equals("Mother")){
                title = "Mother Information";
                if (kid.getMother() != null){
                    Parent mother = kid.getMother();
                    parentFirstName.setText(mother.getFirstName());
                    parentLastName.setText(mother.getLastName());
                    parentAddress.setText(mother.getHomeAddress());
                    parentOccupation.setText(mother.getOccupation());
                    parentNumber.setText(mother.getPhoneNumber());
                    parentDOB.setText(mother.getDOB());
                }
            }
            else {
                title = "Referral Information";
                if (getArguments().get("Referral") != null){
                    title = "Edit Referral Information";
                    Parent referral = (Parent) getArguments().get("Referral");
                    parentFirstName.setText(referral.getFirstName());
                    parentLastName.setText(referral.getLastName());
                    parentAddress.setText(referral.getHomeAddress());
                    parentOccupation.setText(referral.getOccupation());
                    parentNumber.setText(referral.getPhoneNumber());
                    parentDOB.setText(referral.getDOB());
                }
            }

        }



        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle(title)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String flag = (String)getArguments().get("Flag");
                        String firstName = parentFirstName.getText().toString();
                        if (flag == null){
                            flag = "";
                        }

                        if (flag.equals("Referral")){
                            String lastName = parentLastName.getText().toString();
                            String DOB = parentDOB.getText().toString();
                            String occupation = parentOccupation.getText().toString();
                            String number = parentNumber.getText().toString();
                            String address = parentAddress.getText().toString();
                            Parent temp = new Parent(firstName,lastName,DOB,address,occupation,number);
                            kid.addReferrals(temp);
                            listener.onAddPressed();
                        }
                        else if (flag.equals("Father")){
                            String lastName = parentLastName.getText().toString();
                            String DOB = parentDOB.getText().toString();
                            String occupation = parentOccupation.getText().toString();
                            String number = parentNumber.getText().toString();
                            String address = parentAddress.getText().toString();
                            Parent temp = new Parent(firstName,lastName,DOB,address,occupation,number);
                            kid.setFather(temp);
                            listener.onAddPressed();
                        }
                        else if (flag.equals("Mother")){
                            String lastName = parentLastName.getText().toString();
                            String DOB = parentDOB.getText().toString();
                            String occupation = parentOccupation.getText().toString();
                            String number = parentNumber.getText().toString();
                            String address = parentAddress.getText().toString();
                            Parent temp = new Parent(firstName,lastName,DOB,address,occupation,number);
                            kid.setMother(temp);
                            listener.onAddPressed();
                        }
                        else{
                            String lastName = parentLastName.getText().toString();
                            String DOB = parentDOB.getText().toString();
                            String occupation = parentOccupation.getText().toString();
                            String number = parentNumber.getText().toString();
                            String address = parentAddress.getText().toString();
                            Parent referral = (Parent) getArguments().get("Referral");
                            kid.removeReferral(referral);
                            referral.setLastName(lastName);
                            referral.setFirstName(firstName);
                            referral.setDOB(DOB);
                            referral.setOccupation(occupation);
                            referral.setPhoneNumber(number);
                            referral.setHomeAddress(address);
                            kid.addReferrals(referral);
                            listener.onAddPressed();
                        }
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

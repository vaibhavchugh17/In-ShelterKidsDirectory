
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
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
import java.util.Calendar;
import java.util.HashMap;

public class AddKidFragment extends DialogFragment implements Serializable, CommonFragment.OnFragmentInteractionListener, SelectionFragment.OnFragmentInteractionListener {
    public static final String EXTRA_MESSAGE3 = "com.example.dlpbgj.MESSAGE3";
    private TextInputEditText kidFirstName;
    private TextInputEditText kidLastName;
    private TextInputEditText kidMiddleName;
    private TextInputEditText kidNationality;
    private TextInputEditText kidHeight;
    private TextInputEditText kidDOB;
    private TextInputEditText kidEyeColor;
    private TextInputEditText kidHairColor;
    private TextInputEditText kidAllergies;
    private TextInputEditText kidBirthmarks;
    private DatePickerDialog.OnDateSetListener DateListener;
    private Button kidDobButton;
    private Button parentButton;
    private Button concernsButton;
    private Button referralsButton;
    private Button notesButton;
    private Kid kid = new Kid();
    private TextView kidStatus;
    private ImageView kidPic;

    //To add. Button for adding parents. To add functionalities for notes, referrals, concerns, allergies, birthmarks, legalGuardians


    private String kidUid;
    private OnFragmentInteractionListener listener;
    private final int REQUEST = 22;
    int LAUNCH = 23;
    private Uri path;
    private final String statusStr = "Kid Status -";
    FirebaseStorage storage;
    StorageReference storageReference;


    public interface OnFragmentInteractionListener {
        void onOkPressed(Kid newKid);

        void onOkPressed(Kid kid, String oldKidName);

        void onDeletePressed(Kid kid);

        void onOkPressed();
    }

    static AddKidFragment newInstance(Kid kid, User user) {
        Bundle args = new Bundle();
        args.putSerializable("Kid", kid);
        args.putSerializable("User", user);
        AddKidFragment fragment = new AddKidFragment();
        fragment.setArguments(args);
        return fragment;
    }

    static AddKidFragment newInstance(String uid){
        Bundle args = new Bundle();
        args.putSerializable("Uid",uid);
        AddKidFragment fragment = new AddKidFragment();
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
     * When a kid is selected, the edit fragment opens up
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
        kidAllergies = view.findViewById(R.id.kid_allergies);
        kidBirthmarks = view.findViewById(R.id.kid_birthmarks);
        kidPic = view.findViewById(R.id.kidPic);
        kidStatus = view.findViewById(R.id.kid_status_editText);
        kidDobButton = view.findViewById(R.id.select_date_kid);
        parentButton = view.findViewById(R.id.kid_parent);
        referralsButton = view.findViewById(R.id.kid_referrals);
        concernsButton = view.findViewById(R.id.kid_concerns);
        notesButton = view.findViewById(R.id.kid_notes);
        final ArrayList<String> validStatus = new ArrayList<String>();
        validStatus.add("Residential");
        validStatus.add("Out-Reach");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        Spinner spinner = view.findViewById(R.id.kid_status);
        final ArrayList<String> Statuses = new ArrayList<>();
        Statuses.add("Select Status:");
        Statuses.add("Residential");
        Statuses.add("Out-Reach");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout, Statuses);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(adapter);

        String title = "Add Kid";
        StorageReference imagesRef1 = storageReference.child("images/default.png"); //CHANGE THIS TO DISPLAY KID PICTURES
        imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUrl) {
                Glide
                        .with(getContext())
                        .load(downloadUrl.toString())
                        .centerCrop()
                        .into(kidPic);
            }
        });

        kidDobButton.setOnClickListener(new View.OnClickListener() {
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

        parentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectionFragment fragment = SelectionFragment.newInstance(kid);
                fragment.show(getFragmentManager(),"Add_Parent");
            }
        });


        notesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Notes.class);
                HashMap<String,Object> extras = new HashMap<>();
                extras.put("Kid",kid);
                extras.put("Flag","Notes");
                intent.putExtra(EXTRA_MESSAGE3, extras);   //Sending the current kid as a parameter to the Notes activity
                startActivity(intent);
            }
        });

        concernsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Notes.class);
                HashMap<String,Object> extras = new HashMap<>();
                extras.put("Kid",kid);
                extras.put("Flag","Concerns");
                intent.putExtra(EXTRA_MESSAGE3, extras);   //Sending the current kid as a parameter to the Notes activity
                startActivity(intent);
            }
        });

        referralsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), allUserProfiles.class);
                if (kid != null){
                    intent.putExtra("Kid", kid);
                }
                else{
                    intent.putExtra("Kid", new Kid());
                }
                   //Sending the current user as a parameter to the allUserProfiles activity

                startActivityForResult(intent,LAUNCH);
            }
        });

        DateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                String temp = i2 + "/" + i1 + "/" + i ;
                kidDOB.setText(temp);
            }
        };

        if (getArguments().get("Kid") != null) {
            kid  = (Kid) getArguments().get("Kid");
            title = "Edit Kid Details";

            kidFirstName.setText(kid.getFirstName());
            kidLastName.setText(kid.getLastName());
            kidMiddleName.setText(kid.getMiddleName());
            kidNationality.setText(kid.getNationality());
            kidHeight.setText(kid.getHeight()); //Convert to string
            kidDOB.setText(kid.getDOB());
            kidEyeColor.setText(kid.getEyeColor());
            kidHairColor.setText(kid.getHairColor());
            kidStatus.setText(kid.getStatus());
            kidAllergies.setText(kid.getAllergies());
            kidBirthmarks.setText(kid.getBirthmarks());
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference();
            if (kid.getUID()!=null){
                StorageReference imagesRef = storageReference.child("images/" + kid.getUID());
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrl) {
                        Glide
                                .with(getContext())
                                .load(downloadUrl.toString())
                                .centerCrop()
                                .into(kidPic);
                    }
                });
            }

        }
        else if (getArguments().get("Uid")!=null){
            kidUid = (String)getArguments().get("Uid");
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    if (getArguments().get("Kid") != null){
                        kidStatus.setText("Kid Status -" + kid.getStatus());
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
                if (getArguments().get("Kid") != null){
                    kidStatus.setText("Kid Status -" + kid.getStatus());
                }
                else{
                    kidStatus.setText(statusStr);
                }
            }
        });

        kidPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kidImageFragment fragment = kidImageFragment.newInstance(kid);
                fragment.show(getFragmentManager(), "Kid Profile Picture");


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
                        if (getArguments().get("Kid") != null) {
                            kid = (Kid) getArguments().get("Kid");
                            listener.onDeletePressed(kid);
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
                        String kid_allergies = kidAllergies.getText().toString();
                        String kid_birthmarks = kidBirthmarks.getText().toString();
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

                        } else if (getArguments().get("Kid") != null) {

                            Kid oldKid = (Kid) getArguments().get("Kid");
                            User user = (User) getArguments().get("User");

                            oldKid.setFirstName(first_name);
                            oldKid.setLastName(last_name);
                            oldKid.setMiddleName(middle_name);
                            oldKid.setDOB(dob);
                            oldKid.setEyeColor(eye_color);
                            oldKid.setHairColor(hair_color);
                            oldKid.setNationality(nationality);
                            oldKid.setHeight(height);
                            oldKid.setStatus(kid_status);
                            oldKid.setReferrals(kid.getReferrals());
                            String temp = kid.getFirstName();
                            kid.setStatus(kid_status.replace(statusStr,""));
                            listener.onOkPressed(kid, temp);
                            dialog.dismiss();
                        } else {
                            Kid temp = new Kid(first_name,last_name,middle_name,eye_color,dob,hair_color,kid_status,height,nationality,kid_allergies,kid_birthmarks);
                            if (kid != null){
                                temp.setReferrals(kid.getReferrals());
                                temp.setFather(kid.getFather());
                                temp.setMother(kid.getMother());
                            }
                            listener.onOkPressed(temp);//Send the inputted kid as a parameter to the main function's implementation of this method
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
                kidPic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == LAUNCH && resultCode == -1){
            kid = (Kid) data.getSerializableExtra("Kid");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#202F65"));
    }

    @Override
    public void onAddPressed(){
        //nothing yet
    }

    @Override
    public void onBackPressed(){
        //nothing yet
    }

}






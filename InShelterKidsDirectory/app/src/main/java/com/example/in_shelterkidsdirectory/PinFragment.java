package com.example.in_shelterkidsdirectory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.Serializable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PinFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PinFragment extends DialogFragment implements Serializable {

    private PinFragment.OnFragmentInteractionListener listener;
    EditText enterPin;
    static PinFragment newInstance(String userName,String password) {
        Bundle args = new Bundle();
        args.putSerializable("User", userName);
        args.putSerializable("Password",password);
        PinFragment fragment = new PinFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PinFragment.OnFragmentInteractionListener) {
            listener = (PinFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_pin, null);
        String title = "Enter Pin";
        enterPin = view.findViewById(R.id.pin);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle(title)
                .setNeutralButton("Cancel",null)
                .setPositiveButton(title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String pin = enterPin.getText().toString();
                        String userName = (String)getArguments().get("User");
                        String password = (String)getArguments().get("Password");
                        listener.onOkPressed(pin,userName,password);
                    }
                }).create();
    }


    public interface OnFragmentInteractionListener {
        void onOkPressed(String pin, String user, String pass);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#202F65"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#202F65"));
    }


}
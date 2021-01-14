package com.example.dlpbgj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class BookRequestsFragment extends DialogFragment implements Serializable {
    HashMap<String,String> req;
    ArrayList<String> users;
    private BookRequestsFragment.OnFragmentInteractionListener listener;
    private Book book;
    private String selection;

    static BookRequestsFragment newInstance(Book book) {
        Bundle args = new Bundle();
        args.putSerializable("Book", book);
        BookRequestsFragment fragment = new BookRequestsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookRequestsFragment.OnFragmentInteractionListener) {
            listener = (BookRequestsFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.book_requests_fragment, null);
        final Spinner spinner = view.findViewById(R.id.dropDown);
        String title = "Accept / Decline Requests";
        if (getArguments() != null) {
            users = new ArrayList<>();
            book = (Book) getArguments().get("Book");
            req = book.getRequests();
            Log.d("BookRequestFragment",String.valueOf(req));
            users.addAll(req.keySet());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, users);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selection = users.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selection = null;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle(title)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //book.setBorrower(selection);
                        //book.addRequest(selection,"Accepted");
                        for (String key : book.getRequests().keySet()){
                            if (key.equals(selection)){
                                book.addRequest(selection,"Accepted");
                            }
                            else{
                                book.addRequest(key,"Declined");
                            }
                        }
                        Toast toast = Toast.makeText(getContext(), selection + "'s request accepted!", Toast.LENGTH_SHORT);
                        toast.show();
                        listener.onAcceptPressed(book);
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //book.removeRequest(selection);
                        book.addRequest(selection,"Declined");
                        Toast toast = Toast.makeText(getContext(), selection + "'s request declined!", Toast.LENGTH_SHORT);
                        toast.show();
                        listener.onDeclinePressed(book);
                    }
                })
                .setNeutralButton("Cancel", null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#B59C34"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#B59C34"));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#B59C34"));
    }

    public interface OnFragmentInteractionListener {
        void onAcceptPressed(Book book);

        void onDeclinePressed(Book book);
    }


}
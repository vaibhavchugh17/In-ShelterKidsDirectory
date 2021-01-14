package com.example.dlpbgj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;

public class RequestBookFragment extends DialogFragment implements Serializable {
    private RequestBookFragment.OnFragmentInteractionListener listener;
    private Book book;
    private User user;

    static RequestBookFragment newInstance(Book book, User user) {
        Bundle args = new Bundle();
        args.putSerializable("Book", book);
        args.putSerializable("User", user);
        RequestBookFragment fragment = new RequestBookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RequestBookFragment.OnFragmentInteractionListener) {
            listener = (RequestBookFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.request_book_fragment, null);

        final TextView book_title = view.findViewById(R.id.book_title);
        final TextView book_author = view.findViewById(R.id.book_author);
        final TextView book_descr = view.findViewById(R.id.book_descr);
        final TextView book_ISBN = view.findViewById(R.id.book_ISBN);
        final TextView book_status = view.findViewById(R.id.book_status);
        final TextView book_owner = view.findViewById(R.id.book_owner);
        String title = "REQUEST BOOK";
        if (getArguments() != null) {
            book = (Book) getArguments().get("Book");
            user = (User) getArguments().get("User");
            book_title.setText("Book Title: " + book.getTitle());
            book_author.setText("Book Author: " + book.getAuthor());
            book_ISBN.setText("Book ISBN: " + book.getISBN());
            book_status.setText("Book Status: " + book.getStatus());
            book_descr.setText("Book Description: " + book.getDescription());
            book_owner.setText("Book Owner: " +book.getOwner());

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle(title)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("REQUEST", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onOkPressed(book, user);
                    }
                }).create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Do something
    }

    public interface OnFragmentInteractionListener {
        void onOkPressed(Book book, User user);
    }
}
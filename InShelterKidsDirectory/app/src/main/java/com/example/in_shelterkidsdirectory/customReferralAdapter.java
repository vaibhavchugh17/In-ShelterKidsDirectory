package com.example.in_shelterkidsdirectory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class customReferralAdapter extends ArrayAdapter<Parent> {

    private final ArrayList<Parent> referrals;
    private final Context context;


    public customReferralAdapter(Context context, ArrayList<Parent> referrals) {
        super(context, 0, referrals);
        this.referrals = referrals;
        this.context = context;
    }

    /**
     * Function to use our custom array adapter to show the different profiles of users.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.display_referral, parent, false); //Attaches layout from bookcontent to each item inside the ListView
        }

        Parent referral = referrals.get(position);

        TextView dispReferral = view.findViewById(R.id.textViewP);
        dispReferral.setText(referral.getFirstName());

    return view;

    }
}

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

public class customCodeAdapter extends ArrayAdapter<Code> {

    private ArrayList<Code> codes;
    private Context context;

    public customCodeAdapter(@NonNull Context context, ArrayList<Code> codes) {
        super(context,0,codes);
        this.codes = codes;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return super.getView(position, convertView, parent);
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.codecontent, parent,false);
        }

        Code code = codes.get(position);
        TextView codeNumView = view.findViewById(R.id.codeNumber);
        TextView codeValView = view.findViewById(R.id.codeValue);

        codeNumView.setText(code.getNum());
        codeValView.setText(code.getVal());

        return view;

    }
}

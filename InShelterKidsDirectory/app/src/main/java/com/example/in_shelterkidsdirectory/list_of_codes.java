package com.example.in_shelterkidsdirectory;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class list_of_codes extends AppCompatActivity {
    ListView codeList;
    ArrayAdapter<Code> codeAdapter;
    ArrayList<Code> codeDataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_codes);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("List of Codes");


        codeList = findViewById(R.id.codeList);

        String []nums = {"10202","10304","10407","10509","10697","10764","10832","10944","10109","10112"};
        String []values = {"Residential Admission Request", "First Contact", "Intake Assessment", "Case Conferencing / Management", "Direct Contact", "Indirect Contact", "Discharge Started", "Departure Completed", "Request for Outreach Services", "Outreach Initiation Completed"};

        codeDataList = new ArrayList<>();

        for(int i=0;i<nums.length;i++){
            codeDataList.add((new Code(nums[i], values[i])));
        }
        codeAdapter = new customCodeAdapter(this,codeDataList);
        codeList.setAdapter(codeAdapter);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
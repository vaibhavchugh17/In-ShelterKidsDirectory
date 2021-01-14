package com.example.dlpbgj;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ViewNotifications extends AppCompatActivity {
    ListView notificationList;
    ListView readNotificationList;
    private User currentUser;
    ArrayList<String> notifications;
    ArrayList<String> readNotifications;
    ArrayAdapter<String> notifAdapter;
    ArrayAdapter<String> readNotifAdapter;
    FirebaseFirestore db;
    FirebaseFirestore db2;
    FirebaseFirestore db3;
    CollectionReference userBookCollectionReference;
    CollectionReference userBookCollectionReference2;
    Button clear;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notifications);
        notificationList = findViewById(R.id.notification_listview);
        readNotificationList = findViewById(R.id.readNotification_listview);
        currentUser = (User) getIntent().getSerializableExtra("User");
        notifications = new ArrayList<>();
        readNotifications = new ArrayList<>();
        notifAdapter = new CustomNotificationAdapter(this, notifications);   //Implementing a custom adapter that connects the ListView with the ArrayList using bookcontent.xml layout
        readNotifAdapter = new CustomNotificationAdapter(this,readNotifications);
        notificationList.setAdapter(notifAdapter);
        clear = findViewById(R.id.clearButton);
        readNotificationList.setAdapter(readNotifAdapter);
        db = FirebaseFirestore.getInstance();
        userBookCollectionReference = db.collection("Users/" + currentUser.getUsername() + "/MyBooks");//Creating/pointing to a sub-collection of the books that user owns
        userBookCollectionReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Log.d("sucesss", doc.getId() + " => " + doc.getData());

                                HashMap<String,Long> allNotifications = (HashMap<String,Long>) doc.getData().get("Notifications");
                                HashMap<String,String> reqs = (HashMap<String,String>) doc.getData().get("Requests");
                                if(allNotifications!=null) {
                                    for (String key : reqs.keySet()) {
                                        if(!reqs.get(key).equals("Requested"))
                                            continue;
                                        long zero = 0;
                                        long one = 1;
                                        long temp = allNotifications.get(key);
                                        if(temp==1)
                                            readNotifications.add(key+" requested " + doc.getId());
                                        else if (temp == 0) {
                                            notifications.add(key + " requested " + doc.getId());
                                            allNotifications.put(key,one);
                                            HashMap<String,Object> data = new HashMap<>();
                                            data.put("Notifications",allNotifications);
                                            userBookCollectionReference
                                                    .document(doc.getId())
                                                    .update(data)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("ViewNotif","Notifications updated");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("ViewNotif","Failed to update Notifications");
                                                        }
                                                    });
                                        }
                                    }
                                }
                            } //For loop ends
                            notifAdapter.notifyDataSetChanged();
                            readNotifAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("fail", "Error getting documents: ", task.getException());
                        }
                    }
                });



        db2 = FirebaseFirestore.getInstance();
        userBookCollectionReference2 = db2.collection("Users/");//Creating/pointing to a sub-collection of the books that user owns
        userBookCollectionReference2
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                        if (task2.isSuccessful()) {
                            System.out.println("Starting NOW : ");
                            for (QueryDocumentSnapshot doc2 : task2.getResult()) {
                                Log.d("sucesss", doc2.getId() + " => " + doc2.getData());
                                System.out.println("User: "+doc2.getId());
                                if(doc2.getId().equals(currentUser.getUsername())){
                                    System.out.println("Skipping User: "+doc2.getId());
                                    continue;}
                                else{
                                    System.out.println("Running User: "+doc2.getId());
                                    db3 = FirebaseFirestore.getInstance();
                                    CollectionReference userBookCollectionReference3 = db.collection("Users/" + doc2.getId() + "/MyBooks");
                                    userBookCollectionReference3
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task3) {
                                                    if (task3.isSuccessful()) {
                                                        for (QueryDocumentSnapshot doc3 : task3.getResult()) {
                                                            Log.d("sucesss", doc3.getId() + " => " + doc3.getData());

                                                            System.out.println("Reading Book name : " + doc3.getId());
                                                            HashMap<String,Long> acceptNotifications= (HashMap<String,Long>) doc3.getData().get("Notifications");
                                                            HashMap<String,String> reqs = (HashMap<String,String>) doc3.getData().get("Requests");
                                                            if(acceptNotifications!=null) {
                                                                for (String key : reqs.keySet()) {
                                                                    if(!key.equals(currentUser.getUsername())){
                                                                        continue;
                                                                    }
                                                                    if(!(reqs.get(key).equals("Accepted")))
                                                                        continue;
                                                                    long zero = 0;
                                                                    long one = 1;
                                                                    long temp = acceptNotifications.get(key);
                                                                    if(temp==1)
                                                                        readNotifications.add(doc2.getId()+" accepted " + doc3.getId());
                                                                    else if (temp == 0) {
                                                                        notifications.add(doc2.getId() + " accepted " + doc3.getId());
                                                                        System.out.println(key + " accepted " + doc3.getId()+"******");
                                                                        acceptNotifications.put(key,one);
                                                                        System.out.println("FFF:"+key);
                                                                        System.out.println("NNN:"+acceptNotifications.get(key));
                                                                        HashMap<String,Object> data2 = new HashMap<>();
                                                                        data2.put("Notifications",acceptNotifications);
                                                                        System.out.println("***************************" + doc3.getId() +" Acceptor : " +doc2.getId() + "Req : " + currentUser.getUsername());
                                                                        userBookCollectionReference3
                                                                                .document(doc3.getId())
                                                                                .update(data2)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.d("ViewNotif","AcceptNotifications updated");
                                                                                        System.out.println("Database Updated");
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        Log.d("ViewNotif","Failed to update AcceptNotifications");
                                                                                    }
                                                                                });

                                                                        System.out.println("Book Added*********************");
                                                                    }
                                                                }
                                                            }
                                                        } //For loop ends
                                                        notifAdapter.notifyDataSetChanged();
                                                        readNotifAdapter.notifyDataSetChanged();
                                                    } else {
                                                        Log.d("fail", "Error getting documents: ", task3.getException());
                                                    }
                                                }
                                            });




                                }


                            } //For loop ends
                            notifAdapter.notifyDataSetChanged();
                            readNotifAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("fail", "Error getting documents: ", task2.getException());
                        }
                    }
                });

        notificationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), BookRequests.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);
            }
        });



        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readNotifications.clear();
                readNotifAdapter.notifyDataSetChanged();
            }
        });



    }
}
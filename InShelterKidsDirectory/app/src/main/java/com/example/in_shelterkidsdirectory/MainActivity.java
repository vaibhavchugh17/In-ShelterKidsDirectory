package com.example.in_shelterkidsdirectory;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE1 = "com.example.iskd.MESSAGE1";
    EditText user;
    EditText pass;
    Button login;
    Button signUp;
    String TAG = "MainActivity";

    /**
     * When app is launched.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = findViewById(R.id.editUserName);
        pass = findViewById(R.id.editUserPassword);
        login = findViewById(R.id.login_button);
        signUp = findViewById(R.id.signup_button);
        final String success = "Login Successful!";
        final String fail = "Invalid Login Details!";
        final String noExist = "Please Sign Up!";
        final String exist = "User already exists!";
        final String signUpS = "Successfully Signed Up!";
        final FirebaseFirestore userDb;

        //Instance of the User db
        userDb = FirebaseFirestore.getInstance();

        final CollectionReference arrayReference = userDb.collection("GlobalArray");
        DocumentReference docRef = arrayReference.document("Array"); //If username does not exist then prompt for a sign-up
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        HashMap<String, Object> value = new HashMap<>();
                        ArrayList<String> array = new ArrayList<>();
                        value.put("Array", array);
                        arrayReference
                                .document("Array")
                                .set(value)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Global Array Successfully Added");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Failed to Add Global Array");
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "Global Array get failed with ", task.getException());
                }
            }
        });

        final CollectionReference collectionReference = userDb.collection("Users");
        login.setOnClickListener(new View.OnClickListener() {
            /**
             * When user chooses to log in
             * Validates whether user with given credentials exists or not
             * If exists, it will launch the HomePage activity and pass the user as an argument.
             * If user does not exist, it prompts the user to sign up.
             * @param v
             */
            @Override
            public void onClick(final View v) {
                final String userName = user.getText().toString();
                final String userPass = pass.getText().toString();
                if (userName.length() != 0 && userPass.length() != 0) {
                    final User newUser = new User(userName, userPass);
                    DocumentReference docRef = collectionReference.document(userName); //If username does not exist then prompt for a sign-up
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Map<String, Object> data = document.getData();
                                    final String temp = (String) data.get("Password");
                                    if (Objects.equals(temp, userPass)) {

                                        Toast toast = Toast.makeText(v.getContext(), success, Toast.LENGTH_SHORT);
                                        toast.show();
                                        Intent intent = new Intent(getApplicationContext(), HomePage.class);
                                        intent.putExtra(EXTRA_MESSAGE1, newUser);
                                        startActivity(intent);

                                    } else {
                                        Toast toast = Toast.makeText(v.getContext(), fail, Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                } else {
                                    Toast toast = Toast.makeText(v.getContext(), noExist, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(v.getContext(), "Some details are missing", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            /**
             * When user chooses to signup
             * If a user with given credentials already exists, it prompts an error message
             * Otherwise, it creates a new user with given credentials.
             * @param v
             */
            @Override
            public void onClick(final View v) {
                final String userName = user.getText().toString();
                final String userPass = pass.getText().toString();
                if (userName.length() != 0 && userPass.length() != 0) {
                    final HashMap<String, Object> data = new HashMap<>();
                    data.put("Password", userPass);
                    DocumentReference docRef = collectionReference.document(userName);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Toast toast = Toast.makeText(v.getContext(), exist, Toast.LENGTH_SHORT);
                                    toast.show();
                                } else {
                                    collectionReference
                                            .document(userName)
                                            .set(data)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    /**
                                                     * If a new user is successfully registered to the database.
                                                     */
                                                    Log.d(TAG, "Data has been added succesfully");
                                                    Toast toast = Toast.makeText(v.getContext(), signUpS, Toast.LENGTH_SHORT);
                                                    toast.show();
                                                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                                                    intent.putExtra(EXTRA_MESSAGE1, new User(userName, userPass));
                                                    startActivity(intent);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "Data has been not been added");

                                                }
                                            });
                                }
                            }
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(v.getContext(), "Some details are missing!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        //for realtime database update
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable
                    FirebaseFirestoreException error) {
            }
        });

    }

}
package com.example.hherniiapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    //Strings
    EditText email, password, confirm_password, nombre, apellido, user_username, edad;
    Button registrarse;
    String photoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        nombre = findViewById(R.id.nombre);
        apellido = findViewById(R.id.apellido);
        edad = findViewById(R.id.edad);
        user_username = findViewById(R.id.username);
        registrarse = findViewById(R.id.registrarse);

        registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegistrarUsuario();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        //        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //if(currentUser != null){
        //    reload();
        //}
    }

    public void RegistrarUsuario() {
        if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty() ||
                nombre.getText().toString().isEmpty() ||
                apellido.getText().toString().isEmpty() ||
                edad.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Missing fields",
                    Toast.LENGTH_SHORT).show();
        } else {
            if (password.getText().toString().equals(confirm_password.getText().toString())) {
                mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // Proceed with the user data update only if user is not null
                                    if (user != null) {
                                        User data = new User(user_username.getText().toString(),
                                                nombre.getText().toString(),
                                                apellido.getText().toString(),
                                                edad.getText().toString(),
                                                null,
                                                email.getText().toString().trim()
                                        );

                                        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users");
                                        referenceProfile.child(user.getUid()).child("Informacion").setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Handle success if needed
                                                } else {
                                                    // Handle failure if needed
                                                }
                                            }
                                        });

                                        Toast.makeText(getApplicationContext(), "Registrado Exitosamente.", Toast.LENGTH_SHORT).show();

                                        Intent i = new Intent(getApplicationContext(), MenuActivity.class);
                                        startActivity(i);
                                    } else {
                                        Log.e(TAG, "FirebaseUser es nulo después del registro");
                                        // Handle the case where the user is null
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(getApplicationContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
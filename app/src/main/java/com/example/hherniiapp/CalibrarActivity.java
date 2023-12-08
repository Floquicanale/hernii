package com.example.hherniiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.BreakIterator;
import java.util.ArrayList;

public class CalibrarActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    Button calibracion;
    String a_SBP, a_DBP, b_DBP, b_SBP;
    Button cali1, cali2, cali3;
    EditText sistolica1, diastolica1, sistolica2, diastolica2, sistolica3, diastolica3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        calibracion = findViewById(R.id.calibracion);
        sistolica1 = findViewById(R.id.sistolica1);
        diastolica1 = findViewById(R.id.diastolica1);
        sistolica2 = findViewById(R.id.sistolica2);
        diastolica2 = findViewById(R.id.diastolica2);
        sistolica3 = findViewById(R.id.sistolica3);
        diastolica3 = findViewById(R.id.diastolica3);
        cali1 = findViewById(R.id.cali1);
        cali2 = findViewById(R.id.cali2);
        cali3 = findViewById(R.id.cali3);

        calibracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();

                Calibration data = new Calibration(a_SBP, b_SBP, a_DBP, b_DBP);


                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference();
                referenceProfile.child("Calibration").child(firebaseUser.getUid()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Calibración Exitosa!", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Calibration failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }


}
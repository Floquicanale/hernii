package com.example.hherniiapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
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
    EditText email, password, confirm_password, nombre, apellido, user_username, edad, DNI, obra_social, peso;
    Button registrarse;
    String photoUrl;
    private Uri uri;
    private String resourceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        resourceName = "logo_h_round"; // Cambia esto por el nombre de tu recurso sin extensión

        // Construye la Uri utilizando el esquema android.resource y el nombre del recurso
        uri = Uri.parse("android.resource://" + getPackageName() + "/drawable/" + resourceName);

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
        obra_social = findViewById(R.id.obra_social);
        DNI = findViewById(R.id.DNI);
        peso = findViewById(R.id.peso);

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

    // Supongamos que tienes el ID del recurso drawable
    //private int photo_H_default = R.drawable.logo_h_round;

    // Convierte el ID del recurso drawable a un URI
//    private Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
//            "://" + getResources().getResourcePackageName(photo_H_default)
//            + '/' + getResources().getResourceTypeName(photo_H_default)
//            + '/' + getResources().getResourceEntryName(photo_H_default));

    // Obtén el nombre del recurso (sin extensión) que está en res/drawable


// Ahora puedes utilizar la Uri en tu aplicación, por ejemplo, para establecer una imagen en un ImageView

    // Ahora, `imageUri` contiene el URI del drawable

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
                                        //Aca chequeo si no tienen datos se escribe "No especificado"
                                        verificarYEstablecerNoEspecificado(user_username);
                                        verificarYEstablecerNoEspecificado(DNI);
                                        verificarYEstablecerNoEspecificado(obra_social);
                                        verificarYEstablecerNoEspecificado(peso);

                                        User data = new User(user_username.getText().toString(),
                                                nombre.getText().toString(),
                                                apellido.getText().toString(),
                                                DNI.getText().toString(),
                                                obra_social.getText().toString(),
                                                peso.getText().toString(),
                                                edad.getText().toString(),
                                                uri.toString(),
                                                email.getText().toString().trim(),
                                                "No especificado",
                                                "No especificado",
                                                "No especificado",
                                                "No especificado"
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
    private void verificarYEstablecerNoEspecificado(EditText editText) {
        String valor = editText.getText().toString();
        if (valor.isEmpty()) {
            editText.setText("No especificado");
        }
    }


}
package com.example.hherniiapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;

public class SetProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    String photourl;
    Button save, choose_image;
    EditText name, surname, age, user_email, username;
    ImageView profile_pic;
    Uri imageUri;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        save = findViewById(R.id.save);
        choose_image = findViewById(R.id.choose_image);
        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);
        age = findViewById(R.id.age);
        profile_pic = findViewById(R.id.profile_pic);
        user_email = findViewById(R.id.user_email);
        username = findViewById(R.id.username);


        showUserData();

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        imageUri = data.getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(
                                    getApplicationContext().getContentResolver(), imageUri);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                    if (imageUri != null) {
                        profile_pic.setImageBitmap(bitmap);
                        profile_pic.setImageURI(imageUri);
                    }
                }
            }
        });

        choose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                launcher.launch(intent);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null) {
                    Toast.makeText(SetProfileActivity.this, "Selccione una imagen por favor", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    AlertDialog.Builder builder = new AlertDialog.Builder(SetProfileActivity.this);
                    builder.setCancelable(false);
                    builder.setView(R.layout.progress_layout);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    storage.getReference("Users").child(firebaseUser.getUid()).child("Informacion").putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isComplete()) ;
                            Uri urlImage = uriTask.getResult();
                            photourl = urlImage.toString();
                            uploadData();
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void showUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users");
        AlertDialog.Builder builder = new AlertDialog.Builder(SetProfileActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        referenceProfile.child(firebaseUser.getUid()).child("Informacion").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String usernameFromDB = dataSnapshot.child("username").getValue(String.class);
                    String nameFromDB = dataSnapshot.child("name").getValue(String.class);
                    String surnameFromDB = dataSnapshot.child("surname").getValue(String.class);
                    String genderFromDB = dataSnapshot.child("age").getValue(String.class);
                    String emailFromDB = dataSnapshot.child("user_email").getValue(String.class);
                    String photourlFromDB = dataSnapshot.child("photourl").getValue(String.class);

                    name.setText(nameFromDB);
                    surname.setText(surnameFromDB);
                    username.setText(usernameFromDB);
                    user_email.setText(emailFromDB);
                    age.setText(genderFromDB);
                    Picasso.get().load(photourlFromDB).into(profile_pic);

                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }


    private void uploadData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        User data = new User(username.getText().toString(),
                name.getText().toString(),
                surname.getText().toString(),
                age.getText().toString(),
                photourl,
                user_email.getText().toString().trim());

        if (name.getText().toString().isEmpty() ||
                surname.getText().toString().isEmpty() ||
                age.getText().toString().isEmpty() ||
                user_email.getText().toString().isEmpty() ||
                username.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Missing fields", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users");
            referenceProfile.child(firebaseUser.getUid()).child("Informacion").setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Perfil actualizado exitosamente!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), MenuActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "Falla en la actualización!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
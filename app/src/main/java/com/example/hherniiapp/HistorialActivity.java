package com.example.hherniiapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class HistorialActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1232;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    Button pdf_btn;
    private ListView fecha_list;
    private ListView SBP_list;
    private ListView DBP_list;
    private ListView HR_list;
    private TextView txt_SBP;
    private TextView txt_DBP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        pdf_btn = findViewById(R.id.pdf_btn);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        txt_SBP = findViewById(R.id.SBP_last);
        txt_DBP = findViewById(R.id.DBP_last);
        fecha_list = findViewById(R.id.fecha_list);
        SBP_list = findViewById(R.id.sbp_list);
        DBP_list = findViewById(R.id.dbp_list);
        HR_list = findViewById(R.id.hr_list);

        ArrayList<String> fecha_array = new ArrayList<>();
        ArrayAdapter adapter_fecha = new ArrayAdapter<String>(this, R.layout.list_items, fecha_array);
        fecha_list.setAdapter(adapter_fecha);
        ArrayList<String> sbp_array = new ArrayList<>();
        ArrayAdapter adapter_sbp = new ArrayAdapter<String>(this, R.layout.list_items, sbp_array);
        SBP_list.setAdapter(adapter_sbp);
        ArrayList<String> dbp_array = new ArrayList<>();
        ArrayAdapter adapter_dbp = new ArrayAdapter<String>(this, R.layout.list_items, dbp_array);
        DBP_list.setAdapter(adapter_dbp);
        ArrayList<String> hr_array = new ArrayList<>();
        ArrayAdapter adapter_hr = new ArrayAdapter<String>(this, R.layout.list_items, hr_array);
        HR_list.setAdapter(adapter_hr);


        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).child("Presiones");
        Query query = referenceProfile.orderByKey().limitToLast(3);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    sbp_array.clear();
                    dbp_array.clear();
                    hr_array.clear();
                    fecha_array.clear();
                    Log.d("Username de DB", "existe data snapshot");

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        fecha_array.add(snapshot.getKey().toString());
                        hr_array.add(snapshot.child("hr").getValue().toString());
                        sbp_array.add(snapshot.child("sbp").getValue().toString());
                        dbp_array.add(snapshot.child("dbp").getValue().toString());

                    }
                    Collections.reverse(sbp_array);
                    Collections.reverse(dbp_array);
                    Collections.reverse(hr_array);
                    Collections.reverse(fecha_array);

                    if (!sbp_array.isEmpty() && !dbp_array.isEmpty()) {
                        txt_SBP.setText(sbp_array.get(0).toString());
                        txt_DBP.setText(dbp_array.get(0).toString());
                    } else{
                        Toast.makeText(getApplicationContext(), "No se encontró última medición", Toast.LENGTH_SHORT).show();
                    }
                    adapter_sbp.notifyDataSetChanged();
                    adapter_dbp.notifyDataSetChanged();
                    adapter_hr.notifyDataSetChanged();
                    adapter_fecha.notifyDataSetChanged();
                }
                else{
                    Log.e("Snapshot","no existe el snapshot");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        pdf_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    performActionWithPermissions();
                } else {
                    requestPermission();
                }

            }
        });
    }
    private void performActionWithPermissions() {
        ArrayList<String> sbp_array = new ArrayList<>();
        ArrayList<String> hr_array = new ArrayList<>();
        ArrayList<String> dbp_array = new ArrayList<>();
        ArrayList<String> fecha_array = new ArrayList<>();

        final String[] username = new String[1];
        final String[] name = new String[1];
        final String[] surname = new String[1];
        final String[] edad = new String[1];
        final String[] email = new String[1];

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        DatabaseReference referenceProfile2 = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());

        referenceProfile2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("Username de DB", "existe data snapshot");

                    username[0] = dataSnapshot.child("Informacion").child("username").getValue().toString();
                    name[0] = dataSnapshot.child("Informacion").child("name").getValue().toString();
                    surname[0] = dataSnapshot.child("Informacion").child("surname").getValue().toString();
                    edad[0] = dataSnapshot.child("Informacion").child("age").getValue().toString();
                    email[0] = dataSnapshot.child("Informacion").child("user_email").getValue().toString();

                    for (DataSnapshot childSnapshot : dataSnapshot.child("Presiones").getChildren()) {
                        hr_array.add(childSnapshot.child("hr").getValue().toString());
                        sbp_array.add(childSnapshot.child("sbp").getValue().toString());
                        dbp_array.add(childSnapshot.child("dbp").getValue().toString());
                        fecha_array.add(childSnapshot.getKey().toString());
                    }


                    printPDF(username[0], name[0], surname[0], edad[0], email[0], hr_array, sbp_array, dbp_array, fecha_array);
                    System.out.println(name[0]);
                    //String presion= dataSnapshot.child("presion").getValue(String.class);
                    //String photourlFromD = dataSnapshot.child("photourl").getValue(String.class);


                }
                else{
                    Log.e("Snapshot","no existe el snapshot");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    // request permission for WRITE Access
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(HistorialActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(HistorialActivity.this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(HistorialActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Los permisos fueron otorgados, realizar la acción
                    performActionWithPermissions();
                } else {
                    // Los permisos no fueron otorgados, informar al usuario o tomar medidas alternativas
                    Toast.makeText(HistorialActivity.this, "No se otorgaron permisos", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void printPDF(String usuario, String nombre, String apellido, String edad, String email, ArrayList<String> HR_array, ArrayList<String> SBP_array, ArrayList<String> DBP_array, ArrayList<String> fecha_array) {
        System.out.println("Estoy en el printPDF");
        PdfDocument document = new PdfDocument();

        int viewWidth = 1080;
        int viewHeight = 1920;
        int y;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(viewWidth,viewHeight,1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint titulo = new Paint();
        titulo.setColor(Color.BLACK);
        titulo.setTextSize(41);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(21);

        canvas.drawText("Informe Médico de Presión", 300, 100 , titulo);
        canvas.drawText("Usuario: "+usuario, 100, 150 , paint);
        canvas.drawText("Paciente: "+nombre+" "+apellido, 100, 200 , paint);
        canvas.drawText("Edad: "+edad, 100, 250 , paint);
        canvas.drawText("Email: "+email, 100, 300 , paint);
        canvas.drawLine(100, 350, 1000, 350, paint);

        for (int i=0; i < HR_array.size();i++){
            y = 400 + 50*i;
            canvas.drawText("Fecha: "+fecha_array.get(i), 100, y , paint);
            canvas.drawText("HR: "+HR_array.get(i), 500, y , paint);
            canvas.drawText("SBP: "+SBP_array.get(i), 600, y , paint);
            canvas.drawText("DBP: "+DBP_array.get(i), 700, y , paint);
        }

        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hernii_landscape);
        if (originalBitmap != null) {
            // Dibujar una imagen en el canvas
            int newWidth = 500; // ajusta según tus necesidades
            int newHeight = 150;

            // Crear una matriz de transformación
            Matrix matrix = new Matrix();

            // Calcular la escala para ajustar las dimensiones
            float scaleX = (float) newWidth / originalBitmap.getWidth();
            float scaleY = (float) newHeight / originalBitmap.getHeight();

            // Aplicar la escala a la matriz
            matrix.postScale(scaleX, scaleY);

            // Crear una nueva imagen con las dimensiones escaladas
            Bitmap scaledBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

            canvas.drawBitmap(scaledBitmap, 300, 1700, null);
        } else {
            // Manejar el caso en que la carga de la imagen falla
            Log.e("Error", "Error al cargar la imagen desde los recursos");
        }

        document.finishPage(page);

        Context context = getApplicationContext();
        final File newFile = new File(Environment.getExternalStorageDirectory(),"PDFs");
        if(!newFile.exists())
        {
            newFile.mkdir();
        }
        File file = new File(context.getFilesDir(), "historial.pdf");
        System.out.println("File path: " + file.getAbsolutePath());
        FileOutputStream out = null;
        try {
            out = openFileOutput("historial.pdf", Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            document.writeTo(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        document.close();
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Uri path = FileProvider.getUriForFile(context,"com.example.dataintocsvformat",file);
        //once the file is ready a share option will pop up using which you can share
        // the same CSV from via Gmail or store in Google Drive
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Data");
        intent.putExtra(Intent.EXTRA_STREAM, path);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent,"PDF Data"));

    }


    private void showUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Users");

        referenceProfile.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("Username de DB", "existe data snapshot");

                    String usernameFromDB = dataSnapshot.child("username").getValue(String.class);
                    String nameFromDB = dataSnapshot.child("name").getValue(String.class);
                    String surnameFromDB = dataSnapshot.child("surname").getValue(String.class);
                    String genderFromDB = dataSnapshot.child("gender").getValue(String.class);
                    String emailFromDB = dataSnapshot.child("user_email").getValue(String.class);
                    String photourlFromDB = dataSnapshot.child("photourl").getValue(String.class);


                }
                else{
                    Log.e("Snapshot","no existe el snapshot");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }


}
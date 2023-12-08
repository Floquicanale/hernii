package com.example.hherniiapp;


import static java.security.AccessController.getContext;

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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;


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
    private TextView fijo_SBP;
    private TextView fijo_DBP;


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
        fijo_SBP = findViewById(R.id.SBP_txt);
        fijo_DBP = findViewById(R.id.DBP_txt);

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

        int rojardo = Color.parseColor("#D33333");


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
                        int DBP_actual = Integer.parseInt(dbp_array.get(0));
                        int SBP_actual = Integer.parseInt(sbp_array.get(0));
                        if (DBP_actual < 60 || DBP_actual > 85){
                            txt_DBP.setTextColor(rojardo);
                            fijo_DBP.setTextColor(rojardo);
                        }
                        if (SBP_actual < 80 || SBP_actual > 125){
                            txt_SBP.setTextColor(rojardo);
                            fijo_SBP.setTextColor(rojardo);
                        }
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
                    Collections.reverse(sbp_array);
                    Collections.reverse(dbp_array);
                    Collections.reverse(hr_array);
                    Collections.reverse(fecha_array);

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
    private static double calcularPromedio(ArrayList<String> lista) {
        if (lista.isEmpty()) {
            return 0.0;  // Manejar el caso de una lista vacía para evitar división por cero
        }

        double suma = 0.0;
        for (String valor : lista) {
            suma += Double.parseDouble(valor);
        }

        return suma / lista.size();
    }

    private Bitmap convertirChartABitmap(LineChart chart) {
        chart.measure(View.MeasureSpec.makeMeasureSpec(chart.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(chart.getHeight(), View.MeasureSpec.EXACTLY));
        chart.layout(0, 0, chart.getMeasuredWidth(), chart.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(chart.getWidth(), chart.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        chart.draw(canvas);
        return bitmap;
    }

    private void agregarlogo(Canvas canvas) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hernii_landscape);
        int newWidth = 500; // ajusta según tus necesidades
        int newHeight = 150;
        if (originalBitmap != null) {
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
        int currentPage = 1; // Contador de páginas

        Paint titulo = new Paint();
        titulo.setColor(Color.BLACK);
        titulo.setTextSize(41);

        Paint subtitulo = new Paint();
        subtitulo.setColor(Color.BLACK);
        subtitulo.setTextSize(30);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(21);

        Paint pain = new Paint();
        pain.setColor(Color.RED);
        pain.setTextSize(21);

        canvas.drawText("Informe Médico de Presión", 300, 100 , titulo);
        canvas.drawText("Resumen del estudio", 400, 170 , subtitulo);
        canvas.drawLine(100, 200, 1000, 200, paint);
        canvas.drawText("Datos de Paciente:", 100, 250 , subtitulo);
        canvas.drawText("Usuario: "+usuario, 100, 300 , paint);
        canvas.drawText("DNI: No especificado", 500, 300 , paint);
        canvas.drawText("Paciente: "+nombre+" "+apellido, 100, 350 , paint);
        canvas.drawText("Obra social: No especificado", 500, 350 , paint);
        canvas.drawText("Edad: "+edad, 100, 400 , paint);
        canvas.drawText("Peso: No especificado", 500, 400 , paint);
        canvas.drawText("Email: "+email, 100, 450 , paint);
        canvas.drawLine(100, 500, 1000, 500, paint);

        canvas.drawText("Datos del Estudio:", 100, 550 , subtitulo);
        canvas.drawText("Médico operador: No especificado", 100, 600 , paint);
        canvas.drawText("Médico derivador: No especificado", 500, 600 , paint);
        canvas.drawText("Institución: No especificado", 100, 650 , paint);
        canvas.drawText("Dirección: No especificado", 500, 650 , paint);
        canvas.drawText("Inicio del estudio: "+fecha_array.get(fecha_array.size() - 1), 100, 700 , paint);
        canvas.drawText("Final del estudio: "+fecha_array.get(0), 500, 700 , paint);
        canvas.drawLine(100, 750, 1000, 750, paint);

        //calculo los promedios para agregarlos en un resumen
        canvas.drawText("Mediciones:", 100, 800 , subtitulo);
        canvas.drawText("Total: "+fecha_array.size(), 100, 850 , paint);
        double DBP_prom = calcularPromedio(DBP_array);
        double SBP_prom = calcularPromedio(SBP_array);
        double HR_prom = calcularPromedio(HR_array);
        canvas.drawText("DBP Promedio: "+DBP_prom, 100, 900 , paint);
        canvas.drawText("SBP Promedio: "+SBP_prom, 100, 950 , paint);
        canvas.drawText("HR Promedio: "+HR_prom, 100, 1000 , paint);
        canvas.drawLine(100, 1050, 1000, 1050, paint);

        canvas.drawText("Aclaraciones:", 100, 1100 , subtitulo);
        canvas.drawText("Los valores se muestran en orden del más reciente al más antiguo.", 100, 1150 , paint);
        canvas.drawText("Los valores que se muestran en rojo indican valores fuera de lo normal para un paciente", 100, 1200 , paint);
        canvas.drawText("entre 20 y 60 años en reposo.", 100, 1250 , paint);


        //Le agrego el logo de Herni
        agregarlogo(canvas);
        document.finishPage(page); //termino la pagina actual
        currentPage++;
        PdfDocument.PageInfo newPageInfo = new PdfDocument.PageInfo.Builder(viewWidth, viewHeight, currentPage).create();
        page = document.startPage(newPageInfo); //nueva pagina
        canvas = page.getCanvas();
        canvas.drawText("Resultados del estudio", 400, 100 , subtitulo);
        int current_y = 150;

        for (int i=0; i < HR_array.size();i++){
            y = current_y + 50*i;

            int HR = Integer.parseInt(HR_array.get(i));
            int SBP = Integer.parseInt(SBP_array.get(i));
            int DBP = Integer.parseInt(DBP_array.get(i));

            canvas.drawText("Fecha: "+fecha_array.get(i), 100, y , paint);
            if (HR < 60 || HR > 100){
                canvas.drawText("HR: "+HR_array.get(i), 500, y , pain);
            }
            else {
                canvas.drawText("HR: "+HR_array.get(i), 500, y , paint);
            }
            if (SBP < 80 || SBP > 125){ //Si la presion esta matada la escribe en rojo
                canvas.drawText("SBP: "+SBP_array.get(i), 650, y , pain);
            }
            else{
                canvas.drawText("SBP: "+SBP_array.get(i), 650, y , paint);
            }
            if (DBP < 60 || DBP > 85){ //Si la presion esta matada la escribe en rojo
                canvas.drawText("DBP: "+DBP_array.get(i), 800, y , pain);
            }
            else{
                canvas.drawText("DBP: "+DBP_array.get(i), 800, y , paint);
            }

            //Si se pasa de la pagina creo una nueva
            if (y >= 1500){

                // Agrego el logo antes de terminar la pagina
                agregarlogo(canvas);

                document.finishPage(page); //termino la pagina actual
                currentPage++;

                PdfDocument.PageInfo nuevaPageInfo = new PdfDocument.PageInfo.Builder(viewWidth, viewHeight, currentPage).create();
                page = document.startPage(nuevaPageInfo); //nueva pagina
                canvas = page.getCanvas();
                current_y = 100;
            }

        }

        agregarlogo(canvas);
        document.finishPage(page);
        currentPage++;

        PdfDocument.PageInfo nuevaPageInfo = new PdfDocument.PageInfo.Builder(viewWidth, viewHeight, currentPage).create();
        page = document.startPage(nuevaPageInfo); // Nueva página
        canvas = page.getCanvas();

        canvas.drawText("Gráfico en el tiempo", 400, 100 , subtitulo);

        // Inflar un LinearLayout temporal sin ser visible
        LinearLayout tempLayout = new LinearLayout(this);
        tempLayout.setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight / 2));

        // Crear el LineChart
        LineChart chart = new LineChart(this);
        chart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        chart.getDescription().setEnabled(false); // Desactivar descripción

        // Configurar datos de muestra
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < HR_array.size(); i++) {
            entries.add(new Entry(i, Integer.parseInt(HR_array.get(i))));
        }

        // Agrego los valores de HR
        LineDataSet dataSet = new LineDataSet(entries, "HR");
        int rojo = Color.parseColor("#FC7F5D");
        dataSet.setColor(rojo);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Agrego los de DBP
        ArrayList<Entry> entries2 = new ArrayList<>();
        for (int i = 0; i < DBP_array.size(); i++) {
            entries2.add(new Entry(i, Integer.parseInt(DBP_array.get(i))));
        }
        LineDataSet dataSet2 = new LineDataSet(entries2, "DBP");
        int verde = Color.parseColor("#40C89B");
        dataSet2.setColor(verde);
        lineData.addDataSet(dataSet2);

        // Agrego los de SBP
        ArrayList<Entry> entries3 = new ArrayList<>();
        for (int i = 0; i < SBP_array.size(); i++) {
            entries3.add(new Entry(i, Integer.parseInt(SBP_array.get(i))));
        }
        LineDataSet dataSet3 = new LineDataSet(entries3, "SBP");
        int naranja = Color.parseColor("#FCC95D");
        dataSet3.setColor(naranja);
        lineData.addDataSet(dataSet3);

        // Medir el gráfico dentro del LinearLayout temporal
        tempLayout.addView(chart);
        tempLayout.measure(
                View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(viewHeight / 2, View.MeasureSpec.EXACTLY)
        );
        tempLayout.layout(0, 0, tempLayout.getMeasuredWidth(), tempLayout.getMeasuredHeight());

        // Convertir el gráfico en un Bitmap
        Bitmap chartBitmap = convertirChartABitmap(chart);
        // Crear una matriz de transformación
        Matrix matrix2 = new Matrix();

        // Calcular la escala para ajustar las dimensiones
        float escalaX = (float) 1000 / chartBitmap.getWidth();
        float escalaY = (float) 900 / chartBitmap.getHeight();

        // Aplicar la escala a la matriz
        matrix2.postScale(escalaX, escalaY);

        // Crear una nueva imagen con las dimensiones escaladas
        Bitmap finalBitmap = Bitmap.createBitmap(chartBitmap, 0, 0, chartBitmap.getWidth(), chartBitmap.getHeight(), matrix2, true);

        // Dibujar el Bitmap en el Canvas del PDF
        canvas.drawBitmap(finalBitmap, 50, 200, null);

        agregarlogo(canvas);

        // Finalizar la página
        document.finishPage(page);

        //ESTO PARA MANDAR POR MAIL
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
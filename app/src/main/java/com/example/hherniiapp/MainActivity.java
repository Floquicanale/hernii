package com.example.hherniiapp;

import static java.lang.Math.round;
import static kotlinx.coroutines.DelayKt.delay;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.io.File;

import java.math.RoundingMode;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.github.psambit9791.jdsp.filter.Butterworth;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {
    Button conectar, obtener_disp, desconectar, transmitir;
    Button calibracion;
    Button cali1, cali2, cali3;
    EditText sistolica1, diastolica1, sistolica2, diastolica2, sistolica3, diastolica3;
    BluetoothAdapter myBT;
    private Set<BluetoothDevice> pairedDevices;
    int Request_enable_BT;
    ListView lv;
    private BluetoothSocket BTSocket;
    private InputStream SocketInputStream;
    private OutputStream SocketOutputStream;
    private BluetoothDevice hc05;
    private UUID uuid;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    int count = 0;
    private float PTT_Cali1, PTT_Cali2, PTT_Cali3;
    private String a_SBP, b_SBP, a_DBP, b_DBP;
    ArrayList<Integer> datosArray = new ArrayList<>();
    ArrayList<Integer> ECG = new ArrayList<>(6000);
    ArrayList<Integer> SCG = new ArrayList<>(6000);
    ArrayList<Integer> PPG = new ArrayList<>(6000);
    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        conectar = findViewById(R.id.conectar);
        desconectar = findViewById(R.id.desconectar);
        transmitir = findViewById(R.id.transmitir);
        myBT = BluetoothAdapter.getDefaultAdapter();
        Request_enable_BT = 1;

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //Obtener variables para el Bluetooth Socket
        hc05 = myBT.getRemoteDevice("00:22:06:01:8D:20"); //obtenido en la consola mediante codigo "Prueba"
        System.out.println(hc05.getName()); //para verificar que se unio bien
        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //UUID default de los hc05

        conectar.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (!myBT.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0); //está tachado porq es viejo y se sugiere no usarlo
                    Toast.makeText(getBaseContext(), "Se inició el BT", Toast.LENGTH_SHORT).show();
                }

                try {
                    if (BTSocket != null) {
                        BTSocket.close(); // Cierra el socket Bluetooth
                    } else {
                        System.out.println("Creando el socket");
                        Toast.makeText(getApplicationContext(), "Conectando...", Toast.LENGTH_SHORT).show();
                        BTSocket = hc05.createRfcommSocketToServiceRecord(uuid);
                        BTSocket.connect();
                    }
                } catch(IOException e)
                {
                    Toast.makeText(getApplicationContext(), "Error al conectar Bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });

        desconectar.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view){
                myBT.disable();
                try {
                    BTSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(getApplicationContext(), "Se apagó el BT", Toast.LENGTH_SHORT).show();
            }
        });

        transmitir.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {

                if (BTSocket.isConnected()) {
                    try {
                        SocketInputStream = BTSocket.getInputStream();
                        SocketOutputStream = BTSocket.getOutputStream();
                        System.out.println("Entre al try");
                    } catch (IOException e) {
                        Log.e("Bluetooth", "Error al crear el socket Bluetooth", e);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(false);
                    builder.setView(R.layout.midiendo_layout);
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    Thread dataThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //InputStream socketInputStream = SocketInputStream; //Me parece que puedo borrar esta declaración
                            DataInputStream dataInputStream = new DataInputStream(SocketInputStream);
                            System.out.println("Estoy en el thread");
                            //BufferedReader reader = new BufferedReader(new InputStreamReader(SocketInputStream));
                            //Manda el caracter "1" a Arduino
                            String signal = "1"; // La señal que desea enviar
                            try {
                                SocketOutputStream.write(signal.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            while (true) { //Esto tiene que correr 120 veces para llenar 1 minuto xq de arduino vienen 100 datos por sensor
                                try {
                                    byte highByte = dataInputStream.readByte();
                                    if (count == 0 && highByte == 0) {
                                        highByte = dataInputStream.readByte();
                                    }
                                    byte lowByte = dataInputStream.readByte();
                                    //byte middleByte = dataInputStream.readByte();

                                    //| ((lowByte & 0xFF) << 16);
                                    int valorRecibido = (highByte & 0xFF) | ((lowByte & 0xFF) << 8);
                                    if ((lowByte & 0x80) != 0) {
                                        valorRecibido |= 0xFFFF0000; // Establecer bits adicionales a 1 para valores negativos
                                    }

                                    //System.out.println(" HighByte: "+highByte+"LowByte: "+lowByte);

                                    //short shortReceived = (short) ((highByte << 8) | (lowByte & 0xFF));
                                    //System.out.println(valorRecibido);
                                    datosArray.add(valorRecibido);

                                    count++;

                                    if (count == 18000) { //se toma medio segundo de data
                                        String signal2 = "0"; // La señal que desea enviar
                                        try {
                                            SocketOutputStream.write(signal2.getBytes());//se le pide al arduino que deje de enviar datos
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        System.out.println("YA ESTA");
                                        count = 0;
                                        break;
                                    }

                                    // Muestra en TextView

                                } catch (IOException e) {
                                    // Maneja las excepciones, por ejemplo, si la conexión se interrumpe
                                    Toast.makeText(getApplicationContext(), "No se pudo transmitir", Toast.LENGTH_SHORT).show();
                                }
                            }
                            //ECG = splitArray(datosArray, 0, 12000);
                            //SCG = splitArray(datosArray, 1, 12000);
                            //PPG = splitArray(datosArray, 2, 12000);
                            for (Integer valor : datosArray) {
                                // Los de ECG vienen con una marca, los de acelerometro son los unicos que pueden ser negativos, los de PPG estan entre esos valores
                                if (valor > -1000 && valor < 1000) {
                                    //System.out.println(valor);
                                    SCG.add(valor);
                                } else if (round((float) valor / 10000) == 2) {
                                    ECG.add(valor - 20000);
                                    //System.out.println(valor);
                                } else if (valor > -100000 && valor < -1000) {
                                    PPG.add(valor * -1);
                                    //System.out.println(valor);
                                }
                            }
                            /*
                            if (checkPermission()) {
                                CreateCSV(ECG);
                                CreateCSV(PPG);
                                CreateCSV(SCG);
                            } else {
                                requestPermission();
                                CreateCSV(ECG);
                                CreateCSV(PPG);
                                CreateCSV(SCG);
                            }
                             */


                            try {
                                float Final_PTT = PTT.CalcularPTT(ECG, PPG, SCG);

                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference().child("Users");
                                referenceProfile.child(firebaseUser.getUid()).child("Calibracion").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {

                                            a_SBP = dataSnapshot.child("a_sbp").getValue(String.class);
                                            b_SBP = dataSnapshot.child("b_sbp").getValue(String.class);
                                            a_DBP = dataSnapshot.child("a_dbp").getValue(String.class);
                                            b_DBP = dataSnapshot.child("b_dbp").getValue(String.class);

                                            double A_SBP = Double.parseDouble(a_SBP);
                                            double B_SBP = Double.parseDouble(b_SBP);
                                            double A_DBP = Double.parseDouble(a_DBP);
                                            double B_DBP = Double.parseDouble(b_DBP);

                                            System.out.println("a_SBP: " + A_SBP);
                                            System.out.println("b_SBP: " + B_SBP);
                                            System.out.println("a_DBP: " + A_DBP);
                                            System.out.println("b_DBP: " + B_DBP);


                                            //System.out.println(Arrays.toString(coefSBP)+" "+Arrays.toString(coefDBP));
                                            double PresionSBP = A_SBP / Final_PTT + B_SBP;
                                            double PresionDBP = A_DBP / Final_PTT + B_DBP;

                                            int PresionSBP_int = (int) Math.round(PresionSBP);
                                            int PresionDBP_int = (int) Math.round(PresionDBP);

                                            System.out.println("SBP :" + PresionSBP_int + "  DBP :" + PresionDBP_int);

                                            String SBP = Integer.toString(PresionSBP_int);
                                            String DBP = Integer.toString(PresionDBP_int);

                                            Calendar calendar = Calendar.getInstance();
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            String fecha = sdf.format(calendar.getTime());
                                            String HR = Integer.toString(PTT.HR(ECG));

                                            //Firebase Subir Datos
                                            DatabaseReference resultadosRef = referenceProfile.child(firebaseUser.getUid()).child("Presiones");
                                            Presiones data = new Presiones(SBP, DBP, HR);
                                            resultadosRef.child(fecha).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), "Carga de Presión exitosa!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "La carga de Presión falló!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        } else {
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });

                                /*
                                System.out.println("aaaa: "+a_SBP);
                                System.out.println(b_SBP);
                                System.out.println(a_DBP);
                                System.out.println(b_DBP);
    */

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Actualiza el TextView con los datos recibidos
                                        dialog.dismiss();
                                    }
                                });

                                // Ir a la pantalla de historial.
                                Intent i = new Intent(getApplicationContext(), HistorialActivity.class);
                                startActivity(i);


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    // Inicia el hilo secundario
                    dataThread.start();

                    //dialog.dismiss();
                }
                else{
                    Toast.makeText(getApplicationContext(), "El módulo Bluetooth no está conectado", Toast.LENGTH_SHORT).show();
                }
            }



        });

        cali1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setView(R.layout.midiendo_layout);
                AlertDialog dialog = builder.create();
                dialog.show();

                try {
                    SocketInputStream = BTSocket.getInputStream();
                    SocketOutputStream = BTSocket.getOutputStream();
                    System.out.println("Entre al try");
                } catch (IOException e) {
                    Log.e("Bluetooth", "Error al crear el socket Bluetooth", e);
                }

                Thread dataThread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //InputStream socketInputStream = SocketInputStream; //Me parece que puedo borrar esta declaración
                        DataInputStream dataInputStream = new DataInputStream(SocketInputStream);
                        System.out.println("Estoy en el thread");
                        //BufferedReader reader = new BufferedReader(new InputStreamReader(SocketInputStream));
                        //Manda el caracter "1" a Arduino
                        String signal = "1"; // La señal que desea enviar
                        try {
                            SocketOutputStream.write(signal.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        while (true) { //Esto tiene que correr 120 veces para llenar 1 minuto xq de arduino vienen 100 datos por sensor
                            try {
                                byte highByte = dataInputStream.readByte();
                                if (count == 0 && highByte == 0) {
                                    highByte = dataInputStream.readByte();
                                }
                                byte lowByte = dataInputStream.readByte();
                                //byte middleByte = dataInputStream.readByte();

                                //| ((lowByte & 0xFF) << 16);
                                int valorRecibido = (highByte & 0xFF) | ((lowByte & 0xFF) << 8);
                                if ((lowByte & 0x80) != 0) {
                                    valorRecibido |= 0xFFFF0000; // Establecer bits adicionales a 1 para valores negativos
                                }

                                //System.out.println(" HighByte: "+highByte+"LowByte: "+lowByte);

                                //short shortReceived = (short) ((highByte << 8) | (lowByte & 0xFF));
                                //System.out.println(valorRecibido);
                                datosArray.add(valorRecibido);

                                count++;

                                if (count == 18000) { //se toma medio segundo de data
                                    String signal2 = "0"; // La señal que desea enviar
                                    try {
                                        SocketOutputStream.write(signal2.getBytes());//se le pide al arduino que deje de enviar datos
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("YA ESTA");
                                    count = 0;
                                    break;
                                }

                                // Muestra en TextView
                            } catch (IOException e) {
                                // Maneja las excepciones, por ejemplo, si la conexión se interrumpe
                                Toast.makeText(getApplicationContext(), "No se pudo transmitir", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //ECG = splitArray(datosArray, 0, 12000);
                        //SCG = splitArray(datosArray, 1, 12000);
                        //PPG = splitArray(datosArray, 2, 12000);
                        for (Integer valor : datosArray) {
                            // Los de ECG vienen con una marca, los de acelerometro son los unicos que pueden ser negativos, los de PPG estan entre esos valores
                            if (valor > -1000 && valor < 1000) {
                                //System.out.println(valor);
                                SCG.add(valor);
                            } else if (round((float) valor / 10000) == 2) {
                                ECG.add(valor - 20000);
                                //System.out.println(valor);
                            } else if (valor > -100000 && valor < -1000) {
                                PPG.add(valor * -1);
                                //System.out.println(valor);
                            }
                        }

                        /*
                        if (checkPermission()) {
                            CreateCSV(ECG);
                            CreateCSV(PPG);
                            CreateCSV(SCG);
                        } else {
                            requestPermission();
                            CreateCSV(ECG);
                            CreateCSV(PPG);
                            CreateCSV(SCG);
                        }

                         */

                        try {
                            PTT_Cali1 = PTT.CalcularPTT(ECG, PPG, SCG);
                            System.out.println("PTT Cali 1: " + PTT_Cali1);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                // Inicia el hilo secundario
                dataThread1.start();
            }
        });

        cali2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setView(R.layout.midiendo_layout);
                AlertDialog dialog = builder.create();
                dialog.show();

                try {
                    SocketInputStream = BTSocket.getInputStream();
                    SocketOutputStream = BTSocket.getOutputStream();
                    System.out.println("Entre al try");
                } catch (IOException e) {
                    Log.e("Bluetooth", "Error al crear el socket Bluetooth", e);
                }

                Thread dataThread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //InputStream socketInputStream = SocketInputStream; //Me parece que puedo borrar esta declaración
                        DataInputStream dataInputStream = new DataInputStream(SocketInputStream);
                        System.out.println("Estoy en el thread");
                        //BufferedReader reader = new BufferedReader(new InputStreamReader(SocketInputStream));
                        //Manda el caracter "1" a Arduino
                        String signal = "1"; // La señal que desea enviar
                        try {
                            SocketOutputStream.write(signal.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        while (true) { //Esto tiene que correr 120 veces para llenar 1 minuto xq de arduino vienen 100 datos por sensor
                            try {
                                byte highByte = dataInputStream.readByte();
                                if (count == 0 && highByte == 0) {
                                    highByte = dataInputStream.readByte();
                                }
                                byte lowByte = dataInputStream.readByte();
                                //byte middleByte = dataInputStream.readByte();

                                //| ((lowByte & 0xFF) << 16);
                                int valorRecibido = (highByte & 0xFF) | ((lowByte & 0xFF) << 8);
                                if ((lowByte & 0x80) != 0) {
                                    valorRecibido |= 0xFFFF0000; // Establecer bits adicionales a 1 para valores negativos
                                }

                                //System.out.println(" HighByte: "+highByte+"LowByte: "+lowByte);

                                //short shortReceived = (short) ((highByte << 8) | (lowByte & 0xFF));
                                //System.out.println(valorRecibido);
                                datosArray.add(valorRecibido);

                                count++;

                                if (count == 18000) { //se toma medio segundo de data
                                    String signal2 = "0"; // La señal que desea enviar
                                    try {
                                        SocketOutputStream.write(signal2.getBytes());//se le pide al arduino que deje de enviar datos
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("YA ESTA");
                                    count = 0;
                                    break;
                                }

                                // Muestra en TextView
                            } catch (IOException e) {
                                // Maneja las excepciones, por ejemplo, si la conexión se interrumpe
                                Toast.makeText(getApplicationContext(), "No se pudo transmitir", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //ECG = splitArray(datosArray, 0, 12000);
                        //SCG = splitArray(datosArray, 1, 12000);
                        //PPG = splitArray(datosArray, 2, 12000);
                        for (Integer valor : datosArray) {
                            // Los de ECG vienen con una marca, los de acelerometro son los unicos que pueden ser negativos, los de PPG estan entre esos valores
                            if (valor > -1000 && valor < 1000) {
                                //System.out.println(valor);
                                SCG.add(valor);
                            } else if (round((float) valor / 10000) == 2) {
                                ECG.add(valor - 20000);
                                //System.out.println(valor);
                            } else if (valor > -100000 && valor < -1000) {
                                PPG.add(valor * -1);
                                //System.out.println(valor);
                            }
                        }

                        /*
                        if (checkPermission()) {
                            CreateCSV(ECG);
                            CreateCSV(PPG);
                            CreateCSV(SCG);
                        } else {
                            requestPermission();
                            CreateCSV(ECG);
                            CreateCSV(PPG);
                            CreateCSV(SCG);
                        }

                         */

                        try {
                            PTT_Cali2 = PTT.CalcularPTT(ECG, PPG, SCG);
                            System.out.println("PTT Cali 2: " + PTT_Cali2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                // Inicia el hilo secundario
                dataThread2.start();
            }
        });

        cali3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setView(R.layout.midiendo_layout);
                AlertDialog dialog = builder.create();
                dialog.show();

                try {
                    SocketInputStream = BTSocket.getInputStream();
                    SocketOutputStream = BTSocket.getOutputStream();
                    System.out.println("Entre al try");
                } catch (IOException e) {
                    Log.e("Bluetooth", "Error al crear el socket Bluetooth", e);
                }

                Thread dataThread3 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //InputStream socketInputStream = SocketInputStream; //Me parece que puedo borrar esta declaración
                        DataInputStream dataInputStream = new DataInputStream(SocketInputStream);
                        System.out.println("Estoy en el thread");
                        //BufferedReader reader = new BufferedReader(new InputStreamReader(SocketInputStream));
                        //Manda el caracter "1" a Arduino
                        String signal = "1"; // La señal que desea enviar
                        try {
                            SocketOutputStream.write(signal.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        while (true) { //Esto tiene que correr 120 veces para llenar 1 minuto xq de arduino vienen 100 datos por sensor
                            try {
                                byte highByte = dataInputStream.readByte();
                                if (count == 0 && highByte == 0) {
                                    highByte = dataInputStream.readByte();
                                }
                                byte lowByte = dataInputStream.readByte();
                                //byte middleByte = dataInputStream.readByte();

                                //| ((lowByte & 0xFF) << 16);
                                int valorRecibido = (highByte & 0xFF) | ((lowByte & 0xFF) << 8);
                                if ((lowByte & 0x80) != 0) {
                                    valorRecibido |= 0xFFFF0000; // Establecer bits adicionales a 1 para valores negativos
                                }

                                //System.out.println(" HighByte: "+highByte+"LowByte: "+lowByte);

                                //short shortReceived = (short) ((highByte << 8) | (lowByte & 0xFF));
                                //System.out.println(valorRecibido);
                                datosArray.add(valorRecibido);

                                count++;

                                if (count == 18000) { //se toma medio segundo de data
                                    String signal2 = "0"; // La señal que desea enviar
                                    try {
                                        SocketOutputStream.write(signal2.getBytes());//se le pide al arduino que deje de enviar datos
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("YA ESTA");
                                    count = 0;
                                    break;
                                }

                            } catch (IOException e) {
                                // Maneja las excepciones, por ejemplo, si la conexión se interrumpe
                                Toast.makeText(getApplicationContext(), "No se pudo transmitir", Toast.LENGTH_SHORT).show();
                            }
                        }
                        //ECG = splitArray(datosArray, 0, 12000);
                        //SCG = splitArray(datosArray, 1, 12000);
                        //PPG = splitArray(datosArray, 2, 12000);
                        for (Integer valor : datosArray) {
                            // Los de ECG vienen con una marca, los de acelerometro son los unicos que pueden ser negativos, los de PPG estan entre esos valores
                            if (valor > -1000 && valor < 1000) {
                                //System.out.println(valor);
                                SCG.add(valor);
                            } else if (round((float) valor / 10000) == 2) {
                                ECG.add(valor - 20000);
                                //System.out.println(valor);
                            } else if (valor > -100000 && valor < -1000) {
                                PPG.add(valor * -1);
                                //System.out.println(valor);
                            }
                        }

                        /*
                        if (checkPermission()) {
                            CreateCSV(ECG);
                            CreateCSV(PPG);
                            CreateCSV(SCG);
                        } else {
                            requestPermission();
                            CreateCSV(ECG);
                            CreateCSV(PPG);
                            CreateCSV(SCG);
                        }

                         */

                        try {
                            PTT_Cali3 = PTT.CalcularPTT(ECG, PPG, SCG);
                            System.out.println("PTT Cali 3: " + PTT_Cali3);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Muestra en TextView
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                // Inicia el hilo secundario
                dataThread3.start();
            }
        });

        calibracion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Traer los datos de la calibracion y de PTT

                double Sys1 = Double.parseDouble(sistolica1.getText().toString());
                double Sys2 = Double.parseDouble(sistolica2.getText().toString());
                double Sys3 = Double.parseDouble(sistolica3.getText().toString());
                double Dia1 = Double.parseDouble(diastolica1.getText().toString());
                double Dia2 = Double.parseDouble(diastolica2.getText().toString());
                double Dia3 = Double.parseDouble(diastolica3.getText().toString());

                // Datos de presión sistólica
                double[] SBP = { Sys1, Sys2, Sys3 };

                // Datos de presión diastólica
                double[] DBP = { Dia1, Dia2, Dia3 };

                // Datos de PTT
                double[] PTT_inv = { 1/PTT_Cali1, 1/PTT_Cali2, 1/PTT_Cali3 };

                double[] coefSBP = calcularRegresion(PTT_inv,SBP);
                double[] coefDBP = calcularRegresion(PTT_inv,DBP);

                String A_S = Double.toString(coefSBP[0]);
                String B_S = Double.toString(coefSBP[1]);
                String A_D = Double.toString(coefDBP[0]);
                String B_D = Double.toString(coefDBP[1]);

                FirebaseUser firebaseUser = mAuth.getCurrentUser();

                Calibration data = new Calibration(A_S, B_S, A_D, B_D);

                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference().child("Users");
                referenceProfile.child(firebaseUser.getUid()).child("Calibracion").setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
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


    //Funciones propias (estan fuera de OnCreate xq no puede tener ningún return)
    public static double[] calcularRegresion(double[] x, double[] y) {
        // Calcula la matriz de varianzas y covarianzas
        double[][] cov = new double[2][2];
        for (int i = 0; i < x.length; i++) {
            cov[0][0] += x[i] * x[i];
            cov[0][1] += x[i] * y[i];
            cov[1][0] += x[i] * y[i];
            cov[1][1] += y[i] * y[i];
        }

        // Calcula los coeficientes de la regresión
        double a = (cov[0][1] / cov[0][0]);
        double b = (cov[1][1] - a * cov[0][1]) / cov[1][0];

        // Devuelve los coeficientes
        return new double[] { a, b };

    }
    /*
    //CONVERSIONES DE TIPO DE DATO.
    public static double[] convertArrayListToDoubleArray(ArrayList<Integer> arrayList) {
        int size = arrayList.size();
        double[] doubleArray = new double[size];

        for (int i = 0; i < size; i++) {
            doubleArray[i] = arrayList.get(i);
        }

        return doubleArray;
    }
    private static ArrayList<Float> IntToFloat(ArrayList<Integer> intArray) {
        ArrayList<Float> floatList = new ArrayList<>();
        for (float value : intArray) {
            floatList.add(value);
        }
        return floatList;
    }
    private static ArrayList<Float> convertirDoubleArrayToArrayListFloat(double[] arrayDouble) {
        ArrayList<Float> listaBigDecimal = new ArrayList<>();
        for (double valor : arrayDouble) {
            float valorF = (float) valor ;
            listaBigDecimal.add(valorF);
        }
        return listaBigDecimal;
    }

    //FILTROS

    public static double[] HP_IIR (double[] signal, double lowF){
        int Fs = 200; //Sampling Frequency in Hz
        int order = 4;
        Butterworth flt = new Butterworth(Fs);
        double[] result = flt.highPassFilter(signal, order, lowF); //get the result after filtering

        return result;
    }


    //PROCESAMIENTO
    public static ArrayList<Float> restarPromedio(ArrayList<Float> datos){
        ArrayList<Float> salida = new ArrayList<>();
        float suma = 0;
        for (float dato : datos) {
            suma += dato;
        }
        // Calcular la media
        float media = suma/datos.size();

        for (float dato : datos) {
            float datoRestadoMedia = dato - media;
            salida.add(datoRestadoMedia);
        }

        return salida;
    }

    private static ArrayList<Float> FloatmultiplicarPorNegativo(ArrayList<Float> listaOriginal) {
        ArrayList<Float> resultado = new ArrayList<>();

        for (float elemento : listaOriginal) {
            resultado.add(elemento * -1);
        }

        return resultado;
    }

    public static ArrayList<Integer> find_peaks(ArrayList<Float> datos, float umbral) {
        ArrayList<Integer> picos = new ArrayList<>();
        ArrayList<Integer> salida = new ArrayList<>();

        for (int i = 1; i < datos.size()-1; i++) {
            if (datos.get(i) > umbral && (datos.get(i) - datos.get(i - 1) >= 0) && (datos.get(i) - datos.get(i + 1) >= 0)) {
                picos.add(i);
            }
        }

        int max = 0;
        for (int i = 0; i<picos.size()-1; i++){
            int dif = Math.abs(picos.get(i) - picos.get(i+1));
            if(dif<50){
                if(picos.get(i) - max > 0){
                    max = picos.get(i);
                }
            }
            else{
                if (max == 0) {
                    max = picos.get(i);
                }
                salida.add(max);
                max = 0;
            }

        }

        System.out.println("La salida de find_peaks: " + salida);
        return salida;
    }


    public static int find_peaks_SCG(ArrayList<Float> datos) {
        int save = 0;

        //System.out.println(datos);
        float max = 0;
        for (int i = 0; i < 40; i++) { //solo busca en los primero 40 valores porq si esta mas tarde full patologico
            if(datos.get(i) > max){
                max = datos.get(i);
                save=i;
            }


        }

        //System.out.println("La salida de find_peaks: " + picos);
        return save;
    }

    public static ArrayList<ArrayList<Float>> R_window (ArrayList<Float> data, ArrayList<Integer> R_peaks){
        ArrayList<ArrayList<Float>> segmentosList = new ArrayList<>();
        //ArrayList<ArrayList<Double>> Ventanas_recortadas = new ArrayList<>();
        System.out.println("Longitud del array:"+ data.size());
        int save_i = 0;
        int save_f = 0;
        for (int i = 0; i < R_peaks.size()-1; i++) {
            if (R_peaks.get(i+1) < data.size()){
                save_i = R_peaks.get(i);
                save_f = R_peaks.get(i+1);
                List<Float> subList = data.subList(save_i, save_f);
                // Convierte la sublista a un nuevo ArrayList si es necesario
                ArrayList<Float> ventana = new ArrayList<>(subList);

                //ventana = Arrays.copyOfRange(data,save_i,save_f);
                //System.out.println(ventana);
                segmentosList.add(ventana);
            }

        }

        return segmentosList;
    }

    //PPG - Foot
    public static float punto_mas_bajo(ArrayList<Float> ppgSignal){

        // Find the minimum point in the PPG signal
        int minIndex = findMinIndex(ppgSignal);

        // Find the maximum local gradient
        int maxGradientIndex = findMaxGradientIndex(ppgSignal);

        // Calculate the slope of the line tangent to the maximum local gradient
        float maxGradientSlope = calculateSlope(ppgSignal, maxGradientIndex);

        // Calculate the y-coordinate of the tangent line at the maximum gradient point
        float maxGradientY = ppgSignal.get(maxGradientIndex) - maxGradientSlope * maxGradientIndex;

        // Calculate the x-coordinate of the intersection point
        float intersectionX =  (ppgSignal.get(minIndex) - maxGradientY) / maxGradientSlope;

        // The intersection point is (intersectionX, ppgSignal[minIndex])
        //System.out.println("Intersection Point: (" + intersectionX + ", " + ppgSignal[minIndex] + ")");

        return intersectionX;
    }

    public static int findMinIndex(ArrayList<Float> data) {
        int minIndex = 0;
        for (int i = 40; i < data.size(); i++) {
            if (data.get(i) < data.get(minIndex)) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    // Find the index of the maximum local gradient
    public static int findMaxGradientIndex(ArrayList<Float> data) {
        int maxGradientIndex = 0;
        double maxGradient = 0;

        for (int i = 40; i < data.size() - 1; i++) {
            double gradient = (data.get(i + 1) - data.get(i - 1) );
            if (gradient > maxGradient) {
                maxGradient = gradient;
                maxGradientIndex = i;
            }
        }

        return maxGradientIndex;
    }

    // Calculate the slope of a line given two points
    public static float calculateSlope(ArrayList<Float> data, int index) {
        if (index > 0 && index < data.size() - 1) {
            float x1 = index - 1;
            float y1 = data.get(index - 1);
            float x2 = index + 1;
            float y2 = data.get(index + 1);
            return (y2 - y1) / (x2 - x1);
        } else {
            return 0; // Default slope if index is out of bounds
        }
    }

    public static ArrayList<Float> PTT (ArrayList<Integer> SCG, ArrayList<Integer> Foots) {
        ArrayList<Float> resta = new ArrayList<>();

        while (SCG.size() != Foots.size()) {
            if (Foots.size() > SCG.size()) {
                Foots.remove(Foots.size() - 1);
            }
            else{
                SCG.remove(SCG.size() - 1);
            }
        }

        for (int i = 0; i < SCG.size(); i++){
            float difference = (Foots.get(i).floatValue() - SCG.get(i).floatValue()) * 0.005f;
            if (difference < 0.65){
                resta.add(difference);
            }
        }
        //Sacamos los valores dudosos

        return resta;

    }

    public static ArrayList<Float> removeOutliersZScore(ArrayList<Float> data) {
        ArrayList<Float> filteredData = new ArrayList<>();

        // Calculate the mean and standard deviation
        float mean = calculateMean(data);
        float standardDeviation = calculateStandardDeviation(data, mean);

        // Remove outliers based on z-scores
        for (Float value : data) {
            float zScore = (value - mean) / standardDeviation;
            if (Math.abs(zScore) <= 2) {
                filteredData.add(value);
            }
        }

        return filteredData;
    }

    private static float calculateMean(ArrayList<Float> data) {
        float sum = 0;
        for (Float value : data) {
            sum += value;
        }
        return sum / data.size();
    }

    private static float calculateStandardDeviation(ArrayList<Float> data, float mean) {
        float sumOfSquares = 0;
        for (Float value : data) {
            float difference = value - mean;
            sumOfSquares += difference * difference;
        }
        return (float) Math.sqrt(sumOfSquares / (data.size() - 1));
    }

     */


    //CSV creacion
    private void CreateCSV(ArrayList <Integer> data) {
        Calendar calendar = Calendar.getInstance();
        long time= calendar.getTimeInMillis();
        try {
            //
            FileOutputStream out = openFileOutput("CSV_Data_"+time+".csv", Context.MODE_PRIVATE);


            //store the data in CSV file by passing String Builder data
            out.write("Datos \n".getBytes());
            for (Integer dt : data) {
                out.write(dt.toString().getBytes("UTF-8"));
                out.write("\n".getBytes());
            }
            out.close();
            Context context = getApplicationContext();
            final File newFile = new File(Environment.getExternalStorageDirectory(),"SimpleCVS");
            if(!newFile.exists())
            {
                newFile.mkdir();
            }
            File file = new File(context.getFilesDir(),"CSV_Data_"+time+".csv");
            Uri path = FileProvider.getUriForFile(context,"com.example.dataintocsvformat",file);
            //once the file is ready a share option will pop up using which you can share
            // the same CSV from via Gmail or store in Google Drive
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            intent.putExtra(Intent.EXTRA_STREAM, path);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent,"Excel Data"));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

}
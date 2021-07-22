package com.example.apptfg;

import Peticiones.CerrarSesion;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class PantallaInicioActivity extends AppCompatActivity {

    public String usuario;
    public String idSesion;
    public Socket s = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_inicio);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        usuario = intent.getStringExtra("USUARIO");
        idSesion = intent.getStringExtra("IDSESION");

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.usuario);
        textView.setText(usuario);

        textView = findViewById(R.id.idSesion);
        textView.setText(idSesion);

        try {
            s = new Socket("10.0.2.2",5000);
        } catch (UnknownHostException e) {
            System.out.println(e);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }
/*
    @Override
    protected void onDestroy (){

        try{
            BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
            DataOutputStream     dos = new DataOutputStream(bos);

            CerrarSesion peticion = new CerrarSesion(idSesion);
            peticion.aplanar(dos);

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        super.onDestroy();
    }
*/

    public void cerrarSesion(View view) {

        try{
            BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
            DataOutputStream     dos = new DataOutputStream(bos);

            CerrarSesion peticion = new CerrarSesion(idSesion);
            peticion.aplanar(dos);

            //Creamos el Intent
            Intent intent =  new Intent(PantallaInicioActivity.this, LoginActivity.class);
            //Iniciamos la nueva actividad
            startActivity(intent);

            finish();

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }

    public void historicoPuntuaciones(View view) {


        //Creamos el Intent
        Intent intent =  new Intent(PantallaInicioActivity.this, HistoricoPuntuacionesActivity.class);
        //Creamos la información a pasar entre actividades
        Bundle b = new Bundle();

        b.putString("IDSESION", idSesion);
        b.putString("USUARIO", usuario);

        //Añadimos la información al intent
        intent.putExtras(b);

        //Iniciamos la nueva actividad
        startActivity(intent);
    }

    public void inicioPartida(View view) {


        //Creamos el Intent
        Intent intent =  new Intent(PantallaInicioActivity.this, PartidaActivity.class);
        //Creamos la información a pasar entre actividades
        Bundle b = new Bundle();

        b.putString("IDSESION", idSesion);
        b.putString("USUARIO", usuario);

        //Añadimos la información al intent
        intent.putExtras(b);

        //Iniciamos la nueva actividad
        startActivity(intent);
    }

}
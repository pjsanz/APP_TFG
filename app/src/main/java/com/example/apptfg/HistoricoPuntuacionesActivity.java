package com.example.apptfg;

import Peticiones.HistoricoPuntuaciones;
import Peticiones.RespuestaHistoricoPuntuaciones;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class HistoricoPuntuacionesActivity extends AppCompatActivity {

    public String usuario;
    public String idSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_historico_puntuaciones);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        usuario = intent.getStringExtra("USUARIO");
        idSesion = intent.getStringExtra("IDSESION");

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.usuario);
        textView.setText(usuario);

        textView = findViewById(R.id.idSesion);
        textView.setText(idSesion);

        obtenerHistoricoPuntuaciones();

    }

    protected void obtenerHistoricoPuntuaciones(){

        try {

            Socket s = new Socket("10.0.2.2",5000);

            BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

            BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
            DataOutputStream dos = new DataOutputStream(bos);

            BufferedInputStream bis = new BufferedInputStream(s.getInputStream());
            DataInputStream dis = new DataInputStream(bis);

            HistoricoPuntuaciones peticion = new HistoricoPuntuaciones(idSesion);
            peticion.aplanar(dos);

            ObtenerRespuestaHistorico(dis);

        } catch (UnknownHostException e) {
            System.out.println(e);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private void ObtenerRespuestaHistorico(DataInputStream dis) throws IOException {

        byte [] bytes = new byte[4];
        int n = -1;

        n = dis.read(bytes);

        if (n!=-1){

            RespuestaHistoricoPuntuaciones respuestaPeticion = RespuestaHistoricoPuntuaciones.desaplanar(dis);

            String[] puntuaciones = respuestaPeticion.getPuntuaciones().split("@");

            TextView textView = findViewById(R.id.puntuaciones);

            StringBuilder sb = new StringBuilder();

            sb.append("Puntos | UsuarioColision | fecha \n");

            for (String puntos : puntuaciones) {
                sb.append(puntos + "\n");
            }

            textView.setText(sb.toString());

        }
    }

    public void volverInicio(View view) {

        finish();

    }
}
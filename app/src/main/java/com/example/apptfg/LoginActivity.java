package com.example.apptfg;

import Peticiones.Autenticacion;
import Peticiones.RespuestaAutenticacion;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {

        if(!comprobarCaracterEspecial()) {

            String idSesion = InicioSesion();

            if (!idSesion.equals("")) {

                //Creamos el Intent
                Intent intent = new Intent(LoginActivity.this, PantallaInicioActivity.class);
                //Creamos la información a pasar entre actividades
                Bundle b = new Bundle();
                b.putString("IDSESION", idSesion);

                final EditText txtNombre = (EditText) findViewById(R.id.txtUsuario);

                b.putString("USUARIO", txtNombre.getText().toString());

                //Añadimos la información al intent
                intent.putExtras(b);

                //Iniciamos la nueva actividad
                startActivity(intent);

                finish();
            }
        }
        else{

            final TextView lblError = (TextView)findViewById(R.id.lblErrorLogin);
            lblError.setVisibility(View.VISIBLE);
            lblError.setText("Error: El nombre de usuario no puede contener '&'");

        }

    }

    protected Boolean comprobarCaracterEspecial(){

        final EditText txtUsuario = (EditText)findViewById(R.id.txtUsuario);

        String usuario = txtUsuario.getText().toString();

        if(usuario.contains("&")){
            return true;
        }
        else{
            return false;
        }
    }

    protected String InicioSesion(){

        //Obtenemos una referencia a los controles de la interfaz
        final EditText txtNombre = (EditText)findViewById(R.id.txtUsuario);
        final EditText txtPassword = (EditText)findViewById(R.id.txtPassword);
        final TextView lblError = (TextView)findViewById(R.id.lblErrorLogin);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Realizamos la conexion con el servidor y realizamos la peticion de login
        //Si ha ido bien nos devolvera un id_sesion sino un mensaje de error

        Socket s = null;

        try {
            s = new Socket("10.0.2.2",5000);
        } catch (UnknownHostException e) {
            System.out.println(e);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        byte [] bytes;
        int n;
        String idSesion = "";

        try {

            BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));

            BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
            DataOutputStream dos = new DataOutputStream(bos);

            BufferedInputStream bis = new BufferedInputStream(s.getInputStream());
            DataInputStream dis = new DataInputStream(bis);

            Autenticacion peticion = new Autenticacion(txtNombre.getText().toString(), txtPassword.getText().toString());
            peticion.aplanar(dos);

            while(true){

                bytes = new byte[4];
                n = -1;

                n = dis.read(bytes);

                if (n!=-1){

                    lblError.setVisibility(View.VISIBLE);

                    RespuestaAutenticacion respuestaPeticion = RespuestaAutenticacion.desaplanar(dis);

                    if (respuestaPeticion.getRespuesta().equals("OK")){
                        idSesion = respuestaPeticion.getIdSesion();
                        lblError.setText("Contraseña correcta, sesión iniciada con éxito!");
                        break;
                    }
                    else if (respuestaPeticion.getRespuesta().equals("Registro")){
                        idSesion = respuestaPeticion.getIdSesion();
                        lblError.setText("Registro realizado con éxito!");
                        break;
                    }
                    else if (respuestaPeticion.getRespuesta().equals("KO")){
                        lblError.setText("Contraseña incorrecta, usuario ya existente!");
                        break;
                    }
                    else if (respuestaPeticion.getRespuesta().equals("Duplicado")){
                        lblError.setText("El usuario ya ha iniciado sesion!");
                        break;
                    }
                    else {
                        lblError.setText("Error al realizar la autenticación");
                        break;
                    }

                }

                s.close();

            }


        }catch(IndexOutOfBoundsException | IOException e) {} catch (Exception e) {
            e.printStackTrace();
        }

        return idSesion;
    }

}

package com.example.apptfg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PuntuacionColisionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puntuacion_colision);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.puntuacion);
        textView.setText(intent.getStringExtra("PUNTUACION"));

        textView = findViewById(R.id.usuarioCol);
        textView.setText(intent.getStringExtra("USUARIOCOLISION"));

    }


    public void volverInicio(View view) {

        finish();

    }
}
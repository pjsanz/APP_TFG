package com.example.apptfg;

import Entidades.Coordenadas;
import Entidades.DatosCliente;
import Entidades.LittleEndian;
import Entidades.TipoMensaje;
import Peticiones.Colision;
import Peticiones.EnvioCoordCliente;
import Peticiones.EnvioCoordServidor;
import Peticiones.InicioPartida;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.apptfg.databinding.ActivityPartidaBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class PartidaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityPartidaBinding binding;

    private LocationManager locManager;
    private LocationListener locListener;

    private Socket s;
    private DatosCliente misDatos;
    private ArrayList<DatosCliente> listaDatosRivales = new ArrayList<DatosCliente>();

    private String miLocalizacion = "";
    public String usuario;
    public String idSesion;
    public Boolean partidaIniciada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPartidaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        usuario = intent.getStringExtra("USUARIO");
        idSesion = intent.getStringExtra("IDSESION");

        misDatos = new DatosCliente(usuario);

        misDatos.setSesion(idSesion);

        partidaIniciada = false;

        try {
            s = new Socket("10.0.2.2", 5000);
        } catch (UnknownHostException e) {
            System.out.println(e);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        comenzarLocalizacion();
    }

    private void setUpMap(Location location) {

        if (location != null) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Mi posición de comienzo"));
            //Move the camera to the user's location and zoom in!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));

            DrawLine(location);
        }

    }

    private void DrawLine(Location location) {
        PolylineOptions linea1 = new PolylineOptions()
                .add(new LatLng(location.getLatitude(), location.getLongitude()))
                .add(new LatLng(location.getLatitude() + 0.02, location.getLongitude() + 0.02))
                .add(new LatLng(location.getLatitude() + 0.06, location.getLongitude() + 0.06));

        linea1.width(8);
        linea1.color(Color.RED);

        mMap.addPolyline(linea1);

        PolylineOptions linea2 = new PolylineOptions()
                .add(new LatLng(location.getLatitude() - 0.02, location.getLongitude() - 0.02))
                .add(new LatLng(location.getLatitude() - 0.02, location.getLongitude() - 0.02))
                .add(new LatLng(location.getLatitude() - 0.06, location.getLongitude() - 0.06));

        linea2.width(8);
        linea2.color(Color.BLUE);

        mMap.addPolyline(linea2);

    }

    private void DibujarEstela(String punto1, String punto2, Integer color, String usuario) {

        if(punto2.equals("")){
            punto2 = punto1;
        }

        Double latitud1 = Double.parseDouble(punto1.split(",")[0].toString());
        Double longitud1 = Double.parseDouble(punto1.split(",")[1].toString());

        Double latitud2 = Double.parseDouble(punto2.split(",")[0].toString());
        Double longitud2 = Double.parseDouble(punto2.split(",")[1].toString());

        PolylineOptions linea1 = new PolylineOptions()
                .add(new LatLng(latitud1, longitud1))
                .add(new LatLng(latitud1, longitud1))
                .add(new LatLng(latitud2, longitud2));

        linea1.width(8);
        linea1.color(color);

        mMap.addPolyline(linea1);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(latitud2, longitud2)).title(usuario));

    }

    private void comenzarLocalizacion() {

        //Obtenemos una referencia al LocationManager

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Obtenemos la última posición conocida

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        System.out.println(loc);

        //Mostramos la última posición conocida

        // mostrarPosicion(loc);

        //Nos registramos para recibir actualizaciones de la posición

        locListener = new LocationListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onLocationChanged(Location location) {
                if(!partidaIniciada){
                    try {
                        InicioPartida(location);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else{

                    try {
                        PartidaIniciada(location);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.0f));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            public void onProviderDisabled(String provider) {
                finish();
            }

            public void onProviderEnabled(String provider) {
                System.out.println("Provider On");
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("", "Provider Status: " + status);

            }
        };

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locListener);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void InicioPartida(Location location) throws IOException, InterruptedException {

        BufferedReader bf;

        Double latitudCoord = location.getLatitude();
        Double longitudCoord = location.getLongitude();

        DecimalFormat df = new DecimalFormat("#.0000");

        String coordenadasActuales = latitudCoord + "," +  longitudCoord;

        misDatos.insertarCoordenadas(coordenadasActuales);

        bf = new BufferedReader(new InputStreamReader(System.in));

        BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
        DataOutputStream dos = new DataOutputStream(bos);

        BufferedInputStream bis = new BufferedInputStream(this.s.getInputStream());
        DataInputStream dis = new DataInputStream(bis);

        InicioPartida peticion = new InicioPartida(misDatos.getSesion(), coordenadasActuales);
        peticion.aplanar(dos);

        ObtenerRespuestaInicio(dis, dos);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void ObtenerRespuestaInicio(DataInputStream dis, DataOutputStream dos) throws IOException, InterruptedException {

        //Aqui tengo que recibir las coordenadas por parte del servidor y enviar las primeras mias

        byte [] bytes = new byte[4];
        int n = -1;

        n = dis.read(bytes);

        if (n!=-1){

            EnvioCoordServidor respuestaPeticion = EnvioCoordServidor.desaplanar(dis);

            System.err.println(respuestaPeticion.getUsuarios());
            System.err.println(respuestaPeticion.getCoordenadas());

            //Parte de coordenadas sustituir en app por las coordenadas actuales

            InsertarCoordenadasUsuariosListaRivales(respuestaPeticion);

            //Hemos iniciado el juego

            partidaIniciada = true;

            //Pintamos nuestra coordenada enviada

        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void ObtenerRespuestaServidor(DataInputStream dis, DataOutputStream dos) throws IOException, InterruptedException {

        //Aqui tengo que recibir las coordenadas por parte del servidor y enviar las primeras mias

        byte [] bytes = new byte[4];
        int n = -1;

        n = dis.read(bytes);

        if (n!=-1){

            TipoMensaje tipo = TipoMensaje.values()[LittleEndian.desempaquetar(bytes)];

            switch (tipo) {

                case EnvioCoordServidor:

                    EnvioCoordServidor respuestaPeticion2 = EnvioCoordServidor.desaplanar(dis);
                    System.err.println(respuestaPeticion2.getUsuarios());
                    System.err.println(respuestaPeticion2.getCoordenadas());

                    InsertarCoordenadasUsuariosListaRivales(respuestaPeticion2);

                    partidaIniciada = true;

                    break;

                case Colision:

                    Colision peticionColision = Colision.desaplanar(dis);

                    System.err.println("Has colisionado con la estela de: " + peticionColision.getUsuarioColision());
                    System.err.println("Has obtenido una puntuacion de: " + peticionColision.getPuntuacion());

                    //Reiniciamos los parametros del juego para el cliente

                    listaDatosRivales = new ArrayList<DatosCliente>();
                    misDatos.setCoordenadas(new ArrayList<Coordenadas>());

                    //Creamos el Intent
                    Intent intent = new Intent(PartidaActivity.this, PuntuacionColisionActivity.class);
                    //Creamos la información a pasar entre actividades
                    Bundle b = new Bundle();

                    b.putString("USUARIOCOLISION", peticionColision.getUsuarioColision());
                    b.putString("PUNTUACION", peticionColision.getPuntuacion());

                    //Añadimos la información al intent
                    intent.putExtras(b);

                    //Iniciamos la nueva actividad
                    startActivity(intent);

                    locManager.removeUpdates(locListener);

                    if (locListener != null) {
                        locListener = null;
                    }
                    if (locManager != null) {
                        locManager = null;
                    }
                    partidaIniciada = false;

                    finish();

                    break;

                default:
                    break;
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void PartidaIniciada(Location location) throws InterruptedException, IOException {

        BufferedReader bf;

        String coordenadasActuales = location.getLatitude() + "," +  location.getLongitude();

        ArrayList<Coordenadas> MisCoordenadas = misDatos.getCoordenadas();

        Coordenadas ultimaCoord = MisCoordenadas.get(MisCoordenadas.size() - 1);

        String ultimaCoordenadas = ultimaCoord.getLatitud() + "," + ultimaCoord.getLongitud();

        //DrawLine(location);

        DibujarEstela(coordenadasActuales,ultimaCoordenadas, misDatos.getColor(), misDatos.getUsuario());

        misDatos.insertarCoordenadas(coordenadasActuales);

        bf = new BufferedReader(new InputStreamReader(System.in));

        BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
        DataOutputStream dos = new DataOutputStream(bos);

        BufferedInputStream bis = new BufferedInputStream(this.s.getInputStream());
        DataInputStream dis = new DataInputStream(bis);

        EnvioCoordCliente peticion = new EnvioCoordCliente(misDatos.getSesion(), coordenadasActuales);
        peticion.aplanar(dos);

        ObtenerRespuestaServidor(dis, dos);

    }

    private void EliminarUsuarioLista(String[] usuarios) {
        for (String usuario : usuarios) {
            if(!EncontrarUsuarioLista(usuarios, usuario)) {
                EliminarRival(usuario);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void InsertarCoordenadasUsuariosListaRivales(EnvioCoordServidor respuestaPeticion) {

        try {

            //Descompongo y las anado a la lista de usuarios si esta esta vacia quiere decir
            //que he recibido un primer mensaje del servidor con los usuarios activos

            //Anadimos usuarios menos nosotros mismos!

            DatosCliente datos = null;

            String[] usuarios = respuestaPeticion.getUsuarios().split("&");
            String[] coordenadas = respuestaPeticion.getCoordenadas().split("@");

            Boolean existeUsuario = false;
            int     indiceUsuario = 0;

            for(int i = 0; i < usuarios.length; i++) {

                if(!usuarios[i].equals(misDatos.getUsuario())) {

                    //Comprobamos si el usuario ya estaba anadido para no anadirle de nuevo
                    //Solamente introducimos su ultima coordenada

                    for (DatosCliente datosClienteUsu : listaDatosRivales) {
                        if(datosClienteUsu.getUsuario().equals(usuarios[i])) {
                            existeUsuario = true;
                            indiceUsuario = listaDatosRivales.indexOf(datosClienteUsu);
                            break;
                        }
                    }

                    if(!existeUsuario) {
                        datos = new DatosCliente(usuarios[i]);
                        datos.insertarCoordenadas(coordenadas[i]);
                        listaDatosRivales.add(datos);
                    }
                    else {
                        datos = listaDatosRivales.get(indiceUsuario);
                        datos.insertarCoordenadas(coordenadas[i]);

                        listaDatosRivales.set(indiceUsuario, datos);
                    }

                }

                System.out.println(usuario + " --------------->" + listaDatosRivales.size());

                //Eliminamos los usuarios de la lista por si alguno ha desaparecido ya no es rival

                EliminarUsuarioLista(usuarios);

                //Dibujamos sus coordenadas

                System.out.println(usuario +  "--------------->" + listaDatosRivales.size());

                if(listaDatosRivales.size() > 0){
                    ArrayList<Coordenadas> Coordenadas;
                    Coordenadas ultimaCoord;
                    Coordenadas actualCoord;


                    for (DatosCliente datosClienteUsu : listaDatosRivales) {

                        Coordenadas = datosClienteUsu.getCoordenadas();

                        ultimaCoord = Coordenadas.get(Coordenadas.size() - 2);
                        actualCoord = Coordenadas.get(Coordenadas.size() - 1);

                        DibujarEstela(actualCoord.getLatitud() + "," + actualCoord.getLongitud(),
                                ultimaCoord.getLatitud() + "," + ultimaCoord.getLongitud(), datosClienteUsu.getColor(), datosClienteUsu.getUsuario());
                    }
                }
            }

            //PINTAMOS COORDENADAS EN EL MAPA EN LA APP DE ANDROID las coordenadas de los usuarios que me llegan
        }
        catch(IndexOutOfBoundsException e) {

        }
    }

    private boolean EncontrarUsuarioLista(String[] usuarios, String usuario) {

        boolean retorno = false;

        for(String usu : usuarios) {
            if(usu.equals(usuario)) {
                retorno = true;
                break;
            }
        }

        return retorno;

    }

    private void EliminarRival(String usuario) {

        int indiceUsuario = 0;

        for (DatosCliente datosClienteUsu : listaDatosRivales) {
            if(datosClienteUsu.getUsuario().equals(usuario)) {
                indiceUsuario = listaDatosRivales.indexOf(datosClienteUsu);
                break;
            }
        }

        listaDatosRivales.remove(indiceUsuario);

    }

}
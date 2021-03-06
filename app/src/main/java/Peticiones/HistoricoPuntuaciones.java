package Peticiones;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Entidades.LittleEndian;
import Entidades.TipoMensaje;

public class HistoricoPuntuaciones {

    private String idSesion;
    private TipoMensaje    identificadorPeticion;

    public HistoricoPuntuaciones(String idSesion){

        this.idSesion			   = idSesion;
        this.identificadorPeticion = TipoMensaje.HistoricoPuntuaciones;

    }

    public void aplanar(OutputStream salida){

        int longitud;

        try {

            int tipoOrdinal = this.identificadorPeticion.ordinal();
            salida.write(LittleEndian.empaquetar(tipoOrdinal));

            longitud = this.idSesion.getBytes("UTF-8").length;
            salida.write(LittleEndian.empaquetar(longitud));
            salida.write(this.idSesion.getBytes("UTF-8"));

            salida.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static HistoricoPuntuaciones desaplanar(InputStream entrada){

        HistoricoPuntuaciones peticion = null;
        int longitud;

        try {

            byte[] tamanoBytes = new byte[4];
            entrada.read(tamanoBytes);
            longitud = LittleEndian.desempaquetar(tamanoBytes);

            byte[] peticionBytes = new byte[longitud];
            entrada.read(peticionBytes);
            String idSesion = new String(peticionBytes, "UTF-8");

            peticion = new HistoricoPuntuaciones(idSesion);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return peticion;
    }

    public String getIdSesion() {
        return this.idSesion;
    }

}

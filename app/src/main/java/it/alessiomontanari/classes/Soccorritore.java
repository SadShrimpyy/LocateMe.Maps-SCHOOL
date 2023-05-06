package it.alessiomontanari.classes;

import com.google.android.gms.maps.model.LatLng;

public class Soccorritore {

    private int matricola;
    private String codiceSoccorso;
    private String username;
    private LatLng position;

    public Soccorritore(int uuid, String username, String code, LatLng latLng) {
        this.matricola = uuid;
        this.codiceSoccorso = code;
        this.username = username;
        this.position = latLng;
    }


    // UUID
    public int getMatricola() {
        return matricola;
    }

    public void setMatricola(int matricola) {
        this.matricola = matricola;
    }

    // Codice
    public String getCodiceSoccorso() {
        return codiceSoccorso;
    }

    public void setCodiceSoccorso(String codiceSoccorso) {
        this.codiceSoccorso = codiceSoccorso;
    }

    // Posizione
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    // Nome utente
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStrMatricola() {
        return String.valueOf(matricola);
    }
}

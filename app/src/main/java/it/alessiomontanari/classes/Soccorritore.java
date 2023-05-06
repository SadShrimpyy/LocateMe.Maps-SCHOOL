package it.alessiomontanari.classes;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

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

    public Soccorritore() {
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

    public Soccorritore objIntoNew(Map<String, Object> data, Soccorritore s) {
        s.setMatricola(((Long) data.get("matricola")).intValue());
        s.setCodiceSoccorso((String) data.get("codiceSoccorso"));
        s.setUsername((String) data.get("username"));

        HashMap<String, Double> positionData = (HashMap<String, Double>) data.get("position");
        double latitude = positionData.get("latitude");
        double longitude = positionData.get("longitude");
        s.setPosition(new LatLng(latitude, longitude));

        return s;
    }
}

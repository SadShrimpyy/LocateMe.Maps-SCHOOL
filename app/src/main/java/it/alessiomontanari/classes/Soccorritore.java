package it.alessiomontanari.classes;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public double getLat() {
        if (position != null)
            return position.latitude;
        else
            return 0.0;
    }

    public double getLon() {
        if (position != null)
            return position.longitude;
        else
            return 0.0;
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

    /** Passata una HashMap di oggetti generici e un soccorritore, ritorno l'oggetto soccorritore con i propri valori */
    public Soccorritore objIntoNew(Map<String, Object> data, Soccorritore s) {
        s.setMatricola(((Long) Objects.requireNonNull(data.get("matricola"))).intValue());
        s.setCodiceSoccorso((String) data.get("codiceSoccorso"));
        s.setUsername((String) data.get("username"));

        HashMap<String, Double> positionData = (HashMap<String, Double>) data.get("position");
        double latitude = 0.0;
        double longitude = 0.0;
        if (positionData != null) {
            latitude = Objects.requireNonNull(positionData.get("latitude")).doubleValue();
            longitude = Objects.requireNonNull(positionData.get("longitude")).doubleValue();
        }
        s.setPosition(new LatLng(latitude, longitude));

        return s;
    }
}

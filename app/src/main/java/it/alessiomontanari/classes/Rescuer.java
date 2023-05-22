package it.alessiomontanari.classes;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Rescuer {

    private int serialNumber; // Identificatore Univoco
    private String rescueCode;
    private String username;
    private LatLng position;

    public Rescuer(int serialNumber, String username, String rescudeCode, LatLng latLng) {
        this.serialNumber = serialNumber;
        this.rescueCode = rescudeCode;
        this.username = username;
        this.position = latLng;
    }

    public Rescuer() {
    }


    // UUID
    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    // Codice
    public String getRescueCode() {
        return rescueCode;
    }

    public void setRescueCode(String rescueCode) {
        this.rescueCode = rescueCode;
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
        return String.valueOf(serialNumber);
    }

    /** Passata una HashMap di oggetti generici e un soccorritore, ritorno l'oggetto soccorritore con i propri valori */
    public Rescuer objIntoNew(Map<String, Object> data, Rescuer s) {
        s.setSerialNumber(((Long) Objects.requireNonNull(data.get("matricola"))).intValue());
        s.setRescueCode((String) data.get("codiceSoccorso"));
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

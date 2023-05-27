package it.alessiomontanari.classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExtendedMarker {

    private MarkerOptions marker = new MarkerOptions();
    private String note = null;

    // Default della classe marker
    public void setPosition(LatLng latLng) {
        this.marker.position(latLng);
    }

    public void setTitle(String title) {
        this.marker.title(title);
    }
    public String getTitle() {
        return this.marker.getTitle();
    }

    // Personalizzati
    public MarkerOptions getMarker() {
        return this.marker;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public String getNote() {
        return this.note;
    }

    /** Passata una HashMap di oggetti generici e un marcatore, ritorno l'oggetto marcatore con i propri valori */
    public ExtendedMarker objIntoNew(Map<String, Object> data, ExtendedMarker m) {
        m.setTitle((String) data.get("title"));
        m.setNote((String) data.get("note"));

        HashMap<String, Double> positionData = (HashMap<String, Double>) data.get("position");
        double latitude = 0.0;
        double longitude = 0.0;
        if (positionData != null) {
            latitude = Objects.requireNonNull(positionData.get("latitude")).doubleValue();
            longitude = Objects.requireNonNull(positionData.get("longitude")).doubleValue();
        }
        m.setPosition(new LatLng(latitude, longitude));

        return m;
    }
}

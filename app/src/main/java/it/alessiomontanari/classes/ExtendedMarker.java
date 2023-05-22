package it.alessiomontanari.classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
}

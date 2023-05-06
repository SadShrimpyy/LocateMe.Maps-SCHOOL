package it.alessiomontanari.classes;

import static it.alessiomontanari.MapsActivity.currentPosName;
import static it.alessiomontanari.MapsActivity.markerList;

import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;

public class Listeners {

    private Toast toast;

    public Listeners(Toast toast) {
        this.toast = toast;
    }
    public void clickMarker(GoogleMap mMap) {
        ArrayList<ExtendedMarker> markersCopy = markerList;

        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) marker -> {
            if (markerList.size() <= 0)
                return false;

            for (ExtendedMarker obj : markersCopy) {
                if (Objects.requireNonNull(obj.getMarker().getTitle()).equalsIgnoreCase(currentPosName)) {
                    showToast(toast, "Impossibile rimuovere la Posizione Corrente");
                } else {
                    markersCopy.remove(obj);
                    marker.remove();
                    showToast(toast, "Marcatore " + marker.getTitle() + " rimosso");
                }
            }
            return true;
        });
    }

    private void showToast(Toast toast, String marker) {
        toast.setText(marker);
        toast.show();
    }

}

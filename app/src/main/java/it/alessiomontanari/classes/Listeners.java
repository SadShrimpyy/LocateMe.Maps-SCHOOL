package it.alessiomontanari.classes;

import static it.alessiomontanari.MapsActivity.currentPosName;
import static it.alessiomontanari.MapsActivity.markerList;

import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import it.alessiomontanari.MapsActivity;

import java.util.Iterator;
import java.util.Objects;

public class Listeners {

    private final MapsActivity context;
    private Toast toast;

    public Listeners(Toast toast, MapsActivity context) {
        this.toast = toast;
        this.context = context;
    }

    private void showToast(Toast toast, String marker) {
        toast.setText(marker);
        toast.show();
    }

    public void clickMarker(View view) {
        GoogleMap map = context.getMap();
        map.setOnMarkerClickListener(marker -> {
            if (markerList.isEmpty()) return false;

            Iterator<ExtendedMarker> iterator = markerList.iterator();
            while (iterator.hasNext()) {
                ExtendedMarker obj = iterator.next();
                if (Objects.requireNonNull(obj.getMarker().getTitle()).equalsIgnoreCase(currentPosName)) {
                    showToast(toast, "Impossibile rimuovere la Posizione Corrente");
                } else if (obj.getMarker().equals(marker)) {
                    iterator.remove();
                    marker.remove();
                    showToast(toast, "Marcatore " + marker.getTitle() + " rimosso");
                    break; // Termina il loop dopo la rimozione del marcatore
                }
            }
            return true;
        });
    }

}

package it.alessiomontanari.classes;

import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.HashMap;

import it.alessiomontanari.MapsActivity;
import it.alessiomontanari.R;

public class Firestore {

    private FirebaseFirestore db;
    private MapsActivity context;
    private DocumentReference docRefRescuers;
    private DocumentReference docRefMarkers;
    private Rescuer rescuer;
    private String TAG = "<DB>";
    private MarkerOptions marker;


    public Firestore(MapsActivity context) {
        this.context = context;

        this.db = FirebaseFirestore.getInstance();

        this.marker = new MarkerOptions();
    }

    /** Aggiorna la posizione dell'ultimo soccorritore utilizzato dalla classe */
    public void updatePosLastSocc(LatLng latLng) {
        this.rescuer.setPosition(latLng);

        this.docRefRescuers.set(this.rescuer)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Aggiornamento/inserimento (posizione utente) effettuato con successo"))
                .addOnFailureListener(e -> Log.d(TAG, "Aggiornamento/inserimento (posizione utente) NON effettuato"));
    }

    public HashMap<String, Rescuer> updateAll() {
        if (docRefRescuers == null) return null;
        HashMap<String, Rescuer> others = new HashMap<>();

        CollectionReference collectionRef = db.collection(rescuer.getRescueCode());
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                others.putAll(extract(others, task));
            } else {
                Log.d(TAG, "Errore nel recuperare i documenti: ", task.getException());
            }
        }).addOnFailureListener(e -> Log.d(TAG, "Errore nel recuperare i documenti: " + e.getMessage()));

        return others;
    }

    /** Aggiorna la posizione del soccorritore, ricollocando il suo marcatore */
    public void updatePos() {
        if (docRefRescuers == null) return;

        db.collection(rescuer.getRescueCode())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Rescuer tempSocc = new Rescuer();
                        for (QueryDocumentSnapshot document : task.getResult())
                            setNewMarker(document, tempSocc);
                    } else
                        Log.d(TAG, "Errore nel recuperare i documenti: ", task.getException());
                })
                .addOnFailureListener(e -> Log.d(TAG, "Errore nel recuperare i documenti: " + e.getMessage()));
    }

    private HashMap<String, Rescuer> extract(HashMap<String, Rescuer> objs, Task<QuerySnapshot> task) {
        Rescuer socc = new Rescuer();
        for (QueryDocumentSnapshot document : task.getResult()) {
            objs.put(socc.getStrSerialNumber(), socc.objIntoNew(document.getData(), socc));
            Log.d(TAG, "Aggiornato il soccorritore: " + document.getId() + " => " + document.getData());
        }
        return objs;
    }

    public void delete() {
        if (this.docRefRescuers == null) return;

        this.docRefRescuers.delete()
                .addOnSuccessListener(aVoid -> {
                    // l'eliminazione è stata completata con successo
                })
                .addOnFailureListener(e -> {
                    // gestire eventuali errori
                });
    }

    /** Aggiungi al Firestore un soccorritore nuovo (se presente aggiorna i suoi dati) */
    public void storeNewSocc(Rescuer rescuer) {
        this.rescuer = rescuer;
        this.docRefRescuers = db.collection(this.rescuer.getRescueCode()).document(this.rescuer.getStrSerialNumber());
        if (this.rescuer == null) return;

        docRefRescuers.set(this.rescuer)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Inserimento (soccorritore) effettuato con successo, il documento ha ID: " + docRefRescuers.getId()))
                .addOnFailureListener(e -> Log.d(TAG, "Inserimento (soccorritore) NON effettuato"));
    }

    /** Dato i dati del soccorritore, posiziona il marcatore relativo alla sua posizione */
    private void setNewMarker(QueryDocumentSnapshot document, Rescuer tempSocc) {
        if (document.getId().contains("Marcatore")) return;

        tempSocc = tempSocc.objIntoNew(document.getData(), tempSocc);
        if (!tempSocc.getUsername().equals(rescuer.getUsername())) {
            Log.d(TAG, String.format(" --> FETCHED => Socc %s(%s) with location lat: %f and lon: %f\n",
                    tempSocc.getUsername(), tempSocc.getStrSerialNumber(), tempSocc.getLat(), tempSocc.getLon()));
            marker.position(tempSocc.getPosition())
                    .title("Operatore " + tempSocc.getUsername())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_yellow_marker));
            context.getMap().addMarker(marker);
        }
    }

    /** Aggiungi un marcatore al firebase */
    public void addMarkerToRescue(ExtendedMarker extendedMarker) {
        this.docRefMarkers = db.collection(this.rescuer.getRescueCode() + "<Markers>").document(extendedMarker.getTitle());
        if (this.docRefMarkers == null) return;

        docRefMarkers.set(extendedMarker)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Inserimento (marcatore) effettuato con successo, il documento ha ID: " + docRefRescuers.getId()))
                .addOnFailureListener(e -> Log.d(TAG, "Inserimento (marcatore) NON effettuato"));
    }

    /** Aggiorna i marcatori presi da firebase */
    public void drawMarkers() {
        db.collection(this.rescuer.getRescueCode() + "<Markers>")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ExtendedMarker tempMark = new ExtendedMarker();
                        for (QueryDocumentSnapshot document : task.getResult())
                            tempMark.objIntoNew(document.getData(), tempMark);
                            context.getMap().addMarker(tempMark.getMarker());
                    } else
                        Log.d(TAG, "Errore nel recuperare i marcatori: ", task.getException());
                })
                .addOnFailureListener(e -> Log.d(TAG, "Errore nel recuperare i marcatori: " + e.getMessage()));
    }
}

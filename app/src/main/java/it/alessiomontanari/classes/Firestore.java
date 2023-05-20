package it.alessiomontanari.classes;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
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
    private Toast toast;
    private DocumentReference documentRef;
    private Soccorritore soccorritore;
    private String TAG = "<DB>";
    private MarkerOptions marker;


    public Firestore(MapsActivity context, Toast toast) {
        this.context = context;
        this.toast = toast;

        this.db = FirebaseFirestore.getInstance();

        marker = new MarkerOptions();
    }

    public void updatePosLastSocc(LatLng latLng) {
        soccorritore.setPosition(latLng);

        documentRef.set(soccorritore)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Aggiornamento effettuato con successo"))
                .addOnFailureListener(e -> Log.d(TAG, "Inserimento NON effettuato"));
    }

    public HashMap<String, Soccorritore> updateAll() {
        if (documentRef == null) return null;
        HashMap<String, Soccorritore> others = new HashMap<>();

        CollectionReference collectionRef = db.collection(soccorritore.getCodiceSoccorso());
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                others.putAll(extract(others, task));
            } else {
                Log.d(TAG, "Errore nel recuperare i documenti: ", task.getException());
            }
        }).addOnFailureListener(e -> Log.d(TAG, "Errore nel recuperare i documenti: " + e.getMessage()));

        return others;
    }

    public void updatePos() {
        if (documentRef == null) return;

        db.collection(soccorritore.getCodiceSoccorso())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Soccorritore tempSocc = new Soccorritore();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            tempSocc = tempSocc.objIntoNew(document.getData(), tempSocc);
                            Log.d(TAG, String.format(" --> FETCHED => Socc %s(%s) with location lat: %f and lon: %f\n",
                                    tempSocc.getUsername(), tempSocc.getStrMatricola(), tempSocc.getLat(), tempSocc.getLon()));

                            marker.position(tempSocc.getPosition())
                                    .title("Operatore " + tempSocc.getUsername())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_yellow_marker));
                            context.getmMap().addMarker(marker);
                        }
                    } else {
                        Log.d(TAG, "Errore nel recuperare i documenti: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, "Errore nel recuperare i documenti: " + e.getMessage()));
    }

    private HashMap<String, Soccorritore> extract(HashMap<String, Soccorritore> objs, Task<QuerySnapshot> task) {
        Soccorritore socc = new Soccorritore();
        for (QueryDocumentSnapshot document : task.getResult()) {
            objs.put(socc.getStrMatricola(), socc.objIntoNew(document.getData(), socc));
            Log.d(TAG, "Aggiornato il soccorritore: " + document.getId() + " => " + document.getData());
        }
        return objs;
    }

    public void delete() {
        if (documentRef == null) return;

        documentRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // l'eliminazione è stata completata con successo
                })
                .addOnFailureListener(e -> {
                    // gestire eventuali errori
                });
    }

    public void storeNewSocc(Soccorritore soccorritore) {
        this.soccorritore = soccorritore;

        this.documentRef = db.collection(this.soccorritore.getCodiceSoccorso()).document(this.soccorritore.getStrMatricola());

        if (this.soccorritore == null) return;

        documentRef.set(soccorritore)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Inserimento effettuato con successo, il documento ha ID: " + documentRef.getId()))
                .addOnFailureListener(e -> Log.d(TAG, "Inserimento NON effettuato"));
    }
}

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


    public Firestore(MapsActivity context, Toast toast) {
        this.context = context;
        this.toast = toast;

        this.db = FirebaseFirestore.getInstance();
    }

    public void store() {
        if (this.soccorritore == null) return;

        assert this.soccorritore != null;

        documentRef.set(soccorritore)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Inserimento effettuato con successo, il documento ha ID: " + documentRef.getId());
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "inserimento NON effettiato");
                });
    }

    public void updatePosLastSocc(LatLng latLng) {
        soccorritore.setPosition(latLng);

        documentRef.set(soccorritore)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Aggiornamento effettuato con successo");
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "Inserimento NON effettiato");
                });

    }

    public HashMap<String, Soccorritore> updateAll() {
        if (documentRef == null) return null;
        HashMap<String, Soccorritore> others = new HashMap<>();

        System.out.println("Others readed");

        CollectionReference collectionRef = db.collection(soccorritore.getCodiceSoccorso());
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                others.putAll(extract(others, task)); // passa objs e task alla funzione extract
            } else if (task.getException() != null)
                Log.d(TAG, "Errore nel recuperare i documenti: ", task.getException());
            else
                Log.d(TAG, "Errore nel recuperare i documenti.");
        }).addOnFailureListener(e -> Log.d(TAG, "Errore nel recuperare i documenti: " + e.getMessage()));
        return others;
    }

    public void updatePos() {
        HashMap<String, Soccorritore> others = new HashMap<>();
        if (documentRef == null) return;

        db.collection(soccorritore.getCodiceSoccorso())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Soccorritore s = new Soccorritore();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                s = s.objIntoNew(document.getData(), s);
                                others.put(s.getStrMatricola(), s);
                                System.out.println("Fetched -> " + s.getStrMatricola());
                            }
                        }
                        setAllNew(others);
                    } else {
                        Log.d(TAG, "Errore nel recuperare i documenti: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, "Errore nel recuperare i documenti: " + e.getMessage()));
    }

    private void setAllNew(HashMap<String, Soccorritore> others) {
        GoogleMap mMap = context.mMap;
        MarkerOptions otherSocc = new MarkerOptions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            others.forEach((stri, socc) -> {
                System.out.println("ReFetched -> " + stri); // TODO: 5/15/2023 scorre tutti - cerco di display 
                otherSocc
                        .position(socc.getPosition())
                        .title("Operatore " + socc.getUsername())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_yellow_marker));
                mMap.addMarker(otherSocc); // Aggiungo il marcatore
            });
        }
    }

    private HashMap<String, Soccorritore> extract(HashMap<String, Soccorritore> objs, Task<QuerySnapshot> task) {
        Soccorritore s = new Soccorritore();
        for (QueryDocumentSnapshot document : task.getResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                objs.putIfAbsent(s.getStrMatricola(), s.objIntoNew(document.getData(), s));
            Log.d(TAG, "Aggiornato il soccorritore: " + document.getId() + " => " + document.getData());
        }
        return objs;
    }

    public void delete() {
        if (documentRef == null) return;

        documentRef.delete().addOnSuccessListener(aVoid -> {
            // l'eliminazione è stata completata con successo
        }).addOnFailureListener(e -> {
            // gestire eventuali errori
        });
    }

    public boolean isNew() {
        return documentRef == null;
    }

    public void storeNewSocc(Soccorritore soccorritore) {
        this.soccorritore = soccorritore;

        this.documentRef = db.collection(this.soccorritore.getCodiceSoccorso()).document(this.soccorritore.getStrMatricola());

        this.store();
    }
}

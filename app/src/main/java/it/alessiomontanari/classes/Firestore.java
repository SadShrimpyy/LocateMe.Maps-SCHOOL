package it.alessiomontanari.classes;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.HashMap;

import it.alessiomontanari.MapsActivity;

public class Firestore {

    private FirebaseFirestore db;
    private MapsActivity context;
    private Toast toast;
    private DocumentReference documentRef;
    private Soccorritore soccorritore;
    private String TAG = "DB";


    public Firestore(MapsActivity context, Toast toast) {
        this.context = context;
        this.toast = toast;

        this.db = FirebaseFirestore.getInstance();
    }

    public void store() {
        toast.show();
        if (this.soccorritore == null) return;

        assert this.soccorritore != null;

        documentRef.set(soccorritore)
                .addOnSuccessListener(aVoid -> {
                    Log.d("DB", "Inserimento effettuato con successo, il documento ha ID: " + documentRef.getId());
                }).addOnFailureListener(e -> {
                    Log.d("DB", "inserimento NON effettiato");
                });
    }

    public void updatePosLastSocc(LatLng latLng) {
        System.out.println("Update firestore");
        soccorritore.setPosition(latLng);

        documentRef.set(soccorritore)
                .addOnSuccessListener(aVoid -> {
                    Log.d("DB", "Aggiornamento effettuato con successo");
                }).addOnFailureListener(e -> {
                    Log.d("DB", "Inserimento NON effettiato");
                });

    }

    public HashMap<String, Soccorritore> readOthers() {
        if (documentRef == null) return null;
        HashMap<String, Soccorritore> others = new HashMap<>();

        CollectionReference collectionRef = db.collection(soccorritore.getCodiceSoccorso());
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                others.putAll(extract(others, task));// passa objs e task alla funzione extract
            } else if (task.getException() != null)
                Log.d("DB", "Errore nel recuperare i documenti: ", task.getException());
            else
                Log.d("DB", "Errore nel recuperare i documenti.");
        }).addOnFailureListener(e -> Log.d("[DB]", "Errore nel recuperare i documenti: " + e.getMessage()));
        return others;
    }

    private HashMap<String, Soccorritore> extract(HashMap<String, Soccorritore> objs, Task<QuerySnapshot> task) {
        Soccorritore s = new Soccorritore();
        for (QueryDocumentSnapshot document : task.getResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                objs.putIfAbsent(s.getStrMatricola(), s.objIntoNew(document.getData(), s));
            Log.d("DB", "Aggiornato il soccorritore: " + document.getId() + " => " + document.getData());
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

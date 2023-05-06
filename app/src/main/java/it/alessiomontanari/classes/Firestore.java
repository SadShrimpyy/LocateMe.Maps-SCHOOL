package it.alessiomontanari.classes;

import android.os.Build;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.alessiomontanari.MapsActivity;

public class Firestore {

    private FirebaseFirestore db;
    private MapsActivity context;
    private Toast toast;
    private DocumentReference documentRef;
    private Soccorritore soccorritore;

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
                    System.out.println("[DB] Inserimento effettuato con successo, il documento ha ID: " + documentRef.getId());
                }).addOnFailureListener(e -> {
                    System.out.println("[DB] inserimento NON effettiato");
                });
    }

    public void updatePosLastSocc(LatLng latLng) {
        System.out.println("Update firestore");
        soccorritore.setPosition(latLng);

        documentRef.set(soccorritore)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("[DB] Aggiornamento effettuato con successo");
                }).addOnFailureListener(e -> {
                    System.out.println("[DB] Aggiornamento NON effettiato");
                });

    }

    public void readOthers() {
        if (documentRef == null) return;
        HashMap<String, Object> objs;

        documentRef.get()
                .addOnSuccessListener(snapshot -> {
                    documents = snapshot.getId();
                    if (data != null)
                        //System.out.println("[DB] alls: " + data.get(Integer.toString(soccorritore.getMatricola())));
                        System.out.println("[DB] alls: " + data);

                    assert data != null;
                    List<String> list = new LinkedList<>(data.keySet());
                    if (list.isEmpty()) return;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        list.forEach(str -> {
                            System.out.println("[DB] values in " + str + " :" + data.get(str));
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("[DB] La query non è stata eseguita correttamente: " + e.getCause());
                });
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

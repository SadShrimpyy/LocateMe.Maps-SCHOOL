// Import statements
package it.alessiomontanari;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import it.alessiomontanari.classes.ButtonsManager;
import it.alessiomontanari.classes.ExtendedMarker;
import it.alessiomontanari.classes.Firestore;
import it.alessiomontanari.classes.Listeners;
import it.alessiomontanari.classes.Soccorritore;
import it.alessiomontanari.databinding.ActivityMapsBinding;

// Main activity class implementing OnMapReadyCallback
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Costanti
    public static final int RQ_INSERIMENTO = 1;
    public static String note;
    public Soccorritore soccorritore;

    // Referenze e variabili
    public static String currentPosName = "Posizione corrente";
    private Toast toast;
    private Firestore firestore;
    private int counter = 0;
    public static ArrayList<ExtendedMarker> markerList = new ArrayList<>();
    private GoogleMap map;
    private Listeners clicksListener;
    // Posizione e Marcatori
        private FusedLocationProviderClient fusedLocationClient;
        private LocationManager locationManager;
        private LocationListener locationListener;
        private MarkerOptions currentPosOptions;
        private LatLng tempLatLng = null;


    public MapsActivity() {
        // Ottenute da catta - Dalla parte di LOGIN
        String codiceSoccorso = "codiceSoccorso1234";
        String username = "usr3";
        int matricola = 0000321;

        soccorritore = new Soccorritore(matricola, username, codiceSoccorso, null);
    }

    public GoogleMap getMap() {
        return map;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityMapsBinding.inflate(getLayoutInflater()).getRoot());

        // Inizializza il toast e l'ascoltatore di eventi
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        clicksListener = new Listeners(toast, this);
        firestore = new Firestore(this);

        // Posizione in tempo reale
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener();

        // Bottoni
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            new ButtonsManager(this).addSaveFile(findViewById(R.id.bt_sve_mrk));
        else {
            toast.setText("File non salvato: versione non supportata");
            toast.show();
        }

        // Ottenere il SupportMapFragment e ricevere una notifica quando la mappa è pronta per essere utilizzata
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Memorizzare il nuovo soccorritore nel Firestore
        firestore.storeNewSocc(new Soccorritore(482, "SONOPROVA", "codiceSoccorso1234", new LatLng(49.468051, 0.431726)));
        firestore.storeNewSocc(soccorritore);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setOnMapClickListener(latLng -> {
            tempLatLng = latLng;
            addMarker();
        });

        if (map != null && currentPosOptions != null)
            firestore.updatePosLastSocc(currentPosOptions.getPosition());

        // Aggiungi i listener
        addListeners();

        // Ottieni l'ultima posizione conosciuta
        getLastLocation();
    }

    /** Aggiungi un marcatore alla mappa **/
    private void addMarker() {
        Intent inserimento = new Intent(this, Inserimento.class);
        startActivityForResult(inserimento, RQ_INSERIMENTO);
    }

    /** Gestire il risultato dell'attività d'inserimento */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQ_INSERIMENTO && resultCode == RQ_INSERIMENTO && tempLatLng != null) {
            String note = data.getStringExtra("note");
            if (note != null) {
                Toast.makeText(this, note + " ", Toast.LENGTH_SHORT).show();
                ExtendedMarker extendedMarker = new ExtendedMarker();
                extendedMarker.setPosition(tempLatLng);
                extendedMarker.setTitle("Marcatore " + (markerList.size() + 1));
                extendedMarker.setNote(note);
                map.addMarker(extendedMarker.getMarker());
                markerList.add(extendedMarker);

            }
        }
    }

    /** Ottenere l'ultima posizione conosciuta */
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        map.addMarker(new MarkerOptions()
                                .position(currentLocation)
                                .title(currentPosName));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    } else {
                        toast.setText("Posizione non disponibile");
                        toast.show();
                    }
                });
    }

    /** Gestire il risultato della richiesta di autorizzazione */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            toast.setText("Permesso di accesso alla posizione negato");
            toast.show();
        }
    }

    /** Ascoltatore di posizione */
    @SuppressLint("MissingPermission")
    private void locationListener() {
        locationListener = location -> {
            counter++;
            if (counter % 5 == 0)
                updatePos(location);
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    /** Aggiornare la posizione corrente, dell'utente in Firestore */
    private void updatePos(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (currentPosOptions != null)
            firestore.updatePosLastSocc(currentPosOptions.getPosition());
        firestore.updatePos();
        currentPosOptions = new MarkerOptions()
                .position(latLng)
                .title(currentPosName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        map.addMarker(currentPosOptions);
    }

    /** Aggiunti gli ascoltatori di eventi */
    private void addListeners() {
        /*findViewById(R.id.bt_reset).setOnClickListener(clicksListener::resetMarkers);
        findViewById(R.id.bt_svuota).setOnClickListener(clicksListener::clearMarkers);
        findViewById(R.id.bt_rimuovi_tutti).setOnClickListener(clicksListener::removeAllMarkers);
        findViewById(R.id.bt_canc_pos).setOnClickListener(clicksListener::removeCurrentPos);
        findViewById(R.id.bt_gst_soccorr).setOnClickListener(clicksListener::manageSoccorr);
        findViewById(R.id.bt_zo).setOnClickListener(clicksListener::zoomOnMarker);*/
        findViewById(R.id.bt_sve_mrk).setOnClickListener(clicksListener::clickMarker);
    }
}

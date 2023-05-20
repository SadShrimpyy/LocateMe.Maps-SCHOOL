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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int RQ_INSERIMENTO = 1;
    public static String note;
    public Soccorritore soccorritore;

    // Riferimenti al soccorritore - otteniti da Catta
    private String codiceSoccorso = "codiceSoccorso1234";
    private String username = "usr3";
    private int matricola = 0000321;
    public String getMatricola() { return String.valueOf(matricola); }

    public static String currentPosName = "Posizione corrente";
    private Toast toast;
    private Firestore firestore;
    private int counter = 0;

    // Markers
    public static ArrayList<ExtendedMarker> markerList = new ArrayList<>();
    private GoogleMap mMap;
    private Listeners clicksListener;

    // Posizione
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private MarkerOptions currentPosOptions;

    private LatLng tempLatLng = null;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ActivityMapsBinding.inflate(getLayoutInflater()).getRoot());

        soccorritore = new Soccorritore(matricola, username, codiceSoccorso, null);

        // Toast e ascoltatore di eventi
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        clicksListener = new Listeners(toast);
        firestore = new Firestore(this, toast);

        // Posizione in tempo reale
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener();

        // Bottone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            new ButtonsManager(this).addSaveFile(findViewById(R.id.bt_gst_mrk));
        else {
            toast.setText("File non salvato: versione non supportata");
            toast.show();
        }

        // Ottieni il SupportMapFragment e veniamo notificati quando la mappa sarà pronta ad essere usata
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        firestore.storeNewSocc(new Soccorritore(1011, "firstSoccorrer", "codeApp01", new LatLng(45.806302, 9.004601)));
        firestore.storeNewSocc(new Soccorritore(1012, "secondSoccorrer", "codeApp01", new LatLng(45.489426, 9.185625)));
        firestore.storeNewSocc(new Soccorritore(1013, "thirdSoccorrer", "codeApp02", new LatLng(50.013032, 19.719968)));
        firestore.storeNewSocc(new Soccorritore(1014, "fourthSoccorrer", "codeApp01", new LatLng(3, 3)));

        firestore.updatePosLastSocc(new LatLng(50.586165, 28.290485));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMapClickListener(latLng -> {
            tempLatLng = latLng;
            addMarker();
        });

        if (mMap != null)
            firestore.updatePos();

        // Aggiungere i listener
        addListeners();

        // Prendiamo l'ultima posizione dal tracker
        getLastLocation();
    }

    private void addMarker() {
        // Nuova attività
        Intent inserimento = new Intent(this, Inserimento.class);
        startActivityForResult(inserimento, RQ_INSERIMENTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQ_INSERIMENTO && resultCode == RQ_INSERIMENTO && tempLatLng != null) {
            String note = data.getStringExtra("note");
            if (note != null) {
                Toast.makeText(this, note + " ", Toast.LENGTH_SHORT).show();
                ExtendedMarker extendedMarker = new ExtendedMarker(); // Nuovo oggetto marcatore
                extendedMarker.setPosition(tempLatLng);
                extendedMarker.setTitle("Marcatore " + (markerList.size() + 1));
                extendedMarker.setNote(note);
                // Aggiungo il marcatore e lo salvo nell'array
                mMap.addMarker(extendedMarker.getMarker());
                markerList.add(extendedMarker);
                toast.setText("Clicca sul marcatore per rimuoverlo");
                toast.show();
            }
        }
    }


    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Richiesta permessi
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        // Posizione corrente
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(currentLocation)
                            .title(currentPosName));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    // Prima posizione
                } else {
                    toast.setText("Posizione non disponibile");
                    toast.show();
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getLastLocation();
            else {
                toast.setText("Permesso posizione negato");
                toast.show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }


    // Location listener
    private void locationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (counter > 3) {
                    mMap.clear();
                    firestore.updatePos();
                    // Cambio icona del marcatore
                    LatLng newLatLang = new LatLng(location.getLatitude(), location.getLongitude());
                    currentPosOptions = new MarkerOptions()
                            .position(newLatLang)
                            .title("Posizione attuale")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_blue_marker));
                    mMap.addMarker(currentPosOptions); // Aggiungo il marcatore

                    // Aggiungo i marcatori (ho cancellato la mappa)
                    for (ExtendedMarker marker : markerList)
                        mMap.addMarker(marker.getMarker());
                    soccorritore.setPosition(newLatLang);
                    counter = 0;
                }
                else
                    counter++;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Richiesta del permesso di ACCESS_FINE_LOCATION
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    // Aggiungo i listeners
    private void addListeners() {
        clicksListener.clickMarker(mMap);
    }

    public GoogleMap getmMap() {
        return this.mMap;
    }
}
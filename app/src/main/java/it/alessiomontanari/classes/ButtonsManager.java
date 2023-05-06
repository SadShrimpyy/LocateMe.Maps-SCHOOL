package it.alessiomontanari.classes;

import static it.alessiomontanari.MapsActivity.markerList;

import android.os.Build;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

import it.alessiomontanari.MapsActivity;

public class ButtonsManager {

    private final File file;
    private Toast toast;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ButtonsManager(MapsActivity main) {
        toast = Toast.makeText(main, "", Toast.LENGTH_SHORT);

        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "protezione_civile-" + LocalDate.now().toString() + ".txt");
    }

    public void addSaveFile(Button btGstMark) {
        btGstMark.setOnClickListener(view -> {
            String output = createNewFile();
            if (output != null) {
                toast.setText(output);
                toast.show();
            }

            output = writeIntoFile();
            if (output != null) {
                toast.setText(output);
                toast.show();
            }
        });
    }

    private String createNewFile() {
        try {
            if (file.createNewFile())
                System.out.println("\nFile creato: " + file.getName() + "\nnella cartella: " + file.getPath() + "\n\n");
        } catch (IOException e) {
            System.out.println("\nFile NON creato: " + file.getName() + "\nnella cartella: " + file.getPath() + "\n\n");
            return "ERRORE: File non creato.";
        }
        return null;
    }

    private String writeIntoFile() {
        if (markerList.isEmpty())
            return "Inserire prima dei marcatori";

        try (FileWriter writer = new FileWriter(file)) {
            for (ExtendedMarker marker : markerList)
                writer.append(marker.getMarker().getTitle())
                        .append("<")
                        .append(String.valueOf(marker.getMarker().getPosition()))
                        .append(String.valueOf(marker.getNote()))
                        .append(">")
                        .append("\n");
            return "Marcatori salvati (" + file.getPath() + "/" + file.getName() + ")";
        } catch (IOException e) {
            e.printStackTrace();
            return "Errore durante il salvataggio dei marcatori";
        }
    }
}

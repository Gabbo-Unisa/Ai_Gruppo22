package knn;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Sample {
    double[] features;
    int cls;

    /*
        Costruttore 1: per inizializzare il campione con un dato insieme di
        caratteristiche e l'etichetta della classe.
        Utilizzato quando si costruisce il set di dati.
    */
    public Sample(double[] features, int cls) {
        this.features = features;
        this.cls = cls;
    }

    /*
        Costruttore 2: per inizializzare il campione con un dato insieme di
        caratteristiche senza un'etichetta di classe.
        Utilizzato per classificare un nuovo campione.
    */
    public Sample(double[] features) {
        this.features = features;
        this.cls = -1; //La classe viene settata di default a -1
    }

    /*
        Costruttore 3: per inizializzare il campione da una riga CSV.
        Assume che l'ultimo valore nel CSV sia l'etichetta della classe.
    */
    public Sample(String line) {
        String[] parts = line.split(";");
        int n = parts.length;
        features = new double[n - 1];

        // Configura un NumberFormat per il parsing di numeri con la virgola come separatore decimale,
        // utilizzando le impostazioni regionali italiane.
        NumberFormat format = NumberFormat.getInstance(Locale.ITALIAN);

        for (int i = 0; i < n - 1; i++) {
            try {
                // Utilizza il NumberFormat configurato per fare il parsing della stringa in un numero,
                // quindi ottieni il valore double.
                features[i] = format.parse(parts[i].trim()).doubleValue();
            } catch (ParseException e) {
                System.err.println("Errore nel parsing del numero: '" + parts[i].trim() + "' nella riga: '" + line + "'. Impostato a 0.0.");
                // In caso di errore di parsing (es. stringa non numerica inaspettata),
                // stampa un messaggio di errore e imposta la feature a 0.0 come fallback.
                // Considera se un'altra gestione dell'errore è più appropriata per il tuo caso (es. lanciare un'eccezione).
                features[i] = 0.0;
                e.printStackTrace(); // Stampa lo stack trace dell'errore per debug
            } catch (NumberFormatException nfe) {
                System.err.println("Errore NumberFormatException per: '" + parts[i].trim() + "' nella riga: '" + line + "'. Impostato a 0.0.");
                features[i] = 0.0;
                nfe.printStackTrace();
            }
        }
        // L'etichetta della classe (cls) è un intero, quindi Integer.parseInt dovrebbe andare bene
        // a meno che anche questo non contenga separatori di migliaia inaspettati (improbabile per un ID di classe).
        try {
            this.cls = Integer.parseInt(parts[n - 1].trim());
        } catch (NumberFormatException e) {
            System.err.println("Errore nel parsing della classe: '" + parts[n - 1].trim() + "' nella riga: '" + line + "'. Impostata a -1.");
            this.cls = -1; // Valore di default in caso di errore
            e.printStackTrace();
        }
    }

    /*
        Costruttore 4: specifico per TORCS con tutte le informazioni: angolo, velocità, sensori
                       e posizione sulla pista, più la classe. 
                       Usato per creare campioni etichettati (fase di training).
    */
    public Sample(double angle, double speedX, double[] edgeSensors, double trackPosition, int cls) {
        this.features = new double[edgeSensors.length + 3];
        this.features[0] = angle;
        this.features[1] = speedX;
        System.arraycopy(edgeSensors, 0, this.features, 2, edgeSensors.length);
        this.features[features.length - 1] = trackPosition;
        this.cls = cls;
    }

    /*
        Costruttore 5: variante del costruttore per TORCS usata nella fase di test o guida autonoma:
                       NON richiede una classe, perché verrà predetta dal classificatore.
                       Imposta cls = -1 come valore di default.
    */
    public Sample(double angle, double speedX, double[] edgeSensors, double trackPosition) {
        this(angle, speedX, edgeSensors, trackPosition, -1);
    }

    /*
        Metodo per ottenere le caratteristiche del campione.
        Utile per accedere ai dati del campione.
    */
    public double[] getFeatures() { return features; }

    /*
        Metodo per calcolare la distanza euclidea tra due campioni
    */
    public double distance(Sample other) {
        double sum = 0;
        for (int i = 0; i < this.features.length; i++) {
            sum += Math.pow(this.features[i] - other.features[i], 2);
        }
        return Math.sqrt(sum);
    }
}
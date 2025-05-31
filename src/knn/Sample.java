package knn;

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
        String[] parts = line.split(",");
        int n = parts.length;
        features = new double[n - 1];
        for (int i = 0; i < n - 1; i++) {
            features[i] = Double.parseDouble(parts[i].trim());
        }
        this.cls = Integer.parseInt(parts[n - 1].trim());
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
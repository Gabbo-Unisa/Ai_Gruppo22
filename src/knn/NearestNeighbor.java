package knn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NearestNeighbor {
    private List<Sample>  trainingData;
    private KDTree kdtree;
    private int[] classCounts;
    private double[] featureMin;
    private double[] featureMax;
    private String firstLineOfTheFile;

    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.classCounts = new int[10]; // Assumiamo 10 possibili classi, da -1 a 8
        this.firstLineOfTheFile = "angle;distFromStart;speedX;track_2;track_4;track_6;track_8;track_9;track_10;track_12;track_14;track_16;trackPos;class";
        this.readPointsFromCSV(filename);
        normalizeData();
        this.kdtree = new KDTree(trainingData);
    }

    /*
        Metodo che legge i dati da un file csv e normalizza le feature dei campioni
        questo perchè avendo tutte le feature su scala simile (da 0 a 1) anche se una presenta
        valori molto grandi non influenza particolarmente la distanza.
     */
    private void normalizeData() {
        int featureLength = trainingData.get(0).features.length;
        featureMin = new double[featureLength];
        featureMax = new double[featureLength];

        for (int i = 0; i < featureLength; i++) {
            featureMin[i] = Double.MAX_VALUE;
            featureMax[i] = Double.MIN_VALUE;
        }

        //ricerca max e min per ogni feature
        for (Sample sample : trainingData) {
            for (int i = 0; i < featureLength; i++) {
                if (sample.features[i] < featureMin[i])
                    featureMin[i] = sample.features[i];
                if (sample.features[i] > featureMax[i])
                    featureMax[i] = sample.features[i];
            }
        }

        /*
            Normalizzazione di ogni feature dei campioni utilizzando la formula:
            (valore - minimo) / (massimo-minimo) -> 0 < sample.features[i] < 1
         */
        for (Sample sample : trainingData) {
            for (int i = 0; i < featureLength; i++) {
                sample.features[i] = (sample.features[i] - featureMin[i]) / (featureMax[i] - featureMin[i]);
            }
        }
    }

    /*
     * Metodo per normalizzare un singolo Sample (non nel trainingData)
     * usando i min e max calcolati dal dataset di training.
     */
    public void normalizeSample(Sample sample) {
        if (featureMin == null || featureMax == null || featureMin.length == 0) {
            System.err.println("Errore: featureMin o featureMax non sono stati inizializzati o sono vuoti. Assicurati che i dati di training siano stati letti e normalizzati correttamente.");
            // Potresti voler lanciare un'eccezione o gestire in altro modo
            return;
        }

        double[] features = sample.getFeatures();
        if (features.length != featureMin.length) {
            System.err.println("Errore: il numero di feature del campione non corrisponde alla dimensione dei min/max calcolati. Sample features: " + features.length + ", Min/Max features: " + featureMin.length);
            return; // O gestisci l'errore in modo appropriato
        }

        for (int i = 0; i < features.length; i++) {
            double min = featureMin[i];
            double max = featureMax[i];

            if (max - min != 0) {
                features[i] = (features[i] - min) / (max - min);
            } else {
                // Se la feature è costante nel training data, il valore normalizzato può essere 0.5 o 0.
                // 0.5 è spesso preferito per non dare un bias eccessivo se non c'è varianza.
                features[i] = 0.5;
            }
        }
    }

    /*
        Legge i dati da un file CSV e li aggiunge alla lista trainingData,
        saltando la prima riga se corrisponde all'intestazione.
     */
    private void readPointsFromCSV(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;

            // Salta la prima riga (intestazione con i nomi delle colonne)
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                trainingData.add(new Sample(line));
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Sample> findKNearestNeighbors(Sample testPoint, int k) {
        return kdtree.kNearestNeighbors(testPoint,k);
    }

    public int classify(Sample testPoint, int k){
        List<Sample> kNearestNeighbors = findKNearestNeighbors(testPoint, k);

        //reset del classCounts
        for (int i = 0; i < classCounts.length; i++) {
            classCounts[i] = 0;
        }

        //conta le occorrenze di ogni classe nei k vicini più prossimi
        for (Sample neighbor : kNearestNeighbors) {
            classCounts[neighbor.cls]++;
        }

        //ricerca della classe con il count massimo
        int maxCount = -1;
        int predictedClass = -1;
        for (int i = 0; i < classCounts.length; i++) {
            if (classCounts[i] > maxCount) {
                maxCount = classCounts[i];
                predictedClass = i;
            }
        }

        return predictedClass;
    }

    public double[] getFeatureMin(){return featureMin;}
    public double[] getFeatureMax(){return featureMax;}
    public List<Sample> getTrainingData(){return trainingData;}
}
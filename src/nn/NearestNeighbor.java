package nn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NearestNeighbor {
    private List<Sample> trainingData;
    private double[] featureMin;
    private double[] featureMax;
    
    public NearestNeighbor(String filename) {
        this.trainingData = new ArrayList<>();
        this.readPointsFromCSV(filename);
        normalizeData();
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
    
    public Sample findNearestNeighbor(Sample testPoint) {
        if (trainingData.isEmpty()) {
            System.out.println("training set vuoto");
            // Crea un Sample con tutte le feature a 0
            new Sample(0.0, 0.0, 0.0, new double[18],0.0);
        }
    
        Sample nearestNeighbor = trainingData.get(0); // Imposta il primo punto come punto più vicino iniziale
        double minDistance = testPoint.distance(nearestNeighbor); // Calcola la distanza dal primo punto
    
        // Cerca il punto più vicino
        for (Sample point : trainingData) {
            double distance = testPoint.distance(point);
            if (distance < minDistance) {
                minDistance = distance;
                nearestNeighbor = point;
            }
        }
    
        return nearestNeighbor;
    }

    public int classify(Sample testPoint){
        Sample nearestNeighbor = findNearestNeighbor(testPoint);

        return nearestNeighbor.cls; // Restituisce la classe del vicino più vicino
    }

    public double[] getFeatureMin(){return featureMin;}
    public double[] getFeatureMax(){return featureMax;}
    public List<Sample> getTrainingData() {return trainingData;}
}
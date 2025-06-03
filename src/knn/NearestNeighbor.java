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
        this.classCounts = new int[12]; // Assumiamo 13 classi, da 0 a 12
        this.firstLineOfTheFile = "angle,speedX,track_0,track_1,track_2,track_3,track_4,track_5,track_6,track_7," +
                                  "track_8,track_9,track_10,track_11,track_12,track_13,track_14,track_15,track_16,track_17,track_18,trackPos,class";
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
                if (sample.features[i] < featureMin[i]) featureMin[i] = sample.features[i];
                if (sample.features[i] > featureMax[i]) featureMax[i] = sample.features[i];
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
        Legge i dati da un file CSV e li aggiunge alla lista trainingData,
        saltando la prima riga se corrisponde all'intestazione.
     */
    private void readPointsFromCSV(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(firstLineOfTheFile)) {
                    continue; // Skip header
                }
                // Add the sample by calling the constructor that takes the string input
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
package com.ontariotechu.sofe3980U;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {
    public static void main(String[] args) {
        String[] modelFiles = {"SOFE3980U-Lab4\\SVBR\\model_1.csv", "SOFE3980U-Lab4\\SVBR\\model_2.csv", "SOFE3980U-Lab4\\SVBR\\model_3.csv"};
        String bestModel = "";
        double bestAUC = -1.0;
        
        // Evaluate each model file
        for (String filePath : modelFiles) {
            System.out.println("Evaluating: " + filePath);
            ClassificationMetrics metrics = evaluateModel(filePath);
            if (metrics == null) {
                System.out.println("Skipping " + filePath + " due to read error.\n");
                continue;
            }
            System.out.println("Binary Cross Entropy: " + metrics.bce);
            System.out.println("Confusion Matrix:");
            System.out.println("TP: " + metrics.tp + "  FP: " + metrics.fp);
            System.out.println("FN: " + metrics.fn + "  TN: " + metrics.tn);
            System.out.println("Accuracy: " + metrics.accuracy);
            System.out.println("Precision: " + metrics.precision);
            System.out.println("Recall: " + metrics.recall);
            System.out.println("F1 Score: " + metrics.f1);
            System.out.println("AUC-ROC: " + metrics.auc);
            System.out.println();
            
            // We select the best model based on AUC-ROC
            if (metrics.auc > bestAUC) {
                bestAUC = metrics.auc;
                bestModel = filePath;
            }
        }
        
        if (!bestModel.isEmpty()) {
            System.out.println("Best model based on AUC-ROC: " + bestModel);
        } else {
            System.out.println("No valid model data found.");
        }
    }
    
    /**
     * Reads the CSV file and computes classification metrics.
     */
    private static ClassificationMetrics evaluateModel(String filePath) {
        // Check if file exists
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File " + filePath + " does not exist.");
            return null;
        }
        
        try (FileReader filereader = new FileReader(file);
             CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build()) {
             
             List<String[]> allData = csvReader.readAll();
             if (allData.size() == 0) return null;
             
             double bceSum = 0.0;
             int count = 0;
             int tp = 0, fp = 0, tn = 0, fn = 0;
             List<Pair> aucData = new ArrayList<>();
             // Use a small epsilon to avoid log(0)
             double epsilon = 1e-15;
             
             for (String[] row : allData) {
                 int y_true = Integer.parseInt(row[0]);
                 double y_pred = Double.parseDouble(row[1]);
                 
                 // Clamp y_pred for log stability
                 y_pred = Math.min(Math.max(y_pred, epsilon), 1 - epsilon);
                 
                 // Calculate BCE for this instance
                 bceSum += -(y_true * Math.log(y_pred) + (1 - y_true) * Math.log(1 - y_pred));
                 
                 // Confusion matrix using threshold of 0.5
                 int predictedClass = (y_pred >= 0.5) ? 1 : 0;
                 if (y_true == 1 && predictedClass == 1) {
                     tp++;
                 } else if (y_true == 1 && predictedClass == 0) {
                     fn++;
                 } else if (y_true == 0 && predictedClass == 1) {
                     fp++;
                 } else if (y_true == 0 && predictedClass == 0) {
                     tn++;
                 }
                 
                 // Collect data for AUC computation
                 aucData.add(new Pair(y_pred, y_true));
                 count++;
             }
             
             double bce = bceSum / count;
             double accuracy = (double)(tp + tn) / count;
             double precision = (tp + fp) > 0 ? (double)tp / (tp + fp) : 0;
             double recall = (tp + fn) > 0 ? (double)tp / (tp + fn) : 0;
             double f1 = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0;
             double auc = calculateAUC(aucData);
             
             return new ClassificationMetrics(bce, tp, fp, tn, fn, accuracy, precision, recall, f1, auc);
        } catch (Exception e) {
            System.out.println("Error reading file: " + filePath);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Calculates the AUC-ROC using the Mann–Whitney U statistic approach.
     */
    private static double calculateAUC(List<Pair> data) {
        int n = data.size();
        // Sort by predicted probability (score) in ascending order
        Collections.sort(data);
        int posCount = 0, negCount = 0;
        double rankSum = 0.0;
        for (int i = 0; i < n; i++) {
            Pair p = data.get(i);
            if (p.label == 1) {
                posCount++;
                // Rank is i+1 because ranks are 1-indexed
                rankSum += (i + 1);
            } else {
                negCount++;
            }
        }
        if (posCount == 0 || negCount == 0) {
            return 0.5; // If one class is missing, return a neutral value
        }
        // Compute AUC as per the Mann–Whitney U statistic formula
        return (rankSum - posCount * (posCount + 1) / 2.0) / (posCount * negCount);
    }
    
    // Helper class to hold predicted probability and true label for AUC calculation
    private static class Pair implements Comparable<Pair> {
        double score;
        int label;
        public Pair(double score, int label) {
            this.score = score;
            this.label = label;
        }
        @Override
        public int compareTo(Pair other) {
            return Double.compare(this.score, other.score);
        }
    }
    
    // Helper class to store all the classification metrics for a model
    private static class ClassificationMetrics {
        double bce;
        int tp, fp, tn, fn;
        double accuracy, precision, recall, f1, auc;
        
        public ClassificationMetrics(double bce, int tp, int fp, int tn, int fn, 
                                     double accuracy, double precision, double recall, 
                                     double f1, double auc) {
            this.bce = bce;
            this.tp = tp;
            this.fp = fp;
            this.tn = tn;
            this.fn = fn;
            this.accuracy = accuracy;
            this.precision = precision;
            this.recall = recall;
            this.f1 = f1;
            this.auc = auc;
        }
    }
}

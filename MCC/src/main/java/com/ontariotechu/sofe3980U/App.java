package com.ontariotechu.sofe3980U;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\tejus\\Desktop\\SQPM\\lab4\\SOFE3980U-Lab4\\MCC\\model.csv";
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Error: File " + filePath + " does not exist.");
            return;
        }
        
        List<String[]> allData;
        try (FileReader filereader = new FileReader(file);
             CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build()) {
            allData = csvReader.readAll();
        } catch (Exception e) {
            System.out.println("Error reading the CSV file");
            e.printStackTrace();
            return;
        }
        
        int numClasses = 5;
        int[][] confusionMatrix = new int[numClasses][numClasses];
        double ceSum = 0.0;
        int count = 0;
        double epsilon = 1e-15;  // to avoid log(0)
        
        for (String[] row : allData) {
            // Parse true label; assume labels are 1-indexed (1 to 5)
            int trueLabel = Integer.parseInt(row[0]);
            int trueIndex = trueLabel - 1;
            
            double[] predictions = new double[numClasses];
            for (int i = 0; i < numClasses; i++) {
                predictions[i] = Double.parseDouble(row[i + 1]);
            }
            
            // Calculate Cross Entropy (CE) for the true class probability
            double prob = predictions[trueIndex];
            // Clamp the probability to avoid issues with log(0)
            prob = Math.min(Math.max(prob, epsilon), 1 - epsilon);
            ceSum += -Math.log(prob);
            
            // Determine the predicted class (index of the maximum probability)
            int predictedIndex = 0;
            double maxProb = predictions[0];
            for (int i = 1; i < numClasses; i++) {
                if (predictions[i] > maxProb) {
                    maxProb = predictions[i];
                    predictedIndex = i;
                }
            }
            
            // Update confusion matrix (rows: true, columns: predicted)
            confusionMatrix[trueIndex][predictedIndex]++;
            
            count++;
        }
        
        double averageCE = ceSum / count;
        
        // Output results
        System.out.println("Average Cross Entropy: " + averageCE);
        System.out.println("Confusion Matrix (rows: true labels, columns: predicted labels):");
        
        // Print header row (class labels 1-5)
        System.out.print("    ");
        for (int i = 1; i <= numClasses; i++) {
            System.out.printf("%4d", i);
        }
        System.out.println();
        
        // Print each row of the confusion matrix
        for (int i = 0; i < numClasses; i++) {
            System.out.printf("%4d", i + 1);
            for (int j = 0; j < numClasses; j++) {
                System.out.printf("%4d", confusionMatrix[i][j]);
            }
            System.out.println();
        }
    }
}

package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {
    public static void main(String[] args) {
        String[] modelFiles = {"SOFE3980U-Lab4\\SVCR\\model_1.csv", "SOFE3980U-Lab4\\SVCR\\model_2.csv", "SOFE3980U-Lab4\\SVCR\\model_3.csv"};
        String bestModel = "";
        double lowestMSE = Double.MAX_VALUE;
        
        for (String filePath : modelFiles) {
            System.out.println("Evaluating: " + filePath);
            double[] metrics = evaluateModel(filePath);
            System.out.printf("MSE: %.4f, MAE: %.4f, MARE: %.4f%%\n\n", metrics[0], metrics[1], metrics[2] * 100);
            
            if (metrics[0] < lowestMSE) {
                lowestMSE = metrics[0];
                bestModel = filePath;
            }
        }
        
        System.out.println("Best model based on MSE: " + bestModel);
    }

    private static double[] evaluateModel(String filePath) {
        try (FileReader filereader = new FileReader(filePath);
             CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build()) {
            
            List<String[]> allData = csvReader.readAll();
            double mse = 0, mae = 0, mare = 0;
            int count = 0;
            
            for (String[] row : allData) {
                double y_true = Double.parseDouble(row[0]);
                double y_pred = Double.parseDouble(row[1]);
                double error = y_true - y_pred;
                
                mse += error * error;
                mae += Math.abs(error);
                mare += Math.abs(error / y_true);
                count++;
            }
            
            if (count == 0) return new double[]{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
            
            mse /= count;
            mae /= count;
            mare /= count;
            
            return new double[]{mse, mae, mare};
        } catch (Exception e) {
            System.out.println("Error reading file: " + filePath);
            return new double[]{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        }
    }
}

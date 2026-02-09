package com.enadd.test;

import com.enadd.core.visualization.ComprehensiveConflictListGenerator;


/**
 * Simple test class to verify comprehensive conflict list generation
 */
public class TestConflictListGeneration {
    public static void main(String[] args) {
        System.out.println("Testing comprehensive conflict list generation...");

        try {
            ComprehensiveConflictListGenerator generator = new ComprehensiveConflictListGenerator();
            generator.generateComprehensiveConflictList();

            System.out.println("Comprehensive conflict list generated successfully!");
        } catch (Exception e) {
            System.err.println("Error generating comprehensive conflict list: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

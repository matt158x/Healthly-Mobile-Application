package com.example.healthlyandroid;

public class ingredientsList {
    private String name;
    private double kcal;
    private double protein;
    private double fat;
    private double carbs;

    public ingredientsList(String name, double kcal, double protein, double fat, double carbs) {
        this.name = name;
        this.kcal = kcal;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    public String getName() {
        return name;
    }

    public double getKcal() {
        return kcal;
    }

    public double getProtein() {
        return protein;
    }

    public double getFat() {
        return fat;
    }

    public double getCarbs() {
        return carbs;
    }
}
package com.college.models;

public class Transaction {
    private String date;
    private String type;
    private double amount;
    private String description;

    public Transaction(String date, String type, double amount, String description) {
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.description = description;
    }

    // Getters
    public String getDate() { return date; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }

    // Setters
    public void setDate(String date) { this.date = date; }
    public void setType(String type) { this.type = type; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
}

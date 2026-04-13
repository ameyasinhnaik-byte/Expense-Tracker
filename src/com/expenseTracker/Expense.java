package com.expenseTracker;

import java.time.LocalDate;

public class Expense {

    private int id;
    private double amount;
    private String category;
    private LocalDate date;
    private String description;

    public Expense(int id, double amount, String category, LocalDate date, String description) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("[#%d] %-12s | Rs. %8.2f | %s | %s",
                id, category, amount, date, description);
    }
}
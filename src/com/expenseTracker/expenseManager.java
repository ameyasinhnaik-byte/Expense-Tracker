package com.expenseTracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class expenseManager {

    private ArrayList<Expense> expenses = new ArrayList<>();
    private int nextId = 1;

    // Add a new expense
    public void addExpense(double amount, String category, String description) {
        Expense expense = new Expense(nextId++, amount, category, LocalDate.now(), description);
        expenses.add(expense);
        System.out.println("Expense added successfully!");
    }

    // List all expenses
    public void listExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded yet.");
            return;
        }
        System.out.println("\n--- All Expenses ---");
        for (Expense e : expenses) {
            System.out.println(e);
        }
        System.out.println("--------------------");
        System.out.printf("Total: Rs. %.2f%n%n", getTotal());
    }

    // Calculate total
    public double getTotal() {
        double total = 0;
        for (Expense e : expenses) {
            total += e.getAmount();
        }
        return total;
    }

    // Get all expenses (useful for future features like filtering)
    public List<Expense> getExpenses() {
        return expenses;
    }
}
package com.expenseTracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Month;

public class expenseManager {

    private ArrayList<Expense> expenses = new ArrayList<>();
    private int nextId = 1;

    // Add a new expense
    public void addExpense(double amount, String category, String description) {
        String cat = normalizeCategory(category);
        Expense expense = new Expense(nextId++, amount, cat, LocalDate.now(), description);
        expenses.add(expense);
        System.out.println("  Expense added! [#" + expense.getId() + "]");
    }

    // -------------------------
    //  VIEW / LIST
    // -------------------------

    public void listExpenses() {
        printExpenseList(expenses, "All Expenses");
    }

    public void listByCategory(String category) {
        String cat = normalizeCategory(category);
        List<Expense> filtered = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getCategory().equalsIgnoreCase(cat)) filtered.add(e);
        }
        printExpenseList(filtered, "Expenses — " + cat);
    }

    public void listByMonth(int month, int year) {
        List<Expense> filtered = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getDate().getMonthValue() == month && e.getDate().getYear() == year) {
                filtered.add(e);
            }
        }
        String label = Month.of(month).name() + " " + year;
        printExpenseList(filtered, "Expenses — " + label);
    }

    public void listCategories() {
        if (expenses.isEmpty()) {
            System.out.println("  No expenses yet.");
            return;
        }
        Map<String, Double> totals = getCategoryTotals();
        System.out.println("\n  Categories in use:");
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            System.out.printf("    %-15s Rs. %.2f%n", entry.getKey(), entry.getValue());
        }
        System.out.println();
    }

    // -------------------------
    //  TOTALS / HELPERS
    // -------------------------

    public double getTotal() {
        double total = 0;
        for (Expense e : expenses) total += e.getAmount();
        return total;
    }

    public Map<String, Double> getCategoryTotals() {
        Map<String, Double> map = new HashMap<>();
        for (Expense e : expenses) {
            map.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        return map;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    // -------------------------
    //  PRIVATE UTILITIES
    // -------------------------

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) return "Other";
        String trimmed = category.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    private void printExpenseList(List<Expense> list, String title) {
        System.out.println("\n  --- " + title + " ---");
        if (list.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (Expense e : list) System.out.println("  " + e);
            System.out.println("  " + "-".repeat(60));
            double total = 0;
            for (Expense e : list) total += e.getAmount();
            System.out.printf("  Total: Rs. %.2f%n", total);
        }
        System.out.println();
    }
}
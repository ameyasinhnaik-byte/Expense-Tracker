package com.expenseTracker;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportPrinter
 * Generates formatted reports from expense data.
 * All methods are static — no need to instantiate this class.
 */
public class ReportPrinter {

    private static final int BAR_WIDTH = 30; // max chars for bar chart bars

    // -------------------------
    //  FULL SUMMARY REPORT
    // -------------------------

    public static void printFullReport(ExpenseManager manager) {
        List<Expense> expenses = manager.getExpenses();
        if (expenses.isEmpty()) {
            System.out.println("  No data to report.");
            return;
        }

        System.out.println("\n  ============================================");
        System.out.println("              EXPENSE SUMMARY REPORT          ");
        System.out.println("  ============================================");
        System.out.printf("  Total expenses recorded : %d%n", expenses.size());
        System.out.printf("  Grand total             : Rs. %.2f%n", manager.getTotal());

        printCategoryBreakdown(manager);
        printMonthlyBreakdown(expenses);
        printTopExpenses(expenses, 3);
        printCategoryBarChart(manager);
    }

    // -------------------------
    //  CATEGORY BREAKDOWN
    // -------------------------

    public static void printCategoryBreakdown(ExpenseManager manager) {
        Map<String, Double> totals = manager.getCategoryTotals();
        double grand = manager.getTotal();

        System.out.println("\n  --- Spending by Category ---");
        System.out.printf("  %-15s  %10s  %7s%n", "Category", "Amount", "Share");
        System.out.println("  " + "-".repeat(38));

        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            double pct = (grand > 0) ? (entry.getValue() / grand * 100) : 0;
            System.out.printf("  %-15s  Rs. %7.2f  %5.1f%%%n",
                    entry.getKey(), entry.getValue(), pct);
        }
        System.out.println();
    }

    // -------------------------
    //  MONTHLY BREAKDOWN
    // -------------------------

    public static void printMonthlyBreakdown(List<Expense> expenses) {
        // Key: "YYYY-MM"
        Map<String, Double> monthly = new HashMap<>();
        for (Expense e : expenses) {
            String key = e.getDate().getYear() + "-" +
                    String.format("%02d", e.getDate().getMonthValue());
            monthly.merge(key, e.getAmount(), Double::sum);
        }

        System.out.println("  --- Monthly Breakdown ---");
        System.out.printf("  %-12s  %10s%n", "Month", "Total");
        System.out.println("  " + "-".repeat(28));

        List<String> keys = new ArrayList<>(monthly.keySet());
        java.util.Collections.sort(keys);
        for (String key : keys) {
            String[] parts = key.split("-");
            String label = Month.of(Integer.parseInt(parts[1])).name() + " " + parts[0];
            System.out.printf("  %-12s  Rs. %7.2f%n", label, monthly.get(key));
        }
        System.out.println();
    }

    // -------------------------
    //  TOP N EXPENSES
    // -------------------------

    public static void printTopExpenses(List<Expense> expenses, int n) {
        if (expenses.isEmpty()) return;

        List<Expense> sorted = new ArrayList<>(expenses);
        sorted.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        int limit = Math.min(n, sorted.size());
        System.out.println("  --- Top " + limit + " Expenses ---");
        for (int i = 0; i < limit; i++) {
            System.out.printf("  %d. %s%n", i + 1, sorted.get(i));
        }
        System.out.println();
    }

    // -------------------------
    //  BAR CHART (ASCII)
    // -------------------------

    public static void printCategoryBarChart(ExpenseManager manager) {
        Map<String, Double> totals = manager.getCategoryTotals();
        if (totals.isEmpty()) return;

        double max = 0;
        for (double v : totals.values()) if (v > max) max = v;

        System.out.println("  --- Category Bar Chart ---");
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            int bars = (max > 0) ? (int) (entry.getValue() / max * BAR_WIDTH) : 0;
            String bar = "#".repeat(bars);
            System.out.printf("  %-12s | %-30s Rs. %.2f%n",
                    entry.getKey(), bar, entry.getValue());
        }
        System.out.println();
    }
}

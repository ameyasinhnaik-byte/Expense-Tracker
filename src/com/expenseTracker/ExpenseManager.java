package com.expenseTracker;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseManager {

    private ArrayList<Expense> expenses = new ArrayList<>();
    private int nextId = 1;
    private Map<String, Double> budgets = new HashMap<>();

    // -------------------------
    //  ADD
    // -------------------------

    public void addExpense(double amount, String category, String description) {
        String cat = normalizeCategory(category);
        Expense expense = new Expense(nextId++, amount, cat, LocalDate.now(), description);
        expenses.add(expense);
        System.out.println("  Expense added! [#" + expense.getId() + "]");

        checkBudget(cat);
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
    //  DELETE / EDIT / CLEAR
    // -------------------------

    public void deleteExpense(int id) {
        Expense target = findById(id);
        if (target == null) {
            System.out.println("  No expense found with ID #" + id);
            return;
        }
        expenses.remove(target);
        System.out.println("  Deleted: " + target);
    }

    public void editExpense(int id, double newAmount, String newCategory, String newDescription) {
        Expense target = findById(id);
        if (target == null) {
            System.out.println("  No expense found with ID #" + id);
            return;
        }
        expenses.remove(target);
        String cat = normalizeCategory(newCategory);
        Expense updated = new Expense(id, newAmount, cat, target.getDate(), newDescription);
        expenses.add(updated);
        expenses.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        System.out.println("  Updated: " + updated);

        // FIX: also check budget after editing, not just after adding
        checkBudget(cat);
    }

    public void clearAll() {
        expenses.clear();
        nextId = 1;
        System.out.println("  All expenses cleared.");
    }

    // -------------------------
    //  BUDGET
    // -------------------------

    public void setBudget(String category, double limit) {
        String cat = normalizeCategory(category);
        budgets.put(cat, limit);
        System.out.printf("  Budget set: Rs. %.2f for %s%n", limit, cat);
    }

    public void showBudgets() {
        if (budgets.isEmpty()) {
            System.out.println("  No budgets set yet.");
            return;
        }
        System.out.println("\n  --- Budgets ---");
        for (Map.Entry<String, Double> entry : budgets.entrySet()) {
            String cat = entry.getKey();
            double limit = entry.getValue();
            double spent = getTotalByCategory(cat);
            double remaining = limit - spent;
            String status = remaining < 0 ? "!! OVER BUDGET" : "OK";
            System.out.printf("  %-15s  Spent: Rs. %8.2f  /  Limit: Rs. %.2f  [%s]%n",
                    cat, spent, limit, status);
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

    public double getTotalByCategory(String category) {
        String cat = normalizeCategory(category);
        double total = 0;
        for (Expense e : expenses) {
            if (e.getCategory().equalsIgnoreCase(cat)) total += e.getAmount();
        }
        return total;
    }

    public Map<String, Double> getBudgets() {
        return budgets;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public boolean isEmpty() {
        return expenses.isEmpty();
    }

    // NEW: called by Main.java to show "category not found" message
    public boolean categoryExists(String category) {
        String cat = normalizeCategory(category);
        for (Expense e : expenses) {
            if (e.getCategory().equalsIgnoreCase(cat)) return true;
        }
        return false;
    }

    // NEW: called by Main.java handleEdit() to validate ID before asking more questions
    public boolean expenseExists(int id) {
        return findById(id) != null;
    }

    // NEW: called by FileHandler after loading to fix nextId so IDs don't repeat after restart
    public void syncNextId() {
        int maxId = 0;
        for (Expense e : expenses) {
            if (e.getId() > maxId) maxId = e.getId();
        }
        nextId = maxId + 1;
    }

    // -------------------------
    //  PRIVATE UTILITIES
    // -------------------------

    // FIX: extracted budget check into one place — used by both addExpense and editExpense
    private void checkBudget(String cat) {
        if (budgets.containsKey(cat)) {
            double spent = getTotalByCategory(cat);
            double limit = budgets.get(cat);
            if (spent > limit) {
                System.out.printf("  !! WARNING: You've exceeded your %s budget! (Rs. %.2f / Rs. %.2f)%n",
                        cat, spent, limit);
            } else {
                System.out.printf("  Budget check: Rs. %.2f of Rs. %.2f used for %s.%n",
                        spent, limit, cat);
            }
        }
    }

    public static String normalizeCategory(String category) {
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

    private Expense findById(int id) {
        for (Expense e : expenses) {
            if (e.getId() == id) return e;
        }
        return null;
    }
}
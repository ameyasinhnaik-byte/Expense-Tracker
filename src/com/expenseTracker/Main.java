package com.expenseTracker;

import java.util.Scanner;

public class Main {

    private static ExpenseManager manager = new ExpenseManager();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("\n  ==============================");
        System.out.println("       EXPENSE TRACKER v2.0    ");
        System.out.println("  ==============================");

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            System.out.println();

            switch (choice) {
                case "1" -> handleAdd();
                case "2" -> manager.listExpenses();
                case "3" -> handleFilterByCategory();
                case "4" -> handleFilterByMonth();
                case "5" -> manager.listCategories();
                case "6"  -> handleDelete();
                case "7"  -> handleEdit();
                case "8"  -> handleClearAll();
                case "9"  -> ReportPrinter.printFullReport(manager);
                case "0"  -> { System.out.println("  Goodbye!"); return; }
                default   -> System.out.println("  Invalid choice. Please try again.");
            }

        }


    }
    // -------------------------
    //  MENU
    // -------------------------

    private static void printMenu() {
        System.out.println("  --- Main Menu ---");
        System.out.println("  1.  Add expense");
        System.out.println("  2.  View all expenses");
        System.out.println("  3.  Filter by category");
        System.out.println("  4.  Filter by month");
        System.out.println("  5.  List categories");
        System.out.println("  6.  Delete expense");
        System.out.println("  7.  Edit expense");
        System.out.println("  8.  Clear all expenses");
        System.out.println("  9.  View full report");
        System.out.println("  0.  Save & quit");
        System.out.print("  Enter choice: ");
    }


    // -------------------------
    //  HANDLERS
    // -------------------------

    private static void handleAdd() {
        double amount = promptDouble("  Amount (Rs.): ");
        if (amount < 0) return;

        System.out.print("  Category (e.g. Food, Travel, Shopping): ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) { System.out.println("  Category cannot be empty."); return; }

        System.out.print("  Description: ");
        String description = scanner.nextLine().trim();

        manager.addExpense(amount, category, description);
    }

    private static void handleFilterByCategory() {
        manager.listCategories();
        System.out.print("  Enter category name to filter: ");
        String cat = scanner.nextLine().trim();
        if (!cat.isEmpty()) manager.listByCategory(cat);
    }

    private static void handleFilterByMonth() {
        int month = promptInt("  Enter month (1-12): ");
        if (month < 1 || month > 12) { System.out.println("  Invalid month."); return; }

        int year = promptInt("  Enter year (e.g. 2025): ");
        if (year < 2000) { System.out.println("  Invalid year."); return; }

        manager.listByMonth(month, year);
    }

    private static void handleDelete() {
        manager.listExpenses();
        if (manager.isEmpty()) return;

        int id = promptInt("  Enter expense ID to delete: ");
        if (id < 0) return;

        System.out.print("  Are you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes") || confirm.equals("y")) {
            manager.deleteExpense(id);
        } else {
            System.out.println("  Cancelled.");
        }
    }

    private static void handleEdit() {
        manager.listExpenses();
        if (manager.isEmpty()) return;

        int id = promptInt("  Enter expense ID to edit: ");
        if (id < 0) return;

        double amount = promptDouble("  New amount (Rs.): ");
        if (amount < 0) return;

        System.out.print("  New category: ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) { System.out.println("  Category cannot be empty."); return; }

        System.out.print("  New description: ");
        String description = scanner.nextLine().trim();

        manager.editExpense(id, amount, category, description);
    }

    private static void handleClearAll() {
        if (manager.isEmpty()) { System.out.println("  Nothing to clear."); return; }
        System.out.print("  This will delete ALL expenses! Type 'CLEAR' to confirm: ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equals("CLEAR")) {
            manager.clearAll();
        } else {
            System.out.println("  Cancelled.");
        }
    }

    // -------------------------
    //  INPUT HELPERS
    // -------------------------

    private static double promptDouble(String prompt) {
        System.out.print(prompt);
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Invalid number.");
            return -1;
        }
    }

    private static int promptInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Invalid number.");
            return -1;
        }
    }
}

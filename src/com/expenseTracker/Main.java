package com.expenseTracker;

import java.util.Scanner;

public class Main {

    private static expenseManager manager = new expenseManager();
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

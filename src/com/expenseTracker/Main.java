package com.expenseTracker;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        expenseManager manager = new expenseManager();
        Scanner scanner = new Scanner(System.in);

        System.out.println("===== Expense Tracker =====");

        while (true) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("  1. Add expense");
            System.out.println("  2. View all expenses");
            System.out.println("  3. Quit");
            System.out.print("Enter choice (serial number): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Amount (Rs.): ");
                    double amount;
                    try {
                        amount = Double.parseDouble(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount. Please enter a number.");
                        break;
                    }

                    System.out.print("Category (e.g. Food, Travel, Shopping): ");
                    String category = scanner.nextLine().trim();

                    System.out.print("Description: ");
                    String description = scanner.nextLine().trim();

                    manager.addExpense(amount, category, description);
                    break;

                case "2":
                    manager.listExpenses();
                    break;

                case "3":
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }
}

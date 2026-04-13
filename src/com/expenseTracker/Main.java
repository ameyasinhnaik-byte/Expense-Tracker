package com.expenseTracker;

import javax.swing.*;

/**
 * Main
 * Entry point. Loads data and launches the Swing GUI.
 *
 * Branch: feature/swing-core
 */
public class Main {

    public static void main(String[] args) {
        // Always touch Swing components on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AppTheme.apply();

            ExpenseManager manager = new ExpenseManager();
            FileHandler.loadExpenses(manager);

            new MainWindow(manager);
        });
    }
}

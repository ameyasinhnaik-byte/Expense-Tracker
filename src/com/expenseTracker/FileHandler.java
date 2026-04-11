package com.expenseTracker;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

/**
 * FileHandler
 * Saves and loads expenses from a CSV file.
 * Format: id,amount,category,date,description
 */
public class FileHandler {

    private static final String EXPENSES_FILE = "expenses.csv";

    // -------------------------
    //  SAVE
    // -------------------------

    public static void saveExpenses(ExpenseManager manager) {
        List<Expense> expenses = manager.getExpenses();
        try (PrintWriter pw = new PrintWriter(new FileWriter(EXPENSES_FILE))) {
            pw.println("id,amount,category,date,description");
            for (Expense e : expenses) {
                // Escape commas in description by wrapping in quotes
                String desc = e.getDescription().replace("\"", "\"\"");
                pw.printf("%d,%.2f,%s,%s,\"%s\"%n",
                        e.getId(), e.getAmount(), e.getCategory(), e.getDate(), desc);
            }
            System.out.println("  Expenses saved to " + EXPENSES_FILE);
        } catch (IOException ex) {
            System.out.println("  ERROR saving expenses: " + ex.getMessage());
        }

    }

    // -------------------------
    //  LOAD
    // -------------------------

    public static void loadExpenses(ExpenseManager manager) {
        File file = new File(EXPENSES_FILE);
        if (!file.exists()) return; // First run, nothing to load

        int loaded = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Handle quoted description field
                String[] parts = parseCSVLine(line);
                if (parts.length < 5) continue;

                try {
                    int id         = Integer.parseInt(parts[0].trim());
                    double amount  = Double.parseDouble(parts[1].trim());
                    String cat     = parts[2].trim();
                    LocalDate date = LocalDate.parse(parts[3].trim());
                    String desc    = parts[4].trim().replaceAll("^\"|\"$", "")
                            .replace("\"\"", "\"");

                    // Inject directly — bypass addExpense to avoid side effects
                    manager.getExpenses().add(new Expense(id, amount, cat, date, desc));
                    loaded++;
                } catch (Exception ex) {
                    System.out.println("  Skipping malformed line: " + line);
                }
            }
            if (loaded > 0)
                System.out.println("  Loaded " + loaded + " expense(s) from " + EXPENSES_FILE);
        } catch (IOException ex) {
            System.out.println("  ERROR loading expenses: " + ex.getMessage());
        }

    }

    // -------------------------
    //  SIMPLE CSV PARSER
    // -------------------------

    private static String[] parseCSVLine(String line) {
        // Handles one quoted field at the end (description)
        int quoteStart = line.indexOf('"');
        if (quoteStart == -1) return line.split(",", 5);

        String[] pre = line.substring(0, quoteStart).split(",");
        String quoted = line.substring(quoteStart); // includes the quotes
        String[] result = new String[pre.length + 1];
        System.arraycopy(pre, 0, result, 0, pre.length);
        result[pre.length] = quoted;
        return result;
    }
}
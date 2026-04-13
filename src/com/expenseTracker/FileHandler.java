package com.expenseTracker;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * FileHandler
 * Saves and loads expenses + budgets from a CSV file.
 * Format: id,amount,category,date,description
 */
public class FileHandler {

    private static final String EXPENSES_FILE = "expenses.csv";
    private static final String BUDGETS_FILE  = "budgets.csv";

    // -------------------------
    //  SAVE
    // -------------------------

    public static void saveExpenses(ExpenseManager manager) {
        List<Expense> expenses = manager.getExpenses();
        try (PrintWriter pw = new PrintWriter(new FileWriter(EXPENSES_FILE))) {
            pw.println("id,amount,category,date,description");
            for (Expense e : expenses) {
                String desc = e.getDescription().replace("\"", "\"\"");
                pw.printf("%d,%.2f,%s,%s,\"%s\"%n",
                        e.getId(), e.getAmount(), e.getCategory(), e.getDate(), desc);
            }
            System.out.println("  Expenses saved to " + EXPENSES_FILE);
        } catch (IOException ex) {
            System.out.println("  ERROR saving expenses: " + ex.getMessage());
        }

        Map<String, Double> budgets = manager.getBudgets();
        if (!budgets.isEmpty()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(BUDGETS_FILE))) {
                pw.println("category,limit");
                for (Map.Entry<String, Double> entry : budgets.entrySet()) {
                    pw.printf("%s,%.2f%n", entry.getKey(), entry.getValue());
                }
                System.out.println("  Budgets saved to " + BUDGETS_FILE);
            } catch (IOException ex) {
                System.out.println("  ERROR saving budgets: " + ex.getMessage());
            }
        }
    }

    // -------------------------
    //  LOAD
    // -------------------------

    public static void loadExpenses(ExpenseManager manager) {
        File file = new File(EXPENSES_FILE);
        if (!file.exists()) return;

        int loaded = 0;
        int skipped = 0; // FIX: track skipped lines and report at end

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = parseCSVLine(line);
                if (parts.length < 5) { skipped++; continue; }

                try {
                    int id         = Integer.parseInt(parts[0].trim());
                    double amount  = Double.parseDouble(parts[1].trim());
                    String cat     = parts[2].trim();
                    LocalDate date = LocalDate.parse(parts[3].trim());
                    String desc    = parts[4].trim().replaceAll("^\"|\"$", "")
                            .replace("\"\"", "\"");

                    manager.getExpenses().add(new Expense(id, amount, cat, date, desc));
                    loaded++;
                } catch (Exception ex) {
                    skipped++;
                    System.out.println("  Skipping malformed line: " + line);
                }
            }

            if (loaded > 0)
                System.out.println("  Loaded " + loaded + " expense(s) from " + EXPENSES_FILE);

            // FIX: report how many lines were skipped so user knows something went wrong
            if (skipped > 0)
                System.out.println("  Warning: " + skipped + " line(s) skipped due to bad format.");

        } catch (IOException ex) {
            System.out.println("  ERROR loading expenses: " + ex.getMessage());
        }

        // FIX: sync nextId so new expenses don't reuse IDs from the loaded data
        manager.syncNextId();

        // Load budgets
        File budgetFile = new File(BUDGETS_FILE);
        if (budgetFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(budgetFile))) {
                br.readLine(); // skip header
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length < 2) continue;
                    // Use normalizeCategory to keep casing consistent with expense data
                    String cat = ExpenseManager.normalizeCategory(parts[0].trim());
                    manager.setBudget(cat, Double.parseDouble(parts[1].trim()));
                }
            } catch (IOException ex) {
                System.out.println("  ERROR loading budgets: " + ex.getMessage());
            }
        }
    }

    // -------------------------
    //  SIMPLE CSV PARSER
    // -------------------------

    private static String[] parseCSVLine(String line) {
        int quoteStart = line.indexOf('"');
        if (quoteStart == -1) return line.split(",", 5);

        String[] pre = line.substring(0, quoteStart).split(",");
        String quoted = line.substring(quoteStart);
        String[] result = new String[pre.length + 1];
        System.arraycopy(pre, 0, result, 0, pre.length);
        result[pre.length] = quoted;
        return result;
    }
}

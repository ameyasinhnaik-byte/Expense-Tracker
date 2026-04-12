package com.expenseTracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class ExpenseTrackerGUI extends JFrame {

    private ExpenseManager manager = new ExpenseManager();
    private FileHandler fileHandler = new FileHandler();
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    // Input fields
    private JTextField amountField, descField, categoryField;
    private JTextField budgetCatField, budgetAmtField;
    private JTextField filterCatField, filterMonthField, filterYearField;
    private JTextField editIdField, editAmtField, editCatField, editDescField;

    public ExpenseTrackerGUI() {
        setTitle("Expense Tracker");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Load saved expenses
        fileHandler.loadFromFile(manager);

        // --- TOP: Title ---
        JLabel title = new JLabel("  Expense Tracker", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setOpaque(true);
        title.setBackground(new Color(33, 97, 140));
        title.setForeground(Color.WHITE);
        title.setPreferredSize(new Dimension(900, 50));
        add(title, BorderLayout.NORTH);

        // --- LEFT: Input Panel with Tabs ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(300, 500));
        tabs.addTab("Add", buildAddPanel());
        tabs.addTab("Edit", buildEditPanel());
        tabs.addTab("Budget", buildBudgetPanel());
        tabs.addTab("Filter", buildFilterPanel());
        add(tabs, BorderLayout.WEST);

        // --- CENTER: Table ---
        String[] columns = {"ID", "Category", "Amount (Rs.)", "Date", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(33, 97, 140));
        table.getTableHeader().setForeground(Color.WHITE);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- BOTTOM: Total + Buttons ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(230, 230, 230));

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setBackground(new Color(192, 57, 43));
        deleteBtn.setForeground(Color.WHITE);

        JButton clearBtn = new JButton("Clear All");
        clearBtn.setBackground(new Color(120, 120, 120));
        clearBtn.setForeground(Color.WHITE);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(39, 174, 96));
        refreshBtn.setForeground(Color.WHITE);

        totalLabel = new JLabel("Total: Rs. 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        bottom.add(refreshBtn);
        bottom.add(deleteBtn);
        bottom.add(clearBtn);
        bottom.add(Box.createHorizontalStrut(20));
        bottom.add(totalLabel);
        add(bottom, BorderLayout.SOUTH);

        // --- BUTTON ACTIONS ---
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a row to delete!");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            manager.deleteExpense(id);
            fileHandler.saveToFile(manager);
            refreshTable();
        });

        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Clear all expenses?");
            if (confirm == JOptionPane.YES_OPTION) {
                manager.clearAll();
                fileHandler.saveToFile(manager);
                refreshTable();
            }
        });

        refreshBtn.addActionListener(e -> refreshTable());

        refreshTable();
        setVisible(true);
    }

    // --- ADD PANEL ---
    private JPanel buildAddPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        amountField = new JTextField();
        categoryField = new JTextField();
        descField = new JTextField();

        panel.add(new JLabel("Amount (Rs.):"));
        panel.add(amountField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);

        JButton addBtn = new JButton("Add Expense");
        addBtn.setBackground(new Color(33, 97, 140));
        addBtn.setForeground(Color.WHITE);
        panel.add(new JLabel());
        panel.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String category = categoryField.getText().trim();
                String desc = descField.getText().trim();

                if (category.isEmpty() || desc.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Fill all fields!");
                    return;
                }

                manager.addExpense(amount, category, desc);
                fileHandler.saveToFile(manager);
                refreshTable();
                amountField.setText("");
                categoryField.setText("");
                descField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid amount!");
            }
        });

        return panel;
    }

    // --- EDIT PANEL ---
    private JPanel buildEditPanel() {
        JPanel panel = new JPanel(new GridLayout(10, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        editIdField = new JTextField();
        editAmtField = new JTextField();
        editCatField = new JTextField();
        editDescField = new JTextField();

        panel.add(new JLabel("Expense ID:"));
        panel.add(editIdField);
        panel.add(new JLabel("New Amount:"));
        panel.add(editAmtField);
        panel.add(new JLabel("New Category:"));
        panel.add(editCatField);
        panel.add(new JLabel("New Description:"));
        panel.add(editDescField);

        JButton editBtn = new JButton("Update Expense");
        editBtn.setBackground(new Color(33, 97, 140));
        editBtn.setForeground(Color.WHITE);
        panel.add(new JLabel());
        panel.add(editBtn);

        editBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(editIdField.getText().trim());
                double amt = Double.parseDouble(editAmtField.getText().trim());
                String cat = editCatField.getText().trim();
                String desc = editDescField.getText().trim();
                manager.editExpense(id, amt, cat, desc);
                fileHandler.saveToFile(manager);
                refreshTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid ID and amount!");
            }
        });

        return panel;
    }

    // --- BUDGET PANEL ---
    private JPanel buildBudgetPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        budgetCatField = new JTextField();
        budgetAmtField = new JTextField();

        panel.add(new JLabel("Category:"));
        panel.add(budgetCatField);
        panel.add(new JLabel("Budget Limit (Rs.):"));
        panel.add(budgetAmtField);

        JButton setBudgetBtn = new JButton("Set Budget");
        setBudgetBtn.setBackground(new Color(33, 97, 140));
        setBudgetBtn.setForeground(Color.WHITE);

        JButton showBudgetBtn = new JButton("Show Budgets");
        showBudgetBtn.setBackground(new Color(39, 174, 96));
        showBudgetBtn.setForeground(Color.WHITE);

        panel.add(setBudgetBtn);
        panel.add(showBudgetBtn);

        setBudgetBtn.addActionListener(e -> {
            try {
                String cat = budgetCatField.getText().trim();
                double limit = Double.parseDouble(budgetAmtField.getText().trim());
                manager.setBudget(cat, limit);
                JOptionPane.showMessageDialog(this, "Budget set for " + cat);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid amount!");
            }
        });

        showBudgetBtn.addActionListener(e -> {
            Map<String, Double> budgets = manager.getBudgets();
            if (budgets.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No budgets set yet.");
                return;
            }
            StringBuilder sb = new StringBuilder("--- Budgets ---\n");
            for (Map.Entry<String, Double> entry : budgets.entrySet()) {
                String cat = entry.getKey();
                double limit = entry.getValue();
                double spent = manager.getTotalByCategory(cat);
                String status = spent > limit ? "OVER BUDGET!" : "OK";
                sb.append(String.format("%-15s Rs. %.2f / Rs. %.2f  [%s]\n", cat, spent, limit, status));
            }
            JOptionPane.showMessageDialog(this, sb.toString());
        });

        return panel;
    }

    // --- FILTER PANEL ---
    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        filterCatField = new JTextField();
        filterMonthField = new JTextField();
        filterYearField = new JTextField();

        panel.add(new JLabel("Filter by Category:"));
        panel.add(filterCatField);

        JButton filterCatBtn = new JButton("Filter");
        filterCatBtn.setBackground(new Color(33, 97, 140));
        filterCatBtn.setForeground(Color.WHITE);
        panel.add(filterCatBtn);

        panel.add(new JLabel("Month (1-12):"));
        panel.add(filterMonthField);
        panel.add(new JLabel("Year (e.g. 2026):"));
        panel.add(filterYearField);

        JButton filterMonthBtn = new JButton("Filter by Month");
        filterMonthBtn.setBackground(new Color(33, 97, 140));
        filterMonthBtn.setForeground(Color.WHITE);
        panel.add(filterMonthBtn);

        filterCatBtn.addActionListener(e -> {
            String cat = filterCatField.getText().trim();
            if (cat.isEmpty()) { refreshTable(); return; }
            tableModel.setRowCount(0);
            double total = 0;
            for (Expense ex : manager.getExpenses()) {
                if (ex.getCategory().equalsIgnoreCase(cat)) {
                    tableModel.addRow(new Object[]{
                        ex.getId(), ex.getCategory(),
                        String.format("%.2f", ex.getAmount()),
                        ex.getDate(), ex.getDescription()
                    });
                    total += ex.getAmount();
                }
            }
            totalLabel.setText("Total: Rs. " + String.format("%.2f", total));
        });

        filterMonthBtn.addActionListener(e -> {
            try {
                int month = Integer.parseInt(filterMonthField.getText().trim());
                int year = Integer.parseInt(filterYearField.getText().trim());
                tableModel.setRowCount(0);
                double total = 0;
                for (Expense ex : manager.getExpenses()) {
                    if (ex.getDate().getMonthValue() == month && ex.getDate().getYear() == year) {
                        tableModel.addRow(new Object[]{
                            ex.getId(), ex.getCategory(),
                            String.format("%.2f", ex.getAmount()),
                            ex.getDate(), ex.getDescription()
                        });
                        total += ex.getAmount();
                    }
                }
                totalLabel.setText("Total: Rs. " + String.format("%.2f", total));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid month and year!");
            }
        });

        return panel;
    }

    // --- REFRESH TABLE ---
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Expense e : manager.getExpenses()) {
            tableModel.addRow(new Object[]{
                e.getId(), e.getCategory(),
                String.format("%.2f", e.getAmount()),
                e.getDate(), e.getDescription()
            });
        }
        totalLabel.setText("Total: Rs. " + String.format("%.2f", manager.getTotal()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerGUI::new);
    }
}
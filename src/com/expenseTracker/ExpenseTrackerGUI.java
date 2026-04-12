package com.expenseTracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;

public class ExpenseTrackerGUI extends JFrame {

    private ExpenseManager manager = new ExpenseManager();
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    // Colors - Gen Z Palette
    private static final Color BG_DARK = new Color(15, 15, 25);
    private static final Color CARD_BG = new Color(25, 25, 40);
    private static final Color PURPLE = new Color(139, 92, 246);
    private static final Color PINK = new Color(236, 72, 153);
    private static final Color CYAN = new Color(34, 211, 238);
    private static final Color GREEN = new Color(52, 211, 153);
    private static final Color RED = new Color(248, 113, 113);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color MUTED = new Color(100, 116, 139);

    // Input fields
    private JTextField amountField, descField, categoryField;
    private JTextField budgetCatField, budgetAmtField;
    private JTextField filterCatField, filterMonthField, filterYearField;
    private JTextField editIdField, editAmtField, editCatField, editDescField;

    public ExpenseTrackerGUI() {
        setTitle("Expense Tracker");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BG_DARK);

        FileHandler.loadExpenses(manager);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_DARK);
        setContentPane(mainPanel);

        mainPanel.add(buildHeader(), BorderLayout.NORTH);
        mainPanel.add(buildSidebar(), BorderLayout.WEST);
        mainPanel.add(buildTablePanel(), BorderLayout.CENTER);
        mainPanel.add(buildBottomBar(), BorderLayout.SOUTH);

        refreshTable();
        setVisible(true);
    }

    // --- HEADER ---
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setPreferredSize(new Dimension(1000, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PURPLE));

        JLabel logo = new JLabel("  Expense Tracker");
        logo.setFont(new Font("Arial", Font.BOLD, 18));
        logo.setForeground(TEXT);

        JLabel sub = new JLabel("Manage your expenses  ");
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(MUTED);

        header.add(logo, BorderLayout.WEST);
        header.add(sub, BorderLayout.EAST);
        return header;
    }

    // --- SIDEBAR ---
    private JTabbedPane buildSidebar() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(280, 500));
        tabs.setBackground(CARD_BG);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("Arial", Font.BOLD, 12));

        tabs.addTab("Add", buildAddPanel());
        tabs.addTab("Edit", buildEditPanel());
        tabs.addTab("Budget", buildBudgetPanel());
        tabs.addTab("Filter", buildFilterPanel());

        return tabs;
    }

    // --- TABLE PANEL ---
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Category", "Amount (Rs.)", "Date", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 4));
        table.setSelectionBackground(PURPLE);
        table.setSelectionForeground(Color.WHITE);

        table.getTableHeader().setBackground(new Color(30, 30, 50));
        table.getTableHeader().setForeground(CYAN);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                if (isSelected) {
                    setBackground(PURPLE);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? CARD_BG : new Color(30, 30, 50));
                    setForeground(col == 2 ? GREEN : TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(PURPLE, 1));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // --- BOTTOM BAR ---
    private JPanel buildBottomBar() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottom.setBackground(CARD_BG);
        bottom.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, PURPLE));

        JButton refreshBtn = makeButton("Refresh", CYAN);
        JButton deleteBtn = makeButton("Delete", RED);
        JButton clearBtn = makeButton("Clear All", PINK);

        totalLabel = new JLabel("Total: Rs. 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(GREEN);

        bottom.add(refreshBtn);
        bottom.add(deleteBtn);
        bottom.add(clearBtn);
        bottom.add(Box.createHorizontalStrut(20));
        bottom.add(totalLabel);

        refreshBtn.addActionListener(e -> refreshTable());

        deleteBtn.addActionListener(e -> {
            Component center = ((BorderLayout) getContentPane().getLayout())
                    .getLayoutComponent(BorderLayout.CENTER);
            if (center instanceof JPanel) {
                JScrollPane scroll = (JScrollPane) ((JPanel) center).getComponent(0);
                JTable table = (JTable) scroll.getViewport().getView();
                int row = table.getSelectedRow();
                if (row == -1) {
                    showMsg("Please select a row to delete!", "Error");
                    return;
                }
                int id = (int) tableModel.getValueAt(row, 0);
                manager.deleteExpense(id);
                FileHandler.saveExpenses(manager);
                refreshTable();
            }
        });

        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to clear all expenses?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                manager.clearAll();
                FileHandler.saveExpenses(manager);
                refreshTable();
            }
        });

        return bottom;
    }

    // --- ADD PANEL ---
    private JPanel buildAddPanel() {
        JPanel panel = makeCardPanel();
        panel.setLayout(new GridLayout(9, 1, 5, 8));

        amountField = makeField("e.g. 299");
        categoryField = makeField("e.g. Food");
        descField = makeField("e.g. Lunch");

        panel.add(makeLabel("Amount (Rs.)"));
        panel.add(amountField);
        panel.add(makeLabel("Category"));
        panel.add(categoryField);
        panel.add(makeLabel("Description"));
        panel.add(descField);
        panel.add(new JLabel());

        JButton addBtn = makeButton("Add Expense", PURPLE);
        panel.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                String category = categoryField.getText().trim();
                String desc = descField.getText().trim();
                if (category.isEmpty() || desc.isEmpty()) {
                    showMsg("Please fill all fields!", "Incomplete");
                    return;
                }
                manager.addExpense(amount, category, desc);
                FileHandler.saveExpenses(manager);
                refreshTable();
                amountField.setText("");
                categoryField.setText("");
                descField.setText("");
                showMsg("Expense added successfully!", "Done");
            } catch (NumberFormatException ex) {
                showMsg("Please enter a valid number!", "Error");
            }
        });

        return panel;
    }

    // --- EDIT PANEL ---
    private JPanel buildEditPanel() {
        JPanel panel = makeCardPanel();
        panel.setLayout(new GridLayout(11, 1, 5, 8));

        editIdField = makeField("Expense ID");
        editAmtField = makeField("New Amount");
        editCatField = makeField("New Category");
        editDescField = makeField("New Description");

        panel.add(makeLabel("Expense ID"));
        panel.add(editIdField);
        panel.add(makeLabel("New Amount"));
        panel.add(editAmtField);
        panel.add(makeLabel("New Category"));
        panel.add(editCatField);
        panel.add(makeLabel("New Description"));
        panel.add(editDescField);
        panel.add(new JLabel());

        JButton editBtn = makeButton("Update Expense", CYAN);
        panel.add(editBtn);

        editBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(editIdField.getText().trim());
                double amt = Double.parseDouble(editAmtField.getText().trim());
                String cat = editCatField.getText().trim();
                String desc = editDescField.getText().trim();
                manager.editExpense(id, amt, cat, desc);
                FileHandler.saveExpenses(manager);
                refreshTable();
                showMsg("Expense updated successfully!", "Done");
            } catch (NumberFormatException ex) {
                showMsg("Please enter valid ID and amount!", "Error");
            }
        });

        return panel;
    }

    // --- BUDGET PANEL ---
    private JPanel buildBudgetPanel() {
        JPanel panel = makeCardPanel();
        panel.setLayout(new GridLayout(7, 1, 5, 8));

        budgetCatField = makeField("e.g. Food");
        budgetAmtField = makeField("e.g. 5000");

        panel.add(makeLabel("Category"));
        panel.add(budgetCatField);
        panel.add(makeLabel("Budget Limit (Rs.)"));
        panel.add(budgetAmtField);
        panel.add(new JLabel());

        JButton setBudgetBtn = makeButton("Set Budget", PURPLE);
        JButton showBudgetBtn = makeButton("View Budgets", GREEN);

        panel.add(setBudgetBtn);
        panel.add(showBudgetBtn);

        setBudgetBtn.addActionListener(e -> {
            try {
                String cat = budgetCatField.getText().trim();
                double limit = Double.parseDouble(budgetAmtField.getText().trim());
                manager.setBudget(cat, limit);
                showMsg("Budget set for " + cat, "Done");
            } catch (NumberFormatException ex) {
                showMsg("Please enter a valid amount!", "Error");
            }
        });

        showBudgetBtn.addActionListener(e -> {
            Map<String, Double> budgets = manager.getBudgets();
            if (budgets.isEmpty()) {
                showMsg("No budgets set yet.", "Info");
                return;
            }
            StringBuilder sb = new StringBuilder("Budget Report\n\n");
            for (Map.Entry<String, Double> entry : budgets.entrySet()) {
                String cat = entry.getKey();
                double limit = entry.getValue();
                double spent = manager.getTotalByCategory(cat);
                String status = spent > limit ? "OVER BUDGET" : "OK";
                sb.append(String.format("%-15s Rs.%.2f / Rs.%.2f  [%s]\n",
                        cat, spent, limit, status));
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Budget Report",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    // --- FILTER PANEL ---
    private JPanel buildFilterPanel() {
        JPanel panel = makeCardPanel();
        panel.setLayout(new GridLayout(9, 1, 5, 8));

        filterCatField = makeField("e.g. Food");
        filterMonthField = makeField("1-12");
        filterYearField = makeField("e.g. 2026");

        panel.add(makeLabel("Filter by Category"));
        panel.add(filterCatField);

        JButton filterCatBtn = makeButton("Filter", CYAN);
        panel.add(filterCatBtn);

        panel.add(makeLabel("Month (1-12)"));
        panel.add(filterMonthField);
        panel.add(makeLabel("Year"));
        panel.add(filterYearField);

        JButton filterMonthBtn = makeButton("Filter by Month", PINK);
        panel.add(filterMonthBtn);

        JButton resetBtn = makeButton("Show All", MUTED);
        panel.add(resetBtn);

        filterCatBtn.addActionListener(e -> {
            String cat = filterCatField.getText().trim();
            if (cat.isEmpty()) { refreshTable(); return; }
            tableModel.setRowCount(0);
            double total = 0;
            for (Expense ex : manager.getExpenses()) {
                if (ex.getCategory().equalsIgnoreCase(cat)) {
                    tableModel.addRow(new Object[]{ex.getId(), ex.getCategory(),
                            String.format("%.2f", ex.getAmount()), ex.getDate(), ex.getDescription()});
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
                        tableModel.addRow(new Object[]{ex.getId(), ex.getCategory(),
                                String.format("%.2f", ex.getAmount()), ex.getDate(), ex.getDescription()});
                        total += ex.getAmount();
                    }
                }
                totalLabel.setText("Total: Rs. " + String.format("%.2f", total));
            } catch (NumberFormatException ex) {
                showMsg("Please enter valid month and year!", "Error");
            }
        });

        resetBtn.addActionListener(e -> refreshTable());

        return panel;
    }

    // --- REFRESH TABLE ---
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Expense e : manager.getExpenses()) {
            tableModel.addRow(new Object[]{e.getId(), e.getCategory(),
                    String.format("%.2f", e.getAmount()), e.getDate(), e.getDescription()});
        }
        totalLabel.setText("Total: Rs. " + String.format("%.2f", manager.getTotal()));
    }

    // --- HELPERS ---
    private JPanel makeCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }

    private JTextField makeField(String placeholder) {
        JTextField field = new JTextField();
        field.setBackground(new Color(30, 30, 50));
        field.setForeground(TEXT);
        field.setCaretColor(PURPLE);
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PURPLE, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return field;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showMsg(String msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerGUI::new);
    }
}
package com.expenseTracker;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/**
 * ExpensePanel
 * Full expenses table with search/filter bar, add, edit, delete.
 *
 * Branch: feature/swing-panels
 */
public class ExpensePanel extends JPanel implements MainWindow.Refreshable {

    private final ExpenseManager manager;
    private final MainWindow window;

    private JTable table;
    private ExpenseTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;

    public ExpensePanel(ExpenseManager manager, MainWindow window) {
        this.manager = manager;
        this.window  = window;
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        build();
    }

    private void build() {
        // ── Header ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Expenses");
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JButton addBtn = AppTheme.primaryButton("+ Add Expense", AppTheme.ACCENT_GREEN);
        addBtn.addActionListener(e -> showAddDialog());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(addBtn);
        header.add(btnRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Filter bar ──────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterBar.setOpaque(false);

        searchField = AppTheme.inputField("Search description...");
        searchField.setPreferredSize(new Dimension(220, AppTheme.INPUT_HEIGHT));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(AppTheme.FONT_BODY);
        categoryFilter.setBackground(AppTheme.BG_INPUT);
        categoryFilter.setForeground(AppTheme.TEXT_PRIMARY);
        categoryFilter.setPreferredSize(new Dimension(160, AppTheme.INPUT_HEIGHT));
        categoryFilter.addActionListener(e -> applyFilter());

        statusLabel = AppTheme.subLabel("0 entries");

        filterBar.add(AppTheme.subLabel("Search:"));
        filterBar.add(searchField);
        filterBar.add(AppTheme.subLabel("Category:"));
        filterBar.add(categoryFilter);
        filterBar.add(statusLabel);
        add(filterBar, BorderLayout.CENTER); // temp, replaced below

        // ── Table ───────────────────────────────────────────────
            class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = AppTheme.ACCENT_BLUE; // scrollbar handle
            this.trackColor = AppTheme.BG_PANEL;     // background track
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            return btn;
        }
    }

        tableModel = new ExpenseTableModel();
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(AppTheme.BG_PANEL);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // ── Action buttons row ───────────────────────────────────
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actionRow.setOpaque(false);

        JButton editBtn   = AppTheme.ghostButton("✎  Edit");
        JButton deleteBtn = AppTheme.primaryButton("✕  Delete", AppTheme.ACCENT_RED);

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { toast("Select an expense first."); return; }
            showEditDialog(tableModel.getExpense(row));
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { toast("Select an expense first."); return; }
            confirmDelete(tableModel.getExpense(row));
        });

        JButton clearAllBtn = AppTheme.ghostButton("  Clear All");
            clearAllBtn.setForeground(AppTheme.ACCENT_RED);
                clearAllBtn.addActionListener(e -> {
                    if (manager.isEmpty()) { toast("No expenses to clear."); return; }
                    int choice = JOptionPane.showConfirmDialog(this,
                        "Delete ALL expenses? This cannot be undone.",
                        "Clear All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        manager.clearAll();
                        refresh();
            }
        });
        actionRow.add(clearAllBtn);
        actionRow.add(editBtn);
        actionRow.add(deleteBtn);

        // ── Assemble ────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.add(filterBar, BorderLayout.NORTH);
        center.add(scroll,    BorderLayout.CENTER);
        center.add(actionRow, BorderLayout.SOUTH);

        remove(filterBar); // remove temp, add proper layout
        add(center, BorderLayout.CENTER);
    }

    // ─── TABLE STYLING ──────────────────────────────────────────

    private void styleTable() {
        table.setBackground(AppTheme.BG_PANEL);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setFont(AppTheme.FONT_BODY);
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(AppTheme.BG_HOVER);
        table.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setBackground(AppTheme.BG_DARK);
        header.setForeground(AppTheme.TEXT_SECONDARY);
        header.setFont(AppTheme.FONT_LABEL);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_COLOR));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setBackground(AppTheme.BG_DARK);
                lbl.setForeground(AppTheme.TEXT_SECONDARY);
                lbl.setFont(AppTheme.FONT_HEADING);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                lbl.setOpaque(true);
                return lbl;
            }
        });

        // Column widths
        int[] widths = { 50, 100, 120, 100, 300 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Custom cell renderer: alternating rows + colored amount + badge category
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                lbl.setBackground(sel ? AppTheme.BG_HOVER :
                                  (row % 2 == 0 ? AppTheme.BG_PANEL : AppTheme.BG_ROW_ALT));
                lbl.setOpaque(true);
                lbl.setFont(AppTheme.FONT_BODY);
                lbl.setForeground(AppTheme.TEXT_PRIMARY);

                if (col == 2) { // Amount column
                    lbl.setForeground(AppTheme.ACCENT_GREEN);
                    lbl.setFont(AppTheme.FONT_LABEL);
                }
                if (col == 1) { // Category column
                    lbl.setForeground(AppTheme.ACCENT_PURPLE);
                }
                return lbl;
            }
        });
    }

    // ─── FILTER ─────────────────────────────────────────────────

    private void applyFilter() {
        String search = searchField.getText().trim().toLowerCase();
        String cat    = (String) categoryFilter.getSelectedItem();
        boolean allCats = cat == null || cat.equals("All");

        List<Expense> filtered = new ArrayList<>();
        for (Expense e : manager.getExpenses()) {
            boolean matchSearch = search.isEmpty() ||
                e.getDescription().toLowerCase().contains(search) ||
                e.getCategory().toLowerCase().contains(search);
            boolean matchCat = allCats || e.getCategory().equalsIgnoreCase(cat);
            if (matchSearch && matchCat) filtered.add(e);
        }
        tableModel.setData(filtered);
        statusLabel.setText(filtered.size() + " entries");
    }

    // ─── ADD DIALOG ─────────────────────────────────────────────

    public void showAddDialog() {
        JDialog dlg = styledDialog("Add Expense", 420, 320);
        JPanel form = formPanel();

        JTextField amtField  = AppTheme.inputField("e.g. 500");
        JTextField catField  = AppTheme.inputField("e.g. Food");
        JTextField descField = AppTheme.inputField("e.g. Lunch at cafe");

        addFormRow(form, "Amount (Rs.)", amtField);
        addFormRow(form, "Category",    catField);
        addFormRow(form, "Description", descField);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton cancel = AppTheme.ghostButton("Cancel");
        JButton save   = AppTheme.primaryButton("Add", AppTheme.ACCENT_GREEN);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                String cat = catField.getText().trim();
                String desc = descField.getText().trim();
                if (cat.isEmpty()) { toast("Category cannot be empty."); return; }
                manager.addExpense(amt, cat, desc);
                refresh();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                toast("Invalid amount.");
            }
        });
        btns.add(cancel); btns.add(save);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ─── EDIT DIALOG ────────────────────────────────────────────

    private void showEditDialog(Expense e) {
        JDialog dlg = styledDialog("Edit Expense #" + e.getId(), 420, 320);
        JPanel form = formPanel();

        JTextField amtField  = AppTheme.inputField("Amount");
        JTextField catField  = AppTheme.inputField("Category");
        JTextField descField = AppTheme.inputField("Description");

        amtField.setText(String.valueOf(e.getAmount()));
        catField.setText(e.getCategory());
        descField.setText(e.getDescription());

        addFormRow(form, "Amount (Rs.)", amtField);
        addFormRow(form, "Category",    catField);
        addFormRow(form, "Description", descField);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton cancel = AppTheme.ghostButton("Cancel");
        JButton save   = AppTheme.primaryButton("Save", AppTheme.ACCENT_BLUE);

        cancel.addActionListener(ev -> dlg.dispose());
        save.addActionListener(ev -> {
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                String cat = catField.getText().trim();
                String desc = descField.getText().trim();
                if (cat.isEmpty()) { toast("Category cannot be empty."); return; }
                manager.editExpense(e.getId(), amt, cat, desc);
                refresh();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                toast("Invalid amount.");
            }
        });
        btns.add(cancel); btns.add(save);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ─── DELETE ─────────────────────────────────────────────────

    private void confirmDelete(Expense e) {
        int choice = JOptionPane.showConfirmDialog(this,
            "Delete expense \"" + e.getDescription() + "\" (₹ " +
                String.format("%.2f", e.getAmount()) + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            manager.deleteExpense(e.getId());
            refresh();
        }
    }

    // ─── DIALOG / FORM HELPERS ──────────────────────────────────

    private JDialog styledDialog(String title, int w, int h) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dlg.setSize(w, h);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(AppTheme.BG_PANEL);
        dlg.setLayout(new BorderLayout(0, 0));
        dlg.setBackground(AppTheme.BG_PANEL);
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 14, 18, 14));
        return dlg;
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }

    private void addFormRow(JPanel form, String label, JComponent field) {
        GridBagConstraints g = new GridBagConstraints();
        int row = form.getComponentCount() / 2;
        g.gridy = row; g.insets = new Insets(8, 0, 0, 12); g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        JLabel lbl = AppTheme.subLabel(label);
        form.add(lbl, g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        form.add(field, g);
    }

    private void toast(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── REFRESH ────────────────────────────────────────────────

    @Override public void refresh() {
        // Rebuild category filter
        String prev = (String) categoryFilter.getSelectedItem();
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All");
        manager.getCategoryTotals().keySet().stream().sorted()
            .forEach(categoryFilter::addItem);
        if (prev != null) categoryFilter.setSelectedItem(prev);

        applyFilter();
    }

    // ─── TABLE MODEL ────────────────────────────────────────────

    private static class ExpenseTableModel extends AbstractTableModel {
        private final String[] COLS = { "#", "Category", "Amount", "Date", "Description" };
        private List<Expense> data = new ArrayList<>();

        void setData(List<Expense> d) { data = new ArrayList<>(d); fireTableDataChanged(); }
        Expense getExpense(int row)   { return data.get(row); }

        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override public Object getValueAt(int r, int c) {
            Expense e = data.get(r);
            return switch (c) {
                case 0 -> "#" + e.getId();
                case 1 -> e.getCategory();
                case 2 -> String.format("₹ %.2f", e.getAmount());
                case 3 -> e.getDate().toString();
                case 4 -> e.getDescription();
                default -> "";
            };
        }
    }
}

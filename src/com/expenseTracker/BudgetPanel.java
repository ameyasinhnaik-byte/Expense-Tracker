package com.expenseTracker;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * BudgetPanel
 * Set budgets per category. Visual progress bar shows spent vs limit.
 * Over-budget categories shown in red.
 *
 * Branch: feature/swing-panels
 */
public class BudgetPanel extends JPanel implements MainWindow.Refreshable {

    private final ExpenseManager manager;
    private final MainWindow window;
    private JPanel listArea;

    public BudgetPanel(ExpenseManager manager, MainWindow window) {
        this.manager = manager;
        this.window  = window;
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        build();
    }

    private void build() {
        // ── Header ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Budgets");
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JButton addBtn = AppTheme.primaryButton("+ Set Budget", AppTheme.ACCENT_AMBER);
        addBtn.addActionListener(e -> showSetBudgetDialog());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(addBtn);
        header.add(btnRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Scrollable list of budget cards ─────────────────────
        listArea = new JPanel();
        listArea.setLayout(new BoxLayout(listArea, BoxLayout.Y_AXIS));
        listArea.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    // ─── BUDGET CARD ────────────────────────────────────────────

    private JPanel buildBudgetCard(String cat, double limit, double spent) {
        JPanel card = AppTheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        double pct       = (limit > 0) ? Math.min(spent / limit, 1.0) : 0;
        boolean overBudget = spent > limit && limit > 0;
        Color barColor   = overBudget ? AppTheme.ACCENT_RED :
                pct > 0.8  ? AppTheme.ACCENT_AMBER : AppTheme.ACCENT_GREEN;

        // ── Top row: category name + amounts ────────────────────
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel catLabel = new JLabel(cat);
        catLabel.setFont(AppTheme.FONT_HEADING);
        catLabel.setForeground(AppTheme.TEXT_PRIMARY);

        String amtText = String.format("₹ %.2f  /  ₹ %.2f", spent, limit);
        JLabel amtLabel = new JLabel(amtText);
        amtLabel.setFont(AppTheme.FONT_LABEL);
        amtLabel.setForeground(barColor);

        topRow.add(catLabel,  BorderLayout.WEST);
        topRow.add(amtLabel, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // ── Progress bar ─────────────────────────────────────────
        JPanel progressTrack = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Track
                g2.setColor(AppTheme.BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                // Fill
                int fillW = (int)(pct * getWidth());
                if (fillW > 0) {
                    g2.setColor(barColor);
                    g2.fillRoundRect(0, 0, fillW, getHeight(), getHeight(), getHeight());
                }
                g2.dispose();
            }
        };
        progressTrack.setOpaque(false);
        progressTrack.setPreferredSize(new Dimension(0, 12));
        card.add(progressTrack, BorderLayout.CENTER);

        // ── Bottom row: percent + status tag ────────────────────
        JPanel botRow = new JPanel(new BorderLayout());
        botRow.setOpaque(false);

        String pctStr = limit > 0 ? String.format("%.0f%% used", pct * 100) : "No limit set";
        JLabel pctLabel = AppTheme.subLabel(pctStr);

        String status = overBudget ? "!! OVER BUDGET" : (pct > 0.8 ? "⚠ Near limit" : "✓ Within limit");
        JLabel statusLbl = AppTheme.subLabel(status);
        statusLbl.setForeground(barColor);

        JButton removeBtn = AppTheme.ghostButton("Remove");
        removeBtn.setFont(AppTheme.FONT_SMALL);
        removeBtn.setPreferredSize(new Dimension(80, 24));
        removeBtn.addActionListener(e -> {
            manager.getBudgets().remove(cat);
            refresh();
        });

        botRow.add(pctLabel,  BorderLayout.WEST);
        botRow.add(statusLbl, BorderLayout.CENTER);
        botRow.add(removeBtn, BorderLayout.EAST);
        card.add(botRow, BorderLayout.SOUTH);

        return card;
    }

    // ─── SET BUDGET DIALOG ──────────────────────────────────────

    private void showSetBudgetDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Set Category Budget", true);
        dlg.setSize(380, 220);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(AppTheme.BG_PANEL);
        dlg.setLayout(new BorderLayout(0, 0));
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 0, 0, 12); g.anchor = GridBagConstraints.WEST;

        // Category: combo of existing + free text
        g.gridy = 0; g.gridx = 0;
        form.add(AppTheme.subLabel("Category:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        JComboBox<String> catCombo = new JComboBox<>();
        catCombo.setEditable(true);
        catCombo.setBackground(AppTheme.BG_INPUT);
        catCombo.setForeground(AppTheme.TEXT_PRIMARY);
        catCombo.setFont(AppTheme.FONT_BODY);
        catCombo.addItem(""); // blank default
        manager.getCategoryTotals().keySet().stream().sorted().forEach(catCombo::addItem);
        form.add(catCombo, g);

        g.gridy = 1; g.gridx = 0; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        form.add(AppTheme.subLabel("Limit (Rs.):"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        JTextField limitField = AppTheme.inputField("e.g. 5000");
        form.add(limitField, g);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setOpaque(false);
        JButton cancel = AppTheme.ghostButton("Cancel");
        JButton save   = AppTheme.primaryButton("Set Budget", AppTheme.ACCENT_AMBER);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String cat = ((String) catCombo.getEditor().getItem()).trim();
            if (cat.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Category cannot be empty.");
                return;
            }
            try {
                double limit = Double.parseDouble(limitField.getText().trim());
                manager.setBudget(cat, limit);
                refresh();
                dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid amount.");
            }
        });

        btns.add(cancel);
        btns.add(save);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ─── REFRESH ────────────────────────────────────────────────

    @Override public void refresh() {
        listArea.removeAll();

        Map<String, Double> budgets = manager.getBudgets();
        Map<String, Double> totals  = manager.getCategoryTotals();

        if (budgets.isEmpty()) {
            JLabel empty = AppTheme.subLabel("No budgets set yet. Click '+ Set Budget' to start.");
            empty.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
            listArea.add(empty);
        } else {
            List<String> cats = new ArrayList<>(budgets.keySet());
            Collections.sort(cats);
            for (String cat : cats) {
                double limit = budgets.get(cat);
                double spent = totals.getOrDefault(cat, 0.0);
                listArea.add(buildBudgetCard(cat, limit, spent));
                listArea.add(Box.createVerticalStrut(10));
            }
        }

        // ── Also show categories with spending but no budget set ─
        boolean anyUnbudgeted = false;
        for (String cat : totals.keySet()) {
            if (!budgets.containsKey(cat)) {
                if (!anyUnbudgeted) {
                    listArea.add(Box.createVerticalStrut(16));
                    listArea.add(AppTheme.subLabel("  Categories without a budget:"));
                    listArea.add(Box.createVerticalStrut(6));
                    anyUnbudgeted = true;
                }
                double spent = totals.get(cat);
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
                row.setOpaque(false);
                row.add(AppTheme.badge(cat, AppTheme.TEXT_MUTED));
                row.add(AppTheme.subLabel(String.format("₹ %.2f spent — no limit", spent)));
                JButton setBtn = AppTheme.ghostButton("Set limit");
                setBtn.setFont(AppTheme.FONT_SMALL);
                setBtn.addActionListener(e -> showSetBudgetDialog());
                row.add(setBtn);
                listArea.add(row);
            }
        }

        listArea.revalidate();
        listArea.repaint();
    }
}

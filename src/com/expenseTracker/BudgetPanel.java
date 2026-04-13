package com.expenseTracker;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

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

    // ─── CUSTOM SCROLLBAR ───────────────────────────────────────
    static class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            this.thumbColor = AppTheme.ACCENT_BLUE;
            this.trackColor = AppTheme.BG_PANEL;
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
        private JButton zeroBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
    }

    private void build() {
        // ── Header ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Budgets");
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel subtitle = AppTheme.subLabel("Set spending limits per category to stay on track.");
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        JButton addBtn = AppTheme.primaryButton("+ Set Budget", AppTheme.ACCENT_AMBER);
        addBtn.addActionListener(e -> showSetBudgetDialog(null));
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
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new CustomScrollBarUI());

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

    private void showSetBudgetDialog(String preselectedCategory) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Set Category Budget", true);
        dlg.setSize(380, 220);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(AppTheme.BG_PANEL);
        dlg.setLayout(new BorderLayout(0, 0));
        dlg.setBackground(AppTheme.BG_PANEL);
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 14, 12, 14));

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
        // Auto-select the passed category if provided
        if (preselectedCategory != null) {
            catCombo.setSelectedItem(preselectedCategory);
        }
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
            // // Centered empty state
            // JPanel emptyState = new JPanel(new GridBagLayout());
            // emptyState.setOpaque(false);
            // JPanel inner = new JPanel();
            // inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            // inner.setOpaque(false);
            // JLabel emptyIcon = new JLabel("◎");
            // emptyIcon.setFont(new Font("SansSerif", Font.PLAIN, 48));
            // emptyIcon.setForeground(AppTheme.TEXT_MUTED);
            // emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            // JLabel emptyMsg = AppTheme.subLabel("No budgets set yet.");
            // emptyMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
            // JLabel emptyHint = AppTheme.subLabel("Click '+ Set Budget' above to get started.");
            // emptyHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            // inner.add(emptyIcon);
            // inner.add(Box.createVerticalStrut(8));
            // inner.add(emptyMsg);
            // inner.add(Box.createVerticalStrut(4));
            // inner.add(emptyHint);
            // emptyState.add(inner);
            // listArea.add(emptyState);
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
                    JLabel sectionLbl = AppTheme.subLabel("Categories without a budget:");
                    sectionLbl.setFont(AppTheme.FONT_HEADING);
                    sectionLbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 0));
                    listArea.add(sectionLbl);
                    listArea.add(Box.createVerticalStrut(6));
                    anyUnbudgeted = true;
                }
                double spent = totals.get(cat);
                Color catColor = AppTheme.categoryColor(cat);

                JPanel card = AppTheme.card();
                card.setLayout(new BorderLayout(0, 0));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
                left.setOpaque(false);
                left.add(AppTheme.badge(cat, catColor));
                JLabel spentLbl = AppTheme.subLabel(String.format("₹ %.2f spent — no budget/limit set", spent));
                left.add(spentLbl);
                card.add(left, BorderLayout.CENTER);

                JButton setBtn = AppTheme.ghostButton("Set Budget");
                setBtn.setFont(AppTheme.FONT_SMALL);
                setBtn.setPreferredSize(new Dimension(110, 28));
                setBtn.addActionListener(e -> showSetBudgetDialog(cat));
                JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                btnWrap.setOpaque(false);
                btnWrap.add(setBtn);
                card.add(btnWrap, BorderLayout.EAST);

                listArea.add(card);
                listArea.add(Box.createVerticalStrut(8));
            }
        }

        listArea.revalidate();
        listArea.repaint();
    }
}

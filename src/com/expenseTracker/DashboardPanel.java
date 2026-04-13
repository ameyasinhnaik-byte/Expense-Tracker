package com.expenseTracker;

import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class DashboardPanel extends JPanel implements MainWindow.Refreshable {

    private final ExpenseManager manager;
    private final MainWindow window;

    private JLabel lblTotal, lblMonth, lblTopCat, lblCount;
    private JPanel recentList, chartPanel;

    public DashboardPanel(ExpenseManager manager, MainWindow window) {
        this.manager = manager;
        this.window  = window;
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        build();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Dashboard");
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JButton addBtn = AppTheme.primaryButton("+ Add Expense", AppTheme.ACCENT_GREEN);
        addBtn.addActionListener(e -> { window.goToExpenses(); window.openAddExpenseDialog(); });
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(addBtn);
        header.add(btnRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(12, 0, 0, 12);

        gbc.gridy = 0; gbc.weighty = 0;
        gbc.weightx = 1; gbc.gridwidth = 1;

        lblTotal  = new JLabel("Rs. 0.00");
        lblMonth  = new JLabel("Rs. 0.00");
        lblTopCat = new JLabel("-");
        lblCount  = new JLabel("0");

        gbc.gridx = 0; body.add(statCard(lblTotal,  "Total Spent",   AppTheme.ACCENT_GREEN),  gbc);
        gbc.gridx = 1; body.add(statCard(lblMonth,  "This Month",    AppTheme.ACCENT_BLUE),   gbc);
        gbc.gridx = 2; body.add(statCard(lblTopCat, "Top Category",  AppTheme.ACCENT_PURPLE), gbc);
        gbc.gridx = 3; gbc.insets = new Insets(12, 0, 0, 0);
                        body.add(statCard(lblCount,  "Entries",       AppTheme.ACCENT_AMBER),  gbc);

        gbc.gridy = 1; gbc.weighty = 1.0; gbc.insets = new Insets(12, 0, 0, 12);

        recentList = new JPanel();
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        recentList.setOpaque(false);

        JPanel recentCard = AppTheme.card();
        recentCard.setLayout(new BorderLayout(0, 10));
        recentCard.add(AppTheme.heading("Recent Expenses"), BorderLayout.NORTH);
        JScrollPane recentScroll = new JScrollPane(recentList,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        recentScroll.setOpaque(false);
        recentScroll.getViewport().setOpaque(false);
        recentScroll.setBorder(null);
        recentCard.add(recentScroll, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridwidth = 2; body.add(recentCard, gbc);

        chartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPieChart(g);
            }
        };
        chartPanel.setOpaque(false);

        JPanel chartCard = AppTheme.card();
        chartCard.setLayout(new BorderLayout(0, 10));
        chartCard.add(AppTheme.heading("Spending by Category"), BorderLayout.NORTH);
        chartCard.add(chartPanel, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.gridwidth = 2; gbc.insets = new Insets(12, 0, 0, 0);
        body.add(chartCard, gbc);

        add(body, BorderLayout.CENTER);
    }

    private void drawPieChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Map<String, Double> totals = manager.getCategoryTotals();
        if (totals.isEmpty()) {
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(AppTheme.FONT_BODY);
            g2.drawString("No data yet.", 20, chartPanel.getHeight() / 2);
            g2.dispose();
            return;
        }

        List<Map.Entry<String, Double>> entries = new ArrayList<>(totals.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (entries.size() > 7) entries = entries.subList(0, 7);

        double total = 0;
        for (Map.Entry<String, Double> e : entries) total += e.getValue();

        int W = chartPanel.getWidth();
        int H = chartPanel.getHeight();
        int diameter = Math.min(W / 2, H - 40);
        int px = (W / 4) - (diameter / 2);
        int py = (H - diameter) / 2;

        double startAngle = 0;
        for (Map.Entry<String, Double> entry : entries) {
            double slice = (entry.getValue() / total) * 360.0;
            Color col = AppTheme.categoryColor(entry.getKey());
            g2.setColor(col);
            g2.fillArc(px, py, diameter, diameter, (int) startAngle, (int) slice);
            g2.setColor(AppTheme.BG_DARK);
            g2.setStroke(new BasicStroke(2));
            g2.drawArc(px, py, diameter, diameter, (int) startAngle, (int) slice);
            startAngle += slice;
        }

        int legendX = W / 2 + 10;
        int legendY = 20;
        for (Map.Entry<String, Double> entry : entries) {
            Color col = AppTheme.categoryColor(entry.getKey());
            g2.setColor(col);
            g2.fillRoundRect(legendX, legendY, 12, 12, 4, 4);
            g2.setFont(AppTheme.FONT_SMALL);
            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.drawString(entry.getKey(), legendX + 18, legendY + 11);
            g2.setColor(col);
            g2.drawString(String.format("Rs.%.0f", entry.getValue()), legendX + 18, legendY + 24);
            legendY += 38;
        }

        g2.dispose();
    }

    private JPanel statCard(JLabel valueLabel, String caption, Color accent) {
        JPanel card = AppTheme.card();
        card.setLayout(new BorderLayout(0, 6));

        JPanel accentBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setPreferredSize(new Dimension(0, 3));
        card.add(accentBar, BorderLayout.NORTH);

        valueLabel.setFont(AppTheme.FONT_AMOUNT);
        valueLabel.setForeground(accent);
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel cap = AppTheme.subLabel(caption);
        card.add(cap, BorderLayout.SOUTH);

        return card;
    }

    private JPanel recentRow(Expense e) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel desc = new JLabel(e.getDescription().isEmpty() ? "(no description)" : e.getDescription());
        desc.setFont(AppTheme.FONT_BODY);
        desc.setForeground(AppTheme.TEXT_PRIMARY);

        Color catColor = AppTheme.categoryColor(e.getCategory());
        JLabel cat = AppTheme.badge(e.getCategory(), catColor);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(desc);
        left.add(Box.createHorizontalStrut(8));
        left.add(cat);

        JLabel amt = new JLabel("Rs. " + String.format("%.2f", e.getAmount()));
        amt.setFont(AppTheme.FONT_LABEL);
        amt.setForeground(AppTheme.ACCENT_GREEN);

        row.add(left, BorderLayout.CENTER);
        row.add(amt,  BorderLayout.EAST);

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.add(row);
        wrap.add(AppTheme.separator());
        return wrap;
    }

    @Override public void refresh() {
        List<Expense> expenses = manager.getExpenses();

        double total = manager.getTotal();
        int month = LocalDate.now().getMonthValue();
        int year  = LocalDate.now().getYear();
        double thisMonth = 0;
        for (Expense e : expenses) {
            if (e.getDate().getMonthValue() == month && e.getDate().getYear() == year)
                thisMonth += e.getAmount();
        }

        Map<String, Double> catTotals = manager.getCategoryTotals();
        String topCat = "-";
        double maxVal = 0;
        for (Map.Entry<String, Double> entry : catTotals.entrySet()) {
            if (entry.getValue() > maxVal) { maxVal = entry.getValue(); topCat = entry.getKey(); }
        }

        lblTotal.setText("Rs. " + String.format("%.2f", total));
        lblMonth.setText("Rs. " + String.format("%.2f", thisMonth));
        lblTopCat.setText(topCat);
        lblCount.setText(String.valueOf(expenses.size()));

        recentList.removeAll();
        List<Expense> copy = new ArrayList<>(expenses);
        Collections.reverse(copy);
        int limit = Math.min(6, copy.size());
        for (int i = 0; i < limit; i++) recentList.add(recentRow(copy.get(i)));
        if (expenses.isEmpty()) {
            JLabel empty = AppTheme.subLabel("No expenses yet. Add one!");
            empty.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            recentList.add(empty);
        }
        recentList.revalidate();
        recentList.repaint();
        chartPanel.repaint();
    }
}
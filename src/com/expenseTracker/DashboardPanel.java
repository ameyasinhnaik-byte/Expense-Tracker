package com.expenseTracker;

import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * DashboardPanel
 * Landing screen. Shows: total spent, this month, top category,
 * recent expenses list, and a horizontal category bar chart.
 *
 * Branch: feature/swing-core
 */
public class DashboardPanel extends JPanel implements MainWindow.Refreshable {

    private final ExpenseManager manager;
    private final MainWindow window;

    // Live labels updated on refresh
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
        // ── Top heading row ──────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Dashboard");
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JButton addBtn = AppTheme.primaryButton("+ Add Expense", AppTheme.ACCENT_GREEN);
        addBtn.addActionListener(e -> {
            window.goToExpenses();
        });
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(addBtn);
        header.add(btnRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Main body ────────────────────────────────────────────
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(12, 0, 0, 12);

        // Row 0: 4 stat cards
        gbc.gridy = 0; gbc.weighty = 0;
        gbc.weightx = 1; gbc.gridwidth = 1;

        lblTotal  = bigStatLabel("₹ 0.00", "Total Spent");
        lblMonth  = bigStatLabel("₹ 0.00", "This Month");
        lblTopCat = bigStatLabel("—",      "Top Category");
        lblCount  = bigStatLabel("0",      "Total Entries");

        gbc.gridx = 0; body.add(statCard(lblTotal,  AppTheme.ACCENT_GREEN),  gbc);
        gbc.gridx = 1; body.add(statCard(lblMonth,  AppTheme.ACCENT_BLUE),   gbc);
        gbc.gridx = 2; body.add(statCard(lblTopCat, AppTheme.ACCENT_PURPLE), gbc);
        gbc.gridx = 3; gbc.insets = new Insets(12,0,0,0);
                        body.add(statCard(lblCount,  AppTheme.ACCENT_AMBER),  gbc);

        // Row 1: recent list (left) + chart (right)
        gbc.gridy = 1; gbc.weighty = 1.0; gbc.insets = new Insets(12,0,0,12);

        recentList = new JPanel();
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        recentList.setOpaque(false);

        JPanel recentCard = AppTheme.card();
        recentCard.setLayout(new BorderLayout(0, 10));
        JLabel recentTitle = AppTheme.heading("Recent Expenses");
        recentCard.add(recentTitle, BorderLayout.NORTH);
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
                drawChart(g);
            }
        };
        chartPanel.setOpaque(false);

        JPanel chartCard = AppTheme.card();
        chartCard.setLayout(new BorderLayout(0, 10));
        chartCard.add(AppTheme.heading("Spending by Category"), BorderLayout.NORTH);
        chartCard.add(chartPanel, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.gridwidth = 2; gbc.insets = new Insets(12,0,0,0);
        body.add(chartCard, gbc);

        add(body, BorderLayout.CENTER);
    }

    // ─── CHART DRAWING ──────────────────────────────────────────

    private void drawChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Map<String, Double> totals = manager.getCategoryTotals();
        if (totals.isEmpty()) {
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(AppTheme.FONT_BODY);
            g2.drawString("No data yet.", 20, chartPanel.getHeight() / 2);
            g2.dispose(); return;
        }

        List<Map.Entry<String, Double>> entries = new ArrayList<>(totals.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (entries.size() > 7) entries = entries.subList(0, 7);

        double max = entries.get(0).getValue();
        int W = chartPanel.getWidth() - 16;
        int rowH = Math.min(36, (chartPanel.getHeight() - 10) / entries.size());
        int barMaxW = W - 130;

        Color[] palette = {
            AppTheme.ACCENT_GREEN, AppTheme.ACCENT_BLUE, AppTheme.ACCENT_PURPLE,
            AppTheme.ACCENT_AMBER, AppTheme.ACCENT_RED, new Color(64,196,255), new Color(255,120,198)
        };

        for (int i = 0; i < entries.size(); i++) {
            String cat  = entries.get(i).getKey();
            double val  = entries.get(i).getValue();
            Color  col  = palette[i % palette.length];
            int    y    = 8 + i * (rowH + 6);
            int    barW = (int)(val / max * barMaxW);

            // Background track
            g2.setColor(AppTheme.BG_INPUT);
            g2.fillRoundRect(110, y + 8, barMaxW, rowH - 14, 6, 6);

            // Bar
            g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 200));
            if (barW > 0) g2.fillRoundRect(110, y + 8, barW, rowH - 14, 6, 6);

            // Category label
            g2.setFont(AppTheme.FONT_SMALL);
            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.drawString(truncate(cat, 12), 0, y + rowH - 12);

            // Amount
            g2.setFont(AppTheme.FONT_SMALL);
            g2.setColor(col);
            g2.drawString(String.format("₹%.0f", val), 110 + barW + 6, y + rowH - 12);
        }
        g2.dispose();
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    // ─── STAT CARD BUILDER ──────────────────────────────────────

    private JLabel bigStatLabel(String value, String caption) {
        return new JLabel(value); // value label — caption attached in statCard
    }

    private JPanel statCard(JLabel valueLabel, Color accent) {
        JPanel card = AppTheme.card();
        card.setLayout(new BorderLayout(0, 6));

        // top accent bar
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

        // caption = the label's name field (hacky but works for static cards)
        // We derive caption from the accent color
        JLabel cap = AppTheme.subLabel("—");
        cap.setName("caption");
        card.add(cap, BorderLayout.SOUTH);

        return card;
    }

    // ─── RECENT EXPENSE ROW ─────────────────────────────────────

    private JPanel recentRow(Expense e) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel desc = new JLabel(e.getDescription().isEmpty() ? "(no description)" : e.getDescription());
        desc.setFont(AppTheme.FONT_BODY);
        desc.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel cat = AppTheme.badge(e.getCategory(), AppTheme.ACCENT_PURPLE);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(desc);
        left.add(Box.createHorizontalStrut(8));
        left.add(cat);

        JLabel amt = new JLabel("₹ " + String.format("%.2f", e.getAmount()));
        amt.setFont(AppTheme.FONT_LABEL);
        amt.setForeground(AppTheme.ACCENT_GREEN);

        row.add(left, BorderLayout.CENTER);
        row.add(amt,  BorderLayout.EAST);

        // Separator
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.add(row);
        wrap.add(AppTheme.separator());
        return wrap;
    }

    // ─── REFRESH ────────────────────────────────────────────────

    @Override public void refresh() {
        List<Expense> expenses = manager.getExpenses();

        // Stat cards
        double total = manager.getTotal();
        int month    = LocalDate.now().getMonthValue();
        int year     = LocalDate.now().getYear();
        double thisMonth = expenses.stream()
            .filter(e -> e.getDate().getMonthValue() == month && e.getDate().getYear() == year)
            .mapToDouble(Expense::getAmount).sum();

        Map<String, Double> catTotals = manager.getCategoryTotals();
        String topCat = catTotals.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("—");

        lblTotal.setText("₹ " + String.format("%.2f", total));
        lblMonth.setText("₹ " + String.format("%.2f", thisMonth));
        lblTopCat.setText(topCat);
        lblCount.setText(String.valueOf(expenses.size()));

        // Caption labels — re-derive from panel structure
        updateCaption(lblTotal,  "Total Spent");
        updateCaption(lblMonth,  "This Month");
        updateCaption(lblTopCat, "Top Category");
        updateCaption(lblCount,  "Entries");

        // Recent expenses (last 6)
        recentList.removeAll();
        List<Expense> copy = new ArrayList<>(expenses);
        Collections.reverse(copy);
        copy.stream().limit(6).forEach(e -> recentList.add(recentRow(e)));
        if (expenses.isEmpty()) {
            JLabel empty = AppTheme.subLabel("No expenses yet. Add one!");
            empty.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
            recentList.add(empty);
        }
        recentList.revalidate();
        recentList.repaint();
        chartPanel.repaint();
    }

    private void updateCaption(JLabel valueLabel, String caption) {
        Container card = valueLabel.getParent();
        if (card == null) return;
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel && "caption".equals(c.getName())) {
                ((JLabel) c).setText(caption);
            }
        }
    }
}

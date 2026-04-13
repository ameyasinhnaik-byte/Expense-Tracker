package com.expenseTracker;

import java.awt.*;
import java.time.Month;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * ReportPanel
 * Visual report screen: summary stats, monthly bar chart, top expenses table.
 *
 * Branch: feature/swing-panels
 */
public class ReportPanel extends JPanel implements MainWindow.Refreshable {

    private final ExpenseManager manager;
    private JPanel monthlyChartPanel;
    private JPanel topListPanel;
    private JLabel lblGrandTotal, lblAvgMonthly, lblBiggestExpense;

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

    public ReportPanel(ExpenseManager manager) {
        this.manager = manager;
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        build();
    }

    private void build() {
        // ── Header ──────────────────────────────────────────────
        JLabel title = new JLabel("Reports");
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        // ── Main layout: top stat row + bottom two columns ───────
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 12, 12);

        // Row 0: 3 summary stat cards
        gbc.gridy = 0; gbc.weighty = 0; gbc.gridwidth = 1;

        lblGrandTotal      = statLabel("₹ 0.00");
        lblAvgMonthly      = statLabel("₹ 0.00");
        lblBiggestExpense  = statLabel("₹ 0.00");

        gbc.gridx = 0; gbc.weightx = 1;
        body.add(miniStatCard(lblGrandTotal,     "Grand Total",      AppTheme.ACCENT_GREEN),  gbc);
        gbc.gridx = 1;
        body.add(miniStatCard(lblAvgMonthly,     "Avg / Month",      AppTheme.ACCENT_BLUE),   gbc);
        gbc.gridx = 2; gbc.insets = new Insets(0, 0, 12, 0);
        body.add(miniStatCard(lblBiggestExpense, "Biggest Expense",  AppTheme.ACCENT_AMBER),  gbc);

        // Row 1: Monthly bar chart (left 2/3) + Top expenses (right 1/3)
        gbc.gridy = 1; gbc.weighty = 1; gbc.insets = new Insets(0, 0, 0, 12);

        monthlyChartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMonthlyChart(g);
            }
        };
        monthlyChartPanel.setOpaque(false);

        JPanel chartCard = AppTheme.card();
        chartCard.setLayout(new BorderLayout(0, 10));
        chartCard.add(AppTheme.heading("Monthly Spending"), BorderLayout.NORTH);
        chartCard.add(monthlyChartPanel, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridwidth = 2;
        body.add(chartCard, gbc);

        // Top expenses list
        topListPanel = new JPanel();
        topListPanel.setLayout(new BoxLayout(topListPanel, BoxLayout.Y_AXIS));
        topListPanel.setOpaque(false);

        JScrollPane topScroll = new JScrollPane(topListPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        topScroll.setOpaque(false);
        topScroll.getViewport().setBackground(AppTheme.BG_PANEL);
        topScroll.setBorder(BorderFactory.createEmptyBorder());
        topScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());

        JPanel topCard = AppTheme.card();
        topCard.setLayout(new BorderLayout(0, 10));
        topCard.add(AppTheme.heading("Top Expenses"), BorderLayout.NORTH);
        topCard.add(topScroll, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.gridwidth = 1; gbc.insets = new Insets(0, 0, 0, 0);
        body.add(topCard, gbc);

        add(body, BorderLayout.CENTER);
    }

    // ─── MONTHLY BAR CHART ──────────────────────────────────────

    private void drawMonthlyChart(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Build monthly data
        Map<String, Double> monthly = new LinkedHashMap<>();
        for (Expense e : manager.getExpenses()) {
            String key = e.getDate().getYear() + "-" +
                    String.format("%02d", e.getDate().getMonthValue());
            monthly.merge(key, e.getAmount(), Double::sum);
        }

        if (monthly.isEmpty()) {
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(AppTheme.FONT_BODY);
            g2.drawString("No monthly data yet.", 20, monthlyChartPanel.getHeight() / 2);
            g2.dispose(); return;
        }

        List<String> keys = new ArrayList<>(monthly.keySet());
        Collections.sort(keys);
        // Show last 6 months max
        if (keys.size() > 6) keys = keys.subList(keys.size() - 6, keys.size());

        double max = keys.stream().mapToDouble(k -> monthly.get(k)).max().orElse(1);
        int W = monthlyChartPanel.getWidth();
        int H = monthlyChartPanel.getHeight();
        int padL = 60, padR = 20, padT = 20, padB = 50;
        int chartW = W - padL - padR;
        int chartH = H - padT - padB;
        int barW = Math.max(20, chartW / keys.size() - 12);

        // Grid lines
        g2.setColor(AppTheme.BORDER_COLOR);
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{4, 4}, 0));
        for (int i = 0; i <= 4; i++) {
            int y = padT + (int)(chartH * i / 4.0);
            g2.drawLine(padL, y, padL + chartW, y);
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(AppTheme.FONT_SMALL);
            g2.drawString(String.format("₹%.0f", max * (4 - i) / 4), 0, y + 4);
            g2.setColor(AppTheme.BORDER_COLOR);
        }
        g2.setStroke(new BasicStroke(1));

        // Bars
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            double val = monthly.get(key);
            int barH = (int)(val / max * chartH);
            int x = padL + i * (chartW / keys.size()) + (chartW / keys.size() - barW) / 2;
            int y = padT + chartH - barH;

            // Bar gradient effect — two tone
            GradientPaint gp = new GradientPaint(
                    x, y, AppTheme.ACCENT_GREEN,
                    x, padT + chartH, new Color(35, 197, 94, 60));
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barW, barH, 6, 6);

            // Amount on top
            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.setFont(AppTheme.FONT_SMALL);
            String valStr = val >= 1000 ? String.format("₹%.0fk", val/1000) : String.format("₹%.0f", val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(valStr, x + (barW - fm.stringWidth(valStr)) / 2, y - 4);

            // Month label
            String[] parts = key.split("-");
            String label = Month.of(Integer.parseInt(parts[1])).name().substring(0, 3)
                    + " " + parts[0].substring(2);
            g2.setColor(AppTheme.TEXT_SECONDARY);
            g2.drawString(label, x + (barW - fm.stringWidth(label)) / 2,
                    padT + chartH + 18);
        }

        g2.dispose();
    }

    // ─── STAT HELPERS ───────────────────────────────────────────

    private JLabel statLabel(String val) {
        JLabel l = new JLabel(val);
        l.setFont(AppTheme.FONT_AMOUNT);
        return l;
    }

    private JPanel miniStatCard(JLabel lbl, String caption, Color accent) {
        JPanel card = AppTheme.card();
        card.setLayout(new BorderLayout(0, 4));

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

        lbl.setForeground(accent);
        card.add(lbl, BorderLayout.CENTER);

        JLabel cap = AppTheme.subLabel(caption);
        card.add(cap, BorderLayout.SOUTH);
        return card;
    }

    private JPanel topExpenseRow(int rank, Expense e) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel rankLbl = new JLabel(String.valueOf(rank));
        rankLbl.setFont(AppTheme.FONT_HEADING);
        rankLbl.setForeground(AppTheme.TEXT_MUTED);
        rankLbl.setPreferredSize(new Dimension(24, 0));

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 2));
        center.setOpaque(false);
        JLabel desc = new JLabel(e.getDescription().isEmpty() ? "(no description)" : e.getDescription());
        desc.setFont(AppTheme.FONT_BODY);
        desc.setForeground(AppTheme.TEXT_PRIMARY);
        JLabel cat = new JLabel(e.getCategory() + "  ·  " + e.getDate());
        cat.setFont(AppTheme.FONT_SMALL);
        cat.setForeground(AppTheme.TEXT_SECONDARY);
        center.add(desc);
        center.add(cat);

        JLabel amt = new JLabel("₹ " + String.format("%.2f", e.getAmount()));
        amt.setFont(AppTheme.FONT_LABEL);
        amt.setForeground(AppTheme.ACCENT_GREEN);
        amt.setHorizontalAlignment(SwingConstants.RIGHT);
        amt.setPreferredSize(new Dimension(90, 0));

        row.add(rankLbl, BorderLayout.WEST);
        row.add(center,  BorderLayout.CENTER);
        row.add(amt,     BorderLayout.EAST);

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

        // Stats
        double total = manager.getTotal();
        lblGrandTotal.setText("₹ " + String.format("%.2f", total));

        // Avg per month
        Set<String> months = new HashSet<>();
        for (Expense e : expenses)
            months.add(e.getDate().getYear() + "-" + e.getDate().getMonthValue());
        double avg = months.isEmpty() ? 0 : total / months.size();
        lblAvgMonthly.setText("₹ " + String.format("%.2f", avg));

        // Biggest
        double biggest = expenses.stream().mapToDouble(Expense::getAmount).max().orElse(0);
        lblBiggestExpense.setText("₹ " + String.format("%.2f", biggest));

        // Top expenses list
        topListPanel.removeAll();
        List<Expense> sorted = new ArrayList<>(expenses);
        sorted.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        int limit = Math.min(8, sorted.size());
        for (int i = 0; i < limit; i++) {
            topListPanel.add(topExpenseRow(i + 1, sorted.get(i)));
        }
        if (expenses.isEmpty()) {
            topListPanel.add(AppTheme.subLabel("No data."));
        }
        topListPanel.revalidate();
        topListPanel.repaint();
        monthlyChartPanel.repaint();
    }
}

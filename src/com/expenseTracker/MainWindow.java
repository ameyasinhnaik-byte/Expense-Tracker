package com.expenseTracker;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * MainWindow
 * Root JFrame. Hosts the left sidebar nav + right content area.
 * Switching panels = clicking nav items. No tabs, no menu bar.
 *
 * Branch: feature/swing-core
 */
public class MainWindow extends JFrame {

    private final ExpenseManager manager;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea   = new JPanel(cardLayout);

    // Nav button references so we can highlight the active one
    private final JButton[] navButtons = new JButton[4];
    private int activeNav = 0;

    // Panel names for CardLayout
    private static final String PANEL_DASHBOARD = "dashboard";
    private static final String PANEL_EXPENSES  = "expenses";
    private static final String PANEL_BUDGETS   = "budgets";
    private static final String PANEL_REPORT    = "report";

    public MainWindow(ExpenseManager manager) {
        this.manager = manager;
        AppTheme.apply();
        buildFrame();
        buildContent();
        buildSidebar();
        switchTo(0);
        setVisible(true);
    }

    // ─── FRAME SETUP ────────────────────────────────────────────

    private void buildFrame() {
        setTitle("Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppTheme.BG_DARK);
        setLayout(new BorderLayout());

        // Save on window close
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                FileHandler.saveExpenses(manager);
            }
        });
    }

    // ─── CONTENT AREA ───────────────────────────────────────────

    private void buildContent() {
        contentArea.setOpaque(false);
        contentArea.setBackground(AppTheme.BG_DARK);
        add(contentArea, BorderLayout.CENTER);
    }

    // ─── SIDEBAR ────────────────────────────────────────────────

    private void buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.BG_PANEL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // right border line
                g2.setColor(AppTheme.BORDER_COLOR);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(AppTheme.NAV_WIDTH, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // ── Logo area ──
JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 22));
logoArea.setOpaque(false);
JLabel logo = new JLabel("₹ EXPENSIO");
logo.setFont(AppTheme.FONT_TITLE);
logo.setForeground(AppTheme.ACCENT_GREEN);
logoArea.add(logo);

// ── Dark/Light mode toggle ──
JButton themeToggle = new JButton("☀ Light") {
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(AppTheme.BG_INPUT);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
        super.paintComponent(g);
    }
};
themeToggle.setFont(AppTheme.FONT_SMALL);
themeToggle.setForeground(AppTheme.ACCENT_AMBER);
themeToggle.setContentAreaFilled(false);
themeToggle.setBorderPainted(false);
themeToggle.setFocusPainted(false);
themeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
themeToggle.setPreferredSize(new Dimension(80, 28));
final boolean[] isDark = {true};
themeToggle.addActionListener(e -> {
    isDark[0] = !isDark[0];
    if (isDark[0]) {
        AppTheme.BG_DARK         = new Color(13, 17, 23);
        AppTheme.BG_PANEL        = new Color(22, 27, 34);
        AppTheme.BG_INPUT        = new Color(30, 37, 48);
        AppTheme.TEXT_PRIMARY_MUT   = new Color(230, 237, 243);
        AppTheme.TEXT_SECONDARY_MUT = new Color(139, 148, 158);
        AppTheme.TEXT_MUTED_MUT     = new Color(72, 82, 94);
        AppTheme.BORDER_COLOR_MUT   = new Color(33, 41, 54);
        AppTheme.BG_ROW_ALT_MUT     = new Color(18, 23, 30);
        AppTheme.BG_HOVER_MUT       = new Color(35, 45, 60);
        themeToggle.setText("☀ Light");
    } else {
        AppTheme.BG_DARK         = new Color(240, 242, 245);
        AppTheme.BG_PANEL        = new Color(255, 255, 255);
        AppTheme.BG_INPUT        = new Color(228, 232, 238);
        AppTheme.TEXT_PRIMARY_MUT   = new Color(15, 20, 30);
        AppTheme.TEXT_SECONDARY_MUT = new Color(80, 90, 110);
        AppTheme.TEXT_MUTED_MUT     = new Color(140, 150, 165);
        AppTheme.BORDER_COLOR_MUT   = new Color(200, 208, 220);
        AppTheme.BG_ROW_ALT_MUT     = new Color(245, 247, 250);
        AppTheme.BG_HOVER_MUT       = new Color(215, 222, 232);
        themeToggle.setText("☾ Dark");
    }
    AppTheme.applyMutable();
    SwingUtilities.updateComponentTreeUI(MainWindow.this);
    repaint();
});
logoArea.add(themeToggle);
sidebar.add(logoArea);
        // ── Nav items ──
        String[][] navItems = {
            { "  Dashboard",  "●" },
            { "  Expenses",   "◈" },
            { "  Budgets",    "◎" },
            { "  Reports",    "≡" },
        };
        String[] panelNames = { PANEL_DASHBOARD, PANEL_EXPENSES, PANEL_BUDGETS, PANEL_REPORT };

        for (int i = 0; i < navItems.length; i++) {
            final int idx = i;
            JButton btn = buildNavButton(navItems[i][1] + navItems[i][0]);
            navButtons[i] = btn;
            btn.addActionListener(e -> switchTo(idx));
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // ── Save button at bottom ──
        sidebar.add(AppTheme.separator());
        JButton saveBtn = buildNavButton("  ↑  Save Now");
        saveBtn.setForeground(AppTheme.ACCENT_AMBER);
        saveBtn.addActionListener(e -> {
            FileHandler.saveExpenses(manager);
            JOptionPane.showMessageDialog(this, "Data saved successfully!",
                "Saved", JOptionPane.INFORMATION_MESSAGE);
        });
        sidebar.add(saveBtn);
        sidebar.add(Box.createVerticalStrut(16));

        add(sidebar, BorderLayout.WEST);

        // ── Register all panels ──
        contentArea.add(new DashboardPanel(manager, this), PANEL_DASHBOARD);
        contentArea.add(new ExpensePanel(manager, this),   PANEL_EXPENSES);
        contentArea.add(new BudgetPanel(manager, this),    PANEL_BUDGETS);
        contentArea.add(new ReportPanel(manager),          PANEL_REPORT);
    }

    private JButton buildNavButton(String text) {
        JButton btn = new JButton(text) {
            boolean active = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(new Color(35, 197, 94, 25));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                    g2.setColor(AppTheme.ACCENT_GREEN);
                    g2.fillRoundRect(0, 8, 3, getHeight()-16, 3, 3); // left accent bar
                } else if (getModel().isRollover()) {
                    g2.setColor(AppTheme.BG_HOVER);
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
            public void setActive(boolean a) { active = a;
                setForeground(a ? AppTheme.ACCENT_GREEN : AppTheme.TEXT_SECONDARY); repaint(); }
        };
        btn.setFont(AppTheme.FONT_NAV);
        btn.setForeground(AppTheme.TEXT_SECONDARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(AppTheme.NAV_WIDTH, 44));
        btn.setPreferredSize(new Dimension(AppTheme.NAV_WIDTH, 44));
        return btn;
    }

    // ─── NAVIGATION ─────────────────────────────────────────────

    public void switchTo(int idx) {
        String[] panels = { PANEL_DASHBOARD, PANEL_EXPENSES, PANEL_BUDGETS, PANEL_REPORT };
        cardLayout.show(contentArea, panels[idx]);
        activeNav = idx;
        for (int i = 0; i < navButtons.length; i++) {
            // Cast to set active state
            JButton b = navButtons[i];
            b.putClientProperty("active", i == idx);
            b.setForeground(i == idx ? AppTheme.ACCENT_GREEN : AppTheme.TEXT_SECONDARY);
        }
        // Refresh the shown panel if it supports it
        Component shown = null;
        for (Component c : contentArea.getComponents()) {
            if (c.isVisible()) { shown = c; break; }
        }
        if (shown instanceof Refreshable) ((Refreshable) shown).refresh();
    }

    public void goToExpenses() { switchTo(1); }
    public void openAddExpenseDialog() {
        for (Component c : contentArea.getComponents()) {
            if (c.isVisible() && c instanceof ExpensePanel) {
                ((ExpensePanel) c).showAddDialog();
                break;
            }
        }
    }
    public void goToBudgets()  { switchTo(2); }

    /** Marker interface for panels that refresh their data when shown */
    public interface Refreshable { void refresh(); }
}

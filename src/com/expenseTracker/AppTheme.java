package com.expenseTracker;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * AppTheme
 * Central design system — all colors, fonts, and UI constants live here.
 * Every UI class imports from here so the whole app stays visually consistent.
 *
 * Branch: feature/swing-core
 */
public class AppTheme {

    // ─── PALETTE ────────────────────────────────────────────────
    public static  Color BG_DARK        = new Color(13, 17, 23);       // deep navy black
    public static Color BG_PANEL       = new Color(22, 27, 34);       // card background
    public static Color BG_INPUT       = new Color(30, 37, 48);       // input fields
    public static final Color BG_ROW_ALT     = new Color(18, 23, 30);       // alternating table row
    public static final Color BG_HOVER       = new Color(35, 45, 60);       // hover state

    public static final Color ACCENT_GREEN   = new Color(35, 197, 94);      // primary action / positive
    public static final Color ACCENT_RED     = new Color(240, 71, 71);      // delete / warning
    public static final Color ACCENT_AMBER   = new Color(255, 171, 0);      // budget warning
    public static final Color ACCENT_BLUE    = new Color(88, 166, 255);     // links / info
    public static final Color ACCENT_PURPLE  = new Color(163, 113, 247);    // category tag

// ─── CATEGORY COLORS ────────────────────────────────────────
public static final java.util.Map<String, Color> CATEGORY_COLORS = new java.util.LinkedHashMap<>();
static {
    CATEGORY_COLORS.put("Food",          new Color(255, 107, 107));  // red
    CATEGORY_COLORS.put("Travel",        new Color(88,  166, 255));  // blue
    CATEGORY_COLORS.put("Shopping",      new Color(163, 113, 247));  // purple
    CATEGORY_COLORS.put("Health",        new Color(35,  197, 94));   // green
    CATEGORY_COLORS.put("Entertainment", new Color(255, 171, 0));    // amber
    CATEGORY_COLORS.put("Education",     new Color(0,   210, 211));  // teal
    CATEGORY_COLORS.put("Bills",         new Color(240, 71,  71));   // dark red
    CATEGORY_COLORS.put("Other",         new Color(139, 148, 158));  // grey
}

/** Returns a color for a category, or a default if not found */
public static Color categoryColor(String category) {
    return CATEGORY_COLORS.getOrDefault(category, ACCENT_PURPLE);
}
    public static final Color TEXT_PRIMARY   = new Color(230, 237, 243);
    public static final Color TEXT_SECONDARY = new Color(139, 148, 158);
    public static final Color TEXT_MUTED     = new Color(72, 82, 94);
    public static final Color BORDER_COLOR   = new Color(33, 41, 54);

    // ─── TYPOGRAPHY ─────────────────────────────────────────────
    public static final Font FONT_TITLE      = new Font("SansSerif", Font.BOLD,  26);
    public static final Font FONT_HEADING    = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FONT_LABEL      = new Font("SansSerif", Font.BOLD,  12);
    public static final Font FONT_BODY       = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_MONO       = new Font("Monospaced", Font.PLAIN, 13);
    public static final Font FONT_SMALL      = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_AMOUNT     = new Font("SansSerif", Font.BOLD,  22);
    public static final Font FONT_NAV        = new Font("SansSerif", Font.BOLD,  13);

    // ─── DIMENSIONS ─────────────────────────────────────────────
    public static final int  NAV_WIDTH       = 200;
    public static final int  CORNER_RADIUS   = 10;
    public static final int  BTN_HEIGHT      = 38;
    public static final int  INPUT_HEIGHT    = 38;

    // ─── GLOBAL LOOK & FEEL ─────────────────────────────────────
    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background",              BG_DARK);
        UIManager.put("OptionPane.background",         BG_PANEL);
        UIManager.put("OptionPane.messageForeground",  TEXT_PRIMARY);
        UIManager.put("Button.background",             BG_INPUT);
        UIManager.put("Button.foreground",             TEXT_PRIMARY);
        UIManager.put("TextField.background",          BG_INPUT);
        UIManager.put("TextField.foreground",          TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",     ACCENT_GREEN);
        UIManager.put("ComboBox.background",           BG_INPUT);
        UIManager.put("ComboBox.foreground",           TEXT_PRIMARY);
        UIManager.put("ScrollBar.background",          BG_PANEL);
        UIManager.put("ScrollBar.thumb",               BG_INPUT);
        UIManager.put("ScrollBar.thumbDarkShadow",     BG_INPUT);
        UIManager.put("ScrollBar.thumbHighlight",      BG_HOVER);
        UIManager.put("ScrollBar.thumbShadow",         BG_INPUT);
        UIManager.put("ScrollBar.track",               BG_PANEL);
        UIManager.put("Table.background",              BG_PANEL);
        UIManager.put("Table.foreground",              TEXT_PRIMARY);
        UIManager.put("Table.gridColor",               BORDER_COLOR);
        UIManager.put("Table.selectionBackground",     BG_HOVER);
        UIManager.put("Table.selectionForeground",     ACCENT_GREEN);
        UIManager.put("TableHeader.background",        BG_DARK);
        UIManager.put("TableHeader.foreground",        TEXT_SECONDARY);
        UIManager.put("Label.foreground",              TEXT_PRIMARY);
    }

    // ─── FACTORY METHODS ────────────────────────────────────────

    /** Rounded pill button with accent color */
    public static JButton primaryButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? accent.darker() :
                            getModel().isRollover() ? accent.brighter() : accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_LABEL);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, BTN_HEIGHT));
        return btn;
    }

    /** Ghost / secondary button with border */
    public static JButton ghostButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_HOVER : BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, CORNER_RADIUS, CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_LABEL);
        btn.setForeground(TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, BTN_HEIGHT));
        return btn;
    }

    /** Styled text field */
    public static JTextField inputField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_INPUT);
        field.setCaretColor(ACCENT_GREEN);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(BORDER_COLOR, CORNER_RADIUS),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        field.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        return field;
    }

    /** Card panel with rounded border */
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CORNER_RADIUS*2, CORNER_RADIUS*2);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return p;
    }

    /** Section heading label */
    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    /** Muted secondary label */
    public static JLabel subLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    /** Colored category badge pill */
    public static JLabel badge(String text, Color color) {
        JLabel l = new JLabel(" " + text + " ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(FONT_SMALL);
        l.setForeground(color);
        l.setOpaque(false);
        return l;
    }

    /** Separator line */
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        return sep;
    }

    // ─── HELPER BORDER ──────────────────────────────────────────
    public static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        public RoundBorder(Color c, int r) { color = c; radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(1,1,1,1); }
    }
}

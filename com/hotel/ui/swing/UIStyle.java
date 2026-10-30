package com.hotel.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class UIStyle {

    public static final Color COLOR_PRIMARY_DARK  = new Color(0x1E, 0x29, 0x3B);
    public static final Color COLOR_SIDEBAR_HOVER = new Color(0x33, 0x41, 0x55);
    public static final Color COLOR_ACCENT        = new Color(0x4F, 0x46, 0xE5);
    public static final Color COLOR_ACCENT_HOVER  = new Color(0x43, 0x38, 0xCA);
    public static final Color COLOR_CYAN          = new Color(0x0E, 0xA5, 0xE9);
    public static final Color COLOR_BG            = new Color(0xF8, 0xFA, 0xFC);
    public static final Color COLOR_CARD          = Color.WHITE;
    public static final Color COLOR_TEXT_MAIN     = new Color(0x0F, 0x17, 0x2A);
    public static final Color COLOR_TEXT_MUTED    = new Color(0x64, 0x74, 0x8B);
    public static final Color COLOR_SUCCESS       = new Color(0x16, 0xA3, 0x4A);
    public static final Color COLOR_DANGER        = new Color(0xDC, 0x26, 0x26);
    public static final Color COLOR_WARNING       = new Color(0xD9, 0xA0, 0x06);
    public static final Color COLOR_BORDER        = new Color(0xE2, 0xE8, 0xF0);

    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_REGULAR  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD     = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 13);

    public static void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {

            }
        }
        UIManager.put("defaultFont", FONT_REGULAR);
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_ACCENT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(COLOR_TEXT_MAIN);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_DANGER);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_DANGER.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns > 0 ? columns : 15);
        tf.setFont(FONT_REGULAR);
        tf.setBackground(Color.WHITE);
        tf.setForeground(new Color(15, 23, 42));
        tf.setCaretColor(new Color(15, 23, 42));
        tf.setEditable(true);
        tf.setEnabled(true);
        tf.setPreferredSize(new Dimension(200, 38));
        tf.setMinimumSize(new Dimension(140, 36));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns > 0 ? columns : 15);
        pf.setFont(FONT_REGULAR);
        pf.setBackground(Color.WHITE);
        pf.setForeground(new Color(15, 23, 42));
        pf.setCaretColor(new Color(15, 23, 42));
        pf.setEditable(true);
        pf.setEnabled(true);
        pf.setPreferredSize(new Dimension(200, 38));
        pf.setMinimumSize(new Dimension(140, 36));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return pf;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(COLOR_BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(new Color(0xEE, 0xF2, 0xFF));
        table.setSelectionForeground(COLOR_ACCENT);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(0xF1, 0xF5, 0xF9));
        header.setForeground(COLOR_TEXT_MAIN);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public static DefaultTableCellRenderer createStatusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(FONT_BOLD);
                String text = value != null ? value.toString() : "";
                lbl.setText(text);

                String upper = text.toUpperCase();
                if (upper.startsWith("AVAILABLE") || upper.equals("CONFIRMED") || upper.equals("PAID") || upper.equals("COMPLETED")) {
                    lbl.setForeground(COLOR_SUCCESS);
                } else if (upper.contains("BOOKED") || upper.equals("OCCUPIED") || upper.equals("CANCELLED") || upper.equals("REJECTED") || upper.equals("FAILED")) {
                    lbl.setForeground(COLOR_DANGER);
                } else if (upper.contains("MAINTENANCE") || upper.contains("PENDING") || upper.equals("REFUNDED")) {
                    lbl.setForeground(COLOR_WARNING);
                } else {
                    lbl.setForeground(COLOR_TEXT_MAIN);
                }
                return lbl;
            }
        };
    }
}

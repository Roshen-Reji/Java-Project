package com.hotel.ui.swing;

import com.hotel.domain.Reservation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class CalendarPickerDialog extends JDialog {

    private static final DateTimeFormatter MONTH_YEAR_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final Color COLOR_BOOKED = new Color(254, 226, 226);
    private static final Color COLOR_BOOKED_TEXT = new Color(185, 28, 28);
    private static final Color COLOR_TURNOVER = new Color(254, 243, 199);
    private static final Color COLOR_TURNOVER_TEXT = new Color(180, 83, 9);
    private static final Color COLOR_DISABLED = new Color(241, 245, 249);
    private static final Color COLOR_DISABLED_TEXT = new Color(148, 163, 184);

    private YearMonth currentYearMonth;
    private final LocalDate minDate;
    private final LocalDate initialDate;
    private final boolean isCheckOutPicker;
    private final List<Reservation> activeReservations;

    private LocalDate selectedDate = null;

    private JLabel lblMonthYear;
    private JPanel daysGrid;
    private JButton btnPrev;
    private JButton btnNext;

    public CalendarPickerDialog(Window owner,
                                String title,
                                LocalDate initialDate,
                                LocalDate minDate,
                                boolean isCheckOutPicker,
                                List<Reservation> activeReservations) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.initialDate = initialDate;
        this.minDate = minDate != null ? minDate : LocalDate.now();
        this.isCheckOutPicker = isCheckOutPicker;
        this.activeReservations = activeReservations;

        LocalDate startMonthDate = (initialDate != null && !initialDate.isBefore(this.minDate)) ? initialDate : this.minDate;
        this.currentYearMonth = YearMonth.from(startMonthDate);

        setSize(460, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.COLOR_BG);
        setResizable(false);

        initUI(title);
        updateCalendar();
    }

    public static LocalDate showDialog(Window owner,
                                       String title,
                                       LocalDate initialDate,
                                       LocalDate minDate,
                                       boolean isCheckOutPicker,
                                       List<Reservation> activeReservations) {
        CalendarPickerDialog dialog = new CalendarPickerDialog(owner, title, initialDate, minDate, isCheckOutPicker, activeReservations);
        dialog.setVisible(true);
        return dialog.getSelectedDate();
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    private void initUI(String dialogTitle) {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(UIStyle.COLOR_PRIMARY_DARK);
        topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(dialogTitle);
        titleLbl.setFont(UIStyle.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);
        topPanel.add(titleLbl);

        JLabel subLbl = new JLabel(isCheckOutPicker ? "Select Check-Out Date (Single-click to select)" : "Select Check-In Date (Single-click to select)");
        subLbl.setFont(UIStyle.FONT_SMALL);
        subLbl.setForeground(new Color(0x94, 0xA3, 0xB8));
        subLbl.setAlignmentX(CENTER_ALIGNMENT);
        topPanel.add(subLbl);
        topPanel.add(Box.createVerticalStrut(10));

        // Month Navigation Bar
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setOpaque(false);

        btnPrev = UIStyle.createSecondaryButton("◀ Prev");
        btnPrev.setPreferredSize(new Dimension(84, 32));
        btnPrev.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblMonthYear.setForeground(Color.WHITE);

        btnNext = UIStyle.createSecondaryButton("Next ▶");
        btnNext.setPreferredSize(new Dimension(84, 32));
        btnNext.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        navPanel.add(btnPrev, BorderLayout.WEST);
        navPanel.add(lblMonthYear, BorderLayout.CENTER);
        navPanel.add(btnNext, BorderLayout.EAST);
        topPanel.add(navPanel);

        add(topPanel, BorderLayout.NORTH);

        // Center: Calendar Body (Day names + 6x7 Grid)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 14, 6, 14));

        // Day of Week Header Grid (7 Columns)
        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        JPanel weekDaysPanel = new JPanel(new GridLayout(1, 7, 4, 0));
        weekDaysPanel.setOpaque(false);
        for (String wd : weekDays) {
            JLabel lbl = new JLabel(wd, SwingConstants.CENTER);
            lbl.setFont(UIStyle.FONT_BOLD);
            lbl.setForeground(UIStyle.COLOR_TEXT_MUTED);
            lbl.setPreferredSize(new Dimension(0, 24));
            weekDaysPanel.add(lbl);
        }
        centerPanel.add(weekDaysPanel, BorderLayout.NORTH);

        // Days Grid (6x7)
        daysGrid = new JPanel(new GridLayout(6, 7, 4, 4));
        daysGrid.setOpaque(false);
        centerPanel.add(daysGrid, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // South: Legend & Cancel Button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyle.COLOR_BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        legendPanel.setOpaque(false);

        legendPanel.add(createLegendBadge("Available", Color.WHITE, UIStyle.COLOR_TEXT_MAIN, UIStyle.COLOR_BORDER));
        legendPanel.add(createLegendBadge("Reserved", COLOR_BOOKED, COLOR_BOOKED_TEXT, new Color(252, 165, 165)));
        if (isCheckOutPicker) {
            legendPanel.add(createLegendBadge("Turnover Check-Out", COLOR_TURNOVER, COLOR_TURNOVER_TEXT, new Color(252, 211, 77)));
        }

        JButton btnCancel = UIStyle.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        bottomPanel.add(legendPanel, BorderLayout.CENTER);
        bottomPanel.add(btnCancel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createLegendBadge(String text, Color bg, Color fg, Color border) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(12, 12));
        box.setBackground(bg);
        box.setBorder(BorderFactory.createLineBorder(border, 1));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(UIStyle.COLOR_TEXT_MUTED);
        p.add(box);
        p.add(lbl);
        return p;
    }

    private void updateCalendar() {
        lblMonthYear.setText(currentYearMonth.format(MONTH_YEAR_FORMAT));

        // Disable Prev button if at or before minimum month
        YearMonth minYearMonth = YearMonth.from(minDate != null ? minDate : LocalDate.now());
        btnPrev.setEnabled(!currentYearMonth.isBefore(minYearMonth));

        daysGrid.removeAll();

        LocalDate firstDay = currentYearMonth.atDay(1);
        int startCol = firstDay.getDayOfWeek().getValue() % 7; // Sunday=0, Monday=1, ..., Saturday=6

        // Add leading blanks
        for (int i = 0; i < startCol; i++) {
            JLabel empty = new JLabel("");
            daysGrid.add(empty);
        }

        int daysInMonth = currentYearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        // Find nearest future reservation starting at or after minDate (if picking checkout)
        LocalDate earliestReservationCheckIn = null;
        if (isCheckOutPicker && activeReservations != null) {
            for (Reservation res : activeReservations) {
                LocalDate rIn = res.getCheckInDate();
                if (rIn != null && !rIn.isBefore(minDate.minusDays(1))) {
                    if (earliestReservationCheckIn == null || rIn.isBefore(earliestReservationCheckIn)) {
                        earliestReservationCheckIn = rIn;
                    }
                }
            }
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            JButton btn = new JButton(String.valueOf(day));
            btn.setFont(UIStyle.FONT_BOLD);
            btn.setFocusPainted(false);
            btn.setMargin(new Insets(2, 2, 2, 2));

            // Check if date is in the past or before minDate
            boolean isBeforeMin = (minDate != null && date.isBefore(minDate)) || date.isBefore(today);

            if (isBeforeMin) {
                btn.setEnabled(false);
                btn.setBackground(COLOR_DISABLED);
                btn.setForeground(COLOR_DISABLED_TEXT);
                btn.setBorder(BorderFactory.createLineBorder(COLOR_DISABLED, 1));
                btn.setToolTipText("Past / Unavailable Date");
            } else {
                // Check reservation overlap / visual aid
                Reservation overlappingRes = findReservationForDate(date);

                if (isCheckOutPicker) {
                    // For check-out: guest CAN check out on earliestReservationCheckIn!
                    if (earliestReservationCheckIn != null && date.equals(earliestReservationCheckIn)) {
                        btn.setEnabled(true);
                        btn.setBackground(COLOR_TURNOVER);
                        btn.setForeground(COLOR_TURNOVER_TEXT);
                        btn.setBorder(BorderFactory.createLineBorder(new Color(245, 158, 11), 1));
                        btn.setToolTipText("Next guest arrives today (" + earliestReservationCheckIn + ") — Click to Check-Out on this date!");
                    } else if (earliestReservationCheckIn != null && date.isAfter(earliestReservationCheckIn)) {
                        btn.setEnabled(false);
                        btn.setBackground(COLOR_BOOKED);
                        btn.setForeground(COLOR_BOOKED_TEXT);
                        btn.setBorder(BorderFactory.createLineBorder(new Color(252, 165, 165), 1));
                        btn.setToolTipText("Cannot check out on or after " + date + " (Reserved from " + earliestReservationCheckIn + ")");
                    } else if (overlappingRes != null) {
                        btn.setEnabled(false);
                        btn.setBackground(COLOR_BOOKED);
                        btn.setForeground(COLOR_BOOKED_TEXT);
                        btn.setBorder(BorderFactory.createLineBorder(new Color(252, 165, 165), 1));
                        btn.setToolTipText("Booked from " + overlappingRes.getCheckInDate() + " to " + overlappingRes.getCheckOutDate());
                    } else {
                        btn.setEnabled(true);
                        btn.setBackground(Color.WHITE);
                        btn.setForeground(UIStyle.COLOR_TEXT_MAIN);
                        btn.setBorder(BorderFactory.createLineBorder(UIStyle.COLOR_BORDER, 1));
                        btn.setToolTipText("Available for Check-Out");
                    }
                } else {
                    // For check-in: cannot check in on a date inside an existing reservation [checkIn, checkOut)
                    if (overlappingRes != null) {
                        btn.setEnabled(false);
                        btn.setBackground(COLOR_BOOKED);
                        btn.setForeground(COLOR_BOOKED_TEXT);
                        btn.setBorder(BorderFactory.createLineBorder(new Color(252, 165, 165), 1));
                        btn.setToolTipText("Booked from " + overlappingRes.getCheckInDate() + " to " + overlappingRes.getCheckOutDate());
                    } else {
                        btn.setEnabled(true);
                        btn.setBackground(Color.WHITE);
                        btn.setForeground(UIStyle.COLOR_TEXT_MAIN);
                        btn.setBorder(BorderFactory.createLineBorder(UIStyle.COLOR_BORDER, 1));
                        btn.setToolTipText("Available for Check-In");
                    }
                }

                // Highlight currently selected date
                if (date.equals(initialDate)) {
                    btn.setBorder(BorderFactory.createLineBorder(UIStyle.COLOR_ACCENT, 2));
                }

                if (btn.isEnabled()) {
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btn.addActionListener(e -> {
                        selectedDate = date;
                        dispose();
                    });
                }
            }

            daysGrid.add(btn);
        }

        // Fill trailing blanks to complete 42 cells (6 rows * 7 columns)
        int totalAdded = startCol + daysInMonth;
        for (int i = totalAdded; i < 42; i++) {
            daysGrid.add(new JLabel(""));
        }

        daysGrid.revalidate();
        daysGrid.repaint();
    }

    private Reservation findReservationForDate(LocalDate date) {
        if (activeReservations == null || activeReservations.isEmpty()) {
            return null;
        }
        for (Reservation res : activeReservations) {
            LocalDate in = res.getCheckInDate();
            LocalDate out = res.getCheckOutDate();
            if (in == null || out == null) continue;
            // Room is occupied during [in, out)
            if (!date.isBefore(in) && date.isBefore(out)) {
                return res;
            }
        }
        return null;
    }
}


package com.hotel.ui.swing;

import com.hotel.domain.Customer;
import com.hotel.domain.Payment;
import com.hotel.domain.Reservation;
import com.hotel.domain.Room;
import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomManagementService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class CustomerDashboardPanel extends JPanel {

    private final HotelAppFrame mainFrame;
    private final AuthenticationCustomerService authService;
    private final RoomManagementService roomService;
    private final ReservationService resService;
    private final PaymentService payService;

    private Customer currentCustomer;

    private JLabel guestNameLbl;
    private JLabel guestEmailLbl;

    private CardLayout contentCardLayout;
    private JPanel contentCards;

    // Top Bar controls
    private JComboBox<String> cbRoomTypeFilter;
    private JTextField tfCheckIn;
    private JTextField tfCheckOut;
    private JTextField tfSelectedRoom;
    private JLabel lblAvailabilityNotice;
    private JLabel lblEstimatedTotal;

    // Rooms table
    private DefaultTableModel roomTableModel;
    private JTable roomTable;

    // Bookings table
    private DefaultTableModel bookingsTableModel;
    private JTable bookingsTable;

    public CustomerDashboardPanel(HotelAppFrame mainFrame,
                                  AuthenticationCustomerService authService,
                                  RoomManagementService roomService,
                                  ReservationService resService,
                                  PaymentService payService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        this.roomService = roomService;
        this.resService = resService;
        this.payService = payService;

        setLayout(new BorderLayout());
        setBackground(UIStyle.COLOR_BG);

        initUI();
    }

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        if (customer != null) {
            guestNameLbl.setText(customer.getName());
            guestEmailLbl.setText(customer.getEmail() + " | ID #" + customer.getCustomerId());
            refreshRooms();
            refreshBookings();
        }
    }

    private void initUI() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyle.COLOR_PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        JLabel brandLbl = new JLabel("HOTEL ASTRA");
        brandLbl.setFont(UIStyle.FONT_TITLE);
        brandLbl.setForeground(Color.WHITE);
        brandLbl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subBrand = new JLabel("Customer Portal");
        subBrand.setFont(UIStyle.FONT_SMALL);
        subBrand.setForeground(UIStyle.COLOR_CYAN);
        subBrand.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(brandLbl);
        sidebar.add(subBrand);
        sidebar.add(Box.createVerticalStrut(24));

        JPanel profileBox = new JPanel();
        profileBox.setLayout(new BoxLayout(profileBox, BoxLayout.Y_AXIS));
        profileBox.setBackground(UIStyle.COLOR_SIDEBAR_HOVER);
        profileBox.setBorder(new EmptyBorder(12, 12, 12, 12));
        profileBox.setAlignmentX(LEFT_ALIGNMENT);
        profileBox.setMaximumSize(new Dimension(210, 70));

        guestNameLbl = new JLabel("Guest");
        guestNameLbl.setFont(UIStyle.FONT_BOLD);
        guestNameLbl.setForeground(Color.WHITE);

        guestEmailLbl = new JLabel("email@example.com");
        guestEmailLbl.setFont(UIStyle.FONT_SMALL);
        guestEmailLbl.setForeground(new Color(0x94, 0xA3, 0xB8));

        profileBox.add(guestNameLbl);
        profileBox.add(Box.createVerticalStrut(4));
        profileBox.add(guestEmailLbl);
        sidebar.add(profileBox);

        sidebar.add(Box.createVerticalStrut(24));

        JButton btnBrowse = createSidebarNavButton("Browse & Book Rooms", true);
        JButton btnBookings = createSidebarNavButton("My Bookings & Receipts", false);
        sidebar.add(btnBrowse);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnBookings);

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = UIStyle.createDangerButton("Log Out");
        btnLogout.setAlignmentX(LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(210, 36));
        btnLogout.addActionListener(e -> mainFrame.showAuth());
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        contentCardLayout = new CardLayout();
        contentCards = new JPanel(contentCardLayout);
        contentCards.setOpaque(false);

        btnBrowse.addActionListener(e -> {
            btnBrowse.setBackground(UIStyle.COLOR_ACCENT);
            btnBookings.setBackground(UIStyle.COLOR_SIDEBAR_HOVER);
            contentCardLayout.show(contentCards, "BROWSE");
            refreshRooms();
        });

        btnBookings.addActionListener(e -> {
            btnBookings.setBackground(UIStyle.COLOR_ACCENT);
            btnBrowse.setBackground(UIStyle.COLOR_SIDEBAR_HOVER);
            contentCardLayout.show(contentCards, "BOOKINGS");
            refreshBookings();
        });

        contentCards.add(buildBrowseView(), "BROWSE");
        contentCards.add(buildBookingsView(), "BOOKINGS");

        add(contentCards, BorderLayout.CENTER);
    }

    private JButton createSidebarNavButton(String title, boolean active) {
        JButton btn = new JButton(title);
        btn.setFont(UIStyle.FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(active ? UIStyle.COLOR_ACCENT : UIStyle.COLOR_SIDEBAR_HOVER);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(210, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildBrowseView() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Top Consolidated Filter & Booking Container
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Header Title Bar
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 4, 8, 4));

        JLabel headerLbl = new JLabel("Browse & Book Rooms");
        headerLbl.setFont(UIStyle.FONT_TITLE);
        headerLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);

        JLabel subHeaderLbl = new JLabel("Select your check-in & check-out dates to view real-time room availability and reserve instantly.");
        subHeaderLbl.setFont(UIStyle.FONT_SMALL);
        subHeaderLbl.setForeground(UIStyle.COLOR_TEXT_MUTED);

        titlePanel.add(headerLbl, BorderLayout.NORTH);
        titlePanel.add(subHeaderLbl, BorderLayout.SOUTH);
        topContainer.add(titlePanel);

        // Top Two-Row Card Container
        JPanel topCard = UIStyle.createCardPanel();
        topCard.setLayout(new BoxLayout(topCard, BoxLayout.Y_AXIS));

        // ------------------ Row 1 (Search & Dates) ------------------
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        row1.setOpaque(false);

        JLabel lblType = new JLabel("Room Type:");
        lblType.setFont(UIStyle.FONT_BOLD);
        lblType.setForeground(UIStyle.COLOR_TEXT_MAIN);
        row1.add(lblType);

        cbRoomTypeFilter = new JComboBox<>(new String[]{"All", "Standard", "Deluxe", "Suite"});
        cbRoomTypeFilter.setFont(UIStyle.FONT_REGULAR);
        cbRoomTypeFilter.setPreferredSize(new Dimension(110, 36));
        cbRoomTypeFilter.addActionListener(e -> refreshRooms());
        row1.add(cbRoomTypeFilter);

        row1.add(Box.createHorizontalStrut(8));

        JLabel lblCheckIn = new JLabel("Check-In Date:");
        lblCheckIn.setFont(UIStyle.FONT_BOLD);
        lblCheckIn.setForeground(UIStyle.COLOR_TEXT_MAIN);
        row1.add(lblCheckIn);

        tfCheckIn = UIStyle.createTextField(9);
        tfCheckIn.setEditable(false);
        tfCheckIn.setText(LocalDate.now().toString());
        tfCheckIn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tfCheckIn.setToolTipText("Click to choose Check-In date");
        tfCheckIn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openCheckInCalendar();
            }
        });
        row1.add(tfCheckIn);

        JButton btnCheckInCal = UIStyle.createSecondaryButton("📅 Select Check-In");
        btnCheckInCal.addActionListener(e -> openCheckInCalendar());
        row1.add(btnCheckInCal);

        row1.add(Box.createHorizontalStrut(8));

        JLabel lblCheckOut = new JLabel("Check-Out Date:");
        lblCheckOut.setFont(UIStyle.FONT_BOLD);
        lblCheckOut.setForeground(UIStyle.COLOR_TEXT_MAIN);
        row1.add(lblCheckOut);

        tfCheckOut = UIStyle.createTextField(9);
        tfCheckOut.setEditable(false);
        tfCheckOut.setText(LocalDate.now().plusDays(1).toString());
        tfCheckOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tfCheckOut.setToolTipText("Click to choose Check-Out date");
        tfCheckOut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openCheckOutCalendar();
            }
        });
        row1.add(tfCheckOut);

        JButton btnCheckOutCal = UIStyle.createSecondaryButton("📅 Select Check-Out");
        btnCheckOutCal.addActionListener(e -> openCheckOutCalendar());
        row1.add(btnCheckOutCal);

        row1.add(Box.createHorizontalStrut(8));

        JButton btnCheckAvail = UIStyle.createPrimaryButton("Check Availability / Refresh");
        btnCheckAvail.addActionListener(e -> refreshRooms());
        row1.add(btnCheckAvail);

        topCard.add(row1);

        // ------------------ Row 2 (Selected Room & Action) ------------------
        JPanel row2 = new JPanel(new BorderLayout(10, 0));
        row2.setOpaque(false);
        row2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyle.COLOR_BORDER),
                new EmptyBorder(8, 4, 4, 4)
        ));

        // Row 2 Left
        JPanel row2Left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row2Left.setOpaque(false);

        JLabel lblSelected = new JLabel("Selected Room No:");
        lblSelected.setFont(UIStyle.FONT_BOLD);
        lblSelected.setForeground(UIStyle.COLOR_TEXT_MAIN);
        row2Left.add(lblSelected);

        tfSelectedRoom = UIStyle.createTextField(6);
        tfSelectedRoom.setEditable(false);
        tfSelectedRoom.setHorizontalAlignment(JTextField.CENTER);
        tfSelectedRoom.setFont(UIStyle.FONT_BOLD);
        tfSelectedRoom.setForeground(UIStyle.COLOR_ACCENT);
        tfSelectedRoom.setText("-");
        row2Left.add(tfSelectedRoom);

        lblAvailabilityNotice = new JLabel("Select a room from the table below to check details");
        lblAvailabilityNotice.setFont(UIStyle.FONT_BOLD);
        lblAvailabilityNotice.setForeground(UIStyle.COLOR_TEXT_MUTED);
        row2Left.add(lblAvailabilityNotice);

        row2.add(row2Left, BorderLayout.CENTER);

        // Row 2 Right
        JPanel row2Right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        row2Right.setOpaque(false);

        lblEstimatedTotal = new JLabel("Est. Total: ₹0.00");
        lblEstimatedTotal.setFont(UIStyle.FONT_BOLD);
        lblEstimatedTotal.setForeground(UIStyle.COLOR_ACCENT);
        row2Right.add(lblEstimatedTotal);

        JButton btnBook = UIStyle.createPrimaryButton("Book & Proceed to Payment");
        btnBook.addActionListener(e -> handleBookRoom());
        row2Right.add(btnBook);

        row2.add(row2Right, BorderLayout.EAST);

        topCard.add(row2);
        topContainer.add(topCard);

        panel.add(topContainer, BorderLayout.NORTH);

        // Center: Rooms Table
        String[] columns = {"Room No", "Room Type", "Price / Night (₹)", "Status for Selected Dates", "Upcoming Schedule / Availability Info", "Description"};
        roomTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        roomTable = new JTable(roomTableModel);
        UIStyle.styleTable(roomTable);
        roomTable.getColumnModel().getColumn(3).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column sizing for clear readability
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        roomTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(210);
        roomTable.getColumnModel().getColumn(4).setPreferredWidth(380);
        roomTable.getColumnModel().getColumn(5).setPreferredWidth(200);

        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = roomTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Object val = roomTableModel.getValueAt(selectedRow, 0);
                    tfSelectedRoom.setText(String.valueOf(val));
                    updateEstimatedTotalAndNotice();
                }
            }
        });

        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        // No form at the bottom (BorderLayout.SOUTH is removed)
        return panel;
    }

    private void openCheckInCalendar() {
        LocalDate currentIn = parseDate(tfCheckIn.getText().trim(), LocalDate.now());
        List<Reservation> activeReservations = getActiveReservationsForSelectedRoom();

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        LocalDate picked = CalendarPickerDialog.showDialog(parentWindow,
                "Select Check-In Date",
                currentIn,
                LocalDate.now(),
                false,
                activeReservations);

        if (picked != null) {
            tfCheckIn.setText(picked.toString());
            LocalDate currentOut = parseDate(tfCheckOut.getText().trim(), picked.plusDays(1));
            if (!currentOut.isAfter(picked)) {
                tfCheckOut.setText(picked.plusDays(1).toString());
            }
            refreshRooms();
            updateEstimatedTotalAndNotice();
        }
    }

    private void openCheckOutCalendar() {
        LocalDate currentIn = parseDate(tfCheckIn.getText().trim(), LocalDate.now());
        LocalDate minCheckOut = currentIn.plusDays(1);
        LocalDate currentOut = parseDate(tfCheckOut.getText().trim(), minCheckOut);
        if (currentOut.isBefore(minCheckOut)) {
            currentOut = minCheckOut;
        }

        List<Reservation> activeReservations = getActiveReservationsForSelectedRoom();

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        LocalDate picked = CalendarPickerDialog.showDialog(parentWindow,
                "Select Check-Out Date",
                currentOut,
                minCheckOut,
                true,
                activeReservations);

        if (picked != null) {
            tfCheckOut.setText(picked.toString());
            refreshRooms();
            updateEstimatedTotalAndNotice();
        }
    }

    private List<Reservation> getActiveReservationsForSelectedRoom() {
        String roomStr = tfSelectedRoom.getText().trim();
        if (!roomStr.isEmpty() && !roomStr.equals("-")) {
            try {
                int roomNo = Integer.parseInt(roomStr);
                return resService.getReservationRepository().findActiveReservationsByRoomId(roomNo);
            } catch (Exception ignored) {}
        }
        return new ArrayList<>();
    }

    private LocalDate parseDate(String text, LocalDate fallback) {
        try {
            return LocalDate.parse(text);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void updateEstimatedTotalAndNotice() {
        try {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow < 0) {
                lblEstimatedTotal.setText("Est. Total: ₹0.00");
                lblAvailabilityNotice.setText("Select a room from the table below to check details");
                lblAvailabilityNotice.setForeground(UIStyle.COLOR_TEXT_MUTED);
                return;
            }

            int roomNo = (Integer) roomTableModel.getValueAt(selectedRow, 0);
            String priceStr = String.valueOf(roomTableModel.getValueAt(selectedRow, 2)).replace("₹", "").replace("$", "").trim();
            double price = Double.parseDouble(priceStr);

            LocalDate in = parseDate(tfCheckIn.getText().trim(), LocalDate.now());
            LocalDate out = parseDate(tfCheckOut.getText().trim(), in.plusDays(1));

            long nights = ChronoUnit.DAYS.between(in, out);
            if (nights <= 0) {
                lblEstimatedTotal.setText("Invalid Dates");
                lblAvailabilityNotice.setText("Check-Out date must be after Check-In date.");
                lblAvailabilityNotice.setForeground(UIStyle.COLOR_DANGER);
                return;
            }

            double total = nights * price;
            lblEstimatedTotal.setText(String.format("Est. Total: ₹%.2f (%d night%s)", total, nights, nights > 1 ? "s" : ""));

            Room room = roomService.findRoom(roomNo);
            if (room != null && "Maintenance".equalsIgnoreCase(room.getStatus())) {
                lblAvailabilityNotice.setText("✖ Room #" + roomNo + " is currently under maintenance.");
                lblAvailabilityNotice.setForeground(UIStyle.COLOR_WARNING);
                return;
            }

            // Check reservations for this room
            List<Reservation> activeList = resService.getReservationRepository().findActiveReservationsByRoomId(roomNo);
            Reservation conflictingRes = null;
            Reservation nextFutureRes = null;

            for (Reservation res : activeList) {
                LocalDate rIn = res.getCheckInDate();
                LocalDate rOut = res.getCheckOutDate();
                if (rIn == null || rOut == null) continue;

                // Overlap check
                if (in.isBefore(rOut) && out.isAfter(rIn)) {
                    conflictingRes = res;
                    break;
                }

                // Future reservation after customer's check-in
                if (rIn.isAfter(in) || rIn.isEqual(out)) {
                    if (nextFutureRes == null || rIn.isBefore(nextFutureRes.getCheckInDate())) {
                        nextFutureRes = res;
                    }
                }
            }

            if (conflictingRes != null) {
                lblAvailabilityNotice.setText("✖ Room #" + roomNo + " is already reserved from " + conflictingRes.getCheckInDate() + " to " + conflictingRes.getCheckOutDate() + "!");
                lblAvailabilityNotice.setForeground(UIStyle.COLOR_DANGER);
            } else if (nextFutureRes != null) {
                lblAvailabilityNotice.setText("✔ Available for your stay! Note: Another guest arrives on " + nextFutureRes.getCheckInDate() + ", so checkout must be on or before " + nextFutureRes.getCheckInDate() + ".");
                lblAvailabilityNotice.setForeground(UIStyle.COLOR_SUCCESS);
            } else {
                lblAvailabilityNotice.setText("✔ Room #" + roomNo + " is fully available for your selected dates!");
                lblAvailabilityNotice.setForeground(UIStyle.COLOR_SUCCESS);
            }

        } catch (Exception e) {
            lblEstimatedTotal.setText("Est. Total: ₹0.00");
        }
    }

    private void handleBookRoom() {
        if (currentCustomer == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.", "Auth Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roomStr = tfSelectedRoom.getText().trim();
        if (roomStr.isEmpty() || roomStr.equals("-")) {
            JOptionPane.showMessageDialog(this, "Please select a room from the table.", "Room Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int roomNo = Integer.parseInt(roomStr);
        Room room = roomService.findRoom(roomNo);
        if (room == null || "Maintenance".equalsIgnoreCase(room.getStatus())) {
            JOptionPane.showMessageDialog(this, "Room " + roomNo + " is currently under maintenance.", "Unavailable", JOptionPane.ERROR_MESSAGE);
            refreshRooms();
            return;
        }

        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(tfCheckIn.getText().trim());
            checkOut = LocalDate.parse(tfCheckOut.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please select dates using the calendar.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (checkIn.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Check-in date cannot be in the past.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            JOptionPane.showMessageDialog(this, "Check-out date must be after check-in date.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Reservation res = ReservationService.createReservation(currentCustomer, room, checkIn, checkOut);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Failed to create reservation.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            PaymentDialog dialog = new PaymentDialog(parentWindow, res, () -> {
                refreshRooms();
                refreshBookings();
                contentCardLayout.show(contentCards, "BOOKINGS");
            });
            dialog.setVisible(true);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Room Unavailable", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void refreshRooms() {
        int previousSelectedRoom = -1;
        try {
            String sel = tfSelectedRoom != null ? tfSelectedRoom.getText().trim() : "";
            if (!sel.isEmpty() && !sel.equals("-")) {
                previousSelectedRoom = Integer.parseInt(sel);
            }
        } catch (Exception ignored) {}

        roomTableModel.setRowCount(0);
        String typeFilter = cbRoomTypeFilter != null ? (String) cbRoomTypeFilter.getSelectedItem() : "All";
        List<Room> rooms;
        if (typeFilter == null || "All".equalsIgnoreCase(typeFilter)) {
            rooms = roomService.listAllRooms();
        } else {
            rooms = roomService.getRoomRepository().findByType(typeFilter);
        }

        LocalDate checkIn = parseDate(tfCheckIn != null ? tfCheckIn.getText().trim() : "", LocalDate.now());
        LocalDate checkOut = parseDate(tfCheckOut != null ? tfCheckOut.getText().trim() : "", checkIn.plusDays(1));
        if (!checkOut.isAfter(checkIn)) {
            checkOut = checkIn.plusDays(1);
        }

        LocalDate today = LocalDate.now();

        int rowToSelect = -1;
        int rowIndex = 0;

        for (Room r : rooms) {
            int rNo = r.getRoomNo();
            String rType = r.getRoomType();
            double price = r.getPrice();
            String desc = r.getDescription();

            String statusForDates;
            String scheduleInfo;

            if ("Maintenance".equalsIgnoreCase(r.getStatus())) {
                statusForDates = "MAINTENANCE";
                scheduleInfo = "Under Maintenance";
            } else {
                List<Reservation> activeReservations = resService.getReservationRepository().findActiveReservationsByRoomId(rNo);

                // Overlap check
                Reservation conflictingRes = null;
                Reservation nextFutureRes = null;

                for (Reservation res : activeReservations) {
                    LocalDate rIn = res.getCheckInDate();
                    LocalDate rOut = res.getCheckOutDate();
                    if (rIn == null || rOut == null) continue;

                    // Overlap formula: checkIn < rOut && checkOut > rIn
                    if (checkIn.isBefore(rOut) && checkOut.isAfter(rIn)) {
                        conflictingRes = res;
                        break;
                    }

                    // Find nearest upcoming future reservation after or on check-in
                    if (rIn.isAfter(checkIn) || rIn.isEqual(checkOut) || !rIn.isBefore(today)) {
                        if (nextFutureRes == null || rIn.isBefore(nextFutureRes.getCheckInDate())) {
                            nextFutureRes = res;
                        }
                    }
                }

                if (conflictingRes != null) {
                    // Case 3 — Customer's selected dates overlap with an existing reservation
                    statusForDates = "BOOKED FOR SELECTED DATES";
                    scheduleInfo = "Reserved: " + conflictingRes.getCheckInDate() + " to " + conflictingRes.getCheckOutDate() +
                                   " (Available before " + conflictingRes.getCheckInDate() + " or after " + conflictingRes.getCheckOutDate() + ")";
                } else if (nextFutureRes != null) {
                    // Case 2 — Room has a future reservation, and Customer's selected dates do NOT overlap
                    statusForDates = "AVAILABLE (Until " + nextFutureRes.getCheckInDate() + ")";
                    scheduleInfo = "Free now until " + nextFutureRes.getCheckInDate() + " | Next Booking: " +
                                   nextFutureRes.getCheckInDate() + " to " + nextFutureRes.getCheckOutDate();
                } else {
                    // Case 1 — Room has NO active reservations
                    statusForDates = "AVAILABLE";
                    scheduleInfo = "Open — Available for all dates";
                }
            }

            roomTableModel.addRow(new Object[]{
                    rNo,
                    rType,
                    String.format("₹%.2f", price),
                    statusForDates,
                    scheduleInfo,
                    desc
            });

            if (rNo == previousSelectedRoom) {
                rowToSelect = rowIndex;
            }
            rowIndex++;
        }

        if (rowToSelect >= 0 && rowToSelect < roomTable.getRowCount()) {
            roomTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }

        updateEstimatedTotalAndNotice();
    }

    private JPanel buildBookingsView() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel headerLbl = new JLabel("My Bookings & Billing History");
        headerLbl.setFont(UIStyle.FONT_TITLE);
        headerLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);
        topBar.add(headerLbl, BorderLayout.WEST);

        JButton refreshBtn = UIStyle.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshBookings());
        topBar.add(refreshBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Res ID", "Room No", "Room Type", "Check-In", "Check-Out", "Nights", "Total Price", "Booking Status", "Payment Status"};
        bookingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        bookingsTable = new JTable(bookingsTableModel);
        UIStyle.styleTable(bookingsTable);
        bookingsTable.getColumnModel().getColumn(7).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        bookingsTable.getColumnModel().getColumn(8).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        actions.setOpaque(false);

        JButton viewBillBtn = UIStyle.createSecondaryButton("View Itemized Bill");
        viewBillBtn.addActionListener(e -> handleViewBill());

        JButton cancelBtn = UIStyle.createDangerButton("Cancel Selected Reservation");
        cancelBtn.addActionListener(e -> handleCancelBooking());

        actions.add(viewBillBtn);
        actions.add(cancelBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void handleViewBill() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int resId = (Integer) bookingsTableModel.getValueAt(row, 0);
        Reservation res = resService.getReservationById(resId);
        if (res == null) {
            JOptionPane.showMessageDialog(this, "Reservation not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Payment pay = res.getPayment();
        if (pay == null) {
            pay = payService.getPaymentRepository().findByReservationId(resId);
        }
        if (pay == null) {
            JOptionPane.showMessageDialog(this, "No payment record attached to Reservation #" + resId, "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String receipt = PaymentDialog.buildReceiptString(pay);
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Bill Receipt — Reservation #" + resId, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        d.setSize(560, 600);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JTextArea ta = new JTextArea(receipt);
        ta.setFont(UIStyle.FONT_MONO);
        ta.setEditable(false);
        ta.setBorder(new EmptyBorder(16, 16, 16, 16));
        d.add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = UIStyle.createPrimaryButton("Close");
        close.addActionListener(ev -> d.dispose());
        p.add(close);
        d.add(p, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void handleCancelBooking() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int resId = (Integer) bookingsTableModel.getValueAt(row, 0);
        String status = (String) bookingsTableModel.getValueAt(row, 7);
        if ("Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This reservation is already cancelled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel Reservation #" + resId + "?\nRoom will be made available again and refund issued.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = resService.cancelReservation(resId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Reservation #" + resId + " has been cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                refreshRooms();
                refreshBookings();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel reservation.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshBookings() {
        bookingsTableModel.setRowCount(0);
        if (currentCustomer == null) return;
        List<Reservation> list = resService.getReservationsForCustomer(currentCustomer.getCustomerId());
        for (Reservation r : list) {
            long nights = 1;
            if (r.getCheckInDate() != null && r.getCheckOutDate() != null) {
                nights = ChronoUnit.DAYS.between(r.getCheckInDate(), r.getCheckOutDate());
                if (nights <= 0) nights = 1;
            }
            int roomNo = r.getRoom() != null ? r.getRoom().getRoomNo() : 0;
            String roomType = r.getRoom() != null ? r.getRoom().getRoomType() : "N/A";
            String payStatus = (r.getPayment() != null) ? r.getPayment().getPaymentStatus() : "Unpaid";

            bookingsTableModel.addRow(new Object[]{
                    r.getReservationId(),
                    roomNo,
                    roomType,
                    r.getCheckInDate(),
                    r.getCheckOutDate(),
                    nights,
                    String.format("₹%.2f", r.getTotalAmount()),
                    r.getStatus(),
                    payStatus
            });
        }
    }
}

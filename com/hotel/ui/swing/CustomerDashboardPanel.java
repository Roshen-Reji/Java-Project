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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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

    // Browse view components
    private JComboBox<String> cbRoomTypeFilter;
    private DefaultTableModel roomTableModel;
    private JTable roomTable;
    private JTextField tfSelectedRoom;
    private JTextField tfCheckIn;
    private JTextField tfCheckOut;
    private JLabel lblEstimatedTotal;

    // Bookings view components
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
        // --- Left Sidebar ---
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

        // Profile Card
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

        // --- Center Content Area ---
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

    // --- View 1: Browse & Book Rooms ---
    private JPanel buildBrowseView() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Header & Filter Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel headerLbl = new JLabel("Available Rooms & Suites");
        headerLbl.setFont(UIStyle.FONT_TITLE);
        headerLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);
        topBar.add(headerLbl, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Room Category:"));
        cbRoomTypeFilter = new JComboBox<>(new String[]{"All", "Standard", "Deluxe", "Suite"});
        cbRoomTypeFilter.setFont(UIStyle.FONT_REGULAR);
        cbRoomTypeFilter.addActionListener(e -> refreshRooms());
        filterPanel.add(cbRoomTypeFilter);

        JButton refreshBtn = UIStyle.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshRooms());
        filterPanel.add(refreshBtn);

        topBar.add(filterPanel, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"Room No", "Room Type", "Price / Night", "Status", "Description"};
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

        roomTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow >= 0) {
                int roomNo = (Integer) roomTableModel.getValueAt(selectedRow, 0);
                tfSelectedRoom.setText(String.valueOf(roomNo));
                updateEstimatedTotal();
            }
        });

        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        // Bottom Booking Card Panel
        JPanel bookingCard = UIStyle.createCardPanel();
        bookingCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        bookingCard.add(new JLabel("Selected Room:"), gbc);
        gbc.gridx = 1;
        tfSelectedRoom = UIStyle.createTextField(6);
        tfSelectedRoom.setEditable(false);
        bookingCard.add(tfSelectedRoom, gbc);

        gbc.gridx = 2;
        bookingCard.add(new JLabel("Check-In (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3;
        tfCheckIn = UIStyle.createTextField(10);
        tfCheckIn.setText(LocalDate.now().plusDays(1).toString());
        tfCheckIn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) { updateEstimatedTotal(); }
        });
        bookingCard.add(tfCheckIn, gbc);

        gbc.gridx = 4;
        bookingCard.add(new JLabel("Check-Out (YYYY-MM-DD):"), gbc);
        gbc.gridx = 5;
        tfCheckOut = UIStyle.createTextField(10);
        tfCheckOut.setText(LocalDate.now().plusDays(3).toString());
        tfCheckOut.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) { updateEstimatedTotal(); }
        });
        bookingCard.add(tfCheckOut, gbc);

        gbc.gridx = 6;
        lblEstimatedTotal = new JLabel("Est. Total: $0.00");
        lblEstimatedTotal.setFont(UIStyle.FONT_BOLD);
        lblEstimatedTotal.setForeground(UIStyle.COLOR_ACCENT);
        bookingCard.add(lblEstimatedTotal, gbc);

        gbc.gridx = 7;
        JButton btnBook = UIStyle.createPrimaryButton("Book & Proceed to Payment");
        btnBook.addActionListener(e -> handleBookRoom());
        bookingCard.add(btnBook, gbc);

        panel.add(bookingCard, BorderLayout.SOUTH);

        return panel;
    }

    private void updateEstimatedTotal() {
        try {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow < 0) {
                lblEstimatedTotal.setText("Est. Total: $0.00");
                return;
            }
            String priceStr = String.valueOf(roomTableModel.getValueAt(selectedRow, 2)).replace("$", "").trim();
            double price = Double.parseDouble(priceStr);

            LocalDate in = LocalDate.parse(tfCheckIn.getText().trim());
            LocalDate out = LocalDate.parse(tfCheckOut.getText().trim());
            long nights = ChronoUnit.DAYS.between(in, out);
            if (nights <= 0) {
                lblEstimatedTotal.setText("Invalid Dates");
            } else {
                double total = nights * price;
                lblEstimatedTotal.setText(String.format("Est. Total: $%.2f (%d nights)", total, nights));
            }
        } catch (Exception e) {
            lblEstimatedTotal.setText("Est. Total: $0.00");
        }
    }

    private void handleBookRoom() {
        if (currentCustomer == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.", "Auth Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roomStr = tfSelectedRoom.getText().trim();
        if (roomStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a room from the table above.", "Room Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int roomNo = Integer.parseInt(roomStr);
        Room room = roomService.findRoom(roomNo);
        if (room == null || !room.checkAvailability()) {
            JOptionPane.showMessageDialog(this, "Room " + roomNo + " is no longer available for booking.", "Unavailable", JOptionPane.ERROR_MESSAGE);
            refreshRooms();
            return;
        }

        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(tfCheckIn.getText().trim());
            checkOut = LocalDate.parse(tfCheckOut.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            JOptionPane.showMessageDialog(this, "Check-out date must be after check-in date.", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create the reservation in pending status
        Reservation res = ReservationService.createReservation(currentCustomer, room, checkIn, checkOut);
        if (res == null) {
            JOptionPane.showMessageDialog(this, "Failed to create reservation.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Open Payment and Itemized Bill Dialog
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        PaymentDialog dialog = new PaymentDialog(parentWindow, res, () -> {
            refreshRooms();
            refreshBookings();
            contentCardLayout.show(contentCards, "BOOKINGS");
        });
        dialog.setVisible(true);
    }

    // --- View 2: My Bookings & Receipts ---
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

        // Actions footer
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

    public void refreshRooms() {
        roomTableModel.setRowCount(0);
        String typeFilter = cbRoomTypeFilter != null ? (String) cbRoomTypeFilter.getSelectedItem() : "All";
        List<Room> rooms;
        if (typeFilter == null || "All".equalsIgnoreCase(typeFilter)) {
            rooms = roomService.listAllRooms();
        } else {
            rooms = roomService.getRoomRepository().findByType(typeFilter);
        }

        for (Room r : rooms) {
            roomTableModel.addRow(new Object[]{
                    r.getRoomNo(),
                    r.getRoomType(),
                    String.format("$%.2f", r.getPrice()),
                    r.getStatus(),
                    r.getDescription()
            });
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
                    String.format("$%.2f", r.getTotalAmount()),
                    r.getStatus(),
                    payStatus
            });
        }
    }
}

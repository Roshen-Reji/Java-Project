package com.hotel.ui.swing;

import com.hotel.domain.Customer;
import com.hotel.domain.HotelStaff;
import com.hotel.domain.Reservation;
import com.hotel.domain.Room;
import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomManagementService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class StaffDashboardPanel extends JPanel {

    private final HotelAppFrame mainFrame;
    private final AuthenticationCustomerService authService;
    private final RoomManagementService roomService;
    private final ReservationService resService;

    private HotelStaff currentStaff;
    private JLabel staffNameLbl;
    private JLabel staffRoleLbl;

    private DefaultTableModel resTableModel;
    private JTable resTable;

    private DefaultTableModel roomTableModel;
    private JTable roomTable;

    private DefaultTableModel custTableModel;
    private JTable custTable;
    private JTextField tfCustSearch;

    public StaffDashboardPanel(HotelAppFrame mainFrame,
                               AuthenticationCustomerService authService,
                               RoomManagementService roomService,
                               ReservationService resService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        this.roomService = roomService;
        this.resService = resService;

        setLayout(new BorderLayout());
        setBackground(UIStyle.COLOR_BG);

        initUI();
    }

    public void setStaff(HotelStaff staff) {
        this.currentStaff = staff;
        if (staff != null) {
            staffNameLbl.setText(staff.getName());
            staffRoleLbl.setText("Role: " + staff.getRole() + " (ID #" + staff.getStaffId() + ")");
            refreshAll();
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

        JLabel subBrand = new JLabel("Hotel Staff Portal");
        subBrand.setFont(UIStyle.FONT_SMALL);
        subBrand.setForeground(UIStyle.COLOR_CYAN);
        subBrand.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(brandLbl);
        sidebar.add(subBrand);
        sidebar.add(Box.createVerticalStrut(24));

        // Staff Info Card
        JPanel staffCard = new JPanel();
        staffCard.setLayout(new BoxLayout(staffCard, BoxLayout.Y_AXIS));
        staffCard.setBackground(UIStyle.COLOR_SIDEBAR_HOVER);
        staffCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        staffCard.setAlignmentX(LEFT_ALIGNMENT);
        staffCard.setMaximumSize(new Dimension(210, 70));

        staffNameLbl = new JLabel("Staff Member");
        staffNameLbl.setFont(UIStyle.FONT_BOLD);
        staffNameLbl.setForeground(Color.WHITE);

        staffRoleLbl = new JLabel("Role: Concierge");
        staffRoleLbl.setFont(UIStyle.FONT_SMALL);
        staffRoleLbl.setForeground(new Color(0x94, 0xA3, 0xB8));

        staffCard.add(staffNameLbl);
        staffCard.add(Box.createVerticalStrut(4));
        staffCard.add(staffRoleLbl);
        sidebar.add(staffCard);

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = UIStyle.createDangerButton("Log Out");
        btnLogout.setAlignmentX(LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(210, 36));
        btnLogout.addActionListener(e -> mainFrame.showAuth());
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        // --- Tabbed Content Area ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIStyle.FONT_BOLD);
        tabbedPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        tabbedPane.addTab("All Reservations", buildReservationsTab());
        tabbedPane.addTab("Room Status Management", buildRoomManagementTab());
        tabbedPane.addTab("Customer Directory", buildCustomerDirectoryTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- Tab 1: All Reservations ---
    private JPanel buildReservationsTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Reservation Operations & Front Desk");
        title.setFont(UIStyle.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);

        JButton refreshBtn = UIStyle.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshReservations());
        topBar.add(refreshBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Res ID", "Customer", "Phone", "Room No", "Room Type", "Check-In", "Check-Out", "Amount", "Status", "Payment"};
        resTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        resTable = new JTable(resTableModel);
        UIStyle.styleTable(resTable);
        resTable.getColumnModel().getColumn(8).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        resTable.getColumnModel().getColumn(9).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        resTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(resTable), BorderLayout.CENTER);

        // Action Toolbar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actions.setOpaque(false);

        JButton btnConfirm = UIStyle.createPrimaryButton("Confirm Booking");
        btnConfirm.addActionListener(e -> handleConfirmBooking());

        JButton btnCheckIn = UIStyle.createSecondaryButton("Check-In (Mark Occupied)");
        btnCheckIn.addActionListener(e -> handleCheckIn());

        JButton btnCheckOut = UIStyle.createSecondaryButton("Check-Out (Free Room)");
        btnCheckOut.addActionListener(e -> handleCheckOut());

        JButton btnCancel = UIStyle.createDangerButton("Cancel Reservation");
        btnCancel.addActionListener(e -> handleCancelRes());

        actions.add(btnConfirm);
        actions.add(btnCheckIn);
        actions.add(btnCheckOut);
        actions.add(btnCancel);

        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void handleConfirmBooking() {
        int row = resTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation to confirm.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int resId = (Integer) resTableModel.getValueAt(row, 0);
        boolean ok = resService.confirmReservation(resId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Reservation #" + resId + " confirmed!", "Confirmed", JOptionPane.INFORMATION_MESSAGE);
            refreshReservations();
            refreshRooms();
        } else {
            JOptionPane.showMessageDialog(this, "Could not confirm reservation #" + resId, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckIn() {
        int row = resTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation for Check-In.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int resId = (Integer) resTableModel.getValueAt(row, 0);
        Reservation r = resService.getReservationById(resId);
        if (r == null || r.getRoom() == null) return;

        RoomManagementService.updateStatus(r.getRoom().getRoomNo(), "Occupied");
        JOptionPane.showMessageDialog(this, "Guest checked into Room " + r.getRoom().getRoomNo() + ". Status set to OCCUPIED.", "Check-In Complete", JOptionPane.INFORMATION_MESSAGE);
        refreshReservations();
        refreshRooms();
    }

    private void handleCheckOut() {
        int row = resTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation for Check-Out.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int resId = (Integer) resTableModel.getValueAt(row, 0);
        Reservation r = resService.getReservationById(resId);
        if (r == null || r.getRoom() == null) return;

        RoomManagementService.updateStatus(r.getRoom().getRoomNo(), "Available");
        r.setStatus("Completed");
        resService.getReservationRepository().update(r);
        JOptionPane.showMessageDialog(this, "Check-Out complete. Room " + r.getRoom().getRoomNo() + " is now AVAILABLE.", "Check-Out", JOptionPane.INFORMATION_MESSAGE);
        refreshReservations();
        refreshRooms();
    }

    private void handleCancelRes() {
        int row = resTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation to cancel.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int resId = (Integer) resTableModel.getValueAt(row, 0);
        int conf = JOptionPane.showConfirmDialog(this, "Cancel Reservation #" + resId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            resService.cancelReservation(resId);
            refreshReservations();
            refreshRooms();
        }
    }

    // --- Tab 2: Room Status Management ---
    private JPanel buildRoomManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Live Room Status Overview");
        title.setFont(UIStyle.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);

        JButton refreshBtn = UIStyle.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshRooms());
        topBar.add(refreshBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Room No", "Room Type", "Price", "Status", "Description"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        roomTable = new JTable(roomTableModel);
        UIStyle.styleTable(roomTable);
        roomTable.getColumnModel().getColumn(3).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        // Status Toggle Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        actions.setOpaque(false);

        JButton btnAvail = UIStyle.createSecondaryButton("Set Available");
        btnAvail.addActionListener(e -> setRoomStatus("Available"));

        JButton btnOcc = UIStyle.createSecondaryButton("Set Occupied");
        btnOcc.addActionListener(e -> setRoomStatus("Occupied"));

        JButton btnMaint = UIStyle.createSecondaryButton("Set Maintenance");
        btnMaint.addActionListener(e -> setRoomStatus("Maintenance"));

        actions.add(btnAvail);
        actions.add(btnOcc);
        actions.add(btnMaint);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void setRoomStatus(String status) {
        int row = roomTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a room first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int roomNo = (Integer) roomTableModel.getValueAt(row, 0);
        RoomManagementService.updateStatus(roomNo, status);
        refreshRooms();
    }

    // --- Tab 3: Customer Directory ---
    private JPanel buildCustomerDirectoryTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Registered Guest Directory");
        title.setFont(UIStyle.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Search Guest:"));
        tfCustSearch = UIStyle.createTextField(14);
        searchBar.add(tfCustSearch);

        JButton searchBtn = UIStyle.createSecondaryButton("Search");
        searchBtn.addActionListener(e -> filterCustomers());
        searchBar.add(searchBtn);

        JButton refreshBtn = UIStyle.createSecondaryButton("All");
        refreshBtn.addActionListener(e -> { tfCustSearch.setText(""); refreshCustomers(); });
        searchBar.add(refreshBtn);

        topBar.add(searchBar, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Customer ID", "Name", "Email", "Phone", "Address"};
        custTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        custTable = new JTable(custTableModel);
        UIStyle.styleTable(custTable);
        panel.add(new JScrollPane(custTable), BorderLayout.CENTER);

        return panel;
    }

    private void filterCustomers() {
        String query = tfCustSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshCustomers();
            return;
        }
        custTableModel.setRowCount(0);
        List<Customer> list = authService.listAllCustomers();
        for (Customer c : list) {
            if (c.getName().toLowerCase().contains(query) ||
                c.getEmail().toLowerCase().contains(query) ||
                c.getPhone().toLowerCase().contains(query)) {
                custTableModel.addRow(new Object[]{
                        c.getCustomerId(),
                        c.getName(),
                        c.getEmail(),
                        c.getPhone(),
                        c.getAddress()
                });
            }
        }
    }

    public void refreshAll() {
        refreshReservations();
        refreshRooms();
        refreshCustomers();
    }

    public void refreshReservations() {
        resTableModel.setRowCount(0);
        List<Reservation> list = resService.getAllReservations();
        for (Reservation r : list) {
            String custName = r.getCustomer() != null ? r.getCustomer().getName() : "N/A";
            String phone = r.getCustomer() != null ? r.getCustomer().getPhone() : "N/A";
            int roomNo = r.getRoom() != null ? r.getRoom().getRoomNo() : 0;
            String roomType = r.getRoom() != null ? r.getRoom().getRoomType() : "N/A";
            String payStatus = r.getPayment() != null ? r.getPayment().getPaymentStatus() : "Unpaid";

            resTableModel.addRow(new Object[]{
                    r.getReservationId(),
                    custName,
                    phone,
                    roomNo,
                    roomType,
                    r.getCheckInDate(),
                    r.getCheckOutDate(),
                    String.format("$%.2f", r.getTotalAmount()),
                    r.getStatus(),
                    payStatus
            });
        }
    }

    public void refreshRooms() {
        roomTableModel.setRowCount(0);
        List<Room> rooms = roomService.listAllRooms();
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

    public void refreshCustomers() {
        custTableModel.setRowCount(0);
        List<Customer> customers = authService.listAllCustomers();
        for (Customer c : customers) {
            custTableModel.addRow(new Object[]{
                    c.getCustomerId(),
                    c.getName(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getAddress()
            });
        }
    }
}

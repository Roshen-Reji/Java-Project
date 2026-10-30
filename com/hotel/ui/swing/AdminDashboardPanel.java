package com.hotel.ui.swing;

import com.hotel.domain.Admin;
import com.hotel.domain.HotelStaff;
import com.hotel.domain.Payment;
import com.hotel.domain.Reservation;
import com.hotel.domain.Room;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomManagementService;
import com.hotel.service.StaffService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

public class AdminDashboardPanel extends JPanel {

    private final HotelAppFrame mainFrame;
    private final RoomManagementService roomService;
    private final StaffService staffService;
    private final ReservationService resService;
    private final PaymentService payService;

    private Admin currentAdmin;

    private DefaultTableModel roomTableModel;
    private JTable roomTable;
    private JTextField tfRoomNo;
    private JComboBox<String> cbRoomType;
    private JTextField tfRoomPrice;
    private JComboBox<String> cbRoomStatus;
    private JTextField tfRoomDesc;
    private JTextField tfLinkedResId;
    private JButton btnViewLinkedRes;
    private Integer currentLinkedResId = null;
    private boolean isPopulatingFromTable = false;

    private DefaultTableModel staffTableModel;
    private JTable staffTable;
    private JTextField tfStaffId;
    private JTextField tfStaffName;
    private JComboBox<String> cbStaffRole;
    private JTextField tfStaffEmail;
    private JTextField tfStaffPhone;
    private JTextField tfStaffPass;

    private JLabel lblTotalBookings;
    private JLabel lblAvailRooms;
    private JLabel lblTotalRevenue;
    private DefaultTableModel paymentTableModel;
    private JTable paymentTable;

    public AdminDashboardPanel(HotelAppFrame mainFrame,
                               RoomManagementService roomService,
                               StaffService staffService,
                               ReservationService resService,
                               PaymentService payService) {
        this.mainFrame = mainFrame;
        this.roomService = roomService;
        this.staffService = staffService;
        this.resService = resService;
        this.payService = payService;

        setLayout(new BorderLayout());
        setBackground(UIStyle.COLOR_BG);

        initUI();
    }

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        refreshAll();
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

        JLabel subBrand = new JLabel("Administrator Portal");
        subBrand.setFont(UIStyle.FONT_SMALL);
        subBrand.setForeground(new Color(0x38, 0xBD, 0xF8));
        subBrand.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(brandLbl);
        sidebar.add(subBrand);
        sidebar.add(Box.createVerticalStrut(24));

        JPanel adminCard = new JPanel();
        adminCard.setLayout(new BoxLayout(adminCard, BoxLayout.Y_AXIS));
        adminCard.setBackground(UIStyle.COLOR_SIDEBAR_HOVER);
        adminCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        adminCard.setAlignmentX(LEFT_ALIGNMENT);
        adminCard.setMaximumSize(new Dimension(210, 70));

        JLabel aName = new JLabel("Administrator");
        aName.setFont(UIStyle.FONT_BOLD);
        aName.setForeground(Color.WHITE);

        JLabel aPriv = new JLabel("Master Root Access");
        aPriv.setFont(UIStyle.FONT_SMALL);
        aPriv.setForeground(new Color(0x94, 0xA3, 0xB8));

        adminCard.add(aName);
        adminCard.add(Box.createVerticalStrut(4));
        adminCard.add(aPriv);
        sidebar.add(adminCard);

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = UIStyle.createDangerButton("Log Out");
        btnLogout.setAlignmentX(LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(210, 36));
        btnLogout.addActionListener(e -> mainFrame.showAuth());
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIStyle.FONT_BOLD);
        tabbedPane.setBorder(new EmptyBorder(12, 12, 12, 12));

        tabbedPane.addTab("Manage Rooms", buildRoomsTab());
        tabbedPane.addTab("Manage Staff & Admins", buildStaffTab());
        tabbedPane.addTab("Payments & Revenue Overview", buildRevenueTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildRoomsTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Hotel Inventory — Room Management");
        title.setFont(UIStyle.FONT_TITLE);
        top.add(title, BorderLayout.WEST);

        JButton refBtn = UIStyle.createSecondaryButton("Refresh Table");
        refBtn.addActionListener(e -> refreshRooms());
        top.add(refBtn, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"Room No", "Room Type", "Price (₹)", "Status", "Reservation ID", "Description"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        roomTable = new JTable(roomTableModel);
        UIStyle.styleTable(roomTable);
        roomTable.getColumnModel().getColumn(3).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        roomTable.getColumnModel().getColumn(4).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = roomTable.getSelectedRow();
                if (row >= 0) {
                    isPopulatingFromTable = true;
                    try {
                        tfRoomNo.setText(String.valueOf(roomTableModel.getValueAt(row, 0)));
                        String type = String.valueOf(roomTableModel.getValueAt(row, 1)).trim().toUpperCase();
                        cbRoomType.setSelectedItem(type);
                        String pStr = String.valueOf(roomTableModel.getValueAt(row, 2)).replaceAll("[^0-9.]", "").trim();
                        tfRoomPrice.setText(pStr);
                        String status = String.valueOf(roomTableModel.getValueAt(row, 3)).trim().toUpperCase();
                        cbRoomStatus.setSelectedItem(status);
                        String resIdVal = String.valueOf(roomTableModel.getValueAt(row, 4));
                        Object descVal = roomTableModel.getValueAt(row, 5);
                        tfRoomDesc.setText(descVal != null ? String.valueOf(descVal) : "");

                        if (resIdVal != null && !resIdVal.equals("—") && !resIdVal.trim().isEmpty()) {
                            tfLinkedResId.setText(resIdVal);
                            try {
                                currentLinkedResId = Integer.parseInt(resIdVal.replace("#", "").trim());
                                btnViewLinkedRes.setEnabled(true);
                            } catch (Exception parseEx) {
                                currentLinkedResId = null;
                                btnViewLinkedRes.setEnabled(false);
                            }
                        } else {
                            tfLinkedResId.setText("—");
                            currentLinkedResId = null;
                            btnViewLinkedRes.setEnabled(false);
                        }
                    } finally {
                        isPopulatingFromTable = false;
                    }
                }
            }
        });

        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        JPanel formCard = UIStyle.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;

        JLabel lblRoomNo = new JLabel("Room Number:");
        lblRoomNo.setFont(UIStyle.FONT_BOLD);
        lblRoomNo.setForeground(UIStyle.COLOR_TEXT_MAIN);
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        formCard.add(lblRoomNo, g);

        tfRoomNo = UIStyle.createTextField(12);
        g.gridx = 1; g.gridy = 0; g.weightx = 1.0;
        formCard.add(tfRoomNo, g);

        JLabel lblRoomType = new JLabel("Room Type:");
        lblRoomType.setFont(UIStyle.FONT_BOLD);
        lblRoomType.setForeground(UIStyle.COLOR_TEXT_MAIN);
        g.gridx = 2; g.gridy = 0; g.weightx = 0;
        formCard.add(lblRoomType, g);

        cbRoomType = new JComboBox<>(new String[]{"STANDARD", "DELUXE", "SUITE"});
        cbRoomType.setFont(UIStyle.FONT_REGULAR);
        cbRoomType.setPreferredSize(new Dimension(160, 38));
        cbRoomType.addActionListener(e -> {
            if (isPopulatingFromTable) return;
            String selected = (String) cbRoomType.getSelectedItem();
            if ("STANDARD".equalsIgnoreCase(selected)) {
                tfRoomPrice.setText("2500.0");
            } else if ("DELUXE".equalsIgnoreCase(selected)) {
                tfRoomPrice.setText("4500.0");
            } else if ("SUITE".equalsIgnoreCase(selected)) {
                tfRoomPrice.setText("8000.0");
            }
        });
        g.gridx = 3; g.gridy = 0; g.weightx = 1.0;
        formCard.add(cbRoomType, g);

        JLabel lblPrice = new JLabel("Price per Night (₹):");
        lblPrice.setFont(UIStyle.FONT_BOLD);
        lblPrice.setForeground(UIStyle.COLOR_TEXT_MAIN);
        g.gridx = 4; g.gridy = 0; g.weightx = 0;
        formCard.add(lblPrice, g);

        tfRoomPrice = UIStyle.createTextField(12);
        tfRoomPrice.setText("2500.0");
        g.gridx = 5; g.gridy = 0; g.weightx = 1.0;
        formCard.add(tfRoomPrice, g);

        JLabel lblStatus = new JLabel("Room Status:");
        lblStatus.setFont(UIStyle.FONT_BOLD);
        lblStatus.setForeground(UIStyle.COLOR_TEXT_MAIN);
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        formCard.add(lblStatus, g);

        cbRoomStatus = new JComboBox<>(new String[]{"AVAILABLE", "OCCUPIED", "BOOKED", "MAINTENANCE"});
        cbRoomStatus.setFont(UIStyle.FONT_REGULAR);
        cbRoomStatus.setPreferredSize(new Dimension(160, 38));
        g.gridx = 1; g.gridy = 1; g.weightx = 1.0;
        formCard.add(cbRoomStatus, g);

        JLabel lblDesc = new JLabel("Description:");
        lblDesc.setFont(UIStyle.FONT_BOLD);
        lblDesc.setForeground(UIStyle.COLOR_TEXT_MAIN);
        g.gridx = 2; g.gridy = 1; g.weightx = 0;
        formCard.add(lblDesc, g);

        tfRoomDesc = UIStyle.createTextField(18);
        g.gridx = 3; g.gridy = 1; g.gridwidth = 3; g.weightx = 2.0;
        formCard.add(tfRoomDesc, g);

        JLabel lblLinkedRes = new JLabel("Connected Reservation ID:");
        lblLinkedRes.setFont(UIStyle.FONT_BOLD);
        lblLinkedRes.setForeground(UIStyle.COLOR_TEXT_MAIN);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 1; g.weightx = 0;
        formCard.add(lblLinkedRes, g);

        tfLinkedResId = UIStyle.createTextField(10);
        tfLinkedResId.setEditable(false);
        tfLinkedResId.setText("—");
        tfLinkedResId.setBackground(new Color(241, 245, 249));
        g.gridx = 1; g.gridy = 2; g.gridwidth = 1; g.weightx = 1.0;
        formCard.add(tfLinkedResId, g);

        btnViewLinkedRes = UIStyle.createSecondaryButton("View Linked Reservation");
        btnViewLinkedRes.setEnabled(false);
        btnViewLinkedRes.addActionListener(e -> showLinkedReservationDetails());
        g.gridx = 2; g.gridy = 2; g.gridwidth = 2; g.weightx = 0;
        formCard.add(btnViewLinkedRes, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 6; g.weightx = 1.0;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        btns.setOpaque(false);

        JButton btnClear = UIStyle.createSecondaryButton("Clear Fields");
        btnClear.addActionListener(e -> {
            roomTable.clearSelection();
            tfRoomNo.setText("");
            cbRoomType.setSelectedIndex(0);
            tfRoomPrice.setText("2500.0");
            cbRoomStatus.setSelectedIndex(0);
            tfRoomDesc.setText("");
            tfLinkedResId.setText("—");
            currentLinkedResId = null;
            btnViewLinkedRes.setEnabled(false);
        });

        JButton btnAdd = UIStyle.createPrimaryButton("Add Room");
        btnAdd.addActionListener(e -> handleAddRoom());

        JButton btnUpdate = UIStyle.createSecondaryButton("Update Room");
        btnUpdate.addActionListener(e -> handleUpdateRoom());

        JButton btnDel = UIStyle.createDangerButton("Delete Room");
        btnDel.addActionListener(e -> handleDeleteRoom());

        btns.add(btnClear);
        btns.add(btnAdd);
        btns.add(btnUpdate);
        btns.add(btnDel);
        formCard.add(btns, g);

        panel.add(formCard, BorderLayout.SOUTH);
        return panel;
    }

    private void handleAddRoom() {
        try {
            int roomNo = Integer.parseInt(tfRoomNo.getText().trim());
            String type = (String) cbRoomType.getSelectedItem();
            double price = Double.parseDouble(tfRoomPrice.getText().trim());
            String status = (String) cbRoomStatus.getSelectedItem();
            String desc = tfRoomDesc.getText().trim();

            Room r = roomService.addRoom(roomNo, type, price, status, desc);
            if (r != null) {
                JOptionPane.showMessageDialog(this, "Room " + roomNo + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshRooms();
            } else {
                JOptionPane.showMessageDialog(this, "Room " + roomNo + " already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter valid room details (check Room No and Price).", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleUpdateRoom() {
        try {
            int roomNo = Integer.parseInt(tfRoomNo.getText().trim());
            String type = (String) cbRoomType.getSelectedItem();
            double price = Double.parseDouble(tfRoomPrice.getText().trim());
            String status = (String) cbRoomStatus.getSelectedItem();
            String desc = tfRoomDesc.getText().trim();

            if ("AVAILABLE".equalsIgnoreCase(status)) {
                Reservation activeRes = resService.getActiveReservationForRoom(roomNo);
                while (activeRes != null) {
                    activeRes.setStatus("Completed");
                    resService.getReservationRepository().update(activeRes);
                    activeRes = resService.getActiveReservationForRoom(roomNo);
                }
            }

            Room r = roomService.updateRoomDetails(roomNo, type, price, status, desc);
            if (r != null) {
                JOptionPane.showMessageDialog(this, "Room " + roomNo + " updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshRooms();
            } else {
                JOptionPane.showMessageDialog(this, "Room " + roomNo + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter valid room details.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showLinkedReservationDetails() {
        if (currentLinkedResId == null) {
            JOptionPane.showMessageDialog(this, "No reservation is connected to this room.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Reservation r = resService.getReservationById(currentLinkedResId);
        if (r == null) {
            JOptionPane.showMessageDialog(this, "Reservation #" + currentLinkedResId + " not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String custName = r.getCustomer() != null ? r.getCustomer().getName() + " (ID #" + r.getCustomer().getCustomerId() + ")" : "N/A";
        String custPhone = r.getCustomer() != null ? r.getCustomer().getPhone() : "N/A";
        String custEmail = r.getCustomer() != null ? r.getCustomer().getEmail() : "N/A";
        int roomNo = r.getRoom() != null ? r.getRoom().getRoomNo() : 0;
        String roomType = r.getRoom() != null ? r.getRoom().getRoomType() : "N/A";
        String payStatus = r.getPayment() != null ? r.getPayment().getPaymentStatus() : "Unpaid";

        String details = String.format(
                "Connected Booking Details\n\n" +
                "• Reservation ID : #%d\n" +
                "• Room Allocated : Room %d [%s]\n" +
                "• Customer       : %s\n" +
                "• Phone          : %s\n" +
                "• Email          : %s\n" +
                "• Check-In Date  : %s\n" +
                "• Check-Out Date : %s\n" +
                "• Booking Status : %s\n" +
                "• Payment Status : %s\n" +
                "• Total Amount   : ₹%.2f\n",
                r.getReservationId(),
                roomNo,
                roomType,
                custName,
                custPhone,
                custEmail,
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getStatus(),
                payStatus,
                r.getTotalAmount()
        );

        JOptionPane.showMessageDialog(this, details, "Connected Reservation #" + r.getReservationId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDeleteRoom() {
        try {
            int roomNo = Integer.parseInt(tfRoomNo.getText().trim());
            int conf = JOptionPane.showConfirmDialog(this, "Delete Room " + roomNo + " permanently?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                roomService.deleteRoom(roomNo);
                refreshRooms();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Select a room or enter Room No to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel buildStaffTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Personnel Administration — Staff & Admins");
        title.setFont(UIStyle.FONT_TITLE);
        top.add(title, BorderLayout.WEST);

        JButton refBtn = UIStyle.createSecondaryButton("Refresh");
        refBtn.addActionListener(e -> refreshStaff());
        top.add(refBtn, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = {"Staff ID", "Name", "Role", "Email", "Phone"};
        staffTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        staffTable = new JTable(staffTableModel);
        UIStyle.styleTable(staffTable);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        JPanel formCard = UIStyle.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0;
        formCard.add(new JLabel("Staff ID:"), g);
        g.gridx = 1;
        tfStaffId = UIStyle.createTextField(6);
        formCard.add(tfStaffId, g);

        g.gridx = 2;
        formCard.add(new JLabel("Name:"), g);
        g.gridx = 3;
        tfStaffName = UIStyle.createTextField(14);
        formCard.add(tfStaffName, g);

        g.gridx = 4;
        formCard.add(new JLabel("Role:"), g);
        g.gridx = 5;
        cbStaffRole = new JComboBox<>(new String[]{"Manager", "Receptionist", "Housekeeping", "Concierge", "Admin"});
        cbStaffRole.setFont(UIStyle.FONT_REGULAR);
        formCard.add(cbStaffRole, g);

        g.gridx = 0; g.gridy = 1;
        formCard.add(new JLabel("Email:"), g);
        g.gridx = 1;
        tfStaffEmail = UIStyle.createTextField(12);
        formCard.add(tfStaffEmail, g);

        g.gridx = 2;
        formCard.add(new JLabel("Phone:"), g);
        g.gridx = 3;
        tfStaffPhone = UIStyle.createTextField(12);
        formCard.add(tfStaffPhone, g);

        g.gridx = 4;
        formCard.add(new JLabel("Password:"), g);
        g.gridx = 5;
        tfStaffPass = UIStyle.createTextField(10);
        tfStaffPass.setText("staff123");
        formCard.add(tfStaffPass, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 6;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton btnAddStaff = UIStyle.createPrimaryButton("Add Staff Member");
        btnAddStaff.addActionListener(e -> handleAddStaff());

        JButton btnDelStaff = UIStyle.createDangerButton("Delete Selected Staff");
        btnDelStaff.addActionListener(e -> handleDeleteStaff());

        btns.add(btnAddStaff);
        btns.add(btnDelStaff);
        formCard.add(btns, g);

        panel.add(formCard, BorderLayout.SOUTH);
        return panel;
    }

    private void handleAddStaff() {
        try {
            int id = Integer.parseInt(tfStaffId.getText().trim());
            String name = tfStaffName.getText().trim();
            String role = (String) cbStaffRole.getSelectedItem();
            String email = tfStaffEmail.getText().trim();
            String phone = tfStaffPhone.getText().trim();
            String pass = tfStaffPass.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Email are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            HotelStaff s;
            if ("Admin".equalsIgnoreCase(role)) {
                s = new Admin(id, name, email, phone, pass);
            } else {
                s = new HotelStaff(id, name, role, email, phone, pass);
            }
            HotelStaff added = staffService.addStaff(s);
            if (added != null) {
                JOptionPane.showMessageDialog(this, "Staff " + name + " (" + role + ") registered!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshStaff();
            } else {
                JOptionPane.showMessageDialog(this, "Staff ID " + id + " already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter valid staff details (numeric ID).", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDeleteStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a staff member from table.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int staffId = (Integer) staffTableModel.getValueAt(row, 0);
        int conf = JOptionPane.showConfirmDialog(this, "Delete Staff ID #" + staffId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            staffService.deleteStaff(staffId);
            refreshStaff();
        }
    }

    private JPanel buildRevenueTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel kpiContainer = new JPanel(new GridLayout(1, 3, 14, 0));
        kpiContainer.setOpaque(false);

        kpiContainer.add(createKpiCard("TOTAL BOOKINGS", lblTotalBookings = new JLabel("0"), UIStyle.COLOR_ACCENT));
        kpiContainer.add(createKpiCard("AVAILABLE ROOMS", lblAvailRooms = new JLabel("0"), UIStyle.COLOR_SUCCESS));
        kpiContainer.add(createKpiCard("TOTAL REVENUE", lblTotalRevenue = new JLabel("$0.00"), UIStyle.COLOR_CYAN));

        panel.add(kpiContainer, BorderLayout.NORTH);

        String[] cols = {"Payment ID", "Reservation ID", "Method", "Amount", "Status", "Date", "Details"};
        paymentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        paymentTable = new JTable(paymentTableModel);
        UIStyle.styleTable(paymentTable);
        paymentTable.getColumnModel().getColumn(4).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(paymentTable), BorderLayout.CENTER);

        JPanel bottomBar = UIStyle.createCardPanel();
        bottomBar.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 6));

        JLabel lblChangeStatus = new JLabel("Change Selected Payment Status:");
        lblChangeStatus.setFont(UIStyle.FONT_BOLD);
        lblChangeStatus.setForeground(UIStyle.COLOR_TEXT_MAIN);
        bottomBar.add(lblChangeStatus);

        JComboBox<String> cbPaymentStatus = new JComboBox<>(new String[]{"COMPLETED", "PAID", "PENDING", "REFUNDED", "FAILED"});
        cbPaymentStatus.setFont(UIStyle.FONT_REGULAR);
        cbPaymentStatus.setPreferredSize(new Dimension(160, 36));
        bottomBar.add(cbPaymentStatus);

        JButton btnUpdateStatus = UIStyle.createPrimaryButton("Update Payment Status");
        btnUpdateStatus.addActionListener(e -> {
            int row = paymentTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a payment row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int paymentId = (Integer) paymentTableModel.getValueAt(row, 0);
            String selectedStatus = (String) cbPaymentStatus.getSelectedItem();

            boolean success = payService.updatePaymentStatus(paymentId, selectedStatus);
            if (success) {
                refreshRevenue();
                JOptionPane.showMessageDialog(this, "Payment #" + paymentId + " status updated to " + selectedStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update payment status for Payment #" + paymentId, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomBar.add(btnUpdateStatus);

        JButton refBtn = UIStyle.createSecondaryButton("Refresh Ledger");
        refBtn.addActionListener(e -> refreshRevenue());
        bottomBar.add(refBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createKpiCard(String title, JLabel valueLbl, Color accentColor) {
        JPanel card = UIStyle.createCardPanel();
        card.setLayout(new BorderLayout(6, 6));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(UIStyle.FONT_SMALL);
        tLbl.setForeground(UIStyle.COLOR_TEXT_MUTED);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accentColor);

        card.add(tLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    public void refreshAll() {
        refreshRooms();
        refreshStaff();
        refreshRevenue();
    }

    public void refreshRooms() {
        roomTableModel.setRowCount(0);
        for (Room r : roomService.listAllRooms()) {
            Reservation activeRes = resService.getActiveReservationForRoom(r.getRoomNo());
            String resIdStr = "—";
            String status = r.getStatus();
            if (activeRes != null) {
                resIdStr = "#" + activeRes.getReservationId();
                if ("AVAILABLE".equalsIgnoreCase(status)) {
                    status = "BOOKED";
                }
            }
            roomTableModel.addRow(new Object[]{
                    r.getRoomNo(),
                    r.getRoomType(),
                    String.format("₹%.2f", r.getPrice()),
                    status,
                    resIdStr,
                    r.getDescription()
            });
        }
    }

    public void refreshStaff() {
        staffTableModel.setRowCount(0);
        for (HotelStaff s : staffService.listAllStaff()) {
            staffTableModel.addRow(new Object[]{
                    s.getStaffId(),
                    s.getName(),
                    s.getRole(),
                    s.getEmail(),
                    s.getPhone()
            });
        }
    }

    public void refreshRevenue() {
        paymentTableModel.setRowCount(0);
        List<Payment> payments = payService.getAllPayments();
        double totalRev = 0.0;
        int totalBookings = resService.getAllReservations().size();
        int availRooms = roomService.listAvailableRooms().size();

        for (Payment p : payments) {
            int resId = p.getReservation() != null ? p.getReservation().getReservationId() : 0;
            String status = p.getPaymentStatus();
            if (status != null && (status.equalsIgnoreCase("PAID") || status.equalsIgnoreCase("COMPLETED"))) {
                totalRev += p.getAmount();
            }
            paymentTableModel.addRow(new Object[]{
                    p.getPaymentId(),
                    resId,
                    p.getPaymentMethod(),
                    String.format("₹%.2f", p.getAmount()),
                    status,
                    p.getPaymentDate() != null ? p.getPaymentDate() : "N/A",
                    p instanceof com.hotel.domain.CardPayment ? "Card (" + ((com.hotel.domain.CardPayment)p).getCardNumber() + ")" :
                    p instanceof com.hotel.domain.UpiPayment ? "UPI (" + ((com.hotel.domain.UpiPayment)p).getUpiId() + ")" : "Cash Desk"
            });
        }

        lblTotalBookings.setText(String.valueOf(totalBookings));
        lblAvailRooms.setText(String.valueOf(availRooms));
        lblTotalRevenue.setText(String.format("₹%.2f", totalRev));
    }
}

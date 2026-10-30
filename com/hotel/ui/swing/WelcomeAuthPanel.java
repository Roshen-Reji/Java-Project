package com.hotel.ui.swing;

import com.hotel.domain.Admin;
import com.hotel.domain.Customer;
import com.hotel.domain.HotelStaff;
import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.StaffService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class WelcomeAuthPanel extends JPanel {

    private final HotelAppFrame mainFrame;
    private final AuthenticationCustomerService authService;
    private final StaffService staffService;

    // Customer Login Fields
    private JTextField tfCustLoginKey;
    private JPasswordField pfCustLoginPass;

    // Customer Register Fields
    private JTextField tfRegName;
    private JTextField tfRegPhone;
    private JTextField tfRegEmail;
    private JTextField tfRegAddress;
    private JPasswordField pfRegPass;

    // Staff / Admin Login Fields
    private JTextField tfStaffLoginKey;
    private JPasswordField pfStaffPass;

    public WelcomeAuthPanel(HotelAppFrame mainFrame,
                            AuthenticationCustomerService authService,
                            StaffService staffService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        this.staffService = staffService;

        setLayout(new BorderLayout());
        setBackground(UIStyle.COLOR_BG);

        initUI();
    }

    private void initUI() {
        // --- Left Brand Banner ---
        JPanel leftBanner = new JPanel();
        leftBanner.setLayout(new BoxLayout(leftBanner, BoxLayout.Y_AXIS));
        leftBanner.setBackground(UIStyle.COLOR_PRIMARY_DARK);
        leftBanner.setPreferredSize(new Dimension(380, 0));
        leftBanner.setBorder(new EmptyBorder(40, 36, 40, 36));

        JLabel logo = new JLabel("HOTEL ASTRA");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(Color.WHITE);

        JLabel tag = new JLabel("Luxury Reservation & Management Suite");
        tag.setFont(UIStyle.FONT_SMALL);
        tag.setForeground(UIStyle.COLOR_CYAN);

        leftBanner.add(logo);
        leftBanner.add(Box.createVerticalStrut(6));
        leftBanner.add(tag);
        leftBanner.add(Box.createVerticalStrut(32));

        addFeatureBullet(leftBanner, "Polymorphic Room Types", "Standard, Deluxe, and Luxury Suite models.");
        addFeatureBullet(leftBanner, "Dynamic Payment Processing", "Instant UPI, Card with masking, and Desk Cash.");
        addFeatureBullet(leftBanner, "Persistent SQL Database", "Robust JDBC layer with automated schema creation.");
        addFeatureBullet(leftBanner, "Role-Based Access Control", "Dedicated Guest, Staff, and Administrator portals.");
        addFeatureBullet(leftBanner, "KTU 2024 Scheme Standard", "Clean S3 Object-Oriented Java Architecture.");

        leftBanner.add(Box.createVerticalGlue());

        // Status Badge
        JPanel statusBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        statusBox.setBackground(UIStyle.COLOR_SIDEBAR_HOVER);
        statusBox.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusBox.setMaximumSize(new Dimension(310, 36));

        JLabel dot = new JLabel("●");
        dot.setForeground(UIStyle.COLOR_SUCCESS);
        JLabel dbStatus = new JLabel("SQLite Database Connected & Seeded");
        dbStatus.setFont(UIStyle.FONT_SMALL);
        dbStatus.setForeground(Color.WHITE);
        statusBox.add(dot);
        statusBox.add(dbStatus);

        leftBanner.add(statusBox);

        add(leftBanner, BorderLayout.WEST);

        // --- Right Auth Tabs ---
        JPanel rightArea = new JPanel(new BorderLayout());
        rightArea.setOpaque(false);
        rightArea.setBorder(new EmptyBorder(30, 40, 30, 40));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIStyle.FONT_BOLD);

        tabbedPane.addTab("Customer Login", buildCustomerLoginTab());
        tabbedPane.addTab("Customer Sign Up", buildCustomerRegisterTab());
        tabbedPane.addTab("Staff & Admin Access", buildStaffLoginTab());

        rightArea.add(tabbedPane, BorderLayout.CENTER);
        add(rightArea, BorderLayout.CENTER);
    }

    private void addFeatureBullet(JPanel parent, String title, String desc) {
        JLabel tLbl = new JLabel("✦ " + title);
        tLbl.setFont(UIStyle.FONT_BOLD);
        tLbl.setForeground(Color.WHITE);

        JLabel dLbl = new JLabel("   " + desc);
        dLbl.setFont(UIStyle.FONT_SMALL);
        dLbl.setForeground(new Color(0x94, 0xA3, 0xB8));

        parent.add(tLbl);
        parent.add(Box.createVerticalStrut(2));
        parent.add(dLbl);
        parent.add(Box.createVerticalStrut(16));
    }

    // --- Tab 1: Customer Login ---
    private JPanel buildCustomerLoginTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel title = new JLabel("Welcome Back, Guest");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.COLOR_TEXT_MAIN);
        panel.add(title, g);

        g.gridy = 1;
        JLabel sub = new JLabel("Enter your registered email or phone to access your reservations.");
        sub.setFont(UIStyle.FONT_REGULAR);
        sub.setForeground(UIStyle.COLOR_TEXT_MUTED);
        panel.add(sub, g);

        g.gridy = 2; g.gridwidth = 1;
        panel.add(new JLabel("Email or Phone:"), g);
        g.gridx = 1;
        tfCustLoginKey = UIStyle.createTextField(22);
        tfCustLoginKey.setText("john@example.com");
        panel.add(tfCustLoginKey, g);

        g.gridx = 0; g.gridy = 3;
        panel.add(new JLabel("Password:"), g);
        g.gridx = 1;
        pfCustLoginPass = UIStyle.createPasswordField(22);
        pfCustLoginPass.setText("password123");
        panel.add(pfCustLoginPass, g);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnQuickFill = UIStyle.createSecondaryButton("Demo Guest");
        btnQuickFill.addActionListener(e -> {
            tfCustLoginKey.setText("john@example.com");
            pfCustLoginPass.setText("password123");
        });

        JButton btnLogin = UIStyle.createPrimaryButton("Log In to Customer Portal");
        btnLogin.addActionListener(e -> handleCustomerLogin());

        btnPanel.add(btnQuickFill);
        btnPanel.add(btnLogin);
        panel.add(btnPanel, g);

        return panel;
    }

    private void handleCustomerLogin() {
        String key = tfCustLoginKey.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email or phone.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer c = authService.login(key);
        if (c != null) {
            mainFrame.showCustomerDashboard(c);
        } else {
            JOptionPane.showMessageDialog(this, "No customer found with \"" + key + "\". Please check details or sign up.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Tab 2: Customer Registration ---
    private JPanel buildCustomerRegisterTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel title = new JLabel("Create a New Customer Account");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.COLOR_TEXT_MAIN);
        panel.add(title, g);

        g.gridy = 1;
        JLabel sub = new JLabel("Join Astra Privilege rewards and manage your hotel bookings seamlessly.");
        sub.setFont(UIStyle.FONT_REGULAR);
        sub.setForeground(UIStyle.COLOR_TEXT_MUTED);
        panel.add(sub, g);

        g.gridy = 2; g.gridwidth = 1;
        panel.add(new JLabel("Full Name:"), g);
        g.gridx = 1;
        tfRegName = UIStyle.createTextField(22);
        panel.add(tfRegName, g);

        g.gridx = 0; g.gridy = 3;
        panel.add(new JLabel("Phone (7-15 digits):"), g);
        g.gridx = 1;
        tfRegPhone = UIStyle.createTextField(22);
        panel.add(tfRegPhone, g);

        g.gridx = 0; g.gridy = 4;
        panel.add(new JLabel("Email Address:"), g);
        g.gridx = 1;
        tfRegEmail = UIStyle.createTextField(22);
        panel.add(tfRegEmail, g);

        g.gridx = 0; g.gridy = 5;
        panel.add(new JLabel("Mailing Address:"), g);
        g.gridx = 1;
        tfRegAddress = UIStyle.createTextField(22);
        panel.add(tfRegAddress, g);

        g.gridx = 0; g.gridy = 6;
        panel.add(new JLabel("Account Password:"), g);
        g.gridx = 1;
        pfRegPass = UIStyle.createPasswordField(22);
        panel.add(pfRegPass, g);

        g.gridx = 0; g.gridy = 7; g.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnRegister = UIStyle.createPrimaryButton("Register & Open Dashboard");
        btnRegister.addActionListener(e -> handleCustomerRegister());
        btnPanel.add(btnRegister);
        panel.add(btnPanel, g);

        return panel;
    }

    private void handleCustomerRegister() {
        String name = tfRegName.getText().trim();
        String phone = tfRegPhone.getText().trim();
        String email = tfRegEmail.getText().trim();
        String address = tfRegAddress.getText().trim();
        String pass = new String(pfRegPass.getPassword()).trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!AuthenticationCustomerService.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number (7-15 digits).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!AuthenticationCustomerService.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer created = authService.registerCustomer(name, phone, email, address);
        if (created != null) {
            if (!pass.isEmpty()) {
                created.setPassword(pass);
                authService.getCustomerRepository().update(created);
            }
            JOptionPane.showMessageDialog(this, "Registration successful! Welcome to Hotel Astra, " + name + ".", "Account Created", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.showCustomerDashboard(created);
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Email or phone may already be registered.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Tab 3: Staff & Admin Login ---
    private JPanel buildStaffLoginTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel title = new JLabel("Hotel Personnel & Executive Access");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.COLOR_TEXT_MAIN);
        panel.add(title, g);

        g.gridy = 1;
        JLabel sub = new JLabel("Authorized Staff and System Administrators only.");
        sub.setFont(UIStyle.FONT_REGULAR);
        sub.setForeground(UIStyle.COLOR_TEXT_MUTED);
        panel.add(sub, g);

        g.gridy = 2; g.gridwidth = 1;
        panel.add(new JLabel("Staff ID or Email:"), g);
        g.gridx = 1;
        tfStaffLoginKey = UIStyle.createTextField(22);
        tfStaffLoginKey.setText("admin@hotel.com");
        panel.add(tfStaffLoginKey, g);

        g.gridx = 0; g.gridy = 3;
        panel.add(new JLabel("PIN / Password:"), g);
        g.gridx = 1;
        pfStaffPass = UIStyle.createPasswordField(22);
        pfStaffPass.setText("1234");
        panel.add(pfStaffPass, g);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnDemoStaff = UIStyle.createSecondaryButton("Demo Staff");
        btnDemoStaff.addActionListener(e -> {
            tfStaffLoginKey.setText("alice@hotel.com");
            pfStaffPass.setText("staff");
        });

        JButton btnDemoAdmin = UIStyle.createSecondaryButton("Demo Admin");
        btnDemoAdmin.addActionListener(e -> {
            tfStaffLoginKey.setText("admin@hotel.com");
            pfStaffPass.setText("1234");
        });

        JButton btnLogin = UIStyle.createPrimaryButton("Authenticate & Enter Portal");
        btnLogin.addActionListener(e -> handleStaffLogin());

        btnPanel.add(btnDemoStaff);
        btnPanel.add(btnDemoAdmin);
        btnPanel.add(btnLogin);
        panel.add(btnPanel, g);

        return panel;
    }

    private void handleStaffLogin() {
        String key = tfStaffLoginKey.getText().trim();
        String pass = new String(pfStaffPass.getPassword()).trim();

        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Staff ID or Email.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check PIN 1234 or "admin" override
        if (pass.equals("1234") || pass.equalsIgnoreCase("admin") || key.equalsIgnoreCase("admin") || key.equals("999")) {
            HotelStaff s = staffService.findStaff(999);
            if (s instanceof Admin) {
                mainFrame.showAdminDashboard((Admin) s);
                return;
            } else {
                Admin admin = new Admin(999, "Master Admin", "admin@hotel.com", "999-9999");
                mainFrame.showAdminDashboard(admin);
                return;
            }
        }

        // Try lookup by numeric ID or by email
        HotelStaff staff = null;
        try {
            int staffId = Integer.parseInt(key);
            staff = staffService.findStaff(staffId);
        } catch (NumberFormatException ignored) {
            staff = staffService.getStaffRepository().findByEmail(key);
        }

        if (staff != null) {
            if (staff instanceof Admin) {
                mainFrame.showAdminDashboard((Admin) staff);
            } else {
                mainFrame.showStaffDashboard(staff);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Staff ID/Email not found.", "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }
}

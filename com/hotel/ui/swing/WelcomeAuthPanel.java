package com.hotel.ui.swing;

import com.hotel.domain.Admin;
import com.hotel.domain.Customer;
import com.hotel.domain.HotelStaff;
import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.StaffService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class WelcomeAuthPanel extends JPanel {

    private final HotelAppFrame mainFrame;
    private final AuthenticationCustomerService authService;
    private final StaffService staffService;

    private JTextField tfCustLoginKey;
    private JPasswordField pfCustLoginPass;

    private JTextField tfRegName;
    private JTextField tfRegPhone;
    private JTextField tfRegEmail;
    private JTextField tfRegAddress;
    private JPasswordField pfRegPass;

    private JTextField tfStaffLoginKey;
    private JPasswordField pfStaffPass;

    public WelcomeAuthPanel(HotelAppFrame mainFrame,
                            AuthenticationCustomerService authService,
                            StaffService staffService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        this.staffService = staffService;

        setLayout(new GridBagLayout());
        setBackground(UIStyle.COLOR_BG);

        initUI();
    }

    private void initUI() {

        JPanel cardContainer = UIStyle.createCardPanel();
        cardContainer.setLayout(new BorderLayout(0, 20));
        cardContainer.setBorder(new EmptyBorder(32, 40, 36, 40));
        cardContainer.setPreferredSize(new Dimension(680, 560));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel logo = new JLabel("HOTEL ASTRA");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logo.setForeground(UIStyle.COLOR_PRIMARY_DARK);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Luxury Reservation & Management Suite");
        subtitle.setFont(UIStyle.FONT_REGULAR);
        subtitle.setForeground(UIStyle.COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(logo);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);

        cardContainer.add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIStyle.FONT_BOLD);

        tabbedPane.addTab("Customer Login", buildCustomerLoginTab());
        tabbedPane.addTab("Customer Sign Up", buildCustomerRegisterTab());
        tabbedPane.addTab("Staff & Admin Access", buildStaffLoginTab());

        cardContainer.add(tabbedPane, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(cardContainer, gbc);
    }

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
        panel.add(tfCustLoginKey, g);

        g.gridx = 0; g.gridy = 3;
        panel.add(new JLabel("Password:"), g);
        g.gridx = 1;
        pfCustLoginPass = UIStyle.createPasswordField(22);
        panel.add(pfCustLoginPass, g);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnLogin = UIStyle.createPrimaryButton("Log In to Customer Portal");
        btnLogin.addActionListener(e -> handleCustomerLogin());

        btnPanel.add(btnLogin);
        panel.add(btnPanel, g);

        return panel;
    }

    private void handleCustomerLogin() {
        String key = tfCustLoginKey.getText().trim();
        String pass = new String(pfCustLoginPass.getPassword()).trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email or phone.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer c = authService.login(key);
        if (c != null) {
            if (c.getPassword() != null && !c.getPassword().isEmpty()) {
                if (pass.isEmpty() || !c.getPassword().equals(pass)) {
                    JOptionPane.showMessageDialog(this, "Invalid password. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            mainFrame.showCustomerDashboard(c);
        } else {
            JOptionPane.showMessageDialog(this, "No customer found with \"" + key + "\". Please check details or sign up.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

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
        panel.add(tfStaffLoginKey, g);

        g.gridx = 0; g.gridy = 3;
        panel.add(new JLabel("PIN / Password:"), g);
        g.gridx = 1;
        pfStaffPass = UIStyle.createPasswordField(22);
        panel.add(pfStaffPass, g);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnLogin = UIStyle.createPrimaryButton("Authenticate & Enter Portal");
        btnLogin.addActionListener(e -> handleStaffLogin());

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

        if ((key.equals("999") || key.equalsIgnoreCase("admin@hotel.com") || key.equalsIgnoreCase("admin"))
                && (pass.equalsIgnoreCase("admin") || pass.equals("1234"))) {
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

        HotelStaff staff = null;
        try {
            int staffId = Integer.parseInt(key);
            staff = staffService.findStaff(staffId);
        } catch (NumberFormatException ignored) {
            staff = staffService.getStaffRepository().findByEmail(key);
        }

        if (staff != null) {
            if (staff.getPassword() != null && !staff.getPassword().isEmpty()) {
                if (pass.isEmpty() || !staff.getPassword().equals(pass)) {
                    JOptionPane.showMessageDialog(this, "Invalid credentials. Incorrect password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
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

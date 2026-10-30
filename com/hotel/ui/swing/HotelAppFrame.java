package com.hotel.ui.swing;

import com.hotel.domain.Admin;
import com.hotel.domain.Customer;
import com.hotel.domain.HotelStaff;
import com.hotel.service.AuthenticationCustomerService;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import com.hotel.service.RoomManagementService;
import com.hotel.service.StaffService;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class HotelAppFrame extends JFrame {

    public static final String VIEW_AUTH     = "AUTH";
    public static final String VIEW_CUSTOMER = "CUSTOMER";
    public static final String VIEW_STAFF    = "STAFF";
    public static final String VIEW_ADMIN    = "ADMIN";

    private final CardLayout cardLayout;
    private final JPanel rootPanel;

    private final WelcomeAuthPanel authPanel;
    private final CustomerDashboardPanel customerPanel;
    private final StaffDashboardPanel staffPanel;
    private final AdminDashboardPanel adminPanel;

    public HotelAppFrame(AuthenticationCustomerService authService,
                         RoomManagementService roomService,
                         ReservationService resService,
                         PaymentService payService,
                         StaffService staffService) {
        super("Astra Hotel Reservation & Management System");

        UIStyle.setupLookAndFeel();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        authPanel = new WelcomeAuthPanel(this, authService, staffService);
        customerPanel = new CustomerDashboardPanel(this, authService, roomService, resService, payService);
        staffPanel = new StaffDashboardPanel(this, authService, roomService, resService);
        adminPanel = new AdminDashboardPanel(this, roomService, staffService, resService, payService);

        rootPanel.add(authPanel, VIEW_AUTH);
        rootPanel.add(customerPanel, VIEW_CUSTOMER);
        rootPanel.add(staffPanel, VIEW_STAFF);
        rootPanel.add(adminPanel, VIEW_ADMIN);

        add(rootPanel);

        showAuth();
    }

    public void showAuth() {
        cardLayout.show(rootPanel, VIEW_AUTH);
    }

    public void showCustomerDashboard(Customer customer) {
        customerPanel.setCustomer(customer);
        cardLayout.show(rootPanel, VIEW_CUSTOMER);
    }

    public void showStaffDashboard(HotelStaff staff) {
        staffPanel.setStaff(staff);
        cardLayout.show(rootPanel, VIEW_STAFF);
    }

    public void showAdminDashboard(Admin admin) {
        adminPanel.setAdmin(admin);
        cardLayout.show(rootPanel, VIEW_ADMIN);
    }
}

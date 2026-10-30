package com.hotel.ui.swing;

import com.hotel.domain.Payment;
import com.hotel.domain.Reservation;
import com.hotel.service.PaymentService;
import com.hotel.service.ReservationService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class PaymentDialog extends JDialog {

    private final Reservation reservation;
    private final Runnable onPaymentSuccess;

    private JRadioButton rbUpi;
    private JRadioButton rbCard;
    private JRadioButton rbCash;

    private JTextField tfUpiId;
    private JTextField tfCardNum;
    private JTextField tfCardHolder;
    private JTextField tfCardExp;
    private JTextField tfCardCvv;

    private JPanel methodCards;
    private CardLayout methodCardLayout;

    public PaymentDialog(Window owner, Reservation reservation, Runnable onPaymentSuccess) {
        super(owner, "Astra Hotel — Process Payment & Bill", ModalityType.APPLICATION_MODAL);
        this.reservation = reservation;
        this.onPaymentSuccess = onPaymentSuccess;

        setSize(650, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.COLOR_BG);

        initUI();
    }

    private void initUI() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.COLOR_PRIMARY_DARK);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Secure Payment & Bill Checkout");
        titleLbl.setFont(UIStyle.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel("Reservation #" + reservation.getReservationId() + " — Hotel Astra");
        subLbl.setFont(UIStyle.FONT_SMALL);
        subLbl.setForeground(new Color(0x94, 0xA3, 0xB8));

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(subLbl, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 14, 0);

        JPanel summaryCard = UIStyle.createCardPanel();
        summaryCard.setLayout(new GridBagLayout());
        GridBagConstraints sGbc = new GridBagConstraints();
        sGbc.anchor = GridBagConstraints.WEST;
        sGbc.insets = new Insets(4, 6, 4, 6);

        long nights = 1;
        if (reservation.getCheckInDate() != null && reservation.getCheckOutDate() != null) {
            nights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
            if (nights <= 0) nights = 1;
        }

        String guestName = reservation.getCustomer() != null ? reservation.getCustomer().getName() : "Guest";
        String roomStr = reservation.getRoom() != null ? "Room " + reservation.getRoom().getRoomNo() + " (" + reservation.getRoom().getRoomType() + ")" : "Room";

        addSummaryRow(summaryCard, sGbc, 0, "Guest:", guestName, "Room:", roomStr);
        addSummaryRow(summaryCard, sGbc, 1, "Check-In:", String.valueOf(reservation.getCheckInDate()), "Check-Out:", String.valueOf(reservation.getCheckOutDate()));
        addSummaryRow(summaryCard, sGbc, 2, "Nights:", nights + " night(s)", "Total Amount:", "₹" + String.format("%.2f", reservation.getTotalAmount()));

        gbc.gridy = 0;
        body.add(summaryCard, gbc);

        JPanel methodSelectorCard = UIStyle.createCardPanel();
        methodSelectorCard.setLayout(new BorderLayout(8, 8));

        JLabel selectLbl = new JLabel("Choose Payment Method:");
        selectLbl.setFont(UIStyle.FONT_BOLD);
        selectLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);
        methodSelectorCard.add(selectLbl, BorderLayout.NORTH);

        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 4));
        radios.setOpaque(false);

        rbUpi = new JRadioButton("UPI (Instant)", true);
        rbCard = new JRadioButton("Credit / Debit Card");
        rbCash = new JRadioButton("Pay Cash at Front Desk");

        rbUpi.setFont(UIStyle.FONT_BOLD);
        rbCard.setFont(UIStyle.FONT_BOLD);
        rbCash.setFont(UIStyle.FONT_BOLD);
        rbUpi.setOpaque(false);
        rbCard.setOpaque(false);
        rbCash.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(rbUpi);
        group.add(rbCard);
        group.add(rbCash);

        radios.add(rbUpi);
        radios.add(rbCard);
        radios.add(rbCash);
        methodSelectorCard.add(radios, BorderLayout.CENTER);

        gbc.gridy = 1;
        body.add(methodSelectorCard, gbc);

        methodCardLayout = new CardLayout();
        methodCards = UIStyle.createCardPanel();
        methodCards.setLayout(methodCardLayout);

        JPanel upiPanel = new JPanel(new GridBagLayout());
        upiPanel.setOpaque(false);
        GridBagConstraints uGbc = new GridBagConstraints();
        uGbc.anchor = GridBagConstraints.WEST;
        uGbc.insets = new Insets(6, 6, 6, 6);
        uGbc.gridx = 0; uGbc.gridy = 0;
        upiPanel.add(new JLabel("Enter UPI ID (VPA):"), uGbc);
        uGbc.gridx = 1; uGbc.fill = GridBagConstraints.HORIZONTAL; uGbc.weightx = 1.0;
        tfUpiId = UIStyle.createTextField(20);
        tfUpiId.setText("guest@okhdfcbank");
        upiPanel.add(tfUpiId, uGbc);
        uGbc.gridx = 1; uGbc.gridy = 1; uGbc.fill = GridBagConstraints.NONE;
        JLabel upiHelp = new JLabel("Supported: GPay, PhonePe, Paytm, BHIM, AmazonPay");
        upiHelp.setFont(UIStyle.FONT_SMALL);
        upiHelp.setForeground(UIStyle.COLOR_TEXT_MUTED);
        upiPanel.add(upiHelp, uGbc);

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setOpaque(false);
        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.anchor = GridBagConstraints.WEST;
        cGbc.insets = new Insets(4, 6, 4, 6);

        cGbc.gridx = 0; cGbc.gridy = 0;
        cardPanel.add(new JLabel("Card Number:"), cGbc);
        cGbc.gridx = 1; cGbc.gridwidth = 3; cGbc.fill = GridBagConstraints.HORIZONTAL;
        tfCardNum = UIStyle.createTextField(20);
        tfCardNum.setText("4532 8901 2345 6789");
        cardPanel.add(tfCardNum, cGbc);

        cGbc.gridx = 0; cGbc.gridy = 1; cGbc.gridwidth = 1; cGbc.fill = GridBagConstraints.NONE;
        cardPanel.add(new JLabel("Holder Name:"), cGbc);
        cGbc.gridx = 1; cGbc.gridwidth = 3; cGbc.fill = GridBagConstraints.HORIZONTAL;
        tfCardHolder = UIStyle.createTextField(20);
        tfCardHolder.setText(guestName);
        cardPanel.add(tfCardHolder, cGbc);

        cGbc.gridx = 0; cGbc.gridy = 2; cGbc.gridwidth = 1; cGbc.fill = GridBagConstraints.NONE;
        cardPanel.add(new JLabel("Expiry (MM/YY):"), cGbc);
        cGbc.gridx = 1; cGbc.fill = GridBagConstraints.HORIZONTAL;
        tfCardExp = UIStyle.createTextField(6);
        tfCardExp.setText("08/28");
        cardPanel.add(tfCardExp, cGbc);

        cGbc.gridx = 2; cGbc.fill = GridBagConstraints.NONE;
        cardPanel.add(new JLabel("CVV:"), cGbc);
        cGbc.gridx = 3; cGbc.fill = GridBagConstraints.HORIZONTAL;
        tfCardCvv = UIStyle.createTextField(4);
        tfCardCvv.setText("789");
        cardPanel.add(tfCardCvv, cGbc);

        JPanel cashPanel = new JPanel(new BorderLayout(8, 8));
        cashPanel.setOpaque(false);
        JLabel cashLbl = new JLabel("<html><b>Front Desk Cash Collection:</b><br/>"
                + "Your booking will be placed in confirmed status. Please tender exact cash "
                + "at the hotel concierge counter upon check-in.</html>");
        cashLbl.setFont(UIStyle.FONT_REGULAR);
        cashLbl.setForeground(UIStyle.COLOR_TEXT_MAIN);
        cashPanel.add(cashLbl, BorderLayout.CENTER);

        methodCards.add(upiPanel, "UPI");
        methodCards.add(cardPanel, "CARD");
        methodCards.add(cashPanel, "CASH");

        rbUpi.addActionListener(e -> methodCardLayout.show(methodCards, "UPI"));
        rbCard.addActionListener(e -> methodCardLayout.show(methodCards, "CARD"));
        rbCash.addActionListener(e -> methodCardLayout.show(methodCards, "CASH"));

        gbc.gridy = 2;
        body.add(methodCards, gbc);

        add(new JScrollPane(body), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyle.COLOR_BORDER));

        JButton cancelBtn = UIStyle.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton payBtn = UIStyle.createPrimaryButton("Pay & Confirm Reservation");
        payBtn.addActionListener(e -> handlePayment());

        footer.add(cancelBtn);
        footer.add(payBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void addSummaryRow(JPanel panel, GridBagConstraints gbc, int row, String lbl1, String val1, String lbl2, String val2) {
        gbc.gridy = row;

        gbc.gridx = 0; gbc.weightx = 0;
        JLabel l1 = new JLabel(lbl1);
        l1.setFont(UIStyle.FONT_BOLD);
        l1.setForeground(UIStyle.COLOR_TEXT_MUTED);
        panel.add(l1, gbc);

        gbc.gridx = 1; gbc.weightx = 0.5;
        JLabel v1 = new JLabel(val1);
        v1.setFont(UIStyle.FONT_REGULAR);
        panel.add(v1, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        JLabel l2 = new JLabel(lbl2);
        l2.setFont(UIStyle.FONT_BOLD);
        l2.setForeground(UIStyle.COLOR_TEXT_MUTED);
        panel.add(l2, gbc);

        gbc.gridx = 3; gbc.weightx = 0.5;
        JLabel v2 = new JLabel(val2);
        v2.setFont(UIStyle.FONT_BOLD);
        v2.setForeground(UIStyle.COLOR_ACCENT);
        panel.add(v2, gbc);
    }

    private void handlePayment() {
        String method = "UPI";
        String detail = "";

        if (rbUpi.isSelected()) {
            method = "UPI";
            detail = tfUpiId.getText().trim();
            if (detail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a valid UPI ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else if (rbCard.isSelected()) {
            method = "Card";
            String cardNum = tfCardNum.getText().trim();
            String holder = tfCardHolder.getText().trim();
            if (cardNum.isEmpty() || holder.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter card number and holder name.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            detail = cardNum + ";" + holder;
        } else {
            method = "Cash";
            detail = "Counter Cash Collection";
        }

        Payment payment = PaymentService.createPendingPayment(reservation, method, detail);
        if (payment == null) {
            JOptionPane.showMessageDialog(this, "Failed to create payment record.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean confirmed = ReservationService.confirmReservation(reservation.getReservationId());
        if (!confirmed) {

            payment.confirmPayment();
            PaymentService.getPaymentRepository().update(payment);
            reservation.confirmBooking();
            ReservationService.getReservationRepository().update(reservation);
            if (reservation.getRoom() != null) {
                LocalDate today = LocalDate.now();
                if (reservation.getCheckInDate() != null && !reservation.getCheckInDate().isAfter(today) && reservation.getCheckOutDate().isAfter(today)) {
                    com.hotel.service.RoomManagementService.updateStatus(reservation.getRoom().getRoomNo(), "Booked");
                } else {
                    com.hotel.service.RoomManagementService.updateStatus(reservation.getRoom().getRoomNo(), "Available");
                }
            }
        }

        showReceiptDialog(payment);
    }

    private void showReceiptDialog(Payment payment) {
        JDialog receiptDialog = new JDialog(this, "Hotel Astra — Itemized Bill Receipt", true);
        receiptDialog.setSize(560, 620);
        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.setLayout(new BorderLayout());

        String receiptText = buildReceiptString(payment);

        JTextArea ta = new JTextArea(receiptText);
        ta.setFont(UIStyle.FONT_MONO);
        ta.setEditable(false);
        ta.setBackground(new Color(0xFB, 0xFB, 0xFD));
        ta.setBorder(new EmptyBorder(16, 16, 16, 16));

        receiptDialog.add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(Color.WHITE);

        JButton saveBtn = UIStyle.createSecondaryButton("Save Receipt to File");
        saveBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Astra_Receipt_Reservation_" + reservation.getReservationId() + ".txt"));
            int res = chooser.showSaveDialog(receiptDialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                    fw.write(receiptText);
                    JOptionPane.showMessageDialog(receiptDialog, "Receipt saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(receiptDialog, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton closeBtn = UIStyle.createPrimaryButton("Done & View Bookings");
        closeBtn.addActionListener(e -> {
            receiptDialog.dispose();
            dispose();
            if (onPaymentSuccess != null) {
                onPaymentSuccess.run();
            }
        });

        btnPanel.add(saveBtn);
        btnPanel.add(closeBtn);
        receiptDialog.add(btnPanel, BorderLayout.SOUTH);

        receiptDialog.setVisible(true);
    }

    public static String buildReceiptString(Payment payment) {
        Reservation res = payment.getReservation();
        if (res != null && (res.getCustomer() == null || res.getRoom() == null)) {
            Reservation full = new com.hotel.repository.ReservationRepository().getById(res.getReservationId());
            if (full != null) {
                res = full;
                payment.setReservation(full);
            }
        }
        String custName = (res != null && res.getCustomer() != null) ? res.getCustomer().getName() : "Valued Customer";
        String custEmail = (res != null && res.getCustomer() != null) ? res.getCustomer().getEmail() : "N/A";
        String roomType = (res != null && res.getRoom() != null) ? res.getRoom().getRoomType() : "Standard";
        int roomNo = (res != null && res.getRoom() != null) ? res.getRoom().getRoomNo() : 0;
        double rate = (res != null && res.getRoom() != null) ? res.getRoom().getPrice() : 0.0;
        long nights = 1;
        if (res != null && res.getCheckInDate() != null && res.getCheckOutDate() != null) {
            nights = ChronoUnit.DAYS.between(res.getCheckInDate(), res.getCheckOutDate());
            if (nights <= 0) nights = 1;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================\n");
        sb.append("                    HOTEL ASTRA                          \n");
        sb.append("          LUXURY RESIDENCE & SUITES                      \n");
        sb.append("          100 Ocean Drive, Beverly Hills, CA             \n");
        sb.append("          Phone: +1 (800) 555-ASTRA                      \n");
        sb.append("=========================================================\n");
        sb.append("OFFICIAL PAYMENT RECEIPT & BILL\n");
        sb.append("Date & Time    : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Payment ID     : #").append(payment.getPaymentId()).append("\n");
        sb.append("Reservation ID : #").append(res != null ? res.getReservationId() : "N/A").append("\n");
        sb.append("---------------------------------------------------------\n");
        sb.append("GUEST DETAILS:\n");
        sb.append("Name           : ").append(custName).append("\n");
        sb.append("Email          : ").append(custEmail).append("\n");
        sb.append("---------------------------------------------------------\n");
        sb.append("RESERVATION DETAILS:\n");
        sb.append("Room Allocated : Room ").append(roomNo).append(" [").append(roomType).append(" Suite]\n");
        sb.append("Check-In Date  : ").append(res != null ? res.getCheckInDate() : "N/A").append("\n");
        sb.append("Check-Out Date : ").append(res != null ? res.getCheckOutDate() : "N/A").append("\n");
        sb.append("Duration       : ").append(nights).append(" night(s)\n");
        sb.append("Nightly Tariff : ₹").append(String.format("%.2f", rate)).append("\n");
        sb.append("---------------------------------------------------------\n");
        sb.append("ITEMIZED CHARGES:\n");
        sb.append(String.format(" %-30s %10s %10s\n", "Description", "Qty", "Amount"));
        sb.append(String.format(" %-30s %10d ₹%9.2f\n", roomType + " Room Stay", nights, nights * rate));
        sb.append(String.format(" %-30s %10s ₹%9.2f\n", "Service & Maintenance (0%)", "Included", 0.0));
        sb.append("---------------------------------------------------------\n");
        sb.append(String.format(" TOTAL AMOUNT  : ₹%39.2f\n", payment.getAmount()));
        sb.append(" Payment Method: ").append(payment.getPaymentMethod()).append("\n");
        sb.append(" Payment Status: ").append(payment.getPaymentStatus()).append(" (CONFIRMED)\n");
        sb.append("=========================================================\n");
        sb.append("       Thank you for choosing Hotel Astra!               \n");
        sb.append("   Please present this receipt at reception on check-in. \n");
        sb.append("=========================================================\n");
        return sb.toString();
    }
}

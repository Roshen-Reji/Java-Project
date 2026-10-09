# Hotel Reservation System - College Project Guide

## 1. Project Overview
This project is a console-based **Hotel Reservation System** built in pure Java for an Object-Oriented Programming (OOP) course. It simulates a hotel management system with three main roles: **Customer**, **Hotel Staff**, and **Administrator**.

---

## 2. Key OOP Concepts Implemented

### Inheritance Hierarchy
1. **User / Person Hierarchy**:
   - `Person` (Base Class): Contains common attributes such as `id`, `name`, `phone`, `email`, and method `displayDetails()`.
   - `Customer` (extends `Person`): Adds `address`, dynamic reservation tracking, room booking, and cancellation.
   - `HotelStaff` (extends `Person`): Adds `role` and staff operations (viewing rooms, customers, and reservations).
   - `Admin` (extends `HotelStaff`): Demonstrates **multilevel inheritance**. Adds administrative powers (creating/deleting rooms and staff members).

2. **Room Hierarchy**:
   - `Room` (Base Class): Base properties (`roomNo`, `roomType`, `price`, `status`, `description`).
   - `StandardRoom` (extends `Room`): Default standard room setup.
   - `DeluxeRoom` (extends `Room`): Deluxe room specification.
   - `SuiteRoom` (extends `Room`): Luxury suite specification.

3. **Payment Hierarchy**:
   - `Payment` (Base Class): Common payment attributes (`paymentId`, `amount`, `paymentMethod`, `paymentStatus`, `paymentDate`).
   - `UpiPayment` (extends `Payment`): Handles UPI ID and UPI-specific billing.
   - `CardPayment` (extends `Payment`): Handles card number masking and cardholder details.
   - `CashPayment` (extends `Payment`): Handles counter cash collection notes.

### Other OOP Concepts
- **Encapsulation**: Private and protected fields accessed via getters and setters.
- **Polymorphism & Method Overriding**: Subclasses override `displayDetails()` and `generateBill()` using `super`.
- **Constructor Chaining**: Subclass constructors invoke `super(...)`.

---

## 3. How to Run the Program

### Prerequisites
- Java Development Kit (JDK 8 or higher, e.g., JDK 17 / 21).

### Step 1: Open Terminal / Command Prompt
Navigate to the project root folder:
```bash
cd "d:\Roshen\Java Project"
```

### Step 2: Compile the Java Files
Compile all Java files using `javac`:
```bash
javac com/hotel/Main.java com/hotel/domain/*.java com/hotel/repository/*.java com/hotel/service/*.java com/hotel/ui/*.java
```

### Step 3: Run the Application
Execute the compiled `Main` class:
```bash
java com.hotel.Main
```

---

## 4. Default Seeded Data

When the application launches, initial sample records are automatically loaded:
- **Admin**:
  - Name: `Master Admin`, ID: `999`, Default PIN: `1234`
- **Staff Members**:
  - Staff ID 1: `Alice` (Manager)
  - Staff ID 2: `Bob` (Receptionist)
- **Rooms**:
  - Room 101: `Deluxe` ($80.00 / night, Available)
  - Room 102: `Standard` ($50.00 / night, Available)
  - Room 103: `Suite` ($150.00 / night, Available)
  - Room 104: `Standard` ($55.00 / night, Available)
  - Room 105: `Deluxe` ($90.00 / night, Maintenance)

---

## 5. System Features and Workflow Guide

### A. Customer Flow
1. **Register**: Choose option `1` to enter your name, phone number, email, and address.
2. **Login**: Login with your registered email or phone number.
3. **View Available Rooms**: View list of rooms currently marked `Available`.
4. **Book a Room**:
   - Enter Room Number (e.g., `101`).
   - Enter Check-in Date (format `YYYY-MM-DD`, e.g., `2026-10-15`).
   - Enter Check-out Date (format `YYYY-MM-DD`, e.g., `2026-10-17`).
   - System calculates total price.
   - Select Payment Method:
     - `1. UPI` (Enter UPI ID, e.g., `student@upi`)
     - `2. Card` (Enter 16-digit Card Number and Card Holder Name)
     - `3. Cash` (Pay at front desk)
   - Reservation is placed with status: **`Pending Approval`**. The room is held as **`Pending`** to avoid double booking.
5. **View My Bookings + Bill**:
   - Displays all reservations associated with your account across sessions, booking status, and detailed itemized bills.
6. **Cancel a Reservation**:
   - Enter Reservation ID.
   - Checks if the reservation belongs to your account and is active.
   - Cancels booking, releases the room back to `Available`, and automatically refunds/cancels the payment.

### B. Staff Flow
1. From Main Menu, select `2. Staff Portal`.
2. Select `3. View pending reservations` to view bookings submitted by customers.
3. Select `4. Confirm pending payment & reservation`:
   - Enter the Reservation ID.
   - Choose `1. Confirm` to confirm payment and finalize the room reservation (status changes to `Confirmed`, room status becomes `Booked`).
   - Or choose `2. Reject` to cancel the request and make the room `Available` again.
4. Staff can also view all rooms, update room status, view all customers, or view all reservations.

### C. Administrator Flow (Powerful Admin)
1. From Main Menu, select `3. Admin Portal`.
2. Enter the Admin PIN: `1234` (or `admin`).
3. Administrative operations include:
   - **Room Management**: Add new rooms (Standard, Deluxe, Suite), update room prices/statuses, delete rooms.
   - **Staff Management**: Add new staff members with specific roles, view staff list, delete staff members.
   - **Customer Management**: View all registered customers, delete customer records.
   - **Reservation & Payment Management**: View all reservations, confirm pending payments, cancel reservations, inspect payment transactions.

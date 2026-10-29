# AGENT.md — Hotel Management System (Astra) Upgrade Plan
**Target Environment:** Java SE (JDK 17/21+), JDBC (SQLite / MySQL), Java Swing (`javax.swing`, `java.awt`)
**Academic Standard:** KTU 2024 Scheme — S3 Object-Oriented Programming in Java

---

## 0. Agent Directives & Ground Rules
1. **Inspect Before Modifying:** Before writing any code, read all existing `.java` files inside:
   - `com/hotel/Main.java`
   - `com/hotel/domain/*` (`Person`, `Customer`, `HotelStaff`, `Admin`, `Room`, `StandardRoom`, `DeluxeRoom`, `SuiteRoom`, `Reservation`, `Payment`, `CardPayment`, `CashPayment`, `UpiPayment`)
   - `com/hotel/repository/*` (`CustomerRepository`, `RoomRepository`, `ReservationRepository`, `PaymentRepository`, `StaffRepository`)
   - `com/hotel/service/*` (`AuthenticationCustomerService`, `RoomManagementService`, `ReservationService`, `PaymentService`, `PaymentGateway`, `StaffService`)
   - `com/hotel/ui/*` (`AdminPortal`, `CustomerPortal`, `StaffPortal`)
2. **Preserve OOP & Service Contracts:** Do **not** flatten or delete the domain inheritance hierarchies (`Person`, `Room`, `Payment`). Keep existing public method signatures in `com.hotel.service.*` and `com.hotel.domain.*` intact wherever possible so business logic does not break.
3. **Zero Broken Builds:** Compile the project (`javac`) after completing each phase to verify there are no missing imports, type mismatches, or unhandled `SQLException` errors.
4. **Delete Stale `.class` Files:** Clean out pre-compiled `.class` files in the source tree and ensure compilation outputs cleanly.

---

## Phase 1: SQL Database & JDBC Infrastructure (`com.hotel.database`)

Create a new package `com.hotel.database` to handle persistent storage using standard JDBC (`java.sql.*`).

### 1.1 `DatabaseConnection.java`
- Implement a singleton/utility connection manager using `java.sql.DriverManager` and `java.sql.Connection`.
- Default to **SQLite** (`jdbc:sqlite:hotel_astra.db`) for zero-configuration portability during lab evaluation, with easy fallback configuration constants (`DB_URL`, `DB_USER`, `DB_PASSWORD`) for **MySQL** (`jdbc:mysql://localhost:3306/hotel_astra`).
- Provide a static method `public static Connection getConnection() throws SQLException`.

### 1.2 `DatabaseInitializer.java` (Schema & Seed Data)
- Create a method `public static void initialize()` that executes `CREATE TABLE IF NOT EXISTS` statements on startup for the 5 core entities:
  1. **`customers`**:
     - `customer_id INT PRIMARY KEY`
     - `name VARCHAR(100) NOT NULL`
     - `email VARCHAR(100) UNIQUE NOT NULL`
     - `phone VARCHAR(20)`
     - `address VARCHAR(255)`
     - `password VARCHAR(100)` (check `Customer.java` and `AuthenticationCustomerService.java` for exact auth fields used in the current codebase and match them).
  2. **`staff`**:
     - `staff_id INT PRIMARY KEY`
     - `name VARCHAR(100) NOT NULL`
     - `role VARCHAR(50) NOT NULL` (Discriminator: `'ADMIN'` or `'STAFF'` to map to `Admin` and `HotelStaff` subclasses)
     - `email VARCHAR(100) UNIQUE`
     - `phone VARCHAR(20)`
     - `password VARCHAR(100)` (if used by existing auth/staff login).
  3. **`rooms`**:
     - `room_id INT PRIMARY KEY` (or `room_no` matching `Room.java`)
     - `room_type VARCHAR(50) NOT NULL` (Discriminator: `'STANDARD'`, `'DELUXE'`, `'SUITE'` to instantiate `StandardRoom`, `DeluxeRoom`, or `SuiteRoom`)
     - `price DOUBLE NOT NULL`
     - `status VARCHAR(30) NOT NULL` (e.g., `'AVAILABLE'`, `'BOOKED'`, `'OCCUPIED'`, `'MAINTENANCE'`)
     - `description VARCHAR(255)` (and any subclass-specific attributes present in `StandardRoom`, `DeluxeRoom`, `SuiteRoom`).
  4. **`reservations`**:
     - `reservation_id INT PRIMARY KEY`
     - `customer_id INT NOT NULL`
     - `room_id INT NOT NULL`
     - `check_in_date VARCHAR(30) NOT NULL` (or `DATE`/`BIGINT` matching the exact type of `checkInDate` in `Reservation.java`)
     - `check_out_date VARCHAR(30) NOT NULL`
     - `status VARCHAR(30) NOT NULL` (`'CONFIRMED'`, `'CANCELLED'`, `'COMPLETED'`, `'PENDING'`)
     - `total_amount DOUBLE`
     - Foreign keys referencing `customers(customer_id)` and `rooms(room_id)`.
  5. **`payments`**:
     - `payment_id INT PRIMARY KEY`
     - `reservation_id INT`
     - `amount DOUBLE NOT NULL`
     - `payment_method VARCHAR(30) NOT NULL` (Discriminator: `'CARD'`, `'CASH'`, `'UPI'` to instantiate `CardPayment`, `CashPayment`, or `UpiPayment`)
     - `payment_status VARCHAR(30) NOT NULL`
     - `payment_date VARCHAR(30)`
     - `payment_details VARCHAR(255)` (to store masked card number / UPI ID / cash receipt info from subclasses).
- **Seed Data:** If the `rooms` and `staff` tables are empty on startup, automatically insert:
  - 6 sample rooms (2 `StandardRoom`, 2 `DeluxeRoom`, 2 `SuiteRoom`).
  - 1 default `Admin` account and 2 default `HotelStaff` accounts.
  - 1 default `Customer` account for quick testing.

---

## Phase 2: Repository Layer JDBC Refactoring (`com.hotel.repository`)

Refactor all 5 repositories to replace in-memory `List`/`Map` storage with SQL queries using `PreparedStatement` and `ResultSet` (preventing SQL injection and satisfying KTU JDBC best practices). **Keep all existing public method names and return types identical.**

### 2.1 Polymorphic Object Reconstruction (Crucial OOP Requirement)
- **`RoomRepository.java`**:
  - When reading a row from `rooms`, inspect `rs.getString("room_type")` and instantiate the concrete subclass: `new StandardRoom(...)`, `new DeluxeRoom(...)`, or `new SuiteRoom(...)`.
  - Implement `save/add`, `findById/findByRoomNo`, `findAll`, `findAvailableRooms`, `updateStatus`, and `delete` via SQL `INSERT`, `SELECT`, `UPDATE`, and `DELETE`.
- **`CustomerRepository.java`**:
  - Replace in-memory collection operations with SQL `INSERT INTO customers`, `SELECT * FROM customers WHERE ...`, `UPDATE customers`, etc.
- **`StaffRepository.java`**:
  - Reconstruct either `Admin` or `HotelStaff` based on the `role` column in `ResultSet`.
- **`ReservationRepository.java`**:
  - Implement SQL CRUD for `Reservation`. When reconstructing a `Reservation` object, if `Reservation.java` holds object references to `Customer` and `Room` (rather than just IDs), resolve them via `CustomerRepository` and `RoomRepository` or a SQL `JOIN`.
- **`PaymentRepository.java`**:
  - Inspect `payment_method` (`CARD`, `CASH`, `UPI`) and instantiate `CardPayment`, `CashPayment`, or `UpiPayment` accordingly.

---

## Phase 3: Java Swing User Interface (`com.hotel.ui.swing`)

Keep the existing console portals (`AdminPortal`, `CustomerPortal`, `StaffPortal`) intact for backward compatibility, and create a new package `com.hotel.ui.swing` implementing the full desktop GUI following the System Workflow.

### 3.1 Visual Theme & Styling (`UIStyle.java`)
- Configure `UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())` or `NimbusLookAndFeel`.
- Define a centralized palette in `UIStyle.java`:
  - Primary Dark Navy/Slate sidebar (`#1E293B`), Accent Gold/Indigo buttons (`#4F46E5` / `#0EA5E9`), Clean Light background (`#F8FAFC`), Card surface (`#FFFFFF`), Status badges (Green for Available/Confirmed, Red for Occupied/Cancelled).
  - Helper factory methods for styled `JButton`, `JTextField`, `JLabel`, and `JTable` headers with clean padding (`EmptyBorder`) and readable sans-serif fonts (`Segoe UI` / `SansSerif`, 14pt).

### 3.2 Main Window & Navigation (`HotelAppFrame.java`)
- Extend `JFrame` (`title: "Astra Hotel Reservation & Management System"`), size `1150x750`, centered on screen (`setLocationRelativeTo(null)`), `EXIT_ON_CLOSE`.
- Use a top-level `CardLayout` containing 4 primary views:
  1. `WelcomeAuthPanel` (Role Selection, Customer Login & Registration, Staff/Admin Login)
  2. `CustomerDashboardPanel` (Customer Portal)
  3. `StaffDashboardPanel` (Hotel Staff Portal)
  4. `AdminDashboardPanel` (Admin Portal)
- Pass the existing service instances (`AuthenticationCustomerService`, `RoomManagementService`, `ReservationService`, `PaymentService`, `StaffService`) into the panels via constructor dependency injection.

### 3.3 `WelcomeAuthPanel.java`
- Split layout: Left side displays Hotel Astra branding and system status; Right side provides a `JTabbedPane` for:
  - **Customer Login / Sign Up:** Fields for Name, Email, Phone, Address, Password (matching `AuthenticationCustomerService` methods).
  - **Staff / Admin Portal Access:** Staff ID / Credentials login routing to either `StaffDashboardPanel` or `AdminDashboardPanel`.

### 3.4 `CustomerDashboardPanel.java` (8-Step Workflow Implementation)
Use a left sidebar navigation + `CardLayout` content area with 3 views:
1. **Browse & Book Rooms View:**
   - Top filter bar: Room Type dropdown (`All`, `Standard`, `Deluxe`, `Suite`) and Refresh button.
   - Center: `JTable` with `DefaultTableModel` displaying Room No, Type, Price/Night, Status, and Description.
   - Bottom/Right Booking Form: Selected Room No, Check-In Date (`YYYY-MM-DD`), Check-Out Date (`YYYY-MM-DD`), and **"Book & Proceed to Payment"** button.
2. **Payment & Bill Dialog (`PaymentDialog.java` - `JDialog`):**
   - Triggered upon booking confirmation.
   - Radio buttons / ComboBox for Payment Method: **Card**, **UPI**, or **Cash**.
   - Dynamic input fields (Card Number/CVV for `CardPayment`, UPI ID for `UpiPayment`, Cash Tendered for `CashPayment`).
   - Invokes `PaymentService` / `PaymentGateway` polymorphically, updates reservation & room status in SQL DB, and displays a formatted **Itemized Bill Receipt** in a monospaced `JTextArea` with a "Print / Save Receipt" option.
3. **My Reservations View:**
   - `JTable` listing all bookings for the logged-in customer (`ReservationService`).
   - **"Cancel Selected Reservation"** button (`ActionListener`) that calls `ReservationService`, frees the room back to `AVAILABLE`, and refreshes the table.
   - **"View Bill"** button to re-open the receipt for any paid reservation.

### 3.5 `StaffDashboardPanel.java`
- **All Reservations Tab:** `JTable` of all customer reservations with buttons to **Confirm Booking**, **Check-In / Check-Out**, or **Cancel Reservation**.
- **Room Status Management Tab:** `JTable` of all rooms allowing staff to toggle room status (`AVAILABLE`, `OCCUPIED`, `MAINTENANCE`) in real time via `RoomManagementService`.
- **Customer Directory Tab:** Searchable `JTable` of registered customers and their contact details.

### 3.6 `AdminDashboardPanel.java`
- **Manage Rooms Tab:** Form to add new `StandardRoom`, `DeluxeRoom`, or `SuiteRoom` records to the database, update room pricing, or delete rooms.
- **Manage Staff Tab:** Form and `JTable` to add/view/remove `HotelStaff` and `Admin` records via `StaffService`.
- **Payments & Revenue Overview Tab:** Summary cards (Total Bookings, Available Rooms, Total Revenue) + `JTable` showing all recorded transactions from `PaymentRepository`.

---

## Phase 4: Application Entry Point & Build Verification (`com.hotel.Main`)

1. Update `com/hotel/Main.java`:
   - Call `DatabaseInitializer.initialize()` on startup.
   - Instantiate the repositories and services.
   - If launched with `--cli` argument, run the legacy console menu (`CustomerPortal`, `StaffPortal`, `AdminPortal`).
   - By default (no arguments), launch `HotelAppFrame` on the Swing Event Dispatch Thread using `SwingUtilities.invokeLater(() -> new HotelAppFrame(...).setVisible(true))`.
2. Update `README.md` with:
   - Instructions to download/include `sqlite-jdbc.jar` (or `mysql-connector-j.jar`) in the `lib/` folder.
   - Exact compile and run commands for Windows and Linux/macOS:
     ```bash
     javac -cp ".:lib/*" com/hotel/Main.java
     java -cp ".:lib/*" com.hotel.Main
     ```
     *(Use `;` instead of `:` on Windows PowerShell/CMD).*

---

## Phase 5: Self-Verification Checklist (Agent Must Complete Before Finishing)
- [ ] All `.java` files compile with zero errors.
- [ ] `DatabaseInitializer` creates all 5 SQL tables and seeds initial rooms and staff without throwing `SQLException`.
- [ ] All 5 repositories execute `PreparedStatement` queries and properly reconstruct polymorphic subclasses (`StandardRoom`/`DeluxeRoom`/`SuiteRoom` and `CardPayment`/`CashPayment`/`UpiPayment`).
- [ ] Swing GUI launches via `SwingUtilities.invokeLater` and performs end-to-end Customer Registration -> Room Selection -> Reservation Creation -> Polymorphic Payment -> Bill Generation -> Cancellation, persisting data across application restarts.
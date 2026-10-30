# Astra — Hotel Reservation & Management System
**Course:** KTU 2024 Scheme — S3 Object-Oriented Programming in Java  
**Institution:** College of Engineering, Kidangoor (Department of Computer Science & Engineering)  
**Team 7 (Astra):** Aban Muhammed, Anfal Navas, Cyriac Thomas, Devakrishna M, Harisankar Dinesh, Roshen Reji, Samson Thomas

---

## 1. Executive Summary (Presentation Pitch)
> *"Good morning/afternoon. We are Team 7 — Astra, and today we are presenting our Hotel Reservation and Management System built in Java.*  
> *Manual hotel booking operations often suffer from slow record-keeping, lack of real-time room availability updates, and errors in billing or cancellations. Our system solves this by integrating a clean **Java Swing desktop interface**, a strictly layered **Object-Oriented business architecture**, and a persistent **SQL database via JDBC**.*  
> *It provides dedicated role-based portals for Customers, Hotel Staff, and Administrators—automating the entire lifecycle from room discovery and reservation to polymorphic payment processing and itemized bill generation."*

---

## 2. System Architecture (4-Tier Layered Design)
Our codebase (`com.hotel`) is organized into four distinct architectural layers to ensure separation of concerns, modularity, and maintainability:

1. **User Interface Layer (`com.hotel.ui.swing` & `com.hotel.ui`):**
   - Built with **Java Swing (`javax.swing`, `java.awt`)** using `CardLayout` navigation, `JTabbedPane` authentication, and interactive `JTable` dashboards for Customers, Staff, and Admins.
   - Retains legacy CLI support (`CustomerPortal`, `StaffPortal`, `AdminPortal`) via the `--cli` flag.
2. **Application / Service Layer (`com.hotel.service`):**
   - Houses core business rules and validations across `AuthenticationCustomerService`, `ReservationService`, `RoomManagementService`, `PaymentService`, `PaymentGateway`, and `StaffService`.
3. **Domain / Business Layer (`com.hotel.domain`):**
   - Models real-world hospitality entities using pure Object-Oriented principles (`Person`, `Customer`, `HotelStaff`, `Admin`, `Room`, `StandardRoom`, `DeluxeRoom`, `SuiteRoom`, `Reservation`, `Payment`, `CardPayment`, `CashPayment`, `UpiPayment`).
4. **Data Persistence Layer (`com.hotel.repository` & `com.hotel.database`):**
   - Implements the Repository Pattern backed by **JDBC (`java.sql.*`)** using parameterized `PreparedStatement` queries against a relational SQL database (`customers`, `staff`, `rooms`, `reservations`, `payments`).

---

## 3. Core OOP Concepts Demonstrated (Viva Reference)

| OOP Concept | How We Implemented It in Code | Classes Involved |
| :--- | :--- | :--- |
| **1. Class & Object** | Blueprints defining state (attributes) and behavior (methods) instantiated dynamically for every booking, room, and user. | `Reservation`, `Customer`, `Room`, `Payment` |
| **2. Encapsulation** | All domain attributes are declared `private` or `protected`, accessed and mutated strictly through validated public getters and setters. | All classes in `com.hotel.domain.*` |
| **3. Inheritance** | Hierarchical reuse of common attributes and behaviors from parent superclasses to specialized child subclasses. | `Person` $\rightarrow$ `Customer`, `HotelStaff`, `Admin`<br>`Room` $\rightarrow$ `StandardRoom`, `DeluxeRoom`, `SuiteRoom`<br>`Payment` $\rightarrow$ `CardPayment`, `CashPayment`, `UpiPayment` |
| **4. Polymorphism** | Method overriding and dynamic method dispatch: the service and repository layers process general `Room` and `Payment` references while executing subclass-specific pricing, status, and payment logic at runtime. | `PaymentService` processing `CardPayment`, `UpiPayment`, and `CashPayment` through `Payment` |
| **5. Abstraction** | Base classes and service interfaces hide low-level SQL and payment gateway complexities from the UI panels. | `PaymentGateway`, `Room`, `Payment`, and Repository classes |

---

## 4. End-to-End System Workflow (8-Step Automated Process)
1. **Customer Authentication & Entry:** Customer registers a new profile or logs in with their registered email/phone and password via `AuthenticationCustomerService`.
2. **Real-Time Availability Check:** `RoomManagementService` queries the SQL `rooms` table in real time to filter available inventory.
3. **Room Selection:** Customer browses `StandardRoom`, `DeluxeRoom`, and `SuiteRoom` options and selects check-in/check-out dates.
4. **Reservation Creation:** `ReservationService` validates dates, calculates total stay cost, and creates a `Reservation` record in the database.
5. **Booking Confirmation:** Room status is atomically updated in the SQL database to prevent double-booking.
6. **Polymorphic Payment Processing:** Customer selects **UPI**, **Card**, or **Cash**; `PaymentService` processes the specific `Payment` subclass via `PaymentGateway`.
7. **Itemized Bill Generation:** System generates a formatted receipt detailing customer info, room type, stay duration, payment method, and transaction status.
8. **Real-Time Status Synchronization:** Staff and Admin dashboards immediately reflect updated occupancy, reservation logs, and total revenue.

---

## 5. Role-Based Portals & Features
* **Customer Portal:**
  - Filter available rooms by category (`Standard`, `Deluxe`, `Suite`).
  - Book rooms with check-in/check-out date validation.
  - Complete payments via Card, UPI, or Cash and generate printable bills.
  - View booking history and cancel active reservations (which automatically frees the room back to `AVAILABLE`).
* **Hotel Staff Portal:**
  - View and manage all customer reservations (Confirm, Check-In, Check-Out, Cancel).
  - Toggle room statuses (`AVAILABLE`, `OCCUPIED`, `MAINTENANCE`) in real time.
  - Search and view the customer directory.
* **Admin Portal:**
  - Add new rooms (`Standard`, `Deluxe`, `Suite`), modify room pricing, or remove rooms.
  - Manage hotel staff and administrator accounts.
  - Monitor total bookings, room occupancy, and complete payment/revenue logs.

---

## 6. Default Database Seed Credentials (For Live Demo)

| Portal Role | Login Field (Email / Phone / ID) | Password |
| :--- | :--- | :--- |
| **Demo Customer** | `john@example.com` or `+1 555-123-4567` | `password123` |
| **Hotel Staff** | `1` or `alice@hotel.com` | `staff` |
| **Administrator** | `999` or `admin@hotel.com` | `admin` |

---

## 7. How to Compile and Run

### Windows (PowerShell / CMD)
```bash
# Compile
javac -cp ".;lib/*" com/hotel/database/*.java com/hotel/domain/*.java com/hotel/repository/*.java com/hotel/service/*.java com/hotel/ui/swing/*.java com/hotel/ui/*.java com/hotel/*.java

# Run Desktop Swing GUI (Default)
java -cp ".;lib/*" com.hotel.Main

# Run Interactive CLI Console Mode
java -cp ".;lib/*" com.hotel.Main --cli

# Run CLI Database Inspector (Interactive Menu)
java -cp ".;lib/*" com.hotel.Main --db

# Run CLI Database Inspector (One-shot Full Database Dump)
java -cp ".;lib/*" com.hotel.Main --db-dump
```

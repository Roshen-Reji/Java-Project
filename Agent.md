# AGENT.md — Top Booking Bar, Interactive Calendar Picker & Date-Range Room Availability
**Target Packages:** `com.hotel.ui.swing`, `com.hotel.service`, `com.hotel.repository`

---

## Task 1: Move Customer Booking & Filter Bar to the Top (`CustomerDashboardPanel.java`)

1. Open `com/hotel/ui/swing/CustomerDashboardPanel.java` in the **Browse & Book Rooms** view.
2. **Remove the booking input form from the bottom (`BorderLayout.SOUTH`)** and consolidate the **Filter Bar + Booking Bar at the very top (`BorderLayout.NORTH`)** directly above the Rooms `JTable` (`BorderLayout.CENTER`).
3. Structure the top bar using a clean two-row card panel (`GridBagLayout` or `FlowLayout` inside a titled/padded white card container):
   - **Row 1 (Search & Dates):**
     - `Room Type:` `JComboBox<String>` (`All`, `STANDARD`, `DELUXE`, `SUITE`)
     - `Check-In Date:` Read-only display field + **"📅 Select Check-In"** calendar button (defaults to today: `LocalDate.now()`).
     - `Check-Out Date:` Read-only display field + **"📅 Select Check-Out"** calendar button (defaults to tomorrow: `LocalDate.now().plusDays(1)`).
     - **"Check Availability / Refresh"** button.
   - **Row 2 (Selected Room & Action):**
     - `Selected Room No:` Read-only/auto-filled badge or field populated when clicking a row in the table below.
     - `Availability Notice:` Dynamic status label (`JLabel`) showing exact booking window details for the selected room (e.g., *"Room #101 is available for your dates! (Available until 2026-11-10)"*).
     - **"Book & Proceed to Payment"** primary action button on the right.

---

## Task 2: Replace Typing with an Interactive Swing Calendar Page (`CalendarPickerDialog.java`)

1. Create a new reusable class `com/hotel/ui/swing/CalendarPickerDialog.java` extending `JDialog` (modal) using standard `java.time.LocalDate` and `java.time.YearMonth` (zero external JAR dependencies required).
2. **Calendar Page UI & Behavior:**
   - **Header Bar:**
     - `"◀ Prev"` button (`currentYearMonth = currentYearMonth.minusMonths(1)`).
     - Center `JLabel` displaying `"MMMM yyyy"` (e.g., `"November 2026"`).
     - `"Next ▶"` button (`currentYearMonth = currentYearMonth.plusMonths(1)`).
   - **Day-of-Week Header Grid (7 Columns):** `Sun`, `Mon`, `Tue`, `Wed`, `Thu`, `Fri`, `Sat`.
   - **Day Buttons Grid (`6x7` `GridLayout`):**
     - Render day numbers `1` through `currentYearMonth.lengthOfMonth()` aligned under the correct weekday column.
     - **Disable Past Dates:** Any date before `LocalDate.now()` (or before `checkInDate.plusDays(1)` when picking Check-Out) must be disabled (`setEnabled(false)`) and grayed out.
     - **Highlight Reserved Dates (Visual Aid):** If a room is currently selected in the table, pass its active `List<Reservation>` into `CalendarPickerDialog`:
       - Color dates that are already booked by another customer in soft amber/red (`new Color(254, 226, 226)`) with a tooltip (`"Booked from " + checkIn + " to " + checkOut`), while keeping the **check-in day of a future reservation clickable as a valid Check-Out date** so a guest can book right up until the next guest arrives!
     - **Single-Click Selection:** Clicking a valid date button sets the selected `LocalDate`, closes the dialog, updates the Check-In or Check-Out field in `CustomerDashboardPanel`, and automatically refreshes the Rooms `JTable` availability column.
3. **Remove Manual Typing:**
   - Make the Check-In and Check-Out fields non-editable by keyboard (`setEditable(false)`), and trigger `CalendarPickerDialog` whenever the user clicks either the date text field (`MouseListener`) or the **"📅 Select Check-In / Check-Out"** button.

---

## Task 3: Date-Range Room Availability & Pre-Arrival Booking Logic (`ReservationRepository` & `ReservationService`)

### Core Rule
If Customer A books a room for 10 days later (e.g., `2026-11-10` to `2026-11-15`), that room **MUST still appear to Customer B** and **MUST be bookable for any dates before `2026-11-10` (including checking out ON `2026-11-10` when Customer A arrives)**, or after `2026-11-15`.

1. **In `com/hotel/repository/ReservationRepository.java`:**
   - Add a method to fetch all active/upcoming reservations for a specific room, sorted chronologically:
     ```java
     public List<Reservation> findActiveReservationsByRoomId(int roomId) {
         List<Reservation> list = new ArrayList<>();
         String sql = "SELECT * FROM reservations WHERE room_id = ? " +
                      "AND UPPER(status) NOT IN ('CANCELLED', 'COMPLETED', 'CHECKED_OUT') " +
                      "ORDER BY check_in_date ASC";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, roomId);
             try (ResultSet rs = pstmt.executeQuery()) {
                 while (rs.next()) {
                     list.add(mapResultSetToReservation(rs));
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return list;
     }
     ```
   - Add a method `isRoomAvailableForDateRange(int roomId, LocalDate reqCheckIn, LocalDate reqCheckOut)`:
     - Parse each active reservation's `checkInDate` and `checkOutDate` into `LocalDate`.
     - **Overlap Formula (Allows Same-Day Turnover):**
       Two stays overlap **if and only if**:
       `reqCheckIn.isBefore(existingCheckOut) && reqCheckOut.isAfter(existingCheckIn)`
     - Notice that if Customer A arrives on `2026-11-10` (`existingCheckIn = 2026-11-10`) and Customer B selects `reqCheckOut = 2026-11-10`, `reqCheckOut.isAfter(existingCheckIn)` is `false` — meaning **Customer B IS allowed to book right up until the day Customer A arrives!**

2. **In `com/hotel/service/ReservationService.java` & `RoomManagementService.java`:**
   - **Do NOT permanently block a room just because its status column says `'BOOKED'` for a future reservation.**
   - Only block a room globally if its status is `'MAINTENANCE'`.
   - When creating a reservation in `ReservationService`:
     - Validate that `checkOutDate.isAfter(checkInDate)` and `!checkInDate.isBefore(LocalDate.now())`.
     - Check `reservationRepository.isRoomAvailableForDateRange(roomId, checkInDate, checkOutDate)`.
     - If there is an overlap, throw a descriptive exception or return a clear error message stating the exact conflicting dates and when the room is free (e.g., *"Room #102 is already reserved from 2026-11-10 to 2026-11-15. You can book it before 2026-11-10 (Check-Out on or before 2026-11-10) or from 2026-11-15 onwards."*).

---

## Task 4: Display Clear Upcoming Booking & Availability Information to Customers

1. **Add an `"Availability & Schedule Info"` Column to the Customer Rooms `JTable` (`CustomerDashboardPanel.java`):**
   - Update the Customer Rooms table columns to:
     `["Room No", "Room Type", "Price / Night (₹)", "Status for Selected Dates", "Upcoming Schedule / Availability Info", "Description"]`
   - For every room (except `'MAINTENANCE'`), compute its status dynamically based on the currently selected `Check-In` and `Check-Out` dates in the Top Bar:
     - **Case 1 — Room has NO active reservations:**
       - `Status for Selected Dates`: `"AVAILABLE"`
       - `Upcoming Schedule / Availability Info`: `"Open — Available for all dates"`
     - **Case 2 — Room has a future reservation (e.g., 10 days later: `2026-11-10` to `2026-11-15`), and Customer's selected dates do NOT overlap (e.g., `2026-10-31` to `2026-11-10`):**
       - `Status for Selected Dates`: `"AVAILABLE (Until 2026-11-10)"`
       - `Upcoming Schedule / Availability Info`: `"Free now until 2026-11-10 | Next Booking: 2026-11-10 to 2026-11-15"`
     - **Case 3 — Customer's selected dates overlap with an existing reservation (`2026-11-10` to `2026-11-15`):**
       - `Status for Selected Dates`: `"BOOKED FOR SELECTED DATES"`
       - `Upcoming Schedule / Availability Info`: `"Reserved: 2026-11-10 to 2026-11-15 (Available before 2026-11-10 or after 2026-11-15)"`
2. **Interactive Banner & Dialog Feedback:**
   - When a customer clicks a room in the table that has an upcoming reservation, update the top bar's `Availability Notice` banner in blue/green if their dates fit before the future reservation (e.g., *"✔ Available for your stay! Note: Another guest arrives on 2026-11-10, so checkout must be on or before 2026-11-10"*), or in red if their dates conflict.

---

## Task 5: Compile & Verify End-to-End Scenario
1. Compile all `.java` files (`javac -cp ".;lib/*" com/hotel/Main.java` or `javac -cp ".:lib/*" com/hotel/Main.java`) with **0 errors**.
2. **Test the 10-Day Future Booking Scenario:**
   - Log in as Customer 1, use the top **Calendar Picker** to book Room `101` for 10 days from today (e.g., Check-In: `+10 days`, Check-Out: `+14 days`), and complete payment.
   - Log in as Customer 2 (or stay in the Customer Portal) and view the Browse & Book Rooms table:
     - Verify the booking inputs are at the **top** of the screen.
     - Verify Room `101` is still visible in the table and clearly displays `"Free now until <Date+10> | Next Booking: <Date+10> to <Date+14>"`.
     - Select Check-In = `Today` and Check-Out = `<Date+10>` using the **Calendar Page** and confirm Customer 2 can successfully book Room `101` right up until Customer 1 arrives.
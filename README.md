# 📱 RepaRizz - Repair Shop Management System

> **"Giving broken gadgets their rizz back."**

RepaRizz is an Android-based Repair Shop Management System designed for repair shop administrators, technicians/staff, and customers. It streamlines device intake, diagnosis, repair estimates, customer approvals, repair progress tracking, final quality testing, payment & tip management, device collection, and digital service report generation.

Financial values are displayed in **Philippine Pesos (₱)**.

---

## 👥 Three Role Architecture & Demo Credentials

RepaRizz enforces role-based access control (RBAC) across three distinct user roles:

1. **ADMINISTRATOR (`ADMIN`)**
   - Full management control over shop operations, staff accounts, customer accounts, financial analytics, pricing, and all repair jobs.
   - **Demo Admin Email**: `admin@malolos.com` | **Password**: `admin123`

2. **STAFF / TECHNICIAN (`STAFF`)**
   - Operational access to register new device intakes, perform diagnoses, submit estimates, update repair stages, execute quality testing checklists, record payments, and release devices.
   - **Demo Staff Email**: `staff@malolos.com` | **Password**: `staff123`

3. **CUSTOMER / USER (`CUSTOMER`)**
   - Personal portal access to track owned or claimed repair devices, review diagnostic findings and cost estimates, approve or reject repair estimates, view payment history and receipts, and generate PDF service reports.
   - **Demo Customer Email**: `customer@malolos.com` | **Password**: `customer123`

---

## ⚙️ Key Features & Workflow

* **🔐 Authentication & Access Security**
  - Hashed password storage using SHA-256 with fixed salt (`PasswordHasher`).
  - Persistent session management with role verification (`SessionManager`).
  - Account recovery via "Forgot Password?" dialog.
  - Customer job isolation enforced at the Room database query level.
  - Customer job linking/claiming workflow using Repair Job ID & contact number verification.

* **📋 Complete 8-Stage Repair Lifecycle**
  1. **Received**: Device intake registered with customer info, specs, reported issue, physical condition, accessories, and before-repair photo.
  2. **Diagnosing**: Technician records fault diagnosis findings and itemized estimate breakdown (*Parts Cost, Labor Cost, Additional Charges*).
  3. **Awaiting Approval**: Customer reviews estimate and approves or rejects repair work.
  4. **Repairing**: Technician executes repair work and records notes.
  5. **Testing**: Required hardware/software testing checklist (*Display, Power/Battery, Audio, USB, OS Boot*) must pass before completion.
  6. **Completed**: Repair completed successfully; after-repair proof photo attached.
  7. **Awaiting Collection**: Device ready for customer pickup with balance display.
  8. **Released**: Device released to customer with release timestamp.

* **💳 Costs, Payments & Separate Tip Tracking**
  - **Total Cost** = Parts Cost + Labor Cost + Other Charges.
  - **Outstanding Balance** = Total Cost − Repair Payments.
  - Customer tips are tracked separately from repair charges to ensure accurate balance calculations.
  - Excess repair payment prompt allows classifying excess funds directly as a tip.

* **📄 Digital Service Report & Warranty PDF**
  - Generates official A4 PDF Service Reports and Warranty Certificates via native `PdfDocument` and `FileProvider`.
  - Supports direct in-app viewing, local downloading (`Downloads/RepaRizz/RepaRizz_Report_[JobID].pdf`), and sharing.

* **📊 Business Analytics & Audit Trail**
  - Shop-wide revenue, tip totals, outstanding unpaid balances, average turnaround time, and total warranty claims.
  - Frequency analysis of top reported device issues.
  - Persistent status change audit trail logging timestamp, staff notes, and state transitions.

---

## 🏗️ Architecture & Technology Stack

* **Language**: Kotlin & Java
* **UI Framework**: Material Design 3 (Dark Theme `#10141D`), ViewBinding, Navigation Component
* **Architecture**: MVVM (*Model-View-ViewModel*) + Repository Pattern
* **Local Database**: Room Persistence Library (`FixFlowDatabase`, SQLite)
* **Testing**: JUnit Unit Tests (`testDebugUnitTest`)

---

## 🛠️ How to Build and Run

1. Open **Android Studio**.
2. Open project directory `Fixflow` (`D:/Fixflow`).
3. Allow Gradle sync to finish.
4. Select a connected Android device or emulator (API 24 or newer).
5. Click **Run (`Shift + F10`)**.

### Running Unit Tests
To run unit tests in terminal or IDE:
```bash
./gradlew testDebugUnitTest
```
*Current test suite: 4 tests passed, 0 failed.*

### Building Debug APK
```bash
./gradlew assembleDebug
```
The output APK is generated at: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📁 Modified & Key Source Files

* **Data Layer**:
  - `UserEntity.java` & `UserDao.java`: User accounts and roles.
  - `RepairJob.java` & `RepairJobDao.java`: Repair job entity & queries.
  - `RepairEstimate.java` & `RepairEstimateDao.java`: Diagnostic cost estimates.
  - `Payment.java` & `PaymentDao.java`: Payments & tip records.
  - `RepairRepository.kt`: Centralized repository handling database operations and coroutines.

* **UI & ViewModel Layer**:
  - `MainActivity.kt`: Nav host controller & bottom navigation handling.
  - `UserLoginFragment.kt`, `AdminLoginFragment.kt`, `UserRegisterFragment.kt`, `WelcomeFragment.kt`: Authentication screens & forgot password dialogs.
  - `DashboardFragment.kt`, `AdminDashboardFragment.kt`, `StaffDashboardFragment.kt`, `UserDashboardFragment.kt`: Role-based dashboards.
  - `NewRepairJobFragment.kt`: Device intake registration.
  - `RepairDetailFragment.kt` & `RepairDetailViewModel.kt`: Diagnostic entry, stage stepper, testing checklist, payment/tip modal, PDF generation.
  - `RepairJobListFragment.kt` & `RepairJobsViewModel.kt`: Search & filter repair jobs.
  - `AnalyticsFragment.kt` & `AnalyticsViewModel.kt`: Financial and operational analytics.

* **Utilities**:
  - `PasswordHasher.kt`: SHA-256 salted password hashing.
  - `SessionManager.kt`: Shared preferences session manager.
  - `PdfReportGenerator.kt`: PDF report generation, viewing, downloading, and sharing.

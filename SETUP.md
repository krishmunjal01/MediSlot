# MediSlot — Environment Setup & Startup Guide

This document provides clear, step-by-step instructions to set up, build, and run **MediSlot** on any Windows, macOS, or Linux workstation.

---

## 1. Prerequisites

Ensure the following tools are installed:
1. **Java Development Kit (JDK)**: Version 17 LTS or higher (OpenJDK / Temurin / Oracle JDK).
   ```bash
   java -version
   ```
2. **Apache Maven**: Version 3.8+ or 3.9+.
   ```bash
   mvn -version
   ```
3. **MySQL Server**: Version 8.0+.
   ```bash
   mysql --version
   ```

---

## 2. MySQL Database Configuration

1. Make sure your MySQL 8.0 service is active.
   - On Windows: Run `Get-Service MySQL80` or start via `Services.msc`.
   - On Linux/macOS: `sudo systemctl start mysql` or `brew services start mysql`.

2. The application is pre-configured with default credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/medislot_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=admin
   ```

3. **Custom Credentials Option**:
   You can override credentials via environment variables without altering the codebase:
   ```powershell
   # Windows PowerShell:
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "your_mysql_password"
   ```
   ```bash
   # Linux / macOS Bash:
   export DB_USERNAME=root
   export DB_PASSWORD=your_mysql_password
   ```

4. **Automatic Schema & Data Population**:
   Upon first application startup, `DataInitializer.java` automatically:
   - Executes `schema.sql` to create all 10 relational tables, indexes, and constraints.
   - Seeds departments, fictional doctors, weekly schedules, demo patients, and admin accounts.
   - Batch-generates forward consultation slots for the next 14 days.
   - **No manual SQL scripts need to be imported.**

---

## 3. Building and Running the Application

### Option A: Running from the Terminal / Command Line
Navigate to the project root directory:
```bash
cd "g:\Krish CU\OOPs project"
```

Compile and run the Spring Boot application using Maven:
```bash
mvn spring-boot:run
```

Once started, the console displays:
```
:: Spring Boot ::                (v3.2.5)
...
INFO ... Tomcat started on port 8080 (http) with context path ''
INFO ... Started MediSlotApplication in 4.5 seconds
```

### Option B: Running from IntelliJ IDEA or Eclipse
1. Open IntelliJ IDEA → **File** → **Open...**
2. Select the `OOPs project` folder (IntelliJ will automatically detect `pom.xml`).
3. Allow Maven to import project dependencies.
4. Locate `src/main/java/com/medislot/MediSlotApplication.java`.
5. Right-click and choose **Run 'MediSlotApplication'** (or press Shift+F10).

---

## 4. Accessing the Application

Open any modern web browser and navigate to:
```
http://localhost:8080
```

You will be redirected to the sign-in page (`http://localhost:8080/login`).

Use any of the pre-configured accounts:
- **Admin**: `admin` / `admin123`
- **Doctor**: `dr.arjun` / `doctor123`
- **Receptionist**: `receptionist` / `recep123`
- **Patient**: `eleanor` / `patient123`

*(You can also simply click any of the 4 quick-fill demo buttons on the login card to auto-populate credentials instantly).*

---

## 5. Running the Automated Test Suite

To verify transactional integrity, duplicate booking prevention, and JDBC repository operations:

```bash
mvn test
```

Expected output:
```
[INFO] Running com.medislot.AppointmentServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.medislot.ConcurrencyBookingTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.medislot.TraditionalJdbcTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

---

## 6. Packaging for University ZIP Submission

To package the application into a standalone runnable JAR file:
```bash
mvn clean package -DskipTests
```
The deployable JAR will be generated in `target/medislot-1.0.0.jar`.

To run the JAR independently:
```bash
java -jar target/medislot-1.0.0.jar
```


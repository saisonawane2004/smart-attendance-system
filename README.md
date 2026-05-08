## Smart Attendance Pro

Professional attendance software with QR verification, location validation, role-based operations, and analytics.

### Tech Stack
- Java 17, Spring Boot 3.4, Spring Security (session-based)
- Thymeleaf + Bootstrap (responsive SSR UI)
- SQLite + Spring Data JPA/Hibernate
- ZXing for QR generation

### Complete Workflow (System Design Flow)
1. Admin logs in and configures classrooms, faculty, students, and subjects.
2. Faculty logs in, creates an attendance session for a subject, and gets a time-limited QR code.
3. Student logs in, scans/pastes QR token, shares live GPS location, and submits attendance.
4. Server validates: authentication, QR expiry, duplicate attendance, and geofence radius using Haversine formula.
5. Attendance is persisted instantly and appears in reporting dashboards.
6. Faculty and students can open daily/monthly/yearly reports, with leave and percentage calculations.

### Feature Coverage
- QR code based attendance with expiring token
- GPS location validation inside classroom radius
- Role-based login for Admin, Faculty, Student
- Real-time attendance save after validation
- Duplicate prevention and expired QR rejection
- Reports: daily, monthly, yearly, subject-wise, student-wise
- Leave analytics: sessions, presents, leaves, attendance percentage
- Secure authentication with BCrypt and session handling
- Responsive modern SaaS dashboard UI for mobile/web/desktop

### Seeded Demo Data
Default users:
- `admin@smart.com` / `password`
- `faculty@smart.com` / `password`
- `student@smart.com` / `password`
- `student2@smart.com` / `password`

Seed includes:
- 1 class, 1 faculty, 2 subjects, 2 students
- 12 historical attendance sessions
- attendance records across multiple days to test monthly/yearly/leave reports

### Run
```powershell
cd backend
mvn spring-boot:run
```

Open: [http://localhost:8080](http://localhost:8080)

### Professional Product Header
All dashboards are branded as **Smart Attendance Pro** with a unified premium UI and responsive layout.

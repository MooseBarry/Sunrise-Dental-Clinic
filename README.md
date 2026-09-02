# Sunrise Dental Clinic Management System

A secure Java web application for managing clinic patients, appointments, treatments, billing, staff access, notifications and management reporting.

## Technology

- Java 21
- Jakarta Servlet 6 and JSP/JSTL
- Apache Tomcat 10.1
- MySQL 8
- Maven
- JUnit 5
- BCrypt password hashing
- Jakarta Mail for optional patient email notifications

## Main features

- Four-role access control: Administrator, Receptionist, Dentist and Cashier
- Secure login, BCrypt passwords, session timeout, CSRF validation and security headers
- Patient registration, directory search and record updates
- Appointment scheduling with complete duration-overlap prevention
- Appointment search and controlled status transitions
- Treatment catalogue and fee administration
- Completed/unbilled appointment selection
- Invoice generation, partial/full payments, receipt history and printable invoices
- Internal staff notification centre
- Optional appointment, invoice and payment emails
- Date-range management reports and printable output
- Audit trail for important staff actions
- JSON database-health and dentist-availability endpoints
- Responsive, role-aware professional interface

## Local configuration

Copy:

```text
src/main/resources/application.properties.example
```

to:

```text
src/main/resources/application.properties
```

Enter the local database password. The real file is ignored by Git and must never be committed.

## Database setup

For a new database, run:

```text
database/schema.sql
database/seed-data.sql
```

For the existing development database that already contains the billing migration, run:

```text
database/migrations/004_add_professional_features.sql
```

The migration adds the Cashier role, notifications, audit history and demonstration staff accounts. The demonstration receptionist and cashier initially reuse the existing administrator demo password. Change them through Staff Management before non-development use.

## Optional email configuration

Email is disabled by default. Enable it in the private properties file or, preferably, through environment variables:

```text
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_smtp_username
MAIL_PASSWORD=your_app_password
MAIL_FROM=your_sender_address
MAIL_STARTTLS=true
```

Use an SMTP app password rather than a personal email password. The application continues normally if email is disabled or delivery fails; internal notifications are retained.

## Build and deploy

```bash
mvn clean test package
```

Deploy the exploded directory through SmartTomcat:

```text
target/sunrise-dental-clinic
```

Context path:

```text
/sunrise-dental-clinic
```

## JSON endpoints

Authenticated dentist availability check:

```text
GET /api/appointments/availability?dentistId=1&date=2026-09-10&startTime=09:00&durationMinutes=45
```

Database health check:

```text
GET /api/health/database
```

## Security note

All authorization is enforced on the server. Hiding a menu link is only a usability feature; the authorization filter separately blocks unauthorised direct URLs and POST actions.

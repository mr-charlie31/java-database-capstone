## Architecture Summary
The Smart Clinic Management System follows a three-tier architecture.
The presentation tier is a mix of Thymeleaf-rendered pages (Admin/Doctor
dashboards, served by MVC controllers) and static HTML/JS pages for
patients that call REST endpoints. The application tier is a Spring Boot
service that separates MVC controllers, REST controllers, and a shared
service layer implementing business rules such as booking conflict
checks. The data tier splits storage by data shape: structured entities
(patients, doctors, admin, appointments) live in MySQL via Spring Data
JPA, while flexible, document-style data (prescriptions) lives in
MongoDB via Spring Data MongoDB.

## Numbered Flow
1. User opens the app and picks a role (Admin, Doctor, or Patient) on the landing page.
2. The browser submits login credentials to a REST login endpoint.
3. The service layer validates credentials against MySQL and issues a signed JWT.
4. The browser redirects to `/{role}Dashboard/{token}`, handled by `DashboardController` (MVC).
5. `DashboardController` validates the token via `TokenService` and returns the correct Thymeleaf view.
6. Dashboard JS calls REST endpoints (e.g. `/api/doctors`, `/api/appointments`) with the JWT attached.
7. REST controllers delegate to service classes, which apply validation and business rules.
8. Services call JPA repositories for relational data or Mongo repositories for prescriptions.
9. Repositories query MySQL or MongoDB and return domain objects/documents.
10. Services map results to DTOs; controllers serialize them to JSON.
11. Frontend JS renders the JSON into the DOM (tables, cards, modals).

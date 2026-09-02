# Smart Clinic Management System (java-database-capstone)

Full-stack clinic management system: Spring Boot (MVC + REST), MySQL (JPA), MongoDB (Spring Data), Thymeleaf + vanilla JS frontend, JWT auth, Docker, and GitHub Actions CI.

## Quick start

1. Start MySQL and MongoDB locally (or via Docker).
2. Create the schema and load sample data:
   ```bash
   mysql -u root -p < sql/schema.sql
   mysql -u root -p < sql/sample_data.sql
   mysql -u root -p < sql/stored_procedures.sql
   mongosh cms_mongo sql/mongo_sample_data.js
   ```
3. Update `app/src/main/resources/application.properties` with your DB credentials and a real `jwt.secret`.
4. Run the app:
   ```bash
   cd app
   mvn spring-boot:run
   ```
5. Open http://localhost:8080/login

## Docker

```bash
cd app
docker build -t smart-clinic-backend .
docker run -p 8080:8080 smart-clinic-backend
```

## Project layout

See `smart-clinic-full-guide.md` in the project root for the complete build guide, or browse:
- `app/src/main/java/com/project/back_end/` — models, repositories, DTOs, services, controllers, MVC controller, security config
- `app/src/main/resources/templates/` — Thymeleaf views (login, admin, doctor, patient)
- `app/src/main/resources/static/` — CSS, and JS split into `config/`, `services/`, `components/`, `pages/`
- `sql/` — schema, sample data, stored procedures, Mongo sample data
- `.github/workflows/` — CI: backend compile, frontend lint, Dockerfile lint

## Documentation

- `schema-architecture.md` — architecture summary + request/response flow
- `schema-design.md` — MySQL table design + MongoDB collection design
- `user-stories.md` — user stories for Admin, Doctor, Patient
- `REFLECTION.md` — Module 6 reflection template

# 📚 Matching Guru Backend

Welcome to the **Matching Guru** backend!  
This is a Spring Boot application providing the API for the **Matching Guru** mentoring platform — enabling mentor–mentee matching, user management, programme management, analytics, and email notifications.

## 🚀 Tech Stack
- Java 17
- Spring Boot 3
- PostgreSQL
- Maven
- SendGrid (Email Notifications)
- JWT (Authentication & Authorization)
- Render (Deployment)
- Hibernate (JPA)
- Docker (Deployment containerization)

## 🏗️ Project Structure
| Layer          | Purpose                                                      |
| -------------- | ------------------------------------------------------------- |
| `controller`   | API endpoints (REST Controllers)                              |
| `service`      | Business logic                                                 |
| `repository`   | Database access (Spring Data JPA)                              |
| `entity`       | Domain models (User, Match, Programme, etc.)                   |
| `dto`          | Data Transfer Objects (API request/response payloads)          |
| `config`       | Spring Security, CORS, JWT configuration                       |
| `utils`        | Helpers (e.g., JWT filter, Email templates)                    |
| `algorithms`   | Matching algorithms (Gale–Shapley, Collaborative Filtering)    |

## 🛠️ Setup Instructions (Local)

### 1. Clone the repository
```bash
git clone https://github.com/your-username/matching-guru-backend.git
cd matching-guru-backend
```

### 2. Configure application.properties
Create a file at `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/matchingguru
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwt.secret=your_jwt_secret
app.jwt.expiration=86400000

# Email (SendGrid)
app.mail.from=no-reply@yourdomain.com
app.mail.fromName=Matching Guru
spring.sendgrid.api-key=your_sendgrid_api_key

# Other
server.port=8080
spring.jpa.show-sql=true
```

> 🛡️ **Important**: Never commit secrets or API keys to GitHub. Use environment variables in production.

### 3. Build the project
```bash
./mvnw clean install
```

### 4. Run the application
```bash
./mvnw spring-boot:run
```

API will be available at:
```
http://localhost:8080
```

## 🐳 Docker (Production Ready)

### Build Docker image
```bash
docker build -t matching-guru-backend .
```

### Run Docker container
```bash
docker run -p 8080:8080 matching-guru-backend
```

## 🌍 Environment Variables (Production)

When deploying to Render, Vercel, or any server: Set these environment variables:

| Key | Example Value |
|-----|---------------|
| DATABASE_URL | postgresql://user:pass@host/db |
| SENDGRID_API_KEY | SG.XXXXXXXXXXXXXXXXX |
| APP_JWT_SECRET | your_super_secret_key |
| APP_MAIL_FROM | no-reply@yourdomain.com |
| APP_MAIL_FROM_NAME | Matching Guru |

## 🔒 Security
- Authentication: JWT Tokens (Authorization: Bearer \<token\>)
- Authorization: Based on user roles (ROLE_USER, ROLE_ADMIN)
- CORS: Only whitelisted frontend origins are allowed

## ✅ Testing
Unit tests are written using:
- JUnit 5
- Mockito

Run tests locally with:
```bash
./mvnw test
```

## 📦 API Documentation

Basic endpoints include:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /auth/signup | User registration |
| POST | /auth/login | User login |
| GET | /participants/{id} | Fetch participant profile |
| POST | /matches/create | Create a mentor–mentee match |
| PATCH | /matches/update-status | Update match status |
| GET | /stats/organisation/{id} | Get analytics for an organisation |

📖 More endpoints are available in the controller package.

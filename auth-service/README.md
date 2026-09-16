# HRMS Auth Service

Auth Service runs on port `8081`. It owns login credentials and creates the single bootstrap
administrator. All other accounts are created through the protected provisioning API.

## Local prerequisites

1. Java 21
2. MySQL on port 3306
3. The MCP server started once so Hibernate creates the shared `employee` table

Auth Service and the Orchestrator must receive the same `JWT_SECRET`. Local defaults are aligned,
but set a strong environment value outside local development. Also override
`AUTH_SEED_ADMIN_PASSWORD`; `password123` is only the local-development default.

Build and run the executable service:

```powershell
.\mvnw.cmd -f auth-service/pom.xml package
java -jar auth-service/target/auth-service.jar
```

## Role hierarchy

- `ADMIN` can create `MANAGER`, `HR`, and `EMPLOYEE` accounts.
- `MANAGER` can create `HR` and `EMPLOYEE` accounts.
- `HR` can create `EMPLOYEE` accounts.
- `EMPLOYEE` cannot create accounts.

## API flow

Login with the bootstrap administrator:

```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin@gmail.com",
  "password": "password123"
}
```

Use the returned token to create a linked employee and login account in one operation:

```http
POST http://localhost:8081/api/auth/users

OpenAPI documentation:

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "manager@example.com",
  "password": "password123",
  "role": "MANAGER",
  "firstName": "Maya",
  "lastName": "Singh",
  "department": "Engineering",
  "designation": "Engineering Manager",
  "managerEmployeeId": null,
  "dateOfJoining": "2026-09-09"
}
```

`GET /api/auth/me` verifies a token and returns its current account-to-employee link. The legacy
`PATCH /api/auth/users/{userId}/link-employee/{employeeId}` endpoint is restricted to `ADMIN` and
is intended only for repairing old data.

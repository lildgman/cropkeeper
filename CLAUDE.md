# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CropKeeper is a Spring Boot 3.5.7 application for farmers, built with Java 17. The project uses:
- Spring MVC with Thymeleaf for server-side rendering
- Spring Data JPA with MySQL (H2 for testing)
- Spring Security with JWT authentication
- Lombok for boilerplate reduction
- Gradle for build automation

## Build and Run Commands

### Development
```bash
# Run the application (with auto-reload via DevTools)
./gradlew bootRun

# Build the project
./gradlew build

# Clean build artifacts
./gradlew clean

# Create executable JAR
./gradlew bootJar
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with verbose output
./gradlew test --info

# Run a single test class
./gradlew test --tests com.cropkeeper.YourTestClass

# Run a specific test method
./gradlew test --tests com.cropkeeper.YourTestClass.testMethodName
```

### Other Useful Commands
```bash
# View all available tasks
./gradlew tasks

# View project dependencies
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates
```

## Project Configuration

### Environment Variables
The application requires these environment variables (or can use defaults):
- `DB_URL` - Database connection URL (default: jdbc:mysql://localhost:3306/cropkeeper)
- `DB_USERNAME` - Database username (default: root)
- `DB_PASSWORD` - Database password (required)
- `JWT_SECRET_KEY` - Base64-encoded secret key for JWT (minimum 256 bits, required)
- `JWT_EXPIRATION_TIME` - JWT token expiration in milliseconds (default: 3600000)
- `DDL_AUTO` - Hibernate DDL auto mode (default: update)
- `MAX_FILE_SIZE` - Maximum file upload size (default: 5MB)
- `MAX_REQUEST_SIZE` - Maximum request size (default: 20MB)
- `FILE_UPLOAD_PATH` - File upload directory path (default: uploads/)

### Secret Configuration
Create `src/main/resources/application-secret.yml` based on `application-secret.yml.example`:
```yaml
spring:
  datasource:
    password: your_database_password_here

jwt:
  secret-key: your_base64_encoded_secret_key_here_minimum_256_bits
```

**IMPORTANT**: The file `application-secret.yml` is NOT currently in .gitignore. Add it to prevent committing secrets:
```bash
echo "" >> .gitignore
echo "### Secrets ###" >> .gitignore
echo "src/main/resources/application-secret.yml" >> .gitignore
```

### Testing Configuration
Tests use H2 in-memory database with a test-specific configuration in `src/test/resources/application.yml`. No secret configuration needed for tests.

## Architecture

### Package Structure
```
com.cropkeeper/
├── CropkeeperApplication.java  # Main Spring Boot application entry point
└── (additional packages to be created as needed)
```

### Standard Spring Boot Layered Architecture
Follow these conventions when adding code:
- `controller/` - REST controllers and MVC controllers (e.g., `UserController`)
- `service/` - Business logic layer (e.g., `UserService`)
- `repository/` - Spring Data JPA repositories (e.g., `UserRepository`)
- `model/` or `entity/` - JPA entities (e.g., `User`, `Crop`)
- `dto/` - Data Transfer Objects for API requests/responses
- `config/` - Spring configuration classes (Security, JPA, etc.)
- `security/` - Security-related classes (JWT filters, authentication providers)
- `exception/` - Custom exceptions and exception handlers
- `util/` - Utility classes

### Key Technologies & Patterns

**Database**:
- MySQL for production (configured via environment variables)
- H2 for testing (in-memory)
- JPA/Hibernate with entity relationships

**Security**:
- Spring Security with JWT authentication
- Tokens expire after 1 hour by default (configurable)
- Thymeleaf integration with Spring Security

**Views**:
- Thymeleaf templates in `src/main/resources/templates/`
- Static resources in `src/main/resources/static/`
- File uploads supported (max 5MB per file, 20MB per request by default)

**Development**:
- Spring Boot DevTools enabled for hot reload during development
- SQL logging enabled (`show-sql: true` and Hibernate SQL debug logging)

## Development Guidelines

### Database Schema Management
- Default DDL mode is `update` (Hibernate auto-updates schema)
- Use `create-drop` for fresh development
- Use `validate` for production to prevent auto-schema changes
- Consider Flyway or Liquibase for production schema migrations

### Security Best Practices
- Never commit `application-secret.yml` (add to .gitignore immediately)
- Use strong, randomly-generated JWT secret keys (minimum 256 bits)
- Validate all user inputs using Bean Validation (`@Valid`, `@NotNull`, etc.)
- Use parameterized queries (JPA does this by default)

### Lombok Usage
- Use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` on entities
- Use `@Slf4j` for logging
- Annotation processor is configured in build.gradle

### Testing
- Write unit tests for services using JUnit 5
- Use `@WebMvcTest` for controller tests
- Use `@DataJpaTest` for repository tests
- Use `@SpringBootTest` for integration tests
- Spring Security test support is available via `spring-security-test`

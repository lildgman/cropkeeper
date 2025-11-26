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
The project follows a domain-driven package structure:
```
com.cropkeeper/
├── CropkeeperApplication.java
├── domain/
│   ├── {domain}/              # e.g., farm, member, cultivation
│   │   ├── entity/            # JPA entities
│   │   ├── vo/                # Value Objects (embedded types)
│   │   ├── repository/        # Spring Data JPA repositories
│   │   ├── service/           # Business logic
│   │   ├── controller/        # REST/MVC controllers
│   │   ├── dto/               # Request/Response DTOs
│   │   ├── exception/         # Domain-specific exceptions
│   │   ├── annotation/        # Custom annotations (e.g., @ValidateFarmAccess)
│   │   └── aspect/            # AOP aspects for cross-cutting concerns
└── global/
    ├── common/                # Base entities (BaseTimeEntity)
    ├── exception/             # Global exception handling
    ├── security/              # JWT, UserPrincipal
    ├── aspect/                # Common AOP utilities
    └── logging/               # Global logging aspects
```

### Layered Architecture Pattern
Each domain follows this flow: **Controller → Service → Repository → Entity**

**Controller Layer:**
- Uses `@RestController` with `@RequestMapping("/api/{resource}")`
- Injects `@AuthenticationPrincipal UserPrincipal` for current user
- Returns `ResponseEntity` with appropriate status codes (201 for creation, 200 for success, 204 for delete)
- Uses `@Valid` for request validation
- Applies custom AOP annotations for access control (e.g., `@ValidateFarmAccess`)

**Service Layer:**
- Annotated with `@Service`, `@RequiredArgsConstructor`, `@Slf4j`
- Uses `@Transactional(readOnly = true)` at class level, `@Transactional` on write operations
- Uses `getReferenceById()` for FK relationships to avoid unnecessary DB queries (proxy pattern)
- Implements private helper methods like `findById()` for entity retrieval with exception handling

**Repository Layer:**
- Extends `JpaRepository<Entity, ID>`
- Custom query methods follow Spring Data JPA naming conventions
- Example: `findByMemberId()`, `findByFarmAndMetadata_LogDateBetween()`

**Entity Layer:**
- All entities extend `BaseTimeEntity` for audit fields (`createdAt`, `updatedAt`)
- Uses Lombok: `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@AllArgsConstructor`, `@Builder`
- Implements soft delete pattern with `deleted` (Boolean) and `deletedAt` (LocalDateTime) fields
- Includes convenience methods for updates (e.g., `updateFarmName()`, `delete()`)
- Uses `@Embedded` for Value Objects (e.g., `FarmingMetadata`, `Address`)

### Key Technologies & Patterns

**Database**:
- MySQL for production (configured via environment variables)
- H2 for testing (in-memory)
- JPA/Hibernate with entity relationships
- Soft delete pattern: entities include `deleted` and `deletedAt` fields instead of hard deletes

**Security**:
- Spring Security with JWT authentication
- Tokens expire after 1 hour by default (configurable)
- `UserPrincipal` contains authenticated user info (ID, username, role)
- Thymeleaf integration with Spring Security

**AOP Access Control Pattern**:
The project uses custom annotations with AOP for resource access validation:
1. **Custom Annotation**: Define annotation in `domain/{domain}/annotation/` (e.g., `@ValidateFarmAccess`)
2. **Aspect Implementation**: Create aspect in `domain/{domain}/aspect/` (e.g., `FarmAccessAspect`)
3. **Usage**: Apply annotation to controller methods with `@PathVariable Long {resource}Id` and `@AuthenticationPrincipal UserPrincipal`
4. **Validation Flow**:
   - Extract `{resource}Id` from `@PathVariable`
   - Extract `UserPrincipal` from method parameters
   - Retrieve resource entity from database
   - Verify authenticated user owns the resource
   - Throw `Forbidden{Resource}AccessException` if unauthorized

**Exception Handling Pattern**:
1. **ErrorCode Interface**: All error codes implement `global.exception.ErrorCode` (code, message, httpStatus)
2. **Domain ErrorCode Enum**: Each domain has an enum like `FarmErrorCode` implementing `ErrorCode`
3. **Custom Exceptions**: Extend `global.exception.BaseException` with specific error codes
4. **Global Handler**: `GlobalExceptionHandler` catches all exceptions and returns standardized `ErrorResponse`

**DTO Pattern**:
- **Request DTOs**: Include Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Size`, `@Min`)
- **Response DTOs**: Use static factory method `from(Entity entity)` for entity-to-DTO conversion
- **Builder Pattern**: All DTOs use Lombok `@Builder` for construction

**Value Objects (Embedded Types)**:
- Common data groups are extracted into `@Embeddable` classes in `domain/{domain}/vo/`
- Example: `FarmingMetadata` contains `logDate`, `weather`, `temperature`, `humidity`, `memo`
- Used across multiple log entities (CultivationLog, FertilizingLog, etc.)

**Views**:
- Thymeleaf templates in `src/main/resources/templates/`
- Static resources in `src/main/resources/static/`
- File uploads supported (max 5MB per file, 20MB per request by default)

**Development**:
- Spring Boot DevTools enabled for hot reload during development
- SQL logging enabled (`show-sql: true` and Hibernate SQL debug logging)

## Development Guidelines

### Adding a New Domain Feature
When implementing CRUD for a new domain (e.g., CultivationLog), follow this order:

1. **Entity & Repository** (usually already exists):
   - Extend `BaseTimeEntity`
   - Add soft delete fields if needed (`deleted`, `deletedAt`)
   - Create repository extending `JpaRepository<Entity, ID>`

2. **Exceptions**:
   - Create `{Domain}ErrorCode` enum implementing `ErrorCode`
   - Create domain-specific exceptions extending `BaseException`

3. **DTOs**:
   - Create request DTOs with Bean Validation annotations
   - Create response DTOs with `from(Entity entity)` factory method

4. **Service**:
   - Use `@Transactional(readOnly = true)` at class level
   - Use `@Transactional` on write operations
   - Use `getReferenceById()` for FK relationships (proxy pattern)
   - Create private `findById()` helper for entity retrieval

5. **Controller**:
   - Use appropriate HTTP methods (POST for create, PUT/PATCH for update, DELETE for delete)
   - Apply access control annotations (e.g., `@ValidateFarmAccess`)
   - Return proper status codes (201 for create, 200 for success, 204 for delete)

6. **AOP Access Control** (if ownership validation needed):
   - Create annotation in `domain/{domain}/annotation/`
   - Create aspect in `domain/{domain}/aspect/` using `AspectParameterExtractor` utility
   - Apply annotation to controller methods

7. **Tests**:
   - Service tests: `@SpringBootTest` with `@Transactional`
   - Repository tests: `@DataJpaTest` with H2
   - Controller tests: `@WebMvcTest` with mocked services

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
- Entities: `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@AllArgsConstructor`, `@Builder`
- Services/Controllers: `@RequiredArgsConstructor`, `@Slf4j`
- DTOs: `@Getter`, `@Builder`
- Annotation processor is configured in build.gradle

### Testing

**Service Layer Tests (Unit Testing):**
- Use `@ExtendWith(MockitoExtension.class)` for fast, isolated unit tests
- Mock dependencies with `@Mock` (repositories, other services)
- Inject mocks into service with `@InjectMocks`
- Use Mockito's `when()`, `verify()`, `times()`, `never()` for behavior verification
- Focus on testing business logic in isolation
- Example: `MemberServiceTest`, `AuthServiceTest`

**Repository Layer Tests (Integration Testing):**
- Use `@DataJpaTest` for repository-focused integration tests
- Automatically configures H2 in-memory database
- Use `TestEntityManager` for setup and assertions
- Tests run in transactions and rollback automatically
- Tests actual JPA queries, relationships, and database constraints
- Example: `MemberRepositoryTest`, `FarmRepositoryTest`

**Controller Layer Tests (Integration Testing):**
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` for full integration tests
- Use `@Transactional` to rollback after each test
- Inject `MockMvc` for HTTP request simulation
- Test entire request-response cycle including security, validation, and exception handling
- Use `@BeforeEach` to set up test data and authentication tokens
- Example: `AuthControllerTest`, `MemberControllerTest`

**Common Testing Practices:**
- Use AssertJ for assertions (`assertThat()`)
- Use `@DisplayName` for descriptive test names in Korean
- Use `MockMvc` methods: `perform()`, `andExpect()`, `andDo(print())`
- Test both success and failure scenarios comprehensively

# Spring Boot Development Best Practices

This guide provides comprehensive best practices for developing Spring Boot applications using our dependency management framework.

## Table of Contents

1. [Project Structure](#project-structure)
2. [Dependency Management](#dependency-management)
3. [Configuration Management](#configuration-management)
4. [REST API Development](#rest-api-development)
5. [Spring Batch Best Practices](#spring-batch-best-practices)
6. [Database & JPA](#database--jpa)
7. [Testing](#testing)
8. [Logging & Monitoring](#logging--monitoring)
9. [Security](#security)
10. [Error Handling](#error-handling)
11. [Performance](#performance)
12. [Code Quality](#code-quality)

---

## Project Structure

### Recommended Package Structure

```
com.yourcompany.projectname/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── service/             # Business logic
├── repository/          # Data access layer
├── domain/              # Domain entities
├── dto/                 # Data Transfer Objects
├── mapper/              # DTO <-> Entity mappers
├── exception/           # Custom exceptions
├── util/                # Utility classes
├── batch/               # Batch jobs (if using Spring Batch)
│   ├── job/
│   ├── step/
│   ├── reader/
│   ├── processor/
│   └── writer/
└── Application.java     # Main application class
```

### Module Organization

For multi-module projects:

```
project-root/
├── pom.xml                          # Parent POM
├── project-api/                     # REST API module
├── project-batch/                   # Batch processing module
├── project-common/                  # Shared code
├── project-domain/                  # Domain entities
└── project-integration/             # Integration tests
```

---

## Dependency Management

### ✅ DO's

1. **Inherit from dependency-parent:**
   ```xml
   <parent>
       <groupId>com.yourcompany</groupId>
       <artifactId>dependency-parent</artifactId>
       <version>1.0.0-SNAPSHOT</version>
   </parent>
   ```

2. **Never specify versions for managed dependencies:**
   ```xml
   <!-- Good -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
   </dependency>

   <!-- Bad -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
       <version>3.5.6</version> <!-- Don't do this! -->
   </dependency>
   ```

3. **Use starter dependencies:**
   ```xml
   <!-- Good - includes everything needed -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa</artifactId>
   </dependency>

   <!-- Bad - manual dependency management -->
   <dependency>
       <groupId>org.hibernate</groupId>
       <artifactId>hibernate-core</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework.data</groupId>
       <artifactId>spring-data-jpa</artifactId>
   </dependency>
   ```

4. **Keep dependencies minimal:**
   - Only add what you actually use
   - Regularly audit and remove unused dependencies

### ❌ DON'Ts

1. Don't mix versions from different Spring Boot releases
2. Don't exclude transitive dependencies unless absolutely necessary
3. Don't add dependencies for individual classes (use starters)
4. Don't copy dependencies from other projects without understanding them

---

## Configuration Management

### Environment-Specific Configuration

Use Spring profiles for environment-specific settings:

**application.yml:**
```yaml
spring:
  application:
    name: my-application

# Common configuration
server:
  port: 8080
```

**application-dev.yml:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb

logging:
  level:
    com.yourcompany: DEBUG
```

**application-prod.yml:**
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

logging:
  level:
    com.yourcompany: INFO
```

### Configuration Properties

Use type-safe configuration:

```java
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    @NotBlank
    private String name;

    @Min(1)
    @Max(100)
    private int maxConnections;

    private List<String> allowedOrigins;

    // Getters and setters
}

// Enable in configuration class
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
}
```

### Secrets Management

**Never hardcode secrets!**

```yaml
# Bad
spring:
  datasource:
    password: mySecretPassword123

# Good - use environment variables
spring:
  datasource:
    password: ${DB_PASSWORD}
```

---

## REST API Development

### Controller Best Practices

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable @Positive Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid CreateUserRequest request) {
        UserDTO user = userService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### DTOs and Validation

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$",
             message = "Password must contain letters and numbers")
    private String password;

    @Past
    private LocalDate dateOfBirth;
}
```

### API Versioning

Use URL versioning for clarity:

```java
@RequestMapping("/api/v1/...")  // Version 1
@RequestMapping("/api/v2/...")  // Version 2
```

### OpenAPI Documentation

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My Application API")
                        .version("1.0")
                        .description("API documentation for My Application")
                        .contact(new Contact()
                                .name("Platform Team")
                                .email("platform@yourcompany.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

---

## Spring Batch Best Practices

### Job Configuration

```java
@Configuration
@RequiredArgsConstructor
public class UserImportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job userImportJob(Step userImportStep) {
        return new JobBuilder("userImportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(userImportStep)
                .build();
    }

    @Bean
    public Step userImportStep(
            ItemReader<UserData> reader,
            ItemProcessor<UserData, User> processor,
            ItemWriter<User> writer) {
        return new StepBuilder("userImportStep", jobRepository)
                .<UserData, User>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(DataIntegrityViolationException.class)
                .retryLimit(3)
                .retry(DeadlockLoserDataAccessException.class)
                .listener(new StepExecutionListener() {
                    @Override
                    public void afterStep(StepExecution stepExecution) {
                        log.info("Step {} completed. Read: {}, Written: {}, Skipped: {}",
                                stepExecution.getStepName(),
                                stepExecution.getReadCount(),
                                stepExecution.getWriteCount(),
                                stepExecution.getSkipCount());
                    }
                })
                .build();
    }
}
```

### Chunk Size

- **Small datasets (< 1000 records)**: chunk size 10-50
- **Medium datasets (1000-100K)**: chunk size 100-500
- **Large datasets (> 100K)**: chunk size 500-1000
- **Very large datasets**: chunk size 1000-5000

### Partitioning for Parallel Processing

```java
@Bean
public Step partitionedStep(
        Partitioner partitioner,
        Step workerStep,
        TaskExecutor taskExecutor) {
    return new StepBuilder("partitionedStep", jobRepository)
            .partitioner("workerStep", partitioner)
            .step(workerStep)
            .gridSize(10)
            .taskExecutor(taskExecutor)
            .build();
}
```

### Monitoring Batch Jobs

Enable Spring Batch Actuator endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,batch
```

---

## Database & JPA

### Entity Best Practices

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                   generator = "user_seq")
    @SequenceGenerator(name = "user_seq",
                      sequenceName = "user_sequence",
                      allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version; // Optimistic locking
}
```

### Repository Patterns

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    Page<User> findByStatus(@Param("status") UserStatus status, Pageable pageable);

    // Use native query only when necessary
    @Query(value = "SELECT * FROM users WHERE LOWER(username) LIKE LOWER(:pattern)",
           nativeQuery = true)
    List<User> searchByUsername(@Param("pattern") String pattern);

    // Projections for performance
    @Query("SELECT new com.yourcompany.dto.UserSummary(u.id, u.username, u.email) " +
           "FROM User u WHERE u.status = :status")
    List<UserSummary> findSummariesByStatus(@Param("status") UserStatus status);
}
```

### Transaction Management

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public Optional<UserDTO> findById(Long id) {
        // Read-only transaction (inherited)
        return userRepository.findById(id)
                .map(userMapper::toDTO);
    }

    @Transactional // Write transaction
    public UserDTO create(CreateUserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        return userMapper.toDTO(saved);
    }
}
```

### Database Migration

Use Flyway or Liquibase for database migrations:

**V1__initial_schema.sql:**
```sql
CREATE SEQUENCE user_sequence START WITH 1 INCREMENT BY 50;

CREATE TABLE users (
    id BIGINT PRIMARY KEY DEFAULT nextval('user_sequence'),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);
```

---

## Testing

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .username(request.getUsername())
                .email(request.getEmail())
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserDTO result = userService.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
    }
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByUsername() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        // When
        Optional<User> result = userRepository.findByUsername("testuser");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }
}
```

### REST API Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateUser() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("Password123")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }
}
```

---

## Logging & Monitoring

### Logging Best Practices

```java
@Slf4j
@Service
public class UserService {

    public UserDTO findById(Long id) {
        log.debug("Finding user by id: {}", id);

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            log.warn("User not found with id: {}", id);
            return null;
        }

        log.info("User found: {}", user.get().getUsername());
        return userMapper.toDTO(user.get());
    }

    public UserDTO create(CreateUserRequest request) {
        log.info("Creating new user: {}", request.getUsername());

        try {
            // Business logic
            log.debug("User created successfully: {}", user.getId());
            return userMapper.toDTO(user);
        } catch (Exception e) {
            log.error("Failed to create user: {}", request.getUsername(), e);
            throw e;
        }
    }
}
```

### Actuator Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    tags:
      application: ${spring.application.name}
```

### Custom Metrics

```java
@Component
@RequiredArgsConstructor
public class UserMetrics {

    private final MeterRegistry meterRegistry;

    public void recordUserCreation() {
        meterRegistry.counter("users.created").increment();
    }

    public void recordUserLoginTime(Duration duration) {
        meterRegistry.timer("users.login.time").record(duration);
    }
}
```

---

## Security

### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults()));

        return http.build();
    }
}
```

### Method-Level Security

```java
@Service
public class UserService {

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    public UserDTO updateUser(String username, UpdateUserRequest request) {
        // Only the user themselves or an admin can update
    }
}
```

---

## Error Handling

### Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request parameters")
                .validationErrors(errors)
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneralError(Exception ex) {
        log.error("Unexpected error", ex);
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .build();
    }
}
```

---

## Performance

### Caching

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("users", "roles");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}

@Service
public class UserService {

    @Cacheable(value = "users", key = "#id")
    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElse(null);
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

### Async Processing

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class NotificationService {

    @Async
    public CompletableFuture<Void> sendEmailAsync(String email, String message) {
        // Send email
        return CompletableFuture.completedFuture(null);
    }
}
```

---

## Code Quality

### Code Review Checklist

- [ ] No hardcoded values (use configuration)
- [ ] Proper exception handling
- [ ] Input validation
- [ ] Logging at appropriate levels
- [ ] Unit tests with good coverage
- [ ] No security vulnerabilities
- [ ] Follow naming conventions
- [ ] Documentation for complex logic
- [ ] No code duplication
- [ ] Efficient database queries

### Static Code Analysis

Run regularly:

```bash
# Semgrep security scan
semgrep scan --config=auto

# Maven code quality checks
mvn clean verify -Pcoverage

# Check for dependency updates
mvn versions:display-dependency-updates
```

---

## Summary

Following these best practices will help you build:
- **Maintainable** applications with clear structure
- **Secure** applications with proper validation and authentication
- **Performant** applications with caching and optimization
- **Testable** code with high coverage
- **Production-ready** applications with proper monitoring

Remember: These are guidelines, not rigid rules. Adapt them to your specific needs while maintaining code quality and consistency.

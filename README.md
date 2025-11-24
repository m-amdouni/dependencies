# Dependencies Service

Spring Boot application with Docker support using Paketo buildpacks.

## Prerequisites

- Java 17 or higher (LTS)
- Maven 3.9+
- Docker Desktop or Docker Engine
- (Optional) Semgrep for code quality checks

## Technology Stack

### Core Framework
- **Spring Boot**: 3.5.8
- **Java**: 17 (LTS)
- **Maven**: 3.9+
- **Paketo Buildpacks**: Latest

### Validation & Utilities
- **Hibernate Validator**: 8.0.x (via spring-boot-starter-validation)
- **Lombok**: 1.18.40 (Spring Boot managed)
- **MapStruct**: 1.6.3
- **Apache Commons Lang3**: (Spring Boot managed)
- **Apache Commons Collections4**: (Spring Boot managed)
- **Apache Commons IO**: (Spring Boot managed)

### Code Quality Tools
- **SonarQube Maven Plugin**: 4.0.0.4121
- **Exec Maven Plugin**: 3.5.0

## Validation & Utility Libraries

This project includes comprehensive validation and utility libraries that are fully compatible with Spring Boot 3.5.8 and Java 17.

### Bean Validation with Hibernate Validator

The project uses **Jakarta Bean Validation 3.0** via `spring-boot-starter-validation`. All validation annotations use the `jakarta.validation.constraints` package.

**Example usage:**
```java
@Data
public class UserDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @Email(message = "Email should be valid")
    private String email;
}
```

See `UserDTO.java` for a complete example.

### Lombok - Code Generation

**Lombok 1.18.40** reduces boilerplate code with annotations like `@Data`, `@Builder`, `@Slf4j`, etc.

**Example usage:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
}
```

**Note**: The maven-compiler-plugin is configured with annotation processor paths for proper Lombok compilation.

### MapStruct - Object Mapping

**MapStruct 1.6.3** provides type-safe bean mapping between entities and DTOs.

**Example usage:**
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
    User toEntity(UserDTO userDTO);
}
```

See `UserMapper.java` for a complete example with update methods.

**Important**: The annotation processors are configured in the correct order:
1. Lombok (processed first)
2. MapStruct (processed after Lombok)
3. Lombok-MapStruct binding (ensures compatibility)

### Apache Commons Utilities

Three Apache Commons libraries are included:

**Commons Lang3** - String and Object utilities:
```java
StringUtils.isBlank(username)
StringUtils.capitalize(text)
```

**Commons Collections4** - Enhanced collection utilities:
```java
CollectionUtils.isEmpty(list)
CollectionUtils.union(list1, list2)
```

**Commons IO** - File and stream utilities:
```java
FileUtils.readFileToString(file, charset)
IOUtils.copy(input, output)
```

### Working Example

A complete working example is available in the `UserController` class:
- **Validation**: `@Valid` on request bodies
- **Lombok**: `@Slf4j`, `@RequiredArgsConstructor`
- **MapStruct**: Entity/DTO conversion
- **Apache Commons**: String manipulation

**Test the API:**
```bash
# Create a user (valid)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","bio":"Developer"}'

# Create a user (invalid email - will fail validation)
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"invalid","bio":"Developer"}'
```

## Building the Application

### Standard Build
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

## Docker Image Building

This project uses **Spring Boot Maven Plugin** with **Paketo buildpacks** to build OCI-compliant container images without requiring a Dockerfile.

### Local Development (docker-local profile)

Build Docker image for local development:

```bash
mvn clean package spring-boot:build-image -P docker-local
```

**Features:**
- Image name: `dependencies:local-0.0.1-SNAPSHOT`
- Includes debug agent on port 5005
- Not pushed to any registry
- Optimized for local development

**Run locally:**
```bash
# Using Docker directly
docker run -p 8080:8080 -p 5005:5005 dependencies:local-0.0.1-SNAPSHOT

# Using docker-compose
docker-compose up
```

Access the application:
- Application: http://localhost:8080
- Health endpoint: http://localhost:8080/actuator/health

### Corporate Deployment (docker-corp profile)

Build and push Docker image to corporate Artifactory:

```bash
mvn clean package spring-boot:build-image -P docker-corp \
  -Ddocker.registry.url=https://your-artifactory.company.com/docker \
  -Ddocker.registry.username=your-username \
  -Ddocker.registry.password=your-password
```

**Features:**
- Image name: `${docker.registry.url}/dependencies:0.0.1-SNAPSHOT`
- Automatically pushes to Artifactory
- Production-ready optimizations
- No debug agent

**Alternative: Using settings.xml**

Add to your `~/.m2/settings.xml`:

```xml
<settings>
  <profiles>
    <profile>
      <id>docker-corp</id>
      <properties>
        <docker.registry.url>https://your-artifactory.company.com/docker</docker.registry.url>
        <docker.registry.username>your-username</docker.registry.username>
        <docker.registry.password>your-password</docker.registry.password>
      </properties>
    </profile>
  </profiles>
</settings>
```

Then run:
```bash
mvn clean package spring-boot:build-image -P docker-corp
```

### Custom Image Tags

Override the image tag:

```bash
mvn clean package spring-boot:build-image -P docker-local \
  -Ddocker.image.tag=custom-tag
```

## Code Quality Analysis

### Using the code-quality Profile

**Yes, it's a good practice** to have a dedicated Maven profile for code quality tools like SonarQube and Semgrep:

**Benefits:**
- ✅ **Separation of concerns** - Quality checks don't slow down normal builds
- ✅ **CI/CD flexibility** - Enable only in specific pipeline stages
- ✅ **Optional dependencies** - Developers don't need Sonar/Semgrep installed locally
- ✅ **Performance** - Faster local builds when not needed
- ✅ **Configurability** - Easy to add/remove tools without affecting main build

### SonarQube Analysis

```bash
# Run SonarQube analysis
mvn clean verify sonar:sonar -P code-quality \
  -Dsonar.host.url=http://your-sonar-server:9000 \
  -Dsonar.login=your-sonar-token
```

### Semgrep Security Scan

```bash
# Install Semgrep (one-time setup)
pip install semgrep

# Run code quality checks including Semgrep
mvn clean verify -P code-quality
```

The Semgrep report will be generated at: `target/semgrep-report.json`

### Combined Quality Check

Run both SonarQube and Semgrep:

```bash
mvn clean verify sonar:sonar -P code-quality \
  -Dsonar.host.url=http://your-sonar-server:9000 \
  -Dsonar.login=your-sonar-token
```

### Skip Code Quality Checks

```bash
mvn clean package -DskipTests
```

## Paketo Buildpack Configuration

The project uses the following Paketo buildpack features:

- **Builder**: `paketobuildpacks/builder-jammy-base:latest`
- **Java Buildpack**: `gcr.io/paketo-buildpacks/java`
- **JVM Version**: Java 17 (configurable via BP_JVM_VERSION)
- **Memory Settings**: Uses 75% of container memory
- **Container Support**: Optimized for containerized environments

### Customizing Buildpack Behavior

You can customize buildpack behavior using environment variables in `pom.xml`:

```xml
<env>
  <BP_JVM_VERSION>17</BP_JVM_VERSION>
  <BPE_APPEND_JAVA_TOOL_OPTIONS>-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0</BPE_APPEND_JAVA_TOOL_OPTIONS>
</env>
```

**Note**: You can specify a different Java version (e.g., 21) by changing the `BP_JVM_VERSION` environment variable.

## CI/CD Pipeline Example

### GitHub Actions / GitLab CI

```yaml
# Build and test
- mvn clean verify

# Code quality check
- mvn verify -P code-quality

# Build Docker image for development
- mvn spring-boot:build-image -P docker-local

# Build and push to Artifactory (production)
- mvn spring-boot:build-image -P docker-corp
```

## Maven Profiles Summary

| Profile | Purpose | Usage | Publish |
|---------|---------|-------|---------|
| **(default)** | Standard build | `mvn clean package` | No |
| `docker-local` | Local Docker image | `mvn spring-boot:build-image -P docker-local` | No |
| `docker-corp` | Artifactory deployment | `mvn spring-boot:build-image -P docker-corp` | Yes |
| `code-quality` | SonarQube & Semgrep | `mvn verify -P code-quality` | No |

## Troubleshooting

### Docker Build Issues

If you encounter Docker build issues:

```bash
# Ensure Docker daemon is running
docker info

# Clean Maven cache
mvn clean

# Rebuild with verbose output
mvn spring-boot:build-image -P docker-local -X
```

### Paketo Buildpack Cache

Clear buildpack cache:

```bash
docker builder prune
```

## Project Structure

```
dependencies/
├── src/
│   ├── main/
│   │   ├── java/com/example/dependencies/
│   │   │   └── DependenciesApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/dependencies/
│           └── DependenciesApplicationTests.java
├── pom.xml
├── docker-compose.yml
└── README.md
```

## License

This project is licensed under the MIT License.
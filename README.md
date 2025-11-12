# Spring Boot Dependency Management

A centralized dependency management repository for Spring Boot 3.X projects, providing consistent versioning, build configuration, and best practices across all microservices and applications.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Module Structure](#module-structure)
- [Usage Guide](#usage-guide)
- [Deployment](#deployment)
- [Security Scanning](#security-scanning)
- [Best Practices](#best-practices)
- [Contributing](#contributing)
- [Version History](#version-history)

## 🎯 Overview

This repository provides:

- **Dependency BOM (Bill of Materials)**: Centralized version management for all dependencies
- **Parent POM**: Build configuration, plugin management, and standard profiles
- **Artifactory Integration**: Ready-to-use deployment configuration
- **Security Scanning**: Semgrep integration for static code analysis
- **Best Practices**: Comprehensive documentation and guidelines

### Why Separate BOM and Parent?

Following Spring Boot's own pattern, we separate concerns:

1. **BOM** (`dependency-bom`):
   - Contains ONLY dependency versions
   - Can be imported without inheriting build configuration
   - Provides maximum flexibility for consuming projects

2. **Parent POM** (`dependency-parent`):
   - Contains build plugins and configuration
   - Imports the BOM for dependency versions
   - Projects inherit from this for complete setup

This separation allows projects to:
- Import just the BOM if they need custom build configuration
- Inherit the parent for a complete, standardized setup

## 🏗️ Architecture

```
dependency-management-root/
├── pom.xml                          # Reactor POM
├── dependency-bom/                  # Bill of Materials
│   └── pom.xml                      # Dependency versions only
├── dependency-parent/               # Parent POM
│   └── pom.xml                      # Build config + BOM import
├── .mvn/                            # Maven configuration
│   ├── settings-template.xml        # Artifactory settings template
│   └── artifactory-config.md        # Deployment documentation
├── .semgrep/                        # Security scanning
│   ├── semgrep.yml                  # Custom security rules
│   └── README.md                    # Semgrep documentation
└── docs/                            # Additional documentation
    └── BEST_PRACTICES.md            # Development best practices
```

## 🚀 Quick Start

### For Consuming Projects

#### Option 1: Use Parent POM (Recommended)

Inherit complete configuration including BOM and build plugins:

```xml
<parent>
    <groupId>com.yourcompany</groupId>
    <artifactId>dependency-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
```

#### Option 2: Import BOM Only

Use only dependency versions without build configuration:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.yourcompany</groupId>
            <artifactId>dependency-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### For Development

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd dependencies
   ```

2. **Build locally:**
   ```bash
   mvn clean install
   ```

3. **Deploy to Artifactory:**
   ```bash
   mvn clean deploy
   ```

## 📦 Module Structure

### dependency-bom

The Bill of Materials module manages versions for:

- **Spring Boot 3.5.6**: Latest stable Spring Boot 3.X release
- **Spring Cloud 2024.0.0**: Cloud-native components
- **REST API**: Web, WebFlux, Validation, OpenAPI/Swagger
- **Spring Batch**: Batch processing framework
- **Data Access**: JPA, Redis, PostgreSQL, MySQL, H2
- **Monitoring**: Actuator, Micrometer, Prometheus
- **Security**: Spring Security, JWT
- **Testing**: JUnit 5, Mockito, AssertJ, Testcontainers, REST Assured
- **Utilities**: Lombok, MapStruct, Commons, Guava
- **Resilience**: Resilience4j
- **Caching**: Caffeine
- **And more...**

**Key Features:**
- No build configuration (pure version management)
- Imports Spring Boot and Spring Cloud BOMs
- Comprehensive coverage of enterprise dependencies
- Regular updates aligned with Spring Boot releases

### dependency-parent

The Parent POM provides:

- **Build Configuration**: Compiler settings, resource handling
- **Plugin Management**: Maven plugins with optimal configuration
- **Testing Setup**: Unit and integration test configuration
- **Code Quality**: JaCoCo code coverage, enforcer rules
- **Containerization**: Jib plugin for Docker images
- **CI/CD Ready**: Git commit ID, build info generation
- **Multiple Profiles**: dev, test, prod, security-scan, etc.

**Key Features:**
- Imports dependency-bom for version management
- Annotation processor paths (Lombok, MapStruct)
- Separate unit and integration test execution
- 80% code coverage enforcement (configurable)
- Layer-enabled Spring Boot images
- Multi-environment support

## 📚 Usage Guide

### Adding Dependencies

When using the parent or importing the BOM, you don't need to specify versions:

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <!-- No version needed! -->
    </dependency>

    <!-- Spring Boot Starter Batch -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
    </dependency>

    <!-- Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Using Profiles

#### Development Profile

```bash
mvn spring-boot:run -Pdev
```

#### Production Build

```bash
mvn clean package -Pprod
```

#### Skip Tests

```bash
mvn clean package -Pskip-tests
```

#### Security Scan

```bash
mvn clean verify -Psecurity-scan
```

#### Code Coverage Report

```bash
mvn clean verify -Pcoverage
```

### Building Docker Images

Using Jib (no Docker daemon required):

```bash
# Build to local Docker daemon
mvn compile jib:dockerBuild

# Build and push to registry
mvn compile jib:build -Dimage=registry.example.com/myapp:1.0.0
```

Using Spring Boot:

```bash
# Build OCI image
mvn spring-boot:build-image
```

### Running Tests

```bash
# Unit tests only
mvn test

# Integration tests only
mvn verify -Dskip.unit.tests=true

# All tests
mvn verify

# Skip all tests
mvn package -DskipTests
```

## 🚢 Deployment

### Artifactory Configuration

See [Artifactory Configuration Guide](.mvn/artifactory-config.md) for detailed instructions.

**Quick Setup:**

1. Configure Maven settings:
   ```bash
   cp .mvn/settings-template.xml ~/.m2/settings.xml
   ```

2. Set environment variables:
   ```bash
   export ARTIFACTORY_URL="https://artifactory.yourcompany.com/artifactory"
   export ARTIFACTORY_USERNAME="your-username"
   export ARTIFACTORY_PASSWORD="your-api-token"
   ```

3. Deploy:
   ```bash
   mvn clean deploy
   ```

### Version Management

#### SNAPSHOT Development

```bash
# Current version: 1.0.0-SNAPSHOT
mvn clean deploy  # Deploys to snapshots repository
```

#### Release Process

```bash
# Update to release version
mvn versions:set -DnewVersion=1.0.0
mvn versions:commit

# Deploy release
mvn clean deploy

# Update to next snapshot
mvn versions:set -DnewVersion=1.1.0-SNAPSHOT
mvn versions:commit
```

## 🔒 Security Scanning

### Semgrep Integration

This project includes comprehensive Semgrep configuration for security scanning.

See [Semgrep Documentation](.semgrep/README.md) for complete details.

**Quick Start:**

```bash
# Install Semgrep
pip install semgrep

# Run security scan
semgrep scan --config=auto

# Run custom rules
semgrep scan --config=.semgrep/semgrep.yml

# Generate SARIF report
semgrep scan --config=auto --sarif -o semgrep-report.sarif
```

**CI/CD Integration:**

The parent POM includes a `security-scan` profile that runs Semgrep:

```bash
mvn clean verify -Psecurity-scan
```

### Security Best Practices

- ✅ Use parameterized queries (prevent SQL injection)
- ✅ Validate all user input (prevent injection attacks)
- ✅ Use strong cryptography (SHA-256+, AES-256)
- ✅ Never hardcode secrets (use environment variables)
- ✅ Enable CSRF protection
- ✅ Use HTTPS everywhere
- ✅ Keep dependencies up to date
- ✅ Regular security scans

## 🎯 Best Practices

See [Best Practices Guide](docs/BEST_PRACTICES.md) for comprehensive guidelines.

### Dependency Management

1. **Never specify versions** in consuming projects
2. **Update this repository** to change dependency versions globally
3. **Test thoroughly** before releasing new versions
4. **Document breaking changes** in release notes

### Build Configuration

1. **Inherit from parent** for consistent builds
2. **Use profiles** for environment-specific configuration
3. **Enable code coverage** (minimum 80%)
4. **Run security scans** regularly
5. **Keep plugins updated**

### Testing Standards

1. **Unit tests**: `*Test.java` in `src/test/java`
2. **Integration tests**: `*IT.java` or `*IntegrationTest.java`
3. **Use Testcontainers** for database tests
4. **Minimum 80% coverage** for production code
5. **Mock external dependencies**

### Logging

1. **Use parameterized logging**: `log.info("User {}", userId)`
2. **Never log sensitive data**: passwords, tokens, secrets
3. **Use appropriate levels**: ERROR, WARN, INFO, DEBUG, TRACE
4. **Include correlation IDs** for distributed tracing

### API Development

1. **Use OpenAPI/Swagger** for documentation
2. **Version your APIs**: `/api/v1/...`
3. **Implement proper error handling**
4. **Use DTOs** for request/response
5. **Validate input** with `@Valid` and Jakarta Validation

### Batch Processing

1. **Use chunk-oriented processing** for large datasets
2. **Implement retry logic** with Spring Retry
3. **Monitor job execution** with Actuator
4. **Use proper transaction management**
5. **Handle failures gracefully**

## 🤝 Contributing

### Updating Dependency Versions

1. Create a feature branch
2. Update versions in `dependency-bom/pom.xml`
3. Test with existing projects
4. Update changelog
5. Create pull request

### Adding New Dependencies

1. Research the dependency (security, license, maintenance)
2. Add to appropriate section in BOM
3. Document the purpose
4. Update this README
5. Create pull request

### Updating Build Configuration

1. Test changes locally
2. Verify with existing projects
3. Document breaking changes
4. Update migration guide if needed
5. Create pull request

## 📊 Version History

### 1.0.0-SNAPSHOT (Current)

- Initial release
- Spring Boot 3.5.6
- Spring Cloud 2024.0.0
- Comprehensive dependency coverage
- Artifactory integration
- Semgrep security scanning
- Complete documentation

## 📄 License

[Specify your license here]

## 📞 Support

For questions or issues:

1. Check the documentation in `docs/`
2. Review [Best Practices Guide](docs/BEST_PRACTICES.md)
3. Check [Artifactory Guide](.mvn/artifactory-config.md)
4. Check [Semgrep Guide](.semgrep/README.md)
5. Contact the platform team

## 🔗 References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Maven Documentation](https://maven.apache.org/guides/)
- [JFrog Artifactory](https://www.jfrog.com/confluence/display/JFROG/JFrog+Artifactory)
- [Semgrep Documentation](https://semgrep.dev/docs/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)

---

**Maintained by**: Platform Team
**Last Updated**: 2025-11-11
**Spring Boot Version**: 3.5.6
# Example Usage

This guide provides practical examples of how to use the dependency management POMs in your Spring Boot projects.

## Table of Contents

1. [Creating a REST API Project](#creating-a-rest-api-project)
2. [Creating a Spring Batch Project](#creating-a-spring-batch-project)
3. [Creating a Multi-Module Project](#creating-a-multi-module-project)
4. [Migrating Existing Projects](#migrating-existing-projects)

---

## Creating a REST API Project

### Step 1: Create Project Structure

```bash
mkdir my-rest-api
cd my-rest-api
```

### Step 2: Create pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Inherit from dependency-parent -->
    <parent>
        <groupId>com.yourcompany</groupId>
        <artifactId>dependency-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <groupId>com.yourcompany</groupId>
    <artifactId>my-rest-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>My REST API</name>
    <description>Example REST API using dependency-parent</description>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Actuator for monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>

        <!-- OpenAPI Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Plugin (inherited but needs to be declared) -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

### Step 3: Application Configuration

**application.yml:**

```yaml
spring:
  application:
    name: my-rest-api

  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/mydb}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:postgres}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### Step 4: Main Application Class

```java
package com.yourcompany.myrestapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MyRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyRestApiApplication.class, args);
    }
}
```

### Step 5: Build and Run

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Run with profile
mvn spring-boot:run -Pdev

# Build Docker image
mvn spring-boot:build-image
```

---

## Creating a Spring Batch Project

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.yourcompany</groupId>
        <artifactId>dependency-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <groupId>com.yourcompany</groupId>
    <artifactId>my-batch-job</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>My Batch Job</name>
    <description>Example Spring Batch project</description>

    <dependencies>
        <!-- Spring Batch -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-batch</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
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

        <dependency>
            <groupId>org.springframework.batch</groupId>
            <artifactId>spring-batch-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

### Batch Job Configuration

```java
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class DataImportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    @Bean
    public Job dataImportJob() {
        return new JobBuilder("dataImportJob", jobRepository)
                .start(importStep())
                .build();
    }

    @Bean
    public Step importStep() {
        return new StepBuilder("importStep", jobRepository)
                .<InputData, OutputData>chunk(100, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<InputData> itemReader() {
        return new FlatFileItemReaderBuilder<InputData>()
                .name("inputDataReader")
                .resource(new ClassPathResource("data/input.csv"))
                .delimited()
                .names("field1", "field2", "field3")
                .targetType(InputData.class)
                .build();
    }

    @Bean
    public ItemProcessor<InputData, OutputData> itemProcessor() {
        return item -> {
            // Process item
            return OutputData.builder()
                    .processedField(item.getField1().toUpperCase())
                    .build();
        };
    }

    @Bean
    public JpaItemWriter<OutputData> itemWriter() {
        JpaItemWriter<OutputData> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
```

---

## Creating a Multi-Module Project

### Root pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.yourcompany</groupId>
        <artifactId>dependency-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <groupId>com.yourcompany</groupId>
    <artifactId>my-application</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>My Application</name>
    <description>Multi-module application</description>

    <modules>
        <module>my-application-domain</module>
        <module>my-application-api</module>
        <module>my-application-batch</module>
        <module>my-application-common</module>
    </modules>

</project>
```

### Domain Module (my-application-domain/pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.yourcompany</groupId>
        <artifactId>my-application</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>my-application-domain</artifactId>
    <packaging>jar</packaging>

    <name>My Application - Domain</name>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>

</project>
```

### API Module (my-application-api/pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.yourcompany</groupId>
        <artifactId>my-application</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>my-application-api</artifactId>
    <packaging>jar</packaging>

    <name>My Application - API</name>

    <dependencies>
        <!-- Internal dependencies -->
        <dependency>
            <groupId>com.yourcompany</groupId>
            <artifactId>my-application-domain</artifactId>
            <version>${project.version}</version>
        </dependency>

        <dependency>
            <groupId>com.yourcompany</groupId>
            <artifactId>my-application-common</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

---

## Migrating Existing Projects

### Step 1: Backup Current pom.xml

```bash
cp pom.xml pom.xml.backup
```

### Step 2: Update Parent

Replace your current parent with dependency-parent:

```xml
<!-- Old -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.6</version>
</parent>

<!-- New -->
<parent>
    <groupId>com.yourcompany</groupId>
    <artifactId>dependency-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
```

### Step 3: Remove Explicit Versions

Remove all version tags from managed dependencies:

```xml
<!-- Before -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.5.6</version>
</dependency>

<!-- After -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### Step 4: Remove Property Overrides

Remove properties that are already defined in the parent:

```xml
<!-- Remove these if they match parent values -->
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

### Step 5: Test the Migration

```bash
# Clean and rebuild
mvn clean install

# Run tests
mvn test

# Verify no dependency issues
mvn dependency:tree
```

### Step 6: Update CI/CD

Update your CI/CD pipelines to use the new parent POM and profiles:

```yaml
# GitHub Actions example
- name: Build with Maven
  run: mvn clean verify -Pprod

# Include security scan
- name: Security Scan
  run: mvn verify -Psecurity-scan
```

---

## Alternative: Import BOM Only

If you need custom build configuration but want consistent dependency versions:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- No parent - using your own build configuration -->

    <groupId>com.yourcompany</groupId>
    <artifactId>my-custom-project</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <dependencyManagement>
        <dependencies>
            <!-- Import BOM only -->
            <dependency>
                <groupId>com.yourcompany</groupId>
                <artifactId>dependency-bom</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Dependencies without versions -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <build>
        <!-- Your custom build configuration -->
    </build>

</project>
```

---

## Troubleshooting

### Issue: Version Conflicts

```bash
# Check dependency tree
mvn dependency:tree

# Check for conflicts
mvn dependency:tree -Dverbose
```

### Issue: Plugin Not Found

Ensure you've declared the plugin in the build section:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

### Issue: Properties Not Resolved

Check that properties are defined in the parent or your project:

```bash
# Display effective POM
mvn help:effective-pom
```

---

## Next Steps

1. Review the [Best Practices Guide](BEST_PRACTICES.md)
2. Configure [Artifactory deployment](../.mvn/artifactory-config.md)
3. Set up [Semgrep security scanning](../.semgrep/README.md)
4. Enable CI/CD integration

For more information, refer to the main [README](../README.md).

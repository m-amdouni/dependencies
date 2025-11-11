# Artifactory Configuration Guide

## Overview

This project is configured to deploy artifacts to JFrog Artifactory. This guide explains how to configure Maven and CI/CD pipelines for artifact deployment.

## Maven Configuration

### 1. Local Development Setup

For local development and deployment, configure your Maven settings:

1. **Copy the settings template:**
   ```bash
   cp .mvn/settings-template.xml ~/.m2/settings.xml
   ```

2. **Set environment variables:**
   ```bash
   export ARTIFACTORY_URL="https://artifactory.yourcompany.com/artifactory"
   export ARTIFACTORY_USERNAME="your-username"
   export ARTIFACTORY_PASSWORD="your-api-token"  # Use API token, not password
   ```

3. **Or edit settings.xml directly:**
   Replace the placeholders in `~/.m2/settings.xml` with actual values.

### 2. Generating Artifactory API Token

**Recommended**: Use API tokens instead of passwords for better security.

1. Log in to Artifactory web UI
2. Click on your username (top right) → "Edit Profile"
3. Generate API Key
4. Use this API key as your password in Maven settings

### 3. Deploying Artifacts

Deploy the dependency management POMs to Artifactory:

```bash
# Deploy all modules (from root)
mvn clean deploy

# Deploy only the BOM
cd dependency-bom
mvn clean deploy

# Deploy only the Parent POM
cd dependency-parent
mvn clean deploy
```

### 4. Configuring Project POM

Update the root `pom.xml` with your Artifactory URLs:

```xml
<properties>
    <artifactory.url>https://artifactory.yourcompany.com/artifactory</artifactory.url>
</properties>

<distributionManagement>
    <repository>
        <id>artifactory-releases</id>
        <name>Artifactory Release Repository</name>
        <url>${artifactory.url}/libs-release-local</url>
    </repository>
    <snapshotRepository>
        <id>artifactory-snapshots</id>
        <name>Artifactory Snapshot Repository</name>
        <url>${artifactory.url}/libs-snapshot-local</url>
    </snapshotRepository>
</distributionManagement>
```

## CI/CD Integration

### GitHub Actions Example

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Artifactory

on:
  push:
    branches: [main, release/*]
  release:
    types: [created]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'

      - name: Configure Maven settings
        uses: whelk-io/maven-settings-xml-action@v21
        with:
          servers: |
            [
              {
                "id": "artifactory-releases",
                "username": "${{ secrets.ARTIFACTORY_USERNAME }}",
                "password": "${{ secrets.ARTIFACTORY_PASSWORD }}"
              },
              {
                "id": "artifactory-snapshots",
                "username": "${{ secrets.ARTIFACTORY_USERNAME }}",
                "password": "${{ secrets.ARTIFACTORY_PASSWORD }}"
              }
            ]

      - name: Deploy to Artifactory
        run: mvn clean deploy -DskipTests
        env:
          ARTIFACTORY_URL: ${{ secrets.ARTIFACTORY_URL }}
```

### GitLab CI Example

Create `.gitlab-ci.yml`:

```yaml
variables:
  MAVEN_CLI_OPTS: "-s .mvn/settings.xml --batch-mode"
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"

cache:
  paths:
    - .m2/repository/

stages:
  - build
  - test
  - deploy

build:
  stage: build
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn $MAVEN_CLI_OPTS clean compile

test:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn $MAVEN_CLI_OPTS test

deploy:
  stage: deploy
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn $MAVEN_CLI_OPTS deploy -DskipTests
  only:
    - main
    - tags
  environment:
    name: artifactory
```

### Jenkins Pipeline Example

Create `Jenkinsfile`:

```groovy
pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk 'JDK 17'
    }

    environment {
        ARTIFACTORY_CREDS = credentials('artifactory-credentials')
        ARTIFACTORY_URL = 'https://artifactory.yourcompany.com/artifactory'
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Deploy to Artifactory') {
            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                }
            }
            steps {
                configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS deploy -DskipTests'
                }
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            jacoco()
        }
    }
}
```

## Repository Configuration in Artifactory

### Recommended Repository Structure

1. **libs-release-local**: For release versions (1.0.0, 2.0.1, etc.)
   - Deployment policy: Disable redeploy
   - Snapshot version behavior: Non-unique

2. **libs-snapshot-local**: For snapshot versions (1.0.0-SNAPSHOT)
   - Deployment policy: Allow redeploy
   - Snapshot version behavior: Unique

3. **libs-release**: Virtual repository aggregating all release repositories
4. **libs-snapshot**: Virtual repository aggregating all snapshot repositories
5. **maven-virtual**: Virtual repository for all Maven repositories

### Permissions

Ensure your Artifactory user has:
- **Deploy/Cache** permission for deployment
- **Read** permission for downloading dependencies
- **Delete/Overwrite** permission for snapshots (optional)

## Versioning Strategy

### Semantic Versioning

Follow semantic versioning (MAJOR.MINOR.PATCH):
- **MAJOR**: Incompatible API changes
- **MINOR**: Backward-compatible functionality additions
- **PATCH**: Backward-compatible bug fixes

### Release Process

1. **Development**: Use SNAPSHOT versions (e.g., `1.0.0-SNAPSHOT`)
2. **Release Candidate**: Use RC versions (e.g., `1.0.0-RC1`)
3. **Release**: Use final versions (e.g., `1.0.0`)

### Example Release Commands

```bash
# Update version to release
mvn versions:set -DnewVersion=1.0.0
mvn versions:commit

# Deploy release
mvn clean deploy

# Update to next snapshot version
mvn versions:set -DnewVersion=1.1.0-SNAPSHOT
mvn versions:commit
```

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Verify credentials in settings.xml
   - Check API token is valid
   - Ensure server ID matches distributionManagement ID

2. **403 Forbidden**
   - Verify user has deploy permissions
   - Check repository allows deployment
   - For releases, ensure version doesn't already exist

3. **Cannot connect to Artifactory**
   - Verify URL is correct
   - Check network connectivity
   - Verify SSL certificates if using HTTPS

### Debug Deployment

Enable debug logging:

```bash
mvn deploy -X
```

## Security Best Practices

1. **Never commit credentials** to version control
2. **Use API tokens** instead of passwords
3. **Rotate tokens regularly** (every 90 days)
4. **Use encrypted passwords** in Maven settings
5. **Limit token permissions** to only what's needed
6. **Use different tokens** for different environments
7. **Store secrets** in CI/CD secret management systems

## Cleanup Policies

Configure Artifactory cleanup policies to:
- Remove old snapshots (e.g., keep last 10 versions)
- Archive old releases
- Save storage space

Example policy:
- **Snapshots**: Keep artifacts from last 30 days or last 10 builds
- **Releases**: Keep all releases or apply custom retention

## Monitoring and Notifications

Set up notifications in Artifactory for:
- Failed deployments
- Storage quota warnings
- Unusual download patterns
- Security vulnerabilities in dependencies

## References

- [Maven Deploy Plugin](https://maven.apache.org/plugins/maven-deploy-plugin/)
- [JFrog Artifactory Documentation](https://www.jfrog.com/confluence/display/JFROG/JFrog+Artifactory)
- [Maven Settings Reference](https://maven.apache.org/settings.html)
- [Maven Password Encryption](https://maven.apache.org/guides/mini/guide-encryption.html)

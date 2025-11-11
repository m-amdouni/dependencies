# Semgrep Security Scanning Configuration

## Overview

Semgrep is a static analysis tool for finding bugs and enforcing code standards. This directory contains custom security rules and configuration for scanning Spring Boot projects.

## Installation

### Local Installation

```bash
# Using pip
pip install semgrep

# Using Homebrew (macOS)
brew install semgrep

# Using Docker
docker pull semgrep/semgrep
```

### Verify Installation

```bash
semgrep --version
```

## Running Semgrep Locally

### Basic Scan

Scan the entire project:

```bash
# From project root
semgrep scan --config=.semgrep/semgrep.yml

# Using auto configuration (recommended)
semgrep scan --config=auto

# Using specific rulesets
semgrep scan --config=p/security-audit
semgrep scan --config=p/owasp-top-ten
semgrep scan --config=p/java
```

### Advanced Scanning

```bash
# Scan with specific severity level
semgrep scan --config=auto --severity=ERROR

# Generate SARIF report for GitHub
semgrep scan --config=auto --sarif -o semgrep-report.sarif

# Generate JSON report
semgrep scan --config=auto --json -o semgrep-report.json

# Scan specific directories
semgrep scan --config=auto src/

# Exclude test files
semgrep scan --config=auto --exclude='**/*Test.java' --exclude='**/test/**'
```

### Using with Maven

Run Semgrep as part of Maven build:

```bash
# Run security scan profile
mvn clean verify -Psecurity-scan

# This profile is configured in dependency-parent/pom.xml
```

## CI/CD Integration

### GitHub Actions

Create `.github/workflows/semgrep.yml`:

```yaml
name: Semgrep Security Scan

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]
  schedule:
    # Run daily at 2 AM UTC
    - cron: '0 2 * * *'

jobs:
  semgrep:
    name: Semgrep Scan
    runs-on: ubuntu-latest

    permissions:
      contents: read
      security-events: write

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Run Semgrep
        uses: semgrep/semgrep-action@v1
        with:
          config: >-
            p/security-audit
            p/owasp-top-ten
            p/java
            .semgrep/semgrep.yml
          generateSarif: true

      - name: Upload SARIF to GitHub
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: semgrep.sarif
        if: always()

      - name: Upload Semgrep results as artifact
        uses: actions/upload-artifact@v4
        with:
          name: semgrep-results
          path: semgrep.sarif
        if: always()
```

### GitLab CI

Add to `.gitlab-ci.yml`:

```yaml
semgrep-scan:
  stage: test
  image: semgrep/semgrep
  script:
    - semgrep scan --config=auto --sarif -o gl-sast-report.json
  artifacts:
    reports:
      sast: gl-sast-report.json
  rules:
    - if: $CI_COMMIT_BRANCH == "main" || $CI_MERGE_REQUEST_ID
```

### Jenkins

Add to `Jenkinsfile`:

```groovy
stage('Security Scan') {
    steps {
        sh '''
            docker run --rm -v $(pwd):/src semgrep/semgrep \
                semgrep scan --config=auto --sarif -o /src/semgrep-report.sarif
        '''

        recordIssues(
            enabledForFailure: true,
            tool: sarif(pattern: 'semgrep-report.sarif')
        )
    }
}
```

## Custom Rules

### Adding Custom Rules

1. Edit `.semgrep/semgrep.yml`
2. Add your custom rule following the pattern:

```yaml
rules:
  - id: your-rule-id
    pattern: |
      your.pattern.here()
    message: Description of the issue
    severity: ERROR|WARNING|INFO
    languages: [java]
    metadata:
      cwe: "CWE-XXX"
      owasp: "AXX:2021"
      category: security
```

### Testing Custom Rules

```bash
# Test a specific rule
semgrep scan --config=.semgrep/semgrep.yml --include='**/YourClass.java'

# Dry run to validate rules
semgrep scan --config=.semgrep/semgrep.yml --dry-run
```

## Semgrep Rulesets

### Recommended Rulesets for Spring Boot

1. **p/security-audit**: Comprehensive security checks
2. **p/owasp-top-ten**: OWASP Top 10 vulnerabilities
3. **p/java**: Java-specific best practices
4. **p/spring**: Spring Framework security rules
5. **p/sql-injection**: SQL injection prevention
6. **p/xss**: Cross-site scripting prevention

### Using Multiple Rulesets

```bash
semgrep scan --config=p/security-audit --config=p/owasp-top-ten --config=.semgrep/semgrep.yml
```

## Interpreting Results

### Severity Levels

- **ERROR**: Critical security issues that must be fixed
- **WARNING**: Potential security issues that should be reviewed
- **INFO**: Best practice violations or code quality issues

### Common Findings

1. **SQL Injection**: Use parameterized queries
2. **Command Injection**: Validate user input before executing commands
3. **Path Traversal**: Sanitize file paths
4. **Weak Cryptography**: Use modern, strong algorithms
5. **Hardcoded Secrets**: Use environment variables or secret management
6. **CSRF Disabled**: Enable CSRF protection for state-changing operations

## Suppressing False Positives

### Inline Suppression

```java
// nosemgrep: rule-id
String password = getPasswordFromSecureStore();

// Or with explanation
// nosemgrep: sql-injection-jdbctemplate - Using trusted internal query
jdbcTemplate.query("SELECT * FROM " + tableName, handler);
```

### File-level Suppression

Create `.semgrepignore`:

```
# Ignore test files
**/test/**
**/*Test.java

# Ignore generated code
**/generated/**
**/target/**

# Ignore specific files
src/main/java/com/example/LegacyCode.java
```

## Baseline and Incremental Scanning

### Create Baseline

```bash
# Scan and save results as baseline
semgrep scan --config=auto --baseline-commit=main
```

### Scan Only New Issues

```bash
# Compare against main branch
semgrep scan --config=auto --baseline-commit=origin/main
```

## Integration with SonarQube

Convert Semgrep results to SonarQube format:

```bash
# Generate SARIF report
semgrep scan --config=auto --sarif -o semgrep.sarif

# Import to SonarQube using sonar-scanner
sonar-scanner \
  -Dsonar.projectKey=your-project \
  -Dsonar.sarifReportPaths=semgrep.sarif
```

## Pre-commit Hook

Create `.git/hooks/pre-commit`:

```bash
#!/bin/bash

echo "Running Semgrep security scan..."

# Run semgrep on staged files
git diff --cached --name-only --diff-filter=ACM | grep '\.java$' | xargs semgrep scan --config=auto --error

if [ $? -ne 0 ]; then
    echo "Semgrep found security issues. Please fix them before committing."
    exit 1
fi
```

Make it executable:

```bash
chmod +x .git/hooks/pre-commit
```

## Performance Optimization

### Faster Scans

```bash
# Scan only changed files
semgrep scan --config=auto $(git diff --name-only origin/main)

# Use fewer rules for faster feedback
semgrep scan --config=p/security-audit --exclude='**/test/**'

# Enable parallel processing
semgrep scan --config=auto --jobs=4
```

### Caching

Semgrep automatically caches rule downloads. To clear cache:

```bash
rm -rf ~/.semgrep/cache
```

## Monitoring and Reporting

### Generate HTML Report

```bash
semgrep scan --config=auto --json | \
  jq -r '.results[] | "\(.path):\(.start.line) - \(.check_id): \(.extra.message)"' \
  > semgrep-report.txt
```

### Metrics

Track security findings over time:

```bash
# Count findings by severity
semgrep scan --config=auto --json | jq '[.results[] | .extra.severity] | group_by(.) | map({severity: .[0], count: length})'
```

## Best Practices

1. **Run Semgrep in CI/CD**: Catch issues before they reach production
2. **Use Multiple Rulesets**: Combine default and custom rules
3. **Review Warnings**: Don't ignore warnings; they often indicate real issues
4. **Update Regularly**: Keep Semgrep and rules up to date
5. **Educate Team**: Share findings and teach secure coding practices
6. **Baseline Existing Code**: Focus on new code while addressing technical debt
7. **Integrate with IDEs**: Use Semgrep extensions for real-time feedback
8. **Document Suppressions**: Always explain why a finding is suppressed

## IDE Integration

### VS Code

Install the Semgrep extension:

1. Open VS Code
2. Go to Extensions (Ctrl+Shift+X)
3. Search for "Semgrep"
4. Install the official Semgrep extension

### IntelliJ IDEA

Install the Semgrep plugin:

1. File → Settings → Plugins
2. Search for "Semgrep"
3. Install and restart IDE

## Troubleshooting

### Common Issues

1. **Semgrep not found**
   ```bash
   # Verify installation
   which semgrep
   semgrep --version
   ```

2. **Rules not loading**
   ```bash
   # Clear cache and retry
   rm -rf ~/.semgrep/cache
   semgrep scan --config=auto
   ```

3. **Too many findings**
   ```bash
   # Start with ERROR severity only
   semgrep scan --config=auto --severity=ERROR

   # Gradually address findings and lower threshold
   ```

## Resources

- [Semgrep Documentation](https://semgrep.dev/docs/)
- [Semgrep Rule Registry](https://semgrep.dev/r)
- [Writing Custom Rules](https://semgrep.dev/docs/writing-rules/overview/)
- [Semgrep Playground](https://semgrep.dev/playground/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CWE Database](https://cwe.mitre.org/)

## Support

For issues with custom rules or configuration:
1. Check rule syntax in [Semgrep Playground](https://semgrep.dev/playground/)
2. Review [official documentation](https://semgrep.dev/docs/)
3. Join [Semgrep Community Slack](https://go.semgrep.dev/slack)

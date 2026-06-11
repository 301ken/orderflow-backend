# Contributing to Orderflow API

Thank you for your interest in contributing to the Orderflow API! This document provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Help keep discussions focused and productive

## Getting Started

1. **Fork the repository** and clone your fork locally
2. **Create a feature branch**: `git checkout -b feature/your-feature-name`
3. **Set up the development environment**:
   ```bash
   ./mvnw clean install
   cp .env.example .env
   # Add your local configuration to .env
   ```
4. **Ensure tests pass**: `./mvnw test`

## Making Changes

### Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Keep methods focused and concise
- Add comments for complex logic
- Use consistent formatting

### Testing

- Write unit tests for new features and bug fixes
- Ensure all tests pass: `./mvnw test`
- Aim for >80% code coverage
- Test edge cases and error scenarios
- Test with H2 (default) and PostgreSQL if database-related

### Commits

- Write clear, descriptive commit messages
- Use present tense: "Add feature" not "Added feature"
- Reference issues where applicable: "Fix #123"
- Keep commits logically organized

## Submitting Changes

1. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create a Pull Request** on GitHub with:
   - Clear title describing the change
   - Description of what changed and why
   - Reference to related issues
   - Screenshot/demo if UI-related

3. **Code Review**:
   - Respond to reviewer feedback promptly
   - Keep the PR focused (don't mix unrelated changes)
   - Update based on feedback

4. **Merge**:
   - Ensure all CI checks pass
   - Squash commits if requested
   - Delete branch after merge

## Project Structure

```
src/main/java/com/example/orderflow/
├── controller/      # REST endpoints
├── service/        # Business logic (with interface-based designs)
├── repository/     # Data access
├── entity/        # JPA entities
├── security/      # JWT & Spring Security
├── exception/     # Error handling
├── config/        # Spring configuration
└── util/          # Helper utilities
```

## Common Development Tasks

### Run the app locally
```bash
# With H2 (no setup required)
./mvnw spring-boot:run
# Runs on http://localhost:8080

# With Docker Compose (PostgreSQL)
docker compose up --build
```

### Run tests with coverage
```bash
./mvnw verify
# Coverage report: target/site/jacoco/index.html
```

### Build Docker image
```bash
docker build -t orderflow-api .
docker run -p 8080:8080 orderflow-api
```

### Access API documentation
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- H2 Console: http://localhost:8080/h2-console (when using H2)

## Integration Configuration

When adding new integrations (Stripe, Firebase, AWS S3):

1. **Use the interface-based pattern** (see existing PaymentService)
2. **Create a noop/fallback implementation** for disabled state
3. **Add configuration flag** (`*_ENABLED`) in `application.properties`
4. **Make it testable** without real credentials
5. **Document** in README with env var requirements

## Security Considerations

- Never commit sensitive data (API keys, credentials)
- Use `.env` and `.env.example` for configuration
- Always add `.env` to `.gitignore`
- Rotate JWT secrets before production deployment
- Validate all user input
- Use parameterized queries (Spring Data JPA handles this)

## Reporting Issues

Please use GitHub Issues to report bugs or request features:

- **Bug Report**: Describe the issue, steps to reproduce, and expected behavior
- **Feature Request**: Explain the use case and proposed solution
- **Include relevant details**: Java version, OS, error messages, logs

## Questions?

- Check existing issues and PRs first
- Open a discussion if unsure
- Ask for clarification in PR comments

---

Happy contributing! 🎉

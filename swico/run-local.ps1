$env:SPRING_DATASOURCE_PASSWORD = "123456"
$env:JWT_SECRET = "swico-local-dev-secret-change-me-2026-long-key"

.\mvnw.cmd spring-boot:run

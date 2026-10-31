# Task Tracker

Manage team resources and dated tasks.

## Requirements
- JDK 25
- Maven 3.6.3 or newer
- Internet access on first build to download Maven dependencies and Bootstrap CDN assets

## Run
```bash
mvn clean spring-boot:run
```
Open: http://localhost:8080

## Important
This project intentionally uses Java in-memory collections, not MySQL, PostgreSQL, H2, or another database.
Data is lost when the application stops or restarts. Bootstrap is loaded from a CDN.

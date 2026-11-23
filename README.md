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



## Jenkins Webhook and S3 Automation

This project uses Jenkins to build Maven artifacts and upload them to AWS S3.


## Jenkins Webhook and S3 Automation wih latest version chnage

This project uses Jenkins to build Maven artifacts and upload them to AWS S3.Where we will create always new version.

## Jenkins Webhook and S3 Automation wih latest version chnage-2

This project uses Jenkins to build Maven artifacts and upload them to AWS S3.Where we will create always new version.-2

## Jenkins Webhook and S3 Automation wih latest version chnage-4

This project uses Jenkins to build Maven artifacts and upload them to AWS S3.Where we will create always new version.-4
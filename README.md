# property-service

Java 21 / Spring Boot 3.4.5. Open this folder as a Maven project in STS or IntelliJ. Configure
`src/main/resources/application.properties`, then run `mvn spring-boot:run`.

Port: 8082. Each business service follows MVC layers: entity/model, repository, service, controller, DTO/config as
applicable. This is a corrected starter and should be integration-tested before production deployment.

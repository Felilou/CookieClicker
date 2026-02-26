FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x ./mvnw
COPY service-discovery/pom.xml service-discovery/pom.xml
RUN ./mvnw -f service-discovery/pom.xml dependency:go-offline -q
COPY service-discovery/src service-discovery/src
RUN ./mvnw -f service-discovery/pom.xml package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/service-discovery/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

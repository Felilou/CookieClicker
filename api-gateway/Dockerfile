FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x ./mvnw
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN ./mvnw -f api-gateway/pom.xml dependency:go-offline -q
COPY api-gateway/src api-gateway/src
RUN ./mvnw -f api-gateway/pom.xml package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

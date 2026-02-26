FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x ./mvnw
COPY persistance-service/pom.xml persistance-service/pom.xml
RUN ./mvnw -f persistance-service/pom.xml dependency:go-offline -q
COPY persistance-service/src persistance-service/src
RUN ./mvnw -f persistance-service/pom.xml package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/persistance-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

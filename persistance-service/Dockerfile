FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x ./mvnw

# Parent POM + common-lib zuerst bauen & installieren
COPY pom.xml pom.xml
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN ./mvnw -N install -DskipTests -q && \
    ./mvnw -f common-lib/pom.xml install -DskipTests -q

# Service bauen
COPY persistance-service/pom.xml persistance-service/pom.xml
RUN ./mvnw -f persistance-service/pom.xml dependency:go-offline -q
COPY persistance-service/src persistance-service/src
RUN ./mvnw -f persistance-service/pom.xml package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/persistance-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

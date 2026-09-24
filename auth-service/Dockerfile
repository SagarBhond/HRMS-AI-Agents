FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
COPY . .

RUN sed -i '/<extensions>/d; /<\/extensions>/d; /<extension>/d; /<\/extension>/d; /com.google.cloud.artifactregistry/d; /artifactregistry-maven-wagon/,+1d' pom.xml \
    && chmod +x ./mvnw \
    && ./mvnw -B -ntp -pl auth-service -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/auth-service/target/*.jar app.jar
RUN useradd --create-home --uid 1000 appuser \
    && chown -R appuser:appuser /app
USER appuser

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

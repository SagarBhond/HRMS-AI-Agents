FROM eclipse-temurin:21-jdk-jammy AS builder

ARG APP_MODULE
WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
COPY . .

RUN test -n "${APP_MODULE}" \
    && sed -i '/<extensions>/d; /<\/extensions>/d; /<extension>/d; /<\/extension>/d; /com.google.cloud.artifactregistry/d; /artifactregistry-maven-wagon/,+1d' pom.xml \
    && chmod +x ./mvnw \
    && ./mvnw -B -ntp -Dmaven.wagon.http.retryHandler.count=4 -Dmaven.wagon.httpconnectionManager.ttlSeconds=60 -Dmaven.wagon.httpconnectionManager.maxPerRoute=8 -Dmaven.wagon.httpconnectionManager.maxTotal=16 -pl "${APP_MODULE}" -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy

ARG APP_MODULE
WORKDIR /app

COPY --from=builder /app/${APP_MODULE}/target/*.jar app.jar
RUN useradd --create-home --uid 1000 appuser \
    && chown -R appuser:appuser /app
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]

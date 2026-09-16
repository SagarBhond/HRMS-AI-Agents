# =============================================================================
# Generic multi-stage Dockerfile for EVERY HRMS-AI-Agents Java service.
# One file builds all 19 services (auth-service, mcp-server, orchestrator-agent,
# hr-agent, employee-agent, attendance-agent, payroll-agent, leave-agent,
# manager-agent, document-agent, notification-agent, policy-agent,
# expense-agent, asset-agent, performance-agent, recruitment-agent,
# audit-agent, compliance-agent, workflow-agent).
#
# Build context MUST be the repository ROOT (where the parent pom.xml lives),
# because every module is a Maven reactor child that depends on the
# SNAPSHOT artifacts "google-adk" (core) and "google-adk-a2a" (a2a) which are
# not published to Maven Central and must be built locally first.
#
# Build:
#   docker build \
#     --build-arg SERVICE=hr-agent \
#     --build-arg JAR_NAME=hr-agent.jar \
#     --build-arg SERVICE_PORT=8088 \
#     -t hrms/hr-agent:latest -f backend/Dockerfile .
#
# Run:
#   docker run -p 8088:8088 \
#     -e GOOGLE_API_KEY=xxx \
#     -e HRMS_DATASOURCE_URL=jdbc:mysql://mysql:3306/hrms_db \
#     hrms/hr-agent:latest
# =============================================================================

######################## STAGE 1 — build ########################
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Which module to package. Must match a top-level folder name in the repo.
ARG SERVICE=hr-agent

WORKDIR /workspace

# ---- Dependency layer cache -------------------------------------------------
# Copy only POM files first so Docker can cache the `go-offline` layer and
# skip re-downloading ~all of Maven Central on every source change.
COPY pom.xml .
COPY core/pom.xml core/pom.xml
COPY a2a/pom.xml a2a/pom.xml
COPY ${SERVICE}/pom.xml ${SERVICE}/pom.xml

# NOTE: the reactor needs *a* valid module list to resolve the parent, so we
# copy the full source in the next step; this two-step COPY still gives you
# real cache hits whenever only source files (not POMs) change.
COPY . .

# Build only what this service actually needs: core -> a2a -> service,
# using "-am" (also-make) so upstream reactor deps are built in order.
# -q keeps CI logs readable; drop it while debugging locally.
RUN mvn -B -q -pl core,a2a,${SERVICE} -am clean package -DskipTests \
    && cp ${SERVICE}/target/*.jar /tmp/app.jar

######################## STAGE 2 — runtime ########################
FROM eclipse-temurin:21-jre-alpine AS runtime

ARG SERVICE=hr-agent
ARG SERVICE_PORT=8080
ENV SERVICE_NAME=${SERVICE} \
    SERVER_PORT=${SERVICE_PORT} \
    JAVA_OPTS=""

RUN apk add --no-cache wget

# Run as non-root
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar
RUN chown -R spring:spring /app
USER spring

EXPOSE ${SERVICE_PORT}

# Container-level healthcheck (requires spring-boot-starter-actuator;
# harmless no-op if actuator isn't on the classpath yet — swap for a TCP
# check if you haven't added actuator to a given module).
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

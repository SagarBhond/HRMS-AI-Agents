FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
RUN useradd --create-home --uid 1000 appuser
COPY --chown=appuser:appuser payroll-agent/target/payroll-agent.jar app.jar
USER appuser

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

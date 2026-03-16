# syntax=docker/dockerfile:1

## Multi-stage build for the `application` module (Spring Boot)
## Build context is the repository root.
##
## Build:
##   docker build -t deposition/application:local .

FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

# Maven official images may not provide a Temurin 25 tag yet, so we install Maven ourselves.
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

# Copy the whole repository to build the multi-module project.
# (The `application` module depends on `domain` and `infra-*` modules.)
COPY . .

# Build only the `application` module (and required dependencies).
RUN mvn -B -pl application -am package -DskipTests \
    && APP_JAR=$(ls application/target/application-*.jar | grep -v '\\.original$' | head -n 1) \
    && cp "$APP_JAR" /workspace/app.jar

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /workspace/app.jar /app/app.jar

# The application expects configuration via environment variables (see application/src/main/resources/application.yml).
EXPOSE 8080

# For runtime JVM flags (heap, GC, etc.) prefer setting JAVA_TOOL_OPTIONS.
# It is natively supported by the JVM and works well with Docker exec-form entrypoint.
ENV JAVA_TOOL_OPTIONS=""

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

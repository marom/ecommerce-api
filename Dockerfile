# syntax=docker/dockerfile:1

########################################
# Stage 1 — build & layer the fat jar
########################################
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /build

# Resolve dependencies first so this layer caches unless pom.xml changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Build. lombok.config carries Spring's @Value/@Qualifier onto the constructor
# params @RequiredArgsConstructor generates — without it, @Value on a final field
# is silently dropped and the security beans fail to start.
COPY lombok.config ./
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q clean package -DskipTests

# Explode the executable jar into layers (dependencies change less often than app code)
RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

########################################
# Stage 2 — minimal runtime image
########################################
FROM eclipse-temurin:21-jre-noble AS runtime

# curl is only needed for the container HEALTHCHECK below
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --system --gid 1001 app && \
    useradd --system --uid 1001 --gid app --home-dir /app --shell /usr/sbin/nologin app

WORKDIR /app

# Copy layers least-likely-to-change first for better image-layer reuse
COPY --from=build --chown=app:app /build/extracted/dependencies/ ./
COPY --from=build --chown=app:app /build/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /build/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /build/extracted/application/ ./

USER app

EXPOSE 8080

# Pass tuning via JAVA_TOOL_OPTIONS (honoured by the JVM without a shell wrapper),
# e.g. -e JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

# Spring Boot Actuator exposes /actuator/health over HTTP by default:
# 200 when UP, 503 otherwise — so `curl -f` alone is a sufficient probe.
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -fsS -o /dev/null http://localhost:8080/actuator/health || exit 1

# `extract --launcher` produces an exploded layout (BOOT-INF/, org/springframework/loader…),
# started via the Spring Boot launcher rather than `-jar`.
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

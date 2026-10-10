# Multi-Stage Production Dockerfile for OmniRecommend AI Platform
# Stage 1: Build & Compile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy dependency libraries and source tree
COPY lib/ ./lib/
COPY src/ ./src/

# Compile all Java sources
RUN mkdir -p bin && \
    javac -cp "lib/*" -d bin $(find src -name "*.java")

# Stage 2: Production Lightweight Runtime
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Aman Yadav <https://github.com/amanyadavaa>" \
      description="OmniRecommend AI - Universal AI-Based Recommendation System" \
      version="1.0.0"

WORKDIR /app

# Create unprivileged application user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled classes, libraries, and static web assets
COPY --from=builder /build/bin ./bin
COPY lib/ ./lib/
COPY web/ ./web/

# Setup persistent data directory for SQLite
RUN mkdir -p /app/data && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Container healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-cp", "bin:lib/*", "com.guvi.recommend.Main", "8080"]

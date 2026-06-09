FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM gradle:8.7-jdk17 AS backend-build
WORKDIR /app/backend
COPY backend/build.gradle.kts backend/settings.gradle.kts ./
COPY backend/src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN gradle bootJar -x test -x buildFrontend -x copyFrontend --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/build/libs/resumeradar.jar ./app.jar
RUN mkdir -p /app/data
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
CMD ["java", "-jar", "app.jar"]

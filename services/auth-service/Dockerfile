FROM gradle:9.2-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew :auth-service:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /opt/iwawka
COPY --from=build /app/services/auth-service/build/install/auth-service ./
EXPOSE 8001
CMD ["bin/auth-service"]

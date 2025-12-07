FROM gradle:9.2-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /opt/iwawka
COPY --from=build /app/build/install/iwawka-backend ./
EXPOSE 8080
CMD ["bin/iwawka-backend"]

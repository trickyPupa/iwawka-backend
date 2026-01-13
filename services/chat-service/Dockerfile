FROM gradle:9.2-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :chat-service:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /opt/iwawka
COPY --from=build /app/services/chat-service/build/install/chat-service ./
EXPOSE 8002
CMD ["bin/chat-service"]

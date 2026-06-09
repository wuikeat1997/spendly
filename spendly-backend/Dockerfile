FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spendly && adduser -S spendly -G spendly

COPY --from=build /workspace/build/libs/spendly-backend.jar /app/spendly-backend.jar

USER spendly

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/spendly-backend.jar"]

# Build the application
FROM gradle:9-jdk21-ubi9 AS build

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

ENV ORG_GRADLE_PROJECT_githubPackagesUsername=$GITHUB_ACTOR
ENV ORG_GRADLE_PROJECT_githubPackagesPassword=$GITHUB_TOKEN

WORKDIR /app
COPY . .
RUN ./gradlew startShadowScript --no-daemon

# Build the runtime container
FROM eclipse-temurin:21-jre-ubi9-minimal

WORKDIR /app
COPY --from=build /app/build/libs/revanced-api-*.jar revanced-api.jar
CMD java -jar revanced-api.jar $COMMAND

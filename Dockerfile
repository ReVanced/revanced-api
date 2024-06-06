# Build the application
FROM gradle:latest AS build

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

ENV GITHUB_ACTOR $GITHUB_ACTOR
ENV GITHUB_TOKEN $GITHUB_TOKEN

WORKDIR /app
COPY . .
RUN gradle startShadowScript --no-daemon

# Build the runtime container
FROM eclipse-temurin:latest

WORKDIR /app
COPY --from=build /app/build/libs/revanced-api-*.jar revanced-api.jar
CMD java -jar revanced-api.jar $COMMAND
